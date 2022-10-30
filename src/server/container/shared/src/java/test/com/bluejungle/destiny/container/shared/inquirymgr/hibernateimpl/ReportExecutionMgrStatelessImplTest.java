/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult;
import com.nextlabs.destiny.container.shared.inquirymgr.InvalidActivityLogIdException;

/**
 * This is the test class for the stateless implementation of the report
 * manager. It verifies that disconnected (i.e. stateless) callers can come back
 * and resume fetching rows from the result table.
 * 
 * This test also tests the API for retrieving the log details.  This call
 * happens when the user already made a report execution call, and comes back
 * to retrieve the details of one of the logs in the report execution result.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrStatelessImplTest.java#1 $
 */

public class ReportExecutionMgrStatelessImplTest extends BaseReportExecutionTest {

    private static final int MAX_FETCH_ROWS = 1000;

    /**
     * Returns an instance of the report execution manager
     * 
     * @return an instance of the report execution manager
     */
    private IStatelessReportExecutionMgr getStatelessReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgr", ReportExecutionMgrStatelessImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IStatelessReportExecutionMgr reportMgr = (IStatelessReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * This test verifies that a caller can properly navigate forward through a
     * list of results by passing the state of the query.
     * 
     * @param report
     *            report to execute
     * @param expectedResultClass
     *            expected class object that all result should use.
     */
    protected void testForwardNavigation(int expectedNbRecord, int pageSize, IReport report, Class expectedResultClass) {
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();

        //Query for the records on the first page
        IStatelessReportExecutionResult resultSet = null;
        try {
            resultSet = reportExecutionMgr.executeReport(report, pageSize, MAX_FETCH_ROWS);
        } catch (DataSourceException e) {
            fail("The report should not have bad arguments");
        } catch (InvalidReportArgumentException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        assertNotNull("A result should be provider to the caller", resultSet);
        IReportResultState state = resultSet.getResultState();
        assertNotNull("A result reader should be provider to the caller", resultSet);
        assertNotNull("A result state should be provider to the caller", state);
        assertTrue("The sequence id provided in the result state should be sequential", state.getFirstRowSequenceId().intValue() <= state.getLastRowSequenceId().intValue());
        assertTrue("The sequence id provided in the result state should be sequential", state.getLastRowSequenceId().intValue() - state.getFirstRowSequenceId().intValue() >= pageSize - 1);
        assertTrue("The query should return results", resultSet.hasNextResult());
        assertEquals("Despite paging, the row count should be the total row count, not just the record count on the current page", expectedNbRecord, resultSet.getStatistics().getTotalRowCount().intValue());
        long beginRowId = state.getFirstRowSequenceId().longValue();
        long endRowId = state.getLastRowSequenceId().longValue();

        //Browse the query results
        int recordCount = 0;
        while (resultSet.hasNextResult()) {
            recordCount++;
            IResultData resultData = resultSet.nextResult();
            assertNotNull("Result record should exist for stateless query", resultData);
            assertTrue("Result records should have the right type", resultData.getClass().isAssignableFrom(expectedResultClass));
        }
        assertTrue("The number of stateless query results should be at most the number specified", recordCount <= pageSize);
        //Fetch the next set of results
        IStatelessReportExecutionResult nextSetResultSet = null;
        try {
            nextSetResultSet = reportExecutionMgr.gotoNextSet(resultSet.getResultState(), pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        IReportResultState nextResultState = nextSetResultSet.getResultState();

        assertNotSame("Navigating to the next set should give different results", resultSet, nextSetResultSet);
        assertNotSame("Navigating to the next set should give different readers", resultSet, nextSetResultSet);
        assertNotSame("Navigating to the next set should give different states", state, nextResultState);
        //Verify that the new state makes sense compared to the old one
        assertEquals("Navigating to the next set return a state with the same query id", state.getQueryId(), nextResultState.getQueryId());
        assertTrue("State for next set should have a greater first row sequence number", nextResultState.getFirstRowSequenceId().longValue() > state.getFirstRowSequenceId().longValue());
        assertTrue("State for next set still be consistent", nextResultState.getLastRowSequenceId().longValue() > nextResultState.getFirstRowSequenceId().longValue());

        recordCount = 0;
        while (nextSetResultSet.hasNextResult()) {
            recordCount++;
            IResultData resultData = nextSetResultSet.nextResult();
            assertNotNull("Result record should exist for stateless query", resultData);
            assertTrue("Result detail record should exist for stateless query", resultData.getClass().isAssignableFrom(expectedResultClass));
        }
        assertTrue("The number of stateless query results should be at most the number specified", recordCount <= pageSize);
        nextSetResultSet.close();
    }

    /**
     * This test verifies that the report execution returns the number of rows
     * specified as an argument
     */
    public void testMaxRowsArgument() {
        final int nbRecords = 300;
        insertPolicyLogRecords(nbRecords);
        IStatelessReportExecutionMgr reportExecMgr = getStatelessReportExecutionMgr();
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        try {
            final int maxRows = 100;
            IReportResultReader obj = reportExecMgr.executeReport(report, maxRows);
            assertNotNull("A reader should be returned", obj);
            assertTrue("The reader should be stateless", obj instanceof IStatelessReportExecutionResult);
            IStatelessReportExecutionResult reader = (IStatelessReportExecutionResult) obj;
            assertNotNull("The reader should have a state", reader.getResultState());
            assertNotNull("The reader should have statistics", reader.getStatistics());
            int resultCount = 0;
            while (reader.hasNextResult()) {
                reader.nextResult();
                resultCount++;
            }
            assertEquals("The number of fetched rows should match", maxRows, resultCount);
            assertEquals("The number of available rows should be correct", new Long(nbRecords), reader.getStatistics().getAvailableRowCount());
            assertEquals("The number of total rows should be correct", new Long(nbRecords), reader.getStatistics().getTotalRowCount());

        } catch (InvalidReportArgumentException e) {
            fail("No invalid report argument exception should be thrown");
        } catch (DataSourceException e) {
            fail("No DataSourceException argument exception should be thrown");
        }

        //Try again with -1 as the maxRows value
        try {
            IReportResultReader obj = reportExecMgr.executeReport(report, -1);
            assertNotNull("A reader should be returned", obj);
            assertTrue("The reader should be stateless", obj instanceof IStatelessReportExecutionResult);
            IStatelessReportExecutionResult reader = (IStatelessReportExecutionResult) obj;
            assertNotNull("The reader should have a state", reader.getResultState());
            assertNotNull("The reader should have statistics", reader.getStatistics());
            int resultCount = 0;
            while (reader.hasNextResult()) {
                reader.nextResult();
                resultCount++;
            }
            assertEquals("The number of fetched rows should match", nbRecords, resultCount);
            assertEquals("The number of available rows should be correct", new Long(nbRecords), reader.getStatistics().getAvailableRowCount());
            assertEquals("The number of total rows should be correct", new Long(nbRecords), reader.getStatistics().getTotalRowCount());

        } catch (InvalidReportArgumentException e) {
            fail("No invalid report argument exception should be thrown");
        } catch (DataSourceException e) {
            fail("No DataSourceException argument exception should be thrown");
        }
    }

    /**
     * This test verifies that the report execution returns the number of rows
     * specified as an argument
     */
    public void testMaxStoreRowsArgument() {
        final int nbRecords = 300;
        final int maxRows = 100;
        insertPolicyLogRecords(nbRecords);
        IStatelessReportExecutionMgr reportExecMgr = getStatelessReportExecutionMgr();
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        try {
            final int maxStoreRows = 200;
            IReportResultReader obj = reportExecMgr.executeReport(report, maxRows, maxStoreRows);
            assertNotNull("A reader should be returned", obj);
            assertTrue("The reader should be stateless", obj instanceof IStatelessReportExecutionResult);
            IStatelessReportExecutionResult reader = (IStatelessReportExecutionResult) obj;
            assertNotNull("The reader should have a state", reader.getResultState());
            assertNotNull("The reader should have statistics", reader.getStatistics());
            int resultCount = 0;
            while (reader.hasNextResult()) {
                reader.nextResult();
                resultCount++;
            }
            assertEquals(maxRows, resultCount);
            assertEquals(new Long(maxStoreRows), reader.getStatistics().getAvailableRowCount());
            assertEquals(new Long(nbRecords), reader.getStatistics().getTotalRowCount());

        } catch (InvalidReportArgumentException e) {
            fail("No invalid report argument exception should be thrown");
        } catch (DataSourceException e) {
            fail("No DataSourceException argument exception should be thrown");
        }

        //Try it again with -1 as the max store value
        try {

            IReportResultReader obj = reportExecMgr.executeReport(report, maxRows, -1);
            assertNotNull("A reader should be returned", obj);
            assertTrue("The reader should be stateless", obj instanceof IStatelessReportExecutionResult);
            IStatelessReportExecutionResult reader = (IStatelessReportExecutionResult) obj;
            assertNotNull("The reader should have a state", reader.getResultState());
            assertNotNull("The reader should have statistics", reader.getStatistics());
            int resultCount = 0;
            while (reader.hasNextResult()) {
                reader.nextResult();
                resultCount++;
            }
            assertEquals(maxRows, resultCount);
            assertEquals(new Long(nbRecords), reader.getStatistics().getAvailableRowCount());
            assertEquals(new Long(nbRecords), reader.getStatistics().getTotalRowCount());

        } catch (InvalidReportArgumentException e) {
            fail("No invalid report argument exception should be thrown");
        } catch (DataSourceException e) {
            fail("No DataSourceException argument exception should be thrown");
        }
    }

    /**
     * This test verifies that the backward / forward navigation works fine for
     * detail reports on policy
     */
    public void testForwardNavigationForQueryByIdWithPolicy() {
        final int nbRecords = 300;
        final int pageSize = 20;
        insertPolicyLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        testForwardNavigation(nbRecords, pageSize, report, ReportPolicyActivityDetailResultDO.class);
    }

    /**
     * This test verifies that the backward / forward navigation works fine for
     * detail reports on tracking
     */
    public void testForwardNavigationForQueryByIdWithTracking() {
        final int nbRecords = 200;
        final int pageSize = 5;
        insertTrackingLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        testForwardNavigation(nbRecords, pageSize, report, ReportTrackingActivityDetailResultDO.class);
    }

    /**
     * This test verifies that the backward / forward navigation works fine for
     * summary report on policy
     */
    public void testForwardNavigationForQuerySummaryWithPolicy() {
        final int nbRecords = 100;
        final int pageSize = 20;

        Session s = null;

        //Insert some dummy users
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 30; i < nbRecords; i++) {
                PolicyDO policy = new PolicyDO();
                policy.setFullName("Policy_" + i);
                policy.setId(new Long(i));
                s.save(policy);
                TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
                model.setPolicyId(new Long(i));
                this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(i * 10), 5, model);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Log records insertion failed.");
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.POLICY);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        testForwardNavigation(nbRecords - 30, pageSize, report, ReportSummaryResultDO.class);
    }

    /**
     * This test verifies that the backward / forward navigation works fine for
     * summary report on policy
     */
    public void testForwardNavigationForQuerySummaryWithTracking() {
        final int nbRecords = 100;
        final int pageSize = 20;
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 30; i < nbRecords; i++) {
                TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
                model.setUserName("user" + i + "@bluejungle.com");
                this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(i * 10), 5, model);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("Log records insertion failed.");
            HibernateUtils.rollbackTransation(t, null);
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.USER);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        testForwardNavigation(nbRecords - 30, pageSize, report, ReportSummaryResultDO.class);
    }

    /**
     * This test verifies that if the same test is provided to the API, the
     * exact same set of results should be returned to the caller.
     */
    public void testSameStateSameResults() {
        final int nbRecords = 200;
        final int pageSize = 20;
        insertPolicyLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        IStatelessReportExecutionResult results = null;
        try {
            results = reportExecutionMgr.executeReport(report, pageSize, MAX_FETCH_ROWS);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        }
        try {
            //Go to a few pages forward, and save the results
            results = reportExecutionMgr.gotoNextSet(results.getResultState(), pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        try {
            results = reportExecutionMgr.gotoNextSet(results.getResultState(), pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        IReportResultState stateToKeep = results.getResultState();
        results.close();
        try {
            results = reportExecutionMgr.gotoNextSet(stateToKeep, pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        Set resultSet = new LinkedHashSet();
        while (results.hasNextResult()) {
            resultSet.add(results.nextResult());
        }

        try {
            //Go another page forward
            reportExecutionMgr.gotoNextSet(results.getResultState(), pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }

        try {
            //Now, try to reuse the preserved state, and make sure we get back
            // exactly the same records.
            results = reportExecutionMgr.gotoNextSet(stateToKeep, pageSize);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        }
        Iterator it = resultSet.iterator();
        while (results.hasNextResult()) {
            IReportPolicyActivityDetailResult detailResult = (IReportPolicyActivityDetailResult) it.next();
            IReportPolicyActivityDetailResult resultData = (IReportPolicyActivityDetailResult) results.nextResult();
            //We could check all of the properties, but I assume that if these
            // work, the other would
            assertEquals("Reusing the same state should result in getting exactly the same records", detailResult.getId(), resultData.getId());
            assertEquals("Reusing the same state should result in getting exactly the same records", detailResult.getPolicyDecision(), resultData.getPolicyDecision());
            assertEquals("Reusing the same state should result in getting exactly the same records", detailResult.getPolicyName(), resultData.getPolicyName());
        }
        results.close();
    }

    /**
     * This test verifies that the result from a stateless query returned still
     * makes senses even if there are no matching records returned.
     */
    public void testStatelessQueryWithoutResults() {
        final int nbRecords = 200;
        final int pageSize = 20;
        insertPolicyLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().addUser("(User) unknownUser");
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        IStatelessReportExecutionResult resultSet = null;
        try {
            resultSet = reportExecutionMgr.executeReport(report, pageSize, MAX_FETCH_ROWS);
        } catch (DataSourceException e) {
            fail("The report execution should not have thrown a datasource exception");
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        }
        assertNotNull("Stateless queries should return a result object, even if query has no matching records", resultSet);
        final IReportResultState state = resultSet.getResultState();
        assertNotNull("Stateless queries should return a state object, even if query has no matching records", state);
        assertFalse("If a stateless query has no matching records, the reader should say so", resultSet.hasNextResult());
        assertNull("If a stateless query has no matching records, the state should not specify start / end sequence id", state.getFirstRowSequenceId());
        assertNull("If a stateless query has no matching records, the state should not specify start / end sequence id", state.getLastRowSequenceId());
        assertNotNull("If a stateless query has no matching records, the state should still give a query id", state.getQueryId());
    }
    
    /**
     * This tests getting the details of a policy activity log without any custom attributes
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testGetPolicyLogDetailWithoutCustomAttributes() throws InvalidActivityLogIdException, DataSourceException{
        final int nbRecords = 1;
        insertPolicyLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 5 custom attributes", 5, customAttributes.size());
    }
    
    /**
     * This tests getting the details of a document activity log without any custom attributes
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testGetTrackingLogDetailWithoutCustomAttributes() throws InvalidActivityLogIdException, DataSourceException{
        final int nbRecords = 1;
        insertTrackingLogRecords(nbRecords);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 5 custom attributes", 5, customAttributes.size());
    }
    
    /**
     * This tests getting the details of a policy activity log with custom attributes
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testGetPolicyLogDetailWithCustomAttributes() throws InvalidActivityLogIdException, DataSourceException{
        final int nbRecords = 1;
        final int nbAttributes = 5;
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, null);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 10 custom attributes", 10, customAttributes.size());
    }
    
    /**
     * This tests getting the details of a document activity log with custom attributes
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testGetTrackingLogDetailWithCustomAttributes() throws InvalidActivityLogIdException, DataSourceException{
        final int nbRecords = 1;
        final int nbAttributes = 5;
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, null);
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 10 custom attributes", 10, customAttributes.size());
    }
    
    // NOTE: the following test cases are here only because the from resource attributes (
    //       resource name, resource size, resource owner id, created date, modified date)
    //       are still in the activity logs (fromResourceInfo).  Therefore we need to test
    //       what happens if their values are null.  In the future when these attributes 
    //       are moved under the custom attributes tables, then these test cases can be
    //       removed.
    /**
     * This tests getting the policy log details when from resource created date is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testPolicyLogDetailWithNullResourceCreatedDate() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(null);
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);        
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the policy log details when from resource modified date is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testPolicyLogDetailWithNullResourceModifiedDate() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(null);
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the policy log details when from resource name is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testPolicyLogDetailWithNullResourceName() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName(null);
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the policy log details when from resource owner id is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testPolicyLogDetailWithNullResourceOwnerId() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId(null);
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }

    /**
     * This tests getting the policy log details when from resource size is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testPolicyLogDetailWithNullResourceSize() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestPolicyActivityLogEntryDO model = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(null);
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomPolicyLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
 
    /**
     * This tests getting the tracking log details when from resource created date is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testTrackingLogDetailWithNullResourceCreatedDate() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(null);
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);        
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the tracking log details when from resource modified date is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testTrackingLogDetailWithNullResourceModifiedDate() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(null);
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the tracking log details when from resource name is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testTrackingLogDetailWithNullResourceName() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName(null);
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
    
    /**
     * This tests getting the tracking log details when from resource owner id is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testTrackingLogDetailWithNullResourceOwnerId() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId(null);
        fromResourceInfo.setSize(new Long(10000));
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());;
        }
    }

    /**
     * This tests getting the tracking log details when from resource size is null
     * @throws DataSourceException 
     * @throws InvalidActivityLogIdException 
     */
    public void testTrackingLogDetailWithNullResourceSize() throws InvalidActivityLogIdException, DataSourceException{
        int nbRecords = 1;
        int nbAttributes = 0;
        TestTrackingActivityLogEntryDO model = this.sampleDataMgr.getBasicTrackingLogRecord();
        FromResourceInformationDO fromResourceInfo = new FromResourceInformationDO();
        fromResourceInfo.setCreatedDate(Calendar.getInstance());
        fromResourceInfo.setModifiedDate(Calendar.getInstance());
        fromResourceInfo.setName("file:///c/docs/fromResource");
        fromResourceInfo.setOwnerId("123456-58889");
        fromResourceInfo.setSize(null);
        model.setFromResourceInfo(fromResourceInfo);
        insertCustomTrackingLogRecords(nbRecords, nbAttributes, model);
        
        IReportMgr reportMgr = getReportMgr();
        IReport report = reportMgr.createReport();
        report.setSummaryType(ReportSummaryType.NONE);
        report.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        IStatelessReportExecutionMgr reportExecutionMgr = getStatelessReportExecutionMgr();
        ILogDetailResult result = reportExecutionMgr.getLogDetail(report, new Long(1));
        BaseActivityLogDO activityLog = result.getActivityLog();
        HashMap customAttributes = result.getActivityCustomAttributes();
        assertEquals("The log retrieved has incorrect log id", new Long(1), activityLog.getId());
        assertEquals("There should be 4 custom attributes", 4, customAttributes.size());
        Iterator iter = customAttributes.entrySet().iterator();
        for (int i = 0; i < 4; i++){
            Map.Entry entry = (Map.Entry)iter.next();
            assertNotNull("Attribute key is null", entry.getKey());
            assertNotNull("Attribute value is null", entry.getValue());
        }
    }
}