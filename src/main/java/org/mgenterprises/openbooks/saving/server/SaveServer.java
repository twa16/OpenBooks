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
package org.mgenterprises.openbooks.saving.server;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.mgenterprises.openbooks.saving.AbstractSaveableAdapter;
import org.mgenterprises.openbooks.saving.EqualityOperation;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.saving.server.access.ACTION;
import org.mgenterprises.openbooks.saving.server.authentication.UserLoginAttempt;
import org.mgenterprises.openbooks.saving.server.journal.ChangeRecord;
import org.mgenterprises.openbooks.saving.server.journal.ChangeJournal;
import org.mgenterprises.openbooks.saving.server.security.CryptoUtils;
import org.mgenterprises.openbooks.saving.server.security.SecureMessage;
import org.mgenterprises.openbooks.saving.server.users.UserManager;

/**
 * SaveServer manages authentication and controls access to data
 *
 * @author mgauto
 */
public class SaveServer implements Runnable {

    /**
     * Delimiter used for requests to the SaveServer
     */
    public static final String DELIMITER = ":#:";
    /**
     * Address to listen on
     */
    private InetAddress bindAddress;
    /**
     * Port to listen on
     */
    private short port;
    /**
     * Controls the main loop of the server. If false, the server will stop
     * processing requests and end
     */
    private boolean running = true;

    /**
     * GSON instance used to process packets
     */
    private Gson gson;
    private UserManager userManager;
    private SaveManager saveManager;
    private ChangeJournal changeJournal;
    private SecureRandom secureRandom = new SecureRandom();

    private String keyStoreLocation;
    private char[] keyStorePassword;
    public SaveServer(String listenAddress, short port, UserManager userManager, SaveManager saveManager, String keyStoreLocation, char[] keyStorePassword) throws UnknownHostException {
        this.port = port;
        this.bindAddress = InetAddress.getByName(listenAddress);
        this.userManager = userManager;
        this.saveManager = saveManager;
        this.changeJournal = new ChangeJournal(saveManager);
        this.keyStoreLocation = keyStoreLocation;
        this.keyStorePassword = keyStorePassword;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        //gsonBuilder.registerTypeAdapter(Saveable[].class, new AbstractSaveableArrayAdapter());
        gson = gsonBuilder.create();
    }

    public void startServer() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keyStoreLocation), keyStorePassword);
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyStorePassword);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
            
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);
            
            SSLServerSocketFactory factory = sc.getServerSocketFactory();
            SSLServerSocket serverSocket=(SSLServerSocket) factory.createServerSocket(port);
            while (running) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                SaveServerRequestProcessor saveServerRequestProcessor = new SaveServerRequestProcessor(socket, secureRandom, gson, userManager, saveManager, changeJournal);
                new Thread(saveServerRequestProcessor).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        running = false;
    }
}

class SaveServerRequestProcessor implements Runnable {

    private SSLSocket socket;
    private SecureRandom secureRandom;
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private Gson gson;
    private UserManager userManager;
    private SaveManager saveManager;
    private ChangeJournal changeJournal;

    public SaveServerRequestProcessor(SSLSocket socket, SecureRandom secureRandom, Gson gson, UserManager userManager, SaveManager saveManager, ChangeJournal changeJournal) {
        this.socket = socket;
        this.secureRandom = secureRandom;
        this.gson = gson;
        this.userManager = userManager;
        this.saveManager = saveManager;
        this.changeJournal = changeJournal;
    }

    @Override
    public void run() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String userLoginAttemptJson = br.readLine();
            UserLoginAttempt userloginAttempt = gson.fromJson(userLoginAttemptJson, UserLoginAttempt.class);
            String username = userloginAttempt.getUsername();
            if(userManager.checkUserLoginAttempt(userloginAttempt)) {
                //Inform the client of sucessful login
                bw.write("OK");
                bw.newLine();
                bw.flush();
                //Start the command loop
                while(true) {
                    //Wait for command
                    String request = br.readLine();
                    //Check to see if it the disconnect command
                    if(request.equals("DISCONNECT")) break;

                    //Split request string using delimiter
                    String[] requestParts = request.split(SaveServer.DELIMITER);

                    String verb = requestParts[0];
                    String response = "500";
                    switch (verb) {
                        case "GET":
                            response = processGET(username, requestParts);
                            break;
                        case "QUERY":
                            response = processQUERY(username, requestParts);
                            break;
                        case "PUT":
                            response = processPUT(username, requestParts);
                            break;
                        case "REMOVE":
                            response = processREMOVE(username, requestParts);
                            break;
                        case "SIZE":
                            response = processSIZE(username, requestParts);
                            break;
                        case "HIGHESTID":
                            response = processHIGHESTID(username, requestParts);
                            break;
                        case "LOCK":
                            response = processLOCK(username, requestParts);
                            break;
                        case "RELEASE":
                            response = processRELEASE(username, requestParts);
                            break;
                        case "READJOURNAL":
                            response = processREADJOURNAL(username, requestParts);
                            break;
                    } //Switch End
                    //Send the response
                    bw.write(response);
                    bw.newLine();
                    bw.flush();
                } //Command Loop End
                bw.close();
                br.close();
                socket.close();
            } else {
                //Inform the client of failed login
                bw.write("FAIL");
                bw.newLine();
                bw.flush();
                bw.close();
                br.close();
                socket.close();
            }
            
        } catch (IOException ex) {
            Logger.getLogger("SaveServer").log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the requested objects
     * ALL does not lock objects
     * locks object if it is not locked
     * @param user
     * @param requestParts
     * @return 
     */
    private String processGET(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];
        //Make sure user can access this
        if (userManager.userHasAccessRight(user, type, ACTION.GET)) {
            if (id.equals("ALL")) {
                Saveable[] saveables = saveManager.getAllSaveables(type);
                return gson.toJson(saveables);
            } else {
                //Get lock status
                String lockHolder = saveManager.getLockHolder(type, id);
                //If it isn't locked, lock it    
                if (lockHolder.equals("")) {
                    saveManager.createLock(user, type, id);
                    //Set lockholder to current user
                    lockHolder=user;
                }
                Saveable saveable = saveManager.getSaveable(type, id);

                //Check if the GET was successful
                if (saveable != null) {
                    //If it is locked by another user indicate that
                    if (!lockHolder.equals(user)) {
                        saveable.setLocked(true);
                    } else {
                        saveable.setLocked(false);
                    }
                    Logger.getLogger("SaveServer").log(Level.INFO, "GET from {0} for t: {1} i: {2}", new Object[]{user, type, id});
                    return gson.toJson(saveable, Saveable.class);
                } else {
                    Logger.getLogger("SaveServer").log(Level.INFO, "GET from {0} for t: {1} i: {2}", new Object[]{user, type, id});
                    saveManager.removeLock(type, id);
                    return "404";
                }
            }
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied GET from {0} for t: {1} i: {2}", new Object[]{user, type, id});
            return "401";
        }
    }

    private String processQUERY(String user, String[] requestParts) {
        String type = requestParts[1];
        String[] keys = fromArraysToString(requestParts[2]);
        EqualityOperation[] operations = fromArraysToStringEO(requestParts[3]);
        String[] values = fromArraysToString(requestParts[4]);
        String[] conjunctions = fromArraysToString(requestParts[5]);
        
        boolean arraysOK = keys.length==operations.length&&operations.length==values.length&&values.length==conjunctions.length;
        boolean tryLockAll = Boolean.getBoolean(requestParts[6]);
        //Make sure user can access this
        if (userManager.userHasAccessRight(user, type, ACTION.GET) || !arraysOK) {
            Saveable[] result = saveManager.getWhere(type, keys, operations,values, conjunctions);
            for(Saveable saveable : result) {
                boolean isLockedForUser = saveManager.isLockedForUser(user, type, saveable.getUniqueId());
                if(!isLockedForUser && tryLockAll) {
                    saveManager.createLock(user, type, saveable.getUniqueId());
                } else if(isLockedForUser) {
                    saveable.setLocked(true);
                }
            }
            Logger.getLogger("SaveServer").log(Level.INFO, "QUERY from {0} for t: {1} keys: {2}", new Object[]{user, type, requestParts[3]});
            return gson.toJson(result, Saveable.class);
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied QUERY from {0} for t: {1} keys: {2}", new Object[]{user, type, requestParts[3]});
            return "401";
        }
    }

    private static String[] fromArraysToString(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        return strings;
    }
    
    private static EqualityOperation[] fromArraysToStringEO(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        EqualityOperation[] ops = new EqualityOperation[strings.length];
        for(int i = 0; i < strings.length; i++) {
            String s = strings[i];
            for(EqualityOperation eo : EqualityOperation.values()) {
                if(eo.toString().equals(s)) ops[i]=eo;
            }
        }
        return ops;
    }

    private String processSIZE(String user, String[] requestParts) {
        String type = requestParts[1];
        if (userManager.userHasAccessRight(user, type, ACTION.GET)) {
            Logger.getLogger("SaveServer").log(Level.INFO, "SIZE from {0} for t: {1}", new Object[]{user, type});
            return String.valueOf(saveManager.getSaveableCount(type));
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied SIZE from {0} for t: {1}", new Object[]{user, type});
            return "401";
        }
    }

    public String processHIGHESTID(String user, String[] requestParts) {
        String type = requestParts[1];
        if (userManager.userHasAccessRight(user, type, ACTION.GET)) {
            Logger.getLogger("SaveServer").log(Level.INFO, "HIGHESTID from {0} for t: {1}", new Object[]{user, type});
            return String.valueOf(saveManager.getHighestUniqueId(type));
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied HIGHESTID from {0} for t: {1}", new Object[]{user, type});
            return "401";
        }
    }
    
    private String processPUT(String user, String[] requestParts) {
        String data = requestParts[1];

        JsonParser jsonParser = new JsonParser();
        JsonElement json = jsonParser.parse(data);
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        Saveable saveable = gson.fromJson(data, Saveable.class);
        if (userManager.userHasAccessRight(user, saveable.getSaveableModuleName(), ACTION.PUT)) {
            if (!saveManager.isLockedForUser(user, saveable.getSaveableModuleName(), saveable.getUniqueId())) {
                saveManager.persistSaveable(type, user, saveable);
                ChangeRecord change = new ChangeRecord();
                change.setType(type);
                change.setObjectId(saveable.getUniqueId());
                long changeID = changeJournal.recordChange(change);
                Logger.getLogger("SaveServer").log(Level.INFO, "PUT from {0}", new Object[]{user});
                return String.valueOf(changeID);
            } else {
                Logger.getLogger("SaveServer").log(Level.INFO, "Denied PUT from {0}-Locked", new Object[]{user});
                return "-1";
            }
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied PUT from {0}", new Object[]{user});
            return "-1";
        }
    }

    private String processREMOVE(String user, String[] requestParts) {
        if (requestParts.length != 3) {
            return "404";
        }
        String type = requestParts[1];
        String id = requestParts[2];
        if (userManager.userHasAccessRight(user, type, ACTION.REMOVE)) {
            if (saveManager.getLockHolder(type, id).equals(user)) {
                saveManager.removeSaveable(type, id);
                Logger.getLogger("SaveServer").log(Level.INFO, "REMOVE from {0} for {1} {2}", new Object[]{user, type, id});
                return "200";
            } else {
                Logger.getLogger("SaveServer").log(Level.INFO, "Denied REMOVE from {0} for {1} {2}", new Object[]{user, type, id});
                return "503";
            }
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied REMOVE from {0} for {1} {2}", new Object[]{user, type, id});
            return "401";
        }
    }

    private String processRELEASE(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];
        if (saveManager.getLockHolder(type, id).equals(user)) {
            saveManager.removeLock(type, id);
            Logger.getLogger("SaveServer").log(Level.INFO, "RELEASE from {0} for {1} {2}", new Object[]{user, type, id});
            return "200";
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied RELEASE from {0} for {1} {2}", new Object[]{user, type, id});
            return "401";
        }
    }

    private String processLOCK(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];

        String lockHolder = saveManager.getLockHolder(type, id);
        if (lockHolder.equals("")) {
            saveManager.createLock(user, type, id);
            Logger.getLogger("SaveServer").log(Level.INFO, "LOCK from {0} for {1} {2}", new Object[]{user, type, id});
            return "200";
        } else if (lockHolder.equals(user)) {
            Logger.getLogger("SaveServer").log(Level.INFO, "LOCK CHECK from {0} for {1} {2}", new Object[]{user, type, id});
            return "302";
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied LOCK from {0} for {1} {2}", new Object[]{user, type, id});
            return "401";
        }
    }
    
    private String processREADJOURNAL(String user, String[] requestParts) {
        String startIdString = requestParts[1];
        try {
            if(startIdString.equalsIgnoreCase("SIZE")) {
                return String.valueOf(changeJournal.getLatestChangeId());
            }
            long startId = Long.parseLong(startIdString);
            ChangeRecord[] changes = changeJournal.getChangesSince(startId);
            return gson.toJson(changes);
        } catch(NumberFormatException ex) {
            Logger.getLogger("SaveServer").log(Level.INFO, "Invalid READJOURNAL requestfrom {0}", new Object[]{user});
            return "400";
        }
        
    }

}
