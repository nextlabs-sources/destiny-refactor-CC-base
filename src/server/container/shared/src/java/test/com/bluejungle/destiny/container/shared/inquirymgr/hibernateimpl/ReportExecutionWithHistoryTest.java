/*
 * Created on Mar 20, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTrackingActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This set of tests verifies that the report execution works properly with
 * historical data for the cache tables.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionWithHistoryTest.java#1 $
 */

public class ReportExecutionWithHistoryTest extends BaseReportExecutionTest {

    /**
     * Constructor
     */
    public ReportExecutionWithHistoryTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ReportExecutionWithHistoryTest(String testName) {
        super(testName);
    }

    /**
     * Returns the report execution manager
     * 
     * @return the report execution manager
     */
    protected IReportExecutionMgr getReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgrIn10", ReportExecutionMgrIn10.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IReportExecutionMgr reportMgr = (IReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Session s = getActivityDateSource().getSession();
        try {
            this.sampleDataMgr.deleteUsersAndGroups(s);
            this.sampleDataMgr.createHistoricalUsersAndGroups(s);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * This test verifies that if a user was member of a group for some time,
     * querying on that group returns records for that user only when this user
     * was a member of the group.
     * 
     * @throws InvalidReportArgumentException
     *             if the test fails
     * @throws DataSourceException
     *             if the test fails
     * @throws HibernateException
     *             if the test fails
     */
    public void testHistoricalGroupMembershipWithSingleGroupNameOnTracking() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert rows while Sasha is member of the PSR team
            final int nbPSRRows = 15;
            TestTrackingActivityLogEntryDO tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.NOVEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbPSRRows; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows while Sasha is not member of the PSR team, and PSR
            // group did not even exist yet
            tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.MAY, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 10; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbPSRRows + 5 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert junk rows while Sasha is member of the PSR team
            tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(8)); //Iannis ID
            tracking.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbPSRRows + 25 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("PSR");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbPSRRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbPSRRows, reader.getStatistics().getTotalRowCount().intValue());
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the query on a single user group on the policy
     * activity data works properly
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testHistoricalGroupMembershipWithSingleGroupNameOnPolicy() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert rows while Sasha is member of the PSR team
            final int nbPSRRows = 15;
            TestPolicyActivityLogEntryDO policyLog = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyLog.setUserId(new Long(0)); //Sasha ID
            policyLog.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.NOVEMBER, 5);
            policyLog.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbPSRRows; i++) {
                policyLog.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(i), 1, policyLog);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows while Sasha is not member of the PSR team
            policyLog = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyLog.setUserId(new Long(0)); //Sasha ID
            policyLog.setUserName("sasha@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.MAY, 5);
            policyLog.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 10; i++) {
                policyLog.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPSRRows + 5 + i), 1, policyLog);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert junk rows while Sasha is member of the PSR team
            policyLog = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyLog.setUserId(new Long(8)); //Iannis ID
            policyLog.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            policyLog.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                policyLog.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPSRRows + 25 + i), 1, policyLog);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("PSR");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbPSRRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbPSRRows, reader.getStatistics().getTotalRowCount().intValue());
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the group membership for user works fine if the
     * "asOf" date is set. In this case, the group history should be ignored.
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testAsOfMembershipWithSingleGroupNameOnPolicy() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert rows while Sasha is member of the PSR team
            final int nbSashaPSRRows = 15;
            TestPolicyActivityLogEntryDO tracking = this.sampleDataMgr.getBasicPolicyLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.NOVEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbSashaPSRRows; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows while Sasha is not member of the PSR team
            final int nbSashaNonPSRRows = 10;
            tracking = this.sampleDataMgr.getBasicPolicyLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.MAY, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbSashaNonPSRRows; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbSashaPSRRows + 5 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert junk rows while Sasha is member of the PSR team
            tracking = this.sampleDataMgr.getBasicPolicyLogRecord();
            tracking.setUserId(new Long(8)); //Iannis ID
            tracking.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbSashaPSRRows + 25 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("PSR");

            //Use a data where PSR team contains Sasha - All Sasha's rows
            // shoudl be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 15));
            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSashaPSRRows + nbSashaNonPSRRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSashaPSRRows + nbSashaNonPSRRows, reader.getStatistics().getTotalRowCount().intValue());
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            }

            //Retry this report at a data where Sasha is no longer part of PSR,
            // but the group still exists
            report.setAsOf(SampleDataMgr.createDate(2006, Calendar.JANUARY, 1));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());
            assertTrue("The reader should have no rows", !reader.hasNextResult());

            //Retry this report at a data where the PSR group was not even
            // created yet, but sasha existed as a user
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.AUGUST, 1));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());
            assertTrue("The reader should have no rows", !reader.hasNextResult());

        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the group membership for user works fine if the
     * "asOf" date is set. In this case, the group history should be ignored.
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testAsOfMembershipWithSingleGroupNameOnTracking() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert rows while Sasha is member of the PSR team
            final int nbSashaPSRRows = 15;
            TestTrackingActivityLogEntryDO tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.NOVEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbSashaPSRRows; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows while Sasha is not member of the PSR team
            final int nbSashaNonPSRRows = 10;
            tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.MAY, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbSashaNonPSRRows; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbSashaPSRRows + 5 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert junk rows while Sasha is member of the PSR team
            tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(8)); //Iannis ID
            tracking.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbSashaPSRRows + 25 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("PSR");

            //Use a data where PSR team contains Sasha - All Sasha's rows
            // shoudl be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 15));
            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSashaPSRRows + nbSashaNonPSRRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSashaPSRRows + nbSashaNonPSRRows, reader.getStatistics().getTotalRowCount().intValue());
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            }

            //Retry this report at a data where Sasha is no longer part of PSR,
            // but the group still exists
            report.setAsOf(SampleDataMgr.createDate(2006, Calendar.JANUARY, 1));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());
            assertTrue("The reader should have no rows", !reader.hasNextResult());

            //Retry this report at a data where the PSR group was not even
            // created yet, but sasha existed as a user
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.AUGUST, 1));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());
            assertTrue("The reader should have no rows", !reader.hasNextResult());

        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the user group membership boundaries are working
     * properly (active From is included, activeTo is excluded)
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testMembershipBoundariesOnTracking() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert one row when Sasha becomes member of the PSR team
            TestTrackingActivityLogEntryDO tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(0)); //Sasha ID
            tracking.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 3);
            tracking.setAction(ActionEnumType.ACTION_PRINT);
            tracking.setTimestamp(cal);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), 1, tracking);

            //insert one row when Sasha stops being a member of the PSR team
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 28);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            tracking.setTimestamp(cal);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(1), 1, tracking);

            //Insert junk rows while Sasha is member of the PSR team
            tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
            tracking.setUserId(new Long(8)); //Iannis ID
            tracking.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            tracking.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                tracking.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(2 + 25 + i), 1, tracking);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("PSR");

            //Use a data where PSR team contains Sasha - All sasha's rows
            // should be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 15));
            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getTotalRowCount().intValue());

            //Use a data where PSR team contains Sasha - The row at the end of
            // the time period should not be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.DECEMBER, 28));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());

            //Use the date where Sasha just joined the PSR team- The row at the
            // end of
            // the time period should not be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 3));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getTotalRowCount().intValue());

            //Remove the "asOf" and try to query for sasha's record. Only one
            // row should be returned.
            report.setAsOf(null);
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 1, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 1, reader.getStatistics().getTotalRowCount().intValue());
            IResultData data = reader.nextResult();
            assertNotNull("There should be one result returned", data);
            assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
            IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
            assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            assertEquals("Action should be correct", ActionEnumType.ACTION_PRINT, resultData.getAction());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that the member boundaries on policies are working
     * properly (activeFrom is included, activeTo is excluded).
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testMembershipBoundariesOnPolicy() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //insert one row when Sasha becomes member of the PSR team
            TestPolicyActivityLogEntryDO policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(0)); //Sasha ID
            policyActivity.setUserName("sasha@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 3);
            policyActivity.setAction(ActionEnumType.ACTION_PRINT);
            policyActivity.setTimestamp(cal);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), 1, policyActivity);

            //insert one row when Sasha stops being a member of the PSR team
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 28);
            policyActivity.setAction(ActionEnumType.ACTION_DELETE);
            policyActivity.setTimestamp(cal);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(1), 1, policyActivity);

            //Insert junk rows while Sasha is member of the PSR team
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(8)); //Iannis ID
            policyActivity.setUserName("ihanen@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.DECEMBER, 5);
            policyActivity.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < 7; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(2 + 25 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("PSR");

            //Use a data where PSR team contains Sasha - All sasha's rows
            // should be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 15));
            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getTotalRowCount().intValue());

            //Use a data where PSR team contains Sasha - The row at the end of
            // the time period should not be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.DECEMBER, 28));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 0, reader.getStatistics().getTotalRowCount().intValue());

            //Use the date where Sasha just joined the PSR team- The row at the
            // end of
            // the time period should not be returned
            report.setAsOf(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 3));
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 2, reader.getStatistics().getTotalRowCount().intValue());

            //Remove the "asOf" and try to query for sasha's record. Only one
            // row should be returned.
            report.setAsOf(null);
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", 1, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", 1, reader.getStatistics().getTotalRowCount().intValue());
            IResultData data = reader.nextResult();
            assertNotNull("There should be one result returned", data);
            assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
            IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
            assertEquals("User name should be correct", "sasha@bluejungle.com", resultData.getUserName());
            assertEquals("Action should be correct", ActionEnumType.ACTION_PRINT, resultData.getAction());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that query by user group with historical data works
     * fine, when multiple group names are provided
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testHistoricalGroupMembershipWithMultipleGroupNameOnTracking() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //Insert rows for the engineering team (iannis)
            final int nbIannisRows = 150;
            TestTrackingActivityLogEntryDO documentActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            documentActivity.setUserId(new Long(8)); //Iannis ID
            documentActivity.setUserName("ihanen@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            documentActivity.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbIannisRows; i++) {
                documentActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(i), 1, documentActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for the engineering team (sergey)
            final int nbSergeyRows = 100;
            documentActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            documentActivity.setUserId(new Long(15)); //Sergey ID
            documentActivity.setUserName("sergey@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.AUGUST, 1);
            cal.add(Calendar.MINUTE, 5);
            documentActivity.setAction(ActionEnumType.ACTION_OPEN);
            for (int i = 0; i < nbSergeyRows; i++) {
                documentActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + 1 + i), 1, documentActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Andy Han (Andy is not member of any group)
            final int nbAndyRows = 65;
            documentActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            documentActivity.setUserId(new Long(1)); //Andy ID
            documentActivity.setUserName("andy@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            documentActivity.setAction(ActionEnumType.ACTION_EDIT);
            for (int i = 0; i < nbAndyRows; i++) {
                documentActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + nbSergeyRows + 1 + i), 1, documentActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Scott
            final int nbScottRows = 30;
            documentActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            documentActivity.setUserId(new Long(14)); //Scott ID
            documentActivity.setUserName("scott@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 1);
            cal.add(Calendar.MINUTE, 5);
            documentActivity.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbScottRows; i++) {
                documentActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + nbSergeyRows + nbAndyRows + 1 + i), 1, documentActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("PSR");
            report.getInquiry().addUser("PF");
            report.getInquiry().addUser("Engineering");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbScottRows + nbSergeyRows + nbIannisRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbScottRows + nbSergeyRows + nbIannisRows, reader.getStatistics().getTotalRowCount().intValue());
            Set correctUsers = new HashSet();
            correctUsers.add("scott@bluejungle.com");
            correctUsers.add("ihanen@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }

            //For this test, Scott has no data while he is in the tools group.
            // Hence, that query should only return rows about Sergey
            report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("Tools");
            report.getInquiry().addUser("PF");

            execMgr = getReportExecutionMgr();
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows, reader.getStatistics().getTotalRowCount().intValue());
            correctUsers = new HashSet();
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that query by user group with historical data works
     * fine, when multiple group names are provided
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testHistoricalGroupMembershipWithMultipleGroupNameOnPolicy() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //Insert rows for the engineering team (iannis)
            final int nbIannisRows = 150;
            TestPolicyActivityLogEntryDO policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(8)); //Iannis ID
            policyActivity.setUserName("ihanen@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbIannisRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for the engineering team (sergey)
            final int nbSergeyRows = 100;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(15)); //Sergey ID
            policyActivity.setUserName("sergey@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.AUGUST, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_OPEN);
            for (int i = 0; i < nbSergeyRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Andy Han (Andy is not member of any group)
            final int nbAndyRows = 65;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(1)); //Andy ID
            policyActivity.setUserName("andy@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_EDIT);
            for (int i = 0; i < nbAndyRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + nbSergeyRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Scott
            final int nbScottRows = 30;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(14)); //Scott ID
            policyActivity.setUserName("scott@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbScottRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + nbSergeyRows + nbAndyRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("PSR");
            report.getInquiry().addUser("PF");
            report.getInquiry().addUser("Engineering");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbScottRows + nbSergeyRows + nbIannisRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbScottRows + nbSergeyRows + nbIannisRows, reader.getStatistics().getTotalRowCount().intValue());
            Set correctUsers = new HashSet();
            correctUsers.add("scott@bluejungle.com");
            correctUsers.add("ihanen@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }

            //For this test, Scott has no data while he is in the tools group.
            // Hence, that query should only return rows about Sergey
            report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("Tools");
            report.getInquiry().addUser("PF");

            execMgr = getReportExecutionMgr();
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows, reader.getStatistics().getTotalRowCount().intValue());
            correctUsers = new HashSet();
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that a mixed query by user and user group with
     * historical data works fine on policy activity data.
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testHistoricalGroupMembershipWithMixedUsersOnPolicy() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //Insert rows for the engineering team (iannis)
            final int nbIannisRows = 150;
            TestPolicyActivityLogEntryDO policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(8)); //Iannis ID
            policyActivity.setUserName("ihanen@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbIannisRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for the engineering team (sergey)
            final int nbSergeyRows = 100;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(15)); //Sergey ID
            policyActivity.setUserName("sergey@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.AUGUST, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_OPEN);
            for (int i = 0; i < nbSergeyRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Andy Han (Andy is not member of any group)
            final int nbAndyRows = 65;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(1)); //Andy ID
            policyActivity.setUserName("andy@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_EDIT);
            for (int i = 0; i < nbAndyRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + nbSergeyRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Scott
            final int nbScottRows = 30;
            policyActivity = this.sampleDataMgr.getBasicPolicyLogRecord();
            policyActivity.setUserId(new Long(14)); //Scott ID
            policyActivity.setUserName("scott@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 1);
            cal.add(Calendar.MINUTE, 5);
            policyActivity.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbScottRows; i++) {
                policyActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbIannisRows + nbSergeyRows + nbAndyRows + 1 + i), 1, policyActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("andy@bluejungle.com");
            report.getInquiry().addUser("PSR");
            report.getInquiry().addUser("PF");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows, reader.getStatistics().getTotalRowCount().intValue());
            Set correctUsers = new HashSet();
            correctUsers.add("andy@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }

            //Try another report where everyone should be returned
            report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
            report.getInquiry().addUser("andy@bluejungle.com");
            report.getInquiry().addUser("Engineering");

            execMgr = getReportExecutionMgr();
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows + nbIannisRows + nbScottRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows + nbIannisRows + nbScottRows, reader.getStatistics().getTotalRowCount().intValue());
            correctUsers = new HashSet();
            correctUsers.add("andy@bluejungle.com");
            correctUsers.add("ihanen@bluejungle.com");
            correctUsers.add("scott@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies that a mixed query by user and user group with
     * historical data works fine on document activity data.
     * 
     * @throws InvalidReportArgumentException
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testHistoricalGroupMembershipWithMixedUsersOnTracking() throws InvalidReportArgumentException, DataSourceException, HibernateException {
        final Session s = getActivityDateSource().getSession();
        try {
            //Insert rows for the engineering team (iannis)
            final int nbIannisRows = 150;
            TestTrackingActivityLogEntryDO trackingActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            trackingActivity.setUserId(new Long(8)); //Iannis ID
            trackingActivity.setUserName("ihanen@bluejungle.com");
            Calendar cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            trackingActivity.setAction(ActionEnumType.ACTION_DELETE);
            for (int i = 0; i < nbIannisRows; i++) {
                trackingActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(i), 1, trackingActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for the engineering team (sergey)
            final int nbSergeyRows = 100;
            trackingActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            trackingActivity.setUserId(new Long(15)); //Sergey ID
            trackingActivity.setUserName("sergey@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.AUGUST, 1);
            cal.add(Calendar.MINUTE, 5);
            trackingActivity.setAction(ActionEnumType.ACTION_OPEN);
            for (int i = 0; i < nbSergeyRows; i++) {
                trackingActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + 1 + i), 1, trackingActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Andy Han (Andy is not member of any group)
            final int nbAndyRows = 65;
            trackingActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            trackingActivity.setUserId(new Long(1)); //Andy ID
            trackingActivity.setUserName("andy@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.SEPTEMBER, 1);
            cal.add(Calendar.MINUTE, 5);
            trackingActivity.setAction(ActionEnumType.ACTION_EDIT);
            for (int i = 0; i < nbAndyRows; i++) {
                trackingActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + nbSergeyRows + 1 + i), 1, trackingActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            //Insert rows for Scott
            final int nbScottRows = 30;
            trackingActivity = this.sampleDataMgr.getBasicTrackingLogRecord();
            trackingActivity.setUserId(new Long(14)); //Scott ID
            trackingActivity.setUserName("scott@bluejungle.com");
            cal = SampleDataMgr.createCalDate(2005, Calendar.OCTOBER, 1);
            cal.add(Calendar.MINUTE, 5);
            trackingActivity.setAction(ActionEnumType.ACTION_PRINT);
            for (int i = 0; i < nbScottRows; i++) {
                trackingActivity.setTimestamp(cal);
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRows + nbSergeyRows + nbAndyRows + 1 + i), 1, trackingActivity);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            IReport report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("andy@bluejungle.com");
            report.getInquiry().addUser("PSR");
            report.getInquiry().addUser("PF");

            IReportExecutionMgr execMgr = getReportExecutionMgr();
            IReportResultReader reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows, reader.getStatistics().getTotalRowCount().intValue());
            Set correctUsers = new HashSet();
            correctUsers.add("andy@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }

            //Try another report where everyone should be returned
            report = this.getReportMgr().createReport();
            report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
            report.getInquiry().addUser("andy@bluejungle.com");
            report.getInquiry().addUser("Engineering");

            execMgr = getReportExecutionMgr();
            reader = execMgr.executeReport(report);
            assertNotNull("Reader should be provided", reader);
            assertNotNull("Statistics should be provided", reader.getStatistics());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows + nbIannisRows + nbScottRows, reader.getStatistics().getAvailableRowCount().intValue());
            assertEquals("The correct number of rows should be returned", nbSergeyRows + nbAndyRows + nbIannisRows + nbScottRows, reader.getStatistics().getTotalRowCount().intValue());
            correctUsers = new HashSet();
            correctUsers.add("andy@bluejungle.com");
            correctUsers.add("ihanen@bluejungle.com");
            correctUsers.add("scott@bluejungle.com");
            correctUsers.add("sergey@bluejungle.com");
            while (reader.hasNextResult()) {
                IResultData data = reader.nextResult();
                assertTrue("Data should have the right type", data instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult resultData = (IReportTrackingActivityDetailResult) data;
                assertTrue("User name should be correct", correctUsers.contains(resultData.getUserName()));
            }
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
}
