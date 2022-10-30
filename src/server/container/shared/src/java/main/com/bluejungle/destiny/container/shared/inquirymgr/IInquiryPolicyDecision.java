/*
 * Created on Aug 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;

/**
 * This interface represents a policy decision object related to an inquiry
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IInquiryPolicyDecision.java#1 $
 */

public interface IInquiryPolicyDecision {

    /**
     * Returns the policy decision type
     * 
     * @return the policy decision type
     */
    public PolicyDecisionEnumType getPolicyDecisionType();
}