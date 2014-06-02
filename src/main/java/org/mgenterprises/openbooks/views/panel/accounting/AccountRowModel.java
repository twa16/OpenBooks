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

package org.mgenterprises.openbooks.views.panel.accounting;

import org.mgenterprises.openbooks.accounting.account.Account;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Manuel Gauto
 */
public class AccountRowModel implements RowModel{
    private String[] columnNames = {"Account Name", "Account Description", "Account Balance"};

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueFor(Object o, int i) {
        Account account = (Account) o;
        switch(i) {
            case 0:
                return account.getAccountName();
            case 1:
                return account.getAccountDescription();
            case 2:
                return account.getAccountBalance();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int i) {
        switch(i) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Double.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return false;
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
        //No editing
    }

    @Override
    public String getColumnName(int i) {
        if(i>columnNames.length-1) return "";
        return columnNames[i];
    }
    
}
