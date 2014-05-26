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

package org.mgenterprises.mgmoney.saving.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mgenterprises.mgmoney.saving.AbstractSaveableAdapter;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.server.security.SecureMessage;
import org.mgenterprises.mgmoney.saving.server.users.UserManager;

/**
 * SaveServer manages authentication and controls access to data
 * @author mgauto
 */
public class SaveServer implements Runnable{
    private InetAddress bindAddress;
    private short port;
    private boolean running = true;
    
    private Gson gson;
    private UserManager userManager;
    private SaveManager saveManager;

    public SaveServer(String listenAddress, short port, UserManager userManager, SaveManager saveManager) throws UnknownHostException {
        this.port = port;
        this.bindAddress = InetAddress.getByName(listenAddress);
        this.userManager = userManager;
        this.saveManager = saveManager;
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        gson = gsonBilder.create();
    }
    
    private void startServer() {
        
    }

    @Override
    public void run() {
        //No need for workers since there won't be too many simultaneous connections? or am I just lazy....
        try {
            ServerSocket serverSocket = new ServerSocket(port, 100, bindAddress);
            while(running) {
                Socket socket = serverSocket.accept();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String json = br.readLine();
                SecureMessage secureMessage = gson.fromJson(json, SecureMessage.class);
                String username = secureMessage.getUsername();
                String request="";
                
                try {
                    request = userManager.decryptMessage(secureMessage);
                } catch (InvalidKeySpecException ex) {
                    bw.write("ERROR");
                    bw.newLine();
                    bw.flush();
                    bw.close();
                    br.close();
                    socket.close();
                }
                if(request.length()!=0) {
                    String[] requestParts = request.split(Saveable.DELIMITER);

                    String verb = requestParts[0];
                    String response = "ERROR";
                    switch(verb) {
                        case "GET":
                            response = processGET(username, requestParts);
                            break;
                        case "PUT":
                            response = processPUT(username, requestParts);
                            break;
                        case "RELEASE":
                            response = processRELEASE(username, requestParts);
                            break;
                    }
                    bw.write(response);
                    bw.newLine();
                    bw.flush();
                    bw.close();
                    br.close();
                    socket.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String processGET(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];
        saveManager.createLock(user, type, id);
        Saveable saveable = saveManager.getSaveable(type, id);
        if(saveable!=null){
            Logger.getLogger("SaveServer").log(Level.INFO, "GET from {0} for t: {1} i: {2}", new Object[]{user, type, id});
            return gson.toJson(saveable);
        }
        else {
            Logger.getLogger("SaveServer").log(Level.INFO, "GET from {0} for t: {1} i: {2}", new Object[]{user, type, id});
            return "404";
        }
    }
    
    private String processPUT(String user, String[] requestParts) {
        String data = requestParts[1];
        Saveable saveable = gson.fromJson(data, Saveable.class);
        if(saveManager.getLockHolder(saveable.getSaveableModuleName(), saveable.getUniqueId()).equals(user)){
            saveManager.persistSaveable(user, saveable);
            Logger.getLogger("SaveServer").log(Level.INFO, "PUT from {0}", new Object[]{user});
            return "OK";
        }
        else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied PUT from {0}-Locked", new Object[]{user});
            return "LOCKED";
        }
    }
    
    private String processRELEASE(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];
        saveManager.removeLock(type, id);
        Logger.getLogger("SaveServer").log(Level.INFO, "RELEASE from {0} for {1} {2}", new Object[]{user, type, id});
        return "RELEASED";
    }
    
    public void stop() {
        running = false;
    }
}