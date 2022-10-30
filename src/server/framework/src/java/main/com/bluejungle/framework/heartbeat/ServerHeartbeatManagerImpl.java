/*
 * Created on Apr 8, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/heartbeat/ServerHeartbeatManagerImpl.java#1 $
 */

package com.bluejungle.framework.heartbeat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ServerHeartbeatManagerImpl implements IServerHeartbeatManager {
    private Map<String, IHeartbeatProvider> providers = new HashMap<String, IHeartbeatProvider>();

    synchronized public void register(String name, IHeartbeatProvider provider) {
        providers.put(name, provider);
        return;
    }

    synchronized public void unregister(String name, IHeartbeatProvider provider) {
        providers.remove(name);
    }

    synchronized public Serializable processHeartbeatPluginData(String name, String data) {
        IHeartbeatProvider provider = providers.get(name);
        
        if (provider == null) {
            return null;
        }
        
        return provider.serviceHeartbeatRequest(name, data);
    }
}
