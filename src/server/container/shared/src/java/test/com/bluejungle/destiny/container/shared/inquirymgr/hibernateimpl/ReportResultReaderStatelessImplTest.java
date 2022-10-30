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
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderStatelessImplTest.java#1 $
 */

public class ReportResultReaderStatelessImplTest extends DACContainerSharedTestCase {

    /**
     * This test verifies the basic properties of the class
     */
    public void testReportResultReaderStatelessImplClassBasics() {
        IHibernateRepository ds = getActivityDateSource();
        MockQuery q = new MockQuery();
        MockScrollableResults scroll = new MockScrollableResults();
        q.setScrollableResults(scroll);
        MockStoredQuery storedQuery = new MockStoredQuery();
        Session s = null;
        ReportResultStateImpl state = new ReportResultStateImpl();
        state.setQueryId(new Long(0));
        try {
            s = ds.getSession();
            ReportResultReaderStatelessImpl reader = new ReportResultReaderStatelessImpl(s, q, getActivityDateSource(), storedQuery, state);
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
        ReportResultStateImpl state = new ReportResultStateImpl();
        state.setQueryId(new Long(0));
        try {
            s = ds.getSession();
            ReportResultReaderStatelessImpl reader = new ReportResultReaderStatelessImpl(s, q, getActivityDateSource(), storedQuery, state);
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

    /**
     * This test verifies that the reader creates a new valid state after
     * fetching records.
     */
    public void testReportResultReaderStatefulTestStateConstruction() {
        IHibernateRepository ds = getActivityDateSource();
        MockQuery q = new MockQuery();
        MockScrollableResults scroll = new MockScrollableResults();
        q.setScrollableResults(scroll);
        MockStoredQuery storedQuery = new MockStoredQuery();
        Session s = null;
        ReportResultStateImpl state = new ReportResultStateImpl();
        state.setQueryId(new Long(0));
        try {
            s = ds.getSession();
            ReportResultReaderStatelessImpl reader = new ReportResultReaderStatelessImpl(s, q, getActivityDateSource(), storedQuery, state);
            scroll.setNext(true);
            scroll.setFirst(false);
            scroll.setLast(false);
            IReportResultState newState = reader.buildCurrentState(scroll, state);
            assertNull("New state should not contain row id if scrolling failed", newState.getFirstRowSequenceId());
            assertNull("New state should not contain row id if scrolling failed", newState.getLastRowSequenceId());

            scroll.setFirst(true);
            scroll.setLast(true);
            ReportSummaryResultDO rd = new ReportSummaryResultDO();
            Object[] next = { rd };
            scroll.setNextGet(next);
            Long expectedId = new Long(25);
            rd.setId(expectedId);
            newState = reader.buildCurrentState(scroll, state);
            assertEquals("New state should contain row id if scrolling succeeds", expectedId, newState.getFirstRowSequenceId());
            assertEquals("New state should contain row id if scrolling succeeds", expectedId, newState.getLastRowSequenceId());
            int i = 0;
        } catch (HibernateException e) {
            fail("No Hibernate exception should be thrown");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    private class DummyResultData implements IResultData {

        private Long id;

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IResultData#getId()
         */
        public Long getId() {
            return this.id;
        }

        /**
         * Sets the dummy data id
         * 
         * @param newId
         */
        public void setId(Long newId) {
            this.id = newId;
        }

    }
}