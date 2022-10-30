/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import java.util.HashMap;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseActivityLogDO;

/**
 * This is the class that contains a result of a single log record, 
 * including the custom attributes for various enforcer types.
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/ILogDetailResult.java#1 $
 */

public interface ILogDetailResult {
    
    /**
     * 
     * @return the activity log record
     */
    public BaseActivityLogDO getActivityLog();
    
    /**
     * 
     * @return the custom attributes for this activity log
     */
    public HashMap getActivityCustomAttributes();
}
