/*
 * Created on Jan 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseActivityLogDO;

/**
 * This is the base object that maps an activity log to the custom attributes 
 * table inside the Activity database.  This abstract class contains all the 
 * basic fields for data objects.
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/hibernateimpl/BaseActivityLogCustomAttributeDO.java#1 $
 */

public abstract class BaseActivityLogCustomAttributeDO {
    
    private Long id;
    private BaseActivityLogDO record;
    private String type;
    private String key;
    private String value;
    
    
    /**
     * Returns the type.
     * @return the type.
     */
    public String getType() {
        return this.type;
    }
    
    /**
     * Sets the type
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    
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
    public BaseActivityLogDO getRecord() {
        return this.record;
    }

    
    /**
     * Sets the record
     * @param record The record to set.
     */
    public void setRecord(BaseActivityLogDO record) {
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
