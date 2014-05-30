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

package org.mgenterprises.mgmoney.invoicing.invoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.customer.Customer;
import org.mgenterprises.mgmoney.saving.SaveServerConnection;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.ServerBackedMap;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceManager{
    private ServerBackedMap<Invoice> invoices;

    public InvoiceManager(SaveServerConnection saveServerConnection) {
         invoices = new ServerBackedMap<Invoice>(new Invoice(),
                                                 saveServerConnection);
    }
    
    public void addInvoice(Invoice item) throws IOException{
        invoices.put(item);
    }
    
    public Invoice getInvoice(int id) throws IOException {
        return invoices.get(String.valueOf(id));
    }
    
    public void updateInvoice(Invoice invoice) throws IOException {
        invoices.put(invoice);
    }
    
    public boolean exists(long id) throws IOException {
        return invoices.existsAndAllowed(String.valueOf(id));
    }
    
    public int removeAllCustomerInvoices(Customer customer) throws IOException{
        int count = 0;
        for(Invoice invoice : invoices.values()) {
            if(invoice.getCustomerID()==customer.getCustomerNumber()){
                invoices.remove(String.valueOf(invoice.getInvoiceNumber()));
                count++;
            }
        }
        return count;
    }

    public int getHighestID() throws IOException {
        return invoices.size();
    }
    
    public Invoice[] getInvoices() throws IOException {
        ArrayList<Invoice> invoiceList = invoices.values();
        Invoice[] invoices = new Invoice[invoiceList.size()];
        return invoiceList.toArray(invoices);
    }
    
    public Invoice[] getCustomerInvoices(Customer customer) throws IOException {
        ArrayList<Invoice> customerInvoices = new ArrayList<Invoice>();
        for(Invoice invoice : invoices.values()) {
            if(invoice.getCustomerID()==customer.getCustomerNumber()){
                customerInvoices.add(invoice);
            }
        }
        Invoice[] temp = new Invoice[customerInvoices.size()];
        return customerInvoices.toArray(temp);
    }
    
    public boolean releaseLock(String type, String id) throws IOException {
        return invoices.releaseLock(type, id);
    }
    
    public boolean tryLock(String type, String id) throws IOException {
        return invoices.tryLock(type, id);
    }

    public ServerBackedMap<Invoice> getInvoiceMap() {
        return invoices;
    }
    
}
