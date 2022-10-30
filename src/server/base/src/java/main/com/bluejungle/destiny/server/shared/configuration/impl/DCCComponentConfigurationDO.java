/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DCCComponentConfigurationDO.java#1 $
 */

public abstract class DCCComponentConfigurationDO extends BaseConfigurationDO
        implements IDCCComponentConfigurationDO {

    private int heartbeatInterval;
    
    /**
     * Constructor
     *  
     */
    public DCCComponentConfigurationDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.configmgr.IDCCComponentConfigurationDO#getHeartbeatInterval()
     */
    public int getHeartbeatInterval() {
        return this.heartbeatInterval;
    }

    /**
     * Sets the heartbeat interval
     * 
     * @param interval
     *            hearbeat interval to set
     */
    public void setHeartbeatInterval(Integer interval) {
        this.heartbeatInterval = interval.intValue();
    }
}