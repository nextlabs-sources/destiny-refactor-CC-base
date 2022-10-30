/*
 * Created on Apr 8, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/heartbeat/IHeartbeatProvider.java#1 $
 */
package com.bluejungle.framework.heartbeat;

import java.io.Serializable;

public interface IHeartbeatProvider {
    Serializable serviceHeartbeatRequest(
        String name
    ,   String requestData);
}

