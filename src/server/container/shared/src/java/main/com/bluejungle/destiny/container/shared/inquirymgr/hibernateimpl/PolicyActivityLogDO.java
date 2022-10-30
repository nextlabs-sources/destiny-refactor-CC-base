/*
 * Created on Mar 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the data object for the policy activity log.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PolicyActivityLogDO.java#1 $
 */

public class PolicyActivityLogDO extends BaseActivityLogDO implements IPolicyActivityLog {

    private Long decisionRequestId;
    private PolicyDecisionEnumType policyDecision;
    private Long policyId;
    private String userResponse;

    /**
     * Constructor
     */
    public PolicyActivityLogDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicyActivityLog#getDecisionRequestId()
     */
    public Long getDecisionRequestId() {
        return this.decisionRequestId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicyActivityLog#getPolicyDecision()
     */
    public PolicyDecisionEnumType getPolicyDecision() {
        return this.policyDecision;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicyActivityLog#getPolicyId()
     */
    public Long getPolicyId() {
        return this.policyId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicyActivityLog#getUserResponse()
     */
    public String getUserResponse() {
        return this.userResponse;
    }

    /**
     * Sets the decision request id
     * 
     * @param newId
     *            new decision request id to set
     */
    public void setDecisionRequestId(Long newId) {
        this.decisionRequestId = newId;
    }

    /**
     * Sets the policy decision
     * 
     * @param newDecision
     *            new policy decision to set
     */
    public void setPolicyDecision(PolicyDecisionEnumType newDecision) {
        this.policyDecision = newDecision;
    }

    /**
     * Sets the policy id
     * 
     * @param newDecision
     *            new policy id to set
     */
    public void setPolicyId(Long newId) {
        this.policyId = newId;
    }

    /**
     * Sets the user response
     * 
     * @param newResponse
     *            new response to set
     */
    public void setUserResponse(String newResponse) {
        this.userResponse = newResponse;
    }
}