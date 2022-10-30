/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.InquiryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportOwnerDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportSummaryResultDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.GroupingElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryElementImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.SelectElementImpl;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.ResultTableManagerException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the test case for the stored results module. This test class checks
 * the stored results for both query by id and summary query.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredResultsTest.java#1 $
 */

public class StoredResultsTest extends DACContainerSharedTestCase {

    private static final Log LOG = LogFactory.getLog(StoredResultsTest.class.getName());

    protected static final int NB_REPORTS = 50;

    /**
     * Deletes the test data from the database.
     */
    protected void cleanupData() {
        Transaction t = null;
        Session s = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            s.delete("from StoredQueryDO");
            s.delete("from ReportDO");
            t.commit();
        } catch (HibernateException e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            // the close does nothing in StirngWriter
            // writer.close();
            fail("No hibernate exception should happen during report / query deletion." + writer.toString());
        } finally {
            HibernateUtils.rollbackTransation(t, LOG);
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * Returns the activity data source
     * 
     * @return the activity data source
     */
    protected IHibernateRepository getActivityDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns the query by id
     * 
     * @return the query by id
     */
    protected IQuery getIdQuery() {
        IQuery result = new QueryImpl();
        final IQueryElement queryElement = new QueryElementImpl();
        final SelectElementImpl selectElement = new SelectElementImpl();
        selectElement.setDOClassName("ReportDO");
        selectElement.setDOVarName("report");
        selectElement.setFieldName("id");
        queryElement.getSelects().add(selectElement);
        result.addQueryElement(queryElement);
        return result;
    }

    /**
     * Returns the query by id result table manager
     * 
     * @return the query by id result table manager
     */
    protected IResultTableManager getResultTableMgrById() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResultTableManager.DATA_SOURCE_CONFIG_PARAM, getActivityDataSource());
        config.setProperty(IResultTableManager.RESULT_TABLE_NAME_CONFIG_PARAM, "STORED_QUERY_BY_ID_RESULTS");
        ComponentInfo<IResultTableManager> compInfo = 
            new ComponentInfo<IResultTableManager>(
                "ResultById", 
                ResultTableManagerQueryByIdImpl.class,
                IResultTableManager.class, 
                LifestyleType.TRANSIENT_TYPE, 
                config
        );
        return compMgr.getComponent(compInfo);
    }

    /**
     * Returns the summary query result table manager
     * 
     * @return the summary query result table manager
     */
    protected IResultTableManager getResultTableMgrSummary() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IResultTableManager.DATA_SOURCE_CONFIG_PARAM, getActivityDataSource());
        config.setProperty(IResultTableManager.RESULT_TABLE_NAME_CONFIG_PARAM, "STORED_QUERY_SUMMARY_RESULTS");
        ComponentInfo<IResultTableManager> compInfo = 
            new ComponentInfo<IResultTableManager>(
                    "SummaryQuery", 
                    ResultTableManagerSummaryImpl.class, 
                    IResultTableManager.class, 
                    LifestyleType.TRANSIENT_TYPE, 
                    config
        );
        return compMgr.getComponent(compInfo);
    }

    /**
     * Returns the summary query
     * 
     * @return the summary query
     */
    protected IQuery getSummaryQuery() {
        IQuery result = new QueryImpl();
        final IQueryElement queryElement = new QueryElementImpl();
        final SelectElementImpl selectTitleElement = new SelectElementImpl();
        selectTitleElement.setDOClassName("ReportDO");
        selectTitleElement.setDOVarName("report");
        selectTitleElement.setFieldName("title");
        final GroupingElementImpl groupByTitleElement = new GroupingElementImpl("report.title");
        queryElement.getSelects().add(selectTitleElement);
        queryElement.getGroupings().add(groupByTitleElement);

        final SelectElementImpl selectCountTitleElement = new SelectElementImpl();
        selectCountTitleElement.setDOClassName("ReportDO");
        selectCountTitleElement.setDOVarName("report");
        selectCountTitleElement.setFieldName("title");
        selectCountTitleElement.setFunction("count");
        queryElement.getSelects().add(selectCountTitleElement);
        result.addQueryElement(queryElement);
        return result;
    }

    /**
     * Insert data in the database.
     */
    protected void insertReportData(int startTitleId) {
        //Insert records in the database
        Transaction t = null;
        Session s = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            for (int i = startTitleId; i < startTitleId+NB_REPORTS; i++) {
                ReportDO newReport = new ReportDO();
                newReport.setSummaryType(ReportSummaryType.NONE);
                ReportOwnerDO reportOwner = new ReportOwnerDO();
                reportOwner.setIsShared(false);
                reportOwner.setOwnerId(new Long(0));
                newReport.setOwner(reportOwner);
                InquiryDO newInquiry = new InquiryDO();
                newInquiry.setTargetData(InquiryTargetDataType.POLICY);
                newReport.setTitle("Report_" + i);
                newReport.setInquiry(newInquiry);
                s.save(newReport);
            }
            t.commit();
        } catch (HibernateException e) {
            fail("No Hibernate exception should be occuring during report insertion");
        } finally {
            HibernateUtils.rollbackTransation(t, LOG);
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        cleanupData();
        insertReportData(0);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        cleanupData();
        super.tearDown();
    }
    
    public void testQueryByIdResults(int nbFetch) {
        final IResultTableManager resultMgr = getResultTableMgrById();
        Long queryId = null;
        try {
            queryId = resultMgr.storeResults(getIdQuery(), ReportDO.class, nbFetch, NB_REPORTS);
        } catch (ResultTableManagerException e) {
            fail("Exception thrown when storing results");
        }

        //Checks the stored query creation
        IStoredQuery storedQuery = resultMgr.getStoredQuery(queryId);
        assertEquals("query id should match", queryId, storedQuery.getId());
        assertEquals("Stored query should remember that ReportDO was used",
                ReportDO.class.getName(), storedQuery.getDataObjectName());
        assertEquals("Stored query should remember the result DO used to browse the result table", 
                StoredQueryByIdResultDO.class.getName(), storedQuery.getResultObjectName());

        //Fetches the report data objects now
        Session s = null;
        try {
            s = getActivityDataSource().getSession();
            Query resultQuery = s.createQuery("select report"
                    + " from ReportDO report, StoredQueryByIdResultDO result" 
                    + " where result.query.id=:queryId AND result.resultId = report.id");
            resultQuery.setLong("queryId", queryId);
            List<?> results = resultQuery.list();
            if(nbFetch == -1){
                
            }
            assertEquals("The number of queried records should be the same", (nbFetch == -1 ? NB_REPORTS : nbFetch), results.size());
        } catch (HibernateException e) {
            fail("The test threw a Hibernate exception: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * Test the insertion inside the result by id table.
     */
    public void testQueryByIdResults() {
        testQueryByIdResults(-1);
    }

    /**
     * This test verifies if only the maximum number of records are inserted
     * into the result table.
     */
    public void testQueryByIdResultsWithLimitFetch() {
        final int nbFetch = 20;
        assertTrue("The test should be setup properly", nbFetch < NB_REPORTS);
        testQueryByIdResults(nbFetch);
    }

    /**
     * This test verifies that the 0 argument is supported as a maximum row
     * fetch number
     */
    public void testQueryByIdResultsWithZeroFetch() {
        testQueryByIdResults(0);
    }
    
    private void testQuerySummaryResults(int nbFetch) {
        //insert sample data a second time here, to get interesting count
        insertReportData(50);
        
        final IResultTableManager resultMgr = getResultTableMgrSummary();
        Long queryId = null;
        try {
            queryId = resultMgr.storeResults(getSummaryQuery(), ReportDO.class, nbFetch, NB_REPORTS);
        } catch (ResultTableManagerException e) {
            fail("Exception thrown when storing results");
        }

        //Checks the stored query creation
        IStoredQuery storedQuery = resultMgr.getStoredQuery(queryId);
        assertEquals("query id should match", queryId, storedQuery.getId());
        assertEquals("Stored query should remember that ReportDO was used", 
                ReportDO.class.getName(), storedQuery.getDataObjectName());
        assertEquals("Stored query should remember the result DO used to browse the result table", 
                StoredQuerySummaryResultDO.class.getName(), storedQuery.getResultObjectName());

        //Fetches the report data objects now
        Session s = null;
        try {
            s = getActivityDataSource().getSession();
            Query resultQuery = s.createQuery(
                    "select new ReportSummaryResultDO (result.id, result.value, result.count)"
                  + " from StoredQuerySummaryResultDO result"
                  + " where result.query.id=:queryId");
            resultQuery.setLong("queryId", queryId);
            List<?> results = resultQuery.list();
            //why 100?
            assertEquals("The number of queried records should be the same", (nbFetch == -1 ? 100 : nbFetch), results.size());
            Iterator it = results.iterator();
            while (it.hasNext()) {
                Object result = it.next();
                assertTrue("Summary results should come from the right data object", result instanceof ReportSummaryResultDO);
                IReportSummaryResult reportResult = (IReportSummaryResult) result;
                assertEquals("Result data object for summary query should return the right count", new Long(1), reportResult.getCount());
            }
        } catch (HibernateException e) {
            fail("The test threw a Hibernate exception: " + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * This test verifies that summary query results can be stored and queried
     * properly
     */
    public void testQuerySummaryResults() {
        testQuerySummaryResults(-1);
    }

    /**
     * This test verifies that summary query results can be stored and queried
     * properly even when a limit record count is given
     */
    public void testQuerySummaryResultsWithLimitFetch() {
        final int nbFetch = 5;
        assertTrue("The test should be setup properly", nbFetch < NB_REPORTS);
        testQuerySummaryResults(nbFetch);
    }

    /**
     * This test verifies that the 0 number is supported as a maximum row fetch
     * value.
     */
    public void testQuerySummaryResultsWithZeroFetch() {
        testQuerySummaryResults(0);
    }
}