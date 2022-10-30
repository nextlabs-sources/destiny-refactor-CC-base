/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryApplication;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryObligation;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the inquiry manager test class. It tests the in memory inquiry
 * implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryManagerTest.java#5 $
 */

public class InquiryManagerTest extends BaseDestinyTestCase {

    /**
     * Constructor
     */
    public InquiryManagerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public InquiryManagerTest(String testName) {
        super(testName);
    }

    /**
     * Returns an instance of the inquiry manager
     * 
     * @return an instance of the inquiry manager
     */
    private IInquiryMgr getInquiryMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo compInfo = new ComponentInfo("inquiryMgr", InquiryMgrImpl.class.getName(), IInquiryMgr.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IInquiryMgr inquiryMgr = (IInquiryMgr) compMgr.getComponent(compInfo);
        return inquiryMgr;
    }

    /**
     * This test verifies the creation of memory inquiry
     */
    public void testNonPersistentInquiryCreation() {
        IInquiryMgr inquiryMgr = getInquiryMgr();
        assertNotNull("Inquiry manager creation should work", inquiryMgr);
        IInquiry newInquiry = inquiryMgr.createInquiry();
        assertNotNull("Report manager should create a report", newInquiry);
        assertEquals("New inquiry should have an empty action set", 0, newInquiry.getActions().size());
        assertEquals("New inquiry should have an empty application set", 0, newInquiry.getApplications().size());
        assertEquals("New inquiry should have an empty obligation set", 0, newInquiry.getObligations().size());
        assertEquals("New inquiry should have an empty policy set", 0, newInquiry.getPolicies().size());
        assertEquals("New inquiry should have an empty policy decision set", 0, newInquiry.getPolicyDecisions().size());
        assertEquals("New inquiry should have an empty resource set", 0, newInquiry.getResources().size());
        assertEquals("New inquiry should have an empty user set", 0, newInquiry.getUsers().size());
        assertEquals("New inquiry should have logging level 0", 0, newInquiry.getLoggingLevel());
        assertNull("New inquiry do not have target data specified", newInquiry.getTargetData());
    }

    /**
     * This test verifies the creation of memory inquiry works properly
     */
    public void testNonPersistentInquirySettersGetters() {
        IInquiryMgr inquiryMgr = getInquiryMgr();
        assertNotNull("Inquiry manager creation should work", inquiryMgr);
        IInquiry newInquiry = inquiryMgr.createInquiry();
        final ActionEnumType myActionType = ActionEnumType.ACTION_OPEN;
        newInquiry.addAction(myActionType);
        final PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        newInquiry.addPolicyDecision(myPolicyDecision);
        final String app = "myApp";
        newInquiry.addApplication(app);
        final String oblig = "myObligation";
        newInquiry.addObligation(oblig);
        final String policy = "myPolicy";
        newInquiry.addPolicy(policy);
        final String resource = "myResource";
        newInquiry.addResource(resource);
        final String user = "myUser";
        newInquiry.addUser(user);
        final InquiryTargetDataType targetData = InquiryTargetDataType.ACTIVITY;
        newInquiry.setTargetData(targetData);
        final int loggingLevel = 2;
        newInquiry.setLoggingLevel(loggingLevel);
        
        //Checks that setter and getters are fine
        assertEquals("Setter and getters should work properly", 1, newInquiry.getActions().size());
        IInquiryAction inquiryAction = (IInquiryAction) newInquiry.getActions().iterator().next();
        assertEquals("Setter and getters should work properly", myActionType, inquiryAction.getActionType());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getApplications().size());
        IInquiryApplication inquiryApp = (IInquiryApplication) newInquiry.getApplications().iterator().next();
        assertEquals("Setter and getters should work properly", app, inquiryApp.getName());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getObligations().size());
        IInquiryObligation inquiryOb = (IInquiryObligation) newInquiry.getObligations().iterator().next();
        assertEquals("Setter and getters should work properly", oblig, inquiryOb.getName());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getPolicies().size());
        IInquiryPolicy inquiryPolicy = (IInquiryPolicy) newInquiry.getPolicies().iterator().next();
        assertEquals("Setter and getters should work properly", policy, inquiryPolicy.getName());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getPolicyDecisions().size());
        IInquiryPolicyDecision inquiryPolicyDecision = (IInquiryPolicyDecision) newInquiry.getPolicyDecisions().iterator().next();
        assertEquals("Setter and getters should work properly", myPolicyDecision, inquiryPolicyDecision.getPolicyDecisionType());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getResources().size());
        IInquiryResource inquiryResource = (IInquiryResource) newInquiry.getResources().iterator().next();
        assertEquals("Setter and getters should work properly", resource, inquiryResource.getName());
        assertEquals("Setter and getters should work properly", 1, newInquiry.getUsers().size());
        IInquiryUser inquiryUser = (IInquiryUser) newInquiry.getUsers().iterator().next();
        assertEquals("Setter and getters should work properly", user, inquiryUser.getDisplayName());
        int inquiryLoggingLevel = newInquiry.getLoggingLevel();
        assertEquals("Setter and getters should work properly", loggingLevel, inquiryLoggingLevel);
    }

    /**
     * This test verifies the creation of memory inquiry is robust enough to
     * handle null values
     */
    public void testNonPersistentInquiryNullSetters() {
        IInquiryMgr inquiryMgr = getInquiryMgr();
        IInquiry newInquiry = inquiryMgr.createInquiry();
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
        assertTrue("addUser should not accept NULL value", exThrown);
        //TODO: Robert: need to check the setter for logging level

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
     * This test verifies the creation of memory inquiry is robust enough to
     * handle empty values
     */
    public void testNonPersistentInquiryEmptySetters() {
        IInquiryMgr inquiryMgr = getInquiryMgr();
        IInquiry newInquiry = inquiryMgr.createInquiry();
        newInquiry.addApplication("");
        newInquiry.addObligation("");
        newInquiry.addPolicy("");
        newInquiry.addResource("");
        newInquiry.addUser("");
        //TODO: Robert: need to check the setter for logging level
        
        //Checks that setter and getters are fine
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getActions().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getApplications().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getObligations().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getPolicies().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getPolicyDecisions().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getResources().size());
        assertEquals("Setter should handle empty value properly", 0, newInquiry.getUsers().size());
    }
}