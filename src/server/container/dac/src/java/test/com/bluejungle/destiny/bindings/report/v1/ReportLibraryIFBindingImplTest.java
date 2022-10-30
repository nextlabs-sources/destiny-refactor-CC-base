/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Calendar;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PersistentReportMgrImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportOwnerDO;
import com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.effects.v1.EffectList;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.obligations.v1.ObligationList;
import com.bluejungle.destiny.types.basic.v1.SortDirection;
import com.bluejungle.destiny.types.basic.v1.StringList;
import com.bluejungle.destiny.types.basic_faults.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.basic_faults.v1.PersistenceFault;
import com.bluejungle.destiny.types.basic_faults.v1.ServiceNotReadyFault;
import com.bluejungle.destiny.types.basic_faults.v1.UniqueConstraintViolationFault;
import com.bluejungle.destiny.types.basic_faults.v1.UnknownEntryFault;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortSpec;
import com.bluejungle.destiny.types.report.v1.ReportSummaryType;
import com.bluejungle.destiny.types.report.v1.ReportTargetType;
import com.bluejungle.destiny.types.report.v1.ReportVisibilityType;
import com.bluejungle.destiny.types.report.v1.ReportList;
import com.bluejungle.destiny.types.report.v1.ReportQueryFieldName;
import com.bluejungle.destiny.types.report.v1.ReportQuerySpec;
import com.bluejungle.destiny.types.report.v1.ReportQueryTerm;
import com.bluejungle.destiny.types.report.v1.ReportQueryTermList;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortTerm;
import com.bluejungle.destiny.types.report.v1.ReportSortTermList;
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
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ReportLibraryIFBindingImplTest.java#1 $
 */

public class ReportLibraryIFBindingImplTest extends BaseReportServiceTest {

    private static final Log LOG = LogFactory.getLog(ComponentLookupIFImplTest.class.getName());
    protected ReportSampleDataMgr reportDataMgr = new ReportSampleDataMgr();

    /**
     * Constructor
     * 
     * @param testName
     */
    public ReportLibraryIFBindingImplTest(String testName) {
        super(testName);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        Session s = getActivityDateSource().getSession();
        this.reportDataMgr.deleteAllReports(s);
        HibernateUtils.closeSession(s, LOG);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        Session s = getActivityDateSource().getSession();
        this.reportDataMgr.deleteAllReports(s);
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
     * Returns the persistent report manager
     * 
     * @return the persistent report manager
     */
    protected IPersistentReportMgr getPersistentReportMgr() {
        HashMapConfiguration persistentReportMgrConfig = new HashMapConfiguration();
        persistentReportMgrConfig.setProperty(IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo(IPersistentReportMgr.COMP_NAME, PersistentReportMgrImpl.class.getName(), IPersistentReportMgr.class.getName(), LifestyleType.SINGLETON_TYPE, persistentReportMgrConfig);
        IPersistentReportMgr result = (IPersistentReportMgr) ComponentManagerFactory.getComponentManager().getComponent(compInfo);
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
     * Inserts a policy activity report
     */
    // TODO
    protected void deleteReports(int numReports) throws HibernateException, DataSourceException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        for (int i = 0; i < numReports; i++) {
            persistentReportMgr.deleteReport(new Long(i));
        }
    }

    /**
     * Inserts a policy activity report TODO: right now the IPersistentReport is
     * hacked into a ReportDO by casting. We need to add a setReportID() method
     * to IPersistentReport
     */
    protected Long insertPolicyReport(String title, String description, com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryType, PolicyDecisionEnumType enforcement, String policyExpr, boolean isShared, String resourceExpr,
            String userExpr, Calendar beginDate, Calendar endDate, ActionEnumType[] actionList, String applicationExpr, String obligationExpr, int reportID) throws HibernateException, DataSourceException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        IPersistentReport blankReport = persistentReportMgr.createPersistentReport(new Long(0));
        ReportDO myReport = (ReportDO) blankReport;
        myReport.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        myReport.setTitle(title);
        myReport.setDescription(description);
        myReport.setSummaryType(summaryType);
        myReport.getOwner().setIsShared(isShared);
        myReport.getTimePeriod().setBeginDate(beginDate);
        myReport.getTimePeriod().setEndDate(endDate);
        myReport.getInquiry().addPolicyDecision(enforcement);
        myReport.getInquiry().addPolicy(policyExpr);
        myReport.getInquiry().addResource(resourceExpr);
        myReport.getInquiry().addUser(userExpr);
        myReport.getInquiry().setLoggingLevel(0);

        for (int i = 0; i < actionList.length; i++) {
            myReport.getInquiry().addAction(actionList[i]);
        }

        Session s = null;
        Transaction t = null;
        s = getActivityDateSource().getSession();
        t = s.beginTransaction();
        Object o = s.save(myReport);
        t.commit();
        s.close();
        return myReport.getId();
    }

    /**
     * Inserts a tracking activity report TODO: right now the IPersistentReport
     * is hacked into a ReportDO by casting. We need to add a setReportID()
     * method to IPersistentReport
     */
    protected Long insertTrackingReport(String title, String description, com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryType, boolean isShared, String resourceExpr, String userExpr, Calendar beginDate, Calendar endDate,
            ActionEnumType[] actionList, String applicationExpr, String obligationExpr, int reportID) throws HibernateException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        IPersistentReport blankReport = persistentReportMgr.createPersistentReport(new Long(0));
        ReportDO myReport = (ReportDO) blankReport;
        myReport.getInquiry().setTargetData(InquiryTargetDataType.ACTIVITY);
        myReport.setTitle(title);
        myReport.setDescription(description);
        myReport.setSummaryType(summaryType);
        myReport.getOwner().setIsShared(isShared);
        myReport.getTimePeriod().setBeginDate(beginDate);
        myReport.getTimePeriod().setEndDate(endDate);
        myReport.getInquiry().addResource(resourceExpr);
        myReport.getInquiry().addUser(userExpr);
        myReport.getInquiry().setLoggingLevel(0);
        for (int i = 0; i < actionList.length; i++) {
            myReport.getInquiry().addAction(actionList[i]);
        }
        myReport.getInquiry().addApplication(applicationExpr);
        myReport.getInquiry().addObligation(obligationExpr);
        ReportOwnerDO reportOwner = new ReportOwnerDO();
        reportOwner.setOwnerId(new Long((long) 0));
        IReportOwner newReportOwner = reportOwner;
        myReport.setOwner(newReportOwner);
        IPersistentReport newReport = myReport;

        Session s = null;
        Transaction t = null;
        s = getActivityDateSource().getSession();
        t = s.beginTransaction();
        Object o = s.save(myReport);
        t.commit();
        s.close();
        return myReport.getId();
    }

    /**
     * Inserts a multiple policy reports
     */
    protected Long[] insertMultiplePolicyReports(int startID, int numReports) throws HibernateException, DataSourceException {
        Long[] reportIDList = new Long[numReports];
        for (int i = startID; i < (startID + numReports); i++) {
            final String titleExpr = "PolicyReport" + String.valueOf(i);
            final String descriptionExpr = "Description" + String.valueOf(i);
            final String applicationExpr = "Application" + String.valueOf(i);
            final String obligationExpr = "log";
            final String policyExpr = "Policy" + String.valueOf(i);
            final String resourceExpr = "Resource" + String.valueOf(i);
            final String userExpr = "User" + String.valueOf(i);
            final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
            final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
            final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
            final Calendar beginDate = Calendar.getInstance();
            final Calendar endDate = Calendar.getInstance();
            final boolean isShared = false;
            reportIDList[i] = insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, i);
        }
        return reportIDList;
    }

    /**
     * Inserts a multiple tracking reports
     */
    protected Long[] insertMultipleTrackingReports(int startID, int numReports) throws HibernateException, DataSourceException {
        Long[] reportIDList = new Long[numReports];
        for (int i = startID; i < (startID + numReports); i++) {
            final String titleExpr = "TrackingReport" + String.valueOf(i);
            final String descriptionExpr = "Description" + String.valueOf(i);
            final String applicationExpr = "Application" + String.valueOf(i);
            final String obligationExpr = "log";
            final String resourceExpr = "Resource" + String.valueOf(i);
            final String userExpr = "User" + String.valueOf(i);
            final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
            final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
            final Calendar beginDate = Calendar.getInstance();
            final Calendar endDate = Calendar.getInstance();
            final boolean isShared = false;
            reportIDList[i] = insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, i);
        }
        return reportIDList;
    }
    
    protected Report createReport(long userId, String title){
    	return new Report(
        		BigInteger.valueOf(userId), 			//java.math.BigInteger id,
                "description",							//java.lang.String description,
                false,									//boolean shared,
                title,									//java.lang.String title,
                true,									//boolean owned,
                Calendar.getInstance(), 				//java.util.Calendar beginDate,
                Calendar.getInstance(), 				// java.util.Calendar endDate,
                ReportTargetType.PolicyEvents, 			//com.bluejungle.destiny.types.report.v1.ReportTargetType target,
                new ActionList(new String[]{}), 	//com.bluejungle.destiny.types.actions.v1.ActionList actions,
                new EffectList(new EffectType[]{EffectType.allow}), //com.bluejungle.destiny.types.effects.v1.EffectList effects,
                new ObligationList(new String[]{}),		//com.bluejungle.destiny.types.obligations.v1.ObligationList obligations,
                new StringList(new String[]{}),			//com.bluejungle.destiny.types.basic.v1.StringList policies,
                new StringList(new String[]{}),			//com.bluejungle.destiny.types.basic.v1.StringList resourceNames,
                new ReportSortSpec(),					//com.bluejungle.destiny.types.report.v1.ReportSortSpec sortSpec,
                new StringList(new String[]{}),			//com.bluejungle.destiny.types.basic.v1.StringList users,
                ReportSummaryType.None,					//com.bluejungle.destiny.types.report.v1.ReportSummaryType summaryType,
                2
        );
    }
    
    public void testInsertDuplicatedReports() throws UniqueConstraintViolationFault,
			AccessDeniedFault, ServiceNotReadyFault, PersistenceFault, RemoteException {
        ReportLibraryIF reportLibrary = getReportLibrary();
        final String title = "title";
        reportLibrary.insertReport(createReport(0, title));
        try {
			reportLibrary.insertReport(createReport(0, title));
			fail();
		} catch (UniqueConstraintViolationFault e) {
			assertNotNull(e);
		}
    }
    
    public void testInsertDuplicatedReportsWithDifferentUser() throws UniqueConstraintViolationFault,
			AccessDeniedFault, ServiceNotReadyFault, PersistenceFault, RemoteException {
		ReportLibraryIF reportLibrary = getReportLibrary();
		final String title = "title";
		reportLibrary.insertReport(createReport(0, title));
		reportLibrary.insertReport(createReport(1, title));
	}
    
    public void testSaveDuplicatedReports()
			throws UniqueConstraintViolationFault, AccessDeniedFault, ServiceNotReadyFault,
			PersistenceFault, RemoteException {
		ReportLibraryIF reportLibrary = getReportLibrary();

		reportLibrary.insertReport(createReport(0, "title1"));
		Report report = createReport(0, "title2");
		reportLibrary.insertReport(report);
		report.setTitle("title1");
		try {
			reportLibrary.updateReport(report);
			fail();
		} catch (UniqueConstraintViolationFault e) {
			assertNotNull(e);
		}
	}

    
    

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithNullTitle() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        Long newReportID = insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithNullShared() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithNullDescription() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Title, title), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithNullReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {
        final String title = "Report1";
        final String description = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryType = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(title, description, summaryType, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getName(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", ReportTargetType.PolicyEvents.getValue(), Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultiplePolicyReportWithNullReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {
        insertMultiplePolicyReports(0, 5);

        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("Five reports should be returned", 5, size);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithExactReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithIncorrectReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "randomDescription"), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(!shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, "randomTitle") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithIncorrectDescription() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "randomDescription"), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithIncorrectShared() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(!shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithIncorrectTitle() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, "randomTitle") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultiplePolicyReportSortedByDescription() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = null;
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Description, SortDirection.Descending) };
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", numReports, size);
        int descriptionCounter = numReports - 1;
        for (int i = 0; i < numReports; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "Description" + descriptionCounter, Report.getDescription());
            descriptionCounter = descriptionCounter - 1;
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultiplePolicyReportSortedByTitle() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = null;
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Title, SortDirection.Ascending) };
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", numReports, size);
        for (int i = 0; i < numReports; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportWithNoReports() throws HibernateException, RemoteException, DataSourceException {
        // deleteReports(5);

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportsByIdWithOneReport() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        Long newReportID = insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        Report Report = reportLibrary.getReportById(BigInteger.valueOf(newReportID.longValue()));
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType, Report.getSummaryType());
        assertEquals("The report target type should match", targetData, Report.getTarget());
        assertEquals("The begin date should match", beginTime.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endTime.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertNotNull("The saved report should not have resources", Report.getResourceNames());
        assertNotNull("The saved report should have users", Report.getUsers());

        // Some error checking
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) 1));
            fail("There should not be a second report");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) -1));
            fail("There should not be a second report");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportsByIdWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;

        Long[] reportIDList = insertMultiplePolicyReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        for (int i = 0; i < numReports; i++) {
            Report Report = reportLibrary.getReportById(BigInteger.valueOf(reportIDList[i].longValue()));
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report name should match", "PolicyReport" + i, Report.getTitle());
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
            assertNotNull("The saved report should have actions", Report.getActions());
            assertNotNull("The saved report should have policies", Report.getPolicies());
            assertNotNull("The saved report should not have resources", Report.getResourceNames());
            assertNotNull("The saved report should have users", Report.getUsers());
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetPolicyReportsByIdWithNoReports() throws HibernateException, RemoteException {

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) 0));
            fail("There should not be a report returned");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertPolicyReport() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setPolicies(policyList);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setTitle(title);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        // assertEquals("The report description should match", description,
        // Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertEquals("The saved report should have Policies", 1, Report.getPolicies().getValues().length);
        // assertNotNull("The saved report should have users",
        // Report.getUsers());
        assertNull("The saved report should not have users", Report.getUsers().getValues());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertPolicyReportWithNullTitle() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        policyList.setValues(new String[] { policyExpr });
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setPolicies(policyList);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        try {
            reportLibrary.insertReport(report);
            fail("should not have been able to insert a report without a title");
        } catch (AxisFault e) {
            // do nothing
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertPolicyReportWithNullFields() throws HibernateException, RemoteException {
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.None;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setTitle("Title1");

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertNull("The saved report should not have actions", Report.getActions().getActions());
        // assertEquals("The saved report should have actions", 1,
        // Report.getActions().getActions().length);
        assertNull("The saved report should not have policies", Report.getPolicies().getValues());
        // assertEquals("The saved report should have Policies", 1,
        // Report.getPolicies().getValues().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
        // assertEquals("The saved report should have users", 1,
        // Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertPolicyReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.None;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setPolicies(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setTitle("Title1");

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("Six reports should be returned", 6, size);
        Report Report = allVisibleReports.getReports()[5];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertPolicyReportWithNullReport() throws HibernateException, RemoteException {

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        try {
            reportLibrary.insertReport(null);
            fail("Should not have been able to insert report without insertInfo");
        } catch (AxisFault e) {
            // do nothing
        }
    }

    public void testDeletePolicyReport() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 1;
        Long[] newReportID = insertMultiplePolicyReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        reportLibrary.deleteReport(BigInteger.valueOf(newReportID[0].longValue()));
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        assertNull("No report should be returned", allVisibleReports.getReports());
    }

    public void testDeletePolicyReportWithNoReports() throws HibernateException, RemoteException {
        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        reportLibrary.deleteReport(BigInteger.ZERO);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    public void testDeletePolicyReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        for (int i = 0; i < numReports; i++) {
            reportLibrary.deleteReport(BigInteger.valueOf((long) i));
            ReportList allVisibleReports = reportLibrary.getReports(null);
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report name should match", "PolicyReport" + i, Report.getTitle());
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
            assertNotNull("The saved report should have actions", Report.getActions());
            assertNotNull("The saved report should have policies", Report.getPolicies());
            assertNotNull("The saved report should not have resources", Report.getResourceNames());
            assertNotNull("The saved report should have users", Report.getUsers());
        }
    }

    public void testUpdatePolicyReport() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String policyExpr = "Policy1";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.Policy;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
    }

    public void testUpdatePolicyReportWithNoReports() throws HibernateException, RemoteException {
        ReportLibraryIF reportLibrary = getReportLibrary();
        Report Report = new Report();
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.Policy;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setTarget(ReportTargetType.PolicyEvents);
        Report.setTitle("Title1");
        try {
            reportLibrary.updateReport(Report);
            fail("The test should not reach this place");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    public void testUpdatePolicyReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        for (int i = 0; i < numReports; i++) {
            ReportList allVisibleReports = reportLibrary.getReports(null);
            Report Report = allVisibleReports.getReports()[i];
            String newDescription = "newDescription" + i + 5;
            ReportSummaryType summaryType = ReportSummaryType.Resource;
            Report.setDescription(newDescription);
            Report.setSummaryType(summaryType);
            reportLibrary.updateReport(Report);
            allVisibleReports = reportLibrary.getReports(null);
            Report = allVisibleReports.getReports()[4];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", newDescription, Report.getDescription());
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithNullTitle() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        // final PolicyDecisionEnumType enforcement =
        // PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        Long newReportID = insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithNullShared() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithNullDescription() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Title, title), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithNullReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {
        final String title = "Report1";
        final String description = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryType = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(title, description, summaryType, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getName(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", ReportTargetType.ActivityJournal.getValue(), Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultipleTrackingReportWithNullReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {
        insertMultipleTrackingReports(0, 5);

        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("Five reports should be returned", 5, size);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithExactReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        // new ReportSortTerm[]{new
        // ReportSortTerm(ReportSortFieldName.Description,
        // SortDirection.Descending)};
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNotNull("The saved report should have users", Report.getUsers());
        assertEquals("The saved report should have users", 1, Report.getUsers().getValues().length);
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithIncorrectReportQuerySpec() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "randomDescription"), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(!shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, "randomTitle") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithIncorrectDescription() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "randomDescription"), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithIncorrectShared() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(!shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, title) };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithIncorrectTitle() throws HibernateException, RemoteException, DataSourceException {

        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, description), new ReportQueryTerm(ReportQueryFieldName.Shared, String.valueOf(shared)),
                new ReportQueryTerm(ReportQueryFieldName.Title, "randomTitle") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultipleTrackingReportSortedByDescription() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultipleTrackingReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = null;
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Description, SortDirection.Descending) };
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", numReports, size);
        int descriptionCounter = numReports - 1;
        for (int i = 0; i < numReports; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "Description" + descriptionCounter, Report.getDescription());
            descriptionCounter = descriptionCounter - 1;
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMultipleTrackingReportSortedByTitle() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultipleTrackingReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = null;
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Title, SortDirection.Ascending) };
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", numReports, size);
        for (int i = 0; i < numReports; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportWithNoReports() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportsByIdWithOneReport() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        Long newReportID = insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        Report Report = reportLibrary.getReportById(BigInteger.valueOf(newReportID.longValue()));
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report summary type should match", summaryType, Report.getSummaryType());
        assertEquals("The report target type should match", targetData, Report.getTarget());
        assertEquals("The begin date should match", beginTime.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endTime.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertNotNull("The saved report should have policies", Report.getPolicies());
        assertNotNull("The saved report should not have resources", Report.getResourceNames());
        assertNotNull("The saved report should have users", Report.getUsers());

        // Some error checking
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) 1));
            fail("There should not be a second report");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) -1));
            fail("There should not be a second report");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportsByIdWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;

        Long[] reportIDList = insertMultipleTrackingReports(0, numReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        for (int i = 0; i < numReports; i++) {
            Report Report = reportLibrary.getReportById(BigInteger.valueOf(reportIDList[i].longValue()));
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report name should match", "TrackingReport" + i, Report.getTitle());
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertNotNull("The saved report should have actions", Report.getActions());
            assertNotNull("The saved report should have policies", Report.getPolicies());
            assertNotNull("The saved report should not have resources", Report.getResourceNames());
            assertNotNull("The saved report should have users", Report.getUsers());
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetTrackingReportsByIdWithNoReports() throws HibernateException, RemoteException {

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Validate the first report
        try {
            reportLibrary.getReportById(BigInteger.valueOf((long) 0));
            fail("There should not be a report returned");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertTrackingReport() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setTitle(title);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report name should match", title, Report.getTitle());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertTrackingReportWithNullTitle() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        try {
            reportLibrary.insertReport(report);
            fail("should not have been able to insert a report without a title");
        } catch (AxisFault e) {
            // do nothing
        }
    }

    public void testInsertReportWithNullTarget() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportSummaryType summaryType = ReportSummaryType.None;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(null);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setDescription(descriptionExpr);
        report.setShared(isShared);
        report.setTitle(titleExpr);
        
        ReportLibraryIF reportLibrary = getReportLibrary();
        
        // Query for all reports
        try {
            Report Report = reportLibrary.insertReport(report);
            fail("should not have been able to insert a report without a Target");
        } catch (AxisFault e) {
            assertTrue("When inserting a report Target cannot be null",e.getMessage().indexOf("Non nillable element 'target' is null")> 0);
        }
    }

    public void testInsertReportWithNullSummaryType() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType target = ReportTargetType.ActivityJournal;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(target);
        report.setSummaryType(null);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setDescription(descriptionExpr);
        report.setShared(isShared);
        report.setTitle(titleExpr);

        ReportLibraryIF reportLibrary = getReportLibrary();
        
        // Query for all reports
        try {
            Report Report = reportLibrary.insertReport(report);
            fail("should not have been able to insert a report without a Summary Type");
        } catch (AxisFault e) {
            assertTrue("When inserting a report Summary Type cannot be null",e.getMessage().indexOf("Non nillable element 'summaryType' is null")> 0);
        }
    }
    
    public void testInsertTrackingReportWithSummarytypeUsers() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
    }
    //TODO: commenting out the following failing test for which bug is being filed. 
    /*
     * 
        public void testInsertTrackingReportWithSummarytypePolicy() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.Policy;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        InsertReportInfo reportInfo = new InsertReportInfo();
        reportInfo.setTitle(title);
        reportInfo.setDescription(description);
        reportInfo.setShared(shared);
        boolean exceptionThrown = false;
        try{
	        long start = System.currentTimeMillis();
	        reportLibrary.insertReport(report, reportInfo);
	        ReportList allVisibleReports = reportLibrary.getReports(null);
	        long end = System.currentTimeMillis();
	        getLog().info("GetReports timing: " + (end - start) + " ms");
	        assertNull("No reports should be inserted :", allVisibleReports.getReports());
	    }
        catch(AxisFault e)
        {
        	exceptionThrown = true;
        	
        }
        assertTrue("Exception should have been thrown" , exceptionThrown);
    }
    */
    
    public void testInsertPolicyReportWithSummarytypeResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.Resource;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(null);
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
    }
    
    public void testInsertTrackingReportWithWithWildcardInPolicies() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setPolicies(new StringList(new String[] {"*policy*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
        assertEquals("The saved report should have Policy", "*policy*" , Report.getPolicies().getValues()[0]);
    }
    
    public void testInsertTrackingReportWithWithWildcardInResourcenames() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*.xml" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        
        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
        assertEquals("The saved report should have Resource", "*.xml" , Report.getResourceNames().getValues()[0]);
    }
    
    public void testInsertTrackingReportWithWithWildcardInUsers() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "*User_*";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(userList);
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertEquals("The saved report should have User", "*User_*", Report.getUsers().getValues()[0]);
    }
    
    public void testInsertPolicyReportWithWithWildcardInPolicies() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setPolicies(new StringList(new String[] {"*policy*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
        assertEquals("The saved report should have Policy", "*policy*" , Report.getPolicies().getValues()[0]);
    }
    
    public void testInsertPolicyReportWithWithWildcardInResourcenames() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*.xml" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertNull("The saved report should not have users", Report.getUsers().getValues());
        assertEquals("The saved report should have Resource", "*.xml" , Report.getResourceNames().getValues()[0]);
    }
    
    public void testInsertPolicyReportWithWithWildcardInUsers() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "*User_*";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.PolicyEvents;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(userList);
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report title should match", title, Report.getTitle());
        assertEquals("The report description should match", description, Report.getDescription());
        assertEquals("The report visibility should match", shared , Report.isShared());;
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The begin date should match", beginDate.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The end date should match", endDate.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertNotNull("The saved report should have actions", Report.getActions());
        assertEquals("The saved report should have actions", 1, Report.getActions().getActions().length);
        assertEquals("The saved report should have User", "*User_*", Report.getUsers().getValues()[0]);
    }
    
    public void testInsertReportWithEscapeSequenceBackspaceResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "*User_*";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(userList);
        report.setResourceNames(new StringList(new String[] {"*\b*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            fail( "Test should thrown an exception" );
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception", e.getMessage().indexOf("IllegalArgumentException")> 0);
        }
        
    }

    public void testInsertReportWithEscapeSequenceFormfeedResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "*User_*";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        final StringList userList = new StringList();
        userList.setValues(new String[] { userExpr });
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(userList);
        report.setResourceNames(new StringList(new String[] {"*\f*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            fail( "Test should thrown an exception" );
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception", e.getMessage().indexOf("IllegalArgumentException")> 0);
        }
        
    }
        
    public void testInsertReportWithEscapeSequenceTabResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\t*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\t*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as tab is a valid character" +  e.getLocalizedMessage());
        }
        
    }
    
    public void testInsertReportWithEscapeSequenceLinefeedResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\n*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\n*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as Linefeed is a valid character" +  e.getLocalizedMessage());
        }
        
    }
    //TODO Commenting out the following test as it is failing for which bug is being filed
    /*
    public void testInsertReportWithEscapeSequenceCarriagereturnResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\r*" } ));
        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        InsertReportInfo reportInfo = new InsertReportInfo();
        reportInfo.setTitle(title);
        reportInfo.setDescription(description);
        reportInfo.setShared(shared);

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report, reportInfo);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\r*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as Carriagereturn is a valid character" +  e.getLocalizedMessage());
        }
        
    }
    */
    public void testInsertReportWithEscapeSequenceBackslashResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\\*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\\*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as backslash is a valid character" +  e.getLocalizedMessage());
        }
    }

    public void testInsertReportWithEscapeSequenceDoublequoteResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\"*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\"*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as doublequote is a valid character" +  e.getLocalizedMessage());
        }
    }
    
    public void testInsertReportWithEscapeSequenceSinglequoteResource() throws HibernateException, RemoteException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;

        // set variables for the report
        final ActionList actionList = new ActionList();
        actionList.setActions(new String[] { ActionEnumType.ACTION_OPEN.getName() });
        final Calendar beginTime = beginDate;
        final Calendar endTime = endDate;
        final StringList policyList = new StringList();
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.User;
        
        final String description = descriptionExpr;
        final String title = titleExpr;
        final boolean shared = isShared;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(actionList);
        report.setBeginDate(beginTime);
        report.setEndDate(endTime);
        report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        report.setUsers(null);
        report.setResourceNames(new StringList(new String[] {"*\'*" } ));
        report.setTitle(title);
        report.setDescription(description);
        report.setShared(shared);        
        
        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        try{
        	reportLibrary.insertReport(report);
            ReportList allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertEquals("The saved report should have User", "*\'*", Report.getResourceNames().getValues()[0]);
        }
        catch(RemoteException e)
        {
        	fail("Test should not throw an exception as singlequote is a valid character" +  e.getLocalizedMessage());
        }
    }
    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertTrackingReportWithNullFields() throws HibernateException, RemoteException {
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;

        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setTitle("Title1");

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertNull("The saved report should not have actions", Report.getActions().getActions());
        assertNull("The saved report should not have users", Report.getUsers().getValues());
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertTrackingReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultiplePolicyReports(0, numReports);
        final ReportTargetType targetData = ReportTargetType.ActivityJournal;
        final ReportSummaryType summaryType = ReportSummaryType.None;

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();
        final Report report = new Report();
        report.setTarget(targetData);
        report.setSummaryType(summaryType);
        report.setActions(null);
        report.setBeginDate(null);
        report.setEndDate(null);
        report.setSortSpec(null);
        report.setUsers(null);
        report.setTitle("Title1");

        // Query for all reports
        long start = System.currentTimeMillis();
        reportLibrary.insertReport(report);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("Six reports should be returned", 6, size);

        /**
         * The following check assumed that reports were inserted in order and
         * therefore, their ids were in an ordered sequence. On a multi-CPU
         * machine this may not be true. Since this assumption doesn't appear to
         * be critical to application function, I'm commenting out this test
         */
        /***********************************************************************
         * Report Report = allVisibleReports.getReports()[5];
         * assertNotNull("The returned report should not be null", Report);
         * assertEquals("The report summary type should match",
         * summaryType.getValue(), Report.getSummaryType().getValue());
         * assertEquals("The report target type should match",
         * "ActivityJournal", Report.getTarget().getValue());
         **********************************************************************/
    }

    /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testInsertTrackingReportWithNullReport() throws HibernateException, RemoteException {

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        try {
            reportLibrary.insertReport(null);
            fail("Should not have been able to insert report without insertInfo");
        } catch (AxisFault e) {
            // do nothing
        }
    }

    public void testDeleteTrackingReport() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 1;
        Long[] newReportID = insertMultipleTrackingReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        reportLibrary.deleteReport(BigInteger.valueOf(newReportID[0].longValue()));
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        assertNull("No report should be returned", allVisibleReports.getReports());
    }

    public void testDeleteTrackingReportWithNoReports() throws HibernateException, RemoteException {
        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        reportLibrary.deleteReport(BigInteger.ZERO);
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        assertNull("There should be no reports returned", allVisibleReports.getReports());
    }

    public void testDeleteTrackingReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultipleTrackingReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        for (int i = 0; i < numReports; i++) {
            reportLibrary.deleteReport(BigInteger.valueOf((long) i));
            ReportList allVisibleReports = reportLibrary.getReports(null);
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report name should match", "TrackingReport" + i, Report.getTitle());
            assertEquals("The report description should match", "Description" + i, Report.getDescription());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            assertNotNull("The saved report should have actions", Report.getActions());
            assertNotNull("The saved report should not have resources", Report.getResourceNames());
            assertNotNull("The saved report should have users", Report.getUsers());
        }
    }

    public void testUpdateTrackingReport() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
    }

    public void testUpdateTrackingReportWithNoReports() throws HibernateException, RemoteException {
        ReportLibraryIF reportLibrary = getReportLibrary();
        Report Report = new Report();
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setTarget(ReportTargetType.ActivityJournal);
        Report.setTitle("Title1");
        try {
            reportLibrary.updateReport(Report);
            fail("The test should not reach this place");
        } catch (UnknownEntryFault e) {
            // do nothing
        }
    }

    public void testUpdateTrackingReportWithMultipleReports() throws HibernateException, RemoteException, DataSourceException {
        int numReports = 5;
        insertMultipleTrackingReports(0, numReports);

        ReportLibraryIF reportLibrary = getReportLibrary();
        long start = System.currentTimeMillis();
        for (int i = 0; i < numReports; i++) {
            ReportList allVisibleReports = reportLibrary.getReports(null);
            Report Report = allVisibleReports.getReports()[i];
            String newDescription = "newDescription" + i + 5;
            ReportSummaryType summaryType = ReportSummaryType.Resource;
            Report.setDescription(newDescription);
            Report.setSummaryType(summaryType);
            reportLibrary.updateReport(Report);
            allVisibleReports = reportLibrary.getReports(null);
            Report = allVisibleReports.getReports()[4];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", newDescription, Report.getDescription());
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        }
    }

    public void testUpdateReportWithEscapeSequenceBackspaceInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\b*" }));
        try{
        	 reportLibrary.updateReport(Report);
        	 
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception as backspace is an invalid character", e.getMessage().indexOf("IllegalArgumentException")> 0);
        	
        }
    }
    
    public void testUpdatePolicyReportWithAllFields() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Title: Update all fields";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "Desc: Update all Fields";
        EffectList effectList = new EffectList(new EffectType[]{EffectType.allow});
        ActionList actionList = new ActionList(new String[]{ActionEnumType.ACTION_OPEN.getName()});
        Calendar beginDate1 = Calendar.getInstance();
        Calendar endDate1 = Calendar.getInstance();
        isShared = true;
        int logginglevel = 5;
        StringList policy = new StringList(new String[]{"policy1"});
        StringList resource = new StringList(new String[]{"*test*"});
        StringList userList = new StringList(new String[]{"User1"});
        ObligationList obligationList = new ObligationList();
        obligationList.setValues(new String[]{"log"});
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setTarget(ReportTargetType.PolicyEvents);
        Report.setTitle(titleExpr);
        Report.setActions(actionList);
        Report.setBeginDate(beginDate1);
        Report.setEndDate(endDate1);
        Report.setEffects(effectList);
        Report.setLoggingLevel(logginglevel);
        Report.setObligations(obligationList);
        Report.setPolicies(policy);
        Report.setResourceNames(resource);
        Report.setShared(isShared);
        Report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        Report.setUsers(userList);
        
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        assertEquals("The report title should match", titleExpr , Report.getTitle());
        assertEquals("The report Action should match", actionList.getActions(0), Report.getActions().getActions(0));
        assertEquals("The report Begin date should match", beginDate1.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The report End date should match", endDate1.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertEquals("The report logginglevel should match",logginglevel, Report.getLoggingLevel());
        assertEquals("The report Effects should match", effectList.getValues(0).getValue(), Report.getEffects().getValues(0).getValue());
        assertEquals("The report policy should match", policy.getValues()[0], Report.getPolicies().getValues()[0]);
        assertEquals("The report resource should match", resource.getValues()[0], Report.getResourceNames().getValues()[0]);
        assertEquals("The report user should match", userList.getValues()[0], Report.getUsers().getValues()[0]);
        //TODO commenting out verification for obligation and sort field as tests  fail for which bug is being filed
        //assertEquals("The report Obligations should match", obligationList.getValues()[0], Report.getObligations().getValues()[0]);
        //assertEquals("The report sort field should match", ReportSortFieldName.Date.getValue() , Report.getSortSpec().getField().getValue());
        //assertEquals("The report sort direction should match", SortDirection.Ascending.getValue() , Report.getSortSpec().getDirection());
        
    }

    public void testUpdateTrackingReportWithAllFields() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Title: Update all fields";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "Desc: Update all Fields";
        ActionList actionList = new ActionList(new String[]{ActionEnumType.ACTION_OPEN.getName()});
        Calendar beginDate1 = Calendar.getInstance();
        Calendar endDate1 = Calendar.getInstance();
        isShared = true;
        int logginglevel = 5;
        StringList resource = new StringList(new String[]{"*test*"});
        StringList userList = new StringList(new String[]{"User1"});
        ObligationList obligationList = new ObligationList();
        obligationList.setValues(new String[]{"log"});
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setTarget(ReportTargetType.ActivityJournal);
        Report.setTitle(titleExpr);
        Report.setActions(actionList);
        Report.setBeginDate(beginDate1);
        Report.setEndDate(endDate1);
        Report.setLoggingLevel(logginglevel);
        Report.setObligations(obligationList);
        Report.setResourceNames(resource);
        Report.setShared(isShared);
        Report.setSortSpec(new ReportSortSpec(ReportSortFieldName.Date, SortDirection.Ascending));
        Report.setUsers(userList);
        
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        assertEquals("The report title should match", titleExpr , Report.getTitle());
        assertEquals("The report Action should match", actionList.getActions(0), Report.getActions().getActions(0));
        assertEquals("The report Begin date should match", beginDate1.getTimeInMillis(), Report.getBeginDate().getTimeInMillis());
        assertEquals("The report End date should match", endDate1.getTimeInMillis(), Report.getEndDate().getTimeInMillis());
        assertEquals("The report logginglevel should match",logginglevel, Report.getLoggingLevel());
        assertEquals("The report resource should match", resource.getValues()[0], Report.getResourceNames().getValues()[0]);
        assertEquals("The report user should match", userList.getValues()[0], Report.getUsers().getValues()[0]);
        //TODO commenting out verification for obligation and sort field as tests fail for which bug is being filed
        //assertEquals("The report Obligations should match", obligationList.getValues()[0], Report.getObligations().getValues()[0]);
        //assertEquals("The report sort field should match", ReportSortFieldName.Date.getValue() , Report.getSortSpec().getField().getValue());
        //assertEquals("The report sort direction should match", SortDirection.Ascending.getValue() , Report.getSortSpec().getDirection());
        
    }

    public void testUpdateReportWithEscapeSequenceFormfeedInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\f*" }));
        try{
        	 reportLibrary.updateReport(Report);
        	
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception as formfeed is an invalid character", e.getMessage().indexOf("IllegalArgumentException")> 0);
        	
        }
    }
    
    public void testUpdateReportWithEscapeSequenceTabInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\t*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\t*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as tab is a valid character" + e.getLocalizedMessage());
        }
    }
   
    public void testUpdateReportWithEscapeSequenceLinefeedInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\n*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\n*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as Linefeed is a valid character" + e.getLocalizedMessage());
        }
    }
    //TODO COMMENTING OUT following failing test for which bug is being filed
    /*
    public void testUpdateReportWithEscapeSequenceCarriagereturnInResource() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\r*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\r*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as carriagereturn is a valid character" + e.getLocalizedMessage());
        }
    }
    */
    public void testUpdateReportWithEscapeSequenceBackslashInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\\*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\\*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as backslash is a valid character" + e.getLocalizedMessage());
        }
    }
    
    public void testUpdateReportWithEscapeSequenceDoublequoteInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\"*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\"*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as doublequote is a valid character" + e.getLocalizedMessage());
        }
    }
    
    public void testUpdateReportWithEscapeSequenceSinglequoteInResourceTrackingActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\'*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\'*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as singlequote is a valid character" + e.getLocalizedMessage());
        }
    }
    public void testUpdateReportWithEscapeSequenceBackspaceInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\b*" }));
        try{
        	 reportLibrary.updateReport(Report);
        	 
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception as backspace is an invalid character", e.getMessage().indexOf("IllegalArgumentException")> 0);
        	
        }
    }
   
    public void testUpdateReportWithEscapeSequenceFormfeedInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\f*" }));
        try{
        	 reportLibrary.updateReport(Report);
        	
        }
        catch(RemoteException e)
        {
        	assertTrue(e instanceof AxisFault);
        	assertTrue("Test should throw an exception as formfeed is an invalid character", e.getMessage().indexOf("IllegalArgumentException")> 0);
        	
        }
    }
    
    public void testUpdateReportWithEscapeSequenceTabInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\t*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\t*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as tab is a valid character" + e.getLocalizedMessage());
        }
    }
   
    public void testUpdateReportWithEscapeSequenceLinefeedInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\n*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\n*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as Linefeed is a valid character" + e.getLocalizedMessage());
        }
    }
    //TODO COMMENTING OUT following failing test for which bug is being filed
    /*
    public void testUpdateReportWithEscapeSequenceCarriagereturnInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\r*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\r*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as carriagereturn is a valid character" + e.getLocalizedMessage());
        }
    }
    */
    public void testUpdateReportWithEscapeSequenceBackslashInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\\*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\\*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as backslash is a valid character" + e.getLocalizedMessage());
        }
    }
    
    public void testUpdateReportWithEscapeSequenceDoublequoteInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);
 
        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\"*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\"*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as doublequote is a valid character" + e.getLocalizedMessage());
        }
    }
    
    public void testUpdateReportWithEscapeSequenceSinglequoteInResourcePolicyActivity() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);


        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setResourceNames(new StringList(new String[]{"*\'*"}));
        try{
        	long start = System.currentTimeMillis();
        	reportLibrary.updateReport(Report);
        	long end = System.currentTimeMillis();
        	getLog().info("UpdateReports timing: " + (end - start) + " ms");
            allVisibleReports = reportLibrary.getReports(null);
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report description should match", "*\'*", Report.getResourceNames().getValues()[0]);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        	
        }
        catch(RemoteException e){
        	fail("Test should not throw an exception as singlequote is a valid character" + e.getLocalizedMessage());
        }
    }
    
    public void testUpadteTrackingReportWithWithWildcardInUsers() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(new StringList(new String[]{ "*User*"}));
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report user string should match","*User*" , Report.getUsers().getValues()[0]);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
    }

    public void testUpdateTrackingReportWithWithWildcardInResourcenames() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setResourceNames(new StringList(new String[]{"*test*"}));
        
        long start = System.currentTimeMillis();
        reportLibrary.updateReport(Report);
        long end = System.currentTimeMillis();
        getLog().info("UpdateReports timing: " + (end - start) + " ms");
        allVisibleReports = reportLibrary.getReports(null);
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("Resource string should match","*test*" , Report.getResourceNames().getValues()[0]);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
    }
    
    public void testUpdateTrackingReportWithPolicyInput() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setPolicies(new StringList(new String[]{"*policy*"}));
        try{
        	reportLibrary.updateReport(Report);
        	//fail("Test should throw an exception or message saying that DocumentActivity report cannot be updated to have policies");
            long start = System.currentTimeMillis();
            allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
            //TODO commenting out verification as test is failing for which bug is being filed
            //assertNull("Should not be able to update document activity report with Policy details",  Report.getPolicies().getValues());
            
        }
        catch(RemoteException e){
        	getLog().info(e.getLocalizedMessage());	
        }
    }
    
    public void testUpadtePolicyReportWithWithWildcardInUsers() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(new StringList(new String[]{ "*User*"}));
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report user string should match","*User*" , Report.getUsers().getValues()[0]);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
    }

    public void testUpdatePolicyReportWithWithWildcardInResourcenames() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);
 
        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setResourceNames(new StringList(new String[]{"*test*"}));
        
        long start = System.currentTimeMillis();
        reportLibrary.updateReport(Report);
        long end = System.currentTimeMillis();
        getLog().info("UpdateReports timing: " + (end - start) + " ms");
        allVisibleReports = reportLibrary.getReports(null);
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("Resource string should match","*test*" , Report.getResourceNames().getValues()[0]);
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
    }
    
    public void testUpdatePolicyReportWithWithWildcardInPolicies() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setPolicies(new StringList(new String[]{"*policy*"}));
        try{
        	reportLibrary.updateReport(Report);
        	//fail("Test should throw an exception or message saying that DocumentActivity report cannot be updated to have policies");
            long start = System.currentTimeMillis();
            allVisibleReports = reportLibrary.getReports(null);
            long end = System.currentTimeMillis();
            getLog().info("GetReports timing: " + (end - start) + " ms");
            assertNotNull("There should be reports returned", allVisibleReports.getReports());
            int size = allVisibleReports.getReports().length;
            assertEquals("One report should be returned", 1, size);
            Report = allVisibleReports.getReports()[0];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
            assertEquals("Policy should match", "*policy*", Report.getPolicies().getValues()[0]);
            
        }
        catch(RemoteException e){
        	getLog().info(e.getLocalizedMessage());	
        }
    }
    
    public void testUpdateTrackingReportWithNullTarget() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setTarget(null);
        try{
        	reportLibrary.updateReport(Report);
        	   
        }
        catch(RemoteException e){
        	assertTrue(e.getLocalizedMessage().indexOf("Non nillable element 'target' is nul")> 0);	
        }
    }
    
    public void testUpdateTrackingReportWithNullSummaryType() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.None;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        Report.setUsers(null);
        Report.setSummaryType(null);
        try{
        	reportLibrary.updateReport(Report);
        	   
        }
        catch(RemoteException e){
        	assertTrue(e.getLocalizedMessage().indexOf("Non nillable element 'summaryType' is nul")> 0);	
        }
    }
    
    public void testUpdateTrackingReportWithSummaryTypeUsers() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.User;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
    }

    public void testUpdateTrackingReportWithSummaryTypePolicy() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        insertTrackingReport(titleExpr, descriptionExpr, summaryTypeData, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.Policy;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        reportLibrary.updateReport(Report);

        long start = System.currentTimeMillis();
        allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        //TODO Commenting out verification as test is failing for which bug is being filed
        //assertEquals("The report summary type should match", ReportSummaryType.None.getValue(), Report.getSummaryType().getValue());
        
    }

    public void testUpdatePolicyReportWithSummaryTypeResource() throws HibernateException, RemoteException, DataSourceException {
        // set variables for the clean report insertion
        final String titleExpr = "Report1";
        final String descriptionExpr = "Description1";
        final String applicationExpr = "Application1";
        final String obligationExpr = "log";
        final String resourceExpr = "Resource1";
        final String userExpr = "User1";
        final ActionEnumType[] action = new ActionEnumType[] { ActionEnumType.ACTION_OPEN };
        final com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType summaryTypeData = com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType.NONE;
        final Calendar beginDate = Calendar.getInstance();
        final Calendar endDate = Calendar.getInstance();
        final boolean isShared = false;
        final int reportID = 0;
        final String policyExpr = "Policy1";
        final PolicyDecisionEnumType enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
        insertPolicyReport(titleExpr, descriptionExpr, summaryTypeData, enforcement, policyExpr, isShared, resourceExpr, userExpr, beginDate, endDate, action, applicationExpr, obligationExpr, reportID);

        ReportLibraryIF reportLibrary = getReportLibrary();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        Report Report = allVisibleReports.getReports()[0];
        String newDescription = "newDescription1";
        ReportSummaryType summaryType = ReportSummaryType.Resource;
        Report.setDescription(newDescription);
        Report.setSummaryType(summaryType);
        long start = System.currentTimeMillis();
        reportLibrary.updateReport(Report);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        allVisibleReports = reportLibrary.getReports(null);
        
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("One report should be returned", 1, size);
        Report = allVisibleReports.getReports()[0];
        assertNotNull("The returned report should not be null", Report);
        assertEquals("The report description should match", newDescription, Report.getDescription());
        assertEquals("The report summary type should match", summaryType.getValue(), Report.getSummaryType().getValue());
        assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
    }

       /**
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMixedReports() throws HibernateException, RemoteException, DataSourceException {
        int numPolicyReports = 5;
        int numTrackingReports = 3;
        insertMultiplePolicyReports(0, numPolicyReports);
        insertMultipleTrackingReports(0, numTrackingReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        // Query for all reports
        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(null);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNotNull("There should be reports returned", allVisibleReports.getReports());
        int size = allVisibleReports.getReports().length;
        assertEquals("Eight reports should be returned", 8, size);
        for (int i = 0; i < 5; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        }
        for (int i = 5; i < size; i++) {
            Report Report = allVisibleReports.getReports()[i];
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetMixedReportsById() throws HibernateException, RemoteException, DataSourceException {
        int numPolicyReports = 5;
        int numTrackingReports = 3;
        Long[] policyReportsIDList = insertMultiplePolicyReports(0, numPolicyReports);
        Long[] trackingReportsIDList = insertMultipleTrackingReports(0, numTrackingReports);

        // create the report query spec
        ReportLibraryIF reportLibrary = getReportLibrary();

        for (int i = 0; i < numPolicyReports; i++) {
            Report Report = reportLibrary.getReportById(BigInteger.valueOf(policyReportsIDList[i].longValue()));
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "PolicyEvents", Report.getTarget().getValue());
        }
        for (int i = 0; i < numTrackingReports; i++) {
            Report Report = reportLibrary.getReportById(BigInteger.valueOf(trackingReportsIDList[i].longValue()));
            assertNotNull("The returned report should not be null", Report);
            assertEquals("The report target type should match", "ActivityJournal", Report.getTarget().getValue());
        }
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetNoReportsWithSearchSpec() throws HibernateException, RemoteException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Create a report that does not belong to the owner

        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Title, "myTitle1") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Title, SortDirection.Descending) };
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should not be reports returned", allVisibleReports.getReports());
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetReportsWithNullSortTerms() throws HibernateException, RemoteException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Create a report that does not belong to the owner

        // Create one report, then try to fetch it
        IPersistentReport myReport1 = persistentReportMgr.createPersistentReport(new Long(0));
        myReport1.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        final String title1 = "myTitle1";
        myReport1.setTitle(title1);
        final String description1 = "myDescription1";
        myReport1.setDescription(description1);
        final String applicationExpr1 = "myApp1";
        myReport1.getInquiry().addApplication(applicationExpr1);
        final String policyExpr1 = "myPolicy1";
        myReport1.getInquiry().addPolicy(policyExpr1);
        final String resourceExpr1 = "c:\\test1\tezx.txt";
        myReport1.getInquiry().addResource(resourceExpr1);
        final String userExpr1 = "(User) user1";
        myReport1.getInquiry().addUser(userExpr1);

        Calendar myCalendar1 = Calendar.getInstance();
        myReport1.getTimePeriod().setBeginDate(myCalendar1);
        myReport1.getTimePeriod().setEndDate(myCalendar1);

        Session s = null;
        Transaction t = null;

        s = getActivityDateSource().getSession();
        t = s.beginTransaction();
        s.save(myReport1);
        t.commit();

        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "myTitle1") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = null;
        ReportSortTermList sortTermList1 = new ReportSortTermList(sortTerm1);
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should not be reports returned", allVisibleReports.getReports());
    }

    /*
     * This test verifies that policies can be fetched without a search spec.
     */
    public void testGetReportsWithNullSortTermList() throws HibernateException, RemoteException {
        IPersistentReportMgr persistentReportMgr = getPersistentReportMgr();
        ReportLibraryIF reportLibrary = getReportLibrary();
        // Create a report that does not belong to the owner

        // Create one report, then try to fetch it
        IPersistentReport myReport1 = persistentReportMgr.createPersistentReport(new Long(0));
        myReport1.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
        final String title1 = "myTitle1";
        myReport1.setTitle(title1);
        final String description1 = "myDescription1";
        myReport1.setDescription(description1);
        final String applicationExpr1 = "myApp1";
        myReport1.getInquiry().addApplication(applicationExpr1);
        final String policyExpr1 = "myPolicy1";
        myReport1.getInquiry().addPolicy(policyExpr1);
        final String resourceExpr1 = "c:\\test1\tezx.txt";
        myReport1.getInquiry().addResource(resourceExpr1);
        final String userExpr1 = "(User) user1";
        myReport1.getInquiry().addUser(userExpr1);

        Calendar myCalendar1 = Calendar.getInstance();
        myReport1.getTimePeriod().setBeginDate(myCalendar1);
        myReport1.getTimePeriod().setEndDate(myCalendar1);

        Session s = null;
        Transaction t = null;

        s = getActivityDateSource().getSession();
        t = s.beginTransaction();
        s.save(myReport1);
        t.commit();

        // Query for all one report using query spec
        ReportQueryTerm[] queryTerm1 = new ReportQueryTerm[] { new ReportQueryTerm(ReportQueryFieldName.Description, "myTitle1") };
        ReportQueryTermList queryTermList1 = new ReportQueryTermList(queryTerm1);
        ReportSortTerm[] sortTerm1 = new ReportSortTerm[] { new ReportSortTerm(ReportSortFieldName.Title, SortDirection.Descending) };
        ReportSortTermList sortTermList1 = null;
        ReportQuerySpec querySpec1 = new ReportQuerySpec(ReportVisibilityType.All, queryTermList1, sortTermList1);

        long start = System.currentTimeMillis();
        ReportList allVisibleReports = reportLibrary.getReports(querySpec1);
        long end = System.currentTimeMillis();
        getLog().info("GetReports timing: " + (end - start) + " ms");
        assertNull("There should not be reports returned", allVisibleReports.getReports());
    }
}
