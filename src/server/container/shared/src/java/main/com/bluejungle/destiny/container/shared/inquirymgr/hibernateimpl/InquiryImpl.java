/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the "on-the-fly" implementation of an inquiry object. This class is
 * used to create inquiries that are used directly in memory.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryImpl.java#1 $
 */

public class InquiryImpl implements IInquiry {

    protected Set actions = new HashSet();
    protected Set applications = new HashSet();
    protected Set obligations = new HashSet();
    protected Set policies = new HashSet();
    protected Set policyDecisions = new HashSet();
    protected Set resources = new HashSet();
    protected Set users = new HashSet();
    protected ISortSpec sortSpec;
    protected InquiryTargetDataType targetData;
    protected int loggingLevel;

    /**
     * Constructor
     *  
     */
    public InquiryImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addAction(java.lang.String)
     */
    public void addAction(final ActionEnumType actionType) {
        if (actionType == null) {
            throw new NullPointerException("ActionType cannot be null");
        }
        InquiryActionImpl newAction = new InquiryActionImpl();
        newAction.setInquiry(this);
        newAction.setActionType(actionType);
        this.actions.add(newAction);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addApplication(java.lang.String)
     */
    public void addApplication(final String newApplicationExpr) {
        if (newApplicationExpr == null) {
            throw new NullPointerException("Application cannot be null");
        }
        if (newApplicationExpr.length() > 0) {
            InquiryApplicationImpl newApp = new InquiryApplicationImpl();
            newApp.setInquiry(this);
            newApp.setName(newApplicationExpr);
            this.applications.add(newApp);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addObligation(java.lang.String)
     */
    public void addObligation(String obligationName) {
        if (obligationName == null) {
            throw new NullPointerException("Obligation name cannot be null");
        }
        if (obligationName.length() > 0) {
            InquiryObligationImpl newObligation = new InquiryObligationImpl();
            newObligation.setInquiry(this);
            newObligation.setName(obligationName);
            this.obligations.add(newObligation);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addPolicy(java.lang.String)
     */
    public void addPolicy(final String policyName) {
        if (policyName == null) {
            throw new NullPointerException("Policy name cannot be null");
        }
        if (policyName.length() > 0) {
            InquiryPolicyImpl newPolicy = new InquiryPolicyImpl();
            newPolicy.setInquiry(this);
            newPolicy.setName(policyName);
            this.policies.add(newPolicy);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addPolicyDecision(com.bluejungle.domain.policydecision.PolicyDecisionEnumType)
     */
    public void addPolicyDecision(final PolicyDecisionEnumType decisionType) {
        if (decisionType == null) {
            throw new NullPointerException("effectType cannot be null");
        }
        InquiryPolicyDecisionImpl newPolicyDecision = new InquiryPolicyDecisionImpl();
        newPolicyDecision.setInquiry(this);
        newPolicyDecision.setPolicyDecisionType(decisionType);
        this.policyDecisions.add(newPolicyDecision);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addResource(java.lang.String)
     */
    public void addResource(final String resourceName) {
        if (resourceName == null) {
            throw new NullPointerException("Resource name cannot be null");
        }
        if (resourceName.length() > 0) {
            InquiryResourceImpl newResource = new InquiryResourceImpl();
            newResource.setInquiry(this);
            newResource.setName(resourceName);
            this.resources.add(newResource);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addUser(java.lang.String)
     */
    public void addUser(String user) {
        if (user == null) {
            throw new NullPointerException("User name cannot be null");
        }
        if (user.length() > 0) {
            InquiryUserImpl newUser = new InquiryUserImpl();
            newUser.setInquiry(this);
            newUser.setDisplayName(user);
            this.users.add(newUser);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getActions()
     */
    public Set getActions() {
        return this.actions;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getApplications()
     */
    public Set getApplications() {
        return this.applications;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getObligations()
     */
    public Set getObligations() {
        return this.obligations;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getPolicies()
     */
    public Set getPolicies() {
        return this.policies;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getPolicyDecisions()
     */
    public Set getPolicyDecisions() {
        return this.policyDecisions;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getResources()
     */
    public Set getResources() {
        return this.resources;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getTargetData()
     */
    public InquiryTargetDataType getTargetData() {
        return this.targetData;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getUsers()
     */
    public Set getUsers() {
        return this.users;
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#getLoggingLevel()
     */
    public int getLoggingLevel() {
        return this.loggingLevel;
    }

    /**
     * Sets a new set of actions.
     * 
     * @param newActions
     *            new set of action to be set
     */
    public void setActions(Set newActions) {
        this.actions = newActions;
    }

    /**
     * Sets a new set of applications
     * 
     * @param newApps
     *            new applications to set
     */
    public void setApplications(Set newApps) {
        this.applications = newApps;
    }

    /**
     * Sets a new set of obligations
     * 
     * @param newObligations
     *            new set of obligations to be set
     */
    public void setObligations(Set newObligations) {
        this.obligations = newObligations;
    }

    /**
     * Sets a new set of policies
     * 
     * @param newPolicies
     *            new set of obligations to be set
     */
    public void setPolicies(Set newPolicies) {
        this.policies = newPolicies;
    }

    /**
     * Sets a new set of policy decisions
     * 
     * @param newPolicyDecisions
     *            new set of policy decisions to set
     */
    public void setPolicyDecisions(Set newPolicyDecisions) {
        this.policyDecisions = newPolicyDecisions;
    }

    /**
     * Sets a new set of resources
     * 
     * @param resources
     *            new set of resources to be set
     */
    public void setResources(Set newResources) {
        this.resources = newResources;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#setTargetData(com.bluejungle.destiny.container.shared.inquirymgr.IInquiryTargetDataType)
     */
    public void setTargetData(InquiryTargetDataType target) {
        this.targetData = target;
    }

    /**
     * Sets a new list of users
     * 
     * @param newUsers
     *            new list of users to be set
     */
    public void setUsers(Set newUsers) {
        this.users = newUsers;
    }
    
    /**
     * Sets the logging level
     * 
     * @param loggingLevel
     *            new logging level to be set
     */
    public void setLoggingLevel(int loggingLevel){
        this.loggingLevel = loggingLevel;
    }
}