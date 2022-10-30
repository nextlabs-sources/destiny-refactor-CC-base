/*
 * Created on Feb 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events;

import java.util.Properties;

/**
 * This interface represents a DCC server event object.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/events/IDCCServerEvent.java#1 $
 */

public interface IDCCServerEvent {

    /**
     * Returns the event name
     * 
     * @return the event name
     */
    public String getName();

    /**
     * Returns the properties associated with the event
     * 
     * @return the properties associated with the event (may be empty, but not
     *         null)
     */
    public Properties getProperties();
}
