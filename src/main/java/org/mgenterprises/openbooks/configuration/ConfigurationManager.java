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

package org.mgenterprises.openbooks.configuration;

import com.sun.org.apache.bcel.internal.generic.Type;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class ConfigurationManager {
    private HashMap<String, String> configuration = new HashMap<String, String>();

    public void loadDefaultConfiguration(){
        insertIfNotSet("dateFormatString", "MM/dd/yyyy");
        
        insertIfNotSet("defaultDueDateDelay", "30");
    }
    
    public boolean insertIfNotSet(String key, String configurationValue) {
        if(configuration.containsKey(key)) return false;
        //This isn't needed but I like elses
        else {
            configuration.put(key, configurationValue);
            return true;
        }
    }
    
    public String getValue(String key) {
        return configuration.get(key);
    }
    
    public void put(String key, String value) {
        configuration.put(key, value);
    }
    
}
