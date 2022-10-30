/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import com.bluejungle.domain.action.ActionEnumType;

import java.util.Set;

/**
 * IActivityJournalingSettings represent settings utilized by the Agent to
 * control when tracking log entries are created. They can be referred to by
 * name, allowing settings instances to be preconfigured/persisted for later
 * assigned to communication profiles
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IActivityJournalingSettings.java#1 $
 */
public interface IActivityJournalingSettings {

    /**
     * Retrieve the name of this settings instance.
     * 
     * @return the name of this settings instance.
     */
    public String getName();

    /**
     * Retrieve the actions for which the agent should create log entries
     * 
     * @return the actions for which the agent should create log entries
     */
    public Set<ActionEnumType> getLoggedActions();
}