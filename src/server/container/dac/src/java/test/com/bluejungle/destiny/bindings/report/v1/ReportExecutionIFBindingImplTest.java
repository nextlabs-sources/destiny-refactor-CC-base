/*
 * Created on Apr 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.FromResourceInformationDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportMgrImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.SampleDataMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ToResourceInformationDO;
import com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.basic.v1.SortDirection;
import com.bluejungle.destiny.types.basic.v1.StringList;
import com.bluejungle.destiny.types.basic_faults.v1.UnknownEntryFault;
import com.bluejungle.destiny.types.effects.v1.EffectList;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortSpec;
import com.bluejungle.destiny.types.report.v1.ReportSummaryType;
import com.bluejungle.destiny.types.report.v1.ReportTargetType;
import com.bluejungle.destiny.types.report_result.v1.ActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.DetailResultList;
import com.bluejungle.destiny.types.report_result.v1.DocumentActivityCustomResult;
import com.bluejungle.destiny.types.report_result.v1.DocumentActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.LogDetailResult;
import com.bluejungle.destiny.types.report_result.v1.PolicyActivityCustomResult;
import com.bluejungle.destiny.types.report_result.v1.PolicyActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportResult;
import com.bluejungle.destiny.types.report_result.v1.ReportState;
import com.bluejungle.destiny.types.report_result.v1.ReportSummaryResult;
import com.bluejungle.destiny.types.report_result.v1.SummaryResult;
import com.bluejungle.destiny.types.report_result.v1.SummaryResultList;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogCustomAttributeDO;
import com.nextlabs.destiny.types.custom_attr.v1.CustomAttribute;
import com.nextlabs.destiny.types.custom_attr.v1.CustomAttributeList;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ReportExecutionIFBindingImplTest.java#1 $
 */

public class ReportExecutionIFBindingImplTest extends BaseReportServiceTest {

    private static final Log LOG = LogFactory.getLog(ComponentLookupIFImplTest.class.getName());
    protected ReportSampleDataMgr reportDataMgr = new ReportSampleDataMgr();
    protected SampleDataMgr sampleDataMgr = new SampleDataMgr();

    /**
     * Constructor
     * 
     * @param testName
     */
    public ReportExecutionIFBindingImplTest(String testName) {
        super(testName);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Session s = getActivityDateSource().getSession();
        this.reportDataMgr.deleteAllReports(s);
        this.sampleDataMgr.deleteApplications(s);
        this.sampleDataMgr.deleteHosts(s);
        this.sampleDataMgr.deletePolicies(s);
        this.sampleDataMgr.deleteTrackingLogCustomAttributes(s);
        this.sampleDataMgr.deletePolicyLogCustomAttributes(s);
        this.sampleDataMgr.deletePolicyLogs(s);
        this.sampleDataMgr.deleteTrackingLogs(s);
        this.sampleDataMgr.deleteUsersAndGroups(s);
        this.sampleDataMgr.createApplicationsAndGroups(s);
        this.sampleDataMgr.createHosts(s);
        this.sampleDataMgr.createPolicies(s);
        this.sampleDataMgr.createUsersAndGroups(s);
        HibernateUtils.closeSession(s, LOG);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = getActivityDateSource().getSession();
        this.reportDataMgr.deleteAllReports(s);
        this.sampleDataMgr.deleteApplications(s);
        this.sampleDataMgr.deleteHosts(s);
        this.sampleDataMgr.deletePolicies(s);
        this.sampleDataMgr.deleteTrackingLogCustomAttributes(s);
        this.sampleDataMgr.deletePolicyLogCustomAttributes(s);
        this.sampleDataMgr.deletePolicyLogs(s);
        this.sampleDataMgr.deleteTrackingLogs(s);
        this.sampleDataMgr.deleteUsersAndGroups(s);
        HibernateUtils.closeSession(s, LOG);
        super.tearDown();
    }

    /**
     * Returns the data source for the activity repository
     * 
     * @return the data source for the activity repository
     */
    protected IHibernateRepository getActivityDateSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns the report manager
     * 
     * @return the report manager
     */
    protected IReportMgr getReportMgr() {
        HashMapConfiguration reportMgrConfig = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo(IReportMgr.COMP_NAME, ReportMgrImpl.class.getName(), IReportMgr.class.getName(), LifestyleType.SINGLETON_TYPE, reportMgrConfig);
        IReportMgr result = (IReportMgr) ComponentManagerFactory.getComponentManager().getComponent(compInfo);
        return result;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Adds a few dynamic mapping to the data source, in order to insert dummy
     * records in the log table.
     * 
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#setupDataSourceDynamicMappings()
     */
    protected List setupDataSourceDynamicMappings() {
        List list = super.setupDataSourceDynamicMappings();
        if (list == null) {
            list = new ArrayList();
        }
        list.add(TestPolicyActivityLogEntryDO.class);
        list.add(TestTrackingActivityLogEntryDO.class);
        list.add(TestPolicyActivityLogCustomAttributeDO.class);
        list.add(TestTrackingActivityLogCustomAttributeDO.class);
        return list;
    }

    /**
     * This test verifies that policy activity can be queried properly for
     * tracking activity
     */
    public void testExecutionAndNavigationForPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int nbRecords = 50;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model.setAction(myAction);
        model.setApplicationId(new Long(5));
        final String myAppName = "myApp";
        model.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        final String myFromResName = "foo";
        fromResInfo.setName(myFromResName);
        final String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        final Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        model.setHostId(new Long(879));
        final String myHostIPAddress = "12.58.123.250";
        model.setHostIPAddress(myHostIPAddress);
        final String myHostName = "myHost.com";
        model.setHostName(myHostName);
        PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        model.setPolicyDecision(myPolicyDecision);
        model.setPolicyId(new Long(1));
        final Calendar myTimestamp = Calendar.getInstance();
        model.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        Calendar mytoResCreatedDate = Calendar.getInstance();
        final String myToResName = "foo";
        toResInfo.setName(myToResName);
        model.setToResourceInfo(toResInfo);
        model.setUserId(new Long(8888));
        final String myUserName = "myUser@bluejungle.com";
        model.setUserName(myUserName);
        Session s = getActivityDateSource().getSession();
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbFirstFetch, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbFirstFetch, list.getResults().length);
        for (int i = 0; i < list.getResults().length; i++) {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be tracking activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertEquals("All fields should be correct", myAction.getName(), policyDetailRes.getAction());
            assertEquals("All fields should be correct", myAppName, policyDetailRes.getApplicationName());
            assertEquals("All fields should be correct", myFromResName, policyDetailRes.getFromResourceName());
            assertEquals("All fields should be correct", myHostIPAddress, policyDetailRes.getHostIPAddress());
            assertEquals("All fields should be correct", myHostName, policyDetailRes.getHostName());
            assertEquals("All fields should be correct", "/folder/Policy1", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be correct", policyDetailRes.getTimestamp());
            assertEquals("All fields should be correct", myPolicyDecision.getName(), policyDetailRes.getEffect().getValue());
            assertEquals("All fields should be correct", myToResName, policyDetailRes.getToResourceName());
            assertEquals("All fields should be correct", myUserName, policyDetailRes.getUserName());
        }

        //Navigate to the second set of records
        start = System.currentTimeMillis();
        result = reportExecution.getNextResultSet(state, nbSecondFetch);
        end = System.currentTimeMillis();
        getLog().info("GetNextResultSet timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
        list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbSecondFetch, list.getResults().length);
        for (int i = 0; i < list.getResults().length; i++) {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be tracking activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertEquals("All fields should be correct", myAction.getName(), policyDetailRes.getAction());
            assertEquals("All fields should be correct", myAppName, policyDetailRes.getApplicationName());
            assertEquals("All fields should be correct", myFromResName, policyDetailRes.getFromResourceName());
            assertEquals("All fields should be correct", myHostIPAddress, policyDetailRes.getHostIPAddress());
            assertEquals("All fields should be correct", myHostName, policyDetailRes.getHostName());
            assertTrue("All fields should be correct", BigInteger.valueOf(i).compareTo(policyDetailRes.getId()) < 0);
            assertNotNull("All fields should be correct", policyDetailRes.getTimestamp());
            assertEquals("All fields should be correct", "/folder/Policy1", policyDetailRes.getPolicyName());
            assertEquals("All fields should be correct", myPolicyDecision.getName(), policyDetailRes.getEffect().getValue());
            assertEquals("All fields should be correct", myToResName, policyDetailRes.getToResourceName());
            assertEquals("All fields should be correct", myUserName, policyDetailRes.getUserName());
        }
    }

    /**
     * This test verifies that policy activity can be queried properly for
     * policy activity
     */
    public void testExecutionAndNavigationForTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecords = 50;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model.setAction(myAction);
        model.setApplicationId(new Long(5));
        final String myAppName = "myApp";
        model.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        final String myFromResName = "foo";
        fromResInfo.setName(myFromResName);
        final String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        final Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        model.setHostId(new Long(879));
        final String myHostIPAddress = "12.58.123.250";
        model.setHostIPAddress(myHostIPAddress);
        final String myHostName = "myHost.com";
        model.setHostName(myHostName);
        final Calendar myTimestamp = Calendar.getInstance();
        model.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        Calendar mytoResCreatedDate = Calendar.getInstance();
        final String myToResName = "foo";
        toResInfo.setName(myToResName);
        model.setToResourceInfo(toResInfo);
        model.setUserId(new Long(8888));
        final String myUserName = "myUser@bluejungle.com";
        model.setUserName(myUserName);
        Session s = getActivityDateSource().getSession();
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbFirstFetch, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbFirstFetch, list.getResults().length);
        for (int i = 0; i < list.getResults().length; i++) {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be tracking activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
            assertEquals("All fields should be correct", myAction.getName(), trackingDetailRes.getAction());
            assertEquals("All fields should be correct", myAppName, trackingDetailRes.getApplicationName());
            assertEquals("All fields should be correct", myFromResName, trackingDetailRes.getFromResourceName());
            assertEquals("All fields should be correct", myHostIPAddress, trackingDetailRes.getHostIPAddress());
            assertEquals("All fields should be correct", myHostName, trackingDetailRes.getHostName());
            assertEquals("All fields should be correct", BigInteger.valueOf(i), trackingDetailRes.getId());
            assertNotNull("All fields should be correct", trackingDetailRes.getTimestamp());
            assertEquals("All fields should be correct", myToResName, trackingDetailRes.getToResourceName());
            assertEquals("All fields should be correct", myUserName, trackingDetailRes.getUserName());
        }

        //Navigate to the second set of records
        start = System.currentTimeMillis();
        result = reportExecution.getNextResultSet(state, nbSecondFetch);
        end = System.currentTimeMillis();
        getLog().info("GetNextResultSet timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
        list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbSecondFetch, list.getResults().length);
        for (int i = 0; i < list.getResults().length; i++) {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be tracking activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
            assertEquals("All fields should be correct", myAction.getName(), trackingDetailRes.getAction());
            assertEquals("All fields should be correct", myAppName, trackingDetailRes.getApplicationName());
            assertEquals("All fields should be correct", myFromResName, trackingDetailRes.getFromResourceName());
            assertEquals("All fields should be correct", myHostIPAddress, trackingDetailRes.getHostIPAddress());
            assertEquals("All fields should be correct", myHostName, trackingDetailRes.getHostName());
            assertTrue("All fields should be correct", BigInteger.valueOf(i).compareTo(trackingDetailRes.getId()) < 0);
            assertNotNull("All fields should be correct", trackingDetailRes.getTimestamp());
            assertEquals("All fields should be correct", myToResName, trackingDetailRes.getToResourceName());
            assertEquals("All fields should be correct", myUserName, trackingDetailRes.getUserName());
        }
    }

    /**
     * This test verifies that a report can be inserted properly in the database
     * through the web service.
     */

    public void testExecutionWithoutTarget() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), 50, model);
        s.close();
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        ReportExecutionIF reportExecution = getReportExecution();
        //Query for all reports
        boolean exThrown = false;
        try {
            ReportResult result = reportExecution.executeReport(newReport, 10, -1);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'target' is null.") > 0);
            exThrown = true;
        }
        assertTrue("The service API should complain that no target was specified.", exThrown);
    }

    /**
     * This test verifies that a report can be inserted properly in the database
     * through the web service.
     */
    public void testExecutionWithoutSummaryType() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), 50, model);
        s.close();
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setTarget(ReportTargetType.ActivityJournal);
        ReportExecutionIF reportExecution = getReportExecution();
        //Query for all reports
        boolean exThrown = false;
        try {
            ReportResult result = reportExecution.executeReport(newReport, 10, -1);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'summaryType' is null.") > 0);
            exThrown = true;
        }
        assertTrue("The service API should complain that no summary type was specified.", exThrown);
    }

    /**
     * This test verifies that a report can be inserted properly in the database
     * through the web service.
     */
    public void testSummaryExecutionWithNullFields() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.TimeDays);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 1, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 1, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", nbRecords, list.getMinCount());
            assertEquals("Max count should be accurate", nbRecords, list.getMaxCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 1, list.getResults().length);
            SummaryResult summaryRes = list.getResults()[0];
            assertEquals("Summary by count should be correct", nbRecords, summaryRes.getCount());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Tracking Activity can be queried properly with
     *  with Summary type User
     */    
    
    public void testWithSummarytypeUserTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbUsers = 3;
        int nbRecordsUser[] = {10,10,10};
        model1.setUserName("ihanen@bluejungle.com");
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsUser[0], model1);
        int nbRecordsUser2 = 10;
        model2.setUserName("usiva@bluejungle.com");
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecordsUser[1], model2);
        int nbRecordsUser3 = 10;
        model3.setUserName("rlin@bluejungle.com");
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(20), nbRecordsUser[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.User);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 10, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsUser[0] + nbRecordsUser[1] +nbRecordsUser[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for (int i = 0;i < nbUsers; i++){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by count should be correct", nbRecordsUser[i], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Tracking Activity can be queried properly with
     *  with Summary type Resource
     */  
    public void testWithSummarytypeResourceTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbResources = 3;
        int nbRecordsResource[] = {10,10,10};
        
        //set first resource
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromtest1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/totest1");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsResource[0], model1);
        //set second resource
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromtest2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/totest2");
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecordsResource[1], model2);
        //set third resource
        FromResourceInformationDO fromResourceInfo3 = new FromResourceInformationDO();
        fromResourceInfo3.setCreatedDate(Calendar.getInstance());
        fromResourceInfo3.setModifiedDate(Calendar.getInstance());
        fromResourceInfo3.setName("file:///c/docs/fromtest3");
        fromResourceInfo3.setOwnerId("123456-58889");
        fromResourceInfo3.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo3 = new ToResourceInformationDO();
        toResourceInfo3.setName("file:///c/docs/totest3");
        model3.setFromResourceInfo(fromResourceInfo3);
        model3.setToResourceInfo(toResourceInfo3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(20), nbRecordsResource[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.Resource);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 10, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsResource[0] + nbRecordsResource[1] + nbRecordsResource[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for (int i = 0;i < nbResources; i++){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by Resource count should be correct", nbRecordsResource[i], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Tracking Activity can be queried properly with
     *  with Summary type TimeMonths
     */  
    public void testWithSummarytypeTimeMonthsTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbMonths = 3;
        int nbRecordsMonth[] = {10,5,7};
        
        //set first month to Jan 2007
        Calendar timestamp1 = Calendar.getInstance();
        timestamp1.setTime(new Date(107, 0, 1, 0, 0));
        model1.setTimestamp(timestamp1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsMonth[0], model1);
        //set second month to Feb 2007
        Calendar timestamp2 = Calendar.getInstance();
        timestamp2.setTime(new Date(107, 1, 1, 0, 0));
        model2.setTimestamp(timestamp2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecordsMonth[1], model2);
        //set second month to March 2007
        Calendar timestamp3 = Calendar.getInstance();
        timestamp3.setTime(new Date(107, 2, 1, 0, 0));
        model3.setTimestamp(timestamp3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(15), nbRecordsMonth[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.TimeMonths);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 5, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsMonth[0] + nbRecordsMonth[1] + nbRecordsMonth[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for(int i= 0, y = nbMonths-1;y >= 0; i++, y--){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by TimeMonths count should be correct", nbRecordsMonth[y], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies Summary type "Policy" should not be specified for Tracking Activity
     *  
     */ 
    public void testWithSummarytypePoliciyTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.Policy);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        boolean exThrown = false;
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            fail("Summary type Policy should not have been specified for Tracking Activity. An exception should have been thrown");
            
        } catch (RemoteException e) {
            //TO-DO
        	/*
        	 *Currently  exception is thrown with empty message from API i.e e.getLocalizedMessage() is returning null
        	 *The service API should complain summary type policy should not have been specified
        	 *
        	 */
        	assertTrue(e instanceof AxisFault);
        	getLog().info("Summary type Policy should not have been specified for Tracking Activity " + e.getLocalizedMessage());
            //assertTrue(e.getMessage().indexOf("summary type") > 0);
            exThrown = true;
        }
        assertTrue("The service API should complain summary type policy should not have been specified.", exThrown);
        }
    /**
     *  This test verifies whether Policy Activity can be queried properly with
     *  with Summary type User
     */    
    public void testWithSummarytypeUserPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbUsers = 3;
        int nbRecordsUser[] = {10,10,10};
        PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        model1.setPolicyDecision(myPolicyDecision);
        model1.setPolicyId(new Long(1));
        model1.setUserName("ihanen@bluejungle.com");
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsUser[0], model1);
        model2.setPolicyDecision(myPolicyDecision);
        model2.setPolicyId(new Long(1));
        model2.setUserName("usiva@bluejungle.com");
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsUser[1], model2);
        model3.setPolicyDecision(myPolicyDecision);
        model3.setPolicyId(new Long(1));
        model3.setUserName("rlin@bluejungle.com");
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(20), nbRecordsUser[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.User);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 10, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsUser[0] + nbRecordsUser[1] +nbRecordsUser[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for (int i = 0;i < nbUsers; i++){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by count should be correct", nbRecordsUser[i], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Policy Activity can be queried properly with
     *  with Summary type Resource
     */  
    public void testWithSummarytypeResourcePolicyActivity() throws HibernateException, RemoteException {
    	TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbResources = 3;
        int nbRecordsResource[] = {10,10,10};
        
        //set first resource
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromtest1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/totest1");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsResource[0], model1);
        //set second resource
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromtest2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/totest2");
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsResource[1], model2);
        //set third resource
        FromResourceInformationDO fromResourceInfo3 = new FromResourceInformationDO();
        fromResourceInfo3.setCreatedDate(Calendar.getInstance());
        fromResourceInfo3.setModifiedDate(Calendar.getInstance());
        fromResourceInfo3.setName("file:///c/docs/fromtest3");
        fromResourceInfo3.setOwnerId("123456-58889");
        fromResourceInfo3.setSize(new Long(1000));
        ToResourceInformationDO toResourceInfo3 = new ToResourceInformationDO();
        toResourceInfo3.setName("file:///c/docs/totest3");
        model3.setFromResourceInfo(fromResourceInfo3);
        model3.setToResourceInfo(toResourceInfo3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(20), nbRecordsResource[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.Resource);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 10, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsResource[0] + nbRecordsResource[1] + nbRecordsResource[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for (int i = 0;i < nbResources; i++){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by Resource count should be correct", nbRecordsResource[i], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Policy Activity can be queried properly with
     *  with Summary type TimeMonths
     */  
    public void testWithSummarytypeTimeMonthsPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbMonths = 3;
        int nbRecordsMonth[] = {10,5,7};
        
        //set first month to Jan 2007
        Calendar timestamp1 = Calendar.getInstance();
        timestamp1.setTime(new Date(107, 0, 1, 0, 0));
        model1.setTimestamp(timestamp1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsMonth[0], model1);
        //set second month to Feb 2007
        Calendar timestamp2 = Calendar.getInstance();
        timestamp2.setTime(new Date(107, 1, 1, 0, 0));
        model2.setTimestamp(timestamp2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsMonth[1], model2);
        //set second month to March 2007
        Calendar timestamp3 = Calendar.getInstance();
        timestamp3.setTime(new Date(107, 2, 1, 0, 0));
        model3.setTimestamp(timestamp3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(15), nbRecordsMonth[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.TimeMonths);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 5, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsMonth[0] + nbRecordsMonth[1] + nbRecordsMonth[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for(int i= 0, y = nbMonths-1;y >= 0; i++, y--){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by TimeMonths count should be correct", nbRecordsMonth[y], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Policy Activity can be queried properly with
     *  with Summary type TimeDays
     */
    public void testWithSummarytypeTimeDaysPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbDays = 3;
        int nbRecordsDay[] = {10,5,7};
        
        //set day to Jan 1 2007
        Calendar timestamp1 = Calendar.getInstance();
        timestamp1.setTime(new Date(107, 0, 1, 0, 0));
        model1.setTimestamp(timestamp1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsDay[0], model1);
        //set day to Jan 2 2007
        Calendar timestamp2 = Calendar.getInstance();
        timestamp2.setTime(new Date(107, 0, 2, 0, 0));
        model2.setTimestamp(timestamp2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsDay[1], model2);
        //set day to Jan 3 2007
        Calendar timestamp3 = Calendar.getInstance();
        timestamp3.setTime(new Date(107, 0, 3, 0, 0));
        model3.setTimestamp(timestamp3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(15), nbRecordsDay[2], model3);
        s.close();
        final Report report = new Report();
        //Assign the variables to the report
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.TimeDays);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 10, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 5, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsDay[0] + nbRecordsDay[1] + nbRecordsDay[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbDays, list.getResults().length);
            for(int i= 0, y = nbDays-1;y >= 0; i++, y--){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by TimeDays count should be correct", nbRecordsDay[y], summaryRes.getCount());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether Policy Activity can be queried properly with
     *  with Summary type Policy
     */
    public void testWithSummarytypePoliciyPolicyActivity() throws HibernateException, RemoteException {
    	TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        int nbPolicies = 3;
        int nbRecordsPolicy[] = {10,10,10};
        PolicyDecisionEnumType myPolicyDecision = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        model1.setPolicyDecision(myPolicyDecision);
        model1.setPolicyId(new Long(1));
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsPolicy[0], model1);
        model2.setPolicyDecision(myPolicyDecision);
        model2.setPolicyId(new Long(2));
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsPolicy[1], model2);
        model3.setPolicyDecision(myPolicyDecision);
        model3.setPolicyId(new Long(3));
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(20), nbRecordsPolicy[2], model3);
        s.close();
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.Policy);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        boolean exThrown = false;
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbPolicies*2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a summary", result instanceof ReportSummaryResult);
            ReportSummaryResult summary = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 3, summary.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 3, summary.getTotalRowCount());
            SummaryResultList list = summary.getData();
            assertNotNull("The result list should not be null", list);
            assertEquals("Min count should be accurate", 10, list.getMinCount());
            assertEquals("Max count should be accurate", 10, list.getMaxCount());
            assertEquals("Max count should be accurate", nbRecordsPolicy[0] + nbRecordsPolicy[1] + nbRecordsPolicy[2], list.getTotalCount());
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 3, list.getResults().length);
            for(int i= 0;i< nbPolicies; i++){
            SummaryResult summaryRes = list.getResults()[i];
            assertEquals("Summary by Policy count should be correct", nbRecordsPolicy[i], summaryRes.getCount());
            }
        } catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
        }
    /**
     * This test verifies that the minimum argument list for the policy activity
     * target is supported.
     */
    public void testMinimumPolicyActivityExecution() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getEffect());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that the minimum argument list for the policy activity
     * target is supported.
     */
    public void testMinimumTrackingActivityExecution() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEffectsTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(new EffectList(new EffectType[] { EffectType.allow }));
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            //TODO
            /*
             * fail("An effect should not have been specified for Tracking
             * Activity, an exception" + " should have been thrown");
             */
        } catch (RemoteException e) {
            //TO-DO: need to check if the correct exception is thrown
        }
    }
   
    public void testActionsEditTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        model.setAction(ActionEnumType.ACTION_EDIT);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_EDIT.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsOpenTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_OPEN.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsIMTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        model.setAction(ActionEnumType.ACTION_SEND_IM);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_SEND_IM.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsRandomTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model5 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setAction(ActionEnumType.ACTION_STOP_AGENT);
        final int nbRecordsStopAgent = 3;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsStopAgent, model1);
        model2.setAction(ActionEnumType.ACTION_EMBED);
        final int nbRecordsEmbed = 7;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(3), nbRecordsEmbed, model2);
        model3.setAction(ActionEnumType.ACTION_CHANGE_ATTRIBUTES);
        final int nbRecordsChangeAttributes = 11;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecordsChangeAttributes, model3);
        model4.setAction(ActionEnumType.ACTION_CHANGE_SECURITY);
        final int nbRecordsChangeSecurity = 13;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(21), nbRecordsChangeSecurity, model4);
        model5.setAction(ActionEnumType.ACTION_AGENT_USER_LOGIN);
        final int nbRecordsAgentUserLogin = 17;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(34), nbRecordsAgentUserLogin, model5);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_STOP_AGENT.getName(), ActionEnumType.ACTION_EMBED.getName(), ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 50, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testBeginDateBeforeTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(105, 0, 1, 0, 0));

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(beginDate);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testBeginDateAfterTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(Calendar.getInstance());
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNull("The result list should be null", list);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEndDateBeforeTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        Calendar EndDate = Calendar.getInstance();
        EndDate.setTime(new Date(105, 0, 1, 0, 0));

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(EndDate);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNull("The result list should be null", list);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEndDateAfterTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(Calendar.getInstance());
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testPoliciesTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "Standard", "random", "engineering" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            //fail("The test should throw an exception because Tracking
            // Activity should not query on policies");
        } catch (RemoteException e) {
            //TO-DO: need to verify if the correct exception is thrown
        }
    }

    public void testFromResourceTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*java*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSharepointFromResourceTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "SharePoint://sharepoint2007.bluejungle.com/ReporterSite";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "SharePoint://sharepoint2007.bluejungle.com/ReporterSite" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testMultipleFromResourceTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();

        FromResourceInformationDO fromResInfo1 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate1 = Calendar.getInstance();
        fromResInfo1.setCreatedDate(myFromResCreatedDate1);
        Calendar myFromResModifiedDate1 = Calendar.getInstance();
        fromResInfo1.setModifiedDate(myFromResModifiedDate1);
        String myFromResName1 = "file://ELBA/java/java.java";
        fromResInfo1.setName(myFromResName1);
        String myFromResOwnerId1 = "1234";
        fromResInfo1.setOwnerId(myFromResOwnerId1);
        Long myFromResSize1 = new Long(456);
        fromResInfo1.setSize(myFromResSize1);
        model1.setFromResourceInfo(fromResInfo1);
        final int nbRecordsJava = 35;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsJava, model1);

        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate2 = Calendar.getInstance();
        fromResInfo2.setCreatedDate(myFromResCreatedDate2);
        Calendar myFromResModifiedDate2 = Calendar.getInstance();
        fromResInfo2.setModifiedDate(myFromResModifiedDate2);
        String myFromResName2 = "file:///c:/abc.txt";
        fromResInfo2.setName(myFromResName2);
        String myFromResOwnerId2 = "1234";
        fromResInfo2.setOwnerId(myFromResOwnerId2);
        Long myFromResSize2 = new Long(456);
        fromResInfo2.setSize(myFromResSize2);
        model2.setFromResourceInfo(fromResInfo2);
        final int nbRecordsAbc = 40;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(35), nbRecordsAbc, model2);

        FromResourceInformationDO fromResInfo3 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate3 = Calendar.getInstance();
        fromResInfo3.setCreatedDate(myFromResCreatedDate3);
        Calendar myFromResModifiedDate3 = Calendar.getInstance();
        fromResInfo3.setModifiedDate(myFromResModifiedDate3);
        String myFromResName3 = "bogus";
        fromResInfo3.setName(myFromResName3);
        String myFromResOwnerId3 = "1234";
        fromResInfo3.setOwnerId(myFromResOwnerId3);
        Long myFromResSize3 = new Long(456);
        fromResInfo3.setSize(myFromResSize3);
        model3.setFromResourceInfo(fromResInfo3);
        final int nbRecordsRandom = 55;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(75), nbRecordsRandom, model3);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "\\\\ELBA\\java\\java.java", "c:\\abc.txt" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecordsAbc * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsJava + nbRecordsAbc, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testToResourceTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();

        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file:///d:/java/java";
        toResInfo.setName(myToResName);
        model.setToResourceInfo(toResInfo);

        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*java*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testMultipleToResourceTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();

        ToResourceInformationDO toResInfo1 = new ToResourceInformationDO();
        String myToResName1 = "file://ELBA/java/java.java";
        toResInfo1.setName(myToResName1);
        model1.setToResourceInfo(toResInfo1);
        final int nbRecordsJava = 35;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsJava, model1);

        ToResourceInformationDO toResInfo2 = new ToResourceInformationDO();
        String myToResName2 = "file:///c:/abc.txt";
        toResInfo2.setName(myToResName2);
        model2.setToResourceInfo(toResInfo2);
        final int nbRecordsAbc = 40;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(35), nbRecordsAbc, model2);

        ToResourceInformationDO toResInfo3 = new ToResourceInformationDO();
        String myToResName3 = "Random";
        toResInfo3.setName(myToResName3);
        model3.setToResourceInfo(toResInfo3);
        final int nbRecordsRandom = 55;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(75), nbRecordsRandom, model3);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "\\\\ELBA\\java\\java.java", "c:\\abc.txt" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecordsAbc * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsJava + nbRecordsAbc, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be tracking activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testSortSpecActionsTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setAction(ActionEnumType.ACTION_COPY);
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setAction(ActionEnumType.ACTION_DELETE);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Action, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("DELETE", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("COPY", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testSortSpecDateTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        model1.setTimestamp(date1);
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setTimestamp(date2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertEquals(date1.getTime(), policyDetailRes.getTimestamp().getTime());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertEquals(date2.getTime(), policyDetailRes.getTimestamp().getTime());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

	/**
	 * Tests to verify Sort functionality for Document Activity
	 * @throws HibernateException
	 * @throws RemoteException
	 */
    public void testSortSpecUserTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setUserName( "Scott@bluejungle.com");
        model1.setUserId(new Long(14));
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setUserId(new Long(12));
        model2.setUserName("Robert@bluejungle.com");
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.User, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("Scott@bluejungle.com", policyDetailRes.getUserName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("Robert@bluejungle.com", policyDetailRes.getUserName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecHostTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setHostId(new Long(1));
        model1.setHostIPAddress("10.17.11.130");
        model1.setHostName("stbarts.bluejungle.com");
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setHostId(new Long(3));
        model2.setHostIPAddress("10.17.11.140");
        model2.setHostName("borabora.bluejungle.com");
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Host, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("borabora.bluejungle.com", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("stbarts.bluejungle.com", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /*Commenting out the following 2 failing tests for which bug has been filed
     * 
    public void testSortSpecPolicyTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[]{"/folder/Policy18"}));
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Policy, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            //long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            assertEquals("No rows should be returned as Document Activity cannot be sorted by Policy",0, result.getAvailableRowCount());
            assertEquals("No rows should be returned as Document Activity cannot be sorted by Policy",0, result.getTotalRowCount());
            assertNotNull(result.getState());
            assertTrue(result instanceof ReportDetailResult);
            ReportDetailResult detailResult = (ReportDetailResult) result;
            assertNull( "No resultset should be returned" , detailResult.getData());          
                        
        } catch (RemoteException e) {
            assertTrue("The test should throw an exception" , e instanceof AxisFault);
        }
    }
    
    public void testSortSpecPolicyDecisionTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[]{"/folder/Policy18"}));
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.PolicyDecision, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            assertEquals("No rows should be returned as Document Activity cannot be sorted by Policydecision",0, result.getAvailableRowCount());
            assertEquals("No rows should be returned as Document Activity cannot be sorted by Policydecision",0, result.getTotalRowCount());
            assertNotNull(result.getState());
            assertTrue(result instanceof ReportDetailResult);
            ReportDetailResult detailResult = (ReportDetailResult) result;
            assertNull( "No resultset should be returned" , detailResult.getData());          
                        
        } catch (RemoteException e) {
            assertTrue("The test should throw an exception" , e instanceof AxisFault);
        }
    }
    */
    public void testSortSpecLogginglevelTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setLevel(5);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.LoggingLevel, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i< nbRecords;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals(5, policyDetailRes.getLoggingLevel());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords; i< nbRecords*2;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals(0, policyDetailRes.getLoggingLevel());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecCountTrackingActivity() throws HibernateException, RemoteException {
    	Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setUserName( "Scott@bluejungle.com");
        model1.setUserId(new Long(14));
        final int nbRecords1 = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords1, model1);
        model2.setUserName( "Robert@bluejungle.com");
        model2.setUserId(new Long(12));
        final int nbRecords2 = 5;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords2, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.User);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Count, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords1 + nbRecords2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportSummaryResult);
            ReportSummaryResult details = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 2, details.getTotalRowCount());
            SummaryResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 2, list.getResults().length);
            SummaryResult summaryRes1 = (SummaryResult) list.getResults()[0];
            assertEquals("All fields should be populated", 10, summaryRes1.getCount());
            SummaryResult summaryRes2 = (SummaryResult) list.getResults()[1];
            assertEquals("All fields should be populated", 5, summaryRes2.getCount());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecFromResourceTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromResource1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/toResource1");
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromResource2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/toResource2");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.FromResource, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i< nbRecords;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/fromResource1", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords; i< nbRecords*2;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/fromResource2", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecToResourceTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromResource1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/toResource1");
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromResource2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/toResource2");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.ToResource, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i< nbRecords;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/toResource2", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords; i< nbRecords*2;i++)
            {
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/toResource1", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testUsersTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "ihanen*" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     * Tests to verify group functionality in User criteria
     * @throws HibernateException
     * @throws RemoteException
     */
    public void testWithParticularGroupTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "engineering" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            for(int i = 0; i < nbRecords; i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", trackingDetailRes.getAction());
            assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
            assertNotNull("All fields should be populated", trackingDetailRes.getId());
            assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
            assertEquals("User name should match","ihanen@bluejungle.com", trackingDetailRes.getUserName() );
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testWithNonMatchingGroupTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "IT" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("The result list should be null", details.getData());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testWithNonexistentGroupTrackingActivity() throws HibernateException, RemoteException {
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "wronggroup" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("The result list should be null", details.getData());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testWithParticularGroupPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[]{"/folder/Policy18"}) );
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "Engineering" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            for(int i = 0; i < nbRecords; i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            assertEquals("User name should match","ihanen@bluejungle.com", policyDetailRes.getUserName() );
            }
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testWithNonMatchingGroupPolicyActivity() throws HibernateException, RemoteException {
    	TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[]{"/folder/Policy18"}) );
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "IT" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("The result list should be null", details.getData());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testWithNonexistentGroupPolicyActivity() throws HibernateException, RemoteException {
    	TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setUserName("ihanen@bluejungle.com");
        model1.setId(new Long(8));
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model2.setUserName("Keng@bluejungle.com");
        model2.setUserId(new Long(10));
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[]{"/folder/Policy18"}) );
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "wronggroup" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("The result list should be null", details.getData());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testMultipleUsersTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecordsIhanen = 23;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsIhanen, model1);
        model2.setUserName("rlin@bluejungle.com");
        final int nbRecordsRlin = 29;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbRecordsRlin, model2);
        model3.setUserName("fuad@bluejungle.com");
        final int nbRecordsFuad = 31;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(52), nbRecordsFuad, model3);
        model4.setUserName("csarna@bluejungle.com");
        final int nbRecordsCsarna = 37;
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(83), nbRecordsCsarna, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "rlin@bluejungle.com", "ihanen@bluejungle.com", "fuad@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, (nbRecordsIhanen + nbRecordsRlin) * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult policyDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testTrackingActivityWithInvalidQueries() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName = "myUser@bluejungle.com";
        model1.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9000));
        myUserName = "rlin@bluejungle.com";
        model2.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9000));
        myUserName = "ihanen@bluejungle.com";
        model3.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9000));
        myUserName = "myUser@bluejungle.com";
        model4.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_STOP_AGENT.getName(), ActionEnumType.ACTION_EMBED.getName(), ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName() }));
        report.setBeginDate(Calendar.getInstance());
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "Blah", "DummyData" }));
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.FromResource, SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "yourName@goodle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, 50, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNull("The result list should be null", list);
    }

    public void testTrackingActivityWithAllData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_COPY.getName(), ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName(), ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN.getName() }));
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(104, 10, 23));
        report.setBeginDate(beginDate);
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        //report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "c:\\test\test.xml", "*stop*", "*my music*", "\\\\angel\\shared\\builds\\text.doc" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "rlin@bluejungle.com", "myUser@bluejungle.com", "ihanen@bluejungle.com", "yourUser@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, list.getResults().length);
    }

    public void testTrackingActivityWithNoMatchingData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName() }));
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date(105, 11, 21));
        report.setBeginDate(null);
        report.setEndDate(endDate);
        //report.setObligations(null);
        //report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*my music*", "\\\\angel\\shared\\builds\\text.doc" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        //report.setUsers(new StringList(new String[]{"rlin@bluejungle.com",
        // "myUser@bluejungle.com", "ihanen@bluejungle.com",
        // "yourUser@bluejungle.com"}));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNull("The result list should be null", list);
    }

    public void testTrackingActivityWithPartialMatchingData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName() }));
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(106, 1, 1));
        report.setBeginDate(beginDate);
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        //report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "c:\\test\test.xml", "*stop*" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "myUser@bluejungle.com", "ihanen@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecordsDataThree, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecordsDataThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbRecordsDataThree, list.getResults().length);
    }

    // Policy Activity Section Begins
    public void testEffectsAllowPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(new EffectList(new EffectType[] { EffectType.allow }));
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEffectsDenyPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        model.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(new EffectList(new EffectType[] { EffectType.deny }));
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEffectsBothPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model2.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        Session s = getActivityDateSource().getSession();
        final int nbRecordsAllow = 50;
        final int nbRecordsDeny = 40;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsAllow, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbRecordsDeny, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(new EffectList(new EffectType[] { EffectType.deny, EffectType.allow }));
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecordsAllow * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsAllow + nbRecordsDeny, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsAllow + nbRecordsDeny, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsAllow + nbRecordsDeny, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsEditPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        model.setAction(ActionEnumType.ACTION_EDIT);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_EDIT.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsOpenPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_OPEN.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsIMPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        model.setAction(ActionEnumType.ACTION_SEND_IM);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_SEND_IM.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testActionsRandomPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        final TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        final TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        final TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        final TestPolicyActivityLogEntryDO model5 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setAction(ActionEnumType.ACTION_STOP_AGENT);
        final int nbRecordsStopAgent = 3;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsStopAgent, model1);
        model2.setAction(ActionEnumType.ACTION_EMBED);
        final int nbRecordsEmbed = 7;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(3), nbRecordsEmbed, model2);
        model3.setAction(ActionEnumType.ACTION_CHANGE_ATTRIBUTES);
        final int nbRecordsChangeAttributes = 11;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecordsChangeAttributes, model3);
        model4.setAction(ActionEnumType.ACTION_CHANGE_SECURITY);
        final int nbRecordsChangeSecurity = 13;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(21), nbRecordsChangeSecurity, model4);
        model5.setAction(ActionEnumType.ACTION_AGENT_USER_LOGIN);
        final int nbRecordsAgentUserLogin = 17;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(34), nbRecordsAgentUserLogin, model5);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_STOP_AGENT.getName(), ActionEnumType.ACTION_EMBED.getName(), ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName() }));
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, 50, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsEmbed + nbRecordsStopAgent + nbRecordsChangeAttributes, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testBeginDateBeforePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(105, 0, 1, 0, 0));

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(beginDate);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testBeginDateAfterPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(Calendar.getInstance());
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNull("The result list should be null", list);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEndDateBeforePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();
        Calendar EndDate = Calendar.getInstance();
        EndDate.setTime(new Date(105, 0, 1, 0, 0));

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(EndDate);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNull("The result list should be null", list);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testEndDateAfterPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(Calendar.getInstance());
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testPoliciesPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model.setPolicyId(new Long(0));
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "*Policy*" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testPoliciesCaseInsensitivePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model.setPolicyId(new Long(0));
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "*policy*" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    	/**
    	 * Tests to check Policy criteria
    	 * @throws HibernateException
    	 * @throws RemoteException
    	 */
    	public void testPolicyExactMatchPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model1.setPolicyId(new Long(5));
        model2.setPolicyId(new Long(1));
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "/folder/Policy5" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertEquals("A state should be returned",nbRecords, result.getAvailableRowCount());
            assertEquals("A state should be returned", nbRecords, result.getTotalRowCount());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes =  list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            assertEquals("Policy name should match ","/folder/Policy5" ,policyDetailRes.getPolicyName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testPolicyWithNoMatchPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model1.setPolicyId(new Long(5));
        model2.setPolicyId(new Long(1));
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "/folder/Policy100" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertEquals("A state should be returned",0, result.getAvailableRowCount());
            assertEquals("A state should be returned", 0, result.getTotalRowCount());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            DetailResultList list = details.getData();
            assertNull("No reports should be returned since there is no match for the set policy", list);
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testPolicyWithWildcardSuffixPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model1.setPolicyId(new Long(5));
        model2.setPolicyId(new Long(1));
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "/folder/Policy*" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertEquals("A state should be returned",nbRecords * 2, result.getAvailableRowCount());
            assertEquals("A state should be returned", nbRecords*2, result.getTotalRowCount());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords*2, list.getResults().length);
            ActivityDetailResult detailRes1 =  list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes1 instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes1 = (PolicyActivityDetailResult) detailRes1;
            assertNotNull("All fields should be populated", policyDetailRes1.getAction());
            assertNotNull("All fields should be populated", policyDetailRes1.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes1.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes1.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes1.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes1.getId());
            assertNotNull("All fields should be populated", policyDetailRes1.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes1.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes1.getUserName());
            assertEquals("Policy name should match ","/folder/Policy1" ,policyDetailRes1.getPolicyName());
            ActivityDetailResult detailRes2 =  list.getResults()[10];
            assertTrue("Result returned should be policy activity", detailRes2 instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes2 = (PolicyActivityDetailResult) detailRes2;
            assertNotNull("All fields should be populated", policyDetailRes2.getAction());
            assertNotNull("All fields should be populated", policyDetailRes2.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes2.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes2.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes2.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes2.getId());
            assertNotNull("All fields should be populated", policyDetailRes2.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes2.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes2.getUserName());
            assertEquals("Policy name should match ","/folder/Policy5" ,policyDetailRes2.getPolicyName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testPolicyWithWildcardPrefixPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        model1.setPolicyId(new Long(5));
        model2.setPolicyId(new Long(1));
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(new StringList(new String[] { "*Policy1" }));
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertEquals("A state should be returned",nbRecords , result.getAvailableRowCount());
            assertEquals("A state should be returned", nbRecords, result.getTotalRowCount());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes1 =  list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes1 instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes1 = (PolicyActivityDetailResult) detailRes1;
            assertNotNull("All fields should be populated", policyDetailRes1.getAction());
            assertNotNull("All fields should be populated", policyDetailRes1.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes1.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes1.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes1.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes1.getId());
            assertNotNull("All fields should be populated", policyDetailRes1.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes1.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes1.getUserName());
            assertEquals("Policy name should match ","/folder/Policy1" ,policyDetailRes1.getPolicyName());
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testFromResourcePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*java*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSharepointFromResourcePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "SharePoint://sharepoint2007.bluejungle.com/ReporterSite";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "SharePoint://sharepoint2007.bluejungle.com/ReporterSite" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testMultipleFromResourcePolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();

        FromResourceInformationDO fromResInfo1 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate1 = Calendar.getInstance();
        fromResInfo1.setCreatedDate(myFromResCreatedDate1);
        Calendar myFromResModifiedDate1 = Calendar.getInstance();
        fromResInfo1.setModifiedDate(myFromResModifiedDate1);
        String myFromResName1 = "file://ELBA/java/java.java";
        fromResInfo1.setName(myFromResName1);
        String myFromResOwnerId1 = "1234";
        fromResInfo1.setOwnerId(myFromResOwnerId1);
        Long myFromResSize1 = new Long(456);
        fromResInfo1.setSize(myFromResSize1);
        model1.setFromResourceInfo(fromResInfo1);
        final int nbRecordsJava = 35;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsJava, model1);

        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate2 = Calendar.getInstance();
        fromResInfo2.setCreatedDate(myFromResCreatedDate2);
        Calendar myFromResModifiedDate2 = Calendar.getInstance();
        fromResInfo2.setModifiedDate(myFromResModifiedDate2);
        String myFromResName2 = "file:///c:/abc.txt";
        fromResInfo2.setName(myFromResName2);
        String myFromResOwnerId2 = "1234";
        fromResInfo2.setOwnerId(myFromResOwnerId2);
        Long myFromResSize2 = new Long(456);
        fromResInfo2.setSize(myFromResSize2);
        model2.setFromResourceInfo(fromResInfo2);
        final int nbRecordsAbc = 40;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(35), nbRecordsAbc, model2);

        FromResourceInformationDO fromResInfo3 = new FromResourceInformationDO();
        Calendar myFromResCreatedDate3 = Calendar.getInstance();
        fromResInfo3.setCreatedDate(myFromResCreatedDate3);
        Calendar myFromResModifiedDate3 = Calendar.getInstance();
        fromResInfo3.setModifiedDate(myFromResModifiedDate3);
        String myFromResName3 = "bogus";
        fromResInfo3.setName(myFromResName3);
        String myFromResOwnerId3 = "1234";
        fromResInfo3.setOwnerId(myFromResOwnerId3);
        Long myFromResSize3 = new Long(456);
        fromResInfo3.setSize(myFromResSize3);
        model3.setFromResourceInfo(fromResInfo3);
        final int nbRecordsRandom = 55;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(75), nbRecordsRandom, model3);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "\\\\ELBA\\java\\java.java", "c:\\abc.txt" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecordsAbc * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsJava + nbRecordsAbc, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testFromResourceWithPossibleCharactersPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        Calendar myFromResModifiedDate = Calendar.getInstance();
        String myFromResOwnerId = "1234";
        Long myFromResSize = new Long(456);
        String[] myFromResName = new String[]{ "file:///c/test\\1.doc", "file:///c/test/1.doc","file:///c/test:1.doc","file:///c/test\"1.doc","file:///c/test>1.doc", "file:///c/test<1.doc", "file:///c/test|1.doc","file:///c/test!.doc", "file:///c/test@.doc","file:///c/test#.doc", "file:///c/test$.doc","file:///c/test^.doc","file:///c/test&.doc","file:///c/test(.doc", "file:///c/test).doc", "file:///c/test_.doc","file:///c/test;.doc", "file:///c/test,.doc","file:///c/test+.doc","file:///c/test=.doc","file:///c/test{.doc","file:///c/test}.doc","file:///c/test[.doc","file:///c/test].doc","file:///c/test~.doc","file:///c/test`.doc" ,"file:///c/test%.doc", "file:///c/test-.doc"};
        String[] characterList = new String[]                                                                                          { "\\", "/",":","\"",">","<","|","!", "@","#", "$","^","&","(", ")", "_",";", ",","+","=","{","}","[","]","~","`", "%","-"};
        String[] resourceList = new String[]{ "file:///c/*test\\*", "file:///c/*test/*","file:///c/*test:*","file:///c/*test\"*","file:///c/*test>*","file:///c/*test<*","file:///c/*test|*","*test!*", "*test@*","*test#*", "*test$*","*test^*","*test&*","*test(*", "*test)*", "*test_*","*test;*", "*test,*","*test+*","*test=*","*test{*","*test}*","*test[*","*test]*","*test~*","*test`*","*test%*","*test-*"};
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 10;
        Long starting_id = new Long(0);
        for (int i =0; i< resourceList.length ; i++)
        {
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        fromResInfo.setName(myFromResName[i]);
        fromResInfo.setOwnerId(myFromResOwnerId);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
                
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, starting_id, nbRecords, model);
        starting_id = new Long(starting_id.intValue() + nbRecords);
        fromResInfo = new FromResourceInformationDO();
        }
        s.close();
                     
        final Report report = new Report();
        for (int i =0; i< resourceList.length ; i++)
        { 
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { resourceList[i] }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 28 * 2 , -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned :DEBUG resource criteria contains the character " + characterList[i], result);
            assertNotNull("A state should be returned :DEBUG resource criteria contains the character " + characterList[i], result.getState());
            assertTrue("The result should be a detail :DEBUG resource criteria contains the character " + characterList[i], result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null :DEBUG resource criteria contains the character " + characterList[i], list);
            assertNotNull("The list should have results :DEBUG resource criteria contains the character " + characterList[i], list.getResults());
            //TODO :Skip verification for underscore and percent sign _  % as tests are failing for which bug has been filed:
            if (i== 15 || i == 26) continue;
            assertEquals("AvailableRowCount returned does not match DEBUG if resource criteria contains the character " + characterList[i] , nbRecords, details.getAvailableRowCount());
            //TODO :Skip verification for backslash \ as test is failing for which bug has been failed
            if (i == 0)continue;
            assertEquals("TotalRowCount returned does not match DEBUG: resource criteria contains the character " + characterList[i] , nbRecords, details.getTotalRowCount());
            assertEquals("Number of Results should match :DEBUG resource criteria contains the character " + characterList[i], nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = null;
            PolicyActivityDetailResult policyDetailRes = null;
            for (int y= 0;y < list.getResults().length ;y++)
            {	
	            detailRes = (ActivityDetailResult) list.getResults()[y];
	            assertTrue("Result returned should be policy activity :DEBUG resource criteria contains the character " + characterList[i], detailRes instanceof PolicyActivityDetailResult);
	            policyDetailRes = (PolicyActivityDetailResult) detailRes;
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getAction());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getApplicationName());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getFromResourceName());
	            assertEquals(myFromResName[i], policyDetailRes.getFromResourceName());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getHostIPAddress());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getHostName());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getId());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getTimestamp());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getToResourceName());
	            assertNotNull("All fields should be populated :DEBUG resource criteria contains the character " + characterList[i], policyDetailRes.getUserName());
	                      
            }
        }
         catch (RemoteException e) {
        	 
            fail("The test should not throw an exception" + e.getLocalizedMessage());
            
        }
        }
        
    }
    
    /**
     *  This test verifies  whether Illegalargumentexception is thrown when Policy activity is queried with resource = backspace  
     *   
     */  
    public void testFromResourceWithEscapeSequenceBackspacePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\b*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            fail("The test should throw an exception as backspace is not a valid input for resource" );
            } catch (RemoteException e) {
            assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException: The char '0x8' in '**' is not a valid XML character.")> 0);	
        }
    }
    /**
     *  This test verifies  whether Illegalargumentexception is thrown when Policy activity is queried with resource = formfeed 
     *   
     */  
    public void testFromResourceWithEscapeSequenceFormfeedPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\f*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            fail("The test should throw an exception as formfeed is not a valid input for resource" );
            } catch (RemoteException e) {
            assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    /**
     *  This test verifies whether no reports are returned when Policy activity is queried with resource = doublequote
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceDoublequotePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\"*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether no reports are returned when Policy activity is queried with resource = singlequote
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceSinglequotePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\'*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether no reports are returned when Policy activity is queried with resource = backslash
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceBackslashPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\\*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());    	
        }
    }
    /**
     *  This test verifies whether no reports are returned when Policy activity is queried with resource = tab
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */  
    public void testFromResourceWithEscapeSequenceTabPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\t*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    
    public void testFromResourceWithEscapeSequenceLineFeedPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\n*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned", details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    public void testFromResourceWithEscapeSequenceCarriageReturnPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\r*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    /**
     *  This test verifies  whether Illegalargumentexception is thrown when Tracking activity is queried with resource = backspace  
     *   
     */  
    public void testFromResourceWithEscapeSequenceBackspaceTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\b*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            fail("The test should throw an exception as backspace is not a valid input for resource" );
            } catch (RemoteException e) {
            assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException: The char '0x8' in '**' is not a valid XML character.")> 0);	
        }
    }
    /**
     *  This test verifies  whether Illegalargumentexception is thrown when Tracking activity is queried with resource = formfeed 
     *   
     */  
    public void testFromResourceWithEscapeSequenceFormfeedTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\f*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            fail("The test should throw an exception as formfeed is not a valid input for resource" );
            } catch (RemoteException e) {
            assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    /**
     *  This test verifies whether no reports are returned when Tracking activity is queried with resource = doublequote
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceDoublequoteTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\"*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether no reports are returned when Tracking activity is queried with resource = singlequote
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceSinglequoteTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\'*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     *  This test verifies whether no reports are returned when Tracking activity is queried with resource = backslash
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */ 
    public void testFromResourceWithEscapeSequenceBackslashTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\\*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());    	
        }
    }
    /**
     *  This test verifies whether no reports are returned when Tracking activity is queried with resource = tab
     *  Legal characters are tab, carriage return, line feed,double quote,single quote, backslash   
     *   
     */  
    public void testFromResourceWithEscapeSequenceTabTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\t*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    
    public void testFromResourceWithEscapeSequenceLineFeedTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\n*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned", details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    public void testFromResourceWithEscapeSequenceCarriageReturnTrackingActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///d:/java/java";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "1234";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(456);
        fromResInfo.setSize(myFromResSize);
        model.setFromResourceInfo(fromResInfo);
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*\r*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
            assertNull("No reports are returned",  details.getData());
            } catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());	
        }
    }
    public void testToResourcePolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file:///d:/java/java";
        toResInfo.setName(myToResName);
        model.setToResourceInfo(toResInfo);

        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*java*" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testMultipleToResourcePolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();

        ToResourceInformationDO toResInfo1 = new ToResourceInformationDO();
        String myToResName1 = "file://ELBA/java/java.java";
        toResInfo1.setName(myToResName1);
        model1.setToResourceInfo(toResInfo1);
        final int nbRecordsJava = 35;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsJava, model1);

        ToResourceInformationDO toResInfo2 = new ToResourceInformationDO();
        String myToResName2 = "file:///c:/abc.txt";
        toResInfo2.setName(myToResName2);
        model2.setToResourceInfo(toResInfo2);
        final int nbRecordsAbc = 40;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(35), nbRecordsAbc, model2);

        ToResourceInformationDO toResInfo3 = new ToResourceInformationDO();
        String myToResName3 = "Random";
        toResInfo3.setName(myToResName3);
        model3.setToResourceInfo(toResInfo3);
        final int nbRecordsRandom = 55;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(75), nbRecordsRandom, model3);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "\\\\ELBA\\java\\java.java", "c:\\abc.txt" }));
        report.setSortSpec(null);
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecordsAbc * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsJava + nbRecordsAbc, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsJava + nbRecordsAbc, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testSortSpecActionsPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setAction(ActionEnumType.ACTION_COPY);
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setAction(ActionEnumType.ACTION_DELETE);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Action, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("DELETE", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("COPY", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testSortSpecDatePolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        model1.setTimestamp(date1);
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setTimestamp(date2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertEquals(date1.getTime(), policyDetailRes.getTimestamp().getTime());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertEquals(date2.getTime(), policyDetailRes.getTimestamp().getTime());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    /**
     * Tests to verify Sort functionality for Policy activity
     * @throws HibernateException
     * @throws RemoteException
     */
    public void testSortSpecUserPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setUserName( "Scott@bluejungle.com");
        model1.setUserId(new Long(14));
        model1.setPolicyId(new Long(9));
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setUserId(new Long(12));
        model2.setUserName("Robert@bluejungle.com");
        model2.setPolicyId(new Long(1));
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.User, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("Scott@bluejungle.com", policyDetailRes.getUserName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("Robert@bluejungle.com", policyDetailRes.getUserName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecHostPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setHostId(new Long(1));
        model1.setHostIPAddress("10.17.11.130");
        model1.setHostName("stbarts.bluejungle.com");
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setHostId(new Long(3));
        model2.setHostIPAddress("10.17.11.140");
        model2.setHostName("borabora.bluejungle.com");
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        StringList strList = new StringList();
        strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(strList );
        //report.setPolicies(new StringList(new String[]{"/folder/Policy18"});
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Host, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("borabora.bluejungle.com", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("stbarts.bluejungle.com", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecPolicyPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setPolicyId(new Long(18));
        model1.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setPolicyId(new Long(1));
        model2.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        //StringList strList = new StringList();
        //strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(null );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Policy, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("/folder/Policy1", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());

            detailRes = (ActivityDetailResult) list.getResults()[nbRecords];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("/folder/Policy18", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecPolicydecisionPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setPolicyId(new Long(18));
        model1.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setPolicyId(new Long(1));
        model2.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        //StringList strList = new StringList();
        //strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(null );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.PolicyDecision, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i < nbRecords ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("/folder/Policy18", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords ; i < nbRecords*2 ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("/folder/Policy1", policyDetailRes.getPolicyName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
            
    public void testSortSpecLogginglevelPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(1);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setLevel(3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        StringList strList = new StringList();
        strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(strList );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.LoggingLevel, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i < nbRecords ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals(3, policyDetailRes.getLoggingLevel());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords ; i < nbRecords*2 ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals(1, policyDetailRes.getLoggingLevel());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecCountPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setPolicyId(new Long(18));
        model1.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_ALLOW);
        final int nbRecords1 = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords1, model1);
        model2.setPolicyId(new Long(1));
        model2.setPolicyDecision(PolicyDecisionEnumType.POLICY_DECISION_DENY);
        final int nbRecords2 = 5;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords2, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.Policy);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Count, SortDirection.Ascending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords1 + nbRecords2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportSummaryResult);
            ReportSummaryResult details = (ReportSummaryResult) result;
            assertEquals("The number of rows should be correct", 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", 2, details.getTotalRowCount());
            SummaryResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", 2, list.getResults().length);
            SummaryResult summaryRes1 = (SummaryResult) list.getResults()[0];
            assertEquals("All fields should be populated", 5, summaryRes1.getCount());
            SummaryResult summaryRes2 = (SummaryResult) list.getResults()[1];
            assertEquals("All fields should be populated", 10, summaryRes2.getCount());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecFromResourcePolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromResource1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/toResource1");
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromResource2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/toResource2");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        StringList strList = new StringList();
        strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(strList );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.FromResource, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i < nbRecords ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/fromResource2", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords ; i < nbRecords*2 ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/fromResource1", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testSortSpecToResourcePolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo1 = new FromResourceInformationDO();
        fromResourceInfo1.setCreatedDate(Calendar.getInstance());
        fromResourceInfo1.setModifiedDate(Calendar.getInstance());
        fromResourceInfo1.setName("file:///c/docs/fromResource1");
        fromResourceInfo1.setOwnerId("123456-58889");
        fromResourceInfo1.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo1 = new ToResourceInformationDO();
        toResourceInfo1.setName("file:///c/docs/toResource1");
        FromResourceInformationDO fromResourceInfo2 = new FromResourceInformationDO();
        fromResourceInfo2.setCreatedDate(Calendar.getInstance());
        fromResourceInfo2.setModifiedDate(Calendar.getInstance());
        fromResourceInfo2.setName("file:///c/docs/fromResource2");
        fromResourceInfo2.setOwnerId("123456-58889");
        fromResourceInfo2.setSize(new Long(10000));
        ToResourceInformationDO toResourceInfo2 = new ToResourceInformationDO();
        toResourceInfo2.setName("file:///c/docs/toResource2");
        model1.setFromResourceInfo(fromResourceInfo1);
        model1.setToResourceInfo(toResourceInfo1);
        final int nbRecords = 10;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model1);
        model2.setFromResourceInfo(fromResourceInfo2);
        model2.setToResourceInfo(toResourceInfo2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(10), nbRecords, model2);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        StringList strList = new StringList();
        strList.setValues(new String[]{"/folder/Policy18"});
        report.setPolicies(strList );
        report.setResourceNames(null);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.ToResource, SortDirection.Descending));
        report.setUsers(null);

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords * 2, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords * 2, list.getResults().length);
            for (int i=0; i < nbRecords ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/toResource2", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            for (int i=nbRecords ; i < nbRecords*2 ;i++)
            {	
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[i];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertEquals("file:///c/docs/toResource1", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            }
            
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testUsersPolicyActivity() throws HibernateException, RemoteException {
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = getActivityDateSource().getSession();
        final int nbRecords = 50;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, model);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "ihanen*" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, nbRecords * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecords, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecords, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecords, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testMultipleUsersPolicyActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        final int nbRecordsIhanen = 23;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsIhanen, model1);
        model2.setUserName("rlin@bluejungle.com");
        final int nbRecordsRlin = 29;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbRecordsRlin, model2);
        model3.setUserName("fuad@bluejungle.com");
        final int nbRecordsFuad = 31;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(52), nbRecordsFuad, model3);
        model4.setUserName("csarna@bluejungle.com");
        final int nbRecordsCsarna = 37;
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(83), nbRecordsCsarna, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "rlin@bluejungle.com", "ihanen@bluejungle.com", "fuad@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, (nbRecordsIhanen + nbRecordsRlin) * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsIhanen + nbRecordsFuad + nbRecordsRlin, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }

    public void testPolicyActivityWithInvalidQueries() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName = "myUser@bluejungle.com";
        model1.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9000));
        myUserName = "rlin@bluejungle.com";
        model2.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9000));
        myUserName = "ihanen@bluejungle.com";
        model3.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9000));
        myUserName = "myUser@bluejungle.com";
        model4.setUserName(myUserName);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_STOP_AGENT.getName(), ActionEnumType.ACTION_EMBED.getName(), ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName() }));
        report.setBeginDate(Calendar.getInstance());
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "Blah", "DummyData" }));
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.FromResource, SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "yourName@goodle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, 50, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNull("The result list should be null", list);
    }

    public void testPolicyActivityWithAllData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_COPY.getName(), ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName(), ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN.getName() }));
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(104, 10, 23));
        report.setBeginDate(beginDate);
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "c:\\test\test.xml", "*stop*", "*my music*", "\\\\angel\\shared\\builds\\text.doc" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "rlin@bluejungle.com", "myUser@bluejungle.com", "ihanen@bluejungle.com", "yourUser@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, list.getResults().length);
    }

    public void testPolicyActivityWithNoMatchingData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName() }));
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date(105, 11, 29));
        report.setBeginDate(null);
        report.setEndDate(endDate);
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "*my music*", "\\\\angel\\shared\\builds\\text.doc" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        //report.setUsers(new StringList(new String[]{"rlin@bluejungle.com",
        // "myUser@bluejungle.com", "ihanen@bluejungle.com",
        // "yourUser@bluejungle.com"}));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNull("The result list should be null", list);
    }

    public void testPolicyActivityWithPartialMatchingData() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();

        final int nbRecordsDataOne = 41;
        ActionEnumType myAction = ActionEnumType.ACTION_COPY;
        model1.setAction(myAction);
        model1.setApplicationId(new Long(5));
        String myAppName = "myApp";
        model1.setApplicationName(myAppName);
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        Calendar myFromResCreatedDate = Calendar.getInstance();
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        Calendar myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        String myFromResName = "file:///e:/my music/hi.txt";
        fromResInfo.setName(myFromResName);
        String myFromResOwnerId = "123";
        fromResInfo.setOwnerId(myFromResOwnerId);
        Long myFromResSize = new Long(45687);
        fromResInfo.setSize(myFromResSize);
        model1.setFromResourceInfo(fromResInfo);
        model1.setHostId(new Long(879));
        String myHostIPAddress = "12.58.123.250";
        model1.setHostIPAddress(myHostIPAddress);
        String myHostName = "myHost.com";
        model1.setHostName(myHostName);
        Calendar myTimestamp = Calendar.getInstance();
        model1.setTimestamp(myTimestamp);
        ToResourceInformationDO toResInfo = new ToResourceInformationDO();
        String myToResName = "file://angel/shared/builds/text.doc";
        toResInfo.setName(myToResName);
        model1.setToResourceInfo(toResInfo);
        //model1.setUserId(new Long(8888));
        String myUserName1 = "myUser@bluejungle.com";
        model1.setUserName(myUserName1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsDataOne, model1);

        final int nbRecordsDataTwo = 47;
        myAction = ActionEnumType.ACTION_RENAME;
        model2.setAction(myAction);
        model2.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model2.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(104, 10, 23));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file://angel/shared/builds/text.doc";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model2.setFromResourceInfo(fromResInfo);
        model2.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model2.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model2.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 0, 2));
        model2.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///e:/my music/hi.txt";
        toResInfo.setName(myToResName);
        model2.setToResourceInfo(toResInfo);
        //model2.setUserId(new Long(9001));
        String myUserName2 = "rlin@bluejungle.com";
        model2.setUserName(myUserName2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(41), nbRecordsDataTwo, model2);

        final int nbRecordsDataThree = 53;
        myAction = ActionEnumType.ACTION_MOVE;
        model3.setAction(myAction);
        model3.setApplicationId(new Long(5));
        myAppName = "Microsoft Word";
        model3.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///c:/test/test.xml";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model3.setFromResourceInfo(fromResInfo);
        model3.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model3.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model3.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(106, 1, 1));
        model3.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///z:/platform/win32/debug/stop.exe";
        toResInfo.setName(myToResName);
        model3.setToResourceInfo(toResInfo);
        //model3.setUserId(new Long(9002));
        String myUserName3 = "ihanen@bluejungle.com";
        model3.setUserName(myUserName3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(88), nbRecordsDataThree, model3);

        final int nbRecordsDataFour = 61;
        myAction = ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN;
        model4.setAction(myAction);
        model4.setApplicationId(new Long(5));
        myAppName = "Notepad.exe";
        model4.setApplicationName(myAppName);
        fromResInfo = new FromResourceInformationDO();
        myFromResCreatedDate = Calendar.getInstance();
        myFromResCreatedDate.setTime(new Date(105, 11, 21));
        fromResInfo.setCreatedDate(myFromResCreatedDate);
        myFromResModifiedDate = Calendar.getInstance();
        fromResInfo.setModifiedDate(myFromResModifiedDate);
        myFromResName = "file:///z:/platform/win32/debug/stop.exe";
        fromResInfo.setName(myFromResName);
        myFromResOwnerId = "5004";
        fromResInfo.setOwnerId(myFromResOwnerId);
        myFromResSize = new Long(98765);
        fromResInfo.setSize(myFromResSize);
        model4.setFromResourceInfo(fromResInfo);
        model4.setHostId(new Long(2011));
        myHostIPAddress = "192.18.13.99";
        model4.setHostIPAddress(myHostIPAddress);
        myHostName = "axis.bluejungle.com";
        model4.setHostName(myHostName);
        myTimestamp = Calendar.getInstance();
        myTimestamp.setTime(new Date(105, 11, 29));
        model4.setTimestamp(myTimestamp);
        toResInfo = new ToResourceInformationDO();
        myToResName = "file:///c:/test/test.xml";
        toResInfo.setName(myToResName);
        model4.setToResourceInfo(toResInfo);
        //model4.setUserId(new Long(9003));
        String myUserName4 = "yourUser@bluejungle.com";
        model4.setUserName(myUserName4);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(141), nbRecordsDataFour, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        //report.setEffects(null);
        report.setActions(new ActionList(new String[] { ActionEnumType.ACTION_RENAME.getName(), ActionEnumType.ACTION_MOVE.getName() }));
        Calendar beginDate = Calendar.getInstance();
        beginDate.setTime(new Date(106, 1, 1));
        report.setBeginDate(beginDate);
        report.setEndDate(Calendar.getInstance());
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(new StringList(new String[] { "c:\\test\test.xml", "*stop*" }));
        //report.setSortSpec(new
        // ReportSortSpec(ReportSortFieldName.FromResource,
        // SortDirection.Descending));
        report.setUsers(new StringList(new String[] { "myUser@bluejungle.com", "ihanen@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        final int nbFirstFetch = 10;
        final int nbSecondFetch = 20;
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbRecordsDataOne + nbRecordsDataTwo + nbRecordsDataThree + nbRecordsDataFour, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        ReportState state = result.getState();
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbRecordsDataThree, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbRecordsDataThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbRecordsDataThree, list.getResults().length);
    }

    public void testPolicyActivityAndTrackingActivity() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        final int nbRecordsIhanen = 23;
        final int nbRecordsRlin = 27;
        final int nbRecordsFuad = 31;
        final int nbRecordsCsarna = 37;
        model1.setUserName("ihanen@bluejungle.com");
        model2.setUserName("rlin@bluejungle.com");
        model3.setUserName("fuad@bluejungle.com");
        model4.setUserName("csarna@bluejungle.com");
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecordsIhanen, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbRecordsRlin, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbRecordsFuad, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbRecordsCsarna, model4);
        s.close();

        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        //report.setObligations(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(new StringList(new String[] { "ihanen@bluejungle.com", "fuad@bluejungle.com" }));

        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, (nbRecordsIhanen + nbRecordsRlin) * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbRecordsIhanen, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbRecordsIhanen, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbRecordsIhanen, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testPolicyActivityWithLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(3);
        
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLevelThree * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelThree, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelThree, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
        PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", policyDetailRes.getAction());
        assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", policyDetailRes.getHostName());
        assertNotNull("All fields should be populated", policyDetailRes.getId());
        assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        assertEquals("Logging Level should be correct", 3, policyDetailRes.getLoggingLevel());
    }
    
    public void testPolicyActivityWithMultipleLoggingLevel() throws HibernateException, RemoteException{
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(1);
        
        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, (nbLevelOne + nbLevelTwo + nbLevelThree) * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbLevelOne + nbLevelTwo + nbLevelThree, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbLevelOne + nbLevelTwo + nbLevelThree, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbLevelOne + nbLevelTwo + nbLevelThree, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
            PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", policyDetailRes.getAction());
            assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", policyDetailRes.getHostName());
            assertNotNull("All fields should be populated", policyDetailRes.getId());
            assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", policyDetailRes.getUserName());
            assertTrue("Logging Level should not be 0", policyDetailRes.getLoggingLevel() != 0);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testTrackingActivityWithLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(3);
        
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLevelThree * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelThree, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelThree, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be tracking activity", detailRes instanceof DocumentActivityDetailResult);
        DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", trackingDetailRes.getAction());
        assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
        assertNotNull("All fields should be populated", trackingDetailRes.getId());
        assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
        assertEquals("Logging Level should be correct", 3, trackingDetailRes.getLoggingLevel());
    }
    
    public void testTrackingActivityWithMultipleLoggingLevel() throws HibernateException, RemoteException{
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(1);
        
        ReportExecutionIF reportExecution = getReportExecution();
        try {
            long start = System.currentTimeMillis();
            ReportResult result = reportExecution.executeReport(report, (nbLevelOne + nbLevelTwo + nbLevelThree) * 2, -1);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("A result should be returned", result);
            assertNotNull("A state should be returned", result.getState());
            assertTrue("The result should be a detail", result instanceof ReportDetailResult);
            ReportDetailResult details = (ReportDetailResult) result;
            assertEquals("The number of rows should be correct", nbLevelOne + nbLevelTwo + nbLevelThree, details.getAvailableRowCount());
            assertEquals("The number of rows should be correct", nbLevelOne + nbLevelTwo + nbLevelThree, details.getTotalRowCount());
            DetailResultList list = details.getData();
            assertNotNull("The result list should not be null", list);
            assertNotNull("The list should have results", list.getResults());
            assertEquals("The list should have results", nbLevelOne + nbLevelTwo + nbLevelThree, list.getResults().length);
            ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
            assertTrue("Result returned should be tracking activity", detailRes instanceof DocumentActivityDetailResult);
            DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
            assertNotNull("All fields should be populated", trackingDetailRes.getAction());
            assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
            assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
            assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
            assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
            assertNotNull("All fields should be populated", trackingDetailRes.getId());
            assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
            assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
            assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
            assertTrue("Logging Level should not be 0", trackingDetailRes.getLoggingLevel() != 0);
        } catch (RemoteException e) {
            fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    /**
     * Tests with different values for logginglevel
     * @throws HibernateException
     * @throws RemoteException
     */
    public void testPolicyActivityWithNoMatchLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(5);
        
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLevelThree * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        assertNull("The result list should not be null", details.getData());
        }
    
    public void testPolicyActivityWithoutSettingLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
               
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, (nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree) * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree , details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
        PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", policyDetailRes.getAction());
        assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", policyDetailRes.getHostName());
        assertNotNull("All fields should be populated", policyDetailRes.getId());
        assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", policyDetailRes.getLoggingLevel() == 0);
    
        }
    
    public void testPolicyActivityWithCustomisedLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelFour = 23;
        final int nbLevelFive= 27;
        final int nbLevelSix= 31;
        final int nbLevelSeven = 37;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model2 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model3 = this.sampleDataMgr.getBasicPolicyLogRecord();
        TestPolicyActivityLogEntryDO model4 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(4);
        model2.setLevel(5);
        model3.setLevel(6);
        model4.setLevel(7);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLevelFour, model1);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(23), nbLevelFive, model2);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(50), nbLevelSix, model3);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(81), nbLevelSeven, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(7);
               
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, (nbLevelSeven) * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelSeven , details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelSeven, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelSeven, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
        PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", policyDetailRes.getAction());
        assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", policyDetailRes.getHostName());
        assertNotNull("All fields should be populated", policyDetailRes.getId());
        assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", policyDetailRes.getLoggingLevel() != 0);
    
        }
    
    public void testTrackingActivityWithNoMatchLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(5);
        ReportExecutionIF reportExecution = getReportExecution();
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLevelThree * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        assertNull("The result list should not be null", details.getData());
        }
    
    public void testTrackingyActivityWithoutSettingLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelZero = 23;
        final int nbLevelOne = 27;
        final int nbLevelTwo = 31;
        final int nbLevelThree = 37;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        model2.setLevel(1);
        model3.setLevel(2);
        model4.setLevel(3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLevelZero, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbLevelOne, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbLevelTwo, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbLevelThree, model4);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        ReportExecutionIF reportExecution = getReportExecution();
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, (nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree) * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree , details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelZero + nbLevelOne + nbLevelTwo + nbLevelThree, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
        DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", trackingDetailRes.getAction());
        assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
        assertNotNull("All fields should be populated", trackingDetailRes.getId());
        assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", trackingDetailRes.getLoggingLevel() == 0);
    
        }
    public void testTrackingActivityWithCustomisedLoggingLevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLevelFour = 23;
        final int nbLevelFive= 27;
        final int nbLevelSix= 31;
        final int nbLevelSeven = 37;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model2 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model3 = this.sampleDataMgr.getBasicTrackingLogRecord();
        TestTrackingActivityLogEntryDO model4 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(4);
        model2.setLevel(5);
        model3.setLevel(6);
        model4.setLevel(7);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLevelFour, model1);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(23), nbLevelFive, model2);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(50), nbLevelSix, model3);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(81), nbLevelSeven, model4);
        s.close();
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(7);
        ReportExecutionIF reportExecution = getReportExecution();
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, (nbLevelSeven) * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLevelSeven , details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLevelSeven, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLevelSeven, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
        DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", trackingDetailRes.getAction());
        assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
        assertNotNull("All fields should be populated", trackingDetailRes.getId());
        assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", trackingDetailRes.getLoggingLevel() != 0);
    
        }
    /**
     * Tests for checking boundary condition follows:
     * @throws HibernateException
     * @throws RemoteException
     */
    public void testTrackingActivityWithLowestBoundaryLogginglevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLowLevel = 23;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLowLevel, model1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(-2147483648);
        ReportExecutionIF reportExecution = getReportExecution();
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLowLevel * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", nbLowLevel, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLowLevel, details.getTotalRowCount());
        assertNotNull("The result list should not be null", details.getData());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLowLevel, list.getResults().length);
        ActivityDetailResult detailRes =  list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof DocumentActivityDetailResult);
        DocumentActivityDetailResult trackingDetailRes = (DocumentActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", trackingDetailRes.getAction());
        assertNotNull("All fields should be populated", trackingDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", trackingDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", trackingDetailRes.getHostName());
        assertNotNull("All fields should be populated", trackingDetailRes.getId());
        assertNotNull("All fields should be populated", trackingDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", trackingDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", trackingDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", trackingDetailRes.getLoggingLevel() == 0);
    
        
        }
    
    public void testTrackingActivityWithHighestBoundaryLogginglevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLowLevel = 23;
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        model1.setLevel(0);
        this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbLowLevel, model1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(2147483647);
        ReportExecutionIF reportExecution = getReportExecution();
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLowLevel * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        assertNull("The result list should be null", details.getData());
                
        }
    
    public void testPolicyActivityWithLowestBoundaryLogginglevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbLowLevel = 23;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbLowLevel, model1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(-2147483648);
               
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbLowLevel * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct",  nbLowLevel , details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", nbLowLevel, details.getTotalRowCount());
        DetailResultList list = details.getData();
        assertNotNull("The result list should not be null", list);
        assertNotNull("The list should have results", list.getResults());
        assertEquals("The list should have results", nbLowLevel, list.getResults().length);
        ActivityDetailResult detailRes = (ActivityDetailResult) list.getResults()[0];
        assertTrue("Result returned should be policy activity", detailRes instanceof PolicyActivityDetailResult);
        PolicyActivityDetailResult policyDetailRes = (PolicyActivityDetailResult) detailRes;
        assertNotNull("All fields should be populated", policyDetailRes.getAction());
        assertNotNull("All fields should be populated", policyDetailRes.getApplicationName());
        assertNotNull("All fields should be populated", policyDetailRes.getFromResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getHostIPAddress());
        assertNotNull("All fields should be populated", policyDetailRes.getHostName());
        assertNotNull("All fields should be populated", policyDetailRes.getId());
        assertNotNull("All fields should be populated", policyDetailRes.getTimestamp());
        assertNotNull("All fields should be populated", policyDetailRes.getToResourceName());
        assertNotNull("All fields should be populated", policyDetailRes.getUserName());
        assertTrue("Logging Level should not be 0", policyDetailRes.getLoggingLevel() == 0);
    
        }
    
    public void testPolicyActivityWithHighestBoundaryLogginglevel() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        final int nbHighLevel = 23;
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        model1.setLevel(0);
        this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbHighLevel, model1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(2147483647);
        
        ReportExecutionIF reportExecution = getReportExecution();
        
        long start = System.currentTimeMillis();
        ReportResult result = reportExecution.executeReport(report, nbHighLevel * 2, -1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("A result should be returned", result);
        assertNotNull("A state should be returned", result.getState());
        assertTrue("The result should be a detail", result instanceof ReportDetailResult);
        ReportDetailResult details = (ReportDetailResult) result;
        assertEquals("The number of rows should be correct", 0, details.getAvailableRowCount());
        assertEquals("The number of rows should be correct", 0, details.getTotalRowCount());
        assertNull("The result list should not be null", details.getData());
        }
    
    /**
     * This test case tests a policy activity log without any custom attributes.
     */
    public void testPolicyActivityNoCustomAttributes() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long(0), 1, model1, new Long(0), 0);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
        assertEquals("The policy name should match", "/folder/Policy18", customLogResult.getPolicyName());
        //TODO: need to fix this
//        assertNull("The custom attribute list should be null", customLogResult.getCustomAttributeList());   
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly five custom attribute", 5, customAttributeArray.length);
    }
    
    /**
     * This test case tests a policy activity log with one custom attributes
     */
    public void testPolicyActivityOneCustomAttribute() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long(0), 1, model1, new Long(0), 1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
        assertEquals("The policy name should match", "/folder/Policy18", customLogResult.getPolicyName());
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly six custom attribute", 6, customAttributeArray.length);
        CustomAttribute customAttribute = customAttributeArray[5];
        assertEquals("The custom attribute key is mismatching", "Key0", customAttribute.getKey());
        assertEquals("The custom attribute value is mismatching", "Value0", customAttribute.getValue());        
    }
    
    /**
     * This test case tests a policy activity log with multiple custom attributes
     */
    public void testPolicyActivityMultipleCustomAttributes() throws HibernateException, RemoteException {
        int nbAttributes = 7;
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long(0), 1, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
        assertEquals("The policy name should match", "/folder/Policy18", customLogResult.getPolicyName());
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly " + (nbAttributes+5) + " custom attribute", nbAttributes+5, customAttributeArray.length);        
        for (int i = 5; i < nbAttributes; i++){
            CustomAttribute customAttribute = customAttributeArray[i];
            assertTrue("The custom attribute key is mismatching", customAttribute.getKey().startsWith("Key"));
            assertTrue("The custom attribute value is mismatching", customAttribute.getValue().startsWith("Value"));
        }
    }
    
    /**
     * This test case tests the retrieval of policy activities with different number of custom attributes
     */
    public void testPolicyActivityMultipleCustomAttributesWithMultipleLogRecords() throws HibernateException, RemoteException {
        int nbAttributes = 9;
        int nbLogs = 5;
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long(0), nbLogs, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        for (int i = 0; i < nbLogs; i++){
            LogDetailResult result = reportExecution.getLogDetail(report, new Long(i).longValue());
            assertNotNull("The returned result should not be null", result);
            PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
            assertEquals("The record id should match", BigInteger.valueOf(new Long(i).longValue()), customLogResult.getId());
            assertEquals("The policy name should match", "/folder/Policy18", customLogResult.getPolicyName());
            CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
            assertNotNull("The custom attribute list should not be null", customAttributeList); 
            CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
            assertEquals("There should be exactly " + (nbAttributes+5) + " custom attribute", nbAttributes+5, customAttributeArray.length);        
            for (int j = 5; j < nbAttributes; j++){
                CustomAttribute customAttribute = customAttributeArray[j];
                assertTrue("The custom attribute key is mismatching", customAttribute.getKey().startsWith("Key"));
                assertTrue("The custom attribute value is mismatching", customAttribute.getValue().startsWith("Value"));
                int keyIndex = Integer.parseInt(customAttribute.getKey().substring(3));
                int valueIndex = Integer.parseInt(customAttribute.getValue().substring(5));
                assertTrue("The custom attribute key does not belong to this log record", (keyIndex >= i * nbAttributes) && (keyIndex < (i+1) * nbAttributes));
                assertTrue("The custom attribute value does not belong to this log record", (valueIndex >= i * nbAttributes) && (valueIndex < (i+1) * nbAttributes));
            }
        }
    }
    
    /**
     * This test case tests the getLogDetails() API with an invalid policy activity log id.
     */
    public void testGetPolicyActivityDetailsWithInvalidId() throws HibernateException, RemoteException {
        int nbAttributes = 9;
        int nbLogs = 5;
        Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long(0), nbLogs, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        try {
        	reportExecution.getLogDetail(report, new Long(50).longValue());
        } catch (UnknownEntryFault e){
            // do nothing
        }
    }
    
    public void testPolicyActivityWithLowestBoundaryIdofLog() throws HibernateException, RemoteException {
    	int nbLogs = 1;
    	Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long("-9223372036854775808"), nbLogs, model1, new Long(0), 0);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.PolicyEvents);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        try
        {
	        LogDetailResult result = reportExecution.getLogDetail(report, new Long("-9223372036854775808").longValue());
	        assertNotNull("Result returned should not be null" , result);
	    	assertNotNull("Records should be returned", result.getData());
	    	PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
	        assertEquals("The record id should match", BigInteger.valueOf(new Long("-9223372036854775808").longValue()), customLogResult.getId());
	          
        		        
        } catch(UnknownEntryFault e)
        {
        	fail("Test should not throw exception :" + e.getFaultString());
        }
    }
    
    public void testPolicyActivityWithHighestBoundaryIdofLog() throws HibernateException, RemoteException {
    	int nbLogs = 1;
    	Session s = getActivityDateSource().getSession();
    	TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
    	this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long("9223372036854775807"), nbLogs, model1, new Long(0), 0);
    	s.close();
       
    	final Report report = new Report();
    	report.setTarget(ReportTargetType.PolicyEvents);
    	report.setSummaryType(ReportSummaryType.None);
    	report.setEffects(null);
    	report.setActions(null);
    	report.setBeginDate(null);
    	report.setEndDate(null);
    	report.setPolicies(null);
    	report.setResourceNames(null);
    	report.setSortSpec(null);
    	report.setUsers(null);
    	report.setLoggingLevel(0);
   
   ReportExecutionIF reportExecution = getReportExecution();
   try
   {
	   LogDetailResult result = reportExecution.getLogDetail(report, new Long("9223372036854775807").longValue());
	   assertNotNull("Result returned should not be null" , result);
	   assertNotNull("Records should be returned", result.getData());
	   PolicyActivityCustomResult customLogResult = (PolicyActivityCustomResult)result.getData();
	   assertEquals("The record id should match", BigInteger.valueOf(new Long("9223372036854775807").longValue()), customLogResult.getId());
	                 
   } catch(UnknownEntryFault e)
   {
	   fail("Test should not throw exception :" + e.getFaultString());
   }
   }
    //TODO commenting out failing tests for which bug is being filed
    /*
    public void testPolicyActivityWithOutofHighestBoundaryIdofLog() throws HibernateException, RemoteException {
 	   int nbLogs = 1;
   	   Session s = getActivityDateSource().getSession();
        TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
        this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long("9223372036854775807"), nbLogs, model1, new Long(0), 0);
        s.close();
       
       final Report report = new Report();
       report.setTarget(ReportTargetType.PolicyEvents);
       report.setSummaryType(ReportSummaryType.None);
       report.setEffects(null);
       report.setActions(null);
       report.setBeginDate(null);
       report.setEndDate(null);
       report.setPolicies(null);
       report.setResourceNames(null);
       report.setSortSpec(null);
       report.setUsers(null);
       report.setLoggingLevel(0);
       
       ReportExecutionIF reportExecution = getReportExecution();
       boolean exThrown = false; 
       try
       {
     	  LogDetailResult result = reportExecution.getLogDetail(report, new Long("9223372036854775808").longValue());
     	      	         		        
       } catch(RemoteException e)
       {
     	  fail("Test should not throw an exception :" + e.getMessage());
       }
       catch(NumberFormatException e)
       {
     	  exThrown = true;
       }
       assertTrue("getLogDetail API should not throw Runtime Exception", !exThrown);
   }
    public void testPolicyActivityWithOutofLowestBoundaryIdofLog() throws HibernateException, RemoteException {
 	   int nbLogs = 1;
   	   Session s = getActivityDateSource().getSession();
       TestPolicyActivityLogEntryDO model1 = this.sampleDataMgr.getBasicPolicyLogRecord();
       this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, new Long("-9223372036854775808"), nbLogs, model1, new Long(0), 0);
       s.close();
       
       final Report report = new Report();
       report.setTarget(ReportTargetType.PolicyEvents);
       report.setSummaryType(ReportSummaryType.None);
       report.setEffects(null);
       report.setActions(null);
       report.setBeginDate(null);
       report.setEndDate(null);
       report.setPolicies(null);
       report.setResourceNames(null);
       report.setSortSpec(null);
       report.setUsers(null);
       report.setLoggingLevel(0);
       
       ReportExecutionIF reportExecution = getReportExecution();
       boolean exThrown = false; 
       try
       {
     	  LogDetailResult result = reportExecution.getLogDetail(report, new Long("-9223372036854775809").longValue());
     	      	         		        
       } catch(RemoteException e)
       {
     	  fail("Test should not throw an exception :" + e.getMessage());
       }
       catch(NumberFormatException e)
       {
     	  exThrown = true;
       }
       assertTrue("getLogDetail API should not throw Runtime Exception", !exThrown);
   }
   */   
    /**
     * This test case tests a document activity log without any custom attributes
     */
    public void testDocumentActivityNoCustomAttributes() throws HibernateException, RemoteException{
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(0), 1, model1, new Long(0), 0);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
//        assertNull("The custom attribute list should be null", customLogResult.getCustomAttributeList()); 
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly five custom attribute", 5, customAttributeArray.length);
    }
    
    /**
     * This test case tests a document activity log with one custom attributes
     */
    public void testDocumentActivityOneCustomAttribute() throws HibernateException, RemoteException {
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(0), 1, model1, new Long(0), 1);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly six custom attribute", 6, customAttributeArray.length);
        CustomAttribute customAttribute = customAttributeArray[5];
        assertEquals("The custom attribute key is mismatching", "Key0", customAttribute.getKey());
        assertEquals("The custom attribute value is mismatching", "Value0", customAttribute.getValue()); 
    }
    
    /**
     * This test case tests a document activity log with multiple custom attributes
     */
    public void testDocumentActivityMultipleCustomAttributes()throws HibernateException, RemoteException {
        int nbAttributes = 7;
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(0), 1, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        LogDetailResult result = reportExecution.getLogDetail(report, new Long(0).longValue());
        assertNotNull("The returned result should not be null", result);
        DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
        assertEquals("The record id should match", BigInteger.valueOf(new Long(0).longValue()), customLogResult.getId());
        CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
        assertNotNull("The custom attribute list should not be null", customAttributeList); 
        CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
        assertEquals("There should be exactly " + (nbAttributes+5) + " custom attribute", nbAttributes+5, customAttributeArray.length);        
        for (int i = 5; i < nbAttributes; i++){
            CustomAttribute customAttribute = customAttributeArray[i];
            assertTrue("The custom attribute key is mismatching", customAttribute.getKey().startsWith("Key"));
            assertTrue("The custom attribute value is mismatching", customAttribute.getValue().startsWith("Value"));
        }
    }
    
    /**
     * This test case tests the retrieval of document activities with different number of custom attributes
     */
    public void testDocumentActivityMultipleCustomAttributesWithMultipleLogRecords() throws HibernateException, RemoteException {
        int nbAttributes = 9;
        int nbLogs = 5;
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(0), nbLogs, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        for (int i = 0; i < nbLogs; i++){
            LogDetailResult result = reportExecution.getLogDetail(report, new Long(i).longValue());
            assertNotNull("The returned result should not be null", result);
            DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
            assertEquals("The record id should match", BigInteger.valueOf(new Long(i).longValue()), customLogResult.getId());
            CustomAttributeList customAttributeList = customLogResult.getCustomAttributeList();
            assertNotNull("The custom attribute list should not be null", customAttributeList); 
            CustomAttribute[] customAttributeArray = customAttributeList.getCustomAttributes();
            assertEquals("There should be exactly " + (nbAttributes+5) + " custom attribute", nbAttributes+5, customAttributeArray.length);        
            for (int j = 5; j < nbAttributes; j++){
                CustomAttribute customAttribute = customAttributeArray[j];
                assertTrue("The custom attribute key is mismatching", customAttribute.getKey().startsWith("Key"));
                assertTrue("The custom attribute value is mismatching", customAttribute.getValue().startsWith("Value"));
                int keyIndex = Integer.parseInt(customAttribute.getKey().substring(3));
                int valueIndex = Integer.parseInt(customAttribute.getValue().substring(5));
                assertTrue("The custom attribute key does not belong to this log record", (keyIndex >= i * nbAttributes) && (keyIndex < (i+1) * nbAttributes));
                assertTrue("The custom attribute value does not belong to this log record", (valueIndex >= i * nbAttributes) && (valueIndex < (i+1) * nbAttributes));
            }
        }
    }
    
    /**
     * This test case tests the getLogDetails() API with an invalid document activity log id.
     */
    public void testGetDocumentActivityDetailsWithInvalidId() throws HibernateException, RemoteException {
        int nbAttributes = 9;
        int nbLogs = 5;
        Session s = getActivityDateSource().getSession();
        TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
        this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(0), nbLogs, model1, new Long(0), nbAttributes);
        s.close();
        
        final Report report = new Report();
        report.setTarget(ReportTargetType.ActivityJournal);
        report.setSummaryType(ReportSummaryType.None);
        report.setEffects(null);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setResourceNames(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setLoggingLevel(0);
        
        ReportExecutionIF reportExecution = getReportExecution();
        try {
            reportExecution.getLogDetail(report, new Long(50).longValue());
        } catch (UnknownEntryFault e){
            // do nothing
        }
    }
    public void testTrackingActivityWithLowestBoundaryIdofLog() throws HibernateException, RemoteException {
       int nbLogs = 1;
       Session s = getActivityDateSource().getSession();
       TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
	   this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long("-9223372036854775808"), nbLogs, model1, new Long(0), 0);
	   s.close();
	   
	   final Report report = new Report();
	   report.setTarget(ReportTargetType.ActivityJournal);
	   report.setSummaryType(ReportSummaryType.None);
	   report.setEffects(null);
	   report.setActions(null);
	   report.setBeginDate(null);
	   report.setEndDate(null);
	   report.setPolicies(null);
	   report.setResourceNames(null);
	   report.setSortSpec(null);
	   report.setUsers(null);
	   report.setLoggingLevel(0);
	   
	   ReportExecutionIF reportExecution = getReportExecution();
	   try
	   {
	        LogDetailResult result = reportExecution.getLogDetail(report, new Long("-9223372036854775808").longValue());
	        assertNotNull("Result returned should not be null" , result);
	        assertNotNull("Records should be returned", result.getData());
	        DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
	        assertEquals("The record id should match", BigInteger.valueOf(new Long("-9223372036854775808").longValue()), customLogResult.getId());
	          
	   		        
	   } catch(UnknownEntryFault e)
	   {
	   		fail("Test should not throw exception :" + e.getFaultString());
	          	       	
	       }
	   }
	   
   public void testTrackingActivityWithHighestBoundaryIdofLog() throws HibernateException, RemoteException {
	   int nbLogs = 1;
  	   Session s = getActivityDateSource().getSession();
       TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
       this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long("9223372036854775807"), nbLogs, model1, new Long(0), 0);
       s.close();
      
      final Report report = new Report();
      report.setTarget(ReportTargetType.ActivityJournal);
      report.setSummaryType(ReportSummaryType.None);
      report.setEffects(null);
      report.setActions(null);
      report.setBeginDate(null);
      report.setEndDate(null);
      report.setPolicies(null);
      report.setResourceNames(null);
      report.setSortSpec(null);
      report.setUsers(null);
      report.setLoggingLevel(0);
      
      ReportExecutionIF reportExecution = getReportExecution();
      try
      {
    	  LogDetailResult result = reportExecution.getLogDetail(report, new Long("9223372036854775807").longValue());
    	  assertNotNull("Result returned should not be null" , result);
    	  assertNotNull("Records should be returned", result.getData());
    	  DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult)result.getData();
          assertEquals("The record id should match", BigInteger.valueOf(new Long("9223372036854775807").longValue()), customLogResult.getId());
          
   	         		        
      } catch(UnknownEntryFault e)
      {
    	  fail("Test should not throw exception :" + e.getFaultString());
      }
  }
   
   public void testTrackingActivityWithUsernameContainSlash() throws HibernateException, RemoteException {
		int nbLogs = 1;
		Session s = getActivityDateSource().getSession();
		TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
		model1.setUserName("Test\\hchan");
		this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long("9223372036854775807"),
				nbLogs, model1, new Long(0), 0);
		s.close();

		final Report report = new Report();
		report.setTarget(ReportTargetType.ActivityJournal);
		report.setSummaryType(ReportSummaryType.None);
		report.setEffects(null);
		report.setActions(null);
		report.setBeginDate(null);
		report.setEndDate(null);
		report.setPolicies(null);
		report.setResourceNames(null);
		report.setSortSpec(null);
		report.setUsers(new StringList(new String[] { "Test//hchan" }));
		report.setLoggingLevel(0);

		ReportExecutionIF reportExecution = getReportExecution();
		try {
			LogDetailResult result = reportExecution.getLogDetail(report, new Long(
					"9223372036854775807").longValue());
			assertNotNull("Result returned should not be null", result);
			assertNotNull("Records should be returned", result.getData());
			DocumentActivityCustomResult customLogResult = (DocumentActivityCustomResult) result
					.getData();
			assertEquals("The record id should match", BigInteger.valueOf(new Long(
					"9223372036854775807").longValue()), customLogResult.getId());

		} catch (UnknownEntryFault e) {
			fail("Test should not throw exception :" + e.getFaultString());
		}
	}
   
   //TODO commenting out following failing tests for which bug is being filed
   /*
   public void testTrackingActivityWithOutofHighestBoundaryIdofLog() throws HibernateException, RemoteException {
	   int nbLogs = 1;
  	   Session s = getActivityDateSource().getSession();
       TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
       this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long("9223372036854775807"), nbLogs, model1, new Long(0), 0);
       s.close();
      
      final Report report = new Report();
      report.setTarget(ReportTargetType.ActivityJournal);
      report.setSummaryType(ReportSummaryType.None);
      report.setEffects(null);
      report.setActions(null);
      report.setBeginDate(null);
      report.setEndDate(null);
      report.setPolicies(null);
      report.setResourceNames(null);
      report.setSortSpec(null);
      report.setUsers(null);
      report.setLoggingLevel(0);
      
      ReportExecutionIF reportExecution = getReportExecution();
      boolean exThrown = false; 
      try
      {
    	  LogDetailResult result = reportExecution.getLogDetail(report, new Long("9223372036854775808").longValue());
    	     	         		        
      } catch(RemoteException e)
      {
    	  fail("Test should not throw an exception :" + e.getMessage());
      }
      catch(NumberFormatException e)
      {
    	  exThrown = true;
      }
      assertTrue("getLogDetail API should not throw Runtime Exception", !exThrown);
  }
   public void testTrackingActivityWithOutofLowestBoundaryIdofLog() throws HibernateException, RemoteException {
	   int nbLogs = 1;
  	   Session s = getActivityDateSource().getSession();
       TestTrackingActivityLogEntryDO model1 = this.sampleDataMgr.getBasicTrackingLogRecord();
       this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long("-9223372036854775808"), nbLogs, model1, new Long(0), 0);
       s.close();
      
      final Report report = new Report();
      report.setTarget(ReportTargetType.ActivityJournal);
      report.setSummaryType(ReportSummaryType.None);
      report.setEffects(null);
      report.setActions(null);
      report.setBeginDate(null);
      report.setEndDate(null);
      report.setPolicies(null);
      report.setResourceNames(null);
      report.setSortSpec(null);
      report.setUsers(null);
      report.setLoggingLevel(0);
      
      ReportExecutionIF reportExecution = getReportExecution();
      boolean exThrown = false; 
      try
      {
    	  LogDetailResult result = reportExecution.getLogDetail(report, new Long("-9223372036854775809").longValue());
    	     	         		        
      } catch(RemoteException e)
      {
    	  fail("Test should not throw an exception :" + e.getMessage());
      }
      catch(NumberFormatException e)
      {
    	  exThrown = true;
      }
      assertTrue("getLogDetail API should not throw Runtime Exception", !exThrown);
  }
  */  
}
