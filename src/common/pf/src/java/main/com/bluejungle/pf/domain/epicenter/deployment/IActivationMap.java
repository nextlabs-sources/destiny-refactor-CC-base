package com.bluejungle.pf.domain.epicenter.deployment;

import java.util.Set;

import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

// Copyright Blue Jungle, Inc.

/*
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/epicenter/deployment/IActivationMap.java#1 $
 * 
 * ActivationMap is a convenience device for policy evaluation.  It
 * maps policy to all the revalation activations.
 */

public interface IActivationMap {

    /**
     * effects: returns a set of activation ids for a given policy
     * 
     * @param policy in question
     * @return ids of all relevant activations
     */
    Set getActivations(IPolicy policy);
}
