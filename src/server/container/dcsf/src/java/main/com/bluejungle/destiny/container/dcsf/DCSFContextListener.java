/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the DCSF context listener class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/DCSFContextListener.java#1 $
 */

public class DCSFContextListener extends DCCContextListener {

    /**
     * Returns the component type (DCSF)
     * 
     * @return the componetn type (DCSF)
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DCSF;
    }
    
    /**
     * Returns the container class name
     * 
     * @return the container class name
     */
    protected Class<? extends IDCCContainer> getContainerClassName() {
        return DCSFContainerImpl.class;
    }

    @Override
    public String getTypeDisplayName() {
        return "Communication Server";
    }
    
}
