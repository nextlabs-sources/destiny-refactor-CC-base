/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import java.util.Set;

import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.ICustomizableActivityJournalingSettings;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * ActivityJournalingSettingsDO is a persistent class containing
 * Activity Journaling Settings utilized by the agent to determine
 * what actions are logged
 *
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/hibernateimpl/ActivityJournalingSettingsDO.java#1 $
 */
public class ActivityJournalingSettingsDO implements ICustomizableActivityJournalingSettings {

    private Long id;
    private String name;
    private Set<ActionEnumType> loggedActions;
    private boolean isCustomizable;

    /**
     * Create an instance of ActivityJournalingSettingsDO
     *  
     */
    public ActivityJournalingSettingsDO() {
        super();
    }

    /**
     * Create an instance of ActivityJournalingSettingsDO
     * 
     * @param name
     * @param loggedActivities
     */
    public ActivityJournalingSettingsDO(String name, Set<ActionEnumType> loggedActivities) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        if (loggedActivities == null) {
            throw new NullPointerException("loggedActivities cannot be null.");
        }

        this.name = name;
        this.loggedActions = loggedActivities;
        this.isCustomizable = true;
    }

    /**
     * Retrieve the id.
     * 
     * @return the id.
     */
    Long getId() {
        return this.id;
    }

    /**
     * Set the id
     * 
     * @param id
     *            The id to set.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name
     * 
     * @param name
     *            The name to set.
     */
    void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        this.name = name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings#getLoggedActions()
     */
    public Set<ActionEnumType> getLoggedActions() {
        return this.loggedActions;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings#setLoggedActions(java.util.Set)
     */
    public void setLoggedActions(Set<ActionEnumType> loggedActions) {
        if (loggedActions == null) {
            throw new NullPointerException("loggedActions cannot be null.");
        }

        this.loggedActions = loggedActions;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings#save()
     */
    public void save() throws DataSourceException {
        if (!isCustomizable()) {
            throw new UnsupportedOperationException("This settings instance is non-customizable");
        }
        
        this.getManager().update(this);
    }

    /**
     * Delete this settings object. Package protected to prevent deletion by
     * clients. Deletion will automatically occur for custom settings when the
     * associated comm profile is deleted
     * 
     * @throws DataSourceException
     */
    void delete() throws DataSourceException {
        if (!isCustomizable()) {
            throw new UnsupportedOperationException("This settings instance is non-customizable");
        }
        
        this.getManager().delete(this);
    }

    /**
     * Retrieve the manager for this activity journaling settings
     * 
     * @return
     */
    private HibernateActivityJournalingSettingsManager getManager() {
        return (HibernateActivityJournalingSettingsManager) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournalingSettingsManager.COMP_NAME);
    }

    /**
     * Determine if this ActivityJournalingSettings instance if customizable
     * 
     * @return the isCustomizable.
     */
    boolean isCustomizable() {
        return this.isCustomizable;
    }

    /**
     * Set this ActivityJournalingSettings to be either customizable or non-customizable
     * 
     * @param isCustomizable
     *            true to be customizable; false otherwise
     */
    void setCustomizable(boolean isCustomizable) {
        this.isCustomizable = isCustomizable;
    }
}
