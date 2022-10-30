/*
 * Created on Feb 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.axis.encoding.Base64;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * This is a utility class to covert activity log objects from their internal types
 * to serialized string. This class will be versioned according to the different 
 * versions of the dabs log service (DABSLogServiceImpl).
 * 
 * This is the converter for v1 of dabs log service
 * 
 * This converter will serialized and deserialize the activity logs into the v1 
 * format and vice versa
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/DABSLogServiceWSConverter.java#1 $
 */

public class DABSLogServiceWSConverter {
    /**
     * Standard User 
     */
    private static final String unknownUser = "<UNKNOWN>";
    
    /**
     * This is the method that reads TrackingLogEntry from a ObjectInput
     * 
     * @param in
     * @return a single TrackingLogEntry from input
     * @throws IOException
     */
    public static TrackingLogEntry readExternalTrackingLog(ObjectInput in) throws IOException {
        long uid = in.readLong();
        long timestamp = in.readLong();
        ActionEnumType action = ActionEnumType.getActionEnum(in.readInt());
        String hostName = in.readUTF();
        String hostIP = in.readUTF();
        long hostId = in.readLong();
        String userName = in.readUTF();
        if (userName.equals("")) {
            userName = unknownUser;
        }
        long userId = in.readLong();
        long applicationId = in.readLong();
        String applicationName = in.readUTF();
        
        String fromResName = in.readUTF();
        long createdDate = in.readLong();
        long modifiedDate = in.readLong();
        long size = in.readLong();
        String ownerId = in.readUTF();
        FromResourceInformation fromResourceInfo = new FromResourceInformation(fromResName, size, createdDate, modifiedDate, ownerId);
        boolean existsToInfo = in.readBoolean();
        ToResourceInformation toResourceInfo = null;
        if (existsToInfo) {
            String toResName = in.readUTF();
            toResourceInfo = new ToResourceInformation(toResName);
        }
        int level = in.readInt();
        
        DynamicAttributes customAttr = new DynamicAttributes();
        
        return new TrackingLogEntry(fromResourceInfo, 
                                    toResourceInfo, 
                                    userName, 
                                    userId, 
                                    hostName, 
                                    hostIP, hostId, 
                                    applicationName, 
                                    applicationId, 
                                    action, 
                                    uid, 
                                    timestamp, 
                                    level, 
                                    customAttr);
    }

    /**
     * This is the method that serializes a single TrackingLogEntry to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalTrackingLog(ObjectOutput out, TrackingLogEntry entry) throws IOException {
        out.writeLong(entry.getUid());
        out.writeLong(entry.getTimestamp());
        out.writeInt(entry.getAction().getType());
        out.writeUTF(entry.getHostName());
        out.writeUTF(entry.getHostIP());
        out.writeLong(entry.getHostId());
        out.writeUTF(entry.getUserName());
        out.writeLong(entry.getUserId());
        out.writeLong(entry.getApplicationId());
        out.writeUTF(entry.getApplicationName());
        out.writeUTF(entry.getFromResourceInfo().getName());
        out.writeLong(entry.getFromResourceInfo().getCreatedDate());
        out.writeLong(entry.getFromResourceInfo().getModifiedDate());
        out.writeLong(entry.getFromResourceInfo().getSize());
        out.writeUTF(entry.getFromResourceInfo().getOwnerId());
        if (entry.getToResourceInfo() != null) {
            out.writeBoolean(true);
            out.writeUTF(entry.getToResourceInfo().getName());
        } else {
            out.writeBoolean(false);
        }
        out.writeInt(entry.getLevel());
    }
    
    /**
     * This is the method that reads PolicyActivityLogEntry from a ObjectInput
     * 
     * @param in
     * @return a single PolicyActivityLogEntry from input
     * @throws IOException
     */
    public static PolicyActivityLogEntry readExternalPolicyLog(ObjectInput in) throws IOException {
        long uid = in.readLong();
        long timestamp = in.readLong();
        long policyId = in.readLong();
        String fromResName = in.readUTF();
        long createdDate = in.readLong();
        long modifiedDate = in.readLong();
        long size = in.readLong();
        String ownerId = in.readUTF();
        FromResourceInformation fromResourceInfo = new FromResourceInformation(fromResName, size, createdDate, modifiedDate, ownerId);
        boolean existsToInfo = in.readBoolean();
        ToResourceInformation toResourceInfo = null;
        if (existsToInfo) {
            String toResName = in.readUTF();
            toResourceInfo = new ToResourceInformation(toResName);
        } 
        long hostId = in.readLong();
        String hostIP = in.readUTF();
        String hostName = in.readUTF();
        long userId = in.readLong();
        String userName = in.readUTF();
        long applicationId = in.readLong();
        String applicationName = in.readUTF();
        ActionEnumType action = ActionEnumType.getActionEnum(in.readInt());
        PolicyDecisionEnumType policyDecision = PolicyDecisionEnumType.getPolicyDecisionEnum(in.readInt());
        long decisionRequestId = in.readLong();
        long ts = in.readLong();
        int level = in.readInt();

        DynamicAttributes customAttr = new DynamicAttributes();
        
        PolicyActivityInfo info = new PolicyActivityInfo(fromResourceInfo, 
                                                         toResourceInfo, 
                                                         userName, 
                                                         userId, 
                                                         hostName, 
                                                         hostIP, 
                                                         hostId, 
                                                         applicationName, 
                                                         applicationId, 
                                                         action, 
                                                         policyDecision, 
                                                         decisionRequestId, 
                                                         ts, 
                                                         level, 
                                                         customAttr);
        
        return new PolicyActivityLogEntry(info, policyId, uid);
    }

    /**
     * This is the method that serializes a single PolicyActivityLogEntry to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLog(ObjectOutput out, PolicyActivityLogEntry entry) throws IOException {
        out.writeLong(entry.getUid());
        out.writeLong(entry.getTimestamp());
        out.writeLong(entry.getPolicyId());
        out.writeUTF(entry.getFromResourceInfo().getName());
        out.writeLong(entry.getFromResourceInfo().getCreatedDate());
        out.writeLong(entry.getFromResourceInfo().getModifiedDate());
        out.writeLong(entry.getFromResourceInfo().getSize());
        out.writeUTF(entry.getFromResourceInfo().getOwnerId());
        if (entry.getToResourceInfo() != null) {
            out.writeBoolean(true);
            out.writeUTF(entry.getToResourceInfo().getName());
        } else {
            out.writeBoolean(false);
        }
        out.writeLong(entry.getHostId());
        out.writeUTF(entry.getHostIP());
        out.writeUTF(entry.getHostName());
        out.writeLong(entry.getUserId());
        out.writeUTF(entry.getUserName());
        out.writeLong(entry.getApplicationId());
        out.writeUTF(entry.getApplicationName());
        out.writeInt(entry.getAction().getType());
        out.writeInt(entry.getPolicyDecision().getType());
        out.writeLong(entry.getDecisionRequestId());
        out.writeLong(entry.getTimestamp());
        out.writeInt(entry.getLevel());
    }
    
    /**
     * Serializes, compresses, and encodes (Base64) collection of log entries.
     * The enclosing collection is not serialized, instead the number of entries
     * is the first entry (int) in the serialized stream.
     * 
     * @param logEntries entries to serialize, must be Externalizable
     * @return String encoding of serialized and compressed log entries
     * @throws IOException
     */
    public static final String encodeLogEntries(Collection logEntries) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        GZIPOutputStream zipStream = new GZIPOutputStream(outStream);
        ObjectOutputStream oos = new ObjectOutputStream(zipStream);
        
        oos.writeInt(logEntries.size());
        for (Iterator iter = logEntries.iterator(); iter.hasNext();) {
            BaseLogEntry entry = (BaseLogEntry) iter.next();
            if (entry instanceof TrackingLogEntry){
                TrackingLogEntry trackingEntry = (TrackingLogEntry)entry;
                writeExternalTrackingLog(oos, trackingEntry);
            } else {
                PolicyActivityLogEntry policyEntry = (PolicyActivityLogEntry)entry;
                writeExternalPolicyLog(oos, policyEntry);
            }
        }
        oos.close();

        byte[] bytes = outStream.toByteArray();
        return Base64.encode(bytes);        
    }
    
    /**
     * Decodes passed in encoded (Base64) and compressed data and provides
     * an ObjectInputStream
     * 
     * @param data data to decode
     * @return ObjectInputStream decoded from the data
     * @throws IOException
     */
    public static final ObjectInputStream decodeData(String data) throws IOException {
        byte[] bytes = Base64.decode(data);
        
        return (new ObjectInputStream (new GZIPInputStream (new ByteArrayInputStream(bytes))));
    }
}
