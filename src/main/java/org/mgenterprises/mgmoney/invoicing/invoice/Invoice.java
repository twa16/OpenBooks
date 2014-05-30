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

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.mgenterprises.mgmoney.customer.Customer;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
@Entity
public class Invoice extends Saveable{
    private long invoiceNumber;
    private Date dateCreated;
    private double amountPaid;
    private Date datePaid = new Date(0);
    private Date dateDue = new Date(System.currentTimeMillis()+(86400000*30));
    private int purchaseOrderNumber;
    private int customerID;
    private InvoiceItem[] invoiceItems;

    public Invoice(int invoiceNumber, Customer customer) {
        this.invoiceNumber = invoiceNumber;
        this.customerID = customer.getCustomerNumber();
    }

    public Invoice() {
        
    }

    @Id
    public long getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public int getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(int purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "invoice")
    @OrderColumn(name = "invoice_index")
    public InvoiceItem[] getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(InvoiceItem[] invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getDateDue() {
        return dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    @Transient
    public double getTotal(){
        double total = 0;
        for(InvoiceItem invoiceItems : getInvoiceItems()) {
            total+=invoiceItems.getPrice();
        }
        return total;
    }

    @Override
    public String getSaveableModuleName() {
        return this.getClass().getName();
    }

    @Override
    public String getUniqueId() {
        return String.valueOf(getInvoiceNumber());
    }

    @Override
    public void setUniqueId(String id) {
        this.invoiceNumber = Long.parseLong(id);
    }
}
