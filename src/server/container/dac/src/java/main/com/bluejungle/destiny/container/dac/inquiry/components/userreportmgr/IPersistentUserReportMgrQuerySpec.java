/*
 * Created on May 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec;

/**
 * This interface exposes the concept of visibility for the report query.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/userreportmgr/IPersistentUserReportMgrQuerySpec.java#1 $
 */

public interface IPersistentUserReportMgrQuerySpec extends IPersistentReportMgrQuerySpec {

    /**
     * Returns the visibility level applied to the query
     * 
     * @return the visibility level applied to the query
     */
    public ReportVisibilityType getVisibility();
}