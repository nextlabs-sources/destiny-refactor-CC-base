/*
 * Created on Nov 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Date;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;

/**
 * This is a special implementation of the report execution manager to allow
 * fetching deployed entities in the future. This implementation is not used in
 * the production code.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrIn10.java#1 $
 */

public class ReportExecutionMgrIn10 extends ReportExecutionMgrStatefulImpl {

    private static final int TEN_MINUTES = 10 * 60 * 1000;

    /**
     * Constructor
     */
    public ReportExecutionMgrIn10() {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseReportExecutionMgr#getResourceClassElement(com.bluejungle.destiny.container.shared.inquirymgr.IReport,
     *      java.util.Date)
     */
    protected IQueryElement getResourceClassElement(IReport report, Date asOf) {
        final Date in10 = new Date(asOf.getTime() + TEN_MINUTES);
        return super.getResourceClassElement(report, in10);
    }
}
