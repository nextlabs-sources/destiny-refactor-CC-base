/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the test class for the "count only" implementation of the execution
 * manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionCountOnlyTest.java#1 $
 */

public class ReportExecutionCountOnlyTest extends BaseReportExecutionTest {

    private static final int MAX_FETCH_ROWS = 1000;
    private SampleDataMgr sampleDataMgr;

    /**
     * Returns an instance of the report manager
     * 
     * @return an instance of the report manager
     */
    protected IReportMgr getReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo compInfo = new ComponentInfo("reportMgr", ReportMgrImpl.class.getName(), IReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IReportMgr reportMgr = (IReportMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * Returns an instance of the "count only" report execution manager
     * implementation
     * 
     * @return an instance of the "count only" report execution manager
     *         implementation
     */
    private ReportExecutionMgrCountOnlyImpl getReportExecutionMgrCountOnly() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgr", ReportExecutionMgrCountOnlyImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        ReportExecutionMgrCountOnlyImpl reportMgr = (ReportExecutionMgrCountOnlyImpl) compMgr.getComponent(compInfo);
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
        this.sampleDataMgr = new SampleDataMgr();
        Session s = getActivityDateSource().getSession();
        try {
            //Cleanup cached data
            this.sampleDataMgr.deletePolicyLogs(s);
            this.sampleDataMgr.deleteTrackingLogs(s);
        } catch (HibernateException e) {
            fail("Unable to cleanup policy / tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.sampleDataMgr = new SampleDataMgr();
        Session s = getActivityDateSource().getSession();
        try {
            //Cleanup cached data
            this.sampleDataMgr.deletePolicyLogs(s);
            this.sampleDataMgr.deleteTrackingLogs(s);
        } catch (HibernateException e) {
            fail("Unable to cleanup policy / tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        super.tearDown();
    }

    /**
     * This test verifies that the number of logs currently in the log table is
     * accounted for properly.
     */
    public void testCountOnlyReportExecutionMgrCountAllLogs() {
        final int nbPolicies = 100;
        final int nbTracking = 150;

        TestPolicyActivityLogEntryDO policyModel = new TestPolicyActivityLogEntryDO();
        TestTrackingActivityLogEntryDO trackingModel = new TestTrackingActivityLogEntryDO();
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicies, this.sampleDataMgr.getBasicPolicyLogRecord());
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbTracking, this.sampleDataMgr.getBasicTrackingLogRecord());
        } catch (HibernateException e) {
            fail("Unable to cleanup policy / tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for the policy count
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportEngine = getReportExecutionMgrCountOnly();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportEngine.executeReport(report, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e1) {
            fail("The report execution should not have throw exception");
        }
        Long policyRowCount = reader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbPolicies, policyRowCount.intValue());
        reader.close();

        //Query for the tracking count
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        try {
            reader = reportEngine.executeReport(report, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        Long trackingCount = reader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbTracking, trackingCount.intValue());
        reader.close();
    }

    /**
     * This test verifies that reports specifiying query constraints can be
     * executed properly and the correct number of matching records is accounted
     * for.
     */
    public void testCountOnlyReportExecutionMgrCountPolicyLogsWithConstraint() {
        final int nbDeletePolicies = 20;
        final int nbCopyPolicies = 30;

        TestPolicyActivityLogEntryDO deleteModel = this.sampleDataMgr.getBasicPolicyLogRecord();
        deleteModel.setAction(ActionEnumType.ACTION_DELETE);
        TestPolicyActivityLogEntryDO copyModel = this.sampleDataMgr.getBasicPolicyLogRecord();
        copyModel.setAction(ActionEnumType.ACTION_COPY);

        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbDeletePolicies, deleteModel);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbDeletePolicies + 2), nbCopyPolicies, copyModel);
        } catch (HibernateException e) {
            fail("Unable to cleanup policy / tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for the 'delete' action policies
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportEngine = getReportExecutionMgrCountOnly();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        report.getInquiry().addAction(ActionEnumType.ACTION_DELETE);
        IReportResultReader deleteReader = null;
        try {
            deleteReader = reportEngine.executeReport(report, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        Long deleteRowCount = deleteReader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbDeletePolicies, deleteRowCount.intValue());

        //Query for the 'copy' action policies
        IReport copyReport = reportMgr.createReport();
        copyReport.setSummaryType(ReportSummaryType.NONE);
        copyReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        copyReport.getInquiry().addAction(ActionEnumType.ACTION_COPY);
        IReportResultReader copyReader = null;
        try {
            copyReader = reportEngine.executeReport(copyReport, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        Long copyRowCount = copyReader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbCopyPolicies, copyRowCount.intValue());

        deleteReader.close();
        copyReader.close();
    }

    /**
     * This test verifies that reports specifiying query constraints can be
     * executed properly and the correct number of matching records is accounted
     * for.
     */
    public void testCountOnlyReportExecutionMgrCountTrackingLogsWithConstraint() {
        final int nbDeletePolicies = 20;
        final int nbCopyPolicies = 30;

        TestTrackingActivityLogEntryDO deleteModel = this.sampleDataMgr.getBasicTrackingLogRecord();
        deleteModel.setAction(ActionEnumType.ACTION_DELETE);
        TestTrackingActivityLogEntryDO copyModel = this.sampleDataMgr.getBasicTrackingLogRecord();
        copyModel.setAction(ActionEnumType.ACTION_COPY);

        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(0), nbDeletePolicies, deleteModel);
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(nbDeletePolicies + 2), nbCopyPolicies, copyModel);
        } catch (HibernateException e) {
            fail("Unable to insert tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for the 'delete' action policies
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportEngine = getReportExecutionMgrCountOnly();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        report.getInquiry().addAction(ActionEnumType.ACTION_DELETE);
        IReportResultReader deleteReader = null;
        try {
            deleteReader = reportEngine.executeReport(report, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        Long deleteRowCount = deleteReader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbDeletePolicies, deleteRowCount.intValue());

        //Query for the 'copy' action policies
        IReport copyReport = reportMgr.createReport();
        copyReport.setSummaryType(ReportSummaryType.NONE);
        copyReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        copyReport.getInquiry().addAction(ActionEnumType.ACTION_COPY);
        IReportResultReader copyReader = null;
        try {
            copyReader = reportEngine.executeReport(copyReport, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        Long copyRowCount = copyReader.getStatistics().getAvailableRowCount();
        assertEquals("The report execution manager for count only should return the right record count", nbCopyPolicies, copyRowCount.intValue());

        deleteReader.close();
        copyReader.close();
    }

    /**
     * This test verifies that the count is only based on records that have a
     * real match in the cache table. "Hollow" records will not be queries
     * anyway, hence shoudl not be part of the count.
     */
    public void testCountOnlyReportExecutionMgrCountPolicyLogsWithMissingCacheValues() {
        final int nbValidPolicies = 20;
        final int nbInvalidPolicies = 30;

        TestPolicyActivityLogEntryDO validModel = this.sampleDataMgr.getBasicPolicyLogRecord();
        validModel.setPolicyId(new Long(5));
        TestPolicyActivityLogEntryDO invalidModel = this.sampleDataMgr.getBasicPolicyLogRecord();
        invalidModel.setPolicyId(new Long(500));

        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbValidPolicies, validModel);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbValidPolicies + 2), nbInvalidPolicies, invalidModel);
        } catch (HibernateException e) {
            fail("Unable to cleanup policy / tracking log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for everything
        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportEngine = getReportExecutionMgrCountOnly();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IReportResultReader reader = null;
        try {
            reader = reportEngine.executeReport(report, MAX_FETCH_ROWS);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader should have a row count", reader.getStatistics().getAvailableRowCount());
        assertEquals("The report execution manager for count only should return the right record count", nbValidPolicies, reader.getStatistics().getAvailableRowCount().intValue());
        reader.close();
    }
}