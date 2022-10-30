/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the DAC Context listener class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/DACContextListener.java#1 $
 */

public class DACContextListener extends DCCContextListener {

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DAC;
    }

    @Override
    public String getTypeDisplayName() {
        return "Intelligence Server";
    }
    
}
