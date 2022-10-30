/*
 * Created on Sep 12, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/nextlabs/domain/log/TrackingLogEntryV3.java#1 $:
 */
package com.nextlabs.domain.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import com.bluejungle.domain.log.AttributeExternalizer;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntryWrapper;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;

public class TrackingLogEntryV3 extends BaseLogEntry implements Externalizable, TrackingLogEntryWrapper {

    private String action;
    private String hostName;
    private String hostIP;
    private long hostId;
    private String userName;
    private long userId;
    private long applicationId;
    private String applicationName;
    private DynamicAttributes customAttr;

    // Oracle treats empty strings as null.  We don't permit null as a user name, so
    // we will convert on assignment
    private final String unknownUser = "<UNKNOWN>";

    int level;

    private FromResourceInformation fromResourceInfo;
    private ToResourceInformation toResourceInfo;

    public TrackingLogEntryV3(FromResourceInformation fromResourceInfo,
            ToResourceInformation toResourceInfo,
            String userName,
            long userId,
            String hostName,
            String hostIP,
            long hostId,
            String applicationName,
            long applicationId,
            String action,
            long uid,
            long ts,
            int level,
            DynamicAttributes customAttr) {

        super(uid, ts);
        this.action = action;
        this.hostName = hostName;
        this.hostIP = hostIP;
        this.hostId = hostId;

        if (userName.equals("")) {
            userName = unknownUser;
        }
        this.userName = userName;

        this.userId = userId;
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.fromResourceInfo = fromResourceInfo;
        this.toResourceInfo = toResourceInfo;
        this.level = level;
        this.customAttr = customAttr;
    }

    public TrackingLogEntryV3() {
        this.customAttr = new DynamicAttributes();
    }


    public String getAction() {
        return action;
    }


    public void setAction(String action) {
        this.action = action;
    }


    public long getApplicationId() {
        return applicationId;
    }


    public void setApplicationId(long applicationId) {
        this.applicationId = applicationId;
    }


    public String getApplicationName() {
        return applicationName;
    }


    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public FromResourceInformation getFromResourceInfo() {
        return fromResourceInfo;
    }


    public void setFromResourceInfo(FromResourceInformation fromResourceInfo) {
        this.fromResourceInfo = fromResourceInfo;
    }


    public long getHostId() {
        return hostId;
    }


    public void setHostId(long hostId) {
        this.hostId = hostId;
    }


    public ToResourceInformation getToResourceInfo() {
        return toResourceInfo;
    }


    public void setToResourceInfo(ToResourceInformation toResourceInfo) {
        this.toResourceInfo = toResourceInfo;
    }


    public long getUserId() {
        return userId;
    }


    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * @return Returns the hostIP.
     */
    public String getHostIP() {
        return hostIP;
    }

    /**
     * @param hostIP The hostIP to set.
     */
    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    /**
     * @return Returns the hostName.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName The hostName to set.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        if (userName.equals("")) {
            userName = unknownUser;
        }

        this.userName = userName;
    }

    /**
     * Returns the logging level.
     *
     * @return the logging level.
     */
    public final int getLevel() {
        return this.level;
    }

    /**
     * @param level The logging level to set.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the customAttr.
     * @return the customAttr.
     */
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

    /**
     * @see com.bluejungle.domain.log.BaseLogEntry#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        action = in.readUTF();
        hostName = in.readUTF();
        hostIP = in.readUTF();
        hostId = in.readLong();
        userName = in.readUTF();
        if (userName.equals("")) {
            userName = unknownUser;
        }
        userId = in.readLong();
        applicationId = in.readLong();
        applicationName = in.readUTF();

        fromResourceInfo = new FromResourceInformation();
        fromResourceInfo.readExternal(in);
        boolean existsToInfo = in.readBoolean();
        if (existsToInfo) {
            toResourceInfo = new ToResourceInformation();
            toResourceInfo.readExternal(in);
        } else {
            toResourceInfo = null;
        }
        level = in.readInt();

        customAttr = AttributeExternalizer.readResourceAttributes(in);
    }


    /**
     * @see com.bluejungle.domain.log.BaseLogEntry#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(action);
        out.writeUTF(hostName);
        out.writeUTF(hostIP);
        out.writeLong(hostId);
        out.writeUTF(userName);
        out.writeLong(userId);
        out.writeLong(applicationId);
        out.writeUTF(applicationName);
        fromResourceInfo.writeExternal(out);
        if (toResourceInfo != null) {
            out.writeBoolean(true);
            toResourceInfo.writeExternal(out);
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(level);

        AttributeExternalizer.writeResourceAttributes(out, getCustomAttr());
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TrackingLogEntryV3)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        TrackingLogEntryV3 entry = (TrackingLogEntryV3) obj;
        boolean toResourceEquals;
        if (toResourceInfo == null) {
            toResourceEquals = (entry.toResourceInfo == null);
        } else {
            toResourceEquals = (toResourceInfo.equals(entry.toResourceInfo));
        }

        if (action.equals(entry.action) &&
            hostName.equals(entry.hostName) &&
            hostIP.equals(entry.hostIP) &&
            hostId == entry.hostId &&
            userName.equals(entry.userName) &&
            userId == entry.userId &&
            applicationId == entry.applicationId &&
            applicationName.equals(entry.applicationName) &&
            fromResourceInfo.equals(entry.fromResourceInfo) &&
            toResourceEquals &&
            level == entry.level){
            DynamicAttributes attrs = entry.getCustomAttr();

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
        return super.hashCode();
    }

    public String toString() {
        StringBuffer rv = new StringBuffer("TrackingLogEntryV3[");
        rv.append(super.toString());
        rv.append(", action: " + action);
        rv.append(", hostName: " + hostName);
        rv.append(", hostIP: " + hostIP);
        rv.append(", hostId: " + hostId);
        rv.append(", userName: " + userName);
        rv.append(", userId: " + userId);
        rv.append(", applicationId: " + applicationId);
        rv.append(", applicationName: " + applicationName);
        rv.append(", fromResourceInfo: " + fromResourceInfo);
        rv.append(", toResourceInfo: " + toResourceInfo);
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
