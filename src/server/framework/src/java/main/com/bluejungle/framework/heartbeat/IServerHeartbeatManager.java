/*
 * Created on Apr 8, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/heartbeat/IServerHeartbeatManager.java#1 $
 */
package com.bluejungle.framework.heartbeat;

import java.io.Serializable;

public interface IServerHeartbeatManager {
    public static final String COMP_NAME = "IServerHeartbeatManager";

    /**
     * Register a provider for a particular name (id) with the heartbeat manager.
     * Only one listener can be registered per name, although a provider could be
     * registered multiple times for different names.
     */
    public void register (String name, IHeartbeatProvider listener);

    /**
     * Unregister the provider associated with the specified name
     */
    public void unregister(String name, IHeartbeatProvider listener);

    /**
     * Find provider for data and get response
     */
    public Serializable processHeartbeatPluginData(String name, String data);
}
