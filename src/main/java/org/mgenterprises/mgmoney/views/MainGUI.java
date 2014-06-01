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

package org.mgenterprises.mgmoney.views;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.mgenterprises.mgmoney.configuration.ConfigurationManager;
import org.mgenterprises.mgmoney.customer.CustomerManager;
import org.mgenterprises.mgmoney.invoicing.invoice.InvoiceManager;
import org.mgenterprises.mgmoney.invoicing.item.ItemManager;
import org.mgenterprises.mgmoney.saving.SaveFile;
import org.mgenterprises.mgmoney.saving.SaveServerConnection;
import org.mgenterprises.mgmoney.views.actionlistener.DeleteCustomerActionListener;
import org.mgenterprises.mgmoney.views.panel.CustomerUpdatePanel;
import org.mgenterprises.mgmoney.views.panel.InvoiceCenterPanel;
import org.mgenterprises.mgmoney.views.panel.InvoiceUpdatePanel;
import org.mgenterprises.mgmoney.views.panel.ItemManagementPanel;

/**
 *
 * @author Manuel Gauto
 */
public class MainGUI extends javax.swing.JFrame implements WindowListener{
    private ConfigurationManager configurationManager = new ConfigurationManager();
    private CustomerManager customerManager;
    private ItemManager itemManager;
    private InvoiceManager invoiceManager;
    
    private SaveFile saveFile;
    /**
     * Creates new form MainGUI
     */
    public MainGUI(SaveServerConnection saveServerConnection) {
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
        this.saveFile = saveFile;
        loadSave();
        customerManager = new CustomerManager(saveServerConnection);
        itemManager = new ItemManager(saveServerConnection);
        invoiceManager = new InvoiceManager(saveServerConnection);
        configurationManager.loadDefaultConfiguration();
        initComponents();
        addWindowListener(this);
    }
    
    public void changePanel(JPanel jpanel) {
        mainPanelArea.setVisible(false);
        this.remove(mainPanelArea);
        mainPanelArea = jpanel;
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanelArea, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanelArea, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }
    
    private void loadSave() {
        
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
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        customerMenu = new javax.swing.JMenu();
        customerCenterMenuItem = new javax.swing.JMenuItem();
        invoiceMenu = new javax.swing.JMenu();
        invoiceCenterButton = new javax.swing.JMenuItem();
        createInvoiceMenuItem = new javax.swing.JMenuItem();
        itemManagerMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("OpenBill");
        setMinimumSize(new java.awt.Dimension(700, 750));
        setPreferredSize(new java.awt.Dimension(700, 750));

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        customerMenu.setText("Customer");

        customerCenterMenuItem.setText("Customer Center");
        customerCenterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerCenterMenuItemActionPerformed(evt);
            }
        });
        customerMenu.add(customerCenterMenuItem);

        jMenuBar1.add(customerMenu);

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

        jMenuBar1.add(invoiceMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanelArea, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanelArea, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void customerCenterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerCenterMenuItemActionPerformed
        changePanel(new CustomerUpdatePanel(customerManager, invoiceManager));
    }//GEN-LAST:event_customerCenterMenuItemActionPerformed

    private void invoiceCenterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceCenterButtonActionPerformed
        changePanel(new InvoiceCenterPanel(configurationManager, invoiceManager, customerManager));
    }//GEN-LAST:event_invoiceCenterButtonActionPerformed

    private void createInvoiceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createInvoiceMenuItemActionPerformed
        try {
            changePanel(new InvoiceUpdatePanel(configurationManager, customerManager, invoiceManager, itemManager.getItems()));
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_createInvoiceMenuItemActionPerformed

    private void itemManagerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemManagerMenuItemActionPerformed
        changePanel(new ItemManagementPanel(itemManager));
    }//GEN-LAST:event_itemManagerMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem createInvoiceMenuItem;
    private javax.swing.JMenuItem customerCenterMenuItem;
    private javax.swing.JMenu customerMenu;
    private javax.swing.JMenuItem invoiceCenterButton;
    private javax.swing.JMenu invoiceMenu;
    private javax.swing.JMenuItem itemManagerMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
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
}
