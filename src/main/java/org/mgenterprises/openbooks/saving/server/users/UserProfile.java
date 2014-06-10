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

package org.mgenterprises.openbooks.saving.server.users;

import java.util.ArrayList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.saving.server.access.ACTION;
import org.mgenterprises.openbooks.saving.server.access.AccessRight;

/**
 *
 * @author mgauto
 */
@Entity
public class UserProfile extends Saveable{
    private String username;
    private String passwordHash;
    private ArrayList<AccessRight> accessRights = new ArrayList<AccessRight>();

    public UserProfile() {
    }

    public UserProfile(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Id
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public ArrayList<AccessRight> getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(ArrayList<AccessRight> accessRights) {
        this.accessRights = accessRights;
    }
    
    public void addAccessRight(String accessRight, ACTION action) {
        this.accessRights.add(new AccessRight(accessRight, action));
    }
    
    public boolean hasAccessRight(String accessRightString, ACTION action) {
        boolean hasRight = false;
        for(AccessRight accessRight : accessRights) {
            if(accessRight.getType().equals(accessRightString) && accessRight.getAction()==action){
                hasRight = true;
            }
        }
        return hasRight;
    }

    @Override
    public String getSaveableModuleName() {
        return this.getClass().getName();
    }

    @Column(unique=true)
    @Override
    public String getUniqueId() {
        return username;
    }

    @Override
    public void setUniqueId(String id) {
        this.username = id;
    }
}
