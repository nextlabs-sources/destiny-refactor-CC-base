package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * IActionType represents a type of action which a particular agent type
 * supports
 * 
 * @author sgoldstein
 */
public interface IActionType {

    /**
     * Retrieve the activity jounrnaling auditing level
     * 
     * @return the activity jounrnaling auditing level
     */
    public IActivityJournalingAuditLevel getActivityJournalingAuditLevel();

    /**
     * Retrieve the id of this action type
     * 
     * @return the id of this action type
     */
    public String getId();

    /**
     * Retrieve the default title of this action type
     * 
     * @return the default title of this action type
     */
    public String getTitle();

}
