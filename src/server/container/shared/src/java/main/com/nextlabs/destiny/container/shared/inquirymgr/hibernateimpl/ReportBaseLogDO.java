/* 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Timestamp;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IFromResourceInformation;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IToResourceInformation;
import com.bluejungle.domain.action.ActionEnumType;

public abstract class ReportBaseLogDO {
    private Long id;
    
    private Timestamp timestamp;
    private Long month;
    private Long day;
    
    private Long hostId;
    private String hostIPAddress;
    private String hostName;
    
    private Long userId;
    private String userName;
    /**
     * From cached_user.sid
     */
    private String userSID;
    
    private Long applicationId;
    private String applicationName;
    
    private ActionEnumType action;
    
    private int level;
    
    private IFromResourceInformation fromResourceInfo;
    /** 
     * Split from fromResourceName of the same table. This is for future use
     * when we query based on protocol or just the resource name rather than
     * search the entire string.
     */
    private String fromResourcePrefix; 
    private String fromResourcePath; 
    private String fromResourceShortName;
    
    private IToResourceInformation toResourceInfo;
    
    
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public Long getMonth() {
        return month;
    }
    public void setMonth(Long month) {
        this.month = month;
    }
    public Long getDay() {
        return day;
    }
    public void setDay(Long day) {
        this.day = day;
    }
    
    public Long getHostId() {
        return hostId;
    }
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
    public String getHostIPAddress() {
        return hostIPAddress;
    }
    public void setHostIPAddress(String hostIPAddress) {
        this.hostIPAddress = hostIPAddress;
    }
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserSID() {
        return userSID;
    }
    public void setUserSID(String userSID) {
        this.userSID = userSID;
    }
    
    public Long getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }
    public String getApplicationName() {
        return applicationName;
    }
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    public ActionEnumType getAction() {
        return action;
    }
    public void setAction(ActionEnumType action) {
        this.action = action;
    }
    
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    
    public IFromResourceInformation getFromResourceInfo() {
        return fromResourceInfo;
    }
    public void setFromResourceInfo(IFromResourceInformation fromResourceInfo) {
        this.fromResourceInfo = fromResourceInfo;
    }
    public String getFromResourcePrefix() {
        return fromResourcePrefix;
    }
    public void setFromResourcePrefix(String fromResourcePrefix) {
        this.fromResourcePrefix = fromResourcePrefix;
    }
    public String getFromResourcePath() {
        return fromResourcePath;
    }
    public void setFromResourcePath(String fromResourcePath) {
        this.fromResourcePath = fromResourcePath;
    }
    public String getFromResourceShortName() {
        return fromResourceShortName;
    }
    public void setFromResourceShortName(String fromResourceShortName) {
        this.fromResourceShortName = fromResourceShortName;
    }
    
    public IToResourceInformation getToResourceInfo() {
        return toResourceInfo;
    }
    public void setToResourceInfo(IToResourceInformation toResourceInfo) {
        this.toResourceInfo = toResourceInfo;
    }
    
}
