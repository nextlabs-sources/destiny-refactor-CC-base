/*
 * Created on Nov 18, 2004
 */
package com.bluejungle.domain.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * Base class for all Log Entry data objects
 * 
 * @author sgoldstein
 */
public abstract class BaseLogEntry implements Externalizable {

    private long uid;
    private long timestamp;

    
    
    


    public BaseLogEntry(long uid, long ts) {
        this.uid = uid;
        this.timestamp = ts;
    }
    
    /**
     * Emtpty Constructor for Hibernate
     *  
     */
    public BaseLogEntry() {
    }

    /**
     * Retrieve the globally unique identifier associated with this log entry
     * 
     * @return the globally unique identifier associated with this log entry
     */
    public final long getUid() {
        return this.uid;
    }

    /**
     * Set the globally unique identifier associated with this log entry
     * 
     * @param uid
     *            the globally unique identifier associated with this log entry
     */
    public void setUid(long uid) {
        this.uid = uid;
    }

    /**
     * Retrieve the timestamp associated with this log entry
     * 
     * @return the timestamp associated with this log entry
     */
    public final long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp associated with this log entry
     * 
     * @param timestamp
     *            the timestamp associated with this log entry
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        uid = in.readLong();
        timestamp = in.readLong();
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(uid);
        out.writeLong(timestamp);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BaseLogEntry)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        
        BaseLogEntry entry = (BaseLogEntry) obj;
        return (uid == entry.uid && timestamp == entry.timestamp);
        
    }

    public int hashCode() {
        return (int) uid ^ (int) (uid >> 32);
    }

    public String toString() {
        return "uid: " + uid + ", timestamp: " + timestamp;
    }
    
    
}