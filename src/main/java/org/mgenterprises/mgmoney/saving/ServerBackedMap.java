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

package org.mgenterprises.mgmoney.saving;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mgenterprises.mgmoney.saving.server.security.CryptoUtils;
import org.mgenterprises.mgmoney.saving.server.security.SecureMessage;

/**
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

    public ServerBackedMap(V v, SaveServerConnection saveServerConnection) {
        this.v = v;
        this.serverAddress = saveServerConnection.getServerAddress();
        this.serverPort = saveServerConnection.getServerPort();
        this.username = saveServerConnection.getUsername();
        this.passwordHash = saveServerConnection.getPasswordHash();
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        gson = gsonBilder.create();
    }
    
    public ServerBackedMap(V v, String serverAddress, short serverPort, String username, String passwordHash) {
        this.v = v;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
        this.passwordHash = passwordHash;
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        gson = gsonBilder.create();
    }
    
    public boolean existsAndAllowed(String key) throws IOException {
        return get(key)!=null;
    }
    
    public boolean put(V value) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "PUT"+Saveable.DELIMITER+gson.toJson(value);
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            String json = br.readLine();
            
            
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
    
    public V get(String key) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String request = "GET"+Saveable.DELIMITER+v.getSaveableModuleName()+Saveable.DELIMITER+key;
        SecureMessage secureMessage;
        try {
            secureMessage = cryptoUtils.encrypt(username, request, passwordHash, salt, false);
            bw.write(gson.toJson(secureMessage));
            bw.newLine();
            String json = br.readLine();
            if(json.equals("NO")) {
                return null;
            }
            
            Saveable saveable = gson.fromJson(json, Saveable.class);
            return (V) saveable;
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
    
    
}
