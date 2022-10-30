/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * This is the heartbeat cookie. This cookie contains the last known state in
 * terms of heartbeat.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IComponentHeartbeatCookie.java#1 $
 */

public interface IComponentHeartbeatCookie {

    /**
     * Returns the last update timestamp
     * 
     * @return the last update timestamp
     */
    public long getUpdateTimestamp();
}
