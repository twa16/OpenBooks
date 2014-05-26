/*
 * Copyright (c) 2014. MG Enterprises Consulting LLC
 * DO NOT DISTRIBUTE.
 * THE SYSTEMS AND ALGORITHMS THAT ARE IMPLEMENTED WITHIN THE SOURCE CODE BELOW
 * ALONG WITH THE SOURCE CODE ITSELF ARE COVERED BY THIS LICENSE
 * AND ARE SUBJECT TO A NON-DISCLOSURE AGREEMENT.
 */

package org.mgenterprises.mgmoney.saving.server.security;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

/**
 * User: mgauto
 * Date: 2/12/14
 * Time: 10:20 PM
 */
public class CryptoUtils {
    private final String ENCODING = "UTF-16";
    public SecureMessage encrypt(String username, String plaintext, String password, byte[] salt, boolean useUnlimited) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKey secretKey = deriveKey(password, salt, useUnlimited);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(ENCODING));
            SecureMessage secureMessage = new SecureMessage(username, ENCODING,ciphertext, iv, salt, useUnlimited);
            return secureMessage;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
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
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secureMessage.getIv()));
            String plaintext = new String(cipher.doFinal(secureMessage.getCiphertext()), secureMessage.getEncoding());
            return plaintext;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
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
        short bits = 128;
        if(useUnlimited) bits = 256;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, bits);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    public byte[] getSalt(SecureRandom secureRandom){
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
