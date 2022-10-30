/*
 * Created on Apr 08, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/inquirymgr/hibernateimpl/TestPolicyActivityLogPolicyTagDO.java#1 $:
 */

package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogDO;

public class TestPolicyActivityLogPolicyTagDO {
    private Long id;
    private PolicyActivityLogDO activityLog;
    private String key;
    private String value;

    /**
     * Returns the id.
     * @return the id.
     */
    public Long getId() {
        return id;
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
        return key;
    }
    
    /**
     * Sets the key
     * @param key The key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }
 
    /**
     * Returns the activityLog.
     * @return the activityLog.
     */
    public PolicyActivityLogDO getActivityLog() {
        return activityLog;
    }

    
    /**
     * Sets the activityLog
     * @param record The activityLog to set.
     */
    public void setActivityLog(PolicyActivityLogDO activityLog) {
        this.activityLog = activityLog;
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
