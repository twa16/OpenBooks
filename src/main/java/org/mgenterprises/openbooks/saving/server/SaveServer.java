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
import org.mgenterprises.openbooks.saving.AbstractSaveableAdapter;
import org.mgenterprises.openbooks.saving.EqualityOperation;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.saving.server.access.ACTION;
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

    public SaveServer(String listenAddress, short port, UserManager userManager, SaveManager saveManager) throws UnknownHostException {
        this.port = port;
        this.bindAddress = InetAddress.getByName(listenAddress);
        this.userManager = userManager;
        this.saveManager = saveManager;
        this.changeJournal = new ChangeJournal(saveManager);
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
                SaveServerRequestProcessor saveServerRequestProcessor = new SaveServerRequestProcessor(socket, secureRandom, gson, userManager, saveManager, changeJournal);
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
    private ChangeJournal changeJournal;

    public SaveServerRequestProcessor(Socket socket, SecureRandom secureRandom, Gson gson, UserManager userManager, SaveManager saveManager, ChangeJournal changeJournal) {
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

            //Read SecureMessage(Encrypted Request)
            String json = br.readLine();
            //Parse JSON
            SecureMessage secureMessage = gson.fromJson(json, SecureMessage.class);
            //Get username of requester
            String username = secureMessage.getUsername();
            String request = "";

            try {
                //Decrypt message using stored password
                request = userManager.decryptMessage(secureMessage);
            } catch (InvalidKeySpecException ex) {
                //Password was incorrect
                bw.write("500");
                bw.newLine();
                bw.flush();
                bw.close();
                br.close();
                socket.close();
            }
            //Check to make sure message was actually decrypted
            if (request.length() != 0) {
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
                    case "LOCK":
                        response = processLOCK(username, requestParts);
                        break;
                    case "RELEASE":
                        response = processRELEASE(username, requestParts);
                        break;
                    case "READJOURNAL":
                        response = processREADJOURNAL(username, requestParts);
                        break;
                 }
                try {
                    //Reuse salt for this conversation
                    byte[] salt = secureMessage.getSalt();//cryptoUtils.getSalt(secureRandom);
                    //Get password from userManager
                    String password = userManager.getUserProfile(username).getPasswordHash();//Hashing.sha256().hashString(userManager.getUserProfile(username).getPasswordHash()+System.currentTimeMillis(), Charsets.UTF_8).toString();
                    //Encrypt message using user password
                    secureMessage = cryptoUtils.encrypt(username, response, password, salt, false);
                    //Write to client
                    bw.write(gson.toJson(secureMessage));
                    //close
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
                if (lockHolder==null) {
                    saveManager.createLock(user, type, id);
                }
                Saveable saveable = saveManager.getSaveable(type, id);

                //Check if the GET was successful
                if (saveable != null) {
                    //If it is locked by another user indicate that
                    if (!lockHolder.equals(user)) {
                        saveable.setLocked(true);
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
        if (lockHolder == null) {
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
