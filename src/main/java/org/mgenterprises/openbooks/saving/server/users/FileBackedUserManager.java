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

package org.mgenterprises.openbooks.saving.server.users;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.mgenterprises.openbooks.saving.server.access.ACTION;
import org.mgenterprises.openbooks.saving.server.packets.UserAuth;
import org.mgenterprises.openbooks.saving.server.security.BCrypt;
import org.mgenterprises.openbooks.saving.server.security.CryptoUtils;
import org.mgenterprises.openbooks.saving.server.security.SecureMessage;

/**
 * @deprecated
 * @author mgauto
 */
public class FileBackedUserManager extends UserManager{
    private File userprofileDirectory;
    private LoadingCache<String, UserProfile> userProfiles;
    private Gson gson = new Gson();
    private CryptoUtils cryptoUtils = new CryptoUtils();

    public FileBackedUserManager(final File userprofileDirectory) {
        this.userprofileDirectory = userprofileDirectory;
        userProfiles = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(10000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(
                new CacheLoader<String, UserProfile>() {
                  public UserProfile load(String key) throws FileNotFoundException, IOException {
                      File userProfileFile = new File(userprofileDirectory+File.separator+key);
                      BufferedReader bufferedReader = new BufferedReader(new FileReader(userProfileFile));
                      String json = bufferedReader.readLine();
                      return gson.fromJson(json, UserProfile.class);
                  }
                });
    }
    
    public String decryptMessage(SecureMessage secureMessage) throws InvalidKeySpecException {
        try {
            UserProfile userProfile = userProfiles.get(secureMessage.getUsername());
            String passHash = userProfile.getPasswordHash();
            return cryptoUtils.decrypt(secureMessage, passHash);
        } catch (ExecutionException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public boolean checkAuth(UserAuth userAuth) {
        try {
            String username = userAuth.getUsername();
            String salt = userAuth.getSalt();
            String hashAttempt = userAuth.getHashAttempt();
            long timestamp = userAuth.getTimestamp();
            
            UserProfile userProfile = getUserProfile(username);
            if(userProfile != null) {
                String correctHash = BCrypt.hashpw(userProfile.getPasswordHash()+timestamp, salt);
                return hashAttempt.equals(correctHash);
            }
        } catch (ExecutionException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public UserProfile getUserProfile(String username) throws ExecutionException {
        return userProfiles.get(username);
    }
    
    public boolean userHasAccessRight(String username, String accessRight, ACTION action) {
        if(username.equals("admin")) {
            return true;
        }
        try {
            UserProfile userProfile = userProfiles.get(username);
            return userProfile.hasAccessRight(accessRight, action);
        } catch (ExecutionException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void addUser(UserProfile userProfile) {
        File userProfileFile = new File(userprofileDirectory+File.separator+userProfile.getUsername());
        try {
            FileUtils.deleteQuietly(userProfileFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(userProfileFile));
            String json = gson.toJson(userProfile);
            bw.write(json);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
