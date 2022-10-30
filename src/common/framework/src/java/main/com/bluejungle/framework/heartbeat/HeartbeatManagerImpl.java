package com.bluejungle.framework.heartbeat;

/*
 * Created on Mar 31, 2008
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/heartbeat/HeartbeatManagerImpl.java#1 $
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HeartbeatManagerImpl implements IHeartbeatManager {
    private Map<String, IHeartbeatListener> listeners = new HashMap<String, IHeartbeatListener>();

    synchronized public void register(String name, IHeartbeatListener listener) {
        listeners.put(name, listener);
        return;
    }

    synchronized public void unregister(String name, IHeartbeatListener listener) {
        listeners.remove(name);
    }

    synchronized public void processHeartbeatPluginData(String name, String data) {
        IHeartbeatListener hbl = listeners.get(name);

        if (hbl == null) {
            return;
        }

        hbl.processResponse(name, data);
    }

    synchronized public Map<String, Serializable> getHeartbeatPluginData() {
        Map<String, Serializable> result = new HashMap<String, Serializable>();

        for (Iterator<Map.Entry<String, IHeartbeatListener>> iter = listeners.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, IHeartbeatListener> entry = iter.next();
            String name = entry.getKey();
            result.put(name, entry.getValue().prepareRequest(name));
        }

        return result;
    }
}
