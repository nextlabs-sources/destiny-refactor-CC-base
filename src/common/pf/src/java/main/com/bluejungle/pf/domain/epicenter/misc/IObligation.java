package com.bluejungle.pf.domain.epicenter.misc;

import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.subject.ISubject;

// Copyright Blue Jungle, Inc.

/*
 * IObligation represents an obligation, i.e. an activity which happens when a
 * given policy/rule/set is determined to be applicable.
 * 
 * @author Sasha Vladimirov
 * 
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/common/pf/com/bluejungle/pf/domain/epicenter/misc/IObligation.java#1 $
 */

public interface IObligation {

    /**
     * sets the policy that this obligation is attached to
     * 
     * @param po policy 
     */
    void setPolicy(IPolicy po);

    /**
     * removes the policy that this obligation is attached to,
     * effectively detaching this obligation from a particular policy
     */
    void removePolicy();
    
    /**
     * Returns string type associated with the obligation
     * 
     */
    String getType ();
}
