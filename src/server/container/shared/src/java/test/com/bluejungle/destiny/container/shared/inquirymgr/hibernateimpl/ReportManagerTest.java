/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the report manager test class. It tests the in memory report
 * implementation.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportManagerTest.java#1 $
 */

public class ReportManagerTest extends BaseDestinyTestCase {

    /**
     * Constructor
     */
    public ReportManagerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ReportManagerTest(String testName) {
        super(testName);
    }

    /**
     * Returns an instance of the report manager
     * 
     * @return an instance of the report manager
     */
    private IReportMgr getReportMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo compInfo = new ComponentInfo("reportMgr", ReportMgrImpl.class.getName(), IReportMgr.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IReportMgr reportMgr = (IReportMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * This test verifies the creation of memory report
     */
    public void testNonPersistentReportCreation() {
        IReportMgr reportMgr = getReportMgr();
        assertNotNull("Report manager creation should work", reportMgr);
        IReport newReport = reportMgr.createReport();
        assertNotNull("Report manager should create a report", newReport);
        assertNotNull("New reports should have an empty inquiry", newReport.getInquiry());
        assertNotNull("New reports shoudl have a default sort", newReport.getSortSpec());
        assertEquals("New reports sort the time descending", SortFieldType.DATE, newReport.getSortSpec().getSortField());
        assertEquals("New reports sort the time descending", SortDirectionType.DESCENDING, newReport.getSortSpec().getSortDirection());
        assertNotNull("New reports should have a default empty time period", newReport.getTimePeriod());
        assertNull("New reports should have a default empty time period", newReport.getTimePeriod().getBeginDate());
        assertNull("New reports should have a default empty time period", newReport.getTimePeriod().getEndDate());
        assertEquals("New reports should have a default summary type set to NONE", ReportSummaryType.NONE, newReport.getSummaryType());
    }
}