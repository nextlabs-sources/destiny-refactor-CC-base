/*
 * Created on Jan 24, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseActivityLogDO;
import com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult;


/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/hibernateimpl/PolicyActivityLogDetailResult.java#1 $
 */

public class PolicyActivityLogDetailResult implements ILogDetailResult {

    private LinkedHashMap customAttributes;
    private BaseActivityLogDO activityLog;
    private String policyName;
    
    
    /**
     * Returns the policyName.
     * @return the policyName.
     */
    public String getPolicyName() {
        return this.policyName;
    }

    
    /**
     * Sets the policyName
     * @param policyName The policyName to set.
     */
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public PolicyActivityLogDetailResult(LinkedHashMap customAttributes, BaseActivityLogDO activityLog, String policyName){
        this.customAttributes = customAttributes;
        this.activityLog = activityLog;
        this.policyName = policyName;
    }
    
    /**
     * @see com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult#getActivityCustomAttributes()
     */
    public LinkedHashMap getActivityCustomAttributes() {
        return this.customAttributes;
    }

    /**
     * @see com.nextlabs.destiny.container.shared.inquirymgr.ILogDetailResult#getActivityLog()
     */
    public BaseActivityLogDO getActivityLog() {
        return this.activityLog;
    }

}
