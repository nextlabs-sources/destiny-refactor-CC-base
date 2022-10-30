/* Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTrackingActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.SortDirectionType;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This class tests the report execution. Through various tests, it inserts
 * sample data in the database, queries the data and compares the query results
 * with the expected data.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionTest.java#1 $
 */

public class ReportExecutionTest extends BaseReportExecutionTest {

    private static final int NB_ANDY_RECORDS = 10;
    private static final int NB_IANNIS_RECORDS = 20;
    private static final int NB_KENI_RECORDS = 30;
    private static final int NB_SASHA_RECORDS = 40;
    private static final int NB_DAVIDL_RECORDS = 50;
    private static final int NB_ENGR_RECORDS = NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS;
    private static final int NB_MARKETING_RECORDS = NB_ANDY_RECORDS;
    private static final int NB_IT_RECORDS = NB_DAVIDL_RECORDS;
    private static final int NB_RESOURCE_A_RECORDS = 25;
    private static final int NB_RESOURCE_A2_RECORDS = 35;
    private static final int NB_RESOURCE_B_RECORDS = 20;
    private static final int NB_RESOURCE_C_RECORDS = 7;
    private static final int NB_RESOURCE_D_RECORDS = 9;
    private static final int NB_RESOURCE_E_RECORDS = 31;

    /**
     * Constructor
     */
    public ReportExecutionTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ReportExecutionTest(String testName) {
        super(testName);
    }

    /**
     * Returns an instance of the report execution manager
     * 
     * @return an instance of the report execution manager
     */
    protected IReportExecutionMgr getReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgr", ReportExecutionMgrStatefulImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IReportExecutionMgr reportMgr = (IReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * This function inserts data in the log table so that queries for users and
     * groups can be performed.
     */
    protected void insertUserPolicyActivityRecords() {
        //Create template log entries for the 3 users
        TestPolicyActivityLogEntryDO iannisLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO keniLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO sashaLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO andyLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO davidLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        //Create records from user Andy (id is 1)
        andyLogEntry.setUserId(new Long(1));
        andyLogEntry.setUserName("ahan@bluejungle.com");
        andyLogEntry.setLevel(0);
        //Create records from user David (id is 4)
        davidLogEntry.setUserId(new Long(4));
        davidLogEntry.setUserName("dlim@bluejungle.com");
        davidLogEntry.setLevel(1);
        //Create records from user Iannis (id is 8)
        iannisLogEntry.setUserId(new Long(8));
        iannisLogEntry.setUserName("ihanen@bluejungle.com");
        iannisLogEntry.setLevel(2);
        //Create records from user Keny (id is 11)
        keniLogEntry.setUserId(new Long(11));
        keniLogEntry.setUserName("pkeni@bluejungle.com");
        keniLogEntry.setLevel(3);
        //Create records from user Sasha (id is 0)
        sashaLogEntry.setUserId(new Long(0));
        sashaLogEntry.setUserName("sasha@bluejungle.com");
        sashaLogEntry.setLevel(0);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), NB_IANNIS_RECORDS, iannisLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(NB_IANNIS_RECORDS + 10), NB_KENI_RECORDS, keniLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + 20), NB_SASHA_RECORDS, sashaLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS + 30), NB_DAVIDL_RECORDS, davidLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS + NB_DAVIDL_RECORDS + 40), NB_ANDY_RECORDS, andyLogEntry);
        } catch (HibernateException e) {
            fail("Test failed when inserting sample log for users: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts tracking records for user
     */
    protected void insertUserTrackingActivityRecords() {
        //Create template log entries for the 3 users
        TestTrackingActivityLogEntryDO iannisLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO keniLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO sashaLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO andyLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO davidLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        //Create records from user Andy (id is 1)
        andyLogEntry.setUserId(new Long(1));
        andyLogEntry.setUserName("ahan@bluejungle.com");
        andyLogEntry.setLevel(0);
        //Create records from user David (id is 4)
        davidLogEntry.setUserId(new Long(4));
        davidLogEntry.setUserName("dlim@bluejungle.com");
        davidLogEntry.setLevel(1);
        //Create records from user Iannis (id is 8)
        iannisLogEntry.setUserId(new Long(8));
        iannisLogEntry.setUserName("ihanen@bluejungle.com");
        iannisLogEntry.setLevel(2);
        //Create records from user Keny (id is 11)
        keniLogEntry.setUserId(new Long(11));
        keniLogEntry.setUserName("pkeni@bluejungle.com");
        keniLogEntry.setLevel(3);
        //Create records from user Sasha (id is 0)
        sashaLogEntry.setUserId(new Long(0));
        sashaLogEntry.setUserName("sasha@bluejungle.com");
        sashaLogEntry.setLevel(0);
        
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), NB_IANNIS_RECORDS, iannisLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_IANNIS_RECORDS + 10), NB_KENI_RECORDS, keniLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + 20), NB_SASHA_RECORDS, sashaLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS + 30), NB_DAVIDL_RECORDS, davidLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS + NB_DAVIDL_RECORDS + 40), NB_ANDY_RECORDS, andyLogEntry);
        } catch (HibernateException e) {
            fail("Test failed when inserting sample log for users: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert resource data in the tracking log
     */
    protected void insertResourceTrakingActivityRecords() {
        TestTrackingActivityLogEntryDO resourceALogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceA2LogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceBLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceCLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceDLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceELogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        FromResourceInformationDO resourceAInfo = new FromResourceInformationDO();
        resourceAInfo.setCreatedDate(Calendar.getInstance());
        resourceAInfo.setModifiedDate(Calendar.getInstance());
        resourceAInfo.setName("file:///Z:/ResourceA");
        resourceAInfo.setOwnerId("8");
        resourceAInfo.setSize(new Long(10000));
        resourceALogEntry.setFromResourceInfo(resourceAInfo);

        FromResourceInformationDO resourceA2Info = new FromResourceInformationDO();
        resourceA2Info.setCreatedDate(Calendar.getInstance());
        resourceA2Info.setModifiedDate(Calendar.getInstance());
        resourceA2Info.setName("file:///Y:/ResourceA2");
        resourceA2Info.setOwnerId("5");
        resourceA2Info.setSize(new Long(50));
        resourceA2LogEntry.setFromResourceInfo(resourceA2Info);

        FromResourceInformationDO resourceBInfo = new FromResourceInformationDO();
        resourceBInfo.setCreatedDate(Calendar.getInstance());
        resourceBInfo.setModifiedDate(Calendar.getInstance());
        resourceBInfo.setName("file://HOST/ResourceB");
        resourceBInfo.setOwnerId("7");
        resourceBInfo.setSize(new Long(100000));
        resourceBLogEntry.setFromResourceInfo(resourceBInfo);

        FromResourceInformationDO resourceCInfo = new FromResourceInformationDO();
        resourceCInfo.setCreatedDate(Calendar.getInstance());
        resourceCInfo.setModifiedDate(Calendar.getInstance());
        resourceCInfo.setName("file:///c:/test/ResourceCFile.txt");
        resourceCInfo.setOwnerId("7");
        resourceCInfo.setSize(new Long(100000));
        resourceCLogEntry.setFromResourceInfo(resourceCInfo);

        FromResourceInformationDO resourceDInfo = new FromResourceInformationDO();
        resourceDInfo.setCreatedDate(Calendar.getInstance());
        resourceDInfo.setModifiedDate(Calendar.getInstance());
        resourceDInfo.setName("file://myMachine/c:/shared/ResourceDFile.txt");
        resourceDInfo.setOwnerId("7");
        resourceDInfo.setSize(new Long(100000));
        resourceDLogEntry.setFromResourceInfo(resourceDInfo);
        
        FromResourceInformationDO resourceEInfo = new FromResourceInformationDO();
        resourceEInfo.setCreatedDate(Calendar.getInstance());
        resourceEInfo.setModifiedDate(Calendar.getInstance());
        resourceEInfo.setName("SharePoint://sharepoint2007.bluejungle.com/ReporterSite");
        resourceEInfo.setOwnerId("7");
        resourceEInfo.setSize(new Long(100000));
        resourceELogEntry.setFromResourceInfo(resourceEInfo);

        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), NB_RESOURCE_A_RECORDS, resourceALogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_RESOURCE_A_RECORDS), NB_RESOURCE_B_RECORDS, resourceBLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_RESOURCE_A_RECORDS + NB_RESOURCE_B_RECORDS), NB_RESOURCE_A2_RECORDS, resourceA2LogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_RESOURCE_A_RECORDS + NB_RESOURCE_B_RECORDS + NB_RESOURCE_A2_RECORDS), NB_RESOURCE_C_RECORDS, resourceCLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_RESOURCE_A_RECORDS + NB_RESOURCE_B_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_C_RECORDS), NB_RESOURCE_D_RECORDS, resourceDLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(NB_RESOURCE_A_RECORDS + NB_RESOURCE_B_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_C_RECORDS + NB_RESOURCE_D_RECORDS), NB_RESOURCE_E_RECORDS, resourceELogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting resource records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert resource classes which can be used with the sample tracking data
     * 
     * @throws PolicyServiceException
     * @throws PQLException
     * @throws EntityManagementException
     *  
     */
    private void insertResourceClassesForTrackingActivity() throws EntityManagementException, PQLException, PolicyServiceException {
        String pql = "resource resourceOne = name=\"\\\\\\\\mymachine\\\\c:\\\\**\"";

        this.sampleDataMgr.createResourceClasses(pql);
    }

    /**
     * This test verifies that the instantiation of the report execution manager
     * works fine.
     */
    public void testReportExecutionMgrInstantiation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration badConfig = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgrInstance", ReportExecutionMgrStatefulImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, badConfig);
        boolean exThrown = false;
        try {
            IReportExecutionMgr reportMgr = (IReportExecutionMgr) compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Report execution manager cannot work unless a data source is given in configuration.", exThrown);

        //Pass the good config now
        try {
            IReportExecutionMgr reportMgr = getReportExecutionMgr();
        } catch (RuntimeException e) {
            fail("No exception should be fired when creating report execution manager");
        }
    }

    /**
     * This test verifies that tracking activity can be queried properly.
     */
    public void testReportExecutionQueryAllTrackingActivity() {
        TestTrackingActivityLogEntryDO track = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecords = 50;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, track);
        } catch (HibernateException e) {
            fail("Test failed when inserting sample log for users: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        IReportResultReader reader = null;
        for (int maxFetchRow = -1; maxFetchRow < 19; maxFetchRow += 20) {
            try {
                reader = maxFetchRow < 0 ? reportExecutionMgr.executeReport(report) : reportExecutionMgr.executeReport(report, maxFetchRow);
            } catch (InvalidReportArgumentException e) {
                fail("The report should not have bad arguments");
            } catch (DataSourceException e1) {
                fail("The report execution should not have throw exception");
            }
            assertNotNull("The reader object should not be null", reader);

            if (!reader.hasNextResult()) {
                fail("There should be records returned");
            } else {
                //Walk through the records and see what is returned
                int recordCount = 0;
                while (reader.hasNextResult()) {
                    recordCount++;
                    IResultData resultData = reader.nextResult();
                    assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                    IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                    assertNotNull("Detail tracking results should have an action", detailResult.getAction());
                    assertNotNull("Detail tracking results should have an application name", detailResult.getApplicationName());
                    assertNotNull("Detail tracking results should have a host name", detailResult.getHostName());
                    assertNotNull("Detail tracking results should have an id", detailResult.getId());
                    assertNotNull("Detail tracking results should have a 'from resource' name", detailResult.getFromResourceName());
                    assertNotNull("Detail tracking results should have a 'to resource' name", detailResult.getToResourceName());
                    assertNotNull("Detail tracking results should have a timestamp", detailResult.getTimestamp());
                    assertNotNull("Detail tracking results should have a user name", detailResult.getUserName());
                }
                final int expectedCount = maxFetchRow < 0 ? nbRecords : maxFetchRow;
                assertEquals("All tracking records should be pulled properly by the result reader", expectedCount, recordCount);
                assertEquals("The reader should provide a correct count of the records", nbRecords, reader.getStatistics().getAvailableRowCount().intValue());
                reader.close();
            }
        }
    }

    /**
     * This test verifies the basic query, that queries for everything in the
     * policy activity log.
     */
    public void testReportExecutionQueryAllPolicyActivity() {

        final int nbRecords = 300;

        insertPolicyLogRecords(nbRecords);

        //Most generic report. The report queries everything.
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport report = reportMgr.createReport();
        IInquiry inquiry = report.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);

        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertNotNull("Detail results should have an action", detailResult.getAction());
                assertNotNull("Detail tracking results should have an application name", detailResult.getApplicationName());
                assertNotNull("Detail results should have a host name", detailResult.getHostName());
                assertNotNull("Detail results should have an id", detailResult.getId());
                assertNotNull("Detail results should have a policy decision", detailResult.getPolicyDecision());
                assertNotNull("Detail results should have a policy name", detailResult.getPolicyName());
                assertNotNull("Detail results should have a 'from resource' name", detailResult.getFromResourceName());
                assertNotNull("Detail results should have a 'to resource' name", detailResult.getToResourceName());
                assertNotNull("Detail results should have a timestamp", detailResult.getTimestamp());
                assertNotNull("Detail results should have a user name", detailResult.getUserName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Deletes the log records
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.deletePolicyLogs(s);
            s.close();
        } catch (HibernateException e) {
            fail("Unable to cleanup policy log records");
        }
    }

    /**
     * This test verifies that the summary by policy name query works properly
     */
    public void testReportExecutionQueryAllSummaryByPolicy() {

        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy8LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy12LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        policy5LogEntry.setPolicyId(new Long(5));
        policy8LogEntry.setPolicyId(new Long(8));
        policy12LogEntry.setPolicyId(new Long(12));

        final int nbPolicy5Records = 50;
        final int nbPolicy8Records = 40;
        final int nbPolicy12Records = 30;

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records + 1), nbPolicy8Records, policy8LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records + nbPolicy8Records + 2), nbPolicy12Records, policy12LogEntry);
        } catch (HibernateException e) {
            fail("Test failed when inserting sample log for users: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByPolicy = reportMgr.createReport();
        IInquiry inquiry = allByPolicy.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        allByPolicy.setSummaryType(ReportSummaryType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByPolicy);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by user");
        } else {
            Map results = new HashMap();
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                results.put(summaryResult.getValue(), summaryResult.getCount());
            }
            assertEquals("Summary by policy record count should be valid", 3, results.size());
            assertEquals("Summary by policy record should give correct count", new Long(nbPolicy5Records), results.get("/folder/Policy5"));
            assertEquals("Summary by policy record should give correct count", new Long(nbPolicy8Records), results.get("/folder/Policy8"));
            assertEquals("Summary by policy record should give correct count", new Long(nbPolicy12Records), results.get("/folder/Policy12"));
            reader.close();
        }
    }

    /**
     * This test verifies that sorting on a policy decision is working properly.
     */
    public void testReportExecutionQueryAllPolicyActivitySortOnPolicyDecision() {
        final int nbAllow = 10;
        final PolicyDecisionEnumType allow = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final int nbDeny = 15;
        final PolicyDecisionEnumType deny = PolicyDecisionEnumType.POLICY_DECISION_DENY;
        final int totalNbRecords = nbAllow + nbDeny;
        TestPolicyActivityLogEntryDO allowRecord = this.sampleDataMgr.getBasicPolicyLogRecord();
        allowRecord.setPolicyDecision(allow);
        TestPolicyActivityLogEntryDO denyRecord = this.sampleDataMgr.getBasicPolicyLogRecord();
        denyRecord.setPolicyDecision(deny);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbAllow, allowRecord);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbAllow + 1), nbDeny, denyRecord);
        } catch (HibernateException e) {
            fail("Unable to insert sample data for the test" + e.getLocalizedMessage());
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport sortByActionAscending = reportMgr.createReport();
        IInquiry inquiry = sortByActionAscending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        sortByActionAscending.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        sortByActionAscending.getSortSpec().setSortField(SortFieldType.POLICY_DECISION);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByActionAscending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query sorted by policy decision");
        } else {
            assertEquals("There should be one row for each record", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            PolicyDecisionEnumType currentDecision = null;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                if (currentDecision != null) {
                    String currentDecisionString = currentDecision.getName();
                    PolicyDecisionEnumType detailDecision = detailResult.getPolicyDecision();
                    assertNotNull("There should be a policy decision returned", detailDecision);
                    assertTrue("Results should be sorted by policy decision ascending", currentDecisionString.compareTo(detailDecision.getName()) <= 0);
                }
                currentDecision = detailResult.getPolicyDecision();
            }
        }

        //Try with a descending query
        IReport sortByActionDescending = reportMgr.createReport();
        inquiry = sortByActionDescending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        sortByActionDescending.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        sortByActionDescending.getSortSpec().setSortField(SortFieldType.POLICY_DECISION);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByActionDescending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query with sort descending on policy decision");
        } else {
            assertEquals("There should be one row per record", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            PolicyDecisionEnumType currentDecision = null;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                if (currentDecision != null) {
                    String currentDecisionString = currentDecision.getName();
                    PolicyDecisionEnumType detailDecision = detailResult.getPolicyDecision();
                    assertNotNull("There should be a policy decision returned", detailDecision);
                    assertTrue("Results should be sorted by policy decision descending", currentDecisionString.compareTo(detailDecision.getName()) >= 0);
                }
                currentDecision = detailResult.getPolicyDecision();
            }
        }
    }

    /**
     * This test verifies that the summary query works properly when the query
     * specifies a grouping by days.
     */
    public void testReportExecutionQueryAllPolicyActivitySummaryByDays() {
        final int nbDays = 50;
        TestPolicyActivityLogEntryDO modelPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        Calendar cal2004 = Calendar.getInstance();
        //Start on March 1st, then add new records for every day in the next 50
        // days
        cal2004.set(Calendar.YEAR, 2004);
        cal2004.set(Calendar.MONTH, Calendar.MARCH);
        cal2004.set(Calendar.DAY_OF_MONTH, 1);

        int totalNbRecords = 0;
        Map storedCounts = new HashMap();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            Random r = new Random();
            //Insert a random number of records (between 1 and 6), each day for
            // 50 days
            for (int i = 0; i < nbDays; i++) {
                cal2004.add(Calendar.DAY_OF_MONTH, 1);
                modelPolicy.setTimestamp(cal2004);
                int nbRecords = r.nextInt(5) + 1;
                storedCounts.put(new Long(i), new Integer(nbRecords));
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(totalNbRecords), nbRecords, modelPolicy);
                totalNbRecords += nbRecords;
            }
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByDay = reportMgr.createReport();
        IInquiry inquiry = allByDay.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        allByDay.setSummaryType(ReportSummaryType.TIME_DAYS);
        allByDay.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        allByDay.getSortSpec().setSortField(SortFieldType.DATE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByDay);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by day");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(nbDays), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be summary records, since a summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTimeInMillis((new Long(summaryResult.getValue())).longValue());
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.HOUR_OF_DAY));
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.MINUTE));
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.MILLISECOND));
                Long count = new Long(((Integer) storedCounts.get(new Long(currentResult))).longValue());
                if (!count.equals(summaryResult.getCount())) {
                    int i = 0;
                }
                assertEquals("The count by day should be correct", count, summaryResult.getCount());
                currentResult++;
            }
            reader.close();
        }
    }

    /**
     * This test verifies that the summary query works properly on policy
     * activity when the query specifies a grouping by months.
     */
    public void testReportExecutionQueryAllPolicyActivitySummaryByMonths() {
        final int nbMonths = 15;
        TestPolicyActivityLogEntryDO modelPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        Calendar cal2004 = Calendar.getInstance();
        //Start on March 1st, then add new records for every day in the next 15
        // months
        cal2004.set(Calendar.YEAR, 2004);
        cal2004.set(Calendar.MONTH, Calendar.MARCH);
        cal2004.set(Calendar.DAY_OF_MONTH, 10);

        int totalNbRecords = 0;
        Map storedCounts = new HashMap();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            Random r = new Random();
            //Insert a random number of records (between 1 and 6), each month
            for (int i = 0; i < nbMonths; i++) {
                cal2004.add(Calendar.MONTH, 1);
                modelPolicy.setTimestamp(cal2004);
                int nbRecords = r.nextInt(5) + 1;
                storedCounts.put(new Long(i), new Integer(nbRecords));
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(totalNbRecords), nbRecords, modelPolicy);
                totalNbRecords += nbRecords;
            }
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by month test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByMonth = reportMgr.createReport();
        IInquiry inquiry = allByMonth.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        allByMonth.setSummaryType(ReportSummaryType.TIME_MONTHS);
        allByMonth.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        allByMonth.getSortSpec().setSortField(SortFieldType.DATE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByMonth);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by month");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(nbMonths), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be summary records, since a summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTimeInMillis((new Long(summaryResult.getValue())).longValue());
                assertEquals("All time results should be set at the beginning of the month", 1, resultCal.get(Calendar.DAY_OF_MONTH));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.HOUR_OF_DAY));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.MINUTE));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.MILLISECOND));
                Long count = new Long(((Integer) storedCounts.get(new Long(currentResult))).longValue());
                assertEquals("The count by month should be correct", count, summaryResult.getCount());
                currentResult++;
            }
            reader.close();
        }
    }

    /**
     * This test verifies that the summary by resource query works properly with
     * policy activity
     */
    public void testReportExecutionQueryAllPolicyActivitySummaryByResource() {
        TestPolicyActivityLogEntryDO resourceALogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceBLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceCLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceDLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final String resourceAName = "file:///C:/rep1/ResourceA.txt";
        final int nbResourceARecords = 10;
        final String resourceBName = "file://HOST/shared/ResourceB.txt";
        final int nbResourceBRecords = 25;
        final String resourceCName = "file:///c:/test/ResourceCFile.txt";
        final int nbResourceCRecords = 32;
        final String resourceDName = "file://myMachine/c:/shared/ResourceDFile.txt";
        final int nbResourceDRecords = 14;

        FromResourceInformationDO resourceAInfo = new FromResourceInformationDO();
        resourceAInfo.setName(resourceAName);
        resourceALogEntry.setFromResourceInfo(resourceAInfo);

        FromResourceInformationDO resourceBInfo = new FromResourceInformationDO();
        resourceBInfo.setName(resourceBName);
        resourceBLogEntry.setFromResourceInfo(resourceBInfo);

        FromResourceInformationDO resourceCInfo = new FromResourceInformationDO();
        resourceCInfo.setName(resourceCName);
        resourceCLogEntry.setFromResourceInfo(resourceCInfo);

        FromResourceInformationDO resourceDInfo = new FromResourceInformationDO();
        resourceDInfo.setName(resourceDName);
        resourceDLogEntry.setFromResourceInfo(resourceDInfo);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbResourceARecords, resourceALogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords), nbResourceBRecords, resourceBLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords), nbResourceCRecords, resourceCLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords + nbResourceCRecords), nbResourceDRecords, resourceDLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting resource records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByResource = reportMgr.createReport();
        IInquiry inquiry = allByResource.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        allByResource.setSummaryType(ReportSummaryType.RESOURCE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByResource);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by resource");
        } else {
            Map results = new HashMap();
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Results should be summary results", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                results.put(summaryResult.getValue(), summaryResult.getCount());
            }
            assertEquals("Summary by resource record count should be valid", 4, results.size());
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceARecords), results.get(resourceAName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceBRecords), results.get(resourceBName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceCRecords), results.get(resourceCName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceDRecords), results.get(resourceDName));
            reader.close();
        }
    }

    /**
     * This test verifies that the summary by user query works properly with
     * policy activity
     */
    public void testReportExecutionQueryAllPolicyActivitySummaryByUser() {
        insertUserPolicyActivityRecords();
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByUser = reportMgr.createReport();
        IInquiry inquiry = allByUser.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        allByUser.setSummaryType(ReportSummaryType.USER);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByUser, -1);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by user");
        } else {
            Map results = new HashMap();
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                results.put(summaryResult.getValue(), summaryResult.getCount());
            }
            assertEquals("Summary by user record count should be valid", 5, results.size());
            assertEquals("Summary by user record should give correct count", new Long(NB_ANDY_RECORDS), results.get("ahan@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_DAVIDL_RECORDS), results.get("dlim@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_IANNIS_RECORDS), results.get("ihanen@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_KENI_RECORDS), results.get("pkeni@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_SASHA_RECORDS), results.get("sasha@bluejungle.com"));
            reader.close();
        }
    }

    /**
     * This test verifies that sorting on an action name is working properly.
     */
    public void testReportExecutionQueryAllTrackingActivitySortOnActionName() {
        final int nbCopy = 10;
        final ActionEnumType copy = ActionEnumType.ACTION_COPY;
        final int nbPrint = 15;
        final ActionEnumType print = ActionEnumType.ACTION_PRINT;
        final int nbRename = 16;
        final ActionEnumType rename = ActionEnumType.ACTION_RENAME;
        final int totalNbRecords = nbCopy + nbPrint + nbRename;
        TestTrackingActivityLogEntryDO copyRecords = this.sampleDataMgr.getBasicTrackingLogRecord();
        copyRecords.setAction(copy);
        TestTrackingActivityLogEntryDO renameRecords = this.sampleDataMgr.getBasicTrackingLogRecord();
        renameRecords.setAction(rename);
        TestTrackingActivityLogEntryDO printRecords = this.sampleDataMgr.getBasicTrackingLogRecord();
        printRecords.setAction(print);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbCopy, copyRecords);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbCopy + 1), nbPrint, printRecords);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbCopy + 1 + nbPrint + 1), nbRename, renameRecords);
        } catch (HibernateException e) {
            fail("Test failed" + e.getLocalizedMessage());
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport sortByActionAscending = reportMgr.createReport();
        IInquiry inquiry = sortByActionAscending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        sortByActionAscending.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        sortByActionAscending.getSortSpec().setSortField(SortFieldType.ACTION);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByActionAscending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query sorted by action");
        } else {
            assertEquals("There should be one row for each record", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            ActionEnumType currentAction = null;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                if (currentAction != null) {
                    String currentActionString = currentAction.getName();
                    ActionEnumType detailAction = detailResult.getAction();
                    assertNotNull("There should be an action returned", detailAction);
                    assertTrue("Results should be sorted by application name ascending", currentActionString.compareTo(detailAction.getName()) <= 0);
                }
                currentAction = detailResult.getAction();
            }
        }

        //Try with a descending query
        IReport sortByActionDescending = reportMgr.createReport();
        inquiry = sortByActionDescending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        sortByActionDescending.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        sortByActionDescending.getSortSpec().setSortField(SortFieldType.ACTION);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByActionDescending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query with sort descending on action");
        } else {
            assertEquals("There should be one row per record", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            ActionEnumType currentAction = null;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                if (currentAction != null) {
                    String currentActionString = currentAction.getName();
                    ActionEnumType detailAction = detailResult.getAction();
                    assertNotNull("There should be an action returned", detailAction);
                    assertTrue("Results should be sorted by application name ascending", currentActionString.compareTo(detailAction.getName()) >= 0);
                }
                currentAction = detailResult.getAction();
            }
        }
    }

    /**
     * This test verifies that application names are fetched properly, and that
     * sorting on application names works fine.
     */
    public void testReportExecutionQueryAllTrackingActivitySortOnApplicationName() {
        final int nbApp1 = 10;
        final String app1Name = "Application1";
        final int nbApp2 = 15;
        final String app2Name = "Application2";
        final int nbApp3 = 16;
        final String app3Name = "Application3";
        final int totalNbRecords = nbApp1 + nbApp2 + nbApp3;
        TestTrackingActivityLogEntryDO app1Tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
        app1Tracking.setApplicationName(app1Name);
        TestTrackingActivityLogEntryDO app2Tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
        app2Tracking.setApplicationName(app2Name);
        TestTrackingActivityLogEntryDO app3Tracking = this.sampleDataMgr.getBasicTrackingLogRecord();
        app3Tracking.setApplicationName(app3Name);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbApp1, app1Tracking);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbApp1 + 1), nbApp2, app2Tracking);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbApp1 + 1 + nbApp2 + 1), nbApp3, app3Tracking);
        } catch (HibernateException e) {
            fail("Test failed" + e.getLocalizedMessage());
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport sortByAppAscending = reportMgr.createReport();
        IInquiry inquiry = sortByAppAscending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        sortByAppAscending.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        sortByAppAscending.getSortSpec().setSortField(SortFieldType.APPLICATION);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByAppAscending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by day");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            String currentAppName = "";
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Results should be sorted by application name ascending", currentAppName.compareTo(detailResult.getApplicationName()) <= 0);
                currentAppName = detailResult.getApplicationName();
            }
        }

        //Try with a descending query
        IReport sortByAppDescending = reportMgr.createReport();
        inquiry = sortByAppDescending.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        sortByAppDescending.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        sortByAppDescending.getSortSpec().setSortField(SortFieldType.APPLICATION);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(sortByAppDescending);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by day");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(totalNbRecords), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            String currentAppName = "ZZZZZZZZZZZZ";
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be detail records", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Results should be sorted by application name descending", currentAppName.compareTo(detailResult.getApplicationName()) >= 0);
                currentAppName = detailResult.getApplicationName();
            }
        }
    }

    /**
     * This test verifies that the summary query works properly on tracking
     * activity when the query specifies a grouping by days.
     */
    public void testReportExecutionQueryAllTrackingActivitySummaryByDays() {
        final int nbDays = 50;
        TestTrackingActivityLogEntryDO modelTracking = this.sampleDataMgr.getBasicTrackingLogRecord();
        Calendar cal2004 = Calendar.getInstance();
        //Start on March 1st, then add new records for every day in the next 50
        // days
        cal2004.set(Calendar.YEAR, 2004);
        cal2004.set(Calendar.MONTH, Calendar.MARCH);
        cal2004.set(Calendar.DAY_OF_MONTH, 1);

        int totalNbRecords = 0;
        Map storedCounts = new HashMap();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            Random r = new Random();
            //Insert a random number of records (between 1 and 6), each day for
            // 50 days
            for (int i = 0; i < nbDays; i++) {
                cal2004.add(Calendar.DAY_OF_MONTH, 1);
                modelTracking.setTimestamp(cal2004);
                int nbRecords = r.nextInt(5) + 1;
                storedCounts.put(new Long(i), new Integer(nbRecords));
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(totalNbRecords), nbRecords, modelTracking);
                totalNbRecords += nbRecords;
            }
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByDay = reportMgr.createReport();
        IInquiry inquiry = allByDay.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        allByDay.setSummaryType(ReportSummaryType.TIME_DAYS);
        allByDay.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        allByDay.getSortSpec().setSortField(SortFieldType.DATE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByDay);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by day");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(nbDays), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be summary records, since a summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTimeInMillis((new Long(summaryResult.getValue())).longValue());
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.HOUR_OF_DAY));
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.MINUTE));
                assertEquals("All time results should be set at the beginning of the day", 0, resultCal.get(Calendar.MILLISECOND));
                Long count = new Long(((Integer) storedCounts.get(new Long(currentResult))).longValue());
                if (!count.equals(summaryResult.getCount())) {
                    int i = 0;
                }
                assertEquals("The count by day should be correct", count, summaryResult.getCount());
                currentResult++;
            }
            reader.close();
        }
    }

    /**
     * This test verifies that the summary query works properly on tracking
     * activity when the query specifies a grouping by months.
     */
    public void testReportExecutionQueryAllTrackingActivitySummaryByMonths() {
        final int nbMonths = 15;
        TestTrackingActivityLogEntryDO modelTracking = this.sampleDataMgr.getBasicTrackingLogRecord();
        Calendar cal2004 = Calendar.getInstance();
        //Start on March 1st, then add new records for every day in the next 15
        // months
        cal2004.set(Calendar.YEAR, 2004);
        cal2004.set(Calendar.MONTH, Calendar.MARCH);
        cal2004.set(Calendar.DAY_OF_MONTH, 10);

        int totalNbRecords = 0;
        Map storedCounts = new HashMap();
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            Random r = new Random();
            //Insert a random number of records (between 1 and 6), each month
            for (int i = 0; i < nbMonths; i++) {
                cal2004.add(Calendar.MONTH, 1);
                modelTracking.setTimestamp(cal2004);
                int nbRecords = r.nextInt(5) + 1;
                storedCounts.put(new Long(i), new Integer(nbRecords));
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(totalNbRecords), nbRecords, modelTracking);
                totalNbRecords += nbRecords;
            }
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by month test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByMonth = reportMgr.createReport();
        IInquiry inquiry = allByMonth.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        allByMonth.setSummaryType(ReportSummaryType.TIME_MONTHS);
        allByMonth.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        allByMonth.getSortSpec().setSortField(SortFieldType.DATE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByMonth);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by month");
        } else {
            assertEquals("There should be one row per day elapsed", new Long(nbMonths), reader.getStatistics().getAvailableRowCount());
            int currentResult = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Records should be summary records, since a summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTimeInMillis((new Long(summaryResult.getValue())).longValue());
                assertEquals("All time results should be set at the beginning of the month", 1, resultCal.get(Calendar.DAY_OF_MONTH));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.HOUR_OF_DAY));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.MINUTE));
                assertEquals("All time results should be set at the beginning of the month", 0, resultCal.get(Calendar.MILLISECOND));
                Long count = new Long(((Integer) storedCounts.get(new Long(currentResult))).longValue());
                assertEquals("The count by month should be correct", count, summaryResult.getCount());
                currentResult++;
            }
            reader.close();
        }
    }

    /**
     * This test verifies that the summary by resource query works properly with
     * policy activity
     */
    public void testReportExecutionQueryAllTrackingActivitySummaryByResource() {
        TestTrackingActivityLogEntryDO resourceALogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceBLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceCLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO resourceDLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        final String resourceAName = "file:///c:/rep1/ResourceA.txt";
        final int nbResourceARecords = 10;
        final String resourceBName = "file://HOST/shared/ResourceB.txt";
        final int nbResourceBRecords = 25;
        final String resourceCName = "file:///c:/test/ResourceCFile.txt";
        final int nbResourceCRecords = 32;
        final String resourceDName = "file://myMachine/c:/shared/ResourceDFile.txt";
        final int nbResourceDRecords = 14;

        FromResourceInformationDO resourceAInfo = new FromResourceInformationDO();
        resourceAInfo.setName(resourceAName);
        resourceALogEntry.setFromResourceInfo(resourceAInfo);

        FromResourceInformationDO resourceBInfo = new FromResourceInformationDO();
        resourceBInfo.setName(resourceBName);
        resourceBLogEntry.setFromResourceInfo(resourceBInfo);

        FromResourceInformationDO resourceCInfo = new FromResourceInformationDO();
        resourceCInfo.setName(resourceCName);
        resourceCLogEntry.setFromResourceInfo(resourceCInfo);

        FromResourceInformationDO resourceDInfo = new FromResourceInformationDO();
        resourceDInfo.setName(resourceDName);
        resourceDLogEntry.setFromResourceInfo(resourceDInfo);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbResourceARecords, resourceALogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbResourceARecords), nbResourceBRecords, resourceBLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords), nbResourceCRecords, resourceCLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords + nbResourceCRecords), nbResourceDRecords, resourceDLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting resource records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByResource = reportMgr.createReport();
        IInquiry inquiry = allByResource.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        allByResource.setSummaryType(ReportSummaryType.RESOURCE);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByResource);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by resource");
        } else {
            Map results = new HashMap();
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Results should be summary results", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                results.put(summaryResult.getValue(), summaryResult.getCount());
            }
            assertEquals("Summary by resource record count should be valid", 4, results.size());
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceARecords), results.get(resourceAName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceBRecords), results.get(resourceBName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceCRecords), results.get(resourceCName));
            assertEquals("Summary by resource record should give correct count", new Long(nbResourceDRecords), results.get(resourceDName));
            reader.close();
        }
    }

    /**
     * This test verifies that the summary on user for tracking activity query
     * works properly
     */
    public void testReportExecutionQueryAllTrackingActivitySummaryByUser() {
        insertUserTrackingActivityRecords();
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allByUser = reportMgr.createReport();
        IInquiry inquiry = allByUser.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        allByUser.setSummaryType(ReportSummaryType.USER);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allByUser);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for query all group by user");
        } else {
            Map results = new HashMap();
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                results.put(summaryResult.getValue(), summaryResult.getCount());
            }
            assertEquals("Summary by user record count should be valid", 5, results.size());
            assertEquals("Summary by user record should give correct count", new Long(NB_ANDY_RECORDS), results.get("ahan@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_DAVIDL_RECORDS), results.get("dlim@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_IANNIS_RECORDS), results.get("ihanen@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_KENI_RECORDS), results.get("pkeni@bluejungle.com"));
            assertEquals("Summary by user record should give correct count", new Long(NB_SASHA_RECORDS), results.get("sasha@bluejungle.com"));
            reader.close();
        }
    }

    /**
     * This test verifies that the right records get returned when a query
     * giving one or more policy names is specified.
     */
    public void testReportExecutionPolicyActivityWithCaseInsensitivePolicyName() {
        //Creates logs for policy id 5, 10 and 15
        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy10LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy15LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbPolicy5Records = 25;
        final int nbPolicy10Records = 20;
        final int nbPolicy15Records = 7;

        policy5LogEntry.setPolicyId(new Long(5));
        policy10LogEntry.setPolicyId(new Long(10));
        policy15LogEntry.setPolicyId(new Long(15));

        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records), nbPolicy10Records, policy10LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy10Records + nbPolicy5Records), nbPolicy15Records, policy15LogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting policy 5 log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for records matching policy 10 or 5
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport policy10Report = reportMgr.createReport();
        IInquiry inquiry = policy10Report.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addPolicy("poLiCY10");
        inquiry.addPolicy("POLICY5");
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(policy10Report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for policy 10 returned");
        } else {
            int recordCount = 0;
            final String policy10Name = "/folder/Policy10";
            final String policy5Name = "/folder/Policy5";
            Set matches = new HashSet();
            matches.add(policy10Name);
            matches.add(policy5Name);
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Results should have policy 10 or policy 5 as their policy name", matches.contains(detailResult.getPolicyName()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbPolicy10Records + nbPolicy5Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbPolicy10Records + nbPolicy5Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

    }

    /**
     * This test verifies the report executes correctly when the inquiry
     * contains one single user name.
     */
    public void testReportExecutionPolicyActivityWithCaseInsensitiveUserName() {
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport iannisReport = reportMgr.createReport();
        iannisReport.setSummaryType(ReportSummaryType.NONE);
        iannisReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        //Enter mixed case user name
        iannisReport.getInquiry().addUser("iHaNEn@BlUEJunGLE.COm");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(iannisReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for case insensitive user query");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Query for user names should be case insensitive", "ihanen@bluejungle.com", detailResult.getUserName());
            }
            assertEquals("The single user query should return the right number of records", NB_IANNIS_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that report executions based on multiple action names
     * are working properly.
     */
    public void testReportExecutionPolicyActivityWithMultipleActionNames() {
        //Creates logs for actions burn, copy and open
        TestPolicyActivityLogEntryDO actionBurnLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO actionCopyLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO actionOpenLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbBurnRecords = 25;
        final int nbCopyRecords = 40;
        final int nbOpenRecords = 50;

        actionBurnLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        actionCopyLogEntry.setAction(ActionEnumType.ACTION_COPY);
        actionOpenLogEntry.setAction(ActionEnumType.ACTION_OPEN);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbBurnRecords, actionBurnLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbBurnRecords), nbCopyRecords, actionCopyLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCopyRecords + nbBurnRecords), nbOpenRecords, actionOpenLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addAction(ActionEnumType.ACTION_EMBED);
        inquiry.addAction(ActionEnumType.ACTION_OPEN);
        Set matchingActions = new HashSet();
        matchingActions.add(ActionEnumType.ACTION_EMBED);
        matchingActions.add(ActionEnumType.ACTION_OPEN);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'copy' action returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Results should have 'burn' or 'open' as their action name", matchingActions.contains(detailResult.getAction()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbBurnRecords + nbOpenRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbBurnRecords + nbOpenRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that a query specifying multiple application names
     * returns the correct results.
     */
    //    public void testReportExecutionWithMultipleApplicationNames() {
    //        //Creates logs for outlook, njstar and excel
    //        TestPolicyActivityLogEntryDO outlookLogEntry =
    // this.sampleDataMgr.getBasicPolicyLogRecord();
    //        TestPolicyActivityLogEntryDO njstarLogEntry =
    // this.sampleDataMgr.getBasicPolicyLogRecord();
    //        TestPolicyActivityLogEntryDO excelLogEntry =
    // this.sampleDataMgr.getBasicPolicyLogRecord();
    //
    //        final int nbOutlookRecords = 25;
    //        final int nbNjstarRecords = 40;
    //        final int nbExcelRecords = 50;
    //
    //        //Outlook id is 2
    //        outlookLogEntry.setApplicationId(new Long(2));
    //        //njstar id is 5
    //        njstarLogEntry.setApplicationId(new Long(5));
    //        //Excel id is 1
    //        excelLogEntry.setApplicationId(new Long(1));
    //
    //        Session s = getActivityDateSource().getSession();
    //        try {
    //            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0),
    // nbOutlookRecords, outlookLogEntry);
    //            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new
    // Long(nbOutlookRecords), nbNjstarRecords, njstarLogEntry);
    //            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new
    // Long(nbNjstarRecords + nbOutlookRecords), nbExcelRecords, excelLogEntry);
    //        } catch (HibernateException e) {
    //            fail("Error when inserting action records");
    //        } finally {
    //            HibernateUtils.closeSession(s, null);
    //        }
    //
    //        IReportMgr reportMgr = getReportMgr();
    //        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
    //        IReport multiAppReport = reportMgr.createReport();
    //        IInquiry inquiry = multiAppReport.getInquiry();
    //        inquiry.setTargetData(InquiryTargetDataType.POLICY);
    //        inquiry.addApplication("njstar");
    //        inquiry.addApplication("excel");
    //        Set matchingNames = new HashSet();
    //        matchingNames.add("njstar");
    //        matchingNames.add("excel");
    //
    //        IReportResultReader reader =
    // reportExecutionMgr.executeReport(multiAppReport);
    //        assertNotNull("The reader object should not be null", reader);
    //        if (!reader.hasNextResult()) {
    //            fail("There should be records for multiple application names returned");
    //        } else {
    //            int recordCount = 0;
    //            while (reader.hasNextResult()) {
    //                recordCount++;
    //                IResultData resultData = reader.nextResult();
    //                assertNotNull("result record should exist", resultData);
    //                assertTrue("Query results should be detailed", resultData instanceof
    // IReportDetailResult);
    //                IReportDetailResult detailResult = (IReportDetailResult) resultData;
    //                assertTrue("Results should have 'njstar' or 'excel' as their application
    // name", matchingNames.contains(detailResult.getApplicationName()));
    //            }
    //            assertEquals("All records should be pulled properly by the result
    // reader", nbExcelRecords + nbNjstarRecords, recordCount);
    //            assertEquals("The reader should provide a correct count of the records",
    // nbExcelRecords + nbNjstarRecords, reader.getRowCount().intValue());
    //            reader.close();
    //        }
    //    }
    /**
     * This test verifies that a queries returning no records are still handled
     * properly, even though no data has to be returned.
     */
    public void testReportExecutionPolicyActivityWithNoResultsReturned() {
        //Creates logs for action burn only
        TestPolicyActivityLogEntryDO actionBurnLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int nbBurnRecords = 25;
        actionBurnLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbBurnRecords, actionBurnLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addAction(ActionEnumType.ACTION_OPEN);

        //Execute the report
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        assertFalse("The reader should indicate that no results are returned", reader.hasNextResult());
        assertEquals("When no records are returned, the reader should give a correct '0' record count.", new Long(0), reader.getStatistics().getAvailableRowCount());
    }

    /**
     * This test verifies that report executions based on action name are
     * working properly.
     */
    public void testReportExecutionPolicyActivityWithSingleActionName() {
        //Creates logs for actions embed, copy and read
        TestPolicyActivityLogEntryDO actionEmbedLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO actionCopyLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO actionReadLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbEmbedRecords = 25;
        final int nbCopyRecords = 40;
        final int nbReadRecords = 50;

        actionEmbedLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        actionCopyLogEntry.setAction(ActionEnumType.ACTION_COPY);
        actionReadLogEntry.setAction(ActionEnumType.ACTION_OPEN);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbEmbedRecords, actionEmbedLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbEmbedRecords), nbCopyRecords, actionCopyLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCopyRecords + nbEmbedRecords), nbReadRecords, actionReadLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addAction(ActionEnumType.ACTION_COPY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e1) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'copy' action returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Results should have 'copy' as their action name", ActionEnumType.ACTION_COPY, detailResult.getAction());
            }
            assertEquals("All records should be pulled properly by the result reader", nbCopyRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbCopyRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that a query on policy activity with effects specified
     * works properly.
     */
    public void testReportExecutionPolicyActivityWithPolicyDecision() {
        TestPolicyActivityLogEntryDO allowLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO denyLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int nbAllow = 25;
        final int nbDeny = 15;
        allowLogEntry.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        denyLogEntry.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbAllow, allowLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbAllow), nbDeny, denyLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting effect records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allowOnlyReport = reportMgr.createReport();
        IInquiry allowOnlyInquiry = allowOnlyReport.getInquiry();
        allowOnlyInquiry.setTargetData(InquiryTargetDataType.POLICY);
        allowOnlyInquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allowOnlyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'allow' policy decision returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Results should have 'allow' as their policy decision name", PolicyDecisionEnumType.POLICY_DECISION_ALLOW, detailResult.getPolicyDecision());
            }
            assertEquals("All records should be pulled properly by the result reader", nbAllow, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbAllow, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Search for deny only
        IReport denyOnlyReport = reportMgr.createReport();
        IInquiry denyOnlyInquiry = denyOnlyReport.getInquiry();
        denyOnlyInquiry.setTargetData(InquiryTargetDataType.POLICY);
        denyOnlyInquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        try {
            reader = reportExecutionMgr.executeReport(denyOnlyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'deny' policy decision returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Results should have 'deny' as their policy decision name", PolicyDecisionEnumType.POLICY_DECISION_DENY, detailResult.getPolicyDecision());
            }
            assertEquals("All records should be pulled properly by the result reader", nbDeny, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbDeny, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Search for both allow and deny
        IReport allowAndDenyReport = reportMgr.createReport();
        IInquiry allowAndDenyInquiry = allowAndDenyReport.getInquiry();
        allowAndDenyInquiry.setTargetData(InquiryTargetDataType.POLICY);
        allowAndDenyInquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        allowAndDenyInquiry.addPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        try {
            reader = reportExecutionMgr.executeReport(allowAndDenyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        final Set matchingSet = new HashSet();
        matchingSet.add(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        matchingSet.add(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'deny' and 'allow' policy decision returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Results should have 'allow' or 'deny' as their policy decision name", matchingSet.contains(detailResult.getPolicyDecision()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbDeny + nbAllow, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbDeny + nbAllow, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

    }

    /**
     * This test verifies that the right records get returned when a query
     * giving one or more policy names is specified.
     */
    public void testReportExecutionPolicyActivityWithPolicyName() {
        //Creates logs for policy id 5, 10 and 15
        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy10LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy15LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbPolicy5Records = 25;
        final int nbPolicy10Records = 20;
        final int nbPolicy15Records = 7;

        policy5LogEntry.setPolicyId(new Long(5));
        policy10LogEntry.setPolicyId(new Long(10));
        policy15LogEntry.setPolicyId(new Long(15));

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records), nbPolicy10Records, policy10LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy10Records + nbPolicy5Records), nbPolicy15Records, policy15LogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting policy 5 log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for records matching policy 10 only
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport policy10Report = reportMgr.createReport();
        IInquiry inquiry = policy10Report.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        final String policy10Name = SampleDataMgr.POLICY_NAME_PATTERN + "10";
        inquiry.addPolicy(policy10Name);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(policy10Report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for policy 10 returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Results should have policy 10 as their policy name", detailResult.getId().longValue() >= nbPolicy5Records);
                assertTrue("Results should have policy 10 as their policy name", detailResult.getId().longValue() < nbPolicy5Records + nbPolicy10Records);
                assertEquals("Results should have policy 10 as their policy name", policy10Name, detailResult.getPolicyName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbPolicy10Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbPolicy10Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records matching either policy 5 or policy 15
        IReport policy5or15Report = reportMgr.createReport();
        inquiry = policy5or15Report.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        final String policy5Name = SampleDataMgr.POLICY_NAME_PATTERN + "5";
        inquiry.addPolicy(policy5Name);
        final String policy15Name = SampleDataMgr.POLICY_NAME_PATTERN + "15";
        inquiry.addPolicy(policy15Name);
        try {
            reader = reportExecutionMgr.executeReport(policy5or15Report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for policy 5 or 15 returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Results should have policy 5 or 15 as their policy name", detailResult.getId().longValue() < nbPolicy5Records || detailResult.getId().longValue() >= nbPolicy5Records + nbPolicy10Records);
                assertTrue("Results should have policy 5 or 15 as their policy name", policy5Name.equals(detailResult.getPolicyName()) || policy15Name.equals(detailResult.getPolicyName()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbPolicy5Records + nbPolicy15Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbPolicy5Records + nbPolicy15Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that wildchards on policy names are working properly.
     */
    public void testReportExecutionPolicyActivityWithPolicyWildcharName() {
        //Creates logs for policy id 5, 10 and 15
        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy10LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy15LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbPolicy5Records = 25;
        final int nbPolicy10Records = 20;
        final int nbPolicy15Records = 7;
        final int nbTotalPolicies = nbPolicy5Records + nbPolicy10Records + nbPolicy15Records;

        policy5LogEntry.setPolicyId(new Long(5));
        policy10LogEntry.setPolicyId(new Long(10));
        policy15LogEntry.setPolicyId(new Long(15));

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records), nbPolicy10Records, policy10LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy10Records + nbPolicy5Records), nbPolicy15Records, policy15LogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting policy log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for records with a wildcard
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport wildcharReport = reportMgr.createReport();
        IInquiry inquiry = wildcharReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        final String allPolicies = SampleDataMgr.POLICY_NAME_PATTERN + "*";
        inquiry.addPolicy(allPolicies);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(wildcharReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for policies returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
            }
            assertEquals("All records should be pulled properly by the result reader", nbTotalPolicies, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbTotalPolicies, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records with double wildchar. One before, and one after the
        // patterns.
        IReport doubleWildcharReport = reportMgr.createReport();
        inquiry = doubleWildcharReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        final String doubleWildcard = "*" + SampleDataMgr.POLICY_NAME_PATTERN + "*";
        inquiry.addPolicy(doubleWildcard);
        try {
            reader = reportExecutionMgr.executeReport(doubleWildcharReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for policy 5 or 15 returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
            }
            assertEquals("All records should be pulled properly by the result reader", nbTotalPolicies, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbTotalPolicies, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records with a wildchar, but where no match should be found
        IReport NoMatchReport = reportMgr.createReport();
        inquiry = NoMatchReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        final String noMatch = "*NoMatch";
        inquiry.addPolicy(noMatch);
        try {
            reader = reportExecutionMgr.executeReport(NoMatchReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (reader.hasNextResult()) {
            fail("There should not be records for the no match query");
        }
        reader.close();
    }

    /**
     * This test verifies that queries containing a resource name are properly
     * executed
     */
    public void testReportExecutionPolicyActivityWithResourceName() {
        TestPolicyActivityLogEntryDO resourceALogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceA2LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceBLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceCLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceDLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO resourceELogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbResourceARecords = 25;
        final int nbResourceA2Records = 35;
        final int nbResourceBRecords = 20;
        final int nbResourceCRecords = 7;
        final int nbResourceDRecords = 9;
        final int nbResourceERecords = 31;

        FromResourceInformationDO resourceAInfo = new FromResourceInformationDO();
        resourceAInfo.setCreatedDate(Calendar.getInstance());
        resourceAInfo.setModifiedDate(Calendar.getInstance());
        resourceAInfo.setName("file:///Z:/ResourceA");
        resourceAInfo.setOwnerId("8");
        resourceAInfo.setSize(new Long(10000));
        resourceALogEntry.setFromResourceInfo(resourceAInfo);

        FromResourceInformationDO resourceA2Info = new FromResourceInformationDO();
        resourceA2Info.setCreatedDate(Calendar.getInstance());
        resourceA2Info.setModifiedDate(Calendar.getInstance());
        resourceA2Info.setName("file:///Y:/ResourceA2");
        resourceA2Info.setOwnerId("5");
        resourceA2Info.setSize(new Long(50));
        resourceA2LogEntry.setFromResourceInfo(resourceA2Info);

        FromResourceInformationDO resourceBInfo = new FromResourceInformationDO();
        resourceBInfo.setCreatedDate(Calendar.getInstance());
        resourceBInfo.setModifiedDate(Calendar.getInstance());
        resourceBInfo.setName("file://HOST/ResourceB");
        resourceBInfo.setOwnerId("7");
        resourceBInfo.setSize(new Long(100000));
        resourceBLogEntry.setFromResourceInfo(resourceBInfo);

        FromResourceInformationDO resourceCInfo = new FromResourceInformationDO();
        resourceCInfo.setCreatedDate(Calendar.getInstance());
        resourceCInfo.setModifiedDate(Calendar.getInstance());
        resourceCInfo.setName("file:///c:/test/ResourceCFile.txt");
        resourceCInfo.setOwnerId("7");
        resourceCInfo.setSize(new Long(100000));
        resourceCLogEntry.setFromResourceInfo(resourceCInfo);

        FromResourceInformationDO resourceDInfo = new FromResourceInformationDO();
        resourceDInfo.setCreatedDate(Calendar.getInstance());
        resourceDInfo.setModifiedDate(Calendar.getInstance());
        resourceDInfo.setName("file://myMachine/c:/shared/ResourceDFile.txt");
        resourceDInfo.setOwnerId("7");
        resourceDInfo.setSize(new Long(100000));
        resourceDLogEntry.setFromResourceInfo(resourceDInfo);
        
        FromResourceInformationDO resourceEInfo = new FromResourceInformationDO();
        resourceEInfo.setCreatedDate(Calendar.getInstance());
        resourceEInfo.setModifiedDate(Calendar.getInstance());
        resourceEInfo.setName("SharePoint://sharepoint2007.bluejungle.com/ReporterSite");
        resourceEInfo.setOwnerId("7");
        resourceEInfo.setSize(new Long(100000));
        resourceELogEntry.setFromResourceInfo(resourceEInfo);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbResourceARecords, resourceALogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords), nbResourceBRecords, resourceBLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords), nbResourceA2Records, resourceA2LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords + nbResourceA2Records), nbResourceCRecords, resourceCLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords + nbResourceA2Records + nbResourceCRecords), nbResourceDRecords, resourceDLogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbResourceARecords + nbResourceBRecords + nbResourceA2Records + nbResourceCRecords + nbResourceDRecords), nbResourceERecords, resourceELogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting resource records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for exact match for resource A
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("Z:\\ResourceA   ");
        IInquiry inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match file:///Z:/ResourceA", "file:///Z:/ResourceA", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceARecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceARecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records matching *sourceA*
        reportMgr = getReportMgr();
        reportExecutionMgr = getReportExecutionMgr();
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" *sourceA*");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().matches(".*sourceA.*"));
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceARecords + nbResourceA2Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceARecords + nbResourceA2Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on c:\\*.txt (which is resource C)
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("  c:\\*.txt   ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceCRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceCRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource C
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" c:\\test\\ResourceCFile.txt ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceCRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceCRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" \\\\myMachine\\c:\\shared\\ResourceDFile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceDRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceDRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try an wildchard match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("\\\\*\\*shared\\*DFile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceDRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceDRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on Resource*
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*ResourceA* ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().startsWith("file:///Z:/ResourceA") || detailResult.getFromResourceName().startsWith("file:///Y:/ResourceA"));
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceARecords + nbResourceA2Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceARecords + nbResourceA2Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        // Now combine a wilcard search on ResourceA* and a non wildcard
        // search on ResourceB + some junk query specification
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*ResourceA* ");
        resourceReport.getInquiry().addResource(" \\\\HOST\\ResourceB  ");
        resourceReport.getInquiry().addResource("*abcNotThere");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);

        Set matchRecords = new HashSet();
        matchRecords.add("file:///Z:/ResourceA");
        matchRecords.add("file:///Y:/ResourceA2");
        matchRecords.add("file://HOST/ResourceB");

        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Returned records should match the query spec", matchRecords.contains(detailResult.getFromResourceName()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceARecords + nbResourceA2Records + nbResourceBRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceARecords + nbResourceA2Records + nbResourceBRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
        
        //Now, try a exact match search on resource E
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("SharePoint://sharepoint2007.bluejungle.com/ReporterSite");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource E returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "SharePoint://sharepoint2007.bluejungle.com/ReporterSite", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceERecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceERecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try an wildchard match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("SharePoint://sharepoint*.bluejungle.com/*orterSite");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource E returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "SharePoint://sharepoint2007.bluejungle.com/ReporterSite", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", nbResourceERecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbResourceERecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that the time period condition is respected properly,
     * and records outside of a given time period are filtered out.
     */
    public void testReportExecutionPolicyActivityWithTimePeriod() {
        TestPolicyActivityLogEntryDO logEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int nbLastYearRecords = 50;
        Calendar lastYear = Calendar.getInstance();

        //Create records as of my birthday last year
        lastYear.set(2004, Calendar.SEPTEMBER, 8);
        logEntry.setTimestamp(lastYear);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLastYearRecords, logEntry);
        } catch (HibernateException e) {
            fail("Error when inserting last year records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Create records as of this year
        final int nbThisYearRecords = 30;
        Calendar thisYear = Calendar.getInstance();
        thisYear.set(2005, Calendar.MARCH, 8);
        logEntry.setTimestamp(thisYear);
        s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbLastYearRecords), nbThisYearRecords, logEntry);
        } catch (HibernateException e) {
            fail("Error when inserting this year records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for the records for year 2004
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport reportFor2004 = reportMgr.createReport();
        IInquiry inquiry = reportFor2004.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        IReportTimePeriod timePeriod = reportFor2004.getTimePeriod();
        Calendar begin = Calendar.getInstance();
        begin.set(2004, Calendar.JANUARY, 1);
        timePeriod.setBeginDate(begin);
        Calendar end = Calendar.getInstance();
        end.set(2004, Calendar.DECEMBER, 31);
        timePeriod.setEndDate(end);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(reportFor2004);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Detail results should belong to year 2004", detailResult.getId().intValue() < nbLastYearRecords);
                assertTrue("Detail results should belong to year 2004", detailResult.getTimestamp().getTimeInMillis() <= end.getTimeInMillis());
                assertTrue("Detail results should belong to year 2004", detailResult.getTimestamp().getTimeInMillis() >= begin.getTimeInMillis());
            }
            assertEquals("All records should be pulled properly by the result reader", nbLastYearRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbLastYearRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, query for the records for year 2005
        IReport reportFor2005 = reportMgr.createReport();
        IInquiry inquiry2005 = reportFor2005.getInquiry();
        inquiry2005.setTargetData(InquiryTargetDataType.POLICY);
        timePeriod = reportFor2005.getTimePeriod();
        begin = Calendar.getInstance();
        begin.set(2005, Calendar.JANUARY, 1);
        timePeriod.setBeginDate(begin);
        end = Calendar.getInstance();
        end.set(2005, Calendar.DECEMBER, 31);
        timePeriod.setEndDate(end);

        try {
            reader = reportExecutionMgr.executeReport(reportFor2005);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Detail results should belong to year 2005", detailResult.getId().intValue() >= nbLastYearRecords);
                assertTrue("Detail results should belong to year 2005", detailResult.getTimestamp().getTimeInMillis() <= end.getTimeInMillis());
                assertTrue("Detail results should belong to year 2005", detailResult.getTimestamp().getTimeInMillis() >= begin.getTimeInMillis());
            }
            assertEquals("All records should be pulled properly by the result reader", nbThisYearRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbThisYearRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that reports with multiple user name and user group
     * names are executed properly. The report should return all the records
     * matching for each group, and for each user, and make sure that overlaps
     * between an individual user name and a group name are not breaking the
     * query.
     */
    public void testReportExecutionPolicyActivityWithMixedUserAndUserGroupNames() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport mixReport = reportMgr.createReport();

        mixReport.setSummaryType(ReportSummaryType.NONE);
        mixReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        //Make Andy overlap with Marketing group - this should work fine.
        mixReport.getInquiry().addUser(" ahan@bluejungle.com");
        mixReport.getInquiry().addUser(" ihanen@bluejungle.com");
        mixReport.getInquiry().addUser(" sasha@bluejungle.com");
        mixReport.getInquiry().addUser(" Marketing");
        mixReport.getInquiry().addUser(" IT");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(mixReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for mixed user and user group names query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
            }
            final int expectedNbRecords = NB_MARKETING_RECORDS + NB_IANNIS_RECORDS + NB_SASHA_RECORDS + NB_IT_RECORDS;
            assertEquals("The mixed user and user group query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that reports with multiple user group are executed
     * properly.
     */
    public void testReportExecutionPolicyActivityWithMultipleUserGroupNames() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport ikReport = reportMgr.createReport();

        ikReport.setSummaryType(ReportSummaryType.NONE);
        ikReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        ikReport.getInquiry().addUser(" Engineering  ");
        ikReport.getInquiry().addUser("Marketing");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(ikReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for multiple group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for multiple user group names query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
            }
            assertEquals("The multiple group query should return the right number of records", NB_ENGR_RECORDS + NB_MARKETING_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_ENGR_RECORDS + NB_MARKETING_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that the report execution works properly when more
     * than one users are specified in the inquiry specification.
     */
    public void testReportExecutionPolicyActivityWithMultipleUserNames() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport ikReport = reportMgr.createReport();

        ikReport.setSummaryType(ReportSummaryType.NONE);
        ikReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        ikReport.getInquiry().addUser("ihanen@bluejungle.com");
        ikReport.getInquiry().addUser(" pkeni@bluejungle.com   ");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(ikReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        Set matchingUsers = new HashSet();
        matchingUsers.add("ihanen@bluejungle.com");
        matchingUsers.add("pkeni@bluejungle.com");

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for multiple user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single multiple query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("In multiple user query, all records returned should have a username with one of the users specified.", matchingUsers.contains(detailResult.getUserName()));
            }
            assertEquals("The multiple user query should return the right number of records", NB_IANNIS_RECORDS + NB_KENI_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS + NB_KENI_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that data coming from a single user group can be
     * fetched properly. This test queries for data from the Engineering
     * department, which has 3 contributors in the policy log table.
     */
    public void testReportExecutionPolicyActivityWithSingleUserGroupName() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport engrReport = reportMgr.createReport();
        engrReport.setSummaryType(ReportSummaryType.NONE);
        engrReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        engrReport.getInquiry().addUser("Engineering");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(engrReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for single user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
            }
            assertEquals("The single group query should return the right number of records", NB_ENGR_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_ENGR_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies the report executes correctly when the inquiry
     * contains one single user name.
     */
    public void testReportExecutionPolicyActivityWithSingleUserName() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport iannisReport = reportMgr.createReport();
        iannisReport.setSummaryType(ReportSummaryType.NONE);
        iannisReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        iannisReport.getInquiry().addUser("ihanen@bluejungle.com");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(iannisReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for single user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("A query for a single user name should return record only involving that user", "ihanen@bluejungle.com", detailResult.getUserName());
            }
            assertEquals("The single user query should return the right number of records", NB_IANNIS_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that the user and group prefixes are working properly
     */
    public void testReportExecutionPolicyActivityWithUserAndGroupPrefixes() {
        insertUserPolicyActivityRecords();
        IReportMgr reportMgr = getReportMgr();
        IReport userReport = reportMgr.createReport();
        userReport.setSummaryType(ReportSummaryType.NONE);
        userReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        userReport.getInquiry().addUser("(User) Engineering");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(userReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (reader.hasNextResult()) {
            fail("The reports should not return any results");
        }
        if (reader != null) {
            reader.close();
        }

        IReport groupReport = reportMgr.createReport();
        groupReport.setSummaryType(ReportSummaryType.NONE);
        groupReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        groupReport.getInquiry().addUser("(Group) Engineering");
        try {
            reader = reportExecutionMgr.executeReport(groupReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("The reports should return results");
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that grouping by something and ordering on count works
     */
    public void testReportExecutionPolicyActivityGroupBySortOnCount() {
        //Creates logs for policy id 5, 10 and 15
        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy10LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy15LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbPolicy5Records = 25;
        final int nbPolicy10Records = 20;
        final int nbPolicy15Records = 7;
        final String policy10Name = SampleDataMgr.POLICY_NAME_PATTERN + "10";
        final String policy5Name = SampleDataMgr.POLICY_NAME_PATTERN + "5";
        final String policy15Name = SampleDataMgr.POLICY_NAME_PATTERN + "15";

        policy5LogEntry.setPolicyId(new Long(5));
        policy10LogEntry.setPolicyId(new Long(10));
        policy15LogEntry.setPolicyId(new Long(15));

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records), nbPolicy10Records, policy10LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy10Records + nbPolicy5Records), nbPolicy15Records, policy15LogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting policy log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Group by policy and sort on count ascending
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport policyReport = reportMgr.createReport();
        IInquiry inquiry = policyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        policyReport.setSummaryType(ReportSummaryType.POLICY);
        //Order results by count ascending
        policyReport.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        policyReport.getSortSpec().setSortField(SortFieldType.COUNT);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(policyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedPolicyName = null;
                if (recordCount == 1) {
                    expectedCount = nbPolicy15Records;
                    expectedPolicyName = policy15Name;
                } else if (recordCount == 2) {
                    expectedCount = nbPolicy10Records;
                    expectedPolicyName = policy10Name;
                } else {
                    expectedCount = nbPolicy5Records;
                    expectedPolicyName = policy5Name;
                }
                assertEquals("Records should be ordered properly", expectedPolicyName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Group by policy and sort on count descending
        policyReport.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        policyReport.getSortSpec().setSortField(SortFieldType.COUNT);
        try {
            reader = reportExecutionMgr.executeReport(policyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedPolicyName = null;
                if (recordCount == 3) {
                    expectedCount = nbPolicy15Records;
                    expectedPolicyName = policy15Name;
                } else if (recordCount == 2) {
                    expectedCount = nbPolicy10Records;
                    expectedPolicyName = policy10Name;
                } else {
                    expectedCount = nbPolicy5Records;
                    expectedPolicyName = policy5Name;
                }
                assertEquals("Records should be ordered properly", expectedPolicyName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that grouping by something and ordering on count works
     */
    public void testReportExecutionPolicyActivityGroupBySortOnName() {
        //Creates logs for policy id 5, 10 and 15
        TestPolicyActivityLogEntryDO policy5LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy10LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO policy15LogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbPolicy5Records = 25;
        final int nbPolicy10Records = 20;
        final int nbPolicy15Records = 7;
        final String policy10Name = SampleDataMgr.POLICY_NAME_PATTERN + "10";
        final String policy5Name = SampleDataMgr.POLICY_NAME_PATTERN + "5";
        final String policy15Name = SampleDataMgr.POLICY_NAME_PATTERN + "15";

        policy5LogEntry.setPolicyId(new Long(5));
        policy10LogEntry.setPolicyId(new Long(10));
        policy15LogEntry.setPolicyId(new Long(15));

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicy5Records, policy5LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy5Records), nbPolicy10Records, policy10LogEntry);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbPolicy10Records + nbPolicy5Records), nbPolicy15Records, policy15LogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting policy log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Group by policy and sort on count ascending
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport groupByPolicyReport = reportMgr.createReport();
        IInquiry inquiry = groupByPolicyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        groupByPolicyReport.setSummaryType(ReportSummaryType.POLICY);
        //Order results by policy name ascending
        groupByPolicyReport.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        groupByPolicyReport.getSortSpec().setSortField(SortFieldType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(groupByPolicyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedPolicyName = null;
                if (recordCount == 1) {
                    expectedCount = nbPolicy10Records;
                    expectedPolicyName = policy10Name;
                } else if (recordCount == 2) {
                    expectedCount = nbPolicy15Records;
                    expectedPolicyName = policy15Name;
                } else {
                    expectedCount = nbPolicy5Records;
                    expectedPolicyName = policy5Name;
                }
                assertEquals("Records should be ordered properly", expectedPolicyName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Group by policy and sort on count descending
        groupByPolicyReport.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        groupByPolicyReport.getSortSpec().setSortField(SortFieldType.POLICY);
        try {
            reader = reportExecutionMgr.executeReport(groupByPolicyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedPolicyName = null;
                if (recordCount == 3) {
                    expectedCount = nbPolicy10Records;
                    expectedPolicyName = policy10Name;
                } else if (recordCount == 2) {
                    expectedCount = nbPolicy15Records;
                    expectedPolicyName = policy15Name;
                } else {
                    expectedCount = nbPolicy5Records;
                    expectedPolicyName = policy5Name;
                }
                assertEquals("Records should be ordered properly", expectedPolicyName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that the using wildchar in user name is working
     * properly
     */
    public void testReportExecutionPolicyActivityWildcharUserOrUserGroupName() {
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport iannisWildCharReport = reportMgr.createReport();
        iannisWildCharReport.setSummaryType(ReportSummaryType.NONE);
        iannisWildCharReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        //Enter mixed case user name with a wildchar
        iannisWildCharReport.getInquiry().addUser("iHa*");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(iannisWildCharReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for case insensitive user query with wilchar");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertEquals("Query for user names should be case insensitive and allow wildchar", "ihanen@bluejungle.com", detailResult.getUserName());
            }
            assertEquals("The single user query should return the right number of records", NB_IANNIS_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }

        //Try with both Iannis and a group name this time
        IReport wildCharReport = reportMgr.createReport();
        wildCharReport.setSummaryType(ReportSummaryType.NONE);
        wildCharReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        //Enter mixed case user name with a wildchar
        wildCharReport.getInquiry().addUser("ihAN*");
        //Should match only the engineering group. With this sample data,
        //only Iannis, Keni and Sasha are inside
        wildCharReport.getInquiry().addUser("*NGI*");
        reportExecutionMgr = getReportExecutionMgr();
        reader = null;
        Set matches = new HashSet();
        matches.add("ihanen@bluejungle.com");
        matches.add("pkeni@bluejungle.com");
        matches.add("sasha@bluejungle.com");
        try {
            reader = reportExecutionMgr.executeReport(wildCharReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for case insensitive user query with wilchar");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
                IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) resultData;
                assertTrue("Query for user and group names should allow wildchar for both user an group", matches.contains(detailResult.getUserName()));
            }
            assertEquals("The single user query should return the right number of records", NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS + NB_KENI_RECORDS + NB_SASHA_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }

    }

    /**
     * This test verifies that only reports that are properly resolved in the
     * policy cache table are returned
     */
    public void testReportExecutionPolicyActivityWithIncompletePolicies() {
        //First, insert valid data and data pertaining to policies not yet in
        // the cache table
        TestPolicyActivityLogEntryDO validRecord = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO unknownPolicy1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO unknownPolicy2 = this.sampleDataMgr.getBasicPolicyLogRecord();

        //Create records for unknown an unknown policy
        unknownPolicy1.setPolicyId(new Long(250));
        final int nbUnknown1 = 40;
        //Create records for another unknown policy
        unknownPolicy2.setPolicyId(new Long(251));
        final int nbUnknown2 = 37;

        final int nbTotalValidRecords = 20;
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbUnknown1, unknownPolicy1);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbUnknown1), nbTotalValidRecords, validRecord);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbTotalValidRecords + nbUnknown1), nbUnknown2, unknownPolicy2);
        } catch (HibernateException e) {
            fail("Test failed when inserting data: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport iannisReport = reportMgr.createReport();
        iannisReport.setSummaryType(ReportSummaryType.NONE);
        iannisReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(iannisReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                reader.nextResult();
            }
            assertEquals("The query results should not display records that don't have a matching policy in the policy cache table", nbTotalValidRecords, recordCount);
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that the correct reports are returned according to the 
     * default logging level
     */
    public void testReportExecutionPolicyActivityWithDefaultLoggingLevel() {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();

        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        //Make Andy overlap with Marketing group - this should work fine.
        report.getInquiry().setLoggingLevel(0);
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for logging level query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
            }
            final int expectedNbRecords = NB_IANNIS_RECORDS + NB_SASHA_RECORDS + NB_ANDY_RECORDS + NB_DAVIDL_RECORDS + NB_KENI_RECORDS;
            assertEquals("The logging level query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that the correct reports are returned according to the 
     * default logging level
     */
    public void testReportExecutionPolicyActivityWithMiddleLoggingLevel() throws DataSourceException, InvalidReportArgumentException {
        //First, insert data
        insertUserPolicyActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();

        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        report.getInquiry().setLoggingLevel(2);
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
       
        reader = reportExecutionMgr.executeReport(report);
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for logging level query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportPolicyActivityDetailResult);
            }
            final int expectedNbRecords = NB_IANNIS_RECORDS + NB_KENI_RECORDS;
            assertEquals("The logging level query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that a queries returning no records are still handled
     * properly, even though no data has to be returned.
     */
    public void testReportExecutionTrackingActivityWithNoResultsReturned() {
        //Creates logs for action burn only
        TestTrackingActivityLogEntryDO actionBurnLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbBurnRecords = 25;
        actionBurnLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbBurnRecords, actionBurnLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        inquiry.addAction(ActionEnumType.ACTION_OPEN);

        //Execute the report
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        assertFalse("The reader should indicate that no results are returned", reader.hasNextResult());
        assertEquals("When no records are returned, the reader should give a correct '0' record count.", new Long(0), reader.getStatistics().getAvailableRowCount());
    }

    /**
     * This test verifies that report executions based on action name are
     * working properly.
     */
    public void testReportExecutionTrackingActivityWithSingleActionName() {
        //Creates logs for actions embed, copy and read
        TestTrackingActivityLogEntryDO actionEmbedLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO actionCopyLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO actionReadLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbEmbedRecords = 25;
        final int nbCopyRecords = 40;
        final int nbReadRecords = 50;

        actionEmbedLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        actionCopyLogEntry.setAction(ActionEnumType.ACTION_COPY);
        actionReadLogEntry.setAction(ActionEnumType.ACTION_OPEN);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbEmbedRecords, actionEmbedLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbEmbedRecords), nbCopyRecords, actionCopyLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbCopyRecords + nbEmbedRecords), nbReadRecords, actionReadLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        inquiry.addAction(ActionEnumType.ACTION_COPY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e1) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'copy' action returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Results should have 'copy' as their action name", ActionEnumType.ACTION_COPY, detailResult.getAction());
            }
            assertEquals("All records should be pulled properly by the result reader", nbCopyRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbCopyRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that report executions based on multiple action names
     * are working properly.
     */
    public void testReportExecutionTrackingActivityWithMultipleActionNames() {
        //Creates logs for actions burn, copy and open
        TestTrackingActivityLogEntryDO actionBurnLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO actionCopyLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO actionOpenLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbBurnRecords = 25;
        final int nbCopyRecords = 40;
        final int nbOpenRecords = 50;

        actionBurnLogEntry.setAction(ActionEnumType.ACTION_EMBED);
        actionCopyLogEntry.setAction(ActionEnumType.ACTION_COPY);
        actionOpenLogEntry.setAction(ActionEnumType.ACTION_OPEN);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbBurnRecords, actionBurnLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbBurnRecords), nbCopyRecords, actionCopyLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbCopyRecords + nbBurnRecords), nbOpenRecords, actionOpenLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting action records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport actionCopyReport = reportMgr.createReport();
        IInquiry inquiry = actionCopyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        inquiry.addAction(ActionEnumType.ACTION_EMBED);
        inquiry.addAction(ActionEnumType.ACTION_OPEN);
        Set matchingActions = new HashSet();
        matchingActions.add(ActionEnumType.ACTION_EMBED);
        matchingActions.add(ActionEnumType.ACTION_OPEN);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(actionCopyReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for 'copy' action returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("Query results should be detailed", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Results should have 'burn' or 'open' as their action name", matchingActions.contains(detailResult.getAction()));
            }
            assertEquals("All records should be pulled properly by the result reader", nbBurnRecords + nbOpenRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbBurnRecords + nbOpenRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that grouping by something on tracking data and
     * ordering on count works
     */
    public void testReportExecutionTrackingActivityGroupBySortOnName() {
        //Creates logs for three users
        TestTrackingActivityLogEntryDO iannisLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO safdarLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO bernardLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbIannisRecords = 25;
        final int nbSafdarRecords = 20;
        final int nbBernardRecords = 7;
        final String iannisName = "ihanen@bluejungle.com";
        final String safdarName = "safdar@bluejungle.com";
        final String bernardName = "bernard@bluejungle.com";

        iannisLogEntry.setUserId(new Long(8)); //Iannis
        iannisLogEntry.setUserName(iannisName);
        safdarLogEntry.setUserId(new Long(13));//Safdar
        safdarLogEntry.setUserName(safdarName);
        bernardLogEntry.setUserId(new Long(17)); //Bernard
        bernardLogEntry.setUserName(bernardName);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbIannisRecords, iannisLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRecords), nbSafdarRecords, safdarLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbSafdarRecords + nbIannisRecords), nbBernardRecords, bernardLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting tracking log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Group by policy and sort on count ascending
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport groupByUserReport = reportMgr.createReport();
        IInquiry inquiry = groupByUserReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        groupByUserReport.setSummaryType(ReportSummaryType.USER);
        //Order results by policy name ascending
        groupByUserReport.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        groupByUserReport.getSortSpec().setSortField(SortFieldType.USER);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(groupByUserReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by user returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedUserName = null;
                if (recordCount == 3) {
                    expectedCount = nbSafdarRecords;
                    expectedUserName = safdarName;
                } else if (recordCount == 1) {
                    expectedCount = nbBernardRecords;
                    expectedUserName = bernardName;
                } else {
                    expectedCount = nbIannisRecords;
                    expectedUserName = iannisName;
                }
                assertEquals("Records should be ordered properly", expectedUserName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Group by policy and sort on count descending
        groupByUserReport.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        groupByUserReport.getSortSpec().setSortField(SortFieldType.USER);
        try {
            reader = reportExecutionMgr.executeReport(groupByUserReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedUserName = null;
                if (recordCount == 1) {
                    expectedCount = nbSafdarRecords;
                    expectedUserName = safdarName;
                } else if (recordCount == 3) {
                    expectedCount = nbBernardRecords;
                    expectedUserName = bernardName;
                } else {
                    expectedCount = nbIannisRecords;
                    expectedUserName = iannisName;
                }
                assertEquals("Records should be ordered properly", expectedUserName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that grouping by something on tracking data and
     * ordering on count works
     */
    public void testReportExecutionTrackingActivityGroupBySortOnCount() {
        //Creates logs for three users
        TestTrackingActivityLogEntryDO iannisLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO safdarLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO bernardLogEntry = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbIannisRecords = 25;
        final int nbSafdarRecords = 20;
        final int nbBernardRecords = 7;
        final String iannisName = "ihanen@bluejungle.com";
        final String safdarName = "safdar@bluejungle.com";
        final String bernardName = "bernard@bluejungle.com";

        iannisLogEntry.setUserId(new Long(8)); //Iannis
        iannisLogEntry.setUserName(iannisName);
        safdarLogEntry.setUserId(new Long(13));//Safdar
        safdarLogEntry.setUserName(safdarName);
        bernardLogEntry.setUserId(new Long(17)); //Bernard
        bernardLogEntry.setUserName(bernardName);

        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbIannisRecords, iannisLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbIannisRecords), nbSafdarRecords, safdarLogEntry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbSafdarRecords + nbIannisRecords), nbBernardRecords, bernardLogEntry);
        } catch (HibernateException e) {
            fail("Error when inserting tracking log records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Group by policy and sort on count ascending
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport groupByUserReport = reportMgr.createReport();
        IInquiry inquiry = groupByUserReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        groupByUserReport.setSummaryType(ReportSummaryType.USER);
        //Order results by policy name ascending
        groupByUserReport.getSortSpec().setSortDirection(SortDirectionType.ASCENDING);
        groupByUserReport.getSortSpec().setSortField(SortFieldType.COUNT);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(groupByUserReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedUserName = null;
                if (recordCount == 2) {
                    expectedCount = nbSafdarRecords;
                    expectedUserName = safdarName;
                } else if (recordCount == 1) {
                    expectedCount = nbBernardRecords;
                    expectedUserName = bernardName;
                } else {
                    expectedCount = nbIannisRecords;
                    expectedUserName = iannisName;
                }
                assertEquals("Records should be ordered properly", expectedUserName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Group by user and sort on count descending
        groupByUserReport.getSortSpec().setSortDirection(SortDirectionType.DESCENDING);
        groupByUserReport.getSortSpec().setSortField(SortFieldType.COUNT);
        try {
            reader = reportExecutionMgr.executeReport(groupByUserReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for group by policy returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("All returned records should be summary record", resultData instanceof IReportSummaryResult);
                IReportSummaryResult summaryResult = (IReportSummaryResult) resultData;
                int expectedCount = 0;
                String expectedUserName = null;
                if (recordCount == 2) {
                    expectedCount = nbSafdarRecords;
                    expectedUserName = safdarName;
                } else if (recordCount == 3) {
                    expectedCount = nbBernardRecords;
                    expectedUserName = bernardName;
                } else {
                    expectedCount = nbIannisRecords;
                    expectedUserName = iannisName;
                }
                assertEquals("Records should be ordered properly", expectedUserName, summaryResult.getValue());
                assertEquals("Records should be ordered properly", new Long(expectedCount), summaryResult.getCount());
            }
            assertEquals("The reader should provide a correct count of the records", 3, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies the report executes correctly when the inquiry
     * contains one single user name.
     */
    public void testReportExecutionTrackingActivityWithCaseInsensitiveResourceName() {
        insertResourceTrakingActivityRecords();
        //Query for exact match for resource A
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("z:\\resOURceA   ");
        IInquiry inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match file:///Z:/ResourceA", "file:///Z:/ResourceA", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records matching *sourceA*
        reportMgr = getReportMgr();
        reportExecutionMgr = getReportExecutionMgr();
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" *SOURCEA*");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().matches(".*sourceA.*"));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on c:\\*.txt (which is resource C)
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("  C:\\*.tXt   ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_C_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_C_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource C
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" c:\\TEST\\resourceCFile.txt ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_C_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_C_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" \\\\MYMACHine\\c:\\shared\\resourcedfile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_D_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_D_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try an wildchard match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("\\\\*\\*shared\\*dfile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_D_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_D_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on Resource*
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*resourceA* ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().startsWith("file:///Z:/ResourceA") || detailResult.getFromResourceName().startsWith("file:///Y:/ResourceA"));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Finally, combine a wilcard search on ResourceA* and a non wildcard
        // search on ResourceB + some junk query specification
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*resourceA* ");
        resourceReport.getInquiry().addResource(" \\\\host\\resourceb  ");
        resourceReport.getInquiry().addResource("*AbcNotHere");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);

        Set matchRecords = new HashSet();
        matchRecords.add("file:///Z:/ResourceA");
        matchRecords.add("file:///Y:/ResourceA2");
        matchRecords.add("file://HOST/ResourceB");

        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match the query spec", matchRecords.contains(detailResult.getFromResourceName()));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_B_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_B_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies the report executes correctly when the inquiry on
     * tracking data contains one single user name.
     */
    public void testReportExecutionTrackingActivityWithSingleUserName() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport iannisReport = reportMgr.createReport();
        iannisReport.setSummaryType(ReportSummaryType.NONE);
        iannisReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        iannisReport.getInquiry().addUser("ihanen@bluejungle.com");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(iannisReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for single user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("A query for a single user name should return record only involving that user", "ihanen@bluejungle.com", detailResult.getUserName());
            }
            assertEquals("The single user query should return the right number of records", NB_IANNIS_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that data coming from a single user group can be
     * fetched properly. This test queries for data from the Engineering
     * department on tracking data, which has 3 contributors in the policy log
     * table.
     */
    public void testReportExecutionTrackingActivityWithSingleUserGroupName() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport engrReport = reportMgr.createReport();
        engrReport.setSummaryType(ReportSummaryType.NONE);
        engrReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        engrReport.getInquiry().addUser("Engineering");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(engrReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for single user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single user query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
            }
            assertEquals("The single group query should return the right number of records", NB_ENGR_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_ENGR_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that the report execution works properly when more
     * than one users are specified in the inquiry specification.
     */
    public void testReportExecutionTrackingActivityWithMultipleUserNames() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport ikReport = reportMgr.createReport();

        ikReport.setSummaryType(ReportSummaryType.NONE);
        ikReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        ikReport.getInquiry().addUser("ihanen@bluejungle.com");
        ikReport.getInquiry().addUser(" pkeni@bluejungle.com   ");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(ikReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        Set matchingUsers = new HashSet();
        matchingUsers.add("ihanen@bluejungle.com");
        matchingUsers.add("pkeni@bluejungle.com");

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for multiple user query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for single multiple query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("In multiple user query, all records returned should have a username with one of the users specified.", matchingUsers.contains(detailResult.getUserName()));
            }
            assertEquals("The multiple user query should return the right number of records", NB_IANNIS_RECORDS + NB_KENI_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_IANNIS_RECORDS + NB_KENI_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that reports with multiple user group are executed
     * properly.
     */
    public void testReportExecutionTrackingActivityWithMultipleUserGroupNames() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport ikReport = reportMgr.createReport();

        ikReport.setSummaryType(ReportSummaryType.NONE);
        ikReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        ikReport.getInquiry().addUser(" Engineering  ");
        ikReport.getInquiry().addUser("Marketing");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(ikReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for multiple group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for multiple user group names query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
            }
            assertEquals("The multiple group query should return the right number of records", NB_ENGR_RECORDS + NB_MARKETING_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_ENGR_RECORDS + NB_MARKETING_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that reports on tracking activity with multiple user
     * name and user group names are executed properly. The report should return
     * all the records matching for each group, and for each user, and make sure
     * that overlaps between an individual user name and a group name are not
     * breaking the query.
     */
    public void testReportExecutionTrackingActivityWithMixedUserAndUserGroupNames() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport mixReport = reportMgr.createReport();

        mixReport.setSummaryType(ReportSummaryType.NONE);
        mixReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        //Make Andy overlap with Marketing group - this should work fine.
        mixReport.getInquiry().addUser(" ahan@bluejungle.com");
        mixReport.getInquiry().addUser(" ihanen@bluejungle.com");
        mixReport.getInquiry().addUser(" sasha@bluejungle.com");
        mixReport.getInquiry().addUser(" Marketing");
        mixReport.getInquiry().addUser(" IT");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(mixReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for mixed user and user group names query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
            }
            final int expectedNbRecords = NB_MARKETING_RECORDS + NB_IANNIS_RECORDS + NB_SASHA_RECORDS + NB_IT_RECORDS;
            assertEquals("The mixed user and user group query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * This test verifies that queries containing a resource name are properly
     * executed
     */
    public void testReportExecutionTrackingActivityWithResourceName() {
        insertResourceTrakingActivityRecords();
        //Query for exact match for resource A
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("Z:\\ResourceA   ");
        IInquiry inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match file:///Z:/ResourceA", "file:///Z:/ResourceA", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Query for records matching *sourceA*
        reportMgr = getReportMgr();
        reportExecutionMgr = getReportExecutionMgr();
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" *sourceA*");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        reader = null;
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().matches(".*sourceA.*"));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on c:\\*.txt (which is resource C)
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("  c:\\*.txt   ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_C_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_C_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource C
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" c:\\test\\ResourceCFile.txt ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource C returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceC", "file:///c:/test/ResourceCFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_C_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_C_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a exact match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource(" \\\\myMachine\\c:\\shared\\ResourceDFile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_D_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_D_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try an wildchard match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("\\\\*\\*shared\\*DFile.txt");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource D returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_D_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_D_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try a wilcard search on Resource*
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*ResourceA* ");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match *resourceA*", detailResult.getFromResourceName().startsWith("file:///Z:/ResourceA") || detailResult.getFromResourceName().startsWith("file:///Y:/ResourceA"));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        // Now combine a wilcard search on ResourceA* and a non wildcard
        // search on ResourceB + some junk query specification
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("*ResourceA* ");
        resourceReport.getInquiry().addResource(" \\\\HOST\\ResourceB  ");
        resourceReport.getInquiry().addResource("*abcNotThere");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);

        Set matchRecords = new HashSet();
        matchRecords.add("file:///Z:/ResourceA");
        matchRecords.add("file:///Y:/ResourceA2");
        matchRecords.add("file://HOST/ResourceB");

        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource A returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Returned records should match the query spec", matchRecords.contains(detailResult.getFromResourceName()));
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_B_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_A_RECORDS + NB_RESOURCE_A2_RECORDS + NB_RESOURCE_B_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
        
        // Now, try a exact match search on resource E
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("SharePoint://sharepoint2007.bluejungle.com/ReporterSite");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource E returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "SharePoint://sharepoint2007.bluejungle.com/ReporterSite", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_E_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_E_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, try an wildchard match search on resource D
        resourceReport = reportMgr.createReport();
        resourceReport.getInquiry().addResource("SharePoint://sharepoint2*.bluejungle.com/*orterSite");
        inquiry = resourceReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportExecutionMgr.executeReport(resourceReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records for resource E returned");
        } else {
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertEquals("Returned records should match resourceD", "SharePoint://sharepoint2007.bluejungle.com/ReporterSite", detailResult.getFromResourceName());
            }
            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_E_RECORDS, recordCount);
            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_E_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that queries containing a resource name are properly
     * executed
     * 
     * @throws DataSourceException
     * @throws InvalidReportArgumentException
     * @throws PolicyServiceException
     * @throws PQLException
     * @throws EntityManagementException
     */
    //TODO: commenting out this test case due to the fact that we removed 
    //      resource class based queries, will need to bring this back when 
    //      we re-enable resource class based queries.
//    public void testReportExecutionTrackingActivityWithResourceClass() throws InterruptedException, InvalidReportArgumentException, DataSourceException, EntityManagementException, PQLException, PolicyServiceException {
//        insertResourceTrakingActivityRecords();
//        insertResourceClassesForTrackingActivity();
//
//        //Wait for policy to be deployed.
//        synchronized (this) {
//            wait(70 * 1000);
//        }
//
//        //Query for exact match for resource A
//        IReportMgr reportMgr = getReportMgr();
//        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
//        IReport resourceReport = reportMgr.createReport();
//        resourceReport.getInquiry().addResource("resourceOne   ");
//        IInquiry inquiry = resourceReport.getInquiry();
//        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
//        IReportResultReader reader = null;
//
//        // Get results
//        reader = reportExecutionMgr.executeReport(resourceReport);
//
//        assertNotNull("The reader object should not be null", reader);
//        if (!reader.hasNextResult()) {
//            fail("There should be records for resource class resourceOne returned");
//        } else {
//            int recordCount = 0;
//            while (reader.hasNextResult()) {
//                recordCount++;
//                IResultData resultData = reader.nextResult();
//                assertNotNull("result record should exist", resultData);
//                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
//                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
//                assertEquals("Returned records should match file://myMachine/c:/shared/ResourceDFile.txt", "file://myMachine/c:/shared/ResourceDFile.txt", detailResult.getFromResourceName());
//            }
//            assertEquals("All records should be pulled properly by the result reader", NB_RESOURCE_D_RECORDS, recordCount);
//            assertEquals("The reader should provide a correct count of the records", NB_RESOURCE_D_RECORDS, reader.getStatistics().getAvailableRowCount().intValue());
//            reader.close();
//        }
//    }

    /**
     * This test verifies that the time period condition on tracking data is
     * respected properly, and records outside of a given time period are
     * filtered out.
     */
    public void testReportExecutionTrackingActivityWithTimePeriod() {
        TestTrackingActivityLogEntryDO logEntry = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbLastYearRecords = 50;
        Calendar lastYear = Calendar.getInstance();

        //Create records as of my birthday last year
        lastYear.set(2004, Calendar.SEPTEMBER, 8);
        logEntry.setTimestamp(lastYear);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLastYearRecords, logEntry);
        } catch (HibernateException e) {
            fail("Error when inserting last year records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Create records as of this year
        final int nbThisYearRecords = 30;
        Calendar thisYear = Calendar.getInstance();
        thisYear.set(2005, Calendar.MARCH, 8);
        logEntry.setTimestamp(thisYear);
        s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbLastYearRecords), nbThisYearRecords, logEntry);
        } catch (HibernateException e) {
            fail("Error when inserting this year records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for the records for year 2004
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport reportFor2004 = reportMgr.createReport();
        IInquiry inquiry = reportFor2004.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
        IReportTimePeriod timePeriod = reportFor2004.getTimePeriod();
        Calendar begin = Calendar.getInstance();
        begin.set(2004, Calendar.JANUARY, 1);
        timePeriod.setBeginDate(begin);
        Calendar end = Calendar.getInstance();
        end.set(2004, Calendar.DECEMBER, 31);
        timePeriod.setEndDate(end);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(reportFor2004);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Detail results should belong to year 2004", detailResult.getId().intValue() < nbLastYearRecords);
                assertTrue("Detail results should belong to year 2004", detailResult.getTimestamp().getTimeInMillis() <= end.getTimeInMillis());
                assertTrue("Detail results should belong to year 2004", detailResult.getTimestamp().getTimeInMillis() >= begin.getTimeInMillis());
            }
            assertEquals("All records should be pulled properly by the result reader", nbLastYearRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbLastYearRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Now, query for the records for year 2005
        IReport reportFor2005 = reportMgr.createReport();
        IInquiry inquiry2005 = reportFor2005.getInquiry();
        inquiry2005.setTargetData(InquiryTargetDataType.ACTIVITY);
        timePeriod = reportFor2005.getTimePeriod();
        begin = Calendar.getInstance();
        begin.set(2005, Calendar.JANUARY, 1);
        timePeriod.setBeginDate(begin);
        end = Calendar.getInstance();
        end.set(2005, Calendar.DECEMBER, 31);
        timePeriod.setEndDate(end);

        try {
            reader = reportExecutionMgr.executeReport(reportFor2005);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
                IReportTrackingActivityDetailResult detailResult = (IReportTrackingActivityDetailResult) resultData;
                assertTrue("Detail results should belong to year 2005", detailResult.getId().intValue() >= nbLastYearRecords);
                assertTrue("Detail results should belong to year 2005", detailResult.getTimestamp().getTimeInMillis() <= end.getTimeInMillis());
                assertTrue("Detail results should belong to year 2005", detailResult.getTimestamp().getTimeInMillis() >= begin.getTimeInMillis());
            }
            assertEquals("All records should be pulled properly by the result reader", nbThisYearRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbThisYearRecords, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Verify the boundary conditions - Insert data just at midnight, and
        // just one before
        final TestTrackingActivityLogEntryDO sept8Entry = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbSept8Records = 20;
        Calendar sept82003 = Calendar.getInstance();
        sept82003.set(2003, Calendar.SEPTEMBER, 8);
        sept82003.set(Calendar.HOUR_OF_DAY, 0);
        sept82003.set(Calendar.MINUTE, 0);
        sept82003.set(Calendar.SECOND, 0);
        sept82003.set(Calendar.MILLISECOND, 0);
        sept8Entry.setTimestamp(sept82003);

        Calendar sept72003 = Calendar.getInstance();
        sept72003.setTimeInMillis(sept82003.getTimeInMillis() - 1);
        final int nbSept7Records = 10;
        final TestTrackingActivityLogEntryDO sept7Entry = this.sampleDataMgr.getBasicTrackingLogRecord();
        sept7Entry.setTimestamp(sept72003);
        s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.deleteTrackingLogs(s);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbSept8Records, sept8Entry);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbSept8Records), nbSept7Records, sept7Entry);
        } catch (HibernateException e) {
            fail("Error when inserting last year records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query data
        IReport reportForSept7 = reportMgr.createReport();
        IInquiry inquiryForSept7 = reportForSept7.getInquiry();
        inquiryForSept7.setTargetData(InquiryTargetDataType.ACTIVITY);
        timePeriod = reportForSept7.getTimePeriod();
        begin = Calendar.getInstance();
        begin.set(2003, Calendar.SEPTEMBER, 7);
        begin.set(Calendar.HOUR_OF_DAY, 0);
        begin.set(Calendar.MINUTE, 0);
        begin.set(Calendar.SECOND, 0);
        begin.set(Calendar.MILLISECOND, 0);
        timePeriod.setBeginDate(begin);
        end = Calendar.getInstance();
        end.set(2003, Calendar.SEPTEMBER, 8);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        timePeriod.setEndDate(end);
        try {
            reader = reportExecutionMgr.executeReport(reportForSept7);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                reader.nextResult();
            }
            assertEquals("All records should be pulled properly by the result reader", nbSept7Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbSept7Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }

        //Try lower boundary condition
        begin = end;
        IReport reportForSept8 = reportMgr.createReport();
        IInquiry inquiryForSept8 = reportForSept8.getInquiry();
        inquiryForSept8.setTargetData(InquiryTargetDataType.ACTIVITY);
        timePeriod = reportForSept8.getTimePeriod();
        timePeriod.setBeginDate(begin);
        timePeriod.setEndDate(null);
        try {
            reader = reportExecutionMgr.executeReport(reportForSept8);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                reader.nextResult();
            }
            assertEquals("All records should be pulled properly by the result reader", nbSept8Records, recordCount);
            assertEquals("The reader should provide a correct count of the records", nbSept8Records, reader.getStatistics().getAvailableRowCount().intValue());
            reader.close();
        }
    }

    /**
     * This test verifies that the user and group prefixes are working properly
     */
    public void testReportExecutionTrackingActivityWithUserAndGroupPrefixes() {
        insertUserTrackingActivityRecords();
        IReportMgr reportMgr = getReportMgr();
        IReport userReport = reportMgr.createReport();
        userReport.setSummaryType(ReportSummaryType.NONE);
        userReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        userReport.getInquiry().addUser("(User) Engineering");
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(userReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (reader.hasNextResult()) {
            fail("The reports should not return any results");
        }
        if (reader != null) {
            reader.close();
        }

        IReport groupReport = reportMgr.createReport();
        groupReport.setSummaryType(ReportSummaryType.NONE);
        groupReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        groupReport.getInquiry().addUser("(Group) Engineering");
        try {
            reader = reportExecutionMgr.executeReport(groupReport);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("The reports should return results");
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that the correct reports are returned according to the 
     * default logging level
     */
    public void testReportExecutionTrackingActivityWithDefaultLoggingLevel() {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();

        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        //Make Andy overlap with Marketing group - this should work fine.
        report.getInquiry().setLoggingLevel(0);
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(report);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for logging level query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
            }
            final int expectedNbRecords = NB_IANNIS_RECORDS + NB_SASHA_RECORDS + NB_ANDY_RECORDS + NB_DAVIDL_RECORDS + NB_KENI_RECORDS;
            assertEquals("The logging level query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }
    
    /**
     * This test verifies that the correct reports are returned according to the 
     * default logging level
     */
    public void testReportExecutionTrackingActivityWithMiddleLoggingLevel() throws DataSourceException, InvalidReportArgumentException {
        //First, insert data
        insertUserTrackingActivityRecords();

        //Create report
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();

        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        report.getInquiry().setLoggingLevel(2);
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReportResultReader reader = null;
       
        reader = reportExecutionMgr.executeReport(report);
        assertNotNull("The reader object should not be null", reader);

        //Look at the query results
        if (!reader.hasNextResult()) {
            fail("There should be records returned for mixed user and user group names query");
        } else {
            //Walk through the records and see what is returned
            int recordCount = 0;
            while (reader.hasNextResult()) {
                recordCount++;
                IResultData resultData = reader.nextResult();
                assertNotNull("Result record should exist for logging level query", resultData);
                assertTrue("details should be written when no summary is specified", resultData instanceof IReportTrackingActivityDetailResult);
            }
            final int expectedNbRecords = NB_IANNIS_RECORDS + NB_KENI_RECORDS;
            assertEquals("The logging level query should return the right number of records", expectedNbRecords, recordCount);
            assertEquals("The reader should provide a correct count of the records", expectedNbRecords, reader.getStatistics().getAvailableRowCount().intValue());
        }
        if (reader != null) {
            reader.close();
        }
    }

}
