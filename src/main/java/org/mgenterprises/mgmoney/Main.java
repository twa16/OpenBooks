/*
 * The MIT License
 *
 * Copyright 2014 MG Enterprises Consulting LLC.
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

package org.mgenterprises.mgmoney;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.net.UnknownHostException;
import org.mgenterprises.mgmoney.saving.AbstractSaveableAdapter;
import org.mgenterprises.mgmoney.saving.SaveFile;
import org.mgenterprises.mgmoney.saving.SaveServerConnection;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.server.SaveManager;
import org.mgenterprises.mgmoney.saving.server.SaveServer;
import org.mgenterprises.mgmoney.saving.server.users.UserManager;
import org.mgenterprises.mgmoney.saving.server.users.UserProfile;
import org.mgenterprises.mgmoney.views.MainGUI;

/**
 *
 * @author Manuel
 */
public class Main {
    private static MainGUI mainGUI;
    
    public static void main(String[] args) throws UnknownHostException {
        /*File file = new File("/home/mgauto/Documents/MGM/");
        SaveManager saveManager = new SaveManager(file);
        UserManager userManager = new UserManager(new File(file+File.separator+"org.mgenterprises.mgmoney.saving.server.users.UserProfile"));
        short port = 6969;
        SaveServer saveServer = new SaveServer("127.0.0.1", port, userManager, saveManager);
        saveServer.startServer();
        
        SaveServerConnection saveServerConnection = new SaveServerConnection("127.0.0.1", port, "admin", "$2a$10$vhtSFeYrU1OX3pIvuno7u.8MQHI7LRJTJ9ucUt/ww1P4CnOYOwIH.");
        mainGUI = new MainGUI(saveServerConnection);
        mainGUI.setVisible(true);*/
        UserProfile user = new UserProfile("testuser", "testhash");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        //gsonBuilder.registerTypeAdapter(Saveable[].class, new AbstractSaveableArrayAdapter());
        Gson gson = gsonBuilder.create();
        System.out.println(gson.toJson(user));
    }
    
    public static MainGUI getInstance(){
        return mainGUI;
    }
}
