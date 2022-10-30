/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogCustomAttributeDO;

/**
 * This is the base class for the report execution tests.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportExecutionTest.java#1 $
 */

public abstract class BaseReportExecutionTest extends DACContainerSharedTestCase {

    protected SampleDataMgr sampleDataMgr = null;

    /**
     * Constructor
     *  
     */
    public BaseReportExecutionTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public BaseReportExecutionTest(String testName) {
        super(testName);
    }

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
     * Inserts new policy log records without any particular characteristics, so
     * that "query all" can be performed.
     * 
     * @param nbRecords
     *            number of records to insert
     */
    protected void insertPolicyLogRecords(int nbRecords) {
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(1), nbRecords, this.sampleDataMgr.getBasicPolicyLogRecord());
        } catch (HibernateException e) {
            fail("Log records insertion failed: "+e);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Inserts new policy log records with custom attributes
     * 
     * @param nbRecords
     * @param nbAttributes
     */
    protected void insertCustomPolicyLogRecords(int nbRecords, int nbAttributes, TestPolicyActivityLogEntryDO modelDO) {
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            if (modelDO == null){
                this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, 1L, nbRecords, this.sampleDataMgr.getBasicPolicyLogRecord(), 1L, nbAttributes);
            } else {
                this.sampleDataMgr.insertIdenticalPolicyCustomRecords(s, 1L, nbRecords, modelDO, 1L, nbAttributes);
            }
        } catch (HibernateException e) {
            fail("Log records insertion failed: "+e);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts new tracking log records without any particular characteristics
     * 
     * @param nbRecords
     *            number of records to insert
     */
    protected void insertTrackingLogRecords(int nbRecords) {
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalTrackingLogRecords(s, new Long(1), nbRecords, this.sampleDataMgr.getBasicTrackingLogRecord());
        } catch (HibernateException e) {
            fail("Log records insertion failed: "+e);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Inserts new tracking log records with custom attributes
     * 
     * @param nbRecords
     * @param nbAttributes
     */
    protected void insertCustomTrackingLogRecords(int nbRecords, int nbAttributes, TestTrackingActivityLogEntryDO modelDO) {
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            if (modelDO == null){
                this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(1), nbRecords, this.sampleDataMgr.getBasicTrackingLogRecord(), new Long(1), nbAttributes);
            } else {
                this.sampleDataMgr.insertIdenticalTrackingCustomRecords(s, new Long(1), nbRecords, modelDO, new Long(1), nbAttributes);
            }
        } catch (HibernateException e) {
            fail("Log records insertion failed.");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.sampleDataMgr = new SampleDataMgr();
        Session s = getActivityDateSource().getSession();
        Session pfSession = getPolicyDataSource().getSession();
        try {
            //Cleanup cached data
            this.sampleDataMgr.deleteApplications(s);
            this.sampleDataMgr.deletePolicies(s);
            this.sampleDataMgr.deletePolicyLogCustomAttributes(s);
            this.sampleDataMgr.deleteTrackingLogCustomAttributes(s);
            this.sampleDataMgr.deletePolicyLogs(s);
            this.sampleDataMgr.deleteTrackingLogs(s);
            this.sampleDataMgr.deleteHosts(s);
            this.sampleDataMgr.deleteUsersAndGroups(s);
            this.sampleDataMgr.deletePFEntities(pfSession);

            //Create cached data
            this.sampleDataMgr.createApplicationsAndGroups(s);
            this.sampleDataMgr.createPolicies(s);
            this.sampleDataMgr.createHosts(s);
            this.sampleDataMgr.createUsersAndGroups(s);
        } catch (HibernateException e) {
            fail("Unable to cleanup policy log records" + e.getLocalizedMessage());
        } finally {
            HibernateUtils.closeSession(s, null);
            HibernateUtils.closeSession(pfSession, null);
        }
    }

    /**
     * Adds a few dynamic mapping to the data source, in order to insert dummy
     * records in the log table.
     * 
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#setupDataSourceDynamicMappings()
     */
    protected List<Class<?>> setupDataSourceDynamicMappings() {
        List<Class<?>> list = super.setupDataSourceDynamicMappings();
        if (list == null) {
            list = new ArrayList<Class<?>>();
        }
        list.add(TestPolicyActivityLogEntryDO.class);
        list.add(TestTrackingActivityLogEntryDO.class);
        list.add(TestPolicyActivityLogCustomAttributeDO.class);
        list.add(TestTrackingActivityLogCustomAttributeDO.class);
        return list;
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = getActivityDateSource().getSession();
        Session pfSession = getPolicyDataSource().getSession();
        Transaction t = null;
        try {
            this.sampleDataMgr.deleteApplications(s);
            this.sampleDataMgr.deletePolicyLogCustomAttributes(s);
            this.sampleDataMgr.deletePolicyLogs(s);
            this.sampleDataMgr.deletePolicies(s);
            this.sampleDataMgr.deleteTrackingLogCustomAttributes(s);
            this.sampleDataMgr.deleteTrackingLogs(s);
            this.sampleDataMgr.deleteHosts(s);
            this.sampleDataMgr.deleteUsersAndGroups(s);
            this.sampleDataMgr.deletePFEntities(pfSession);
            t = s.beginTransaction();
            s.delete("from StoredQueryDO");
            t.commit();
        } finally {
            HibernateUtils.rollbackTransation(t, null);
            HibernateUtils.closeSession(s, null);
            HibernateUtils.closeSession(pfSession, null);
        }
        super.tearDown();
    }
}
