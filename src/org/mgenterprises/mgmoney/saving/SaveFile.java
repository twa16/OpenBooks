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

package org.mgenterprises.mgmoney.saving;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Manuel Gauto
 */
public class SaveFile{
    transient File file;
    HashMap<String, Saveable> saveables = new HashMap<String, Saveable>();

    public SaveFile(File file) {
        this.file = file;
    }

    public HashMap<String, Saveable> getSaveables() {
        return saveables;
    }
    
    public void registerSaveable(Saveable saveable) {
        saveables.put(saveable.getSaveableModuleName(), saveable);
    } 
    
    public boolean exists(String name) {
        return saveables.containsKey(name);
    }
    
    public Saveable getSaveableModule(String name) {
        return saveables.get(name);
    }
    
    public void load() throws FileNotFoundException, IOException, ClassNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        Gson gson = gsonBilder.create();
        SaveFile saveFile = gson.fromJson(bufferedReader.readLine(), SaveFile.class);
        this.saveables = saveFile.getSaveables();
        System.out.println("Loaded data from "+file.getAbsolutePath());
    }
    
    public void save() throws FileNotFoundException, IOException {
        File backup = new File(file.getName()+".bak");
        if(file.exists()){
            FileUtils.deleteQuietly(backup);
            FileUtils.copyFile(file, backup);
            FileUtils.deleteQuietly(file);
        }
        else {
            file.createNewFile();
        }
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        Gson gson = gsonBilder.create();
        String data = gson.toJson(this);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write(data);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        bufferedWriter.close();
        System.out.println("Saved data to "+file.getAbsolutePath());
    }
}
