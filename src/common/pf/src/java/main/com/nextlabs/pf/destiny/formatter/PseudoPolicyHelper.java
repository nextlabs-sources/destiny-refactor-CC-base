package com.nextlabs.pf.destiny.formatter;

/*
 * Created on May 03, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/PseudoPolicyHelper.java#1 $:
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.Target;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicyManager;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

public class PseudoPolicyHelper {
    private static final IDPolicyManager policyMgr = ComponentManagerFactory.getComponentManager().getComponent(IDPolicyManager.COMP_INFO);

    public static List<IDPolicy> createPseudoPolicies(IDPolicy policy) {
        if (policy == null) {
            return null;
        }
        
        IEffectType otherwiseEffect = policy.getOtherwiseEffect();

        if (otherwiseEffect == null) {
            // Monitor policy. This can be handled directly.
            return null;
        }

        if (policy.getMainEffect() == EffectType.ALLOW) {
            return createAllowOnlyPseudoPolicies(policy);
        } else {
            return createDenyPseudoPolicies(policy);
        }
    }

    private static List<IDPolicy> createDenyPseudoPolicies(IDPolicy policy) {
        IDPolicy invertedConditionsPolicy = policyMgr.newPolicy((long) -1, policy.getName() + "-Inverted-Conditions");

        invertedConditionsPolicy.setMainEffect(EffectType.ALLOW);
        invertedConditionsPolicy.setTarget(policy.getTarget());
        invertedConditionsPolicy.setConditions(invert(policy.getConditions()));
        copyObligations(policy, invertedConditionsPolicy);

        return Collections.singletonList(invertedConditionsPolicy);
        
    }

    private static List<IDPolicy> createAllowOnlyPseudoPolicies(IDPolicy policy) {
        List<IDPolicy> policies = new ArrayList<IDPolicy>(2);

        // Create two policies. One with the subject inverted and the other with the condition inverted
        IDPolicy invertedSubjectPolicy = policyMgr.newPolicy((long)-1, policy.getName() + "-Inverted-Subject");

        invertedSubjectPolicy.setMainEffect(EffectType.DENY);

        ITarget newTarget = new Target();
        newTarget.setActionPred(policy.getTarget().getActionPred());
        newTarget.setFromResourcePred(policy.getTarget().getFromResourcePred());
        newTarget.setToResourcePred(policy.getTarget().getToResourcePred());
        newTarget.setSubjectPred(invert(policy.getTarget().getSubjectPred()));
        newTarget.setToSubjectPred(invert(policy.getTarget().getToSubjectPred()));
        invertedSubjectPolicy.setTarget(newTarget);
        invertedSubjectPolicy.setConditions(policy.getConditions());
        copyObligations(policy, invertedSubjectPolicy);
        invertedSubjectPolicy.setPolicyExceptions(policy.getPolicyExceptions());

        policies.add(invertedSubjectPolicy);


        IDPolicy invertedConditionsPolicy = policyMgr.newPolicy((long)-1, policy.getName() + "-Inverted-Conditions");
        invertedConditionsPolicy.setMainEffect(EffectType.DENY);
        invertedConditionsPolicy.setTarget(policy.getTarget());
        invertedConditionsPolicy.setConditions(invert(policy.getConditions()));
        copyObligations(policy, invertedConditionsPolicy);
        invertedConditionsPolicy.setPolicyExceptions(policy.getPolicyExceptions());
        
        policies.add(invertedConditionsPolicy);

        return policies;
    }

    private static IPredicate invert(IPredicate pred) {
        if (pred == null) {
            return PredicateConstants.FALSE;
        }

        return new CompositePredicate(BooleanOp.NOT, pred);
    }

    private static void copyObligations(IDPolicy source, IDPolicy destination) {
        for (EffectType effectType : EffectType.elements()) {
            for (IObligation obl : source.getObligations(effectType)) {
                destination.addObligation(obl, effectType);
            }
        }

        return;
    }
}

