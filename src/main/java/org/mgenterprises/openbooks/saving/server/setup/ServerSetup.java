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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    /**
     * Creates new form ServerSetup
     */
    public ServerSetup() {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
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

        stateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        phoneLabel.setText("Phone Number");

        faxLabel.setText("Fax Number");

        emailLabel.setText("Email");

        websiteLabel.setText("Website");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3)
                    .addComponent(jSeparator2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(companyNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(logoLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logoPathField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findButton))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(mottoLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mottoField))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cityLabel)
                            .addComponent(streetAddressLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(streetAddressField)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cityField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(stateLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(stateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(emailLabel)
                            .addComponent(phoneLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(phoneField)
                            .addComponent(emailField, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(faxLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(faxField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(websiteLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(websiteField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(companyNameLabel)
                    .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logoLabel)
                    .addComponent(logoPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                    .addComponent(stateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addContainerGap(80, Short.MAX_VALUE))
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
                        .addGap(0, 105, Short.MAX_VALUE)))
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
                        .addComponent(exportCombinedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(exportServerButton)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(exportRemoteClient)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(adminUserLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adminUserField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(adminPasswordLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(101, Short.MAX_VALUE))
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
                .addContainerGap(151, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Export Company File", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        }
    }//GEN-LAST:event_exportServerButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.logoPathField.setText(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_findButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminPasswordLabel;
    private javax.swing.JTextField adminUserField;
    private javax.swing.JLabel adminUserLabel;
    private javax.swing.JTextField cityField;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JTextField companyNameField;
    private javax.swing.JLabel companyNameLabel;
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loadCompanyFileButton;
    private javax.swing.JLabel loadCompanyFileLabel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JTextField logoPathField;
    private javax.swing.JTextField mottoField;
    private javax.swing.JLabel mottoLabel;
    private javax.swing.JLabel moveOnLabel;
    private javax.swing.JLabel mysqlSettingsLabel;
    private javax.swing.JPasswordField passwordField;
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
    private javax.swing.JComboBox stateComboBox;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTextField streetAddressField;
    private javax.swing.JLabel streetAddressLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextField websiteField;
    private javax.swing.JLabel websiteLabel;
    // End of variables declaration//GEN-END:variables
}
