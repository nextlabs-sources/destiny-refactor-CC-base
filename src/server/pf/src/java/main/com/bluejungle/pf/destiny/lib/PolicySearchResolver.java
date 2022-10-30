package com.bluejungle.pf.destiny.lib;

import java.util.BitSet;
import java.util.List;

import com.bluejungle.pf.destiny.policymap.ResolvedPolicyData;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;
import com.bluejungle.pf.engine.destiny.ITargetResolver;

public class PolicySearchResolver implements ITargetResolver {
    private ResolvedPolicyData resolvedPolicyData;
    private BitSet applicables;

    public PolicySearchResolver(ResolvedPolicyData resolvedPolicyData) {
        this.resolvedPolicyData = resolvedPolicyData;
        
        applicables = new BitSet(resolvedPolicyData.getParsedPolicies().length);

        // Hidden policies have a special meaning and we shouldn't be evaluating against them
        int i = 0;
        for (IDPolicy policy : resolvedPolicyData.getParsedPolicies()) {
            if (!policy.isHidden()) {
                applicables.set(i);
            }
            i++;
        }
    }
    
    public BitSet getApplicablePolicies(EvaluationRequest request) {
        return applicables;
    }

    public List<Long> getApplicablePolicyIDs(EvaluationRequest  request) { 
        return resolvedPolicyData.getPolicyIds();
    }
    
    public IDPolicy[] getPolicies() {
        return resolvedPolicyData.getParsedPolicies();
    }
}
