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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mgauto
 */
public class SaveServer implements Runnable{
    private InetAddress bindAddress;
    private short port;
    private boolean running = true;

    public SaveServer(String listenAddress, short port) throws UnknownHostException {
        this.port = port;
        this.bindAddress = InetAddress.getByName(listenAddress);
    }
    
    private void startServer() {
        
    }

    @Override
    public void run() {
        while(running) {
            try {
                ServerSocket serverSocket = new ServerSocket(port, 100, bindAddress);
                Socket socket = serverSocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(SaveServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void stop() {
        running = false;
    }
}