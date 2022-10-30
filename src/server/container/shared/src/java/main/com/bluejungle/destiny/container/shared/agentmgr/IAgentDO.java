/*
 * Created on Jan 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.framework.domain.IDomainObject;
import com.bluejungle.version.IVersion;

/**
 * Interface to represent a persisted Agent object. Implementations will be
 * specific to the persistence mechanism used.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IAgentDO extends IDomainObject {

    /*
     * Constants
     */
    public static final int ACCEPTABLE_LOST_HEARTBEATS = 2;

    /**
     * Returns the type of agent
     * 
     * @return string representing the agent type
     */
    public IAgentType getType();

    /**
     * Returns the host name on which this agent is installed
     * 
     * @return
     */
    public String getHost();

    /**
     * Returns the timestamp of the last heartbeat received from this agent
     * 
     * @return last heartbeat date
     */
    public Calendar getLastHeartbeat();

    /**
     * Returns whether the agent is online - based on its last heartbeat and the
     * heartbeat interval.
     * 
     * @return boolean whether the agent is active
     */
    public boolean isOnline();

    /**
     * Returns the name of the agent profile
     * 
     * @return agent profile object
     */
    public IAgentProfileDO getAgentProfile();

    /**
     * Returns the name of the communication profile for this agent
     * 
     * @return comm profile object
     */
    public ICommProfileDO getCommProfile();

    /**
     * Returns whether push is enabled
     * 
     * @return boolean
     */
    public boolean getIsPushReady();

    /**
     * Returns the push port
     * 
     * @return push port number
     */
    public Integer getPushPort();

    /**
     * Check to see if the agent has a push port set
     * 
     * @return true if the push port is set; false otherwise
     */
    public boolean hasPushPort();

    /**
     * Returns the status object for agent profile status
     * 
     * @return profile status
     */
    public IAgentProfileStatus getProfileStatus();

    /**
     * Returns the status object for agent policy assembly status
     * 
     * @return policy assembly status
     */
    public IAgentPolicyAssemblyStatus getPolicyAssemblyStatus();

    /**
     * Returns the version of the component
     * 
     * @return the version of the component
     */
    public IVersion getVersion();

}
