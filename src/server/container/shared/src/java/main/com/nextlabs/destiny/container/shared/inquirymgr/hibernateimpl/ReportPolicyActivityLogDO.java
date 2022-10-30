/* 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the mirror class (of the corresponding 'OLTP' class) that maps to 
 * the table that is used for reporting.
 */
public class ReportPolicyActivityLogDO extends ReportBaseLogDO {
    
    private Long decisionRequestId;
    private PolicyDecisionEnumType policyDecision;
    private Long policyId;
    
    /**
     * These are the new fields added in the mirror table that will be used
     * for reporting. These are here to reduce/remove the need for table joins
     * during queries.
     */
    
    /**
     * From cached_policy.name
     */
    private String policyName; 
    
    /**
     * From cached_policy.fullname
     */
    private String policyFullName;
    
    public Long getDecisionRequestId() {
        return decisionRequestId;
    }
    
    public void setDecisionRequestId(Long decisionRequestId) {
        this.decisionRequestId = decisionRequestId;
    }
    
    public PolicyDecisionEnumType getPolicyDecision() {
        return policyDecision;
    }
    
    public void setPolicyDecision(PolicyDecisionEnumType policyDecision) {
        this.policyDecision = policyDecision;
    }
    
    public Long getPolicyId() {
        return policyId;
    }
    
    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyFullName() {
        return policyFullName;
    }

    public void setPolicyFullName(String policyFullName) {
        this.policyFullName = policyFullName;
    }
}
