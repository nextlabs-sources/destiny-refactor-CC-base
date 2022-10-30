/*
 * Created on Aug 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This is the "on the fly" implementation of the inquiry policy decision class.
 * This class represents a policy decision within the context of an inquiry.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryPolicyDecisionImpl.java#1 $
 */

public class InquiryPolicyDecisionImpl implements IInquiryPolicyDecision {

    private IInquiry inquiry;
    private PolicyDecisionEnumType policyDecisionType;

    /**
     * Constructor
     */
    public InquiryPolicyDecisionImpl() {
        super();
    }

    /**
     * Returns the inquiry associated with this obligation
     * 
     * @return the inquiry associated with this obligation
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicyDecision#getPolicyDecisionType()
     */
    public PolicyDecisionEnumType getPolicyDecisionType() {
        return this.policyDecisionType;
    }

    /**
     * Sets the inquiry associated with this obligation
     * 
     * @param newInquiry
     *            inquiry to be set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }

    /**
     * Sets the policy decision type
     * 
     * @param newType
     *            new decision type to set
     */
    protected void setPolicyDecisionType(PolicyDecisionEnumType newType) {
        this.policyDecisionType = newType;
    }
}