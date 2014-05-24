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

import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.customer.Customer;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceManager extends Saveable {
    private HashMap<Integer, Invoice> invoices = new HashMap<Integer, Invoice>();
    private int highestID = 0;
    
    public void addInvoice(Invoice item){
        invoices.put(item.getInvoiceNumber(), item);
        highestID++;
    }
    
    public Invoice getInvoice(int id) {
        return invoices.get(id);
    }
    
    public void updateInvoice(Invoice invoice) {
        invoices.put(invoice.getInvoiceNumber(), invoice);
    }
    
    public boolean exists(int id) {
        return invoices.containsKey(id);
    }
    
    public int removeAllCustomerInvoices(Customer customer){
        int count = 0;
        for(Invoice invoice : invoices.values()) {
            if(invoice.getCustomerID()==customer.getCustomerNumber()){
                invoices.remove(invoice.getInvoiceNumber());
                count++;
            }
        }
        return count;
    }

    public int getHighestID() {
        return highestID;
    }
    
    public Invoice[] getInvoices() {
        Invoice[] temp = new Invoice[invoices.size()];
        return invoices.values().toArray(temp);
    }
    
    public Invoice[] getCustomerInvoices(Customer customer) {
        ArrayList<Invoice> customerInvoices = new ArrayList<Invoice>();
        for(Invoice invoice : invoices.values()) {
            if(invoice.getCustomerID()==customer.getCustomerNumber()){
                customerInvoices.add(invoice);
            }
        }
        Invoice[] temp = new Invoice[customerInvoices.size()];
        return customerInvoices.toArray(temp);
    }
 
    @Override
    public String getSaveableModuleName() {
        return "InvoiceManager";
    }
}
