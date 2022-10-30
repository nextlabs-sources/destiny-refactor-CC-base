/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;

/**
 * This the persistent report interface. A persistent report extends the basic
 * report because it can be persisted and contains ownership information.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IPersistentReport.java#1 $
 */

public interface IPersistentReport extends IReport {

    /**
     * Returns the report description
     * 
     * @return the report description
     */
    public String getDescription();

    /**
     * Returns the report Id
     * 
     * @return the report Id
     */
    public Long getId();

    /**
     * Returns the report title
     * 
     * @return the report title
     */
    public String getTitle();

    /**
     * Returns the report ownership information
     * 
     * @return the report ownership information
     */
    public IReportOwner getOwner();

    /**
     * Sets the report description
     * 
     * @param description
     *            description to set
     */
    public void setDescription(String description);

    /**
     * Sets the report name
     * 
     * @param name
     *            report name
     */
    public void setTitle(String name);
}