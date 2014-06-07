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
    String type = new Change().getSaveableModuleName();
    private SaveManager saveManager;
    private int cacheCount=100;
    private long changeID = 0;
    private long cacheStartId;
    private HashMap<Long, Change> changeMap = new HashMap<Long, Change>();

    public ChangeJournal(SaveManager saveManager) {
        this.saveManager = saveManager;
        long curSize = saveManager.getSaveableCount(type);
        String[] keys = {"changeId"};
        EqualityOperation[] operations = {EqualityOperation.GREATER_EQUALS};
        long startIndex = curSize-cacheCount;
        String[] values = {String.valueOf(startIndex)};
        String[] conjunctions = {""};
        Logger.getLogger("SaveServer").info("Caching last "+cacheCount+" changes");
        Saveable[] saveables = saveManager.getWhere(type, keys, operations, values, conjunctions);
        Logger.getLogger("SaveServer").info("Changes retrieved");
        Change[] changes = (Change[]) saveables;
        for(Change change : changes) {
            this.changeMap.put(change.getChangeId(), change);
            cacheStartId=change.getChangeId();
        }
        Logger.getLogger("SaveServer").info("Changes stored");
    }
    
    public Change[] getChangesSince(long id) {
        if(id>=cacheStartId) {
            int responseSize = changeMap.size()-((int)id-(int)cacheStartId);
            Change[] response = new Change[responseSize];
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
            Change[] changes = (Change[]) saveables;
            
            return changes;
        }
    }
    
    public Change getChange(long id) {
        Change change = changeMap.get(id);
        if(change==null) {
            change = (Change) saveManager.getSaveable(type, String.valueOf(id));
        }
        return change;
    }
    
    public synchronized void recordChange(Change change) {
        saveManager.persistSaveable(type, "SERVER", change);
        this.changeMap.put(change.getChangeId(), change);
    }
}
