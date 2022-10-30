package com.bluejungle.pf.domain.destiny.obligation;

import java.io.Serializable;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.nextlabs.domain.log.PolicyActivityInfoV5;

// Copyright Blue Jungle, Inc.

/**
 * Implements Destiny obligations.
 *
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/DObligation.java#1 $
 */

public abstract class DObligation implements IDObligation, Serializable {
   
    protected IPolicy po;    
   
    DObligation(){
        super();
    }

    public void setPolicy(IPolicy po) {
        this.po = po;
    }
    
    @Override
    public boolean isActivityAcceptable(EvaluationResult res, PolicyActivityInfoV5 args) {
        // By default we do not perform obligations on "copy/paste"
        return (!ActionEnumType.ACTION_PASTE.getName().equals(args.getAction()));
    }

    /**
     * @see com.bluejungle.pf.domain.epicenter.misc.IObligation#removePolicyObject()
     */
    public void removePolicy() {
        po = null;
    }

    public IPolicy getPolicy() {
        return po;
    }
}
