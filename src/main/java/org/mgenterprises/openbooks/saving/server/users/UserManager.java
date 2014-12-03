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
import org.mgenterprises.openbooks.saving.server.authentication.UserLoginAttempt;
import org.mgenterprises.openbooks.saving.server.packets.UserAuth;
import org.mgenterprises.openbooks.saving.server.security.BCrypt;
import org.mgenterprises.openbooks.saving.server.security.CryptoUtils;
import org.mgenterprises.openbooks.saving.server.security.SecureMessage;

/**
 *
 * @author mgauto
 */
public abstract class UserManager {
    
    public abstract String decryptMessage(SecureMessage secureMessage) throws InvalidKeySpecException ;
    
    public abstract boolean checkUserLoginAttempt(UserLoginAttempt userLoginAttempt);
    
    public abstract boolean checkAuth(UserAuth userAuth) ;
    
    public abstract UserProfile getUserProfile(String username) throws ExecutionException ;
    
    public abstract boolean userHasAccessRight(String username, String accessRight, ACTION action);
    
    public abstract void addUser(UserProfile userProfile);
}
