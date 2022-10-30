/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.hibernateimpl;

import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.HostMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * This is the test class for the host manager component.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/inquiry/components/hostmgr/hibernateimpl/HostMgrTest.java#1 $
 */

public class HostMgrTest extends BaseDACComponentTestCase {

    private static final int NB_MIXED_CASE_HOSTS = 6;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        deleteHostRecords();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteHostRecords();
        super.tearDown();
    }

    /**
     * Returns a host manager instance
     * 
     * @return a host manager instance
     */
    protected HostMgrImpl getHostMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IHostMgr.DATASOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo<HostMgrImpl> info = 
            new ComponentInfo<HostMgrImpl>(
                    "hostMgr", 
                    HostMgrImpl.class, 
                    IHostMgr.class, 
                    LifestyleType.TRANSIENT_TYPE, 
                    config);
        return compMgr.getComponent(info);
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public HostMgrTest(String testName) {
        super(testName);
    }

    /**
     * Deletes all the host records from the database.
     */
    protected void deleteHostRecords() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            s.delete("from HostDO");
            t.commit();
        } catch (HibernateException e) {
            fail("Error when deleting host records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of host records
     */
    protected void insertHostRecords(final int nbHosts) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbHosts; i++) {
                HostDO newHost = new HostDO();
                Long id = new Long(i);
                newHost.setId(id);
                newHost.setOriginalId(id);
                newHost.setName("Host_" + i);
                TimeRelation hostTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
                newHost.setTimeRelation(hostTimeRelation);
                s.save(newHost);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Error when inserting host records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of host records
     */
    protected void insertMixedCaseHostRecords() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            String[] nameList = new String[] { "aHost", "ANotherHost", "bHost", "HOsT2", "bNoMatch", "CHosT" };
            int size = nameList.length;
            for (int i = 0; i < size; i++) {
                HostDO newHost = new HostDO();
                Long id = new Long(i);
                newHost.setId(id);
                newHost.setOriginalId(id);
                newHost.setName(nameList[i]);
                TimeRelation hostTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
                newHost.setTimeRelation(hostTimeRelation);
                s.save(newHost);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Error when inserting host records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies the basic features of the host mgr class
     */
    @SuppressWarnings("cast")
    public void testHostMgrClassBasics() {
        HostMgrImpl hostMgr = getHostMgr();
        assertTrue("Host Mgr should implement the right interface", hostMgr instanceof ILogEnabled);
        assertTrue("Host Mgr should implement the right interface", hostMgr instanceof IConfigurable);
        assertTrue("Host Mgr should implement the right interface", hostMgr instanceof IManagerEnabled);
        assertTrue("Host Mgr should implement the right interface", hostMgr instanceof IHostMgr);
        assertTrue("Host Mgr should implement the right interface", hostMgr instanceof IInitializable);
        assertEquals("Host mgr should have a datasource", getActivityDataSource(), hostMgr.getDataSource());
        assertNotNull("Host mgr should have a log", hostMgr.getLog());
        assertNotNull("Host mgr should have a comp manager", hostMgr.getManager());
    }

    /**
     * This test verifies that the instantiation of the host mgr works properly
     */
    public void testHostMgrInstantiation() {

        //Try with bad config
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<HostMgrImpl> info = 
            new ComponentInfo<HostMgrImpl>(
                "badHostMgr", 
                HostMgrImpl.class, 
                IHostMgr.class, 
                LifestyleType.TRANSIENT_TYPE, 
                new HashMapConfiguration());
        boolean exThrown = false;
        try {
            compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Host manager should refuse configurations without a data source", exThrown);

        HostMgrImpl hostMgr = getHostMgr();
        assertNotNull("Host manager should be created", hostMgr);
    }

    /**
     * This test verifies that all records can be fetched properly
     */
    public void testGetAllHosts() {
        final int nbHosts = 10;
        HostMgrImpl hostMgr = getHostMgr();
        deleteHostRecords();
        insertHostRecords(nbHosts);
        try {
            List<IHost> results = hostMgr.getHosts(null);
            assertEquals("All the hosts records should be returned", nbHosts, results.size());
            Iterator<IHost> it = results.iterator();
            while (it.hasNext()) {
                Object result = it.next();
                assertTrue("Results should be of type IHost", result instanceof IHost);
                IHost host = (IHost) result;
                assertNotNull("Each record should have an id", host.getId());
                assertNotNull("Each record should have a name", host.getName());
            }
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that the case insenstive search works properly
     */
    public void testGetHostsWithCaseInsensitiveSearch() {
        deleteHostRecords();
        insertMixedCaseHostRecords();
        HostMgrImpl hostMgr = getHostMgr();
        try {
            //Query everything
            List<IHost> results = hostMgr.getHosts(null);
            assertEquals("All the hosts records should be returned", NB_MIXED_CASE_HOSTS, results.size());

            //Query for letter A -- there should be two matches
            MockSearchSpec searchSpec = new MockSearchSpec();
            MockSearchTerm searchForLetterA = new MockSearchTerm(HostMgrQueryFieldType.NAME, "A*");
            searchSpec.setSearchTerms(new IHostMgrQueryTerm[] { searchForLetterA });
            results = hostMgr.getHosts(searchSpec);
            assertEquals("The query for hosts should be case insensitive", 2, results.size());

            //Query for letter B and *Host* at the same time
            MockSearchTerm searchForLetterB1 = new MockSearchTerm(HostMgrQueryFieldType.NAME, "B*");
            MockSearchTerm searchForHost = new MockSearchTerm(HostMgrQueryFieldType.NAME, "*Host*");
            searchSpec.setSearchTerms(new IHostMgrQueryTerm[] { searchForLetterB1, searchForHost });
            results = hostMgr.getHosts(searchSpec);
            assertEquals("Combining case insensitive criteria should return the correct results", 1, results.size());
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * Dummy query specification class
     * 
     * @author ihanen
     */
    private class MockSearchSpec implements IHostMgrQuerySpec {

        IHostMgrQueryTerm[] searchTerms;

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec#getSearchSpecTerms()
         */
        public IHostMgrQueryTerm[] getSearchSpecTerms() {
            return this.searchTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQuerySpec#getSortSpecTerms()
         */
        public IHostMgrSortTerm[] getSortSpecTerms() {
            return null;
        }

        /**
         * Sets the search terms
         * 
         * @param terms
         *            search terms to set
         */
        public void setSearchTerms(IHostMgrQueryTerm[] terms) {
            this.searchTerms = terms;
        }
    }

    /**
     * Dummy search specification class
     * 
     * @author ihanen
     */
    private class MockSearchTerm implements IHostMgrQueryTerm {

        private HostMgrQueryFieldType field;
        private String expression;

        public MockSearchTerm(HostMgrQueryFieldType field, String expr) {
            this.field = field;
            this.expression = expr;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm#getFieldName()
         */
        public HostMgrQueryFieldType getFieldName() {
            return this.field;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }
    }
}
