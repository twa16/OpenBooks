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

package org.mgenterprises.openbooks.views;

import java.awt.CardLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.mgenterprises.openbooks.OpenbooksCore;
import org.mgenterprises.openbooks.accounting.account.AccountManager;
import org.mgenterprises.openbooks.accounting.transaction.TransactionManager;
import org.mgenterprises.openbooks.company.CompanyProfile;
import org.mgenterprises.openbooks.configuration.ConfigurationManager;
import org.mgenterprises.openbooks.customer.CustomerManager;
import org.mgenterprises.openbooks.invoicing.estimate.EstimateManager;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceManager;
import org.mgenterprises.openbooks.invoicing.item.ItemManager;
import org.mgenterprises.openbooks.saving.CompanyFile;
import org.mgenterprises.openbooks.saving.SaveServerConnection;
import org.mgenterprises.openbooks.views.actionlistener.DeleteCustomerActionListener;
import org.mgenterprises.openbooks.views.configuration.ConfigurationPanel;
import org.mgenterprises.openbooks.views.panel.CustomerUpdatePanel;
import org.mgenterprises.openbooks.views.panel.HomepagePanel;
import org.mgenterprises.openbooks.views.panel.InvoiceCenterPanel;
import org.mgenterprises.openbooks.views.panel.InvoiceUpdatePanel;
import org.mgenterprises.openbooks.views.panel.ItemManagementPanel;

/**
 *
 * @author Manuel Gauto
 */
public class MainGUI extends javax.swing.JFrame implements WindowListener, OpenbooksCore{
    private ConfigurationManager configurationManager;
    private CustomerManager customerManager;
    private ItemManager itemManager;
    private InvoiceManager invoiceManager;
    private EstimateManager estimateManager;
    private TransactionManager transactionManager;
    private AccountManager accountManager;
    private CompanyProfile companyProfile;
    /**
     * Creates new form MainGUI
     */
    public MainGUI(SaveServerConnection saveServerConnection, CompanyFile companyFile) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        customerManager = new CustomerManager(saveServerConnection);
        itemManager = new ItemManager(saveServerConnection);
        invoiceManager = new InvoiceManager(saveServerConnection);
        estimateManager = new EstimateManager(saveServerConnection);
        configurationManager = companyFile.getConfigurationManager();
        configurationManager.loadDefaultConfiguration();
        transactionManager = new TransactionManager(saveServerConnection);
        accountManager = new AccountManager(saveServerConnection);
        this.companyProfile = companyFile.getCompanyProfile();
        initComponents();
        addWindowListener(this);
        loadCards();
    }
    
    public void loadCards() {
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.add(mainPanelArea, new HomepagePanel(), "HomepagePanel");
        cl.add(mainPanelArea, new InvoiceCenterPanel(configurationManager, invoiceManager, customerManager), "InvoiceCenterPanel");
        cl.add(mainPanelArea, new CustomerUpdatePanel(customerManager, invoiceManager), "CustomerUpdatePanel");
        try {
            cl.add(mainPanelArea, new InvoiceUpdatePanel(this, itemManager.getItems()), "InvoiceUpdatePanel");
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
        cl.add(mainPanelArea, new ItemManagementPanel(itemManager), "ItemManagementPanel");
        cl.add(mainPanelArea, new ConfigurationPanel(configurationManager), "ConfigurationPanel");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanelArea = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        configurationMenuItem = new javax.swing.JMenuItem();
        customerMenu = new javax.swing.JMenu();
        customerCenterMenuItem = new javax.swing.JMenuItem();
        invoiceMenu = new javax.swing.JMenu();
        invoiceCenterButton = new javax.swing.JMenuItem();
        createInvoiceMenuItem = new javax.swing.JMenuItem();
        itemManagerMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Openbooks");
        setMinimumSize(new java.awt.Dimension(700, 750));
        getContentPane().setLayout(new org.mgenterprises.openbooks.views.OBCardLayout());

        mainPanelArea.setLayout(new org.mgenterprises.openbooks.views.OBCardLayout());
        getContentPane().add(mainPanelArea);

        fileMenu.setText("File");

        configurationMenuItem.setText("Configuration");
        configurationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(configurationMenuItem);

        mainMenuBar.add(fileMenu);

        customerMenu.setText("Customer");

        customerCenterMenuItem.setText("Customer Center");
        customerCenterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerCenterMenuItemActionPerformed(evt);
            }
        });
        customerMenu.add(customerCenterMenuItem);

        mainMenuBar.add(customerMenu);

        invoiceMenu.setText("Invoice");

        invoiceCenterButton.setText("Invoice Center");
        invoiceCenterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceCenterButtonActionPerformed(evt);
            }
        });
        invoiceMenu.add(invoiceCenterButton);

        createInvoiceMenuItem.setText("Create Invoice");
        createInvoiceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createInvoiceMenuItemActionPerformed(evt);
            }
        });
        invoiceMenu.add(createInvoiceMenuItem);

        itemManagerMenuItem.setText("Item Manager");
        itemManagerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemManagerMenuItemActionPerformed(evt);
            }
        });
        invoiceMenu.add(itemManagerMenuItem);

        mainMenuBar.add(invoiceMenu);

        setJMenuBar(mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void customerCenterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerCenterMenuItemActionPerformed
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.show(mainPanelArea, "CustomerUpdatePanel");
    }//GEN-LAST:event_customerCenterMenuItemActionPerformed

    private void invoiceCenterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceCenterButtonActionPerformed
        //changePanel(new InvoiceCenterPanel(configurationManager, invoiceManager, customerManager));
         OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
         cl.show(mainPanelArea, "InvoiceCenterPanel");
    }//GEN-LAST:event_invoiceCenterButtonActionPerformed

    private void createInvoiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createInvoiceMenuItemActionPerformed
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.show(mainPanelArea, "InvoiceUpdatePanel");
    }//GEN-LAST:event_createInvoiceMenuItemActionPerformed

    private void itemManagerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemManagerMenuItemActionPerformed
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.show(mainPanelArea, "ItemManagementPanel");
    }//GEN-LAST:event_itemManagerMenuItemActionPerformed

    private void configurationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationMenuItemActionPerformed
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.show(mainPanelArea, "ConfigurationPanel");
    }//GEN-LAST:event_configurationMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem configurationMenuItem;
    private javax.swing.JMenuItem createInvoiceMenuItem;
    private javax.swing.JMenuItem customerCenterMenuItem;
    private javax.swing.JMenu customerMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem invoiceCenterButton;
    private javax.swing.JMenu invoiceMenu;
    private javax.swing.JMenuItem itemManagerMenuItem;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanelArea;
    // End of variables declaration//GEN-END:variables

    @Override
    public void windowClosing(WindowEvent we) {
        try {
            customerManager.releaseAllLocks();
            itemManager.releaseAllLocks();
            invoiceManager.releaseAllLocks();
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
        }

    @Override
    public void windowDeactivated(WindowEvent we) {
        
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    @Override
    public CustomerManager getCustomerManager() {
        return customerManager;
    }

    @Override
    public ItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public InvoiceManager getInvoiceManager() {
        return invoiceManager;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public AccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public CompanyProfile getCompanyProfile() {
        return companyProfile;
    }

    @Override
    public JMenuBar getMainmenuBar() {
        return mainMenuBar;
    }

    @Override
    public EstimateManager getEstimateManager() {
        return estimateManager;
    }
}
