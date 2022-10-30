/*
 * Created on Nov 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.Calendar;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This interface is implemented by all report activity detail results. However,
 * no class directly implements this interface. This interface is added as a
 * convenience to avoid repeating the functions common to all detail results.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportActivityDetailResult.java#1 $
 */

abstract interface IReportActivityDetailResult extends IResultData {

    /**
     * Returns the action name
     * 
     * @return the action name
     */
    public ActionEnumType getAction();

    /**
     * Returns the application name
     * 
     * @return the application name
     */
    public String getApplicationName();

    /**
     * Returns the name of the "from resource"
     * 
     * @return the name of the "from resource"
     */
    public String getFromResourceName();

    /**
     * Returns the host IP address
     * 
     * @return the host IP address
     */
    public String getHostIPAddress();

    /**
     * Returns the host name
     * 
     * @return the host name
     */
    public String getHostName();

    /**
     * Returns the record id
     * 
     * @return the record id
     */
    public Long getId();

    /**
     * Returns the timestamp of the record
     * 
     * @return the timestamp of the record
     */
    public Calendar getTimestamp();

    /**
     * Returns the name of the "to resource"
     * 
     * @return the name of the "to resource"
     */
    public String getToResourceName();

    /**
     * Returns the username
     * 
     * @return the username
     */
    public String getUserName();
    
    /**
     * Returns the logging level
     * 
     * @return the logging level
     */
    public int getLoggingLevel();
}
