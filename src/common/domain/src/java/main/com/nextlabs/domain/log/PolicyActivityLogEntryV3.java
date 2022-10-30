/*
 * Created on Sep 12, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/nextlabs/domain/log/PolicyActivityLogEntryV3.java#1 $:
 */
package com.nextlabs.domain.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityLogEntryWrapper;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;

public final class PolicyActivityLogEntryV3 extends BaseLogEntry implements Externalizable, PolicyActivityLogEntryWrapper {
    private long policyId;
    private PolicyActivityInfoV3 info;

    /**
     * Default constructor for Hibernate
     */
    public PolicyActivityLogEntryV3() {
    }

    public PolicyActivityLogEntryV3(PolicyActivityInfoV3 info, long policyId, long uid) {
        super(uid, info.ts);
        this.info = info;
        this.policyId = policyId;
    }

    public PolicyActivityLogEntryV3(PolicyActivityInfoV3 info, long policyId) {
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
    @Override
    public long getPolicyId() {
        return policyId;
    }

    @Override
    public List<IPair<String, String>> getPolicyTags() {
        return Collections.<IPair<String, String>>emptyList();
    }

    /**
     * Returns the decisionRequestId.
     * 
     * @return the decisionRequestId.
     */
    @Override
    public final long getDecisionRequestId() {
        return info.decisionRequestId;
    }


    /**
     * Returns the "from Resource" information
     * 
     * @return the "from Resource" information
     */
    @Override
    public final FromResourceInformation getFromResourceInfo() {
        return info.fromResourceInfo;
    }

    /**
     * Returns the hostId.
     * 
     * @return the hostId.
     */
    @Override
    public final long getHostId() {
        return info.hostId;
    }

    /**
     * Returns the "to resource" information
     * 
     * @return the "to resource" information
     */
    @Override
    public final ToResourceInformation getToResourceInfo() {
        return info.toResourceInfo;
    }
    
    /**
     * Returns the custom attributes
     * 
     * @return the custom attributes
     */
    @Override
    public final DynamicAttributes getCustomAttributes(){
        return info.customAttr;
    }
    
    /**
     * Returns a map of custom attributes
     * 
     * @return the custom attributes map
     */
    @Override
    public Map<String, DynamicAttributes> getAttributesMap(){
    	Map <String, DynamicAttributes> attributesMap = new HashMap<String, DynamicAttributes>();
    	attributesMap.put(PolicyActivityInfoV5.FROM_RESOURCE_ATTRIBUTES_TAG, getCustomAttributes());
    	return attributesMap;
    }

    /**
     * Returns the userId.
     * 
     * @return the userId.
     */
    @Override
    public final long getUserId() {
        return info.userId;
    }

    @Override
    public final String getUserName() {
        return info.userName;
    }
    /**
     * Returns the action.
     * 
     * @return the action.
     */
    @Override
    public final String getAction() {
        return info.action;
    }

    /**
     * Returns the applicationId.
     * 
     * @return the applicationId.
     */
    @Override
    public final long getApplicationId() {
        return info.applicationId;
    }

    /**
     * Returns the application name
     * 
     * @return the application name
     */
    @Override
    public final String getApplicationName() {
        return info.applicationName;
    }

    /**
     * Returns the decision.
     * 
     * @return the decision.
     */
    @Override
    public final PolicyDecisionEnumType getPolicyDecision() {
        return info.policyDecision;
    }
    
    public final void setPolicyDecision(PolicyDecisionEnumType policyDecision) {
        info.policyDecision = policyDecision;
    }

    @Override
    public final String getHostName() {
        return info.hostName;
    }
    
    @Override
    public final String getHostIP() {
        return info.hostIP;
    }
    
    /**
     * Returns the logging level.
     * 
     * @return the logging level.
     */
    @Override
    public final int getLevel() {
        return info.level;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        policyId = in.readLong();
        info = new PolicyActivityInfoV3();
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
        if (!(obj instanceof PolicyActivityLogEntryV3)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        PolicyActivityLogEntryV3 entry = (PolicyActivityLogEntryV3) obj;
        
        return (super.equals(entry) && 
                policyId == entry.policyId && 
                info.equals(entry.info));
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuffer rv = new StringBuffer("PolicyActivityLogEntryV3[");
        rv.append(super.toString());
        rv.append(", policyId: " + policyId);
        rv.append(", info: " + info);
        return rv.toString();
    }
    
}
