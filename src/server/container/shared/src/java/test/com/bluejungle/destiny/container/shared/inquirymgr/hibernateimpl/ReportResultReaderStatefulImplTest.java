/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderStatefulImplTest.java#1 $
 */

public class ReportResultReaderStatefulImplTest extends DACContainerSharedTestCase {

    /**
     * This test verifies the basic properties of the class
     */
    public void testReportResultReaderStatefulImplClassBasics() {
        IHibernateRepository ds = getActivityDateSource();
        MockQuery q = new MockQuery();
        MockScrollableResults scroll = new MockScrollableResults();
        q.setScrollableResults(scroll);
        MockStoredQuery storedQuery = new MockStoredQuery();
        storedQuery.setId (new Long (0));
        Session s = null;
        try {
            s = ds.getSession();
            ReportResultReaderStatefulImpl reader = new ReportResultReaderStatefulImpl(s, q, getActivityDateSource(), storedQuery);
            assertTrue("Class should implement the right interface", reader instanceof IReportResultReader);
            assertTrue("Class should extends the right base class", reader instanceof BaseReportResultReader);
            assertEquals("Member variables should be set properly", getActivityDateSource(), reader.getDataSource());
            assertEquals("Member variables should be set properly", scroll, reader.getScrollResults());
            assertEquals("Member variables should be set properly", s, reader.getSession());
            assertEquals("Member variables should be set properly", storedQuery, reader.getStoredQuery());
            assertTrue("Reader should go on the first record", scroll.isBeforeFirst());
            assertNotNull("Reader should have a log set", reader.getLog());
        } catch (HibernateException e) {
            fail("No Hibernate exception should be thrown");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the reader navigates properly between records.
     */
    public void testReportResultReaderStatefulNavigation() {
        IHibernateRepository ds = getActivityDateSource();
        MockQuery q = new MockQuery();
        MockScrollableResults scroll = new MockScrollableResults();
        q.setScrollableResults(scroll);
        MockStoredQuery storedQuery = new MockStoredQuery();
        Session s = null;
        try {
            s = ds.getSession();
            ReportResultReaderStatefulImpl reader = new ReportResultReaderStatefulImpl(s, q, getActivityDateSource(), storedQuery);
            assertNull("If nextRecord fails, returned result should be null", reader.nextResult());
            assertFalse("If nextRecord fails, hasNextResult should be false", reader.hasNextResult());
            scroll.setNext(true);
            DummyResultData data = new DummyResultData();
            Object[] result = { data };
            scroll.setNextGet(result);
            assertEquals("If nextRecord succeeds, result should be returned", data, reader.nextResult());
            assertTrue("If nextRecord fails, hasNextResult should be true", reader.hasNextResult());
        } catch (HibernateException e) {
            fail("No Hibernate exception should be thrown");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    private class DummyResultData implements IResultData {

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IResultData#getId()
         */
        public Long getId() {
            return null;
        }

    }
}