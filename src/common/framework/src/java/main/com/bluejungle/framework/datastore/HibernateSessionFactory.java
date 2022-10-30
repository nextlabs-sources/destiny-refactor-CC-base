/*
 * Created on Sep 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore;

import java.io.File;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

/**
 * @author rbelkin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/datastore/HibernateSessionFactory.java#1 $
 */
public final class HibernateSessionFactory {

    private SessionFactory sf;

    /**
     * Constructor
     * @param cfgFile configuration file
     * @param hibProps Hibernate property list
     * @throws HibernateException
     */
    HibernateSessionFactory(File cfgFile, Properties hibProps) throws HibernateException {

        Configuration cfg = new Configuration();
        try {

            cfg.configure(cfgFile);

            cfg.setProperties(hibProps);
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * Returns an Hibernate session
     * @return a Hibernate session 
     * @throws HibernateException
     */
    public Session getSession() throws HibernateException {
        return sf.openSession();
    }
}