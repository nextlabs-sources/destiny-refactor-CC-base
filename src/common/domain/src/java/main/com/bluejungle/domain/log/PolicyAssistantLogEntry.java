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

public class PolicyAssistantLogEntry extends BaseLogEntry implements Externalizable, PolicyAssistantLogEntryWrapper {
    private String logIdentifier;
    private String assistantName;
    private String attrOne;
    private String attrTwo;
    private String attrThree;

    public PolicyAssistantLogEntry() {
    }

    public PolicyAssistantLogEntry(String logIdentifier,
                                   String assistantName,
                                   String attrOne,
                                   String attrTwo,
                                   String attrThree) {
        super();
        this.logIdentifier = logIdentifier;
        this.assistantName = assistantName;
        this.attrOne = attrOne;
        this.attrTwo = attrTwo;
        this.attrThree = attrThree;
    }

    public PolicyAssistantLogEntry(String logIdentifier,
                                   String assistantName,
                                   String attrOne,
                                   String attrTwo,
                                   String attrThree,
                                   long uid,
                                   long ts) {
        super(uid, ts);
        this.logIdentifier = logIdentifier;
        this.assistantName = assistantName;
        this.attrOne = attrOne;
        this.attrTwo = attrTwo;
        this.attrThree = attrThree;
    }

    public final String getLogIdentifier() {
        return logIdentifier;
    }

    public final String getAssistantName() {
        return assistantName;
    }

    public final String getAttrOne() {
        return attrOne;
    }

    public final String getAttrTwo() {
        return attrTwo;
    }

    public final String getAttrThree() {
        return attrThree;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PolicyAssistantLogEntry)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        PolicyAssistantLogEntry entry = (PolicyAssistantLogEntry) obj;
        
        return (getUid() == entry.getUid() &&
        		logIdentifier.equals(entry.logIdentifier) && 
        		assistantName.equals(entry.assistantName) &&
        		attrOne.equals(entry.attrOne) &&
        		attrTwo.equals(entry.attrTwo) &&
        		attrThree.equals(entry.attrThree));
    }
    
    public String toString() {
        return "uid: " + getUid() + ", logIdentifier: " + logIdentifier;
    }
    
    public void readExternal(ObjectInput in) 
    	throws IOException, ClassNotFoundException {
        super.readExternal(in);
        logIdentifier = in.readUTF();
        assistantName = in.readUTF();
        attrOne = in.readUTF();
        attrTwo = in.readUTF();
        attrThree = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(logIdentifier);
        out.writeUTF(assistantName);
        out.writeUTF(attrOne);
        out.writeUTF(attrTwo);
        out.writeUTF(attrThree);
    }
}
