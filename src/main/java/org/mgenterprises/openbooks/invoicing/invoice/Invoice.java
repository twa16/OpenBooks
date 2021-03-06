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
package org.mgenterprises.openbooks.invoicing.invoice;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.hibernate.annotations.Cascade;
import org.mgenterprises.openbooks.customer.Customer;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Invoice extends Saveable implements Serializable {

    private long invoiceNumber;
    private Date dateCreated;
    private double amountPaid;
    private double taxRate;
    private String currencySymbol = "$";
    private Date datePaid = new Date(0);
    private Date dateDue = new Date(System.currentTimeMillis() + (86400000 * 30));
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

    public void setInvoiceNumber(long invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @Transient
    public String getDateCreatedString() {
        return new SimpleDateFormat("MM/dd/yyyy").format(dateCreated);
    }
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
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

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "invoice_invoiceitems", joinColumns = {
        @JoinColumn(name = "Invoice_ID")}, inverseJoinColumns = {
        @JoinColumn(name = "Item_ID")})
    @OrderColumn(name="invoiceitem_index")
    public InvoiceItem[] getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(InvoiceItem[] invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    @Transient
    public String getDateDueString() {
        return new SimpleDateFormat("MM/dd/yyyy").format(dateDue);
    }
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getDateDue() {
        return dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    @Transient
    public double getSubTotal() {
        double total = 0;
        for (InvoiceItem invoiceItems : getInvoiceItems()) {
            total += invoiceItems.getPrice();
        }
        return total;
    }
    
    @Transient
    public double getTax() {
        return (getSubTotal()*taxRate);
    }
    
    @Transient
    public double getTotal() {
        return getTax()+getSubTotal();
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
