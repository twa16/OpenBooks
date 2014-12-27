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

package org.mgenterprises.openbooks.saving.server;

import org.mgenterprises.openbooks.saving.EqualityOperation;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 * Manages data access and persistence
 * @author mgauto
 */
public interface SaveManager {
    
    public Saveable[] getWhere(String type, String[] keys, EqualityOperation[] operations, String[] values, String[] conjunctions);
    
    public boolean persistSaveable(String type, String holder, Saveable saveable);
    
    public void removeLock(String type, String id);
    
    public void removeSaveable(String type, String id);
    
    public void createLock(String holder, String type, String id);
    
    public boolean hasLock(String type, String id);
    
    public boolean isLockedForUser(String user, String type, String id);
    
    public String getLockHolder(String type, String id);
    
    public Saveable getSaveable(String type, String id);
    
    public Saveable[] getAllSaveables(String type);
    
    public long getSaveableCount(String type);
    
    public long getHighestUniqueId(String type);
}
