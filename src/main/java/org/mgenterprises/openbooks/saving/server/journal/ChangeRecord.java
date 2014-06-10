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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
@Entity
@Table(name="ChangeRecord")
public class ChangeRecord extends Saveable{
    private long changeId;
    private String type;
    private String objectId;

    public ChangeRecord() {
    }
    
    public ChangeRecord(long changeId, String type, String objectId) {
        this.changeId = changeId;
        this.type = type;
        this.objectId = objectId;
    }

    @Id
    public long getChangeId() {
        return changeId;
    }

    public void setChangeId(long chjangeId) {
        this.changeId = chjangeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String getSaveableModuleName() {
        return this.getClass().getName();
    }

    @Column(unique=true)
    @Override
    public String getUniqueId() {
        return String.valueOf(this.changeId);
    }

    @Override
    public void setUniqueId(String id) {
        this.changeId = Long.parseLong(id);
    }
    
    
}
