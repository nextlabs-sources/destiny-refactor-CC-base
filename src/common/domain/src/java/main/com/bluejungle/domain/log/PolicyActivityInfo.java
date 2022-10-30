package com.bluejungle.domain.log;

/*
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author sasha
 * 
 */
public class PolicyActivityInfo implements Externalizable {
    private static final Log log = LogFactory.getLog(PolicyActivityInfo.class.getName());

    FromResourceInformation fromResourceInfo;
    ToResourceInformation toResourceInfo;
    long hostId;
    String hostIP;
    String hostName;
    long userId;
    String userName;
    long applicationId;
    String applicationName;
    ActionEnumType action;
    PolicyDecisionEnumType policyDecision;
    long decisionRequestId;
    long ts;
    int level;
    DynamicAttributes customAttr;

    
    PolicyActivityInfo() {
    }

    /**
     * Copy constructor used by PolicyEvaluatorImpl
     */
    public PolicyActivityInfo(PolicyActivityInfo orig) {
        this(orig.fromResourceInfo,
             orig.toResourceInfo,
             orig.userName,
             orig.userId,
             orig.hostName,
             orig.hostIP,
             orig.hostId,
             orig.applicationName,
             orig.applicationId,
             orig.action,
             orig.policyDecision,
             orig.decisionRequestId,
             orig.ts,
             orig.level,
             new DynamicAttributes());

        customAttr.putAll(orig.customAttr);
    }

    public PolicyActivityInfo(FromResourceInformation fromResourceInfo, 
            ToResourceInformation toResourceInfo, 
            String userName, 
            long userId, 
            String hostName, 
            String hostIP,
            long hostId, 
            String applicationName, 
            long applicationId, 
            ActionEnumType action,
            PolicyDecisionEnumType policyDecision, 
            long decisionRequestId, 
            long ts, 
            int level, 
            DynamicAttributes customAttr) {
        super();
        this.fromResourceInfo = fromResourceInfo;
        this.toResourceInfo = toResourceInfo;
        this.userName = userName;
        this.userId = userId;
        this.hostName = hostName;
        this.hostIP = hostIP;
        this.hostId = hostId;
        this.applicationName = applicationName;
        this.applicationId = applicationId;
        this.action = action;
        this.policyDecision = policyDecision;
        this.decisionRequestId = decisionRequestId;
        this.ts = ts;
        this.level = level;
        this.customAttr = customAttr;
    }

    public ActionEnumType getAction() {
        return action;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getDecisionRequestId() {
        return decisionRequestId;
    }

    public ResourceInformation getFromResourceInfo() {
        return fromResourceInfo;
    }

    public long getHostId() {
        return hostId;
    }

    public String getHostName() {
        return hostName;
    }
    
    public String getHostIP() {
        return hostIP;
    }

    public PolicyDecisionEnumType getPolicyDecision() {
        return policyDecision;
    }

    public ResourceInformation getToResourceInfo() {
        return toResourceInfo;
    }

    public long getTs() {
        return ts;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }
    
    public int getLevel() {
        return level;
    }

    public DynamicAttributes getCustomAttr() {
        return this.customAttr;
    }
    
    /**
     * Sets the customAttr
     * @param customAttr The customAttr to set.
     */
    public void setCustomAttr(DynamicAttributes customAttr) {
        this.customAttr = customAttr;
    }

    public void addCustomAttr(DynamicAttributes customAttr) {
        this.customAttr.putAll(customAttr);
    }

    public void addCustomAttr(String key, String value) {
        this.customAttr.add(key, value);
    }

    public void putCustomAttr(String key, IEvalValue value) {
        this.customAttr.put(key, value);
    }


    public void writeExternal(ObjectOutput out) throws IOException {
        fromResourceInfo.writeExternal(out);
        if (toResourceInfo != null) {
            out.writeBoolean(true);
            toResourceInfo.writeExternal(out);
        } else {
            out.writeBoolean(false);
        }
        out.writeLong(hostId);
        out.writeUTF(hostIP);
        out.writeUTF(hostName);
        out.writeLong(userId);
        out.writeUTF(userName);
        out.writeLong(applicationId);
        out.writeUTF(applicationName);
        out.writeInt(action.getType());
        out.writeInt(policyDecision.getType());
        out.writeLong(decisionRequestId);
        out.writeLong(ts);
        out.writeInt(level);

        AttributeExternalizer.writeResourceAttributes(out, getCustomAttr());
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        fromResourceInfo = new FromResourceInformation();
        fromResourceInfo.readExternal(in);
        boolean existsToInfo = in.readBoolean();
        if (existsToInfo) {
            toResourceInfo = new ToResourceInformation();
            toResourceInfo.readExternal(in);
        } else {
            toResourceInfo = null;
        }
        hostId = in.readLong();
        hostIP = in.readUTF();
        hostName = in.readUTF();
        userId = in.readLong();
        userName = in.readUTF();
        applicationId = in.readLong();
        applicationName = in.readUTF();
        action = ActionEnumType.getActionEnum(in.readInt());
        policyDecision = PolicyDecisionEnumType.getPolicyDecisionEnum(in.readInt());
        decisionRequestId = in.readLong();
        ts = in.readLong();
        level = in.readInt();

        customAttr = AttributeExternalizer.readResourceAttributes(in);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof PolicyActivityInfo)) {
            return false;
        }
        
        if (this == obj) {
            return true;
        }
        
        PolicyActivityInfo info = (PolicyActivityInfo) obj;
        boolean toResourceEquals;
        if (toResourceInfo == null) {
            toResourceEquals = (info.toResourceInfo == null);
        } else {
            toResourceEquals = toResourceInfo.equals(info.toResourceInfo);
        }

        if (fromResourceInfo.equals(info.fromResourceInfo) &&
            toResourceEquals &&
            hostId == info.hostId &&
            hostIP.equals(info.hostIP) &&
            hostName.equals(info.hostName) &&
            userId == info.userId &&
            userName.equals(info.userName) &&
            applicationId == info.applicationId &&
            applicationName.equals(info.applicationName) &&
            action.equals(info.action) &&
            policyDecision.equals(info.policyDecision) &&
            decisionRequestId == info.decisionRequestId &&
            ts == info.ts &&
            level == info.level){

            DynamicAttributes attrs = info.getCustomAttr();
            if (attrs == null || getCustomAttr() == null) {
                return (attrs == getCustomAttr());
            } else {
                if (attrs.size() == getCustomAttr().size()) {
                    for (Map.Entry<String,IEvalValue> ca : getCustomAttr().entrySet()) {
                        if (!attrs.containsKey(ca.getKey())) {
                            return false;
                        }
                        if (ca.getValue() != null) {
                            if (!ca.getValue().equals(attrs.get(ca.getKey()))) {
                                return false;
                            }
                        } else {
                            if (attrs.get(ca.getKey()) != null) {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
                return true;
            }
        } else {
            return false;
        }
    }


    public int hashCode() {
        return (int) decisionRequestId ^ (int) (decisionRequestId >> 32);
    }


    public String toString() {
        StringBuffer rv = new StringBuffer("PolicyActivityInfo[");
        rv.append("fromResource: " + fromResourceInfo);
        rv.append(", toResource: " + toResourceInfo);
        rv.append(", hostId: " + hostId);
        rv.append(", hostIP: " + hostIP);        
        rv.append(", hostName: " + hostName);
        rv.append(", userId: " + userId);
        rv.append(", userName: " + userName);
        rv.append(", applicationId: " + applicationId);
        rv.append(", applicationName: " + applicationName);
        rv.append(", action: " + action);
        rv.append(", policyDecision: " + policyDecision);
        rv.append(", decisionRequestId: " + decisionRequestId);
        rv.append(", ts: " + ts);
        rv.append(", level: " + level);
        rv.append(", custom attributes: ");
        if (getCustomAttr() == null) {
            rv.append(" <EMPTY> ");
        } else {
            for (Map.Entry<String,IEvalValue> entry : getCustomAttr().entrySet()) {
                rv.append("{" + entry.getKey() + ", " + entry.getValue().getValue() + "}");
            }
            rv.append("]");
        }
        return rv.toString();
    }
    
    

}
