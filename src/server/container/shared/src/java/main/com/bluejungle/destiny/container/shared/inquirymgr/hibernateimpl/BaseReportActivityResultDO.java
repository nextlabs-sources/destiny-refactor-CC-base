/*
 * Created on Nov 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This is the base data object for report detail results
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportActivityResultDO.java#1 $
 */

abstract class BaseReportActivityResultDO extends BaseReportResultDO {

    private ActionEnumType action;
    private String applicationName;
    private String hostIPAddress;
    private String hostName;
    private String fromResourceName;
    private Long storedResultId;
    private String toResourceName;
    private Calendar timestamp;
    private String userName;
    private int loggingLevel;

    /**
     * Constructor
     */
    public BaseReportActivityResultDO() {
        super();
    }

    /**
     * Constructor
     * 
     * @param newAction
     *            new action type to set
     * @param newApplicationName
     *            new application name to set
     * @param newFromResource
     *            new "from resource" to set
     * @param newHostIPAddress
     *            new host IP address to set
     * @param newHostName
     *            new host name to set
     * @param newId
     *            new id to set
     * @param newTimeStamp
     *            new timestamp to set
     * @param newToResource
     *            new "to resource" to set
     * @param newUserName
     */
    public BaseReportActivityResultDO(ActionEnumType newAction, String newApplicationName, String newFromResource, String newHostIPAddress, String newHostName, Long newId, Calendar newTimeStamp, String newToResource, String newUserName, int loggingLevel) {
        super(newId);
        setAction(newAction);
        setApplicationName(newApplicationName);
        setHostIPAddress(newHostIPAddress);
        setHostName(newHostName);
        setFromResourceName(newFromResource);
        setToResourceName(newToResource);
        setTimestamp(newTimeStamp);
        setUserName(newUserName);
        setLoggingLevel(loggingLevel);
    }

    /**
     * Constructor
     * 
     * @param newAction
     *            new action type to set
     * @param newApplicationName
     *            new application name to set
     * @param newFromResource
     *            new "from resource" to set
     * @param newHostIPAddress
     *            new host IP address to set
     * @param newHostName
     *            new host name to set
     * @param newId
     *            new id to set
     * @param newStoredResultId
     *            new stored result id to set
     * @param newTimeStamp
     *            new timestamp to set
     * @param newToResource
     *            new "to resource" to set
     * @param newUserName
     */
    public BaseReportActivityResultDO(ActionEnumType newAction, String newApplicationName, String newFromResource, String newHostIPAddress, String newHostName, Long newId, Long newStoredResultId, Calendar newTimeStamp, String newToResource, 
            String newUserName, int loggingLevel) {
        this(newAction, newApplicationName, newFromResource, newHostIPAddress, newHostName, newId, newTimeStamp, newToResource, newUserName, loggingLevel);
        setStoredResultId(newStoredResultId);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getAction()
     */
    public ActionEnumType getAction() {
        return this.action;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getApplicationName()
     */
    public String getApplicationName() {
        return this.applicationName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getFromResourceName()
     */
    public String getFromResourceName() {
        return this.fromResourceName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getHostIPAddress()
     */
    public String getHostIPAddress() {
        return this.hostIPAddress;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getHostName()
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns the stored result id
     * 
     * @return the stored result id
     */
    public Long getStoredResultId() {
        return this.storedResultId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getTimestamp()
     */
    public Calendar getTimestamp() {
        return this.timestamp;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getToResourceName()
     */
    public String getToResourceName() {
        return this.toResourceName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getUserName()
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportPolicyActivityDetailResult#getLoggingLevel()
     */
    public int getLoggingLevel() {
        return this.loggingLevel;
    }

    /**
     * Sets the action name
     * 
     * @param actionName
     *            action to set
     */
    protected void setAction(ActionEnumType actionName) {
        this.action = actionName;
    }

    /**
     * Sets the application name
     * 
     * @param newAppName
     *            the application name to set
     */
    protected void setApplicationName(String newAppName) {
        this.applicationName = newAppName;
    }

    /**
     * Sets the "from resource" name
     * 
     * @param newName
     *            resource name to set
     */
    protected void setFromResourceName(String newName) {
        this.fromResourceName = newName;
    }

    /**
     * Sets the host IP address
     * 
     * @param newIPAddress
     *            new IP address to set
     */
    protected void setHostIPAddress(String newIPAddress) {
        this.hostIPAddress = newIPAddress;
    }

    /**
     * Sets the new host name
     * 
     * @param newHostName
     *            host name to set
     */
    protected void setHostName(String newHostName) {
        this.hostName = newHostName;
    }

    /**
     * Sets the stored result id. The stored result id is the row id of the
     * result record in the result table. (In a detail result, it is distinct
     * from the row id of the actual data row the result record represents). The
     * result id is useful for stateless queries where it is necessary to know
     * the first/last result id fetched in the result table.
     * 
     * @param newStoredResultId
     *            new stored result id to set.
     */
    protected void setStoredResultId(Long newStoredResultId) {
        this.storedResultId = newStoredResultId;
    }

    /**
     * Sets the new timestamp
     * 
     * @param newTimestamp
     *            new timestamp to set
     */
    protected void setTimestamp(Calendar newTimestamp) {
        this.timestamp = newTimestamp;
    }

    /**
     * Sets the "to resource" name
     * 
     * @param newName
     *            resource name to set
     */
    protected void setToResourceName(String newName) {
        this.toResourceName = newName;
    }

    /**
     * Sets the new user name
     * 
     * @param newName
     *            new user name to set
     */
    protected void setUserName(String newName) {
        this.userName = newName;
    }
    
    /**
     * Set the logging level
     * 
     * @param newName
     *            new policy name to set
     */
    protected void setLoggingLevel(int level) {
        this.loggingLevel = level;
    }
}
