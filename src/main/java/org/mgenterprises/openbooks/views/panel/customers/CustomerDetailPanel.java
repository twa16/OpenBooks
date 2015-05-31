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
package org.mgenterprises.openbooks.views.panel.customers;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.mgenterprises.openbooks.customer.Customer;
import org.mgenterprises.openbooks.customer.CustomerManager;
import org.mgenterprises.openbooks.invoicing.invoice.Invoice;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceManager;
import org.mgenterprises.openbooks.util.InvoiceUtils;
import org.mgenterprises.openbooks.util.State;
import org.mgenterprises.openbooks.views.ViewChangeListener;
import org.mgenterprises.openbooks.views.actionlistener.DeleteCustomerActionListener;

/**
 *
 * @author Manuel Gauto
 */
public class CustomerDetailPanel extends JPanel implements ViewChangeListener{

    private CustomerManager customerManager;
    private InvoiceManager invoiceManager;

    /**
     * Creates new form CustomerUpdatePanel
     */
    public CustomerDetailPanel(CustomerManager customerManager, InvoiceManager invoiceManager) {
        this.customerManager = customerManager;
        this.invoiceManager = invoiceManager;
        initComponents();
        processData();
        stateCombo.setModel(new DefaultComboBoxModel<>(State.values()));
    }

    /**
     * Get data from CustomerManager and load into table model and
     * use model for table.
     */
    public void processData() {
        try {
            DefaultTableModel tableModel = (DefaultTableModel) customerTable.getModel();
            tableModel.setRowCount(0);
            for (Customer customer : customerManager.getCustomers()) {
                Object[] data = new Object[4];
                data[0] = customer.getCustomerNumber();
                data[1] = customer.getCompanyName();
                data[2] = customer.getState().toString();
                Invoice[] invoiceItems = invoiceManager.getCustomerInvoices(customer);
                data[3] = new InvoiceUtils().getInvoiceSetTotal(invoiceItems);
                tableModel.addRow(data);
            }
            customerTable.setModel(tableModel);
            customerTable.repaint();
        } catch (IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog(null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        companyNameLabel = new javax.swing.JLabel();
        companyNameField = new javax.swing.JTextField();
        contactFirstLabel = new javax.swing.JLabel();
        contactFirstField = new javax.swing.JTextField();
        contactLastLabel = new javax.swing.JLabel();
        contactLastField = new javax.swing.JTextField();
        phoneNumberLabel = new javax.swing.JLabel();
        phoneNumberField = new javax.swing.JTextField();
        streetAddressLabel = new javax.swing.JLabel();
        streetAddressField = new javax.swing.JTextField();
        cityLabel = new javax.swing.JLabel();
        cityField = new javax.swing.JTextField();
        stateLabel = new javax.swing.JLabel();
        stateCombo = new javax.swing.JComboBox(State.values());
        jScrollPane2 = new javax.swing.JScrollPane();
        customerTable = new javax.swing.JTable();
        newCustomerButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        idField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        emailLabel = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();

        setMinimumSize(new java.awt.Dimension(650, 650));
        setName(""); // NOI18N

        companyNameLabel.setText("Company Name");

        companyNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                companyNameFieldActionPerformed(evt);
            }
        });

        contactFirstLabel.setText("Contact First Name");

        contactFirstField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactFirstFieldActionPerformed(evt);
            }
        });

        contactLastLabel.setText("Company Last Name");

        contactLastField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactLastFieldActionPerformed(evt);
            }
        });

        phoneNumberLabel.setText("Phone Number");

        phoneNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberFieldActionPerformed(evt);
            }
        });

        streetAddressLabel.setText("Street Address");

        streetAddressField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                streetAddressFieldActionPerformed(evt);
            }
        });

        cityLabel.setText("City");

        cityField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cityFieldActionPerformed(evt);
            }
        });

        stateLabel.setText("State");

        stateCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        stateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stateComboActionPerformed(evt);
            }
        });

        customerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Company Name", "State", "Total Due"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        customerTable.getTableHeader().setReorderingAllowed(false);
        customerTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customerTableMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                customerTableMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(customerTable);
        if (customerTable.getColumnModel().getColumnCount() > 0) {
            customerTable.getColumnModel().getColumn(0).setMinWidth(50);
            customerTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            customerTable.getColumnModel().getColumn(0).setMaxWidth(50);
            customerTable.getColumnModel().getColumn(2).setMinWidth(100);
            customerTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            customerTable.getColumnModel().getColumn(2).setMaxWidth(100);
            customerTable.getColumnModel().getColumn(3).setMinWidth(150);
            customerTable.getColumnModel().getColumn(3).setPreferredWidth(150);
            customerTable.getColumnModel().getColumn(3).setMaxWidth(150);
        }

        newCustomerButton.setText("New");
        newCustomerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCustomerButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        idField.setEditable(false);
        idField.setText("000000");
        idField.setPreferredSize(new java.awt.Dimension(50, 20));

        jLabel1.setText("ID");

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        emailLabel.setText("Email");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(companyNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(newCustomerButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(streetAddressLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(streetAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cityLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cityField, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 62, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(clearButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(contactFirstLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactFirstField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(contactLastLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contactLastField, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(saveButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(phoneNumberLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phoneNumberField)
                        .addGap(18, 18, 18)
                        .addComponent(emailLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(companyNameLabel)
                        .addComponent(companyNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(newCustomerButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(contactFirstLabel)
                        .addComponent(contactFirstField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(contactLastLabel)
                        .addComponent(contactLastField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneNumberLabel)
                    .addComponent(phoneNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(emailLabel)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(streetAddressLabel)
                    .addComponent(streetAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cityLabel)
                    .addComponent(cityField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stateLabel)
                    .addComponent(stateCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newCustomerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCustomerButtonActionPerformed
        try {
            this.idField.setText(String.valueOf(customerManager.getHighestId()));
        } catch (IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog(null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_newCustomerButtonActionPerformed

    /**
     * Clears all the fields on the form
     */
    private void clearFields() {
        this.idField.setText("");
        this.companyNameField.setText("");
        this.contactFirstField.setText("");
        this.contactLastField.setText("");
        this.phoneNumberField.setText("");
        this.emailField.setText("");
        this.streetAddressField.setText("");
        this.cityField.setText("");
        this.stateCombo.setSelectedIndex(0);
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        try {
            if (this.idField.getText().equals("000000")) {
                return;
            }
            Customer customer = new Customer();
            customer.setCustomerNumber(Integer.parseInt(this.idField.getText()));
            customer.setCompanyName(this.companyNameField.getText());
            customer.setContactFirst(this.contactFirstField.getText());
            customer.setContactLast(this.contactLastField.getText());
            customer.setPhoneNumber(this.phoneNumberField.getText());
            customer.setStreetAddress(this.streetAddressField.getText());
            customer.setCityName(this.cityField.getText());
            customer.setState((State) this.stateCombo.getSelectedItem());
            customer.setEmailAddress(this.emailField.getText());

            if (customerManager.exists(customer.getCustomerNumber())) {
                customerManager.updateCustomer(customer);
            } else {
                customerManager.addCustomer(customer);
            }
            customerManager.releaseLock(new Customer().getSaveableModuleName(), idField.getText());
            this.saveButton.setText("Save");
            this.saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN));
            clearFields();
            processData();
        } catch (IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog(null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void customerTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_customerTableMouseClicked
        int selected = customerTable.getSelectedRow();
        if (selected != -1) {
            setFields(selected);
        }
    }//GEN-LAST:event_customerTableMouseClicked

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearFields();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void customerTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_customerTableMouseReleased
        int r = customerTable.rowAtPoint(evt.getPoint());
        if (r >= 0 && r < customerTable.getRowCount()) {
            customerTable.setRowSelectionInterval(r, r);
        } else {
            customerTable.clearSelection();
        }

        int rowindex = customerTable.getSelectedRow();
        if (rowindex < 0) {
            return;
        }
        if (evt.isPopupTrigger() && evt.getComponent() instanceof JTable) {
            int cusID = (int) customerTable.getValueAt(rowindex, 0);
            JPopupMenu popup = new JPopupMenu();
            JMenuItem deleteCustomerMenuItem = new JMenuItem("Delete");
            deleteCustomerMenuItem.addActionListener(new DeleteCustomerActionListener(customerManager, invoiceManager, cusID));
            popup.add(deleteCustomerMenuItem);
            popup.show(evt.getComponent(), evt.getX(), evt.getY());
            processData();
        }
    }//GEN-LAST:event_customerTableMouseReleased

    private void companyNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_companyNameFieldActionPerformed
        onModify();
    }//GEN-LAST:event_companyNameFieldActionPerformed

    private void contactFirstFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contactFirstFieldActionPerformed
        onModify();
    }//GEN-LAST:event_contactFirstFieldActionPerformed

    private void contactLastFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contactLastFieldActionPerformed
        onModify();
    }//GEN-LAST:event_contactLastFieldActionPerformed

    private void phoneNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberFieldActionPerformed
        onModify();
    }//GEN-LAST:event_phoneNumberFieldActionPerformed

    private void streetAddressFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streetAddressFieldActionPerformed
        onModify();
    }//GEN-LAST:event_streetAddressFieldActionPerformed

    private void cityFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cityFieldActionPerformed
        onModify();
    }//GEN-LAST:event_cityFieldActionPerformed

    private void stateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stateComboActionPerformed
        onModify();
    }//GEN-LAST:event_stateComboActionPerformed

    /**
     * Called when a field is modified.
     */
    private void onModify() {
        if (!saveButton.getText().contains("(Locked)")) {
            this.saveButton.setText("Save *");
            this.saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
        }
    }

    /**
     * Set the values of the fields using the data of a customer
     * from the selected row.
     * 
     * @param row Row to get data from
     */
    public void setFields(int row) {
        try {
            //Get Customer ID from Table
            int cusId = (int) customerTable.getValueAt(row, 0);
            this.idField.setText(String.valueOf(cusId));

            Customer customer = customerManager.getCustomer(cusId);
            this.companyNameField.setText(customer.getCompanyName());
            this.contactFirstField.setText(customer.getContactFirst());
            this.contactLastField.setText(customer.getContactLast());
            this.phoneNumberField.setText(customer.getPhoneNumber());
            this.emailField.setText(customer.getEmailAddress());
            this.streetAddressField.setText(customer.getStreetAddress());
            this.cityField.setText(customer.getCityName());
            this.stateCombo.setSelectedItem(customer.getState());

            if (!customerManager.exists(cusId)) {
                this.saveButton.setText("Save (Locked)");
                this.saveButton.setEnabled(false);
            } else {
                this.saveButton.setText("Save");
                this.saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN));
            }
        } catch (IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog(null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cityField;
    private javax.swing.JLabel cityLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField companyNameField;
    private javax.swing.JLabel companyNameLabel;
    private javax.swing.JTextField contactFirstField;
    private javax.swing.JLabel contactFirstLabel;
    private javax.swing.JTextField contactLastField;
    private javax.swing.JLabel contactLastLabel;
    private javax.swing.JTable customerTable;
    private javax.swing.JTextField emailField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton newCustomerButton;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JLabel phoneNumberLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JComboBox stateCombo;
    private javax.swing.JLabel stateLabel;
    private javax.swing.JTextField streetAddressField;
    private javax.swing.JLabel streetAddressLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onSwitchTo(Object object) {
        SwingWorker swingWorker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                processData();
                return null;
            }
            
        };
        swingWorker.execute();
    }

    @Override
    public void onSwitchFrom() {
        try {
            customerManager.releaseAllLocks();
        } catch (IOException ex) {
            Logger.getLogger(CustomerDetailPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
