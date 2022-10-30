/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/ComponentHeartbeatCookieImpl.java#1 $
 */

public class ComponentHeartbeatCookieImpl implements IComponentHeartbeatCookie {

    private long timestamp;

    /**
     * Constructor
     *  
     */
    public ComponentHeartbeatCookieImpl(long timestamp) {
        super();
        this.timestamp = timestamp;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie#getUpdateTimestamp()
     */
    public long getUpdateTimestamp() {
        return this.timestamp;
    }

}
