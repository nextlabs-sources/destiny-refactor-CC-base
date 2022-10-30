/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.hibernateimpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportAccessException;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportVisibilityType;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PersistentReportMgrImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;

/**
 * This is the user report manager component test class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/inquiry/components/userreportmgr/hibernateimpl/UserReportMgrImplTest.java#1 $
 */

public class UserReportMgrImplTest extends BaseDACComponentTestCase {

    private static final Log LOG = LogFactory.getLog(UserReportMgrImplTest.class.getName());

    /**
     * Constructor
     * 
     * @param testName
     */
    public UserReportMgrImplTest(String testName) {
        super(testName);
    }

    /**
     * Creates a persistent report and saves it
     * 
     * @param ownerId
     *            id of the report owner
     * @param shared
     *            true if the report should be shared, false otherwise
     * @return the id of the report that was created
     */
    private Long createPersistentReport(final Long ownerId, boolean shared, String title) {
		IPersistentReport report = createAndSavePersistentReport(ownerId, shared, title);
		return report.getId();
	}
    
    private IPersistentReport createAndSavePersistentReport(final Long ownerId, boolean shared,
			String title) {
        Session s = null;
        Transaction t = null;
        IPersistentReport report = null;
        try {
            s = getActivityDataSource().getSession();
            IPersistentReportMgr reportMgr = getPersistentReportMgr();
            
            report = reportMgr.createPersistentReport(ownerId);
            if (title == null){
                report.setTitle("dummyTitle");
            } else {
                report.setTitle(title);
            }
            report.setDescription("dummyDescription");
            report.getOwner().setIsShared(shared);
            IPersistentInquiry inquiry = (IPersistentInquiry) report.getInquiry();
            inquiry.setTargetData(InquiryTargetDataType.POLICY);
            t = s.beginTransaction();
            s.save(report);
            t.commit();
        } catch (HibernateException e) {
            fail("No hibernate exception should be thrown");
            HibernateUtils.rollbackTransation(t, LOG);
        } finally {
            HibernateUtils.closeSession(s, LOG);
        }
        return report;
    }
    

    /**
     * Deletes all the reports in the database.
     */
    protected void deleteReports() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            s.delete("from ReportDO");
            t.commit();
        } catch (HibernateException e) {
            fail("No error should occur when deleting persistent reports");
            HibernateUtils.rollbackTransation(t, LOG);
        } finally {
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * Returns an instance of the persistent report manager
     * 
     * @return an instance of the persistent report manager
     */
    private IPersistentReportMgr getPersistentReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo compInfo =
				new ComponentInfo("persistentReportMgr", PersistentReportMgrImpl.class.getName(),
						IPersistentReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IPersistentReportMgr reportMgr = (IPersistentReportMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * Returns the user report manager
     * 
     * @return the user report manager
     */
    protected IUserReportMgr getUserReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration userReportMgrConfig = new HashMapConfiguration();
        userReportMgrConfig.setProperty(IUserReportMgr.DATASOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo compInfo =
				new ComponentInfo(IUserReportMgr.COMP_NAME, UserReportMgrImpl.class.getName(),
						IUserReportMgr.class.getName(), LifestyleType.SINGLETON_TYPE,
						userReportMgrConfig);
		IUserReportMgr result = (IUserReportMgr) compMgr.getComponent(compInfo);
        return result;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        deleteReports();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteReports();
        super.tearDown();
    }

    /**
     * This test verifies the getReport API
     */
    public void testGetReport() {
        final Long ownerId = new Long(0);
        final Long badOwnerId = new Long(1);

        //Create one non shared and one shared report
        final Long reportId = createPersistentReport(ownerId, false, null);
        Long unknownReportId = new Long(reportId.longValue() + 1);

        IUserReportMgr userReportMgr = getUserReportMgr();
        IPersistentReport report = null;
        try {
            report = userReportMgr.getReport(reportId, ownerId);
        } catch (ReportAccessException e) {
            fail("No report access exception should be thrown");
        }
        assertNotNull("There should be a report returned", report);
        assertEquals("The report Id should be correct", reportId, report.getId());
        assertNotNull("There should be a default sort spec on the report", report.getSortSpec());

        //Test the case where another user tries to get the report
        boolean exThrown = false;
        try {
            report = userReportMgr.getReport(reportId, badOwnerId);
        } catch (ReportAccessException e) {
            exThrown = true;
        }
        assertTrue("The getReport API should enforce the report owner", exThrown);

        //Test the case where the report does not exist
        exThrown = false;
        try {
            report = userReportMgr.getReport(unknownReportId, ownerId);
        } catch (ReportAccessException e) {
            exThrown = true;
        }
        assertNull("No report should be returned", report);
        assertFalse("The getReport API should not throw an exception if the report does not exist", exThrown);
    }

    /**
     * This test verifies that the visibility "all" is properly applied. When
     * "All" is set, all reports that are shared by other people, and the
     * personnal ones, are returned.
     */
    public void testGetReportsWithAllVisibility() {
        final Long ownerId = new Long(0);
        final Long otherOwnerId = new Long(1);
        final Set<Long> matchingOwnerReports = new HashSet<Long>();

        //Create one non shared and one shared report
        matchingOwnerReports.add(createPersistentReport(ownerId, false, "dummyTitle1"));
        matchingOwnerReports.add(createPersistentReport(ownerId, true, "dummyTitle2"));
        createPersistentReport(otherOwnerId, false, "dummyTitle3");
        matchingOwnerReports.add(createPersistentReport(otherOwnerId, true, "dummyTitle4"));

        IUserReportMgr userReportMgr = getUserReportMgr();
        //Query for all reports for that owner id. All is implicit here
        List<IPersistentReport> result = userReportMgr.getReports(null, ownerId);
        assertEquals("The report query with implicit 'all' visibility should return the right records", 
        		matchingOwnerReports.size(), result.size());
        
        for(IPersistentReport report : result){
            assertTrue("The report query with implicit 'all' visibility should return the right records", 
            		matchingOwnerReports.contains(report.getId()));
        }

        //Redo the same test with an explicit "All"
        result = userReportMgr.getReports(new DummyQuerySpec(ReportVisibilityType.ALL_REPORTS), ownerId);
        assertEquals("The report query with explicit 'all' visibility should return the right records", 
        		matchingOwnerReports.size(), result.size());
        for(IPersistentReport report : result){
            assertTrue("The report query with explicit 'all' visibility should return the right records", 
            		matchingOwnerReports.contains(report.getId()));
        }
    }

    /**
     * This test verifies that the visibility "my" is properly applied. When
     * "my" is set, only reports belonging the creator are returned. Shared
     * reports from other people should not show up.
     */
    public void testGetReportsWithMyVisibility() {
        final Long ownerId = new Long(0);
        final Long otherOwnerId = new Long(1);
        final Set matchingOwnerReports = new HashSet();

        //Create one non shared and one shared report
        matchingOwnerReports.add(createPersistentReport(ownerId, false, "dummyTitle1"));
        matchingOwnerReports.add(createPersistentReport(ownerId, true, "dummyTitle2"));
        createPersistentReport(otherOwnerId, false, "dummyTitle3");
        createPersistentReport(otherOwnerId, true, "dummyTitle4");

        IUserReportMgr userReportMgr = getUserReportMgr();
        List<IPersistentReport> result = userReportMgr.getReports(
        		new DummyQuerySpec(ReportVisibilityType.MY_REPORTS), ownerId);
        assertEquals(matchingOwnerReports.size(), result.size());
        for(IPersistentReport report : result){
            assertTrue(matchingOwnerReports.contains(report.getId()));
        }
    }

    /**
     * This test verifies that the visibility "shared" is properly applied. When
     * "shared" is set, only shared reports belonging to other people than the
     * creator are returned. Shared reports from the creator should not show up.
     */
    public void testGetReportsWithSharedVisibility() {
        final Long ownerId = new Long(0);
        final Long otherOwnerId = new Long(1);
        final Set matchingOwnerReports = new HashSet();

        //Create one non shared and one shared report
        createPersistentReport(ownerId, false, "dummyTitle1");
        createPersistentReport(ownerId, true, "dummyTitle2");
        createPersistentReport(otherOwnerId, false, "dummyTitle3");
        matchingOwnerReports.add(createPersistentReport(otherOwnerId, true, "dummyTitle4"));

        IUserReportMgr userReportMgr = getUserReportMgr();
        List<IPersistentReport> result = userReportMgr.getReports(
        		new DummyQuerySpec(ReportVisibilityType.SHARED_REPORTS), ownerId);
        assertEquals(matchingOwnerReports.size(), result.size());
        for(IPersistentReport report : result){
            assertTrue(matchingOwnerReports.contains(report.getId()));
        }
    }
    
    private static final String DEFAULT_REPORT_TITLE = "new report";
    private static final String NEW_REPORT_TITLE = "Dark-eyed Junco";
    private static final String DEFAULT_DESCRIPTION = "description";
    
    public void testSaveAndInsert() throws ReportAccessException,
			DataSourceException {
    	final Long ownerId = new Long(0);
		final IPersistentReportMgr reportMgr = getPersistentReportMgr();
		final IUserReportMgr userReportMgr = getUserReportMgr();

		IPersistentReport report = reportMgr.createPersistentReport(ownerId);
		report.setTitle(NEW_REPORT_TITLE);
		report.setDescription(DEFAULT_DESCRIPTION);
		report.getOwner().setIsShared(false);
		report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);

		try {
			userReportMgr.saveReport(report, ownerId);
			fail("can't save a report that is not in the database.");
			
			//TODO should throw better Exception	
		} catch (NullPointerException e) {
			assertNotNull(e);
		}
		
		userReportMgr.insertReport(report, ownerId);
		
	}
    
	private void insertDuplicated(Long userIdA, boolean isSharedUserA, Long userIdB,
			boolean isSharedUserB, boolean expectProblem) throws ReportAccessException,
			UniqueConstraintViolationException, DataSourceException {
		IPersistentReport report;

		IUserReportMgr userReportMgr = getUserReportMgr();

		report = createAndSavePersistentReport(userIdA, isSharedUserA, DEFAULT_REPORT_TITLE);
		report.setTitle(NEW_REPORT_TITLE);
		userReportMgr.saveReport(report, userIdA);

		report = createAndSavePersistentReport(userIdB, isSharedUserB, DEFAULT_REPORT_TITLE);
		report.setTitle(NEW_REPORT_TITLE);
		if(expectProblem){
			try {
				userReportMgr.saveReport(report, userIdB);
				fail("should not be able to insert duplicated title report.");
			} catch (UniqueConstraintViolationException e) {
				assertNotNull(e);
			}
		}else{
			userReportMgr.saveReport(report, userIdB);
		}
	}
	
	private void saveDuplicated(Long userIdA, boolean isSharedUserA, Long userIdB,
			boolean isSharedUserB, boolean expectProblem) throws ReportAccessException,
			UniqueConstraintViolationException, DataSourceException {
		final IPersistentReportMgr reportMgr = getPersistentReportMgr();
		final IUserReportMgr userReportMgr = getUserReportMgr();

		IPersistentReport report = reportMgr.createPersistentReport(userIdA);
		report.setTitle(NEW_REPORT_TITLE);
		report.setDescription(DEFAULT_DESCRIPTION);
		report.getOwner().setIsShared(isSharedUserA);
		report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
		userReportMgr.insertReport(report, userIdA);
		
		report = reportMgr.createPersistentReport(userIdB);
		report.setTitle(NEW_REPORT_TITLE);
		report.setDescription(DEFAULT_DESCRIPTION);
		report.getOwner().setIsShared(isSharedUserB);
		report.getInquiry().setTargetData(InquiryTargetDataType.POLICY);
		if(expectProblem){
			try {
				userReportMgr.insertReport(report, userIdB);
				fail("should not be able to insert duplicated title report.");
			} catch (UniqueConstraintViolationException e) {
				assertNotNull(e);
			}
		}else{
			userReportMgr.insertReport(report, userIdB);
		}
	}
	
	public void testInsertDuplicatedPrivateReportSameUser() throws ReportAccessException,
			DataSourceException {
		final Long ownerId = new Long(0);
		insertDuplicated(ownerId, false, ownerId, false, true);
	}

	public void testSaveDuplicatedPrivateReportSameUser() throws ReportAccessException,
			UniqueConstraintViolationException, DataSourceException {
		final Long ownerId = new Long(0);
		saveDuplicated(ownerId, false, ownerId, false, true);
	}

    
    public void testInsertDuplicatedPrivateAndSharedReportSameUser() throws ReportAccessException,
			DataSourceException {
    	final Long ownerId = new Long(0);
		insertDuplicated(ownerId, false, ownerId, true, true);
    }
    
    public void testSaveDuplicatedPrivateAndSharedReportSameUser() throws ReportAccessException,
			DataSourceException {
		final Long ownerId = new Long(0);
		saveDuplicated(ownerId, false, ownerId, true, true);
	}
    
    public void testInsertDuplicatedSharedAndPrivateReportSameUser() throws ReportAccessException,
			DataSourceException {
    	final Long ownerId = new Long(0);
		insertDuplicated(ownerId, true, ownerId, false, true);
	}

	public void testSaveDuplicatedSharedAndPrivateReportSameUser() throws ReportAccessException,
			DataSourceException {
		final Long ownerId = new Long(0);
		saveDuplicated(ownerId, true, ownerId, false, true);
	}

	public void testInsertDuplicatedSharedReportSameUser() throws ReportAccessException,
			DataSourceException {
		final Long ownerId = new Long(0);
		insertDuplicated(ownerId, true, ownerId, true, true);
	}

	public void testSaveDuplicatedSharedReportSameUser() throws ReportAccessException,
			DataSourceException {
		final Long ownerId = new Long(0);
		saveDuplicated(ownerId, true, ownerId, true, true);
	}

	public void testInsertDuplicatedPrivateReportDifferentUser() throws ReportAccessException,
			DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		insertDuplicated(userAId, false, userBId, false, false);
	}

	public void testSaveDuplicatedPrivateReportDifferentUser() throws ReportAccessException,
			DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		saveDuplicated(userAId, false, userBId, false, false);
	}

	public void testInsertDuplicatedPrivateAndSharedReportDifferentUser()
			throws ReportAccessException, DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		insertDuplicated(userAId, false, userBId, true, false);
	}

	public void testSaveDuplicatedPrivateAndSharedReportDifferentUser()
			throws ReportAccessException, DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		saveDuplicated(userAId, false, userBId, true, false);
	}
	
	
	public void testInsertDuplicatedSharedReportDifferentUser() throws ReportAccessException,
			DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		insertDuplicated(userAId, true, userBId, true, false);
	}
	
	public void testSaveDuplicatedSharedReportDifferentUser() throws ReportAccessException,
			DataSourceException {
		final Long userAId = new Long(0);
		final Long userBId = new Long(1);
		saveDuplicated(userAId, true, userBId, true, false);
	}
	
    

    /**
     * Dummy class for the query specification
     * 
     * @author ihanen
     */
    protected class DummyQuerySpec implements IPersistentUserReportMgrQuerySpec {

        private ReportVisibilityType visibility;

        /**
         * Constructor
         * 
         * @param visibility
         *            visibility type
         */
        public DummyQuerySpec(ReportVisibilityType visibility) {
            this.visibility = visibility;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec#getVisibility()
         */
        public ReportVisibilityType getVisibility() {
            return visibility;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSearchSpecTerms()
         */
        public IPersistentReportMgrQueryTerm[] getSearchSpecTerms() {
            return new IPersistentReportMgrQueryTerm[0];
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSortSpecTerms()
         */
        public IPersistentReportMgrSortTerm[] getSortSpecTerms() {
            return new IPersistentReportMgrSortTerm[0];
        }
    }
}