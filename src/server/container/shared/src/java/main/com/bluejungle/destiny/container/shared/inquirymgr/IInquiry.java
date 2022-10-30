/*
 * Created on Jan 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.Set;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the inquiry object interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IInquiry.java#1 $
 */

public interface IInquiry {

    /**
     * Add a new action to the inquiry.
     * 
     * @param actionExpr
     *            new action for the inquiry
     */
    public void addAction(ActionEnumType action);

    /**
     * Add a new application to the inquiry
     * 
     * @param application
     *            expression for the inquiry
     */
    public void addApplication(String applicationExpr);

    /**
     * Add an obligation to the inquiry.
     * 
     * @param obligation
     *            new obligation to add
     */
    public void addObligation(String obligation);

    /**
     * Add a policy to the inquiry
     * 
     * @param policy
     *            new policy to add
     */
    public void addPolicy(String policy);

    /**
     * Add a new effect to the inquiry
     * 
     * @param effect
     *            new effect to add
     */
    public void addPolicyDecision(PolicyDecisionEnumType effect);

    /**
     * Add a resource or resource class to the inquiry
     * 
     * @param resource
     *            resource to add
     */
    public void addResource(String resource);

    /**
     * Add a user or user group to the inquiry
     * 
     * @param user
     *            name of the user or the group to add
     */
    public void addUser(String user);

    /**
     * Returns the set of actions targeted by the inquiry
     * 
     * @return the set of actions targeted by the inquiry
     */
    public Set getActions();

    /**
     * Returns the set of applications targeted by the inquiry
     * 
     * @return the set of applications targeted by the inquiry
     */
    public Set getApplications();

    /**
     * Returns the set of obligations targeted by the inquiry
     * 
     * @return the set of obligations targeted by the inquiry
     */
    public Set getObligations();

    /**
     * Returns the set of policies targeted by the inquiry
     * 
     * @return the set policies targeted by the inquiry
     */
    public Set getPolicies();

    /**
     * Returns the set of effects targeted by the inquiry
     * 
     * @return the set of effects targeted by the inquiry
     */
    public Set getPolicyDecisions();

    /**
     * Returns the set of resources targeted by the inquiry
     * 
     * @return the set of resources targeted by the inquiry
     */
    public Set getResources();

    /**
     * Returns the type of data fetched for the inquiry
     * 
     * @return the type of data fetched for the inquiry
     */
    public InquiryTargetDataType getTargetData();

    /**
     * Returns the users or user groups targeted by the inquiry.
     * 
     * @return the users or user groups targeted by the inquiry
     */
    public Set getUsers();

    /**
     * Returns the logging level of the inquiry.
     * 
     * @return the logging level of the inquiry
     */
    public int getLoggingLevel();
    
    /**
     * Sets the inquiry type (if necessary). The inquiry type decides the type
     * of data to retrieve from the database.
     * 
     * @param type
     *            type of data to query
     */
    public void setTargetData(InquiryTargetDataType target);
       
    /**
     * Sets the logging level. The logging level decides the level
     * of data to retrieve from the database.
     * 
     * @param level
     *            level of data to query
     */
    public void setLoggingLevel(int level);
}