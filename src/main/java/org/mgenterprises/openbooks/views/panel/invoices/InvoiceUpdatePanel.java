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

package org.mgenterprises.openbooks.views.panel.invoices;

import com.google.gson.Gson;
import com.lowagie.text.DocumentException;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.mgenterprises.openbooks.OpenbooksCore;
import org.mgenterprises.openbooks.configuration.ConfigurationManager;
import org.mgenterprises.openbooks.customer.Customer;
import org.mgenterprises.openbooks.customer.CustomerManager;
import org.mgenterprises.openbooks.invoicing.invoice.Invoice;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceItem;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceManager;
import org.mgenterprises.openbooks.invoicing.item.Item;
import org.mgenterprises.openbooks.invoicing.item.ItemManager;
import org.mgenterprises.openbooks.printing.InvoiceRenderer;
import org.mgenterprises.openbooks.printing.RenderTemplate;
import org.mgenterprises.openbooks.views.ViewChangeListener;
import org.mgenterprises.openbooks.views.actionlistener.DeleteCustomerActionListener;
import org.mgenterprises.openbooks.views.actionlistener.TableCellListener;
import org.mgenterprises.openbooks.views.panel.customers.CustomerUpdatePanel;
import org.mgenterprises.openbooks.views.printing.PDFRenderFrame;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceUpdatePanel extends JPanel implements ViewChangeListener{
    private Invoice loadedInvoice = new Invoice();
    private OpenbooksCore openbooksCore;
    private ConfigurationManager configurationManager;
    private InvoiceManager invoiceManager;
    private CustomerManager customerManager;
    private ItemManager itemManager;
    private SimpleDateFormat dateFormat;
    private Item[] allPossibleItems;

    public InvoiceUpdatePanel(OpenbooksCore openbooksCore, Item[] allPossibleInvoiceItems) {
        this.openbooksCore = openbooksCore;
        this.configurationManager = openbooksCore.getConfigurationManager();
        this.customerManager = openbooksCore.getCustomerManager();
        this.invoiceManager = openbooksCore.getInvoiceManager();
        this.itemManager = openbooksCore.getItemManager();
        this.allPossibleItems = allPossibleInvoiceItems;
        this.dateFormat = new SimpleDateFormat(configurationManager.getValue("dateFormatString"));
        initComponents();
    }

    public InvoiceUpdatePanel(OpenbooksCore openbooksCore, Item[] allPossibleInvoiceItems, Invoice loadedInvoice) {
        this.loadedInvoice = loadedInvoice;
        this.openbooksCore = openbooksCore;
        this.allPossibleItems = allPossibleInvoiceItems;
        this.dateFormat = new SimpleDateFormat(configurationManager.getValue("dateFormatString"));
        initComponents();
        setupTableView();
    }
    
    private void setupTableView(){
        try {
            JComboBox itemComboBox = new JComboBox(allPossibleItems);
            this.invoiceItemTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemComboBox));
            clearFields();
            if(invoiceManager.exists(invoiceManager.getHighestID())) {
                long highestId = invoiceManager.getHighestID();
                Invoice invoice = invoiceManager.getInvoice(highestId);
                loadInvoiceData(invoice);
            }
            else {
               newButtonActionPerformed(null);
            }
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }
    
    private void loadInvoiceData(Invoice invoice){
        this.loadedInvoice = invoice;
        try {
            clearFields();
            DefaultTableModel defaultTableModel = (DefaultTableModel) this.invoiceItemTable.getModel();
            //Top
            Customer customer = customerManager.getCustomer(invoice.getCustomerID());
            this.customerCombobox.setSelectedIndex(customer.getCustomerNumber());
            customerManager.releaseLock(new Customer().getSaveableModuleName(), String.valueOf(invoice.getCustomerID()));
            this.invoiceNumberField.setText(String.valueOf(invoice.getInvoiceNumber()));
            this.poNumberField.setText(String.valueOf(invoice.getPurchaseOrderNumber()));
            this.dateCreatedField.setText(dateFormat.format(invoice.getDateCreated()));
            this.dateDueField.setText(dateFormat.format(invoice.getDateDue()));
            defaultTableModel.setRowCount(0);
            for(InvoiceItem invoiceItem : invoice.getInvoiceItems()){
                defaultTableModel.addRow(invoiceItem.formatForTable());
            }
            //add an empty row so they can actually edit
            ((DefaultTableModel)this.invoiceItemTable.getModel()).addRow(new String[]{});


            //Bottom Row
            this.totalPaid.setText(String.valueOf(invoice.getAmountPaid()));
            if(invoice.getDatePaid().getTime()!=0) this.datePaid.setText(dateFormat.format(invoice.getDatePaid()));

            this.invoiceItemTable.setModel(defaultTableModel);
            
            if(invoice.isLocked()) {
                this.saveButton.setEnabled(false);
                this.saveButton.setText("Save (Locked)");
            }
            else {
                this.saveButton.setEnabled(true);
                this.saveButton.setText("Save");
            }
    
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        customerTextArea = new javax.swing.JTextArea();
        customerCombobox = new javax.swing.JComboBox();
        invoiceNumberField = new javax.swing.JTextField();
        invoiceNumberLabel = new javax.swing.JLabel();
        poNumberLabel = new javax.swing.JLabel();
        poNumberField = new javax.swing.JTextField();
        dateCreatedField = new JFormattedTextField(dateFormat);
        jLabel1 = new javax.swing.JLabel();
        invoiceTotal = new javax.swing.JLabel();
        invoiceTotalLabel = new javax.swing.JLabel();
        totalPaid = new javax.swing.JLabel();
        amountPaidLabel = new javax.swing.JLabel();
        datePaidLabel = new javax.swing.JLabel();
        datePaid = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        invoiceItemTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        dateDueField = new javax.swing.JTextField();
        dateDueLabel = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(650, 650));
        setPreferredSize(new java.awt.Dimension(650, 650));

        customerTextArea.setEditable(false);
        customerTextArea.setColumns(20);
        customerTextArea.setRows(5);
        jScrollPane1.setViewportView(customerTextArea);

        try {
            customerCombobox.setModel(new javax.swing.DefaultComboBoxModel(customerManager.getCustomers()));
            customerCombobox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    customerComboboxActionPerformed(evt);
                }
            });
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }

        invoiceNumberField.setText("-1");
        invoiceNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invoiceNumberFieldActionPerformed(evt);
            }
        });

        invoiceNumberLabel.setText("Invoice Number");

        poNumberLabel.setText("P.O. Number");

        poNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                poNumberFieldActionPerformed(evt);
            }
        });

        dateCreatedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateCreatedFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Date Created");

        invoiceTotal.setText("0.0");
        invoiceTotal.setMaximumSize(new java.awt.Dimension(100, 14));
        invoiceTotal.setMinimumSize(new java.awt.Dimension(100, 14));
        invoiceTotal.setName(""); // NOI18N
        invoiceTotal.setPreferredSize(new java.awt.Dimension(100, 14));

        invoiceTotalLabel.setText("Total:");

        totalPaid.setText("0.0");
        totalPaid.setToolTipText("");
        totalPaid.setMaximumSize(new java.awt.Dimension(100, 14));
        totalPaid.setMinimumSize(new java.awt.Dimension(100, 14));
        totalPaid.setPreferredSize(new java.awt.Dimension(100, 14));

        amountPaidLabel.setText("Paid:");

        datePaidLabel.setText("Date Paid:");

        datePaid.setText("-");
        datePaid.setMaximumSize(new java.awt.Dimension(100, 14));
        datePaid.setMinimumSize(new java.awt.Dimension(100, 14));
        datePaid.setName(""); // NOI18N
        datePaid.setPreferredSize(new java.awt.Dimension(100, 14));

        invoiceItemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Item", "Description", "Price", "Quantity", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        invoiceItemTable.getTableHeader().setReorderingAllowed(false);
        invoiceItemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                invoiceItemTableMousePressed(evt);
            }
        });
        invoiceItemTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                invoiceItemTablePropertyChange(evt);
            }
        });
        jScrollPane3.setViewportView(invoiceItemTable);
        if (invoiceItemTable.getColumnModel().getColumnCount() > 0) {
            invoiceItemTable.getColumnModel().getColumn(0).setMinWidth(80);
            invoiceItemTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            invoiceItemTable.getColumnModel().getColumn(0).setMaxWidth(120);
            invoiceItemTable.getColumnModel().getColumn(2).setMinWidth(80);
            invoiceItemTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            invoiceItemTable.getColumnModel().getColumn(2).setMaxWidth(80);
            invoiceItemTable.getColumnModel().getColumn(3).setMinWidth(60);
            invoiceItemTable.getColumnModel().getColumn(3).setPreferredWidth(60);
            invoiceItemTable.getColumnModel().getColumn(3).setMaxWidth(60);
            invoiceItemTable.getColumnModel().getColumn(4).setMinWidth(80);
            invoiceItemTable.getColumnModel().getColumn(4).setPreferredWidth(80);
            invoiceItemTable.getColumnModel().getColumn(4).setMaxWidth(80);
        }

        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        nextButton.setText("Next");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText("Prev");
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        dateDueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateDueFieldActionPerformed(evt);
            }
        });

        dateDueLabel.setText("Date Due");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addComponent(amountPaidLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalPaid, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(datePaidLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datePaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(invoiceTotalLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(invoiceTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(customerCombobox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 181, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(poNumberLabel)
                                    .addComponent(jLabel1)
                                    .addComponent(dateDueLabel)
                                    .addComponent(invoiceNumberLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(dateCreatedField, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                    .addComponent(invoiceNumberField)
                                    .addComponent(poNumberField, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                                    .addComponent(dateDueField)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(previousButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customerCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nextButton)
                    .addComponent(previousButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(saveButton)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(invoiceTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(invoiceTotalLabel)
                                .addComponent(totalPaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(amountPaidLabel)
                                .addComponent(datePaidLabel)
                                .addComponent(datePaid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(newButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(invoiceNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(invoiceNumberLabel))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(poNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(poNumberLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dateCreatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dateDueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dateDueLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void customerComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerComboboxActionPerformed
        Customer customer = (Customer) this.customerCombobox.getSelectedItem();
        if(customer!=null)
        this.customerTextArea.setText(customer.getCompanyName()+"\n"+customer.getStreetAddress()+"\n"+customer.getCityName()+"\n"+customer.getState().toString());
    }//GEN-LAST:event_customerComboboxActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        try {
            int last = Integer.parseInt(this.invoiceNumberField.getText())-1;
            if(last>=0){
                this.invoiceNumberField.setText(String.valueOf(last));
                clearFields();
                if(invoiceManager.exists(last)){
                    Invoice invoice = invoiceManager.getInvoice(last);
                    loadInvoiceData(invoice);
                    invoiceManager.releaseLock(new Invoice().getSaveableModuleName(), String.valueOf(last));
                }
            }
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        try {
            int next = Integer.parseInt(this.invoiceNumberField.getText())+1;
            clearFields();
            //Removed to allow for arbitrary IDs
            //if(next<=invoiceManager.getHighestID()) this.invoiceNumberField.setText(String.valueOf(next));
            this.invoiceNumberField.setText(String.valueOf(next));
            if(invoiceManager.exists(next)){
                Invoice invoice = invoiceManager.getInvoice(next);
                invoiceManager.releaseLock(new Invoice().getSaveableModuleName(), String.valueOf(next));
                loadInvoiceData(invoice);
            }
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void invoiceItemTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_invoiceItemTableMousePressed
        int lastRow = invoiceItemTable.getRowCount()-1;
        if(lastRow>=0 && (invoiceItemTable.getValueAt(lastRow, 0))instanceof Item) {
            DefaultTableModel defaultTableModel = (DefaultTableModel)invoiceItemTable.getModel();
            //Autofill defaults
            Item item = (Item) defaultTableModel.getValueAt(lastRow, 0);
            defaultTableModel.setValueAt(item.getDescription(), lastRow, 1);
            defaultTableModel.setValueAt(item.getBasePrice(), lastRow, 2);
            defaultTableModel.setValueAt(1, lastRow, 3);
            defaultTableModel.setValueAt(item.getBasePrice(), lastRow, 4);
            
            //Insert blank row
            defaultTableModel.insertRow(lastRow+1, new String[]{});
            calculateInvoiceTotal();
        }
    }//GEN-LAST:event_invoiceItemTableMousePressed
  
    private void invoiceItemTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_invoiceItemTablePropertyChange
        invoiceItemTableMousePressed(null);
        int rowindex = invoiceItemTable.getSelectedRow();
        if (rowindex < 0)
            return;
        try {
            int quantity = (int) invoiceItemTable.getValueAt(rowindex, 3);
            double price = (double) invoiceItemTable.getValueAt(rowindex, 2);
            invoiceItemTable.setValueAt(price*quantity, rowindex, 4);
            calculateInvoiceTotal();
            onModify();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_invoiceItemTablePropertyChange

    private void calculateInvoiceTotal() {
        double total = 0.0;
        for(int i = 0; i < invoiceItemTable.getModel().getRowCount(); i++){
            Object content = invoiceItemTable.getValueAt(i, 4);
            if(content != null) {
                double amount = (double) content;
                total+=amount;
            }
        }
        invoiceTotal.setText(String.valueOf(total));
    }
    
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        try {
            int invoiceID = Integer.parseInt(invoiceNumberField.getText());
            Customer customer = (Customer) customerCombobox.getSelectedItem();
            Invoice invoice = new Invoice(invoiceID, customer);
            invoice.setDateCreated(dateFormat.parse(dateCreatedField.getText()));
            if(poNumberField.getText().length()>0) invoice.setPurchaseOrderNumber(Integer.parseInt(poNumberField.getText()));
            
            InvoiceItem[] invoiceItems = new InvoiceItem[invoiceItemTable.getModel().getRowCount()-1];
            for(int i = 0; i < invoiceItemTable.getModel().getRowCount()-1; i++) {
                InvoiceItem invoiceItem = new InvoiceItem();
                
                //Really weird bug. Sometimes this is an item and sometimes a string for now, I'll code for both--MG 5.22.14
                try {
                    invoiceItem.setName(((Item) invoiceItemTable.getValueAt(i, 0)).getName());
                }
                catch(ClassCastException ex) {
                    invoiceItem.setName(((String) invoiceItemTable.getValueAt(i, 0)));
                }
                invoiceItem.setDescription((String) invoiceItemTable.getValueAt(i, 1));
                invoiceItem.setPrice((double) invoiceItemTable.getValueAt(i, 2));
                invoiceItem.setQuantity((int) invoiceItemTable.getValueAt(i, 3));
                invoiceItems[i] = invoiceItem;
            } 
            invoice.setInvoiceItems(invoiceItems);
            
            if(invoiceManager.exists(invoice.getInvoiceNumber())){
                invoiceManager.updateInvoice(invoice);
            }else {
                invoiceManager.addInvoice(invoice);
            }
            
            invoiceManager.releaseLock(new Invoice().getSaveableModuleName(), this.invoiceNumberField.getText());
            this.saveButton.setText("Save");
            this.saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN));
            this.saveButton.setEnabled(false);
        } catch (ParseException ex) {
            Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch(NullPointerException npe) {
            JOptionPane.showMessageDialog(null, "Please complete the invoice before saving!", "Invoice Incomplete",
                                    JOptionPane.ERROR_MESSAGE);
        } catch(Exception ex) {
            Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        clearFields();
        try {
            customerCombobox.setModel(new javax.swing.DefaultComboBoxModel(customerManager.getCustomers()));
            this.saveButton.setText("Save");
            this.saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN));
            
            long nextInvoiceID = invoiceManager.getHighestID();
            this.invoiceNumberField.setText(String.valueOf(nextInvoiceID));
            while(!invoiceManager.tryLock(new Invoice().getSaveableModuleName(), invoiceNumberField.getText())){
                this.invoiceNumberField.setText(String.valueOf(nextInvoiceID));
            }
            loadedInvoice.setLocked(false);
        } catch (IOException ex) {
            Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void poNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_poNumberFieldActionPerformed
        onModify();
    }//GEN-LAST:event_poNumberFieldActionPerformed

    private void dateCreatedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateCreatedFieldActionPerformed
        onModify();
    }//GEN-LAST:event_dateCreatedFieldActionPerformed

    private void dateDueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateDueFieldActionPerformed
        onModify();
    }//GEN-LAST:event_dateDueFieldActionPerformed

    private void invoiceNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invoiceNumberFieldActionPerformed
        try {
            int next = Integer.parseInt(invoiceNumberField.getText());
            if(invoiceManager.exists(next)){
                Invoice invoice = invoiceManager.getInvoice(next);
                invoiceManager.releaseLock(new Invoice().getSaveableModuleName(), String.valueOf(next));
                loadInvoiceData(invoice);
            }
        } catch (IOException ex) {
            Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_invoiceNumberFieldActionPerformed

    private void clearFields() {
        this.customerCombobox.setSelectedIndex(-1);
        this.customerTextArea.setText("");
        this.dateCreatedField.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        this.poNumberField.setText("");
        ((DefaultTableModel)this.invoiceItemTable.getModel()).setRowCount(0);
        ((DefaultTableModel)this.invoiceItemTable.getModel()).addRow(new String[]{});
        this.totalPaid.setText("-");
        this.invoiceTotal.setText("-");
        this.datePaid.setText("-");
        int daysTillDue = Integer.parseInt(this.configurationManager.getValue("defaultDueDateDelay"));
        Calendar c=new GregorianCalendar();
        c.add(Calendar.DATE, daysTillDue);
        Date dueDate=c.getTime();
        this.dateDueField.setText(dateFormat.format(dueDate));
        this.saveButton.setText("Save");
        this.saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN));
    }
    
    private void onModify() {
        if(!loadedInvoice.isLocked()){
            this.saveButton.setText("Save *");
            this.saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
            this.saveButton.setEnabled(true);
        }
        else {
            this.saveButton.setText("Save (Locked)");
            this.saveButton.setEnabled(false);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amountPaidLabel;
    private javax.swing.JComboBox customerCombobox;
    private javax.swing.JTextArea customerTextArea;
    private javax.swing.JFormattedTextField dateCreatedField;
    private javax.swing.JTextField dateDueField;
    private javax.swing.JLabel dateDueLabel;
    private javax.swing.JLabel datePaid;
    private javax.swing.JLabel datePaidLabel;
    private javax.swing.JTable invoiceItemTable;
    private javax.swing.JTextField invoiceNumberField;
    private javax.swing.JLabel invoiceNumberLabel;
    private javax.swing.JLabel invoiceTotal;
    private javax.swing.JLabel invoiceTotalLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton newButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JTextField poNumberField;
    private javax.swing.JLabel poNumberLabel;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel totalPaid;
    // End of variables declaration//GEN-END:variables
   
    @Override
    public void onSwitchTo(Object object) {
        SwingWorker switchFromWorker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    //Add Print option to file
                    JMenuItem printMenuItem = new JMenuItem();
                    printMenuItem.setText("Print Invoice");
                    printMenuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            RenderTemplate template = new RenderTemplate();
                            try {
                                URL templateURL = this.getClass().getResource("/templates/basic_invoice.html");
                                template.load(templateURL);
                                InvoiceRenderer invoiceRenderer = new InvoiceRenderer(openbooksCore, loadedInvoice, template);
                                File tempPDF = File.createTempFile("openbooks", "pdf-print");
                                invoiceRenderer.renderAsPDF(tempPDF);
                                PDFRenderFrame renderFrame = new PDFRenderFrame(tempPDF, "Invoice-"+loadedInvoice.getInvoiceNumber()+".pdf");
                                renderFrame.setVisible(true);
                            } catch (IOException ex) {
                                Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (DocumentException ex) {
                                Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    openbooksCore.getMainmenuBar().getMenu(0).add(printMenuItem);
                    
                    //Update Item choices
                    ArrayList<Item> itemsList = itemManager.values();
                    itemManager.releaseAllLocks();
                    Item[] items = new Item[itemsList.size()];
                    allPossibleItems = itemsList.toArray(items);                    
                    JComboBox itemComboBox = new JComboBox(allPossibleItems);
                    invoiceItemTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemComboBox));
                   
                } catch (IOException ex) {
                    Logger.getLogger(CustomerUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            
        };
        switchFromWorker.execute();
        if(object != null) {
            loadInvoiceData((Invoice) object);
        } else {
            setupTableView();
        }
    }

    @Override
    public void onSwitchFrom() {
        SwingWorker switchFromWorker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    //Remove print item
                    for(int i = 0; i < openbooksCore.getMainmenuBar().getMenu(0).getItemCount(); i++) {
                        JMenuItem e = openbooksCore.getMainmenuBar().getMenu(0).getItem(i);
                        if(e.getText().equals("Print Invoice")) {
                            openbooksCore.getMainmenuBar().getMenu(0).remove(e);
                        }
                    }
                    //Release locks
                    invoiceManager.releaseAllLocks();
                } catch (IOException ex) {
                    Logger.getLogger(CustomerUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            
        };
        switchFromWorker.execute();
    }
}
