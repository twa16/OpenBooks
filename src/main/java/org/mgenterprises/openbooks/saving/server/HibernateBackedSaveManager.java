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

package org.mgenterprises.openbooks.saving.server;

import com.google.gson.Gson;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.exception.ConstraintViolationException;
import org.mgenterprises.openbooks.saving.EqualityOperation;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class HibernateBackedSaveManager implements SaveManager{
    private final SessionFactory sessionFactory;

    public HibernateBackedSaveManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    
    @Override
    public boolean persistSaveable(String type, String holder, Saveable saveable) {
        String id = saveable.getUniqueId();
        try {
            if(!hasLock(type, id) || getLockHolder(type, id).equals(holder)) {
                Session session = sessionFactory.openSession();
                session.beginTransaction();
                session.saveOrUpdate(Class.forName(saveable.getSaveableModuleName()).cast(saveable));
                session.getTransaction().commit();
                return true;
            }
        } catch(ConstraintViolationException ex) {
            Logger.getLogger(HibernateBackedSaveManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HibernateBackedSaveManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public void removeLock(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.beginTransaction();
        Query query = session.createQuery("delete from ResourceLock where type=:type and id=:id");
        query.setString("type", type);
        query.setString("id", id);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    @Override
    public void removeSaveable(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From "+getClassFromType(type)+" where saveableModuleName=:type AND uniqueId=:id");
        query.setString("type", type);
        query.setString("id", id);
        Saveable saveable = (Saveable) query.uniqueResult();
        session.delete(saveable);
        session.getTransaction().commit();
    }

    @Override
    public void createLock(String holder, String type, String id) {
        if(!hasLock(type, id)) {
            ResourceLock resourceLock = new ResourceLock();
            resourceLock.setHolder(holder);
            resourceLock.setType(type);
            resourceLock.setId(id);
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(resourceLock);
            session.getTransaction().commit();
        }
    }

    @Override
    public boolean hasLock(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From ResourceLock where type=:type and id=:id");
        query.setString("type", type);
        query.setString("id", id);
        ResourceLock resourceLock = (ResourceLock) query.uniqueResult();
        session.getTransaction().commit();
        return resourceLock!=null;
    }

    @Override
    public String getLockHolder(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From ResourceLock where type=:type and id=:id");
        query.setString("type", type);
        query.setString("id", id);
        ResourceLock resourceLock = (ResourceLock) query.uniqueResult();
        session.getTransaction().commit();
        return resourceLock==null ? "" : resourceLock.getHolder();
        //return resourceLock.getHolder();
    }

    @Override
    public Saveable getSaveable(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From "+getClassFromType(type)+" where saveableModuleName=:type AND uniqueId=:id");
        query.setString("type", type);
        query.setString("id", id);
        Saveable saveable = (Saveable) query.uniqueResult();
        session.getTransaction().commit();
        return saveable;
    }

    @Override
    public Saveable[] getAllSaveables(String type) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        String queryString = "From "+getClassFromType(type)+" where saveableModuleName=:type";
        Query query = session.createQuery(queryString);
        query.setString("type", type);
        List list = query.list();
        Saveable[] saveables = new Saveable[list.size()];
        for(int i = 0; i < list.size(); i++){
            saveables[i] = (Saveable) list.get(i);
        }
        session.getTransaction().commit();
        return saveables;
    }

    @Override
    public long getSaveableCount(String type) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Number count = (Number) session.createCriteria(type).setProjection(Projections.rowCount()).uniqueResult();
        session.getTransaction().commit();
        return count==null?0:count.longValue();
    }
    
    public String getClassFromType(String type) {
        String[] parts = type.split("\\.");
        return parts[parts.length-1];
    }

    @Override
    public Saveable[] getWhere(String type, String[] keys, EqualityOperation[] operations, String[] values, String[] conjunctions) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < keys.length; i++) {
            String key = keys[i];
            sb.append(" ");
            sb.append(key);
            sb.append(operations[i]);
            sb.append(values[i]);
            sb.append(" ");
            sb.append(conjunctions[i]);
        }
        String queryS = sb.toString();
        System.out.println("Custom Hibernate Query: "+queryS);
        String queryString = "From "+getClassFromType(type)+" where saveableModuleName=:type AND ("+queryS+")";
        Query query = session.createQuery(queryString);
        query.setString("type", type);
        List list = query.list();
        Saveable[] saveables = new Saveable[list.size()];
        for(int i = 0; i < list.size(); i++){
            saveables[i] = (Saveable) list.get(i);
        }
        session.getTransaction().commit();
        return saveables;
    }

    @Override
    public boolean isLockedForUser(String user, String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From ResourceLock where type=:type and id=:id");
        query.setString("type", type);
        query.setString("id", id);
        ResourceLock resourceLock = (ResourceLock) query.uniqueResult();
        session.getTransaction().commit();
        return resourceLock!=null && !resourceLock.getHolder().equals(user);
    }

    @Override
    public long getHighestUniqueId(String type) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Object critResult = session.createCriteria(type).setProjection(Projections.rowCount()).uniqueResult();
        if(critResult != null) {
            Long maxId = (Long) critResult;
            session.close();
            return maxId;
        }
        else {
            session.close();
            return 0;
        }
    }
}
