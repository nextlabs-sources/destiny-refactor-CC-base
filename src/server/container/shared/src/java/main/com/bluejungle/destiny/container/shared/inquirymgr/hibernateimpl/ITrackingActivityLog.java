/*
 * Created on Mar 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This is the policy activity data object interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ITrackingActivityLog.java#1 $
 */

interface ITrackingActivityLog {

    /**
     * Returns the action associated with the activity
     * 
     * @return the action id associated with the activity
     */
    public ActionEnumType getAction();

    /**
     * Returns the application id associated with the activity
     * 
     * @return the application id associated with the activity
     */
    public Long getApplicationId();

    /**
     * Returns the application name associated with the activity
     * 
     * @return the application name associated with the activity
     */
    public String getApplicationName();

    /**
     * Returns the host id associated with the activity
     * 
     * @return the host id associated with the activity
     */
    public Long getHostId();

    /**
     * Returns the record timestamp
     * 
     * @return the record timestamp
     */
    public Calendar getTimestamp();

    /**
     * Returns the "from resource" information associated with the activity
     * 
     * @return the "from resource" information associated with the activity
     */
    public IFromResourceInformation getFromResourceInfo();

    /**
     * Returns the Id of the record
     * 
     * @return the Id of the record
     */
    public Long getId();

    /**
     * Returns the "to resource" information associated with the activity
     * 
     * @return the "to resource" information associated with the activity
     */
    public IToResourceInformation getToResourceInfo();

    /**
     * Returns the user id associated with the activity
     * 
     * @return the user id associated with the activity
     */
    public Long getUserId();
    
    /**
     * Returns the logging level.
     * 
     * @return the logging level
     */
    public int getLevel();
}