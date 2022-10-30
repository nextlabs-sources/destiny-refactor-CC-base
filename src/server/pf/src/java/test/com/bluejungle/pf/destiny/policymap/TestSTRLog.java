/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.destiny.policymap;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/policymap/TestSTRLog.java#2 $:
 */

public class TestSTRLog extends PFTestWithDataSource {

    /**
     * Constructor for TestSTRLog.
     * 
     * @param arg0
     */
    public TestSTRLog(String arg0) {
        super(arg0);
    }

    public void testVersioning() throws HibernateException {
        Session session = null;
        Transaction tx = null;

        final SessionFactory sf = (SessionFactory) cm.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        session = sf.openSession();
        tx = session.beginTransaction();
        session.delete("from STRLog");

        long dateCounter = 0;
        STRLog log = new STRLog(new String[] { "bluejungle.com", "test.bluejungle.com" }, new Date(dateCounter++));
        session.save(log);
        tx.commit();
        session.close();

        session = sf.openSession();
        List logs = session.find("from STRLog");
        assertNotNull("list of logs should not be null", logs);
        assertEquals("list should have only 1 element", 1, logs.size());

        log = (STRLog) logs.get(0);
        assertNotNull("log should not be null", log);
        assertEquals("version should be 0", 0, log.getVersion());

        tx = session.beginTransaction();
        log.setBuildTime(new Date(dateCounter++));
        session.saveOrUpdate(log);
        tx.commit();
        session.close();

        session = sf.openSession();
        logs = session.find("from STRLog");
        assertNotNull("list of logs should not be null", logs);
        assertEquals("list should have only 1 element", 1, logs.size());

        log = (STRLog) logs.get(0);
        assertNotNull("log should not be null", log);
        assertEquals("version should be 1", 1, log.getVersion());
        session.close();

        session = sf.openSession();
        logs = session.find("from STRLog");
        STRLog newLog = (STRLog) logs.get(0);
        newLog.setBuildTime(new Date(dateCounter++));
        tx = session.beginTransaction();
        session.saveOrUpdate(newLog);
        tx.commit();
        session.close();

        log.setBuildTime(new Date(dateCounter++));
        session = sf.openSession();
        tx = session.beginTransaction();
        boolean updateFailed = false;
        session.saveOrUpdate(log);
        try {
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            updateFailed = true;
        }
        session.close();

        assertTrue("Stale update should've failed", updateFailed);
    }
}
