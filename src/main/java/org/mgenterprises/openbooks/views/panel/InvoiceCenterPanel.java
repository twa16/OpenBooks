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

package org.mgenterprises.openbooks.views.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.mgenterprises.openbooks.OpenbooksCore;
import org.mgenterprises.openbooks.configuration.ConfigurationManager;
import org.mgenterprises.openbooks.customer.Customer;
import org.mgenterprises.openbooks.customer.CustomerManager;
import org.mgenterprises.openbooks.invoicing.invoice.Invoice;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceManager;
import org.mgenterprises.openbooks.invoicing.item.Item;
import org.mgenterprises.openbooks.views.OBCardLayout;
import org.mgenterprises.openbooks.views.ViewChangeListener;
import org.mgenterprises.openbooks.views.actionlistener.DeleteCustomerActionListener;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceCenterPanel extends JPanel implements ViewChangeListener{

    private final OpenbooksCore openbooksCore;
    private final ConfigurationManager configurationManager;
    private final InvoiceManager invoiceManager;
    private final CustomerManager customerManager;
    private SimpleDateFormat simpleDateFormat;
    
    public InvoiceCenterPanel(OpenbooksCore openbooksCore) {
        this.openbooksCore = openbooksCore;
        this.configurationManager = openbooksCore.getConfigurationManager();
        this.invoiceManager = openbooksCore.getInvoiceManager();
        this.customerManager = openbooksCore.getCustomerManager();
        simpleDateFormat  = new SimpleDateFormat(configurationManager.getValue("dateFormatString"));
        initComponents();
        loadInvoiceList();
    }
    
    protected void loadInvoiceList() {
        try {
        Invoice[] invoices = invoiceManager.getInvoices();
        
        ((DefaultTableModel)invoiceTable.getModel()).setRowCount(0);
        double total = 0.0;
        double paid = 0.0;
        DefaultTableModel defaultTableModel = (DefaultTableModel) invoiceTable.getModel();
        for(Invoice invoice : invoices) {
            Object[] data = new Object[6];
            data[0]=invoice.getInvoiceNumber();
            Customer customer = customerManager.getCustomer(invoice.getCustomerID());
            data[1]=customer.toString();
            data[2]=simpleDateFormat.format(invoice.getDateDue());
            data[3]=invoice.getTotal()-invoice.getAmountPaid();
            data[4]=invoice.getTotal();
            data[5]=(invoice.getTotal()==invoice.getAmountPaid());
            defaultTableModel.addRow(data);
            
            total+=invoice.getTotal();
            paid+=invoice.getAmountPaid();
        }
        this.invoiceTable.setModel(defaultTableModel);
        this.totalPaid.setText(String.valueOf(paid));
        this.totalUnpaid.setText(String.valueOf(total));
        }
        catch(IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
        ((DefaultTableModel)invoiceTable.getModel()).fireTableDataChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        totalUnpaid = new javax.swing.JLabel();
        totalUnpaidLabel = new javax.swing.JLabel();
        customerComboBox = new javax.swing.JComboBox();
        customerLabel = new javax.swing.JLabel();
        invoiceStatusLabel = new javax.swing.JLabel();
        totalPaidLabel = new javax.swing.JLabel();
        totalPaid = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        invoiceTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        startDateField = new javax.swing.JFormattedTextField();
        endDateField = new javax.swing.JFormattedTextField();
        jLabel6 = new javax.swing.JLabel();
        paidCheckbox = new javax.swing.JCheckBox();
        pastdueCheckbox = new javax.swing.JCheckBox();
        unpaidCheckbox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        newInvoiceButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(650, 650));

        totalUnpaid.setText("Loading...");

        totalUnpaidLabel.setText("Total Unpaid:");

        customerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        customerLabel.setText("Customer");

        invoiceStatusLabel.setText("Status");

        totalPaidLabel.setText("Total Paid:");

        totalPaid.setText("Loading...");

        invoiceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Customer", "Due Date", "Amount Due", "Total", "Paid"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        invoiceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                invoiceTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(invoiceTable);
        if (invoiceTable.getColumnModel().getColumnCount() > 0) {
            invoiceTable.getColumnModel().getColumn(0).setMinWidth(50);
            invoiceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            invoiceTable.getColumnModel().getColumn(0).setMaxWidth(100);
            invoiceTable.getColumnModel().getColumn(1).setResizable(false);
            invoiceTable.getColumnModel().getColumn(2).setMinWidth(150);
            invoiceTable.getColumnModel().getColumn(2).setPreferredWidth(150);
            invoiceTable.getColumnModel().getColumn(2).setMaxWidth(150);
            invoiceTable.getColumnModel().getColumn(3).setMinWidth(200);
            invoiceTable.getColumnModel().getColumn(3).setPreferredWidth(200);
            invoiceTable.getColumnModel().getColumn(3).setMaxWidth(500);
            invoiceTable.getColumnModel().getColumn(4).setMinWidth(200);
            invoiceTable.getColumnModel().getColumn(4).setPreferredWidth(200);
            invoiceTable.getColumnModel().getColumn(4).setMaxWidth(500);
            invoiceTable.getColumnModel().getColumn(5).setMinWidth(35);
            invoiceTable.getColumnModel().getColumn(5).setPreferredWidth(35);
            invoiceTable.getColumnModel().getColumn(5).setMaxWidth(35);
        }

        jLabel5.setText("Start Date");

        startDateField.setText("jFormattedTextField1");

        endDateField.setText("jFormattedTextField2");

        jLabel6.setText("End Date");

        paidCheckbox.setText("Paid");

        pastdueCheckbox.setText("Pastdue");

        unpaidCheckbox.setText("Unpaid");

        jButton1.setText("Search");

        newInvoiceButton.setText("New Invoice");
        newInvoiceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInvoiceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(newInvoiceButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(totalUnpaidLabel)
                            .addComponent(totalPaidLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(totalUnpaid, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalPaid)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(customerLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(customerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(invoiceStatusLabel))
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(paidCheckbox)
                                .addGap(2, 2, 2)
                                .addComponent(unpaidCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                                .addComponent(jCheckBox2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pastdueCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(endDateField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(customerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(customerLabel)
                        .addComponent(invoiceStatusLabel)
                        .addComponent(jLabel5)
                        .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(paidCheckbox)
                        .addComponent(unpaidCheckbox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(endDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(pastdueCheckbox)
                            .addComponent(jButton1))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(totalPaidLabel)
                            .addComponent(totalPaid))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(totalUnpaid)
                            .addComponent(totalUnpaidLabel)))
                    .addComponent(newInvoiceButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void invoiceTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_invoiceTableMouseClicked
        try {
            System.out.println(evt.getButton());
            int row = invoiceTable.rowAtPoint(evt.getPoint());
            int col = invoiceTable.columnAtPoint(evt.getPoint());
            if (row >= 0 && col >= 0) {
                if(evt.getClickCount()==2 && evt.getButton()==MouseEvent.BUTTON1) { 
                        Object object = invoiceTable.getModel().getValueAt(row, 0);
                        int id = Integer.parseInt(object.toString());
                        Invoice invoice = invoiceManager.getInvoice(id);
                        JPanel mainPanelArea = openbooksCore.getMainPanel();
                        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
                        cl.show(mainPanelArea, "InvoiceUpdatePanel", invoice);

                }
                else if(evt.getButton() != MouseEvent.BUTTON1) {
                    Object object = invoiceTable.getModel().getValueAt(row, 0);
                    int id = Integer.parseInt(object.toString());
                    Invoice invoice = invoiceManager.getInvoice(id);
                    RightClickPopupMenu rcpm = new RightClickPopupMenu(openbooksCore, this, invoice);
                    rcpm.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(InvoiceCenterPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_invoiceTableMouseClicked

    private void newInvoiceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInvoiceButtonActionPerformed
        JPanel mainPanelArea = openbooksCore.getMainPanel();
        OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
        cl.show(mainPanelArea, "InvoiceUpdatePanel", null);
    }//GEN-LAST:event_newInvoiceButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox customerComboBox;
    private javax.swing.JLabel customerLabel;
    private javax.swing.JFormattedTextField endDateField;
    private javax.swing.JLabel invoiceStatusLabel;
    private javax.swing.JTable invoiceTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton newInvoiceButton;
    private javax.swing.JCheckBox paidCheckbox;
    private javax.swing.JCheckBox pastdueCheckbox;
    private javax.swing.JFormattedTextField startDateField;
    private javax.swing.JLabel totalPaid;
    private javax.swing.JLabel totalPaidLabel;
    private javax.swing.JLabel totalUnpaid;
    private javax.swing.JLabel totalUnpaidLabel;
    private javax.swing.JCheckBox unpaidCheckbox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onSwitchTo(Object object) {
        SwingWorker switchFromWorker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                loadInvoiceList();
                return null;
            }
            
        };
        switchFromWorker.execute();
    }

    @Override
    public void onSwitchFrom() {
    }
}
class RightClickPopupMenu extends JPopupMenu {
    private JMenuItem deleteItem = new JMenuItem("Delete");
    private JMenuItem editItem = new JMenuItem("Edit");

    public RightClickPopupMenu(final OpenbooksCore openbooksCore, final InvoiceCenterPanel invoiceCenterPanel, final Invoice invoice) {
        add(deleteItem);
        add(editItem);
        
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to delete invoice number "+invoice.getInvoiceNumber(),"Warning",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    try {
                        openbooksCore.getInvoiceManager().remove(String.valueOf(invoice.getInvoiceNumber()));
                        invoiceCenterPanel.loadInvoiceList();
                    } catch (IOException ex) {
                        Logger.getLogger(RightClickPopupMenu.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
                    }
                }
            }

        });
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel mainPanelArea = openbooksCore.getMainPanel();
                OBCardLayout cl = (OBCardLayout)(mainPanelArea.getLayout());
                cl.show(mainPanelArea, "InvoiceUpdatePanel", invoice);
            }

        });
    }
    
    
}