/*
 * Created on Jan 25, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogEntryDO;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/inquirymgr/hibernateimpl/TestTrackingActivityLogCustomAttributeDO.java#1 $
 */

public class TestTrackingActivityLogCustomAttributeDO {

    private Long id;
    private TestTrackingActivityLogEntryDO record;
    private String key;
    private String value;
    
    /**
     * Returns the id.
     * @return the id.
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * Sets the id
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Returns the key.
     * @return the key.
     */
    public String getKey() {
        return this.key;
    }
    
    /**
     * Sets the key
     * @param key The key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * Returns the record.
     * @return the record.
     */
    public TestTrackingActivityLogEntryDO getRecord() {
        return this.record;
    }

    
    /**
     * Sets the record
     * @param record The record to set.
     */
    public void setRecord(TestTrackingActivityLogEntryDO record) {
        this.record = record;
    }

    /**
     * Returns the value.
     * @return the value.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Sets the value
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
