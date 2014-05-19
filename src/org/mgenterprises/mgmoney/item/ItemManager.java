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

package org.mgenterprises.mgmoney.item;

import java.util.ArrayList;
import java.util.HashMap;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class ItemManager extends Saveable{
    private HashMap<String, Item> items = new HashMap<String, Item>();
    
    public void addItem(Item item){
        items.put(item.getName(), item);
    }
    
    public void deleteItem(String itemName) {
        items.remove(itemName);
    }
    
    public Item getItem(String name) {
        return items.get(name);
    }
    
    public void updateItem(Item item) {
        addItem(item);
    }
    
    public void renameItem(String oldItemName, Item item){
        items.remove(oldItemName);
        addItem(item);
    }
    
    public Item[] getItems() {
        Item[] temp = new Item[items.size()];
        return items.values().toArray(temp);
    }

    @Override
    public String getSaveableModuleName() {
        return "ItemManager";
    }
}
