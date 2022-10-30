/*
 * Created on Feb 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.events.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;

/**
 * This is the DCC server event implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/events/impl/DCCServerEventImpl.java#1 $
 */

public class DCCServerEventImpl implements IDCCServerEvent {

    private String name;
    private Properties properties = new Properties();

    public DCCServerEventImpl(String name) {
        super();
        this.name = name;
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDCCServerEvent#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDCCServerEvent#getProperties()
     */
    public Properties getProperties() {
        return this.properties;
    }

}
