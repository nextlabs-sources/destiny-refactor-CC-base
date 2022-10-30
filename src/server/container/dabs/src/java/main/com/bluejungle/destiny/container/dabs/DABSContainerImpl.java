/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import com.bluejungle.destiny.container.dcc.DefaultContainerImpl;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dcc.IHeartbeatMgr;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/DABSContainerImpl.java#1 $
 */

public class DABSContainerImpl extends DefaultContainerImpl implements IDCCContainer {

    /**
     * Constructor
     *  
     */
    public DABSContainerImpl() {
        super();
    }

    /**
     * Returns the class name to be used for the heartbeat manager. The DABS
     * heartbeat manager is a special implementation.
     * 
     * @return the class name to be used for the heartbeat manager.
     */
    protected Class<? extends IHeartbeatMgr> getHeartbeatMgrClassName() {
        return DABSHeartBeatMgrImpl.class;
    }
}