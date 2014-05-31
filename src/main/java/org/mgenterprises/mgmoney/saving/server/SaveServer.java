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

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mgenterprises.mgmoney.saving.AbstractSaveableAdapter;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.server.access.ACTION;
import org.mgenterprises.mgmoney.saving.server.security.CryptoUtils;
import org.mgenterprises.mgmoney.saving.server.security.SecureMessage;
import org.mgenterprises.mgmoney.saving.server.users.UserManager;

/**
 * SaveServer manages authentication and controls access to data
 *
 * @author mgauto
 */
public class SaveServer implements Runnable {

    public static final String DELIMITER = ":#:";
    private InetAddress bindAddress;
    private short port;
    private boolean running = true;

    private Gson gson;
    private UserManager userManager;
    private SaveManager saveManager;
    private SecureRandom secureRandom = new SecureRandom();

    public SaveServer(String listenAddress, short port, UserManager userManager, SaveManager saveManager) throws UnknownHostException {
        this.port = port;
        this.bindAddress = InetAddress.getByName(listenAddress);
        this.userManager = userManager;
        this.saveManager = saveManager;
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
            ServerSocket serverSocket = new ServerSocket(port, 100, bindAddress);
            while (running) {
                Socket socket = serverSocket.accept();
                SaveServerRequestProcessor saveServerRequestProcessor = new SaveServerRequestProcessor(socket, secureRandom, gson, userManager, saveManager);
                new Thread(saveServerRequestProcessor).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
            running = false;
    }
}
class SaveServerRequestProcessor implements Runnable {

    private Socket socket;
    private SecureRandom secureRandom;
    private CryptoUtils cryptoUtils = new CryptoUtils();
    private Gson gson;
    private UserManager userManager;
    private SaveManager saveManager;

    public SaveServerRequestProcessor(Socket socket, SecureRandom secureRandom, Gson gson, UserManager userManager, SaveManager saveManager) {
        this.socket = socket;
        this.secureRandom = secureRandom;
        this.gson = gson;
        this.userManager = userManager;
        this.saveManager = saveManager;
    }
    
    @Override
    public void run() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String json = br.readLine();
            SecureMessage secureMessage = gson.fromJson(json, SecureMessage.class);
            String username = secureMessage.getUsername();
            String request = "";

            try {
                request = userManager.decryptMessage(secureMessage);
            } catch (InvalidKeySpecException ex) {
                bw.write("500");
                bw.newLine();
                bw.flush();
                bw.close();
                br.close();
                socket.close();
            }
            if (request.length() != 0) {
                String[] requestParts = request.split(SaveServer.DELIMITER);

                System.out.println(Arrays.toString(requestParts));
                String verb = requestParts[0];
                String response = "500";
                switch (verb) {
                    case "GET":
                        response = processGET(username, requestParts);
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
                    case "LOCK":
                        response = processLOCK(username, requestParts);
                        break;
                    case "RELEASE":
                        response = processRELEASE(username, requestParts);
                        break;
                }
                try {
                    byte[] salt = secureMessage.getSalt();//cryptoUtils.getSalt(secureRandom);
                    String password = userManager.getUserProfile(username).getPasswordHash();//Hashing.sha256().hashString(userManager.getUserProfile(username).getPasswordHash()+System.currentTimeMillis(), Charsets.UTF_8).toString();
                    secureMessage = cryptoUtils.encrypt(username, response, password, salt, false);
                    bw.write(gson.toJson(secureMessage));
                    bw.newLine();
                    bw.flush();
                } catch (ExecutionException ex) {
                    Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeySpecException ex) {
                    Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                bw.close();
                br.close();
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SaveServerRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String processGET(String user, String[] requestParts) {
        String type = requestParts[1];
        String id = requestParts[2];
        if (userManager.userHasAccessRight(user, type, ACTION.GET)) {
            if (id.equals("ALL")) {
                Saveable[] saveables = saveManager.getAllSaveables(type);
                return gson.toJson(saveables);
            } else {
                saveManager.createLock(user, type, id);
                Saveable saveable = saveManager.getSaveable(type, id);
                if (saveable != null) {
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

    private String processSIZE(String user, String[] requestParts) {
        String type = requestParts[1];
        if (userManager.userHasAccessRight(user, type, ACTION.GET)) {
            Logger.getLogger("SaveServer").log(Level.INFO, "SIZE from {0} for t: {1} i: {2}", new Object[]{user, type});
            return String.valueOf(saveManager.getSaveableCount(type));
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied SIZE from {0} for t: {1} i: {2}", new Object[]{user, type});
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
            if (!saveManager.hasLock(saveable.getSaveableModuleName(), saveable.getUniqueId()) || saveManager.getLockHolder(saveable.getSaveableModuleName(), saveable.getUniqueId()).equals(user)) {
                saveManager.persistSaveable(type, user, saveable);
                Logger.getLogger("SaveServer").log(Level.INFO, "PUT from {0}", new Object[]{user});
                return "201";
            } else {
                Logger.getLogger("SaveServer").log(Level.INFO, "Denied PUT from {0}-Locked", new Object[]{user});
                return "503";
            }
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied PUT from {0}", new Object[]{user});
            return "401";
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
        if (lockHolder == null) {
            saveManager.createLock(user, type, id);
            Logger.getLogger("SaveServer").log(Level.INFO, "LOCK from {0} for {1} {2}", new Object[]{user, type, id});
            return "200";
        } else if(lockHolder.equals(user)){
            Logger.getLogger("SaveServer").log(Level.INFO, "LOCK CHECK from {0} for {1} {2}", new Object[]{user, type, id});
            return "302";
        } else {
            Logger.getLogger("SaveServer").log(Level.INFO, "Denied LOCK from {0} for {1} {2}", new Object[]{user, type, id});
            return "401";
        }
    }

}
