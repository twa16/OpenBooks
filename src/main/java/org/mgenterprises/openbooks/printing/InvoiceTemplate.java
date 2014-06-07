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

import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceTemplate extends Saveable{
    public static final String TABLE_KEY="{{TABLE}}";
    public static final String TABLE_ROW_START="{{TABLE_ROW_START}}";
    public static final String TABLE_ROW_END="{{TABLE_ROW_END}}";
    public static final String LOGO_KEY="{{LOGO}}";
    public static final String COMPANY_NAME_KEY="{{COMPANY_NAME}}";
    public static final String COMPANY_INFO_KEY="{{COMPANY_INFO}}";
    public static final String CLIENT_NAME="{{CLIENT_NAME}}";
    public static final String CLIENT_INFO="{{CLIENT_INFO}}";
    public static final String INVOICE_NUMBER="{{INVOICE_NUMBER}}";
    public static final String SUBTOTAL="{{SUBTOTAL}}";
    public static final String TAX="{{TAX}}";
    public static final String TOTAL="{{TOTAL}}";
    
    private int templateID;
    private String contentHTML;

    public InvoiceTemplate(){
        
    }
    
    public InvoiceTemplate(int templateID, String contentHTML) {
        this.templateID = templateID;
        this.contentHTML = contentHTML;
    }

    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public String getContentHTML() {
        return contentHTML;
    }

    public void setContentHTML(String contentHTML) {
        this.contentHTML = contentHTML;
    }
    
    @Override
    public String getSaveableModuleName() {
        return this.getClass().getName();
    }

    @Override
    public String getUniqueId() {
        return String.valueOf(templateID);
    }

    @Override
    public void setUniqueId(String id) {
        this.templateID = Integer.parseInt(id);
    }
    
}
