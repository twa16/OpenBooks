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

package org.mgenterprises.openbooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.net.UnknownHostException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mgenterprises.openbooks.company.CompanyProfile;
import org.mgenterprises.openbooks.configuration.ConfigurationManager;
import org.mgenterprises.openbooks.invoicing.invoice.Invoice;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceItem;
import org.mgenterprises.openbooks.saving.AbstractSaveableAdapter;
import org.mgenterprises.openbooks.saving.companyfile.CompanyFile;
import org.mgenterprises.openbooks.saving.SaveServerConnection;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.saving.server.FileBackedSaveManager;
import org.mgenterprises.openbooks.saving.server.HibernateBackedSaveManager;
import org.mgenterprises.openbooks.saving.server.SaveManager;
import org.mgenterprises.openbooks.saving.server.SaveServer;
import org.mgenterprises.openbooks.saving.server.security.BCrypt;
import org.mgenterprises.openbooks.saving.server.users.FileBackedUserManager;
import org.mgenterprises.openbooks.saving.server.users.HibernateBackedUserManager;
import org.mgenterprises.openbooks.saving.server.users.UserManager;
import org.mgenterprises.openbooks.saving.server.users.UserProfile;
import org.mgenterprises.openbooks.views.MainGUI;

/**
 *
 * @author Manuel
 */
public class Main {
    private static MainGUI mainGUI;
    
    public static void main2(String[] args) {
      System.out.println(BCrypt.hashpw("admin", BCrypt.gensalt()));
    }
    
    public static void main1(String[] args) {
        Gson gson = new Gson();
        //CompanyFile company = gson.fromJson("{\"version\":1,\"companyFileComponents\":{\"companyProfile\":{\"companyName\":\"MG Enterprises Consulting LLC\",\"motto\":\"Bringing enterprise level services to small businesses\",\"emailAddress\":\"wetert#fgfg/com\",\"phoneNumber\":\"3254535356\",\"faxNumber\":\"56456456456\",\"streetAddress\":\"3500 Courtland Drive\",\"cityName\":\"Falls Church\",\"state\":\"Item 2\",\"website\":\"sffgdfgsdfgsfdg\",\"locked\":false}}}", CompanyFile.class);
        CompanyProfile companyProfile = new CompanyProfile();
        companyProfile.setCompanyName("My Test Company");
        companyProfile.setEmailAddress("me@mytestcompany.com");
        companyProfile.setStreetAddress("1234 Test Street");
        companyProfile.setCityName("Test City");
        companyProfile.setState("TestState");
        System.out.println(gson.toJson(companyProfile));
    }   
    
    public static void main(String[] args) throws UnknownHostException {
        SessionFactory sessionFactory = buildSessionFactory();
        SaveManager saveManager = new HibernateBackedSaveManager(sessionFactory);//FileBackedSaveManager(file);
        UserManager userManager = new HibernateBackedUserManager(sessionFactory);
        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("admin");
        userProfile.setPasswordHash("$2a$10$Kob.mJvhH6WbB9EcEVi97uenEt2F6q2wRT0zJCgm4KhJTOhY/WqCe");
        userManager.addUser(userProfile);
        short port = 6969;
        
        String keyStorePath = "/Users/mgauto/Work/Code/OpenBooks/test.jks";
        char[] keyStoreStorePassword = {'t', 'e', 's','t','t','e','s','t'};
        char[] keyStoreKeyPassword = {'s', 'e','c','u','r','e','p','a','s','s'};
        SaveServer saveServer = new SaveServer("127.0.0.1", port, userManager, saveManager, keyStorePath, keyStoreStorePassword);
        saveServer.startServer();
        
        CompanyProfile companyProfile = new CompanyProfile();
        companyProfile.setCompanyName("MG Enterprises Consulting LLC");
        companyProfile.setEmailAddress("mgauto@mgenterprises.org");
        companyProfile.setStreetAddress("3500 Courtland Drive");
        companyProfile.setCityName("Falls Church");
        companyProfile.setState("Virginia");
        SaveServerConnection saveServerConnection = new SaveServerConnection("127.0.0.1", port, "admin", "admin", keyStorePath, keyStoreStorePassword);
        CompanyFile companyFile = new CompanyFile();
        companyFile.updateCompanyProfile(companyProfile);
        companyFile.updateConfigurationManager(new ConfigurationManager());
        mainGUI = new MainGUI(saveServerConnection, companyFile);
        mainGUI.setVisible(true);
        
    }
    public static void startup() {
        String home = System.getProperty("user.home");
        String pathToOBHome = home+File.separator+"openbooks"+File.separator;
        File homeDirectory = new File(pathToOBHome);
        
    }
    public static MainGUI getInstance(){
        return mainGUI;
    }



    private static SessionFactory buildSessionFactory() {
        try {
            // Use hibernate.cfg.xml to get a SessionFactory
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
}
