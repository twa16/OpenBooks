/*
 * The MIT License
 *
 * Copyright 2014 Manuel Gauto.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.mgenterprises.openbooks.saving;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mgenterprises.openbooks.saving.server.SaveServer;
import org.mgenterprises.openbooks.saving.server.journal.ChangeRecord;
import org.mgenterprises.openbooks.saving.server.security.CryptoUtils;
import org.mgenterprises.openbooks.saving.server.security.SecureMessage;

/**
 * Allows easy access to the OpenBooks storage backend.
 * We could technically integrate directly with MYSQL or some other SQL backend
 * but that presents some challenges. The main thing that swayed my decision was that
 * some popular databases do not encrypt communication by default which is a no-no 
 * with financial software. Secondly, I was much happier with the idea of having a
 * different system double checking the locks and access since we can't 100 percent
 * trust the client.
 * 
 * @author mgauto
 */
public class ServerBackedMap<V extends Saveable> {
    private V v;
    private String serverAddress;
    private short serverPort;
    private String username;
    private String passwordHash;
    private Gson gson;
    private byte[] salt;
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private ArrayList<String> lockedIDs = new ArrayList<String>(); 
    
    private HashMap<String, V> cache = new HashMap<String, V>();
    long lastJournalId = 0;

    /**
     * Default Constructor 
     * 
     * @param v Instance of the type that this map will be managing
     * @param saveServerConnection Connection data for link to server
     */
    public ServerBackedMap(V v, SaveServerConnection saveServerConnection) {
        this.v = v;
        this.serverAddress = saveServerConnection.getServerAddress();
        this.serverPort = saveServerConnection.getServerPort();
        this.username = saveServerConnection.getUsername();
        this.passwordHash = saveServerConnection.getPasswordHash();//Hashing.sha256().hashString(saveServerConnection.getPasswordHash()+System.currentTimeMillis(), Charsets.UTF_8).toString();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        //gsonBuilder.registerTypeAdapter(Saveable[].class, new AbstractSaveableArrayAdapter());
        gson = gsonBuilder.create();
        salt = cryptoUtils.getSalt(new SecureRandom());
    }
    
    /**
     * Check if the specified key exists and if the user is allowed to edit it.
     * The user is allowed to edit it only if there is no lock on the item or
     * if the user is the holder of the lock
     * 
     * @param key Key to check access to
     * @return true is the key exists and the user is able to edit
     * @throws IOException Thrown if there is a problem connecting to the server
     */
    public boolean existsAndAllowed(String key) throws IOException {
        return get(key)!=null;
    }
    
    /**
     * Persists the specified value to the database.
     * The object will overwrite any object that is already persisted that has the same key and type.
     * 
     * @param value Object that will be persisted in the database
     * @return ChangeRecord Id that is associated with this request or -1 if it failed
     * @throws IOException Thrown if there is a problem connecting to the server
     */
    public long put(V value) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "PUT"+SaveServer.DELIMITER+gson.toJson(value, Saveable.class);
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            long lastID = Long.parseLong(json);
            if((lastID+1)==this.lastJournalId) {
                lastJournalId = lastID;
                cache.put(value.getUniqueId(), value);
            }
            return lastID;
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return -1;
    }
    
    /**
     * Retrieve a saveable with the given key. The key only has to be unique with the scope of the type
     * of the saveable. Returns null if the object was not found.
     * 
     * @param key Key to search for
     * @return Object requested or null if it does not exist
     * @throws IOException Thrown if there is a problem connecting to the server
     */
    public synchronized V get(String key) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "GET"+SaveServer.DELIMITER+v.getSaveableModuleName()+SaveServer.DELIMITER+key;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            if(json.equals("NO")) {
                return null;
            }
            try {
                Saveable saveable = gson.fromJson(json, Saveable.class);
                this.lockedIDs.add(v.getSaveableModuleName()+":#:"+key);
                return (V) saveable;
            }
            catch(JsonSyntaxException ex) {
                return null;
            }
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return null;
    }
    
    /**
     * This method is used to generate custom queries to retrieve a set of saveables.
     * The queries are built from keys, operations, values and conjunctions. These parts
     * are combined by the {@link SaveManager} implementation to retrieve the objects
     * requested. 
     * 
     * @param keys Keys to use in the WHERE query
     * @param operations Determines how the key should be compared with the value
     * @param values values to check for
     * @param conjunctions Determines the logical links between the key/value comparisons(AND, OR)
     * @param tryLockAll Determines whether or not the objects retrieved should be locked.
     * @return
     * @throws IOException  Thrown if there is a problem connecting to the server
     */
    public synchronized V[] getWhere(String[] keys, EqualityOperation[] operations, String[] values, String[] conjunctions, boolean tryLockAll) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "GETWHERE"+SaveServer.DELIMITER+v.getSaveableModuleName()+SaveServer.DELIMITER+Arrays.toString(keys)+SaveServer.DELIMITER+Arrays.toString(operations)+SaveServer.DELIMITER+Arrays.toString(values)+SaveServer.DELIMITER+Arrays.toString(conjunctions)+SaveServer.DELIMITER+tryLockAll;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            if(json.equals("NO")) {
                return null;
            }
            try {
                Saveable[] saveable = gson.fromJson(json, Saveable[].class);
                return (V[]) saveable;
            }
            catch(JsonSyntaxException ex) {
                return null;
            }
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return null;
    }
    
    /**
     * Returns all the values stored of the given type.
     * 
     * @return ArrayList of the values of the given type
     * @throws IOException Thrown if there is a problem connecting to the server 
     */
    public ArrayList<V> values() throws IOException {
        if(cache.isEmpty()) {
            this.lastJournalId = this.getLatestChangeRecordId();
            for(V v : primeCache()) {
                cache.put(v.getUniqueId(), v);
            }
        } else {
            applyChanges();
        }
        return new ArrayList<V>(cache.values());
    }
    
    
    private ArrayList<V> primeCache() throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "GET"+SaveServer.DELIMITER+v.getSaveableModuleName()+SaveServer.DELIMITER+"ALL";
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            if(json.equals("NO")) {
                return null;
            }
            Saveable[] saveables = gson.fromJson(json, Saveable[].class);
            ArrayList<V> saveablesList = new ArrayList<V>(saveables.length);
            for(Saveable saveable : saveables) {
                saveablesList.add((V)saveable);
            }
            return saveablesList;
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return null;
    }
    
    /**
     * Returns an long representing how many objects are stored
     * 
     * @return Count of objects stored
     * @throws IOException IOException Thrown if there is a problem connecting to the server 
     */
    public long size() throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "SIZE"+SaveServer.DELIMITER+v.getSaveableModuleName();
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            return Long.parseLong(json);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return -1;
    }
    
    /**
     * 
     * @param key
     * @return
     * @throws IOException  IOException Thrown if there is a problem connecting to the server 
     */
    public boolean remove(String key) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "REMOVE"+SaveServer.DELIMITER+v.getSaveableModuleName()+SaveServer.DELIMITER+key;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            
            return Boolean.getBoolean(json);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return false;
    }
    
    /**
     * 
     * @throws IOException  IOException Thrown if there is a problem connecting to the server 
     */
    public synchronized void releaseAllLocks() throws IOException {
        System.out.println("Releasing locks: "+v.getSaveableModuleName());
        ArrayList<String> tempIDs = new ArrayList<String>(lockedIDs);
        for(String s : tempIDs) {
            String[] parts = s.split(":#:");
            String type = parts[0];
            String id = parts[1];
            System.out.println("      Released: "+id);
            releaseLock(type, id);
        }
    }
    
    /**
     * 
     * @param type
     * @param id
     * @return
     * @throws IOException  IOException Thrown if there is a problem connecting to the server 
     */
    public synchronized boolean tryLock(String type, String id) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "LOCK"+SaveServer.DELIMITER+type+SaveServer.DELIMITER+id;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            if(json.equals("200") || json.equals("302")) {
                this.lockedIDs.add(type+":#:"+id);
                return true;
            }
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return false;
    }
    
    /**
     * 
     * @param type
     * @param id
     * @return
     * @throws IOException  IOException Thrown if there is a problem connecting to the server 
     */
    public boolean releaseLock(String type, String id) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "RELEASE"+SaveServer.DELIMITER+type+SaveServer.DELIMITER+id;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            this.lockedIDs.remove(type+":#:"+id);
            return (json.equals("200"));
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return false;
    }
    
    private void applyChanges() throws IOException {
        ChangeRecord[] changes = getChangeRecordsSince(lastJournalId);
        for(ChangeRecord change : changes) {
            if(change.getType().equals(v.getSaveableModuleName())) {
                V changed = this.get(change.getObjectId());
                this.cache.put(changed.getUniqueId(), changed);
            }
            this.lastJournalId = change.getChangeId();
        }
    }
    
    private ChangeRecord[] getChangeRecordsSince(long id) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "READJOURNAL"+SaveServer.DELIMITER+String.valueOf(id);
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            if(json.equals("NO")) {
                return null;
            }
            System.out.println(json);
            ChangeRecord[] changes = gson.fromJson(json, ChangeRecord[].class);
            return changes;
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return null;
    }
    
    private long getLatestChangeRecordId() throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "READJOURNAL"+SaveServer.DELIMITER+"SIZE";
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            bw.flush();
            String ejson = br.readLine();
            
            SecureMessage responseSecureMessage = gson.fromJson(ejson, SecureMessage.class);
            String json = cryptoUtils.decrypt(responseSecureMessage, passwordHash);
            
            try {
                long size = Long.parseLong(json);
                return size;
            } catch(NumberFormatException ex) {
                return -1;
            }
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ServerBackedMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            bw.close();
            br.close();
        }
        return -1;
    }
}
