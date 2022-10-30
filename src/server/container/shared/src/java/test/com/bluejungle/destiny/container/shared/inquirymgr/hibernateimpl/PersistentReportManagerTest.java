/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the test class for the persistent report manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentReportManagerTest.java#3 $
 */

public class PersistentReportManagerTest extends DACContainerSharedTestCase {

    private static final Log LOG = LogFactory.getLog(PersistentReportManagerTest.class.getName());

    /**
     * Constructor
     *  
     */
    public PersistentReportManagerTest() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * Constructor
     * 
     * @param testName
     */
    public PersistentReportManagerTest(String testName) {
        super(testName);
    }

    /**
     * Returns an instance of the persistent report manager
     * 
     * @return an instance of the persistent report manager
     */
    private IPersistentReportMgr getPersistentReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("persistentReportMgr", PersistentReportMgrImpl.class.getName(), IPersistentReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IPersistentReportMgr reportMgr = (IPersistentReportMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * This test verifies the class instantiation
     */
    public void testInstantiation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        HashMapConfiguration badConfig = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo("persistentReportMgr", PersistentReportMgrImpl.class.getName(), IPersistentReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        ComponentInfo badCompInfo = new ComponentInfo("bad_persistentReportMgr", PersistentReportMgrImpl.class.getName(), IPersistentReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, badConfig);

        IPersistentReportMgr reportMgr = (IPersistentReportMgr) compMgr.getComponent(compInfo);
        assertNotNull("Persistent report manager should be created properly with a valid configuration", reportMgr);

        boolean exThrown = false;
        try {
            IPersistentReportMgr badReportMgr = (IPersistentReportMgr) compMgr.getComponent(badCompInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Persistent report manager should not accept bad configuration", exThrown);
    }

    /**
     * This test verifies that the new persistent reports are properly created
     */
    public void testReportCreation() {
        IPersistentReportMgr reportMgr = getPersistentReportMgr();
        final Long ownerId = new Long(0);
        IPersistentReport report = null;
        report = reportMgr.createPersistentReport(new Long(0));
        assertNull("New persistent reports should not have id assigned", report.getId());
        assertNotNull("New persistent reports should have an empty inquiry", report.getInquiry());
        assertNotNull("New persistent reports should have an owner", report.getOwner());
        assertEquals("New persistent reports should have the specified", ownerId, report.getOwner().getOwnerId());
        assertFalse("New persistent reports should not be shared", report.getOwner().getIsShared());
        assertNotNull("New persistent reports shoudl have a default sort", report.getSortSpec());
        assertEquals("New persistent reports sort the time descending", SortFieldType.DATE, report.getSortSpec().getSortField());
        assertEquals("New persistent reports sort the time descending", SortDirectionType.DESCENDING, report.getSortSpec().getSortDirection());
        assertNotNull("New reports should have a default empty time period", report.getTimePeriod());
        assertNull("New reports should have a default empty time period", report.getTimePeriod().getBeginDate());
        assertNull("New reports should have a default empty time period", report.getTimePeriod().getEndDate());
        assertEquals("New reports should have a default summary type set to NONE", ReportSummaryType.NONE, report.getSummaryType());
    }

    /**
     * This test verifies that reports and other related objects get deleted
     * properly
     */
    public void testReportDeletion() {
        IPersistentReportMgr reportMgr = getPersistentReportMgr();

        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            final Long ownerId = new Long(0);
            IPersistentReport report = null;
            report = reportMgr.createPersistentReport(ownerId);
            report.setTitle("tempTitle");
            report.setDescription("tempDescription");
            IPersistentInquiry inquiry = (IPersistentInquiry) report.getInquiry();
            inquiry.setTargetData(InquiryTargetDataType.POLICY);
            t = s.beginTransaction();
            s.save(report);
            t.commit();
            Long reportId = report.getId();
            Long inquiryId = inquiry.getId();
            assertNotNull("Report id should not be null after saving", reportId);
            assertNotNull("Inquiry id should not be null after saving", inquiryId);

            //Deletes the report
            IPersistentReport deletedReport = null;
            try {
                reportMgr.deleteReport(reportId);
                deletedReport = reportMgr.getReport(reportId);
            } catch (DataSourceException e1) {
                fail("Failed to delete or get a persistent report");
            }
            assertNull("The report should be deleted", deletedReport);

            //Try to get the inquiry manually
            s.clear();
            IPersistentInquiry deletedInquiry = (IPersistentInquiry) s.get(InquiryDO.class, inquiryId);
            assertNull("The inquiry should be cascade deleted", deletedInquiry);
        } catch (HibernateException e) {
            fail("No hibernate exception should be thrown");
            HibernateUtils.rollbackTransation(t, LOG);
        } finally {
            HibernateUtils.closeSession(s, LOG);
        }
    }

    /**
     * This test verifies that a report is properly persisted in the database
     */
    public void testReportPersistance() {
        final String reportTitle = "myReportTitle";
        final String reportDescription = "myReportDescription";

        IPersistentReportMgr reportMgr = getPersistentReportMgr();

        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            final Long ownerId = new Long(0);
            IPersistentReport report = reportMgr.createPersistentReport(ownerId);
            report.setTitle(reportTitle);
            report.setDescription(reportDescription);
            IInquiry inquiry = report.getInquiry();
            inquiry.setTargetData(InquiryTargetDataType.ACTIVITY);
            Set actionNames = new HashSet();
            actionNames.add(ActionEnumType.ACTION_COPY);
            actionNames.add(ActionEnumType.ACTION_OPEN);
            actionNames.add(ActionEnumType.ACTION_MOVE);
            inquiry.addAction(ActionEnumType.ACTION_COPY);
            inquiry.addAction(ActionEnumType.ACTION_OPEN);
            inquiry.addAction(ActionEnumType.ACTION_MOVE);
            inquiry.setLoggingLevel(3);

            //Commit the report
            t = s.beginTransaction();
            s.save(report);
            t.commit();
            Long reportId = report.getId();
            assertNotNull("Saving the report should assign an Id", reportId);
            s.clear();

            //Fetch the report back and verify things
            report = reportMgr.getReport(reportId);
            assertEquals("Report id should be correct", reportId, report.getId());
            assertEquals("Report name should be fetched", reportTitle, report.getTitle());
            assertEquals("Report description should be fetched", reportDescription, report.getDescription());

            inquiry = report.getInquiry();
            assertNotNull("The report should contain an inquiry", inquiry);
            Set actions = inquiry.getActions();
            assertEquals("All actions should be fetched", actionNames.size(), actions.size());
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                IInquiryAction action = (IInquiryAction) it.next();
                assertTrue("All actions should be fetched properly", actionNames.contains(action.getActionType()));
            }
            assertEquals("The logging level should be correct", 3, inquiry.getLoggingLevel());
        } catch (DataSourceException e) {
            fail("Failed to create persistent report");
        } catch (HibernateException e) {
            fail("No hibernate exception should be thrown");
        } finally {
            HibernateUtils.rollbackTransation(t, LOG);
            HibernateUtils.closeSession(s, LOG);
        }
    }
}