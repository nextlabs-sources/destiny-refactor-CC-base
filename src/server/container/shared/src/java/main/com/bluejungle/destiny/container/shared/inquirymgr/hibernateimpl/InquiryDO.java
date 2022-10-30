/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Iterator;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryApplication;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryObligation;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the inquiry data object. This class can be persisted in Hibernate,
 * and extends the basic (non persistent) inquiry class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryDO.java#1 $
 */

public class InquiryDO extends InquiryImpl implements IPersistentInquiry {

    private Long id;

    /**
     * Constructor
     */
    public InquiryDO() {
        super();
    }

    /**
     * 
     * Constructor from a model object
     * 
     * @param model
     *            model to use
     */
    public InquiryDO(IPersistentInquiry inquiry) {
        if (inquiry != null) {
            Iterator it = inquiry.getActions().iterator();
            while (it.hasNext()) {
                IInquiryAction action = (IInquiryAction) it.next();
                addAction(action.getActionType());
            }
            it = inquiry.getApplications().iterator();
            while (it.hasNext()) {
                IInquiryApplication app = (IInquiryApplication) it.next();
                addApplication(app.getName());
            }
            it = inquiry.getObligations().iterator();
            while (it.hasNext()) {
                IInquiryObligation obligation = (IInquiryObligation) it.next();
                addObligation(obligation.getName());
            }
            it = inquiry.getPolicies().iterator();
            while (it.hasNext()) {
                IInquiryPolicy policy = (IInquiryPolicy) it.next();
                addPolicy(policy.getName());
            }
            it = inquiry.getPolicyDecisions().iterator();
            while (it.hasNext()) {
                IInquiryPolicyDecision policyDecision = (IInquiryPolicyDecision) it.next();
                addPolicyDecision(policyDecision.getPolicyDecisionType());
            }
            it = inquiry.getResources().iterator();
            while (it.hasNext()) {
                IInquiryResource resource = (IInquiryResource) it.next();
                addResource(resource.getName());
            }
            setTargetData(inquiry.getTargetData());
            it = inquiry.getUsers().iterator();
            while (it.hasNext()) {
                IInquiryUser user = (IInquiryUser) it.next();
                addUser(user.getDisplayName());
            }
            setLoggingLevel(inquiry.getLoggingLevel());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addAction(java.lang.String)
     */
    public void addAction(ActionEnumType actionType) {
        if (actionType == null) {
            throw new NullPointerException("Action type cannot be null");
        }
        InquiryActionDO actionDO = new InquiryActionDO();
        actionDO.setInquiry(this);
        actionDO.setActionType(actionType);
        this.actions.add(actionDO);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addApplication(java.lang.String)
     */
    public void addApplication(String newApplicationExpr) {
        if (newApplicationExpr == null) {
            throw new NullPointerException("Application cannot be null");
        }
        if (newApplicationExpr.length() > 0) {
            InquiryApplicationDO appDO = new InquiryApplicationDO();
            appDO.setInquiry(this);
            appDO.setName(newApplicationExpr);
            this.applications.add(appDO);
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
            InquiryObligationDO obligationDO = new InquiryObligationDO();
            obligationDO.setInquiry(this);
            obligationDO.setName(obligationName);
            this.obligations.add(obligationDO);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addPolicy(java.lang.String)
     */
    public void addPolicy(String policyName) {
        if (policyName == null) {
            throw new NullPointerException("Policy name cannot be null");
        }
        if (policyName.length() > 0) {
            InquiryPolicyDO policyDO = new InquiryPolicyDO();
            policyDO.setInquiry(this);
            policyDO.setName(policyName);
            this.policies.add(policyDO);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addPolicyDecision(com.bluejungle.domain.policydecision.PolicyDecisionEnumType)
     */
    public void addPolicyDecision(PolicyDecisionEnumType decisionType) {
        if (decisionType == null) {
            throw new NullPointerException("Policy decision type cannot be null");
        }
        InquiryPolicyDecisionDO newDecision = new InquiryPolicyDecisionDO();
        newDecision.setInquiry(this);
        newDecision.setPolicyDecisionType(decisionType);
        this.policyDecisions.add(newDecision);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiry#addResource(java.lang.String)
     */
    public void addResource(String resourceName) {
        if (resourceName == null) {
            throw new NullPointerException("Resource name cannot be null");
        }
        if (resourceName.length() > 0) {
            InquiryResourceDO resourceDO = new InquiryResourceDO();
            resourceDO.setInquiry(this);
            resourceDO.setName(resourceName);
            this.resources.add(resourceDO);
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
            InquiryUserDO userDO = new InquiryUserDO();
            userDO.setInquiry(this);
            userDO.setDisplayName(user);
            this.users.add(userDO);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the inquiry id
     * 
     * @param newId
     *            id to set
     */
    protected void setId(Long newId) {
        this.id = newId;
    }
}