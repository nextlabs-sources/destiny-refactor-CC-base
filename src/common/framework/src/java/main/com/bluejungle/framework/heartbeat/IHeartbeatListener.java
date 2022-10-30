/*
 * Created on Mar 31, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc., All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/heartbeat/IHeartbeatListener.java#1 $
 */
package com.bluejungle.framework.heartbeat;

import java.io.Serializable;

/**
 * Listeners both produce and consume information for the heatbeat.  Listeners
 * are created at runtime and will register with the heartbeat manager.  Listeners
 * can register with multiple names/ids, which means that they will produce/consume
 * for all those ids
 */

public interface IHeartbeatListener {
    /**
     * Produce data for specified id
     */
    public Serializable prepareRequest(String id);

    /**
     * Process the data associated with the given id
     */
    public void processResponse(String id, String data);
}
