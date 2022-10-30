/*
 * Created on Jan 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * Data Object for storing a policy activity log entry
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/components/log/hibernateimpl/PolicyActivityLogEntry.java#3 $
 */

public final class PolicyActivityLogEntry extends BaseLogEntry implements Externalizable {

    private long policyId;
    private PolicyActivityInfo info;
    
    



    /** 
     * Default constructor for Hibernate
     *
     */
    public PolicyActivityLogEntry() {
        
    }
    
    public PolicyActivityLogEntry(PolicyActivityInfo info, long policyId, long uid) {
        super(uid, info.ts);
        this.info = info;
        this.policyId = policyId;
    }    
    
    public PolicyActivityLogEntry(PolicyActivityInfo info, long policyId) {
        super();
        super.setTimestamp(info.ts);
        this.info = info;
        this.policyId = policyId;
    }
    
    /**
     * Returns the policyId.
     * 
     * @return the policyId.
     */
    public final long getPolicyId() {
        return this.policyId;
    }

    /**
     * Returns the decisionRequestId.
     * 
     * @return the decisionRequestId.
     */
    public final long getDecisionRequestId() {
        return info.decisionRequestId;
    }

    /**
     * Returns the "from Resource" information
     * 
     * @return the "from Resource" information
     */
    public final FromResourceInformation getFromResourceInfo() {
        return info.fromResourceInfo;
    }

    /**
     * Returns the hostId.
     * 
     * @return the hostId.
     */
    public final long getHostId() {
        return info.hostId;
    }

    /**
     * Returns the "to resource" information
     * 
     * @return the "to resource" information
     */
    public final ToResourceInformation getToResourceInfo() {
        return info.toResourceInfo;
    }
    
    /**
     * Returns the custom attributes
     * 
     * @return the custom attributes
     */
    public final DynamicAttributes getCustomAttributes(){
        return info.customAttr;
    }


    /**
     * Returns the userId.
     * 
     * @return the userId.
     */
    public final long getUserId() {
        return info.userId;
    }

    public final String getUserName() {
        return info.userName;
    }
    /**
     * Returns the action.
     * 
     * @return the action.
     */
    public final ActionEnumType getAction() {
        return info.action;
    }

    /**
     * Returns the applicationId.
     * 
     * @return the applicationId.
     */
    public final long getApplicationId() {
        return info.applicationId;
    }

    /**
     * Returns the application name
     * 
     * @return the application name
     */
    public final String getApplicationName() {
        return info.applicationName;
    }

    /**
     * Returns the decision.
     * 
     * @return the decision.
     */
    public final PolicyDecisionEnumType getPolicyDecision() {
        return info.policyDecision;
    }
    
    public final void setPolicyDecision(PolicyDecisionEnumType policyDecision) {
        info.policyDecision = policyDecision;
    }

    public final String getHostName() {
        return info.hostName;
    }
    
    public final String getHostIP() {
        return info.hostIP;
    }
    
    /**
     * Returns the logging level.
     * 
     * @return the logging level.
     */
    public final int getLevel() {
        return info.level;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        policyId = in.readLong();
        info = new PolicyActivityInfo();
        info.readExternal(in);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(policyId);
        info.writeExternal(out);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PolicyActivityLogEntry)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        PolicyActivityLogEntry entry = (PolicyActivityLogEntry) obj;
        
        return (super.equals(entry) && 
                policyId == entry.policyId && 
                info.equals(entry.info));
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuffer rv = new StringBuffer("PolicyActivityLogEntry[");
        rv.append(super.toString());
        rv.append(", policyId: " + policyId);
        rv.append(", info: " + info);
        return rv.toString();
    }
    
    
    
}
