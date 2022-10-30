/*
 * Created on Mar 31, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/heartbeat/IHeartbeatManager.java#1 $
 */
package com.bluejungle.framework.heartbeat;

import java.io.Serializable;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

public interface IHeartbeatManager {
    public static final ComponentInfo COMP_INFO = new ComponentInfo(IHeartbeatManager.class.getName(), HeartbeatManagerImpl.class.getName(), IHeartbeatManager.class.getName(), LifestyleType.SINGLETON_TYPE);

    /**
     * Register a listener for a particular name (id) with the heartbeat manager.
     * Only one listener can be registered per name, although a listener could be
     * registered multiple times for different names.
     */
    public void register (String name, IHeartbeatListener listener);

    /**
     * Unregister the listener associated with the specified name
     */
    public void unregister(String name, IHeartbeatListener listener);

    /**
     * Give the named data to the appropriate listener for processing
     */
    public void processHeartbeatPluginData(String name, String data);

    /**
     * Get data from all the listeners
     */
    public Map<String, Serializable> getHeartbeatPluginData();
}
