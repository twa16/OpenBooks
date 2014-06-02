/*
 * Copyright (c) 2014. MG Enterprises Consulting LLC
 * DO NOT DISTRIBUTE.
 * THE SYSTEMS AND ALGORITHMS THAT ARE IMPLEMENTED WITHIN THE SOURCE CODE BELOW
 * ALONG WITH THE SOURCE CODE ITSELF ARE COVERED BY THIS LICENSE
 * AND ARE SUBJECT TO A NON-DISCLOSURE AGREEMENT.
 */
package org.mgenterprises.openbooks.saving.server.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: mgauto Date: 2/12/14 Time: 10:20 PM
 */
public class CryptoUtils {

    private final String ENCODING = "UTF-16";
    private byte[] saltCache;
    private SecretKey secretKeyCache;
    private Cipher cipher;

    public CryptoUtils() {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(CryptoUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    public SecureMessage encrypt(String username, String plaintext, String password, byte[] salt, boolean useUnlimited) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKey secretKey = deriveKey(password, salt, useUnlimited);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(ENCODING));
            SecureMessage secureMessage = new SecureMessage(username, ENCODING, ciphertext, iv, salt, useUnlimited);
            return secureMessage;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(SecureMessage secureMessage, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKey secretKey = deriveKey(password, secureMessage.getSalt(), secureMessage.isUsingUnlimited());
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secureMessage.getIv()));
            String plaintext = new String(cipher.doFinal(secureMessage.getCiphertext()), secureMessage.getEncoding());
            return plaintext;
        }catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SecretKey deriveKey(String password, byte[] salt, boolean useUnlimited) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (secretKeyCache == null  || !Arrays.equals(this.saltCache, salt)) {
            short bits = 128;
            if (useUnlimited) {
                bits = 256;
            }
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, bits);
            SecretKey tmp = factory.generateSecret(spec);
            secretKeyCache = new SecretKeySpec(tmp.getEncoded(), "AES");
            this.saltCache = salt;
        }
        return secretKeyCache;
    }

    public byte[] getSalt(SecureRandom secureRandom) {
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
