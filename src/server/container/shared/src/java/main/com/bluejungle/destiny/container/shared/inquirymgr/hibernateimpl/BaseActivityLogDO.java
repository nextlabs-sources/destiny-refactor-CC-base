/*
 * Created on Nov 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This is the base log object class. This abstract class contains all the basic
 * fields for data objects.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseActivityLogDO.java#1 $
 */

public abstract class BaseActivityLogDO {

    private ActionEnumType action;
    private Long applicationId;
    private String applicationName;
    private IFromResourceInformation fromResourceInfo;
    private Long hostId;
    private String hostIPAddress;
    private String hostName;
    private Long id;
    private Calendar timestamp;
    private IToResourceInformation toResourceInfo;
    private Long userId;
    private String userName;
    private int level;
    private Boolean syncDone;

    /**
     * Returns the action associated with the record
     * 
     * @return the action associated with the record
     */
    public ActionEnumType getAction() {
        return this.action;
    }

    /**
     * Returns the application id associated with the record
     * 
     * @return the application id associated with the record
     */
    public Long getApplicationId() {
        return this.applicationId;
    }

    /**
     * Returns the application name associated with the record
     * 
     * @return the application name associated with the record
     */
    public String getApplicationName() {
        return this.applicationName;
    }

    /**
     * Returns the "from resource" information
     * 
     * @return the "from resource" information
     */
    public IFromResourceInformation getFromResourceInfo() {
        return this.fromResourceInfo;
    }

    /**
     * Returns the host id associated with the record in the directory
     * 
     * @return the host id associated with the record in the directory
     */
    public Long getHostId() {
        return this.hostId;
    }

    /**
     * Returns the host IP address associated with the record
     * 
     * @return the host IP address associated with the record
     */
    public String getHostIPAddress() {
        return this.hostIPAddress;
    }

    /**
     * Returns the host name associated with the record
     * 
     * @return the host name associated with the record
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns the record id
     * 
     * @return the record id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the record timestamp
     * 
     * @return the record timestamp
     */
    public Calendar getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns the "to resource" information associated with the record
     * 
     * @return the "to resource" information associated with the record
     */
    public IToResourceInformation getToResourceInfo() {
        return this.toResourceInfo;
    }

    /**
     * Returns the user id associated with the record
     * 
     * @return the user id associated with the record
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Returns the username associated with the record
     * 
     * @return the username associated with the record
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Returns the logging level associated with the record
     * 
     * @return the logging level associated with the record
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Sets the action name
     * 
     * @param actionName
     *            action to set
     */
    public void setAction(ActionEnumType actionName) {
        this.action = actionName;
    }

    /**
     * Sets the application id
     * 
     * @param newId
     *            new id to set
     */
    public void setApplicationId(Long newId) {
        this.applicationId = newId;
    }

    /**
     * Sets the application name
     * 
     * @param newAppName
     *            application name to set
     */
    public void setApplicationName(String newAppName) {
        this.applicationName = newAppName;
    }

    /**
     * Sets the "from resource" information
     * 
     * @param newInfo
     *            new information to set
     */
    public void setFromResourceInfo(IFromResourceInformation newInfo) {
        this.fromResourceInfo = newInfo;
    }

    /**
     * Sets the new host id
     * 
     * @param newId
     *            new id to set
     */
    public void setHostId(Long newId) {
        this.hostId = newId;
    }

    /**
     * Sets the host IP address
     * 
     * @param newIPAddress
     *            new IP address to set
     */
    public void setHostIPAddress(String newIPAddress) {
        this.hostIPAddress = newIPAddress;
    }

    /**
     * Sets the host name
     * 
     * @param newHostName
     *            new host name to set
     */
    public void setHostName(String newHostName) {
        this.hostName = newHostName;
    }

    /**
     * Sets the record id
     * 
     * @param newId
     *            new record id to set
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Sets the new timestamp for the record
     * 
     * @param newTimestamp
     *            new timestamp to set
     */
    public void setTimestamp(Calendar newTimestamp) {
        this.timestamp = newTimestamp;
    }

    /**
     * Sets the "to resource" information
     * 
     * @param newInfo
     *            new information to set
     */
    public void setToResourceInfo(IToResourceInformation newInfo) {
        this.toResourceInfo = newInfo;
    }

    /**
     * Sets the user id
     * 
     * @param newId
     *            new id to set
     */
    public void setUserId(Long newId) {
        this.userId = newId;
    }

    /**
     * Sets the username
     * 
     * @param newUserName
     *            new username to set
     */
    public void setUserName(String newUserName) {
        this.userName = newUserName;
    }
    
    /**
     * Sets the logging level
     * 
     * @param newLevel
     *            new logging level to set
     */
    public void setLevel(int newLevel) {
        this.level = newLevel;
    }
    
    public Boolean getSyncDone() {
        return syncDone;
    }

    public void setSyncDone(Boolean syncDone) {
        this.syncDone = syncDone;
    }
}
