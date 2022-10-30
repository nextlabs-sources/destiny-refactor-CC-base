/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.container.shared.profilemgr.ProfileNotFoundException;

/**
 * Interface listing the APIs that need to be supported by an Agent Manager
 * implementation that resides on the DMS.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IAgentManager {

    public static final String COMP_NAME = "agentMgr";

    /**
     * Registers an agent with the management repository. This is mainly to tell
     * the server of its existence and to obtain a security certificate for
     * further communication with the server. If an entry already exists for an
     * agent on this host, it is deleted and a new entry is created.
     * 
     * @param startup
     *            configuration for the agent
     * @param hostName
     *            hostname where the agent is located
     * @return id of the agent
     * @throws PersistenceException
     *             if the agent is unknown
     */
    public IAgentStartupConfiguration registerAgent(IAgentRegistrationData registrationData) throws PersistenceException;

    /**
     * Unregister the agent associated with the specified id. This will not
     * remove the agent from the database, but rather mark it in an unregistered
     * state. In this state, the agent will not be retrievable except by id. The
     * agent can be transferred out of this state if a heartbeat, request to
     * start or request to register is received from the agent's host
     * 
     * @param agentID
     *            the id of the agent to unregister
     */
    public void unregisterAgent(Long agentId) throws PersistenceException, InvalidIDException;

    /**
     * Specifies in the management repository that the agent has started up on
     * the remote host and the push port that it is using, if any. If a push
     * port was not found, it is not specified and the agent is marked as NOT
     * push ready. Otherwise it is marked as push ready.
     * 
     * @param id
     *            id of the agent
     * @param startupData
     * @throws InvalidIDException
     */
    public void startupAgent(Long id, IAgentStartupData startupData) throws PersistenceException, InvalidIDException;

    /**
     * Returns updated information for an agent - including perhaps updated
     * profiles etc.
     * 
     * @param id
     *            id of the agent
     * @param heartbeatData
     *            containing any updates that the agent wants to tell the server
     * @return updates to the agent
     * @throws InvalidIDException
     */
    public IAgentUpdates checkUpdates(Long id, IAgentHeartbeatData statusData) throws PersistenceException, InvalidIDException;

    /**
     * Acknowldges the most recent updates received from the server by providing
     * the agent's status. This is for admin purposes.
     * 
     * @param id
     *            id of the agent
     * @param statusData
     * @throws InvalidIDException
     */
    public void acknowledgeUpdates(Long id, IAgentUpdateAcknowledgementData statusData) throws PersistenceException, InvalidIDException;

    /**
     * Specifies in the management repository that the agent has shut down on
     * the remote host
     * 
     * @param id
     *            id of the agent
     * @param shutdownData
     * @throws InvalidIDException
     */
    public void shutdownAgent(Long id, IAgentShutdownData shutdownData) throws PersistenceException, InvalidIDException;

    /**
     * Returns the list of agent matching a particular agent specification
     * 
     * @param agentSpec
     *            agent specification
     * @return an agent query result object
     * @throws PersistenceException
     *             if the query fails
     * @throws InvalidQuerySpecException
     *             if the query specification is not valid
     */
    public IAgentQueryResults getAgents(IAgentMgrQuerySpec agentSpec) throws PersistenceException, InvalidQuerySpecException;

    /**
     * Retrieve a factory which can be used to build agent query terms
     * 
     * @return a factory which can be used to build agent query terms
     */
    public IAgentMgrQueryTermFactory getAgentMgrQueryTermFactory();

    /**
     * Returns an agent with the given id
     * 
     * @param id
     *            id of the agent to retrieve
     * @return agent with given id
     * @throws PersistenceException
     *             if the query fails
     */
    public IAgentDO getAgentById(Long id) throws PersistenceException;

    /**
     * Returns statistics about the agents in the system
     * 
     * @return statistics about the agents in the system
     * @throws PersistenceException
     *             if the statistics gathering fails.
     */
    public IAgentStatistics getAgentStatistics() throws PersistenceException;

    /**
     * Sets the communication profile for the agent
     * 
     * @param id
     *            id of the agent
     * @param profileName
     *            name of the profile to set
     * @throws InvalidIDException
     *             if the agent id is unknown
     */
    public void setCommProfile(Long id, String profileName) throws PersistenceException, InvalidIDException, ProfileNotFoundException;

    /**
     * Set the communication profile for the specified agents
     * 
     * @param agentIDsAsLongs
     *            a set of agent ids
     * @param profileId
     *            the id of the profile to set
     * @throws ProfileNotFoundException
     * @throws PersistenceException
     */
    public void setCommProfileForAgents(Set agentIDsAsLongs, Long profileId) throws ProfileNotFoundException, PersistenceException;

    /**
     * Sets the agent profile for the agent
     * 
     * @param id
     *            id of the agent
     * @param profileName
     *            name of the profile to set
     * @throws InvalidIDException
     *             if the agent id is unknown
     */
    public void setAgentProfile(Long id, String profileName) throws PersistenceException, InvalidIDException, ProfileNotFoundException;

    /**
     * Retrieve the list of known agent types.  
     * 
     * @return the list of known agent types.  The list will be ordered by agent type default title
     */
    public List<IAgentType> getAgentTypes();

    /**
     * Retrieve an agent type by if
     * 
     * @param id
     *            the id of the agent type to retrieve
     * @return the agent type associated with the specified id
     * @throws IllegalArgumentException
     *             if the id does not correspond to a known agent type
     */
    public IAgentType getAgentType(String id);
}
