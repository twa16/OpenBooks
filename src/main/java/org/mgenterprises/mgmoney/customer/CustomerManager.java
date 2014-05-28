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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.invoicing.invoice.Invoice;
import org.mgenterprises.mgmoney.saving.SaveServerConnection;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.ServerBackedMap;

/**
 *
 * @author Manuel Gauto
 */
public class CustomerManager{
    private int highestId = 0;
    private ServerBackedMap<Customer> customers;
    
    public CustomerManager(SaveServerConnection saveServerConnection) {
         customers = new ServerBackedMap<Customer>(new Customer(),
                                                 saveServerConnection);
    }
    
    public void addCustomer(Customer item) throws IOException{
        customers.put(item);
        highestId++;
    }
    
    public Customer[] getCustomers() throws IOException {
        ArrayList<Customer> customerList = customers.values();
        Customer[] temp = new Customer[customerList.size()];
        return customerList.toArray(temp);
    }
    
    public void updateCustomer(Customer customer) throws IOException {
        customers.put(customer);
    }
    
    public boolean exists(int id) throws IOException{
        return customers.existsAndAllowed(String.valueOf(id));
    }
    
    public void deleteCustomer(int id) throws IOException {
        customers.remove(String.valueOf(id));
    }
    
    public Customer getCustomer(int id) throws IOException{
        return customers.get(String.valueOf(id));
    }

    public int getHighestId() {
        return highestId;
    }

    public ServerBackedMap<Customer> getCustomerMap() {
        return customers;
    }
    
}
