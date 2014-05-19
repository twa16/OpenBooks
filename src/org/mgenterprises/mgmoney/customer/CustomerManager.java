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

package org.mgenterprises.mgmoney.customer;

import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class CustomerManager extends Saveable{
    private int highestId = 0;
    private HashMap<Integer, Customer> customers = new HashMap<Integer, Customer>();
    
    public void addCustomer(Customer item){
        customers.put(item.getCustomerNumber(), item);
        highestId++;
    }
    
    public Customer[] getCustomers() {
        Customer[] temp = new Customer[customers.size()];
        return customers.values().toArray(temp);
    }
    
    public void updateCustomer(Customer customer) {
        customers.put(customer.getCustomerNumber(), customer);
    }
    
    public boolean exists(int id){
        return customers.containsKey(id);
    }
    
    public void deleteCustomer(int id) {
        customers.remove(id);
    }
    
    public Customer getCustomer(int id){
        return customers.get(id);
    }

    public int getHighestId() {
        return highestId;
    }

    @Override
    public String getSaveableModuleName() {
        return "CustomerManager";
    }
}
