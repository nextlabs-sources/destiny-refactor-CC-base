/*
 * Created on Mar 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This is a dummy log entry class for the test. It allows inserting / deleting
 * entries in the log table to support the inquiry center test cases.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/TestPolicyActivityLogEntryDO.java#1 $
 */

public class TestTrackingActivityLogEntryDO {

    private ActionEnumType action;
    private Long applicationId;
    private String applicationName;
    private Long hostId;
    private String hostIPAddress;
    private String hostName;
    private Long id;
    private Calendar timestamp;
    private Long userId;
    private String userName;
    private IFromResourceInformation fromResourceInfo;
    private IToResourceInformation toResourceInfo;
    private int level;

    /**
     * Constructor
     */
    public TestTrackingActivityLogEntryDO() {
        super();
    }

    /**
     * Alternate Constructor
     * 
     * @param otherDO
     *            data object to "clone"
     */
    protected TestTrackingActivityLogEntryDO(TestTrackingActivityLogEntryDO otherDO) {
        this.action = otherDO.action;
        this.applicationId = otherDO.applicationId;
        this.applicationName = otherDO.applicationName;
        this.hostId = otherDO.hostId;
        this.hostIPAddress = otherDO.hostIPAddress;
        this.hostName = otherDO.hostName;
        this.id = otherDO.id;
        this.timestamp = otherDO.timestamp;
        this.fromResourceInfo = otherDO.fromResourceInfo;
        this.toResourceInfo = otherDO.toResourceInfo;
        this.userId = otherDO.userId;
        this.userName = otherDO.userName;
        this.level = otherDO.level;
    }

    /**
     * Returns the action.
     * 
     * @return the action.
     */
    public ActionEnumType getAction() {
        return this.action;
    }

    /**
     * Returns the applicationId.
     * 
     * @return the applicationId.
     */
    public Long getApplicationId() {
        return this.applicationId;
    }

    /**
     * Returns the application name
     * 
     * @return the application name
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
     * Returns the hostId.
     * 
     * @return the hostId.
     */
    public Long getHostId() {
        return this.hostId;
    }

    /**
     * Returns the hostname
     * 
     * @return the hostname
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Returns the host IP address
     * 
     * @return the host IP address
     */
    public String getHostIPAddress() {
        return this.hostIPAddress;
    }

    /**
     * Returns the id.
     * 
     * @return the id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Returns the timestamp.
     * 
     * @return the timestamp.
     */
    public Calendar getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns the "to resource" information
     * 
     * @return the "to resource" information
     */
    public IToResourceInformation getToResourceInfo() {
        return this.toResourceInfo;
    }

    /**
     * Returns the userId.
     * 
     * @return the userId.
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * Returns the username
     * 
     * @return the username
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Returns the logging level
     * 
     * @return the logging level
     */
    public int getLevel() {
        return this.level;
    }
    
    /**
     * Sets the action
     * 
     * @param action
     *            The action to set.
     */
    public void setAction(ActionEnumType action) {
        this.action = action;
    }

    /**
     * Sets the applicationId
     * 
     * @param applicationId
     *            The applicationId to set.
     */
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Sets the application name
     * 
     * @param newApp
     *            new application name to set
     */
    public void setApplicationName(String newAppName) {
        this.applicationName = newAppName;
    }

    /**
     * Sets the "from resource" information
     * 
     * @param newFromResourceInfo
     *            the resource info to set
     */
    public void setFromResourceInfo(IFromResourceInformation newFromResourceInfo) {
        this.fromResourceInfo = newFromResourceInfo;
    }

    /**
     * Sets the hostId
     * 
     * @param hostId
     *            The hostId to set.
     */
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    /**
     * Sets the host name
     * 
     * @param hostName
     *            host name to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets the host IP address
     * 
     * @param hostIPAddress
     *            IP address to set
     */
    public void setHostIPAddress(String hostIPAddress) {
        this.hostIPAddress = hostIPAddress;
    }

    /**
     * Sets the id
     * 
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Sets the timestamp
     * 
     * @param timestamp
     *            The timestamp to set.
     */
    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the "to resource" information
     * 
     * @param newToResourceInfo
     *            the resource info to set
     */
    public void setToResourceInfo(IToResourceInformation newToResourceInfo) {
        this.toResourceInfo = newToResourceInfo;
    }

    /**
     * Sets the userId
     * 
     * @param userId
     *            The userId to set.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Sets the user name
     * 
     * @param userName
     *            user name to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Sets the logging level
     * 
     * @param level
     *            The logging level to set.
     */
    public void setLevel(int level) {
        this.level = level;
    }
}