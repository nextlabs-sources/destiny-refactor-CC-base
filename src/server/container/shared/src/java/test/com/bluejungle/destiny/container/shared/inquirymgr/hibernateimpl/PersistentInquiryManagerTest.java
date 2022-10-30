/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryObligation;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the test class for the persistent inquiry manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentInquiryManagerTest.java#1 $
 */

public class PersistentInquiryManagerTest extends DACContainerSharedTestCase {

    private static final Log LOG = LogFactory.getLog(PersistentInquiryManagerTest.class.getName());

    /**
     * Constructor
     */
    public PersistentInquiryManagerTest() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }
    
    /**
     * Constructor
     * 
     * @param testName
     */
    public PersistentInquiryManagerTest(String testName) {
        super(testName);
    }

    /**
     * Returns an instance of the persistent report manager
     * 
     * @return an instance of the persistent report manager
     */
    private IPersistentInquiryMgr getPersistentInquiryMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPersistentInquiryMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("persistentActivityMgr", PersistentInquiryMgrImpl.class.getName(), IPersistentInquiryMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IPersistentInquiryMgr inquiryMgr = (IPersistentInquiryMgr) compMgr.getComponent(compInfo);
        return inquiryMgr;
    }

    /**
     * This test verifies the class instantiation
     */
    public void testPersistentInquiryMgrInstantiation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        HashMapConfiguration badConfig = new HashMapConfiguration();
        config.setProperty(IPersistentInquiryMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("persistentActivityMgr", PersistentInquiryMgrImpl.class.getName(), IPersistentInquiryMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        ComponentInfo badCompInfo = new ComponentInfo("persistentActivityMgr_bad", PersistentInquiryMgrImpl.class.getName(), IPersistentInquiryMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, badConfig);

        IPersistentInquiryMgr inquiryMgr = (IPersistentInquiryMgr) compMgr.getComponent(compInfo);
        assertNotNull("Persistent inquiry manager should be created properly with a valid configuration", inquiryMgr);

        boolean exThrown = false;
        try {
            IPersistentInquiryMgr badInquiryMgr = (IPersistentInquiryMgr) compMgr.getComponent(badCompInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Persistent inquiry manager should not accept bad configuration", exThrown);
    }

    /**
     * This test verifies that the new persistent reports are properly created
     */
    public void testInquiryCreation() {
        IPersistentInquiryMgr inquiryMgr = getPersistentInquiryMgr();
        IPersistentInquiry inquiry = inquiryMgr.createPersistentInquiry();
        assertNull("New persistent inquiries should not have id assigned list", inquiry.getId());
        assertNotNull("New persistent inquiries should have empty actions list", inquiry.getActions());
        assertEquals("New persistent inquiries should have empty actions list", 0, inquiry.getActions().size());
        assertNotNull("New persistent inquiries should have empty applications list", inquiry.getApplications());
        assertEquals("New persistent inquiries should have empty applications list", 0, inquiry.getApplications().size());
        assertNotNull("New persistent inquiries should have empty obligations list", inquiry.getObligations());
        assertEquals("New persistent inquiries should have empty obligations list", 0, inquiry.getObligations().size());
        assertNotNull("New persistent inquiries should have empty policies list", inquiry.getPolicies());
        assertEquals("New persistent inquiries should have empty policies list", 0, inquiry.getPolicies().size());
        assertNotNull("New persistent inquiries should have empty policy decision list", inquiry.getPolicyDecisions());
        assertEquals("New persistent inquiries should have empty policy decision list", 0, inquiry.getPolicyDecisions().size());
        assertNotNull("New persistent inquiries should have empty resources list", inquiry.getResources());
        assertEquals("New persistent inquiries should have empty resources list", 0, inquiry.getResources().size());
        assertNotNull("New persistent inquiries should have empty users list", inquiry.getUsers());
        assertEquals("New persistent inquiries should have empty users list", 0, inquiry.getUsers().size());
        assertNull("New persistent inquiries should not have target data", inquiry.getTargetData());
        assertEquals("New persistent inquiries should have a logging level 0", 0, inquiry.getLoggingLevel());
    }

    /**
     * This test verifies the cascase deletion for inquiry
     */
    public void testInquiryDeletion() {
        IPersistentInquiryMgr inquiryMgr = getPersistentInquiryMgr();
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            IPersistentInquiry inquiry = inquiryMgr.createPersistentInquiry();
            inquiry.addAction(ActionEnumType.ACTION_OPEN);
            inquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
            final String obligationExpr = "myObligation";
            inquiry.addObligation(obligationExpr);
            final String policyExpr = "myPolicy";
            inquiry.addPolicy(policyExpr);
            final String resourceExpr = "myResource";
            inquiry.addResource(resourceExpr);
            final String userExpr = "myUSer";
            inquiry.addUser(userExpr);
            inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
            t = s.beginTransaction();
            s.save(inquiry);
            Long id = inquiry.getId();
            t.commit();
            s.clear();

            //Fetch the ids of each child collection
            IPersistentInquiry fetchedInquiry = (IPersistentInquiry) s.get(InquiryDO.class, id);
            InquiryActionDO action = (InquiryActionDO) inquiry.getActions().iterator().next();
            Long actionId = action.getId();
            assertNotNull("Id field should not be null", actionId);
            InquiryObligationDO obligation = (InquiryObligationDO) inquiry.getObligations().iterator().next();
            Long obligationId = obligation.getId();
            assertNotNull("Id field should not be null", obligationId);
            InquiryPolicyDO policy = (InquiryPolicyDO) inquiry.getPolicies().iterator().next();
            Long policyId = policy.getId();
            InquiryPolicyDecisionDO policyDecision = (InquiryPolicyDecisionDO) inquiry.getPolicyDecisions().iterator().next();
            Long policyDecisionId = policyDecision.getId();
            assertNotNull("Id field should not be null", policyDecisionId);
            InquiryResourceDO resource = (InquiryResourceDO) inquiry.getResources().iterator().next();
            Long resourceId = resource.getId();
            assertNotNull("Id field should not be null", resourceId);
            InquiryUserDO user = (InquiryUserDO) inquiry.getUsers().iterator().next();
            Long userId = user.getId();
            assertNotNull("Id field should not be null", userId);
            //TODO: Robert: May need to take into account the logging level

            //Now, delete the inquiry and checks the cascade delete worked
            t = s.beginTransaction();
            s.delete(fetchedInquiry);
            t.commit();
            IInquiryAction fetchedAction = (IInquiryAction) s.get(InquiryActionDO.class, actionId);
            assertNull("inquiry actions should be deleted", fetchedAction);
            IInquiryObligation fetchedObligation = (IInquiryObligation) s.get(InquiryObligationDO.class, obligationId);
            assertNull("inquiry obligations should be deleted", fetchedObligation);
            IInquiryPolicy fetchedPolicy = (IInquiryPolicy) s.get(InquiryPolicyDO.class, policyId);
            assertNull("inquiry policies should be deleted", fetchedPolicy);
            IInquiryPolicyDecision fetchedPolicyDecision = (IInquiryPolicyDecision) s.get(InquiryPolicyDecisionDO.class, policyDecisionId);
            assertNull("inquiry policy decisions should be deleted", fetchedPolicyDecision);
            IInquiryResource fetchedResource = (IInquiryResource) s.get(InquiryResourceDO.class, resourceId);
            assertNull("inquiry resources should be deleted", fetchedResource);
            IInquiryUser fetchedUser = (IInquiryUser) s.get(InquiryUserDO.class, userId);
            assertNull("inquiry users should be deleted", fetchedUser);
        } catch (HibernateException e) {
            fail("Hibernate exception occured:" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.rollbackTransation(t, LOG);
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * This test verifies that an inquiry is properly persisted in the database
     * and can be loaded back.
     */
    public void testInquiryPersistance() {

        final int nbActions = 5;
        final int nbUsers = 10;
        final int nbPolicies = 4;
        final int nbObligations = 2;
        final int nbResources = 8;

        IPersistentInquiryMgr inquiryMgr = getPersistentInquiryMgr();
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getCurrentSession();
            IPersistentInquiry inquiry = inquiryMgr.createPersistentInquiry();

            //Sets the target data and logging level
            inquiry.setTargetData(InquiryTargetDataType.POLICY);
            inquiry.setLoggingLevel(2);

            //Add actions
            Set actionNames = new HashSet();
            actionNames.add(ActionEnumType.ACTION_COPY);
            actionNames.add(ActionEnumType.ACTION_OPEN);
            actionNames.add(ActionEnumType.ACTION_MOVE);
            inquiry.addAction(ActionEnumType.ACTION_COPY);
            inquiry.addAction(ActionEnumType.ACTION_OPEN);
            inquiry.addAction(ActionEnumType.ACTION_MOVE);
            final Set policyDecisionNames = new HashSet();
            policyDecisionNames.add(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
            policyDecisionNames.add(PolicyDecisionEnumType.POLICY_DECISION_DENY);
            inquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
            inquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);

            //Add Users
            Set userNames = new HashSet();
            for (int i = 0; i < nbUsers; i++) {
                final String userName = "user" + i;
                inquiry.addUser(userName);
                userNames.add(userName);
            }
            //Add policies
            Set policyNames = new HashSet();
            for (int i = 0; i < nbPolicies; i++) {
                final String policyName = "policy" + i;
                inquiry.addPolicy(policyName);
                policyNames.add(policyName);
            }

            //Add obligations
            Set obligationNames = new HashSet();
            for (int i = 0; i < nbObligations; i++) {
                final String obligationName = "obligation" + i;
                inquiry.addObligation(obligationName);
                obligationNames.add(obligationName);
            }

            //Add resources
            Set resourceNames = new HashSet();
            for (int i = 0; i < nbResources; i++) {
                final String resourceName = "resource" + i;
                inquiry.addResource(resourceName);
                resourceNames.add(resourceName);
            }

            t = s.beginTransaction();
            s.save(inquiry);
            t.commit();
            Long inquiryId = inquiry.getId();
            assertNotNull("Saving the inquiry should assign an Id", inquiryId);
            s.clear();
            getActivityDateSource().closeCurrentSession();

            //Now, retrieve the inquiry again and make sure all parameters are
            // readable.
            s = getActivityDateSource().getCurrentSession();
            inquiry = inquiryMgr.getInquiry(inquiryId);
            assertNotNull("Query by Id should return a valid record", inquiry);
            assertEquals("Query by Id should return a valid record", inquiryId, inquiry.getId());

            //Checks the target data and logging level
            assertEquals("Fetched target data type should match", InquiryTargetDataType.POLICY, inquiry.getTargetData());
            assertEquals("The logging level should match", 2, inquiry.getLoggingLevel());

            //Checks the actions collection
            Collection actions = inquiry.getActions();
            assertEquals("The number of fetched actions should match", actionNames.size(), actions.size());
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                IInquiryAction action = (IInquiryAction) it.next();
                assertTrue("All actions should be fetched properly", actionNames.contains(action.getActionType()));
            }

            //Checks the users collection
            Collection users = inquiry.getUsers();
            assertEquals("The number of fetched users should match", userNames.size(), users.size());
            it = users.iterator();
            while (it.hasNext()) {
                IInquiryUser user = (IInquiryUser) it.next();
                assertTrue("All actions should be fetched properly", userNames.contains(user.getDisplayName()));
            }

            //Checks the policy collection
            Collection policies = inquiry.getPolicies();
            assertEquals("The number of fetched policies should match", policyNames.size(), policies.size());
            it = policies.iterator();
            while (it.hasNext()) {
                IInquiryPolicy policy = (IInquiryPolicy) it.next();
                assertTrue("All policies should be fetched properly", policyNames.contains(policy.getName()));
            }
            
            //Checks the policy decision collection
            Collection policyDecisions = inquiry.getPolicyDecisions();
            assertEquals("The number of fetched policy decisions should match", policyDecisionNames.size(), policyDecisions.size());
            it = policyDecisions.iterator();
            while (it.hasNext()) {
                IInquiryPolicyDecision currentPolicyDecision = (IInquiryPolicyDecision) it.next();
                assertTrue("All policy decisions should be fetched properly", policyDecisionNames.contains(currentPolicyDecision.getPolicyDecisionType()));
            }


            //Checks the obligation collection
            Collection obligations = inquiry.getObligations();
            assertEquals("The number of fetched obligations should match", obligationNames.size(), obligations.size());
            it = obligations.iterator();
            while (it.hasNext()) {
                IInquiryObligation obligation = (IInquiryObligation) it.next();
                assertTrue("All obligations should be fetched properly", obligationNames.contains(obligation.getName()));
            }

            //Checks the resource collection
            Collection resources = inquiry.getResources();
            assertEquals("The number of fetched resources should match", resourceNames.size(), resources.size());
            it = resources.iterator();
            while (it.hasNext()) {
                IInquiryResource resource = (IInquiryResource) it.next();
                assertTrue("All resources should be fetched properly", resourceNames.contains(resource.getName()));
            }
        } catch (HibernateException e) {
            fail("Inquiry manipulation should not throw exceptions");
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown when persisting the inquiry: " + e.getMessage());
        } finally {
            HibernateUtils.rollbackTransation(t, LOG);
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * This test verifies the creation of the persistent inquiry is robust
     * enough to handle null values
     */
    //TODO: Robert: did not account for the logging level
    public void testNonPersistentInquiryNullSetters() {
        IPersistentInquiryMgr inquiryMgr = getPersistentInquiryMgr();
        IInquiry newInquiry = inquiryMgr.createPersistentInquiry();
        boolean exThrown = false;
        try {
            newInquiry.addAction(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addAction should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addApplication(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addApplication should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addObligation(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addObligation should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addPolicy(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addPolicy should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addPolicyDecision(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addPolicyDecision should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addResource(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addResource should not accept NULL value", exThrown);
        exThrown = false;
        try {
            newInquiry.addUser(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("addAcaddUsertion should not accept NULL value", exThrown);

        //Checks that setter and getters are fine
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getActions().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getApplications().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getObligations().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getPolicies().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getPolicyDecisions().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getResources().size());
        assertEquals("Setter should handle NULL value properly", 0, newInquiry.getUsers().size());
    }

    /**
     * This test verifies the creation of the persistent inquiry is robust
     * enough to handle empty values
     */
    public void testPersistentInquiryEmptySetters() {
        IPersistentInquiryMgr inquiryMgr = getPersistentInquiryMgr();
        IInquiry newInquiry = inquiryMgr.createPersistentInquiry();
        newInquiry.addApplication("");
        newInquiry.addObligation("");
        newInquiry.addPolicy("");
        newInquiry.addResource("");
        newInquiry.addUser("");
        //TODO: Robert: may need to check the logging level
        //Checks that setter and getters are fine
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getActions().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getApplications().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getObligations().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getPolicies().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getResources().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getUsers().size());
    }
}
