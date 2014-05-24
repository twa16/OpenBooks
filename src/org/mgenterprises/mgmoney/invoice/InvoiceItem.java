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

package org.mgenterprises.mgmoney.invoice;

import org.mgenterprises.mgmoney.item.Item;

/**
 *
 * @author Manuel Gauto
 */
public class InvoiceItem extends Item{
    private int quantity;
    private double price;
    
    public InvoiceItem() {
    }
    
    public InvoiceItem(Item item){
        super(item.getName(), item.getDescription(), item.getBasePrice());
        price=item.getBasePrice();
    }

    public InvoiceItem(int quantity, String name, String description, double basePrice) {
        super(name, description, basePrice);
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString(){
        return getName();
    }
    
    public Object[] formatForTable() {
        Object[] data = new Object[5];
        data[0] = getName();
        data[1] = getDescription();
        data[2] = getPrice();
        data[3] = getQuantity();
        data[4] = getPrice()*getQuantity();
        return data;
    }
}
