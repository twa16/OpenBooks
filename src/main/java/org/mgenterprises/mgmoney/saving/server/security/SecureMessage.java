/*
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

package org.mgenterprises.mgmoney.saving.server.security;

/**
 * User: mgauto
 * Date: 2/12/14
 * Time: 10:26 PM
 */
public class SecureMessage {
    private final String username;
    private final String encoding;
    private final byte[] ciphertext;
    private final byte[] iv;
    private final byte[] salt;
    private final boolean usesUnlimited;

    public SecureMessage(String username, String encoding, byte[] ciphertext, byte[] iv, byte[] salt, boolean usesUnlimited) {
        this.username = username;
        this.encoding = encoding;
        this.ciphertext = ciphertext;
        this.iv = iv;
        this.salt = salt;
        this.usesUnlimited = usesUnlimited;
    }

    public String getUsername() {
        return username;
    }
    
    public String getEncoding() {
        return encoding;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getSalt() {
        return salt;
    }

    public boolean isUsingUnlimited() {
        return usesUnlimited;
    }
}
