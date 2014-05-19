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

package org.mgenterprises.mgmoney.views.panel;

import com.google.gson.Gson;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.mgenterprises.mgmoney.customer.Customer;
import org.mgenterprises.mgmoney.customer.CustomerManager;
import org.mgenterprises.mgmoney.invoice.Invoice;
import org.mgenterprises.mgmoney.invoice.InvoiceItem;
import org.mgenterprises.mgmoney.invoice.InvoiceManager;
import org.mgenterprises.mgmoney.item.Item;
import org.mgenterprises.mgmoney.views.actionlistener.TableCellListener;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceUpdatePanel extends javax.swing.JPanel {
    private InvoiceManager invoiceManager;
    private CustomerManager customerManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private Item[] allPossibleItems;
    /**
     * Creates new form InvoiceUpdatePanel
     */
    public InvoiceUpdatePanel(CustomerManager customerManager, InvoiceManager invoiceManager, Item[] allPossibleInvoiceItems, Invoice invoice) {
        this.customerManager = customerManager;
        this.invoiceManager = invoiceManager;
        initComponents();
        this.allPossibleItems = allPossibleInvoiceItems;
        setupTableView();
        loadInvoiceData(invoice);
    }
    
    public InvoiceUpdatePanel(CustomerManager customerManager, InvoiceManager invoiceManager, Item[] allPossibleInvoiceItems) {
        this.customerManager = customerManager;
        initComponents();
        this.invoiceManager = invoiceManager;
        this.allPossibleItems = allPossibleInvoiceItems;
        setupTableView();
    }
    
    public InvoiceUpdatePanel(CustomerManager customerManager, InvoiceManager invoiceManager, InvoiceItem[] allPossibleInvoiceItems, Invoice invoice, SimpleDateFormat simpleDateFormat){
        this.customerManager = customerManager;
        initComponents();
        this.invoiceManager = invoiceManager;
        this.dateFormat = simpleDateFormat;
        this.allPossibleItems = allPossibleInvoiceItems;
        loadInvoiceData(invoice);
    }
    
    private void setupTableView(){
        this.customerCombobox.setSelectedIndex(-1);
        this.dateCreatedField.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        
        JComboBox itemComboBox = new JComboBox(allPossibleItems);
        this.invoiceItemTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemComboBox));
        int last = invoiceManager.getHighestID();
        this.invoiceNumberField.setText(String.valueOf(last));
        if(invoiceManager.exists(last))
        loadInvoiceData(invoiceManager.getInvoice(last));
    }
    
    private void loadInvoiceData(Invoice invoice){
        DefaultTableModel defaultTableModel = (DefaultTableModel) this.invoiceItemTable.getModel();
        //Top
        this.customerCombobox.setSelectedItem(customerManager.getCustomer(invoice.getCustomerID()));
        this.invoiceNumberField.setText(String.valueOf(invoice.getInvoiceNumber()));
        this.poNumberField.setText(String.valueOf(invoice.getPurchaseOrderNumber()));
        this.dateCreatedField.setText(dateFormat.format(invoice.getDateCreated()));
        
        defaultTableModel.setRowCount(0);
        for(InvoiceItem invoiceItem : invoice.getInvoiceItems()){
            defaultTableModel.addRow(invoiceItem.formatForTable());
        }
        
        //Bottom Row
        this.totalPaid.setText(String.valueOf(invoice.getAmountPaid()));
        this.datePaid.setText(dateFormat.format(invoice.getDatePaid()));
        
        this.invoiceItemTable.setModel(defaultTableModel);
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

        setMinimumSize(new java.awt.Dimension(650, 650));
        setPreferredSize(new java.awt.Dimension(650, 650));

        customerTextArea.setEditable(false);
        customerTextArea.setColumns(20);
        customerTextArea.setRows(5);
        jScrollPane1.setViewportView(customerTextArea);

        customerCombobox.setModel(new javax.swing.DefaultComboBoxModel(customerManager.getCustomers()));
        customerCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerComboboxActionPerformed(evt);
            }
        });

        invoiceNumberField.setText("-1");

        invoiceNumberLabel.setText("Invoice Number");

        poNumberLabel.setText("P.O. Number");

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
            invoiceItemTable.getColumnModel().getColumn(0).setMaxWidth(80);
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(customerCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(previousButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextButton))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(invoiceNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(poNumberLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateCreatedField, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(invoiceNumberField)
                                .addComponent(poNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))))
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
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
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
        int last = Integer.parseInt(this.invoiceNumberField.getText())-1;
        if(invoiceManager.exists(last)){
            Invoice invoice = invoiceManager.getInvoice(last);
            loadInvoiceData(invoice);
        } else {
            JOptionPane.showMessageDialog(null, "The specified invoice does not exist!", "Invoice Not Found",
                                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        int last = Integer.parseInt(this.invoiceNumberField.getText())+1;
        if(invoiceManager.exists(last)){
            Invoice invoice = invoiceManager.getInvoice(last);
            loadInvoiceData(invoice);
        } else{
            JOptionPane.showMessageDialog(null, "The specified invoice does not exist!", "Invoice Not Found",
                                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void invoiceItemTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_invoiceItemTableMousePressed
        int lastRow = invoiceItemTable.getRowCount()-1;
        if((invoiceItemTable.getValueAt(lastRow, 0))!=null) {
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
        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener)e.getSource();
                
                if(tcl.getColumn()==2 || tcl.getColumn()==3) {
                    int quantity = (int) invoiceItemTable.getValueAt(tcl.getRow(), 3);
                    double price = (double) invoiceItemTable.getValueAt(tcl.getRow(), 2);
                    invoiceItemTable.setValueAt(price*quantity, tcl.getRow(), 4);
                    calculateInvoiceTotal();
                } 
            }
        };

        TableCellListener tcl = new TableCellListener(invoiceItemTable, action);
        System.err.println(evt.getPropertyName()+"\nOld: "+evt.getOldValue()+"\nNew: "+evt.getNewValue());
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
            invoice.setPurchaseOrderNumber(Integer.parseInt(poNumberField.getText()));
            
            InvoiceItem[] invoiceItems = new InvoiceItem[invoiceItemTable.getModel().getRowCount()-1];
            for(int i = 0; i < invoiceItemTable.getModel().getRowCount()-1; i++) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setName(((Item) invoiceItemTable.getValueAt(i, 0)).getName());
                invoiceItem.setDescription((String) invoiceItemTable.getValueAt(i, 1));
                invoiceItem.setPrice((double) invoiceItemTable.getValueAt(i, 2));
                invoiceItem.setQuantity((int) invoiceItemTable.getValueAt(i, 3));
                invoiceItems[i] = invoiceItem;
            } 
            invoice.setInvoiceItems(invoiceItems);
            
            invoiceManager.addInvoice(invoice);
        } catch (ParseException ex) {
            Logger.getLogger(InvoiceUpdatePanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch(NullPointerException npe) {
            JOptionPane.showMessageDialog(null, "Please complete the invoice before saving!", "Invoice Incomplete",
                                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        this.customerCombobox.setSelectedIndex(-1);
        this.customerTextArea.setText("");
        this.dateCreatedField.setText(dateFormat.format(new Date(System.currentTimeMillis())));
        this.invoiceNumberField.setText(String.valueOf(invoiceManager.getHighestID()));
        this.poNumberField.setText("");
        ((DefaultTableModel)this.invoiceItemTable.getModel()).setRowCount(0);
        this.totalPaid.setText("0.0");
        this.invoiceTotal.setText("0.0");
        this.datePaid.setText("-");
    }//GEN-LAST:event_newButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amountPaidLabel;
    private javax.swing.JComboBox customerCombobox;
    private javax.swing.JTextArea customerTextArea;
    private javax.swing.JFormattedTextField dateCreatedField;
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
}
