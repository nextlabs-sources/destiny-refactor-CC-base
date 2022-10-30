/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;

/**
 * This is the "on the fly" implementation of the report manager. This report
 * manager implementation creates report objects that are simply meant to be
 * used and forgotten later on.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportMgrImpl.java#1 $
 */

public class ReportMgrImpl extends BaseReportMgrImpl implements IReportMgr {

    /**
     * Constructor
     */
    public ReportMgrImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr#createReport()
     */
    public IReport createReport() {
        ReportImpl newReport = new ReportImpl();
        newReport.setTimePeriod(getDefaultReportTimePeriod());
        newReport.setSortSpec(getDefaultReportSortSpec());
        InquiryImpl newInquiry = new InquiryImpl();
        newReport.setInquiry(newInquiry);
        newReport.setSummaryType(getDefaultSummaryType());
        return newReport;
    }

}