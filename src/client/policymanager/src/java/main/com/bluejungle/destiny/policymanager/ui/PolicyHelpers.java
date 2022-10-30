/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;

/**
 * @author dstarke
 * 
 */
public class PolicyHelpers {

    public static int getIndexForEffect(IEffectType main, IEffectType otherwise) {
        if (main == EffectType.DENY) {
            return 0;
        } else if (main == EffectType.ALLOW) {
            if (otherwise != null && otherwise == EffectType.DENY) {
                return 1;
            } else {
                return 2;
            }
        }
        return -1;
    }

    public static void saveEffect(IDPolicy policy, int index) {
        switch (index) {
        case 0:
            policy.setMainEffect(EffectType.DENY);
            policy.setOtherwiseEffect(EffectType.ALLOW);
            break;
        case 1:
            policy.setMainEffect(EffectType.ALLOW);
            policy.setOtherwiseEffect(EffectType.DENY);
            break;
        case 2:
            policy.setMainEffect(EffectType.ALLOW);
            policy.setOtherwiseEffect(null);
            break;
        }
    }
}
