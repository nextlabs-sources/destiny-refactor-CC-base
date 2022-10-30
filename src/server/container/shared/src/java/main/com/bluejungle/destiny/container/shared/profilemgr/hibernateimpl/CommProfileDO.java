/*
 * Created on Oct 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.io.Serializable;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICustomizableActivityJournalingSettings;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UnknownEntryException;
import com.bluejungle.framework.utils.CryptUtils;
import com.bluejungle.framework.utils.TimeInterval;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Lifecycle;
import net.sf.hibernate.Session;

/**
 * The Communication Profile DO Implementation
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/data/CommProfileDO.java#4 $
 */
public class CommProfileDO extends BaseProfileDO implements ICommProfileDO {

    private URI DABSLocation;
    private IAgentType agentType;
    private TimeInterval heartBeatFequency;
    private int logLimit;
    private TimeInterval logFrequency;
    private boolean pushEnabled;
    private int defaultPushPort;
    private IActivityJournalingSettings currentJournalingSettings;
    private ICustomizableActivityJournalingSettings customJournalingSettings;
    private byte[] passwordHash;

    /**
     * Empty Constructor. For Hibernate use only
     */
    public CommProfileDO() {
    }

    /**
     * Create a CommProfileDO instance
     * 
     * @param id
     * @param name
     * @param DABSLocation
     * @param heartBeatFequency
     * @param logLimit
     * @param logFrequency
     * @param agentPushParameters
     * @param secure
     * @param password
     * @param createdDate
     * @param modifiedDate
     */
    public CommProfileDO(Long id, String name, boolean isDefault, URI DABSLocation, IAgentType agentType, TimeInterval heartBeatFrequency, int logLimit, TimeInterval logFrequency, boolean pushEnabled, int defaultPushPort, byte[] passwordHash,
            Calendar createdDate, Calendar modifiedDate) {
        super(id, name, isDefault, createdDate, modifiedDate);

        if (DABSLocation == null) {
            throw new IllegalArgumentException("DABSLocation cannot be null.");
        }

        if (passwordHash == null) {
            throw new IllegalArgumentException("passwordHash cannot be null");
        }

        this.DABSLocation = DABSLocation;
        this.agentType = agentType;
        this.heartBeatFequency = heartBeatFrequency;
        this.logLimit = logLimit;
        this.logFrequency = logFrequency;
        this.pushEnabled = pushEnabled;
        this.defaultPushPort = defaultPushPort;
        this.passwordHash = passwordHash;
    }

    /**
     * Create a CommProfileDO instance
     * 
     * @param commProfileData
     *            the data used to populate the Comm Profile DO
     * @throws DataSourceException
     */
    CommProfileDO(ICommProfileData commProfileData, ICustomizableActivityJournalingSettings customActivityJournalingSettings) throws DataSourceException {
        super(commProfileData.getName());

        if (customActivityJournalingSettings == null) {
            throw new NullPointerException("customActivityJournalingSettings cannot be null.");
        }

        this.DABSLocation = commProfileData.getDABSLocation();
        this.agentType = commProfileData.getAgentType();
        this.heartBeatFequency = commProfileData.getHeartBeatFrequency();
        this.logLimit = commProfileData.getLogLimit();
        this.logFrequency = commProfileData.getLogFrequency();
        this.pushEnabled = commProfileData.isPushEnabled();
        this.defaultPushPort = commProfileData.getDefaultPushPort();
        this.customJournalingSettings = customActivityJournalingSettings;
        this.currentJournalingSettings = this.customJournalingSettings;

        // Set the password
        this.setPassword(commProfileData.getPassword());
    }

    /**
     * Gets the DABSLocation value for this CommProfileInfo.
     * 
     * @return DABSLocation
     */
    public URI getDABSLocation() {
        return DABSLocation;
    }

    /**
     * Sets the DABSLocation value for this CommProfileInfo.
     * 
     * @param DABSLocation
     */
    public void setDABSLocation(URI DABSLocation) {
        this.DABSLocation = DABSLocation;
    }

    /**
     * Gets the heartBeatFequency value for this CommProfileInfo.
     * 
     * @return heartBeatFequency
     */
    public TimeInterval getHeartBeatFrequency() {
        return heartBeatFequency;
    }

    /**
     * Sets the heartBeatFequency value for this CommProfileInfo.
     * 
     * @param heartBeatFequency
     */
    public void setHeartBeatFrequency(TimeInterval heartBeatFrequency) {
        this.heartBeatFequency = heartBeatFrequency;
    }

    /**
     * Gets the logLimit value for this CommProfileInfo.
     * 
     * @return logLimit
     */
    public int getLogLimit() {
        return logLimit;
    }

    /**
     * Sets the logLimit value for this CommProfileInfo.
     * 
     * @param logLimit
     */
    public void setLogLimit(int logLimit) {
        this.logLimit = logLimit;
    }

    /**
     * Gets the logFrequency value for this CommProfileInfo.
     * 
     * @return logFrequency
     */
    public TimeInterval getLogFrequency() {
        return logFrequency;
    }

    /**
     * Sets the logFrequency value for this CommProfileInfo.
     * 
     * @param logFrequency
     */
    public void setLogFrequency(TimeInterval logFrequency) {
        this.logFrequency = logFrequency;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#isPushEnabled()
     */
    public boolean isPushEnabled() {
        return this.pushEnabled;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#setPushEnabled(boolean)
     */
    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#getDefaultPushPort()
     */
    public int getDefaultPushPort() {
        return this.defaultPushPort;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#setDefaultPushPort(int)
     */
    public void setDefaultPushPort(int pushPort) {
        this.defaultPushPort = pushPort;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#setAgentType(com.bluejungle.domain.agenttype.AgentTypeEnumType)
     */
    public void setAgentType(IAgentType agentType) {
        if (agentType == null) {
            throw new NullPointerException("agentType cannot be null.");
        }

        this.agentType = agentType;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#getAgentType()
     */
    public IAgentType getAgentType() {
        return this.agentType;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#getCurrentJournalingSettings()
     */
    public IActivityJournalingSettings getCurrentJournalingSettings() {
        return this.currentJournalingSettings;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#setCurrentJournalingSettings(com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings)
     */
    public void setCurrentJournalingSettings(IActivityJournalingSettings journalingSettings) {
        if (journalingSettings == null) {
            throw new NullPointerException("journalingSettings cannot be null.");
        }

        this.currentJournalingSettings = journalingSettings;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO#getCustomJournalingSettings()
     */
    public ICustomizableActivityJournalingSettings getCustomJournalingSettings() {
        return this.customJournalingSettings;
    }

    /**
     * Set the custom journaling settings. Current required for Hibernate
     * 
     * @param journalingSettings
     *            the custeom journaling settings to set
     */
    public void setCustomJournalingSettings(ICustomizableActivityJournalingSettings journalingSettings) {
        if (journalingSettings == null) {
            throw new NullPointerException("journalingSettings cannot be null.");
        }

        this.customJournalingSettings = journalingSettings;
    }

    /**
     * Retrieve the password hash assigned to the profile
     * 
     * @return the password hash assigned to this comm profile
     */
    public byte[] getPasswordHash() {
        return this.passwordHash;
    }

    /**
     * Set the password hash. Currently required for Hibernate
     * 
     * @param passwordHash
     *            the password hash assigned to this comm profile
     */
    void setPasswordHash(byte[] passwordHash) {
        if (passwordHash == null) {
            throw new NullPointerException("passwordHash cannot be null.");
        }

        this.passwordHash = passwordHash;
    }

    /**
     * Set the password. Will be hashed before storing
     * 
     * @param password
     *            the password assigned to this comm profile
     */
    public void setPassword(String password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }

        try {
            this.passwordHash = CryptUtils.digest(password, "SHA1", 0);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA1 must be supported by installed providers");
        }
    }

    private IActivityJournalingSettingsManager getJournalingSettingsManager() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        return (IActivityJournalingSettingsManager) componentManager.getComponent(IActivityJournalingSettingsManager.COMP_NAME);
    }

    /**
     * The following are for Hibernate Use Only *****************************
     */

    /**
     * For Hibernate Use Only
     * 
     * @return the current journaling settings name
     */
    private String getCurrentJournalingSettingsName() {
        return this.getCurrentJournalingSettings().getName();
    }

    /**
     * For Hibernate Use Only. Set the current jounraling settings name
     * 
     * @param journalingSettingsName
     */
    private void setCurrentJournalingSettingsName(String journalingSettingsName) {
        if (journalingSettingsName == null) {
            throw new NullPointerException("journalingSettingsName cannot be null.");
        }

        IActivityJournalingSettingsManager journalingSettingsManager = getJournalingSettingsManager();
        try {
            ICustomizableActivityJournalingSettings customJournalingSettings = this.getCustomJournalingSettings();
            if ((customJournalingSettings != null) && (journalingSettingsName.equals(customJournalingSettings.getName()))) {
                this.currentJournalingSettings = this.customJournalingSettings;
            } else {
                this.currentJournalingSettings = journalingSettingsManager.getActivityJournalingSettings(journalingSettingsName, this.getAgentType());
            }
        } catch (UnknownEntryException exception) {
            throw new IllegalStateException("Could not load journaling settings for profile", exception);
        } catch (DataSourceException exception) {
            throw new IllegalStateException("Could not load journaling settings for profile", exception);
        }
    } 
}