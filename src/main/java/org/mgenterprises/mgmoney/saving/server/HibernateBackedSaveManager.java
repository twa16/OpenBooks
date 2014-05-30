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

package org.mgenterprises.mgmoney.saving.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.mgenterprises.mgmoney.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class HibernateBackedSaveManager implements SaveManager{
    private final SessionFactory sessionFactory = buildSessionFactory();

    public HibernateBackedSaveManager() {
        buildSessionFactory();
    }
    
    private SessionFactory buildSessionFactory() {
            try {
                    // Use hibernate.cfg.xml to get a SessionFactory
                    return new Configuration().configure().buildSessionFactory();
            } catch (Throwable ex) {
                    System.err.println("SessionFactory creation failed." + ex);
                    throw new ExceptionInInitializerError(ex);
            }
    }

    
    @Override
    public boolean persistSaveable(String type, String holder, Saveable saveable) {
        System.out.println("==="+type+"---"+saveable.getSaveableModuleName());
        String id = saveable.getUniqueId();
        try {
            if(!hasLock(type, id) || getLockHolder(type, id).equals(holder)) {
                Session session = sessionFactory.openSession();
                session.beginTransaction();
                session.save(Class.forName(saveable.getSaveableModuleName()).cast(saveable));
                session.getTransaction().commit();
                return true;
            }
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
        session.getTransaction().commit();
    }

    @Override
    public void removeSaveable(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.beginTransaction();
        Query query = session.createQuery("delete from "+getClassFromType(type)+" where id=:id");
        query.setString("id", id);
        session.getTransaction().commit();
    }

    @Override
    public void createLock(String holder, String type, String id) {
        ResourceLock resourceLock = new ResourceLock();
        resourceLock.setHolder(holder);
        resourceLock.setType(type);
        resourceLock.setId(id);
        
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.persist(resourceLock);
        session.getTransaction().commit();
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
        return resourceLock.getHolder();
    }

    @Override
    public Saveable getSaveable(String type, String id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Query query = session.createQuery("From "+getClassFromType(type)+" where id=:id");
        query.setString("id", id);
        Saveable saveable = (Saveable) query.uniqueResult();
        session.getTransaction().commit();
        return saveable;
    }

    @Override
    public Saveable[] getAllSaveables(String type) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        String queryString = "From "+getClassFromType(type);
        Query query = session.createQuery(queryString);
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
        return count.longValue();
    }
    
    public String getClassFromType(String type) {
        String[] parts = type.split("\\.");
        System.out.println("   "+type+" --> "+parts[parts.length-1]);
        return parts[parts.length-1];
    }
}
