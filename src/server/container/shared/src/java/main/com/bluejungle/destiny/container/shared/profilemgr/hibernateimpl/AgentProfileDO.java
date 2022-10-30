/*
 * Created on Jan 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;


/**
 * Agent Profile Data Object Implementation
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/data/AgentProfileDO.java#4 $:
 */
public class AgentProfileDO extends BaseProfileDO implements IAgentProfileDO {

    private boolean logViewingEnabled;
    private boolean trayIconEnabled;

    /**
     * Default Constructor. For Hibernate Use Only
     */
    public AgentProfileDO() {

    }

    /**
     * Create an AgentProfileDO instance
     * 
     * @param id
     *            the id of the AgentProfile
     * @param name
     *            the name of the AgentProfile
     * @param isDefault
     *            is this the default agent profile
     * @param logViewingEnabled
     *            determines if log viewing is enabled on this Agent Profile
     * @param trayIconEnabled
     *            determines if the tray icon is enabled on this Agent Profile
     * @param createdDate
     *            the created date of the AgentProfile
     * @param modifiedDate
     *            the modified date of the AgentProfile
     */
    public AgentProfileDO(Long id, String name, boolean isDefault, boolean logViewingEnabled, boolean trayIconEnabled, Calendar createdDate, Calendar modifiedDate) {
        super(id, name, isDefault, createdDate, modifiedDate);

        this.logViewingEnabled = logViewingEnabled;
        this.trayIconEnabled = trayIconEnabled;
    }

    /**
     * Create an instance of an AgentProfileDO
     * 
     * @param agentProfileData
     *            the data used to populate the agent profile
     */
    AgentProfileDO(IAgentProfileData agentProfileData) {
        super(agentProfileData.getName());

        this.logViewingEnabled = agentProfileData.isLogViewingEnabled();
        this.trayIconEnabled = agentProfileData.isTrayIconEnabled();
    }

    /**
     * Gets the logViewingEnabled value for this AgentProfileInfo.
     * 
     * @return logViewingEnabled
     */
    public boolean isLogViewingEnabled() {
        return logViewingEnabled;
    }

    /**
     * Sets the logViewingEnabled value for this AgentProfileInfo.
     * 
     * @param logViewingEnabled
     */
    public void setLogViewingEnabled(boolean logViewingEnabled) {
        this.logViewingEnabled = logViewingEnabled;
    }

    /**
     * Gets the trayIconEnabled value for this AgentProfileInfo.
     * 
     * @return trayIconEnabled
     */
    public boolean isTrayIconEnabled() {
        return trayIconEnabled;
    }

    /**
     * Sets the trayIconEnabled value for this AgentProfileInfo.
     * 
     * @param trayIconEnabled
     */
    public void setTrayIconEnabled(boolean trayIconEnabled) {
        this.trayIconEnabled = trayIconEnabled;
    }
}