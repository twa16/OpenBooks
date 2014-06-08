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

package org.mgenterprises.openbooks.saving.server.journal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mgenterprises.openbooks.saving.EqualityOperation;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.saving.server.SaveManager;

/**
 *
 * @author Manuel Gauto
 */
public class ChangeJournal {
    String type = new ChangeRecord().getSaveableModuleName();
    private SaveManager saveManager;
    private int cacheCount=100;
    private long changeId = 0;
    private long cacheStartId;
    private HashMap<Long, ChangeRecord> changeMap = new HashMap<Long, ChangeRecord>();

    public ChangeJournal(SaveManager saveManager) {
        this.saveManager = saveManager;
        
        //Get current size of journal
        long curSize = saveManager.getSaveableCount(type);
        //Get the last Id used
        changeId = curSize-1;
        //Lets grab that last 100 entries and cache them
        String[] keys = {"changeId"};
        EqualityOperation[] operations = {EqualityOperation.GREATER_EQUALS};
        long startIndex = curSize-cacheCount;
        String[] values = {String.valueOf(startIndex)};
        String[] conjunctions = {""};
        Logger.getLogger("SaveServer").info("Caching last "+cacheCount+" ChangeRecords");
        Saveable[] saveables = saveManager.getWhere(type, keys, operations, values, conjunctions);
        
        //If there are some entries lets cast them
        if(saveables.length>0) {
            Logger.getLogger("SaveServer").info("ChangeRecords retrieved");
            
            for(Saveable saveable : saveables) {
                ChangeRecord changeRecord = (ChangeRecord) saveable;
                changeMap.put(changeRecord.getChangeId(), changeRecord);
            }
            Logger.getLogger("SaveServer").info("ChangeRecords stored");
        } else {
            Logger.getLogger("SaveServer").info("Journal Empty");
        }
    }
    
    /**
     * Retrieves all ChangeRecords that are logged since the id indicated
     * 
     * @param id Id to start search at
     * @return Requested ChangeRecords
     */
    public ChangeRecord[] getChangesSince(long id) {
        if(id>=cacheStartId) {
            int responseSize = changeMap.size()-((int)id-(int)cacheStartId);
            ChangeRecord[] response = new ChangeRecord[responseSize];
            for(int i = 0; i < responseSize; i++) {
                response[i] = changeMap.get(0);
            }
            return response;
        } else {
            String[] keys = {"changeId"};
            EqualityOperation[] operations = {EqualityOperation.GREATER_EQUALS};
            String[] values = {String.valueOf(id)};
            String[] conjunctions = {""};
            Saveable[] saveables = saveManager.getWhere(type, keys, operations, values, conjunctions);
            ChangeRecord[] changes = (ChangeRecord[]) saveables;
            
            return changes;
        }
    }
    
    /**
     * Get a ChangeRecord by ID
     * 
     * @param id Id to search for
     * @return Requested ChangeRecord
     */
    public ChangeRecord getChange(long id) {
        ChangeRecord change = changeMap.get(id);
        if(change==null) {
            change = (ChangeRecord) saveManager.getSaveable(type, String.valueOf(id));
            saveManager.removeLock(type, String.valueOf(id));
        }
        return change;
    }
    
    /**
     * Returns the latest change id recorded in the journal
     * 
     * @return Latest change id 
     */
    public long getLatestChangeId() {
        return this.changeId;
    }
    
    /**
     * Save ChangeRecord to Journal
     * 
     * @param change ChangeRecord to save
     */
    public synchronized long recordChange(ChangeRecord change) {
        changeId++;
        change.setChangeId(changeId);
        saveManager.persistSaveable(type, "SERVER", change);
        this.changeMap.put(change.getChangeId(), change);
        return changeId;
    }
}
