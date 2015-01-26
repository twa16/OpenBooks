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

package org.mgenterprises.openbooks.saving.server.setup;

import com.lowagie.text.pdf.codec.Base64;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Void;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.hibernate.SessionFactory;
import org.mgenterprises.openbooks.company.CompanyProfile;
import org.mgenterprises.openbooks.configuration.HibernateConfigurationLoader;
import org.mgenterprises.openbooks.saving.companyfile.CompanyFile;
import org.mgenterprises.openbooks.saving.companyfile.CompanyFilePack;
import org.mgenterprises.openbooks.saving.companyfile.CompanyPackType;
import org.mgenterprises.openbooks.saving.server.security.BCrypt;
import org.mgenterprises.openbooks.saving.server.users.HibernateBackedUserManager;
import org.mgenterprises.openbooks.saving.server.users.UserManager;
import org.mgenterprises.openbooks.saving.server.users.UserProfile;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.mgenterprises.openbooks.views.MainGUI;

/**
 *
 * @author Manuel Gauto
 */
public class ServerSetup extends javax.swing.JFrame {
    private CompanyFilePack companyFilePack;
    private CompanyProfile companyProfile;
    private CompanyFile companyFile;
    private Properties hibernateProperties = new Properties();
    private File databaseFile;
    private File keyStoreFile;
    /**
     * Creates new form ServerSetup
     */
    public ServerSetup() throws IOException {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        keyStoreFile = File.createTempFile("openbooks", "keystore");
    }

    public static void main(String[] args) throws IOException {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
        new ServerSetup().setVisible(true);
    }
    
    private void createCompanyFile() {
        //Lets make our company profile
        companyProfile = new CompanyProfile();
        if(logoPathField.getText().length()>0) companyProfile.setLogoBase64(Base64.encodeFromFile(logoPathField.getText()));
        companyProfile.setCompanyName(companyNameField.getText());
        companyProfile.setMotto(mottoField.getText());
        companyProfile.setEmailAddress(emailField.getText());
        companyProfile.setPhoneNumber(phoneField.getText());
        companyProfile.setFaxNumber(faxField.getText());
        companyProfile.setStreetAddress(streetAddressField.getText());
        companyProfile.setCityName(cityField.getText());
        companyProfile.setState(stateComboBox.getSelectedItem().toString());
        companyProfile.setWebsite(websiteField.getText());

        //Put our profile into a company file
        companyFile = new CompanyFile();
        companyFile.updateCompanyProfile(companyProfile);
    }
    
    private void createDatabaseConfiguration() {
        if(passwordField.getPassword().length<1) {
            JOptionPane.showConfirmDialog(null, "You must set an admin password!", "Warning!", JOptionPane.OK_OPTION);
            return;
        }
        if(databaseTypeComboBox.getSelectedIndex()==0) {
            try {
                //So we want a h2 backing
                
                //Load our driver
                Class.forName("org.h2.Driver");
                //Use the company name. Make sure you make your profile first!
                databaseFile = File.createTempFile("dbFile", companyProfile.getCompanyName());
                //Create database file
                Connection connection = DriverManager.getConnection("jdbc:h2:"+databaseFile.getAbsolutePath(), "admin", String.copyValueOf(passwordField.getPassword()));
                connection.close();
            
                //Set some hibernate properties so that it uses this db
                //Set the dialect for H2
                hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
                //Set JDBC url
                hibernateProperties.setProperty("hibernate.connection.url", "jdbc:h2:"+databaseFile.getAbsolutePath());
                //Set the driver
                hibernateProperties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
                //Set username
                hibernateProperties.setProperty("hibernate.connection.username", "admin");
                //Set password
                hibernateProperties.setProperty("hibernate.connection.password", String.copyValueOf(passwordField.getPassword()));
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            //Set some hibernate properties so that it uses this db

            //Set JDBC url
            hibernateProperties.setProperty("hibernate.connection.url", "jdbc:mysql://"+serverAddressField.getText()+":"+serverPortSpinner.getValue()+"/"+databaseNameField.getText()+"?zeroDateTimeBehavior=convertToNull");
            //Set username
            hibernateProperties.setProperty("hibernate.connection.username", serverUsernameField.getText());
            //Set password
            hibernateProperties.setProperty("hibernate.connection.password", String.copyValueOf(serverPasswordField.getPassword()));
        }
        setupInitialDB();
    }

    private void setupInitialDB() {
        try {
            //Start Hibernate
            HibernateConfigurationLoader hibernateConfigurationLoader = new HibernateConfigurationLoader(hibernateProperties);
            SessionFactory sessionFactory = hibernateConfigurationLoader.getConfiguration().buildSessionFactory();

            //Add the admin user
            //Let's use what we have built already
            UserManager userManager = new HibernateBackedUserManager(sessionFactory);
            UserProfile userProfile = new UserProfile();
            //Set the username
            userProfile.setUsername("admin");
            //Hash and set password
            userProfile.setPasswordHash(BCrypt.hashpw(String.copyValueOf(passwordField.getPassword()), BCrypt.gensalt()));
            //Persist User
            userManager.addUser(userProfile);
            //Cleanup
            sessionFactory.close();

        } catch (Throwable ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private File saveHibernateProperties() throws IOException {
        File hibernateFile = File.createTempFile("hibernateconfig", companyProfile.getCompanyName());
        hibernateProperties.store(new FileWriter(hibernateFile), "Initial Configuration for "+ companyProfile.getCompanyName());
        return hibernateFile;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        companyNameLabel = new javax.swing.JLabel();
        companyNameField = new javax.swing.JTextField();
        mottoLabel = new javax.swing.JLabel();
        mottoField = new javax.swing.JTextField();
        logoLabel = new javax.swing.JLabel();
        logoPathField = new javax.swing.JTextField();
        findButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        streetAddressLabel = new javax.swing.JLabel();
        streetAddressField = new javax.swing.JTextField();
        cityLabel = new javax.swing.JLabel();
        cityField = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        stateComboBox = new javax.swing.JComboBox();
        phoneLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        faxLabel = new javax.swing.JLabel();
        faxField = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        emailLabel = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        websiteLabel = new javax.swing.JLabel();
        websiteField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        countryCodeField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        databaseTypeLabel = new javax.swing.JLabel();
        databaseTypeComboBox = new javax.swing.JComboBox();
        moveOnLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        mysqlSettingsLabel = new javax.swing.JLabel();
        serverAddressLabel = new javax.swing.JLabel();
        serverAddressField = new javax.swing.JTextField();
        serverPortLabel = new javax.swing.JLabel();
        serverPortSpinner = new javax.swing.JSpinner();
        serverUsernameLabel = new javax.swing.JLabel();
        serverUsernameField = new javax.swing.JTextField();
        serverPasswordLabel = new javax.swing.JLabel();
        serverPasswordField = new javax.swing.JPasswordField();
        databaseNameLabel = new javax.swing.JLabel();
        databaseNameField = new javax.swing.JTextField();
        loadCompanyFileButton = new javax.swing.JButton();
        loadCompanyFileLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        generationOutput = new javax.swing.JTextArea();
        startGenerationButton = new javax.swing.JButton();
        keyToolPath = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        pathToKeytoolLabel = new javax.swing.JLabel();
        keyStoreServerPasswordLabel = new javax.swing.JLabel();
        keyStoreServerPassword = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        clientKeystorePasswordLabel = new javax.swing.JLabel();
        keyStoreClientPassword = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        exportCombinedButton = new javax.swing.JButton();
        exportServerButton = new javax.swing.JButton();
        exportRemoteClient = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        adminUserLabel = new javax.swing.JLabel();
        adminUserField = new javax.swing.JTextField();
        adminPasswordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();

        jLabel1.setText("jLabel1");

        setMinimumSize(new java.awt.Dimension(700, 333));
        setPreferredSize(new java.awt.Dimension(700, 333));

        companyNameLabel.setText("Company Name");

        mottoLabel.setText("Company Motto");

        logoLabel.setText("Logo Path");

        findButton.setText("Find");
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findButtonActionPerformed(evt);
            }
        });

        streetAddressLabel.setText("Street Address");

        cityLabel.setText("City");

        stateLabel.setText("State");

        stateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming" }));

        phoneLabel.setText("Phone Number");

        faxLabel.setText("Fax Number");

        emailLabel.setText("Email");

        websiteLabel.setText("Website");

        jLabel4.setText("Country Code");

        countryCodeField.setText("US");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(mottoLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(cityLabel)
                                            .addComponent(streetAddressLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(cityField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(stateLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(stateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(countryCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(streetAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 725, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 17, Short.MAX_VALUE)))
                        .addGap(78, 78, 78))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(mottoField, javax.swing.GroupLayout.PREFERRED_SIZE, 716, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(companyNameLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(logoLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(logoPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(findButton))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(phoneLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(emailLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(websiteLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(websiteField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(faxLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(faxField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 805, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(companyNameLabel)
                    .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logoLabel)
                    .addComponent(logoPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mottoLabel)
                    .addComponent(mottoField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streetAddressLabel)
                    .addComponent(streetAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cityLabel)
                    .addComponent(cityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stateLabel)
                    .addComponent(stateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(countryCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneLabel)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(faxLabel)
                    .addComponent(faxField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(emailLabel)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(websiteLabel)
                    .addComponent(websiteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Company Setup", jPanel3);

        databaseTypeLabel.setText("Database Type");

        databaseTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "FILE", "MYSQL" }));

        moveOnLabel.setText("If you are choose FILE move on to the next tab");

        mysqlSettingsLabel.setText("MYSQL Settings");

        serverAddressLabel.setText("Server Address");

        serverAddressField.setText("localhost");

        serverPortLabel.setText("Server Port");

        serverPortSpinner.setValue(3306);

        serverUsernameLabel.setText("Server Username");

        serverPasswordLabel.setText("Server Password");

        databaseNameLabel.setText("Database");

        databaseNameField.setText("Openbooks");

        loadCompanyFileButton.setText("Load Company File");

        loadCompanyFileLabel.setText("This will only work with combined or server company files");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(databaseTypeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(databaseTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(moveOnLabel))
                            .addComponent(mysqlSettingsLabel)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(serverAddressLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(serverPortLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverPortSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(serverUsernameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(serverPasswordLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(serverPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(databaseNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(databaseNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(loadCompanyFileButton)
                            .addComponent(loadCompanyFileLabel))
                        .addGap(0, 276, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseTypeLabel)
                    .addComponent(databaseTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moveOnLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mysqlSettingsLabel)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(serverAddressLabel)
                            .addComponent(serverAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(serverPortLabel)
                        .addComponent(serverPortSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverUsernameLabel)
                    .addComponent(serverUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverPasswordLabel)
                    .addComponent(serverPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseNameLabel)
                    .addComponent(databaseNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addComponent(loadCompanyFileLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadCompanyFileButton)
                .addGap(16, 16, 16))
        );

        tabbedPane.addTab("Configure Database Connection", jPanel1);

        generationOutput.setColumns(20);
        generationOutput.setRows(5);
        jScrollPane1.setViewportView(generationOutput);

        startGenerationButton.setText("Start Generation");
        startGenerationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startGenerationButtonActionPerformed(evt);
            }
        });

        keyToolPath.setText("keytool");

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        pathToKeytoolLabel.setText("Path to Keytool:");

        keyStoreServerPasswordLabel.setText("KeyStore Server Password:");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText("The OpenBooks setup wizard uses the Java Keytool to create the certificate needed for communication between the server and client. The KeyTool server password is used only by the server-side to allow it to decrypt the private key in the keystore. The KeyTool client password is used to read the keystore.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea1);

        clientKeystorePasswordLabel.setText("KeyStore Client Password:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startGenerationButton)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(keyStoreServerPasswordLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(keyToolPath, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton))
                            .addComponent(pathToKeytoolLabel)
                            .addComponent(clientKeystorePasswordLabel)
                            .addComponent(keyStoreClientPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                            .addComponent(keyStoreServerPassword))
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(keyStoreServerPasswordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keyStoreServerPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(clientKeystorePasswordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keyStoreClientPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pathToKeytoolLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keyToolPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseButton)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startGenerationButton)
                .addContainerGap())
        );

        tabbedPane.addTab("Certificate Generation", jPanel4);

        jLabel3.setText("Choose this option if you have choosen the FILE option or wish to run the server locally");

        jLabel5.setText("Choose this option if you want to get the configuration for a standalone server instance");

        exportCombinedButton.setText("Export Combined Company File");
        exportCombinedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCombinedButtonActionPerformed(evt);
            }
        });

        exportServerButton.setText("Export Server Only File");
        exportServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportServerButtonActionPerformed(evt);
            }
        });

        exportRemoteClient.setText("Export Remote Company File");
        exportRemoteClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRemoteClientActionPerformed(evt);
            }
        });

        jLabel2.setText("Choose this option if you have a standalone server and will have remote frontends");

        adminUserLabel.setText("Admin User");

        adminUserField.setEditable(false);
        adminUserField.setText("admin");

        adminPasswordLabel.setText("Admin password");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(adminUserLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adminUserField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(adminPasswordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(exportCombinedButton)
                            .addComponent(exportServerButton)
                            .addComponent(exportRemoteClient))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminUserLabel)
                    .addComponent(adminUserField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(adminPasswordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportCombinedButton)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportServerButton)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportRemoteClient)
                    .addComponent(jLabel2))
                .addContainerGap(112, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Export Company File", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void exportCombinedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCombinedButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //Create a company file
            createCompanyFile();
            //Boom database
            createDatabaseConfiguration();
            //Let's add our extension
            companyFilePack = new CompanyFilePack(new File(jFileChooser.getSelectedFile().getAbsolutePath()+".obpack"));
            //Pack!
            try {
                companyFilePack.pack(companyFile, databaseFile, saveHibernateProperties(), CompanyPackType.COMBINED);
            } catch (IOException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showConfirmDialog(null, "Failed to save database configuration", "Warning!", JOptionPane.OK_OPTION);
            }
            JOptionPane.showMessageDialog(this, "Export Complete!");
        }
    }//GEN-LAST:event_exportCombinedButtonActionPerformed

    private void exportRemoteClientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRemoteClientActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //Create a company file
            createCompanyFile();
            //Boom database
            createDatabaseConfiguration();
            //Let's add our extension
            companyFilePack = new CompanyFilePack(new File(jFileChooser.getSelectedFile().getAbsolutePath()+".obpack"));
            //Pack!
            try {
                companyFilePack.pack(companyFile, null, saveHibernateProperties(), CompanyPackType.CLIENT);
            } catch (IOException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showConfirmDialog(null, "Failed to save database configuration", "Warning!", JOptionPane.OK_OPTION);
            }
            JOptionPane.showMessageDialog(this, "Export Complete!");
        }
    }//GEN-LAST:event_exportRemoteClientActionPerformed

    private void exportServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportServerButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            //Create a company file
            createCompanyFile();
            //Boom database
            createDatabaseConfiguration();
            //Let's add our extension
            companyFilePack = new CompanyFilePack(new File(jFileChooser.getSelectedFile().getAbsolutePath()+".obpack"));
            //Pack!
            try {
                companyFilePack.pack(null, databaseFile, saveHibernateProperties(), CompanyPackType.SERVER);
            } catch (IOException ex) {
                Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showConfirmDialog(null, "Failed to save database configuration", "Warning!", JOptionPane.OK_OPTION);
            }
            JOptionPane.showMessageDialog(this, "Export Complete!");
        }
    }//GEN-LAST:event_exportServerButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.logoPathField.setText(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_findButtonActionPerformed

    private void startGenerationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startGenerationButtonActionPerformed
        try {
            String pathToKeyTool = this.keyToolPath.getText();
            new PrintOutputWorker("Using keytool at " + pathToKeyTool, generationOutput).execute();
            
            String keyStorePath = keyStoreFile.getAbsolutePath();
            String storePass = this.keyStoreClientPassword.getText();
            String keyPass = this.keyStoreServerPassword.getText();
            short keySize = 2048;
            StringBuilder sb = new StringBuilder();
            sb.append("CN=");
            sb.append(companyNameField.getText());
            sb.append(", OU=Finance, O=");
            sb.append(companyNameField.getText());
            sb.append(", L=");
            sb.append(cityField.getText());
            sb.append(", ST=");
            sb.append(stateComboBox.getSelectedItem().toString());
            sb.append(", C=");
            sb.append(countryCodeField.getText());
            new PrintOutputWorker(sb.toString(), generationOutput).execute();
            
            ArrayList<String> commandParts = new ArrayList<String>();
            commandParts.add(pathToKeyTool);
            commandParts.add("-genkey");
            commandParts.add("-keystore "+keyStorePath);
            commandParts.add("-keyalg RSA");
            commandParts.add("-keysize "+keySize);
            commandParts.add("-storepass "+storePass);
            commandParts.add("-keypass "+keyPass);
            commandParts.add("-dname "+sb.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
            new PrintOutputWorker(processBuilder.toString(), generationOutput).get();
            new PrintOutputWorker("Starting keytool...", generationOutput).get();
            processBuilder.start();
            new PrintOutputWorker("Keytool Complete!", generationOutput).get();
            new PrintOutputWorker("Output To: " + keyStorePath, generationOutput).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_startGenerationButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.keyToolPath.setText(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminPasswordLabel;
    private javax.swing.JTextField adminUserField;
    private javax.swing.JLabel adminUserLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField cityField;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JLabel clientKeystorePasswordLabel;
    private javax.swing.JTextField companyNameField;
    private javax.swing.JLabel companyNameLabel;
    private javax.swing.JTextField countryCodeField;
    private javax.swing.JTextField databaseNameField;
    private javax.swing.JLabel databaseNameLabel;
    private javax.swing.JComboBox databaseTypeComboBox;
    private javax.swing.JLabel databaseTypeLabel;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JButton exportCombinedButton;
    private javax.swing.JButton exportRemoteClient;
    private javax.swing.JButton exportServerButton;
    private javax.swing.JTextField faxField;
    private javax.swing.JLabel faxLabel;
    private javax.swing.JButton findButton;
    private javax.swing.JTextArea generationOutput;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField keyStoreClientPassword;
    private javax.swing.JTextField keyStoreServerPassword;
    private javax.swing.JLabel keyStoreServerPasswordLabel;
    private javax.swing.JTextField keyToolPath;
    private javax.swing.JButton loadCompanyFileButton;
    private javax.swing.JLabel loadCompanyFileLabel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JTextField logoPathField;
    private javax.swing.JTextField mottoField;
    private javax.swing.JLabel mottoLabel;
    private javax.swing.JLabel moveOnLabel;
    private javax.swing.JLabel mysqlSettingsLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel pathToKeytoolLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JTextField serverAddressField;
    private javax.swing.JLabel serverAddressLabel;
    private javax.swing.JPasswordField serverPasswordField;
    private javax.swing.JLabel serverPasswordLabel;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSpinner serverPortSpinner;
    private javax.swing.JTextField serverUsernameField;
    private javax.swing.JLabel serverUsernameLabel;
    private javax.swing.JButton startGenerationButton;
    private javax.swing.JComboBox stateComboBox;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTextField streetAddressField;
    private javax.swing.JLabel streetAddressLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextField websiteField;
    private javax.swing.JLabel websiteLabel;
    // End of variables declaration//GEN-END:variables
}

class PrintOutputWorker extends SwingWorker<String, Void> {
    private String output;
    private JTextArea outputArea;

    public PrintOutputWorker(String output, JTextArea outputArea) {
        this.output = output;
        this.outputArea = outputArea;
    }
    
    @Override
    protected String doInBackground() throws Exception {
        outputArea.append(output);
        return output;
    }
    
}
