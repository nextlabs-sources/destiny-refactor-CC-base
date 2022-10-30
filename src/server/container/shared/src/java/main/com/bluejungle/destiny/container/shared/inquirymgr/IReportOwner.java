/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the report ownership interface. The report ownership interface
 * defines the current report owner, and whether the report can be shared with
 * other users.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportOwner.java#1 $
 */

public interface IReportOwner {

    /**
     * Returns whether an report can be shared with other users
     * 
     * @return true if an report can be shared with other users, false otherwise
     */
    public boolean getIsShared();

    /**
     * Returns the destiny id of the report owner
     * 
     * @return the destiny id of the report owner
     */
    public Long getOwnerId();

    /**
     * Sets whether an report can be shared with other users or not
     * 
     * @param shared
     *            share flag
     */
    public void setIsShared(boolean shared);
}