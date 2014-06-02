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

package org.mgenterprises.openbooks.views.actionlistener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.mgenterprises.openbooks.customer.CustomerManager;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceManager;
import org.mgenterprises.openbooks.util.CustomerUtils;

/**
 *
 * @author Manuel Gauto
 */
public class DeleteCustomerActionListener implements ActionListener {
    private CustomerManager customerManager;
    private InvoiceManager invoiceManager;
    private int customerId;

    public DeleteCustomerActionListener(CustomerManager customerManager, InvoiceManager invoiceManager, int customerId) {
        this.customerManager = customerManager;
        this.invoiceManager = invoiceManager;
        this.customerId = customerId;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        try {
            int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you want to delete "+customerManager.getCustomer(customerId).getCompanyName()+"?\nAll associated invoices will be deleted!", "Warning!", JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION){

                    new CustomerUtils(invoiceManager, customerManager).deleteCustomer(customerManager.getCustomer(customerId));

            }
        } catch (IOException ex) {
            Logger.getLogger(DeleteCustomerActionListener.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showConfirmDialog (null, "Unable to complete requested action because of connection problems.", "Warning!", JOptionPane.OK_OPTION);
        }
    }
    
}
