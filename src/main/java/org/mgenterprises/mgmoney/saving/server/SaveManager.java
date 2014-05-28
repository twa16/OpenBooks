/*
 * The MIT License
 *
 * Copyright 2014 Manuel Gauto.
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

package org.mgenterprises.mgmoney.saving.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.mgenterprises.mgmoney.saving.AbstractSaveableAdapter;
import org.mgenterprises.mgmoney.saving.AbstractSaveableArrayAdapter;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 * Manages data access and persistence
 * @author mgauto
 */
public class SaveManager {
    private File saveRootDirectory;
    private Gson gson;

    public SaveManager(File saveRootDirectory) {
        this.saveRootDirectory = saveRootDirectory;
         GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Saveable.class, new AbstractSaveableAdapter());
        //gsonBuilder.registerTypeAdapter(Saveable[].class, new AbstractSaveableArrayAdapter());
        gson = gsonBuilder.create();
    }
    
    public boolean persistSaveable(String holder, Saveable saveable) {
        File saveDir = new File(saveRootDirectory + File.separator + saveable.getSaveableModuleName());
        File saveFile = new File(saveRootDirectory + File.separator + saveable.getSaveableModuleName() + File.separator + saveable.getUniqueId());
        try {
            if(hasLock(saveable.getSaveableModuleName(), saveable.getUniqueId()) && !getLockHolder(saveable.getSaveableModuleName(), saveable.getUniqueId()).equals(holder)) {
                return false;
            }
            else {
                saveDir.mkdirs();
                saveFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
                String json = gson.toJson(saveable, Saveable.class);
                bw.write(json);
                bw.newLine();
                bw.flush();
                bw.close();
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(SaveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void removeLock(String type, String id) {
        File saveFile = new File(saveRootDirectory + File.separator + type + File.separator + id +".lock");
        FileUtils.deleteQuietly(saveFile);
    }
    
    public void removeSaveable(String type, String id) {
        File saveFile = new File(saveRootDirectory + File.separator + type + File.separator + id +".lock");
        FileUtils.deleteQuietly(saveFile);
    }
    
    public void createLock(String holder, String type, String id) {
        File saveFile = new File(saveRootDirectory + File.separator + type + File.separator + id +".lock");
        try {
            saveFile.createNewFile();
            FileWriter fw = new FileWriter(saveFile);
            fw.append(holder);
            fw.append("\n");
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(SaveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean hasLock(String type, String id) {
        return !getLockHolder(type, id).equals("");
    }
    
    public String getLockHolder(String type, String id) {
        File saveFile = new File(saveRootDirectory + File.separator + type + File.separator + id +".lock");
        try {
            BufferedReader br = new BufferedReader(new FileReader(saveFile));
            String data = br.readLine();
            br.close();
            return data;
        } catch (FileNotFoundException ex) {
            return "";
        } catch (IOException ex) {
            Logger.getLogger(SaveManager.class.getName()).log(Level.SEVERE, null, ex);
            return "ERROR";
        }
    }
    
    public Saveable getSaveable(String type, String id) {
        File saveFile = new File(saveRootDirectory + File.separator + type + File.separator + id);
        try {
            BufferedReader br = new BufferedReader(new FileReader(saveFile));
            String data = br.readLine();
            br.close();
            Saveable saveable = gson.fromJson(data, Saveable.class);
            if(getLockHolder(type, id).length()> 0) {
                saveable.setLocked(true);
            }
            else {
                saveable.setLocked(false);
            }
            return saveable;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            Logger.getLogger(SaveManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public Saveable[] getAllSaveables(String type) {
        File dir = new File(saveRootDirectory + File.separator + type);
        if(!dir.exists()) {
            return new Saveable[0];
        }
        File[] files = dir.listFiles();
        
        ArrayList<Saveable> saveableList = new ArrayList<Saveable>();
        for(int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if(!name.contains(".lock")) {
                saveableList.add(getSaveable(type, name));
            }
        }
        Saveable[] saveables = new Saveable[saveableList.size()];
        return saveableList.toArray(saveables);
    }
    
    public int getSaveableCount(String type) {
        File[] files = new File(saveRootDirectory + File.separator + type).listFiles();
        int count = 0;
        for (File file : files) {
            if(!file.getName().contains(".lock")){
                count ++;
            }
        }
        return count;
    }
}
