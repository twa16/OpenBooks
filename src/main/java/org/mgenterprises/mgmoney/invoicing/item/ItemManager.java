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

package org.mgenterprises.mgmoney.invoicing.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.saving.SaveServerConnection;
import org.mgenterprises.mgmoney.saving.Saveable;
import org.mgenterprises.mgmoney.saving.ServerBackedMap;

/**
 *
 * @author Manuel Gauto
 */
public class ItemManager{
    private ServerBackedMap<Item> items;

    public ItemManager(SaveServerConnection saveServerConnection) {
        this.items = new ServerBackedMap<Item>(new Item(),
                                               saveServerConnection);
    }
    
    public void addItem(Item item) throws IOException{
        items.put(item);
    }
    
    public void deleteItem(String itemName) throws IOException {
        items.remove(itemName);
    }
    
    public Item getItem(String name) throws IOException {
        return items.get(name);
    }
    
    public void updateItem(Item item) throws IOException {
        addItem(item);
    }
    
    public void renameItem(String oldItemName, Item item) throws IOException{
        items.remove(oldItemName);
        addItem(item);
    }
    
    public Item[] getItems() throws IOException {
        ArrayList<Item> itemsList =  items.values();
        Item[] temp = new Item[itemsList.size()];
        return itemsList.toArray(temp);
    }
}
