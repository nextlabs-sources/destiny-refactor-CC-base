/*
 * Created on Dec 16, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author ichiang
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/nextlabs/domain/log/PolicyActivityLogEntryV5.java#1 $:
 */
package com.nextlabs.domain.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityLogEntryWrapper;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;

public final class PolicyActivityLogEntryV5 extends BaseLogEntry implements Externalizable, PolicyActivityLogEntryWrapper {
    private long policyId;
    private PolicyActivityInfoV5 info;
    private Collection<IPair<String, String>> tags = new ArrayList<IPair<String, String>>();

    /**
     * Default constructor for Hibernate
     */
    public PolicyActivityLogEntryV5() {
    }

    public PolicyActivityLogEntryV5(final PolicyActivityInfoV5 info, final long policyId, final Collection<IPair<String, String>> tags, final long uid) {
        super(uid, info.ts);
        this.info = info;
        this.policyId = policyId;
        this.tags = tags;
    }

    public PolicyActivityLogEntryV5(final PolicyActivityInfoV5 info, final long policyId, final long uid) {
        super(uid, info.ts);
        this.info = info;
        this.policyId = policyId;
    }

    public PolicyActivityLogEntryV5(final PolicyActivityInfoV5 info, final long policyId) {
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

    /**
     * Returns the tags.
     * 
     * @return the tags.
     */
    @Override
    public Collection<IPair<String, String>> getPolicyTags() {
        return tags;
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
    public final Map<String,DynamicAttributes> getAttributesMap(){
        return info.attributesMap;
    }
    
    @Override
    public final DynamicAttributes getCustomAttributes(){
        return null;
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
    
    public final void setPolicyDecision(final PolicyDecisionEnumType policyDecision) {
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

    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        policyId = in.readLong();
        tags = new ArrayList<IPair<String, String>>();
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            String value = in.readUTF();

            tags.add(new Pair(key, value));
        }

        info = new PolicyActivityInfoV5();
        info.readExternal(in);
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(policyId);
        out.writeInt(tags.size());
        for(final IPair<String, String> tag : tags) {
            out.writeUTF(tag.first());
            out.writeUTF(tag.second());
        }

        info.writeExternal(out);
    }

    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PolicyActivityLogEntryV5)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final PolicyActivityLogEntryV5 entry = (PolicyActivityLogEntryV5) obj;
        
        return (super.equals(entry) && 
                policyId == entry.policyId && 
                tags.equals(entry.tags) &&
                info.equals(entry.info));
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        final StringBuffer rv = new StringBuffer("PolicyActivityLogEntryV5[");
        rv.append(super.toString());
        rv.append(", policyId: " + policyId);
        for (IPair<String, String> tag : tags) {
            rv.append(", " + tag.first() + "=" + tag.second());
        }
        rv.append(", info: " + info);
        return rv.toString();
    }
    
}
