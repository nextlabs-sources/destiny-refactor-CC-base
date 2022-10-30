/*
 * Created on Apr 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This is the data model test class for the inquiry center. This class tests
 * various data objects used by the inquiry center and makes sure they work
 * properly.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/DataModelTest.java#5 $
 */

public class DataModelTest extends BaseReportExecutionTest {

    private SampleDataMgr sampleDataMgr = new SampleDataMgr();

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * This test verifies the class basics for the policy activity class
     */
    public void testPolicyActivityDOClassBasics() {
        PolicyActivityLogDO policyLog = new PolicyActivityLogDO();
        assertTrue(policyLog instanceof IPolicyActivityLog);
    }

    /**
     * This test verifies that the time field is properly stored accross
     * multiple columns in the database for the policy activity data object
     */
    public void testPolicyActivityTimeField() {
        TestPolicyActivityLogEntryDO policyLog = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int dayNb = 15;
        Calendar april15 = Calendar.getInstance();
        april15.set(Calendar.MONTH, Calendar.APRIL);
        april15.set(Calendar.DAY_OF_MONTH, dayNb);
        policyLog.setTimestamp(april15);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), 1, policyLog);
            Query q = s.createQuery("select log.timestamp.monthNb, log.timestamp.dayNb from TestPolicyActivityLogEntryDO log where log.id = :id");
            q.setParameter("id", new Long(0));
            Object result = q.uniqueResult();
            assertTrue("Query should return two elements", result instanceof Object[]);
            Object[] arrayResult = (Object[]) result;
            assertEquals("Query should return two elements", 2, arrayResult.length);
            april15.set(Calendar.HOUR_OF_DAY, 0);
            april15.set(Calendar.MINUTE, 0);
            april15.set(Calendar.SECOND, 0);
            april15.set(Calendar.MILLISECOND, 0);
            assertEquals("Custom date field should store the correct day number", new Long(april15.getTimeInMillis()), arrayResult[1]);
            april15.set(Calendar.DAY_OF_MONTH, 1);
            assertEquals("Custom date field should store the correct month number", new Long(april15.getTimeInMillis()), arrayResult[0]);

        } catch (HibernateException e) {
            fail("Error when inserting dated policy record");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies the setters and getters for the policy activity class
     */
    public void testPolicyActivityDOSettersAndGetters() {
        PolicyActivityLogDO activityLog = new PolicyActivityLogDO();
        final ActionEnumType myAction = ActionEnumType.ACTION_MOVE;
        activityLog.setAction(myAction);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myAction, activityLog.getAction());
        final Long myAppId = new Long(35);
        activityLog.setApplicationId(myAppId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myAppId, activityLog.getApplicationId());
        final String myAppName = "myAppName";
        activityLog.setApplicationName(myAppName);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myAppName, activityLog.getApplicationName());
        final Long myDecisionReqId = new Long(30);
        activityLog.setDecisionRequestId(myDecisionReqId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myDecisionReqId, activityLog.getDecisionRequestId());

        final FromResourceInformationDO myFromResInfo = new FromResourceInformationDO();
        Calendar now = Calendar.getInstance();
        myFromResInfo.setCreatedDate(now);
        myFromResInfo.setModifiedDate(now);
        myFromResInfo.setName("fromResource");
        final String myOwnerId = "25-38768-12736";
        myFromResInfo.setOwnerId(myOwnerId);
        final Long mySize = new Long(1000);
        myFromResInfo.setSize(mySize);
        activityLog.setFromResourceInfo(myFromResInfo);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myFromResInfo, activityLog.getFromResourceInfo());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", now, activityLog.getFromResourceInfo().getCreatedDate());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", now, activityLog.getFromResourceInfo().getModifiedDate());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", "fromResource", activityLog.getFromResourceInfo().getName());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myOwnerId, activityLog.getFromResourceInfo().getOwnerId());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", mySize, activityLog.getFromResourceInfo().getSize());

        final Long myHostId = new Long(55);
        activityLog.setHostId(myHostId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myHostId, activityLog.getHostId());
        final Long myId = new Long(30);
        activityLog.setId(myId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myId, activityLog.getId());
        PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_DENY;
        activityLog.setPolicyDecision(myPolicyDecision);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myPolicyDecision, activityLog.getPolicyDecision());
        final Long myPolicyId = new Long(125);
        activityLog.setPolicyId(myPolicyId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myPolicyId, activityLog.getPolicyId());
        activityLog.setTimestamp(now);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", now, activityLog.getTimestamp());

        final ToResourceInformationDO myToResInfo = new ToResourceInformationDO();
        myToResInfo.setName("toResource");
        activityLog.setToResourceInfo(myToResInfo);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myToResInfo, activityLog.getToResourceInfo());
        assertEquals("Setter and getters should work for PolicyActivityLogDO", "toResource", activityLog.getToResourceInfo().getName());
        final Long myUserId = new Long(25698);
        activityLog.setUserId(myUserId);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myUserId, activityLog.getUserId());
        final String myUserResponse = "myResponse";
        activityLog.setUserResponse(myUserResponse);
        assertEquals("Setter and getters should work for PolicyActivityLogDO", myUserResponse, activityLog.getUserResponse());
    }

    /**
     * This test verifies the class basics for the policy activity class
     */
    public void testTrackingActivityDOClassBasics() {
        TrackingActivityLogDO trackingLog = new TrackingActivityLogDO();
        assertTrue(trackingLog instanceof ITrackingActivityLog);
    }

    /**
     * This test verifies that the time field is properly stored accross
     * multiple columns in the database for the tracking activity data object
     */
    public void testTrackingActivityTimeField() {
        TestTrackingActivityLogEntryDO trackingLog = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int dayNb = 15;
        Calendar april15 = Calendar.getInstance();
        april15.set(Calendar.MONTH, Calendar.APRIL);
        april15.set(Calendar.DAY_OF_MONTH, dayNb);
        trackingLog.setTimestamp(april15);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), 1, trackingLog);
            Query q = s.createQuery("select log.timestamp.monthNb, log.timestamp.dayNb from TestTrackingActivityLogEntryDO log where log.id = :id");
            q.setParameter("id", new Long(0));
            Object result = q.uniqueResult();
            assertTrue("Query should return two elements", result instanceof Object[]);
            Object[] arrayResult = (Object[]) result;
            assertEquals("Query should return two elements", 2, arrayResult.length);
            april15.set(Calendar.HOUR_OF_DAY, 0);
            april15.set(Calendar.MINUTE, 0);
            april15.set(Calendar.SECOND, 0);
            april15.set(Calendar.MILLISECOND, 0);
            assertEquals("Custom date field should store the correct day number", new Long(april15.getTimeInMillis()), arrayResult[1]);
            april15.set(Calendar.DAY_OF_MONTH, 1);
            assertEquals("Custom date field should store the correct month number", new Long(april15.getTimeInMillis()), arrayResult[0]);
        } catch (HibernateException e) {
            fail("Error when inserting dated policy record");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies the setters and getters for the tracking activity
     * class
     */
    public void testTrackingActivityDOSettersAndGetters() {
        TrackingActivityLogDO trackingLog = new TrackingActivityLogDO();
        final ActionEnumType myAction = ActionEnumType.ACTION_MOVE;
        trackingLog.setAction(myAction);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myAction, trackingLog.getAction());
        final Long myAppId = new Long(35);
        trackingLog.setApplicationId(myAppId);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myAppId, trackingLog.getApplicationId());
        final String myAppName = "myAppName";
        trackingLog.setApplicationName(myAppName);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myAppName, trackingLog.getApplicationName());
        final Long myDecisionReqId = new Long(30);
        final FromResourceInformationDO myFromResInfo = new FromResourceInformationDO();
        Calendar now = Calendar.getInstance();
        myFromResInfo.setCreatedDate(now);
        myFromResInfo.setModifiedDate(now);
        myFromResInfo.setName("fromResource");
        final String myOwnerId = "iherw-897324-28";
        myFromResInfo.setOwnerId(myOwnerId);
        final Long mySize = new Long(1000);
        myFromResInfo.setSize(mySize);
        trackingLog.setFromResourceInfo(myFromResInfo);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myFromResInfo, trackingLog.getFromResourceInfo());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", now, trackingLog.getFromResourceInfo().getCreatedDate());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", now, trackingLog.getFromResourceInfo().getModifiedDate());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", "fromResource", trackingLog.getFromResourceInfo().getName());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myOwnerId, trackingLog.getFromResourceInfo().getOwnerId());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", mySize, trackingLog.getFromResourceInfo().getSize());

        final Long myHostId = new Long(55);
        trackingLog.setHostId(myHostId);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myHostId, trackingLog.getHostId());
        final Long myId = new Long(30);
        trackingLog.setId(myId);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myId, trackingLog.getId());
        PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_DENY;
        final Long myPolicyId = new Long(125);
        trackingLog.setTimestamp(now);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", now, trackingLog.getTimestamp());

        final ToResourceInformationDO myToResInfo = new ToResourceInformationDO();
        myToResInfo.setName("toResource");
        trackingLog.setToResourceInfo(myToResInfo);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myToResInfo, trackingLog.getToResourceInfo());
        assertEquals("Setter and getters should work for TrackingActivityLogDO", "toResource", trackingLog.getToResourceInfo().getName());
        final Long myUserId = new Long(25698);
        trackingLog.setUserId(myUserId);
        assertEquals("Setter and getters should work for TrackingActivityLogDO", myUserId, trackingLog.getUserId());
    }
}