package com.bluejungle.destiny.container.shared.agentmgr;

import java.util.Set;

/**
 * IAgentType represents a type of agent (e.g. file server, desktop, sharepoint,
 * etc)
 * 
 * @author sgoldstein
 */
public interface IAgentType {

    /**
     * Retrieve the id of this agent type
     * 
     * @return the id of this agent type
     */
    public abstract String getId();

    /**
     * Retrieve the default title of this agent type
     * 
     * @return the default title of this agent type
     */
    public abstract String getTitle();

    /**
     * Retrieve the set of actions that this agent type supports
     * 
     * @return the set of actions that this agent type supports
     */
    public abstract Set<IActionType> getActionTypes();
}
