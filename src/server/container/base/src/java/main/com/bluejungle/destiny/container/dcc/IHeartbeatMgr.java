/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;

/**
 * This is the interface for the heartbeat manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/IHeartbeatMgr.java#2 $:
 */

public interface IHeartbeatMgr extends IDestinyEventListener, IInitializable, IConfigurable, IDisposable, ILogEnabled, IManagerEnabled {

    public static final String COMP_NAME = "HeartbeatMgr";
    public static final String HEARTBEAT_RATE_CONFIG_PARAM = "Rate";
    public static final String COMPONENT_ID_CONFIG_PARAM = "ComponentId";
    public static final String COMPONENT_TYPE_CONFIG_PARAM = "ComponentType";
}