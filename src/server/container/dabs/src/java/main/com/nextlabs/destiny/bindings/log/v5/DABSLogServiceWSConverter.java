/*
 * Created on Sep 12, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/bindings/log/v5/DABSLogServiceWSConverter.java#1 $:
 */
package com.nextlabs.destiny.bindings.log.v5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.axis.encoding.Base64;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * This is a utility class to covert activity log objects from their internal types
 * to serialized string. This class will be versioned according to the different 
 * versions of the dabs log service (DABSLogServiceImpl)
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/bindings/log/v5/DABSLogServiceWSConverter.java#1 $
 */

public class DABSLogServiceWSConverter {
    /**
     * This is the method that reads TrackingLogEntry from a ObjectInput
     * 
     * @param in
     * @return a single TrackingLogEntry from input
     * @throws IOException
     */
    public static TrackingLogEntry readExternalTrackingLog(ObjectInput in) throws IOException {
        TrackingLogEntry logEntry = new TrackingLogEntry();

        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }

        return logEntry;
    }

    /**
     * This is the method that reads TrackingLogEntryV2 from a ObjectInput
     * 
     * @param in
     * @return a single TrackingLogEntry from input
     * @throws IOException
     */
    public static TrackingLogEntryV2 readExternalTrackingLogV2(ObjectInput in) throws IOException {
        TrackingLogEntryV2 logEntry = new TrackingLogEntryV2();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
            
        }
        return logEntry;
    }
    /**
     * This is the method that reads TrackingLogEntryV3 from a ObjectInput
     * 
     * @param in
     * @return a single TrackingLogEntry from input
     * @throws IOException
     */
    public static TrackingLogEntryV3 readExternalTrackingLogV3(ObjectInput in) throws IOException {
        TrackingLogEntryV3 logEntry = new TrackingLogEntryV3();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
            
        }
        return logEntry;
    }
    /**
     * This is the method that serializes a single TrackingLogEntry to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalTrackingLog(ObjectOutput out, TrackingLogEntry entry) throws IOException {
        entry.writeExternal(out);
    }

    /**
     * This is the method that serializes a single TrackingLogEntryV2 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalTrackingLogV2(ObjectOutput out, TrackingLogEntryV2 entry) throws IOException {
        entry.writeExternal(out);
    }
	
    /**
     * This is the method that serializes a single TrackingLogEntryV3 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalTrackingLogV3(ObjectOutput out, TrackingLogEntryV3 entry) throws IOException {
        entry.writeExternal(out);
    }

    
    /**
     * This is the method that reads PolicyActivityLogEntry from a ObjectInput
     * 
     * @param in
     * @return a single PolicyActivityLogEntry from input
     * @throws IOException
     */
    public static PolicyActivityLogEntry readExternalPolicyLog(ObjectInput in) throws IOException {
        PolicyActivityLogEntry logEntry = new PolicyActivityLogEntry();

        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }

        return logEntry;
    }

    /**
     * This is the method that serializes a single PolicyActivityLogEntry to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLog(ObjectOutput out, PolicyActivityLogEntry entry) throws IOException {
        entry.writeExternal(out);
    }

    /**
     * This is the method that serializes a single PolicyActivityLogEntryV2 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLogV2(ObjectOutput out, PolicyActivityLogEntryV2 entry) throws IOException {
        entry.writeExternal(out);
    }
	
	/**
     * This is the method that serializes a single PolicyActivityLogEntryV3 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLogV3(ObjectOutput out, PolicyActivityLogEntryV3 entry) throws IOException {
        entry.writeExternal(out);
    }
	
    /**
     * This is the method that serializes a single PolicyActivityLogEntryV4 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLogV4(ObjectOutput out, PolicyActivityLogEntryV4 entry) throws IOException {
        entry.writeExternal(out);
    }
	
	/**
     * This is the method that serializes a single PolicyActivityLogEntryV5 to ObjectOutput
     * 
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyLogV5(ObjectOutput out, PolicyActivityLogEntryV5 entry) throws IOException {
        entry.writeExternal(out);
    }

    /**
     * This is the method that reads PolicyActivityLogV2 from a ObjectInput
     * 
     * @param in
     * @return a single TrackingLogEntry from input
     * @throws IOException
     */
    public static PolicyActivityLogEntryV2 readExternalPolicyLogV2(ObjectInput in) throws IOException {
        PolicyActivityLogEntryV2 logEntry = new PolicyActivityLogEntryV2();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }
        return logEntry;
    }

    /**
     * This is the method that reads PolicyActivityLogV3 from a ObjectInput
     * 
     * @param in
     * @return a single PolicyActivityLogEntryV3 from input
     * @throws IOException
     */
    public static PolicyActivityLogEntryV3 readExternalPolicyLogV3(ObjectInput in) throws IOException {
        PolicyActivityLogEntryV3 logEntry = new PolicyActivityLogEntryV3();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }
        return logEntry;
    }



    /**
     * This is the method that reads PolicyActivityLogV4 from a ObjectInput
     * 
     * @param in
     * @return a single PolicyActivityLogEntryV4 from input
     * @throws IOException
     */
    public static PolicyActivityLogEntryV4 readExternalPolicyLogV4(ObjectInput in) throws IOException {
        PolicyActivityLogEntryV4 logEntry = new PolicyActivityLogEntryV4();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }
        return logEntry;
    }
	
	    /**
     * This is the method that reads PolicyActivityLogV5 from a ObjectInput
     * 
     * @param in
     * @return a single PolicyActivityLogEntryV5 from input
     * @throws IOException
     */
    public static PolicyActivityLogEntryV5 readExternalPolicyLogV5(ObjectInput in) throws IOException {
        PolicyActivityLogEntryV5 logEntry = new PolicyActivityLogEntryV5();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }
        return logEntry;
    }

    /**
     * This is the method that reads PolicyAssistantLogEntry from a ObjectInput
     * 
     * @param in
     * @return a single PolicyAssistantLogEntry from input
     * @throws IOException
     */
    public static PolicyAssistantLogEntry readExternalPolicyAssistantLog(ObjectInput in) throws IOException {
        PolicyAssistantLogEntry logEntry = new PolicyAssistantLogEntry();
        try {
            logEntry.readExternal(in);
        } catch (ClassNotFoundException e) {
        }

        return logEntry;
    }

    /**
     * This is the method that serializes a single PolicyAssistantLogEntry to ObjectOutput
     *
     * @param out
     * @param entry
     * @throws IOException
     */
    public static void writeExternalPolicyAssistantLog(ObjectOutput out, PolicyAssistantLogEntry entry) throws IOException {
        entry.writeExternal(out);
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
                writeExternalTrackingLog(oos, (TrackingLogEntry)entry);
            } else if (entry instanceof TrackingLogEntryV2) {
                writeExternalTrackingLogV2(oos, (TrackingLogEntryV2)entry);
            } else if (entry instanceof TrackingLogEntryV3) {
                writeExternalTrackingLogV3(oos, (TrackingLogEntryV3)entry);
            } else if (entry instanceof PolicyActivityLogEntry) {
                writeExternalPolicyLog(oos, (PolicyActivityLogEntry)entry);
            } else if (entry instanceof PolicyActivityLogEntryV2) {
                writeExternalPolicyLogV2(oos, (PolicyActivityLogEntryV2)entry);
            } else if (entry instanceof PolicyActivityLogEntryV3) {
                writeExternalPolicyLogV3(oos, (PolicyActivityLogEntryV3)entry);
            } else if (entry instanceof PolicyActivityLogEntryV4) {
                writeExternalPolicyLogV4(oos, (PolicyActivityLogEntryV4)entry);
			} else if (entry instanceof PolicyActivityLogEntryV5) {
                writeExternalPolicyLogV5(oos, (PolicyActivityLogEntryV5)entry);
            } else {
                writeExternalPolicyAssistantLog(oos, (PolicyAssistantLogEntry)entry);
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
