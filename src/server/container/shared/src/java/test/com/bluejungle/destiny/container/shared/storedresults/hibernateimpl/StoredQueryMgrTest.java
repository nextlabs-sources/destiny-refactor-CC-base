/*
 * Created on Aug 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.util.Date;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseReportExecutionTest;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportExecutionMgrStatelessImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryMgr;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQueryMgrTest.java#1 $
 */

public class StoredQueryMgrTest extends BaseReportExecutionTest {

    /**
     * Returns an instance of the stateless report execution manager
     * 
     * @return an instance of the stateless report execution manager
     */
    protected IStatelessReportExecutionMgr getStatelessReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgr", ReportExecutionMgrStatelessImpl.class.getName(), IStatelessReportExecutionMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        IStatelessReportExecutionMgr reportMgr = (IStatelessReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * Return the stored query manager
     * 
     * @return the stored query manager
     */
    protected StoredQueryMgrImpl getStoredQueryMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IStoredQueryMgr.ACTIVITY_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("storedQueryMgr", StoredQueryMgrImpl.class.getName(), IStoredQueryMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        StoredQueryMgrImpl result = (StoredQueryMgrImpl) compMgr.getComponent(compInfo);
        return result;
    }

    /**
     * This test verifies the class basics.
     */
    public void testStoredQueryMgrClassBasics() {
        final StoredQueryMgrImpl storedQueryMgr = getStoredQueryMgr();
        assertTrue("Stored query manager should implement the right interface", storedQueryMgr instanceof IConfigurable);
        assertTrue("Stored query manager should implement the right interface", storedQueryMgr instanceof IDisposable);
        assertTrue("Stored query manager should implement the right interface", storedQueryMgr instanceof IInitializable);
        assertTrue("Stored query manager should implement the right interface", storedQueryMgr instanceof ILogEnabled);
        assertTrue("Stored query manager should implement the right interface", storedQueryMgr instanceof IStoredQueryMgr);

        //Check the data source
        assertNotNull("The stored query manager should have an activity data source", storedQueryMgr.getActivityDataSource());

        //Test the log setter / getter
        Log log = storedQueryMgr.getLog();
        assertNotNull("The stored query manager should have a log", log);
        storedQueryMgr.setLog(null);
        assertNull("The stored query manager should not have a log", storedQueryMgr.getLog());
        storedQueryMgr.setLog(log);
        assertEquals("The stored query manager should have a log", log, storedQueryMgr.getLog());

        //Test the config setter / getter
        IConfiguration config = storedQueryMgr.getConfiguration();
        assertNotNull("The stored query manager should have a config", config);
        storedQueryMgr.setConfiguration(null);
        assertNull("The stored query manager should not have a config", storedQueryMgr.getConfiguration());
        storedQueryMgr.setConfiguration(config);
        assertEquals("The stored query manager should have a config", config, storedQueryMgr.getConfiguration());
    }

    /**
     * This test verifies that the configuration for the class is set properly
     */
    public void testStoredQueryMgrConfigurationSettings() {
        StoredQueryMgrImpl storedQueryMgr = getStoredQueryMgr();
        assertEquals(new Long(StoredQueryMgrImpl.DEFAULT_DATA_EXPIRATION), new Long(storedQueryMgr.getDataExpirationAmount()));
        assertEquals(new Long(StoredQueryMgrImpl.DEFAULT_SLEEP_AMOUNT), new Long(storedQueryMgr.getSleepAmount()));

        //Test bad configuration
        boolean exThrown = false;
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo("badStoredQueryMgr", StoredQueryMgrImpl.class.getName(), IStoredQueryMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        StoredQueryMgrImpl result = null;
        try {
            compMgr.registerComponent(compInfo, true);
            result = (StoredQueryMgrImpl) compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("The stored query manager class should not accept bad configuration", exThrown);
        exThrown = false;

        //Test good configuration
        config.setProperty(IStoredQueryMgr.ACTIVITY_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        compInfo = new ComponentInfo("okStoredQueryMgr", StoredQueryMgrImpl.class.getName(), IStoredQueryMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        try {
            compMgr.registerComponent(compInfo, true);
            result = (StoredQueryMgrImpl) compMgr.getComponent(compInfo);
            synchronized (result) {
                result.interrupt();
            }
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertFalse("The stored query manager class should accept good configuration", exThrown);

        //Test the sleep time config
        final Long customSleepTime = new Long(456789);
        config.setProperty(IStoredQueryMgr.SLEEP_AMOUNT_CONFIG_PARAM, customSleepTime);
        compInfo = new ComponentInfo("storedQueryMgrWithSleep", StoredQueryMgrImpl.class.getName(), IStoredQueryMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        try {
            compMgr.registerComponent(compInfo, true);
            result = (StoredQueryMgrImpl) compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertFalse("The stored query manager class should accept good configuration", exThrown);
        assertEquals("The custom sleep time duration should be set properly", customSleepTime, new Long(result.getSleepAmount()));
        synchronized (result) {
            result.interrupt();
        }
        assertTrue("Thread should be interrupted", result.isInterrupted());

        //Test the data expiration time config
        final Long customDataExpirationTime = new Long(123456);
        config.setProperty(IStoredQueryMgr.DATA_EXPIRATION_AMOUNT_CONFIG_PARAM, customDataExpirationTime);
        compInfo = new ComponentInfo("storedQueryMgrWithData", StoredQueryMgrImpl.class.getName(), IStoredQueryMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        try {
            compMgr.registerComponent(compInfo, true);
            result = (StoredQueryMgrImpl) compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertFalse("The stored query manager class should accept good configuration", exThrown);
        assertEquals("The custom data expiration time should be set properly", customDataExpirationTime, new Long(result.getDataExpirationAmount()));
        synchronized (result) {
            result.interrupt();
        }
        assertTrue("Thread should be interrupted", result.isInterrupted());
    }

    /**
     * This test verifies that the disposal works properly
     */
    public void testStoredQueryMgrDisposal() {
        StoredQueryMgrImpl storedQueryMgr = getStoredQueryMgr();
        storedQueryMgr.dispose();
        synchronized (storedQueryMgr) {
            assertTrue("Stored query manager should be interrupted after being disposed", storedQueryMgr.isInterrupted());
        }
    }

    /**
     * This test verifies that the result data has been properly deleted.
     */
    public void testStoredQueryMgrResultDataDeletion() throws HibernateException, InvalidReportArgumentException, DataSourceException {
        final int nbPolicyRecords = 100;
        TestPolicyActivityLogEntryDO policyLogEntry = this.sampleDataMgr.getBasicPolicyLogRecord();
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbPolicyRecords, policyLogEntry);
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        //Query for records matching policy 10 or 5
        IReportMgr reportMgr = getReportMgr();
        IStatelessReportExecutionMgr statelessReportExecMgr = getStatelessReportExecutionMgr();
        IReport policyReport = reportMgr.createReport();
        IInquiry inquiry = policyReport.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        IReportResultReader reader = null;
        final int nbReportExecution = 100;
        for (int i = 0; i < nbReportExecution / 2; i++) {
                IStatelessReportExecutionResult result = (IStatelessReportExecutionResult) statelessReportExecMgr.executeReport(policyReport, nbPolicyRecords);
        }
        policyReport.setSummaryType(ReportSummaryType.USER);
        for (int i = 0; i < nbReportExecution / 2; i++) {
                IStatelessReportExecutionResult result = (IStatelessReportExecutionResult) statelessReportExecMgr.executeReport(policyReport, nbPolicyRecords);
        }

        try {
            s = getActivityDateSource().getSession();
            Criteria crit = s.createCriteria(StoredQueryDO.class);
            final int maxResults = 10;
            crit.setMaxResults(maxResults);
            List results = crit.list();
            assertEquals("There should be stored queries in the tables", maxResults, results.size());
            crit = s.createCriteria(StoredQueryByIdResultDO.class);
            crit.setMaxResults(maxResults);
            results = crit.list();
            assertEquals("There should be stored queries by id in the tables", maxResults, results.size());

            crit = s.createCriteria(StoredQuerySummaryResultDO.class);
            crit.setMaxResults(maxResults);
            results = crit.list();
            assertEquals("There should be summary results in the tables", maxResults, results.size());

        } finally {
            HibernateUtils.closeSession(s, null);
        }

        StoredQueryMgrImpl storedQueryMgr = getStoredQueryMgr();
        Date d = new Date(System.currentTimeMillis() + 3600 * 1000 * 25);
        storedQueryMgr.processData(d);

        try {
            s = getActivityDateSource().getSession();
            List storedQueries = s.createCriteria(StoredQueryDO.class).list();
            assertEquals("All stored queries should have been deleted.", 0, storedQueries.size());
            assertEquals("All results by id should have been deleted", 0, s.createCriteria(StoredQueryByIdResultDO.class).list().size());
            assertEquals("All summary results should have been deleted", 0, s.createCriteria(StoredQuerySummaryResultDO.class).list().size());
        } finally {
            HibernateUtils.closeSession(s, null);
        }
        storedQueryMgr.interrupt();
    }
}