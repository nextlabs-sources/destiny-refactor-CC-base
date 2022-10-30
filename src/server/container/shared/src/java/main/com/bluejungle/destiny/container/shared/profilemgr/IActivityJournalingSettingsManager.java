/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UnknownEntryException;

/**
 * IActivityJournalingSettingsManager is responsible for managing
 * IActivityJournalingSettings instances.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/profilemgr/IActivityJournalingSettingsManager.java#1 $
 */
public interface IActivityJournalingSettingsManager {

    String COMP_NAME = "ActivityJournalSettingsManager";

    /**
     * Retrieve an IActivityJournalingSettings instance by name
     * 
     * @param name
     *            the name of the IActivityJournalingSettings instance
     * @param agentType TODO
     * @return an IActivityJournalingSettings instance with the specified name
     * @throws DataSourceException
     *             if an error occurs while accessing the persistent store
     * @throws UnknownEntryException
     *             if the name specified is not assiged to a currently persisted
     *             IActivityJournalingSettings instance
     */
    public IActivityJournalingSettings getActivityJournalingSettings(String name, IAgentType agentType) throws DataSourceException, UnknownEntryException;

    /**
     * Retrieve the list of the pre-defined IActivityJournalingSettings
     * instances defined for a given agent type
     * 
     * @param agentType
     *            The type for which to retrieve pre-defined activity journaling
     *            settings
     * 
     * @return a list of pre-defined IActivityJournalingSettings instances
     *         defined within the system
     * @throws DataSourceException
     *             if an error occurs while accessing the persistence store
     */
    public List<IActivityJournalingSettings> getActivityJournalSettings(IAgentType agentType) throws DataSourceException;
}