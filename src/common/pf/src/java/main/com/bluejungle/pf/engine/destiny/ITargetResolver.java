/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;


import java.util.BitSet;

import com.bluejungle.pf.domain.destiny.policy.IDPolicy;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/ITargetResolver.java#1 $:
 */

public interface ITargetResolver {

    ITargetResolver EMPTY_RESOLVER = new ITargetResolver() {
        public BitSet getApplicablePolicies(EvaluationRequest request) {
            return new BitSet();
        }

        public IDPolicy[] getPolicies() {
            return new IDPolicy[0];
        }
    };

    /**
     * uses maps to return a set of policies that <b>might</b> be applicable
     * to the given resource, action, user, host, and app, provided the conditions of the policy
     * are also met.  
     * 
     * @param request Evaluation request
     * @return a <code>BitSet</code> such that only ordinals of applicable policies
     * are set
     */
    BitSet getApplicablePolicies(EvaluationRequest request);
    
    /**
     * @return all the policies the resolver contains, such that index into the result
     * corresponds to the ordinal returned by getApplicablePolicies
     */
    IDPolicy[] getPolicies();
    
}
