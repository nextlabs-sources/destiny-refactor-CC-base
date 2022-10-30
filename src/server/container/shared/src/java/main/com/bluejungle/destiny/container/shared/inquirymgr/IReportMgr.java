/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the report manager. The report manager allows the creation of
 * reports, either "on the fly", or reports that are meant to be persisted to
 * the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportMgr.java#1 $
 */

public interface IReportMgr {

    public static final String COMP_NAME = "reportMgr";

    /**
     * Returns a new report instance
     * 
     * @return a new report instance
     */
    public IReport createReport();
}