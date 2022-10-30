/*
 * All sources, binaries and HTML pages (C) Copyright 2010
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * NextLabs Inc. All rights reserved worldwide.
 *
 */
package com.bluejungle.pf.destiny.policymap;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

public class ResolvedPolicyData {
    
    IDPolicy[] parsedPolicies;
    List<Long> policyIds = new ArrayList<Long>();
    
    public ResolvedPolicyData( long[] policyIds, IDPolicy[] parsedPolicies) {
        this.parsedPolicies = parsedPolicies;
        for (long thisId :  policyIds) {
            this.policyIds.add(thisId);
        }
    }
    
    public List<Long> getPolicyIds() {
        return policyIds;
    }
    
    public void setPolicyIds(List<Long> policyIds) {
        this.policyIds = policyIds;
    }
    
    public IDPolicy[] getParsedPolicies() {
        return parsedPolicies;
    }

    public void setParsedPolicies(IDPolicy[] parsedPolicies) {
        this.parsedPolicies = parsedPolicies;
    }
}