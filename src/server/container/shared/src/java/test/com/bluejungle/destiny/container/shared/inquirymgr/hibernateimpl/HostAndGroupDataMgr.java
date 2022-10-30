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
 * This is the data manager class for host and host groups. It creates dummy
 * host data to support the JUnit tests.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/HostAndGroupDataMgr.java#1 $
 */

class HostAndGroupDataMgr extends BaseDataMgr {

    /**
     * Creates an host data object
     * 
     * @param id
     *            id of the host
     * @param name
     *            name of the host
     * @return an application data object populaed with the parameters
     */
    protected static IHost createHost(Long id, String name) {
        HostDO host = new HostDO();
        host.setId(id);
        host.setOriginalId(id);
        host.setName(name);
        TimeRelation hostTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
        host.setTimeRelation(hostTimeRelation);
        return host;
    }

    /**
     * Creates sample data for host and groups.
     * 
     * @param s
     *            Hibernate session to use.
     * @throws HibernateException
     *             if database operation fails.
     */
    public static void createHostsAndGroups(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();

            //Create hosts
            IHost moorea = createHost(new Long(0), "moorea");
            IHost stbarts = createHost(new Long(1), "stbarts");
            IHost savaii = createHost(new Long(2), "savaii");
            IHost borabora = createHost(new Long(3), "borabora");
            IHost cuba = createHost(new Long(4), "cuba");
            IHost perf1 = createHost(new Long(5), "perf1");
            IHost perf2 = createHost(new Long(6), "perf2");

            //Save hosts
            Set hosts = new HashSet();
            hosts.add(moorea);
            hosts.add(stbarts);
            hosts.add(savaii);
            hosts.add(borabora);
            hosts.add(cuba);
            hosts.add(perf1);
            hosts.add(perf2);
            saveOjects(s, hosts);

            //Commit data
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Deletes all hosts and groups records
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
            s.delete("from HostDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
}
