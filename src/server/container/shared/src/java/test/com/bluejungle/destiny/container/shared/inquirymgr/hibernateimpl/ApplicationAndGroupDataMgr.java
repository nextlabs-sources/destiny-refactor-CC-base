/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * This is the data manager class for application and application groups. It
 * creates dummy application data to support the JUnit tests.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ApplicationAndGroupDataMgr.java#1 $
 */

class ApplicationAndGroupDataMgr extends BaseDataMgr {

    /**
     * Creates a application data object
     * 
     * @param id
     *            id of the application
     * @param name
     *            name of the application
     * @return an application data object populaed with the parameters
     */
    protected static IApplication createApplication(Long id, String name) {
        ApplicationDO app = new ApplicationDO();
        app.setId(id);
        app.setOriginalId(id);
        app.setName(name);
        TimeRelation applicationTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
        app.setTimeRelation(applicationTimeRelation);
        return app;
    }

    /**
     * Creates sample data for applications and groups.
     * 
     * @param s
     *            Hibernate session to use.
     * @throws HibernateException
     *             if database operation fails.
     */
    public static void createAppsAndGroups(Session s) throws HibernateException {
        Transaction t = s.beginTransaction();
        try {
            //Create apps
            IApplication word = createApplication(new Long(0), "word");
            IApplication excel = createApplication(new Long(1), "excel");
            IApplication outlook = createApplication(new Long(2), "outlook");
            IApplication eclipse = createApplication(new Long(3), "eclipse");
            IApplication msstudio = createApplication(new Long(4), "visual studio");
            IApplication njstar = createApplication(new Long(5), "njstar");

            //Save apps
            Set apps = new HashSet();
            apps.add(word);
            apps.add(excel);
            apps.add(outlook);
            apps.add(eclipse);
            apps.add(msstudio);
            apps.add(njstar);
            saveOjects(s, apps);

            //Commit data
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all applications and groups records
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if database operation fails
     */
    public static void deleteHostsAndGroups(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from ApplicationDO");
            t.commit();
        } catch (HibernateException e) {
            throw e;
        } finally {
            HibernateUtils.rollbackTransation(t, null);
        }
    }
}