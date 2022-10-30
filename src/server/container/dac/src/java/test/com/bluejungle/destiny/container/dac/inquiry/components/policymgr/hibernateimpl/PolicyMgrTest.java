/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.policymgr.hibernateimpl;

import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.PolicyMgrSortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
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
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the test class for the policy manager component.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/hibernateimpl/PolicyMgrTest.java#3 $
 */

public class PolicyMgrTest extends BaseDACComponentTestCase {

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        deletePolicyRecords();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deletePolicyRecords();
        super.tearDown();
    }

    /**
     * Returns a valid policy manager instance
     * 
     * @return a valid policy manager instance
     */
    protected PolicyMgrImpl getPolicyMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPolicyMgr.DATASOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo<PolicyMgrImpl> info = 
            new ComponentInfo<PolicyMgrImpl>(
                    "policyMgr", 
                    PolicyMgrImpl.class, 
                    IPolicyMgr.class, 
                    LifestyleType.TRANSIENT_TYPE, 
                    config);
        return compMgr.getComponent(info);
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public PolicyMgrTest(String testName) {
        super(testName);
    }

    /**
     * Deletes all the policy records from the database.
     */
    protected void deletePolicyRecords() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            s.delete("from PolicyDO");
            t.commit();
        } catch (HibernateException e) {
            fail("Error when deleting policy records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of policy records
     */
    protected void insertMixedCasePolicyRecords() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            String[] nameList = new String[] { "/folder1/newPolicy", "/folder1/NewPolicy2", "/folder1/readPolicy", "/folder1/WriteDeny" };
            int size = nameList.length;
            for (int i = 0; i < size; i++) {
                PolicyDO newPolicy = new PolicyDO();
                newPolicy.setId(new Long(i));
                newPolicy.setFullName(nameList[i]);
                s.save(newPolicy);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Error when inserting policy records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of policy records
     */
    protected void insertPolicyRecords(final int nbPolicies) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbPolicies; i++) {
                PolicyDO newPolicy = new PolicyDO();
                newPolicy.setId(new Long(i));
                newPolicy.setFullName("/folder1/Policy_" + i);
                s.save(newPolicy);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Error when inserting policy records " + e.getLocalizedMessage());
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies the basic features of the policy mgr class
     */
    @SuppressWarnings("cast")
    public void testPolicyMgrClassBasics() {
        PolicyMgrImpl policyMgr = getPolicyMgr();
        assertTrue("Policy Mgr should implement the right interface", policyMgr instanceof ILogEnabled);
        assertTrue("Policy Mgr should implement the right interface", policyMgr instanceof IConfigurable);
        assertTrue("Policy Mgr should implement the right interface", policyMgr instanceof IManagerEnabled);
        assertTrue("Policy Mgr should implement the right interface", policyMgr instanceof IPolicyMgr);
        assertTrue("Policy Mgr should implement the right interface", policyMgr instanceof IInitializable);
        assertEquals("Policy Mgr should have a datasource", getActivityDataSource(), policyMgr.getDataSource());
        assertNotNull("Policy Mgr should have a log", policyMgr.getLog());
        assertNotNull("Policy Mgr should have a comp manager", policyMgr.getManager());
    }

    /**
     * This test verifies that the instantiation of the policy manager works
     * properly
     */
    public void testPolicyMgrInstantiation() {

        //Try with bad config
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<PolicyMgrImpl> info = 
            new ComponentInfo<PolicyMgrImpl>(
                "badPolicyMgr", 
                PolicyMgrImpl.class, 
                IPolicyMgr.class, 
                LifestyleType.TRANSIENT_TYPE, 
                new HashMapConfiguration());
        boolean exThrown = false;
        try {
            compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Policy manager should refuse configurations without a data source", exThrown);

        PolicyMgrImpl policyMgr = getPolicyMgr();
        assertNotNull("Policy manager should be created", policyMgr);
    }

    /**
     * This test verifies that all records can be fetched properly
     */
    public void testGetAllPolicies() {
        final int nbPolicies = 10;
        insertPolicyRecords(nbPolicies);
        PolicyMgrImpl policyMgr = getPolicyMgr();
        try {
            List<IPolicy> results = policyMgr.getPolicies(null);
            assertEquals("All the policy records should be returned", nbPolicies, results.size());
            Iterator<IPolicy> it = results.iterator();
            while (it.hasNext()) {
                Object result = it.next();
                assertTrue("Results should be of type IPolicy", result instanceof IPolicy);
                IPolicy policy = (IPolicy) result;
                assertNotNull("Each record should have an id", policy.getId());
                assertNotNull("Each record should have a name", policy.getName());
                assertNotNull("Each record should have a folder name", policy.getFolderName());
            }
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that the query spec works fine
     */
    public void testGetPoliciesWithCaseInsensiveSearch() {
        insertMixedCasePolicyRecords();
        PolicyMgrImpl policyMgr = getPolicyMgr();
        try {
            //Try the no match query
            String policyMatch = "Policy*";
            MockPolicyMgrQuerySpec searchSpec = new MockPolicyMgrQuerySpec();
            MockPolicyMgrQueryTerm searchTerm = new MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType.NAME, policyMatch);
            searchSpec.setSearchSpecTerms(new IPolicyMgrQueryTerm[] { searchTerm });
            List<IPolicy> results = policyMgr.getPolicies(searchSpec);
            assertEquals("Only the matching policy records should be returned", 0, results.size());

            //Try starting with
            policyMatch = "new*";
            searchTerm = new MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType.NAME, policyMatch);
            searchSpec.setSearchSpecTerms(new IPolicyMgrQueryTerm[] { searchTerm });
            results = policyMgr.getPolicies(searchSpec);
            assertEquals("Only the matching policy records should be returned", 2, results.size());
            Iterator<IPolicy> it = results.iterator();
            Object result = it.next();
            assertTrue("Results should be of type IPolicy", result instanceof IPolicy);
            IPolicy policy = (IPolicy) result;
            assertNotNull("The matching record should have an id", policy.getId());
            assertTrue("The matching record should have a matching name", policy.getName().toLowerCase().startsWith("new"));

            //Try combined search
            policyMatch = "*licy";
            searchTerm = new MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType.NAME, policyMatch);
            policyMatch = "R*";
            MockPolicyMgrQueryTerm searchLikeR = new MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType.NAME, policyMatch);
            searchSpec.setSearchSpecTerms(new IPolicyMgrQueryTerm[] { searchTerm, searchLikeR });
            results = policyMgr.getPolicies(searchSpec);
            assertEquals("Only the matching policy records should be returned", 1, results.size());
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that the query spec works fine
     */
    public void testGetPoliciesWithSearchSpec() {
        final int nbPolicies = 10;
        insertPolicyRecords(nbPolicies);
        PolicyMgrImpl policyMgr = getPolicyMgr();
        try {
            final String policyMatch = "Policy_3*";
            MockPolicyMgrQuerySpec searchSpec = new MockPolicyMgrQuerySpec();
            MockPolicyMgrQueryTerm searchTerm = new MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType.NAME, policyMatch);
            searchSpec.setSearchSpecTerms(new IPolicyMgrQueryTerm[] { searchTerm });
            List<IPolicy> results = policyMgr.getPolicies(searchSpec);
            assertEquals("Only the matching policy records should be returned", 1, results.size());
            Iterator<IPolicy> it = results.iterator();
            Object result = it.next();
            assertTrue("Results should be of type IPolicy", result instanceof IPolicy);
            IPolicy policy = (IPolicy) result;
            assertNotNull("The matching record should have an id", policy.getId());
            assertTrue("The matching record should have a matching name", policyMatch.startsWith(policy.getName()));
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that the query spec works fine
     */
    public void testGetPoliciesWithSortSpec() {
        final int nbPolicies = 10;
        insertPolicyRecords(nbPolicies);
        PolicyMgrImpl policyMgr = getPolicyMgr();
        try {
            MockPolicyMgrSortTerm sortTerm = new MockPolicyMgrSortTerm(PolicyMgrSortFieldType.NAME, SortDirectionType.DESCENDING);
            MockPolicyMgrQuerySpec querySpec = new MockPolicyMgrQuerySpec();
            querySpec.setSortSpecTerms(new IPolicyMgrSortTerm[] { sortTerm });
            List<IPolicy> results = policyMgr.getPolicies(querySpec);
            assertEquals("Only matching policy records should be returned", nbPolicies, results.size());
            Iterator<IPolicy> it = results.iterator();
            Long lastId = null;
            while (it.hasNext()) {
                Object result = it.next();
                assertTrue("Results should be of type IPolicy", result instanceof IPolicy);
                IPolicy policy = (IPolicy) result;
                if (lastId == null) {
                    lastId = policy.getId();
                }
                assertNotNull("The matching record should have an id", policy.getId());
                assertTrue("Records should be order by decrementing id", policy.getId().longValue() <= lastId.longValue());
                lastId = policy.getId();
            }
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * Dummy policy query spec class
     * 
     * @author ihanen
     */
    protected class MockPolicyMgrQuerySpec implements IPolicyMgrQuerySpec {

        private IPolicyMgrQueryTerm[] searchTerms;
        private IPolicyMgrSortTerm[] sortTerms;

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec#getSearchSpecTerms()
         */
        public IPolicyMgrQueryTerm[] getSearchSpecTerms() {
            return this.searchTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQuerySpec#getSortSpecTerms()
         */
        public IPolicyMgrSortTerm[] getSortSpecTerms() {
            return this.sortTerms;
        }

        /**
         * Sets the search terms
         * 
         * @param newTerms
         *            terms to set
         */
        public void setSearchSpecTerms(IPolicyMgrQueryTerm[] newTerms) {
            this.searchTerms = newTerms;
        }

        /**
         * Set the sort terms
         * 
         * @param newTerms
         *            terms to set
         */
        public void setSortSpecTerms(IPolicyMgrSortTerm[] newTerms) {
            this.sortTerms = newTerms;
        }
    }

    protected class MockPolicyMgrQueryTerm implements IPolicyMgrQueryTerm {

        private PolicyMgrQueryFieldType fieldName;
        private String expression;

        /**
         * Constructor
         * 
         * @param field
         *            field to query on
         * @param expr
         *            expression
         */
        public MockPolicyMgrQueryTerm(PolicyMgrQueryFieldType field, String expr) {
            this.fieldName = field;
            this.expression = expr;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm#getFieldName()
         */
        public PolicyMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }
    }

    protected class MockPolicyMgrSortTerm implements IPolicyMgrSortTerm {

        private PolicyMgrSortFieldType fieldName;
        private SortDirectionType direction;

        /**
         * Constructor
         * 
         * @param field
         *            field to query on
         * @param expr
         *            expression
         */
        public MockPolicyMgrSortTerm(PolicyMgrSortFieldType field, SortDirectionType dir) {
            this.fieldName = field;
            this.direction = dir;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm#getFieldName()
         */
        public PolicyMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }
    }
}
