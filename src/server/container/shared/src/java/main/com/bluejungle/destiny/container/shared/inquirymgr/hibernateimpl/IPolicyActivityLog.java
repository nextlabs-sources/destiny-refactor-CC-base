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
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the policy activity data object interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IPolicyActivityLog.java#2 $
 */

interface IPolicyActivityLog {

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
     * Returns the Id of the decision request associated with the activity
     * 
     * @return the Id of the decision request associated with the activity
     */
    public Long getDecisionRequestId();

    /**
     * Returns the "from resource" information associated with the activity
     * 
     * @return the "from resource" information associated with the activity
     */
    public IFromResourceInformation getFromResourceInfo();

    /**
     * Returns the host id associated with the activity. The id corresponds to
     * the ID stored in the directory for that particular host.
     * 
     * @return the host id associated with the activity
     */
    public Long getHostId();

    /**
     * Returns the host IP address. The host IP is the IP captured by the
     * enforcer.
     * 
     * @return the host IP address.
     */
    public String getHostIPAddress();

    /**
     * Returns the host name associated with the activity. The host name is the
     * host name as captured by the enforcer.
     * 
     * @return the host name associated with the activity.
     */
    public String getHostName();

    /**
     * Returns the policy decision accociated with the activity
     * 
     * @return the policy decision accociated with the activity
     */
    public PolicyDecisionEnumType getPolicyDecision();

    /**
     * Returns the ID of the Policy associated with this log entry
     * 
     * @return the ID of the Policy associated with this log entry
     */
    public Long getPolicyId();

    /**
     * Returns the record timestamp
     * 
     * @return the record timestamp
     */
    public Calendar getTimestamp();

    /**
     * Returns the "to resource" information associated with the activity
     * 
     * @return the "to resource" information associated with the activity
     */
    public IToResourceInformation getToResourceInfo();

    /**
     * Returns the Id of the record
     * 
     * @return the Id of the record
     */
    public Long getId();

    /**
     * Returns the user id associated with the activity. The id returned is the
     * ID of the user in the directory.
     * 
     * @return the user id associated with the activity
     */
    public Long getUserId();

    /**
     * Returns the username associated with the activity. The user name is the
     * username captured by the enforcer.
     * 
     * @return the username associated with the activity.
     */
    public String getUserName();

    /**
     * Returns the user's response. The User's Response is the input provided by
     * the user when attempting to perform an action that is associated with a
     * query policy
     * 
     * @return the user's response
     */
    public String getUserResponse();
    
    /**
     * Returns the logging level.
     * 
     * @return the logging level
     */
    public int getLevel();
}