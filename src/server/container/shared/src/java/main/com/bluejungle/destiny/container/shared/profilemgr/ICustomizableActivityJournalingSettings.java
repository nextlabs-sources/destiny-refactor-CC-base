/*
 * Created on Apr 15, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.util.Set;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * ICustomizableActivityJournalingSettings represent IActivityJournalingSettings
 * which can be customized.
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/ICustomizableActivityJournalingSettings.java#1 $
 */
public interface ICustomizableActivityJournalingSettings extends IActivityJournalingSettings {

    /**
     * Set the actions for which the Agent will create a log entry
     * 
     * @param loggedActions
     *            a Set of
     * @see com.bluejungle.domain.ActionEnumType instances
     */
    public void setLoggedActions(Set<ActionEnumType> loggedActions);

    /**
     * Save the changes made to this settings instance
     * 
     * @throws DataSourceException
     *             if an error occurs while saving this instance to the
     *             persistence store
     */
    public void save() throws DataSourceException;
}