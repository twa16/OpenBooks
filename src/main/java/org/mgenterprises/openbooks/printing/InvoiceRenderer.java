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
package org.mgenterprises.openbooks.printing;

import com.lowagie.text.DocumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mgenterprises.openbooks.OpenbooksCore;
import org.mgenterprises.openbooks.company.CompanyProfile;
import org.mgenterprises.openbooks.customer.Customer;
import org.mgenterprises.openbooks.invoicing.invoice.Invoice;
import org.mgenterprises.openbooks.invoicing.invoice.InvoiceItem;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceRenderer {
    private OpenbooksCore openbooksCore;
    private Invoice invoice;
    private InvoiceTemplate invoiceTemplate;

    public InvoiceRenderer(OpenbooksCore openbooksCore, Invoice invoice, InvoiceTemplate invoiceTemplate) {
        this.openbooksCore = openbooksCore;
        this.invoice = invoice;
        this.invoiceTemplate = invoiceTemplate;
    }

    private String invoiceToHTML() throws IOException {
        //Get template
        String result = invoiceTemplate.getContentHTML();

        //Add company info
        result = result.replace(InvoiceTemplate.COMPANY_INFO_KEY, openbooksCore.getCompanyProfile().toString());
        result = result.replace(InvoiceTemplate.COMPANY_NAME_KEY, openbooksCore.getCompanyProfile().getCompanyName());
        
        //Add customer
        Customer customer = openbooksCore.getCustomerManager().getCustomer(invoice.getCustomerID());
        String customerData = getCustomerDescription(customer);
        result = result.replace(InvoiceTemplate.CLIENT_INFO, customerData);
        result = result.replace(InvoiceTemplate.CLIENT_NAME, customer.getCompanyName());
        
        //Add invoice items
        String invoiceItems = processInvoiceItems();
        result = result.replace(InvoiceTemplate.TABLE_KEY, invoiceItems);
        
        return result;
    }

    public String getCustomerDescription(Customer customer) {
        StringBuilder customerInfo = new StringBuilder();
        if(customer.getCompanyName().length()>0) {
            customerInfo.append(customer.getCompanyName());
            customerInfo.append("\n");
        }
        if(customer.getContactFirst().length()>0 || customer.getContactLast().length()>0) {
            customerInfo.append(customer.getContactFirst());
            customerInfo.append(" ");
            customerInfo.append(customer.getContactLast());
            customerInfo.append("\n");
        }
        if(customer.getStreetAddress().length()>0) {
            customerInfo.append(customer.getStreetAddress());
            customerInfo.append("\n");
            customerInfo.append(customer.getCityName());
            customerInfo.append("\n");
            customerInfo.append(customer.getState());
            customerInfo.append("\n");
        }
        if(customer.getPhoneNumber().length()>0) {
            customerInfo.append(customer.getPhoneNumber());
            customerInfo.append("\n");
        }
        if(customer.getEmailAddress().length()>0) {
            customerInfo.append(customer.getEmailAddress());
        }
        return customerInfo.toString();
    }
    private String processInvoiceItems() {
        String regexForTableRow = InvoiceTemplate.TABLE_ROW_START + "(.+)" + InvoiceTemplate.TABLE_ROW_END;
        Pattern pattern = Pattern.compile(regexForTableRow);
        Matcher matcher = pattern.matcher(invoiceTemplate.getContentHTML());
        String tableRowTemplate = matcher.group(1);

        pattern = Pattern.compile("{{InvoiceItem_(.+)}}");
        StringBuilder tableRowBuilder = new StringBuilder();
        for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
            String row = tableRowTemplate;
            matcher = pattern.matcher(row);
            while(matcher.find()) {
                String fieldName = matcher.group();
                String value = getField(invoiceItem, fieldName);
                row.replace("{{InvoiceItem_"+fieldName+"}}", value);
            }
            tableRowBuilder.append(row);
            tableRowBuilder.append("\n");
        }
        return tableRowBuilder.toString();
    }

    private String getField(InvoiceItem invoiceItem, String fieldName) {
        try {
            Class clazz = invoiceItem != null ? invoiceItem.getClass() : null;
            if (clazz == null) {
                return null;
            }
            String getterName = "get" + fieldName;
            Method method = clazz.getMethod(getterName);
            Object valueObject = method.invoke(invoiceItem, (Object[]) null);
            return valueObject != null ? valueObject.toString() : "";
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(InvoiceRenderer.class.getName()).log(Level.SEVERE, null, ex);
            return "NotFound";
        } catch (SecurityException ex) {
            Logger.getLogger(InvoiceRenderer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(InvoiceRenderer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(InvoiceRenderer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(InvoiceRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERROR";
    }

    public File renderAsPDF(File outputFile) throws DocumentException, IOException {
        FileOutputStream os = new FileOutputStream(outputFile);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(this.invoiceToHTML());
        renderer.layout();
        renderer.createPDF(os);

        os.close();
        return outputFile;
    }
}
