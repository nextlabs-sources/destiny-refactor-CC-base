/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the DABS context listener class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/DABSContextListener.java#4 $
 */

public class DABSContextListener extends DCCContextListener {

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DABS;
    }

    /**
     * Returns the container class name
     * 
     * @return the container class name
     */
    protected Class<? extends IDCCContainer> getContainerClassName() {
        return DABSContainerImpl.class;
    }

    @Override
    public String getTypeDisplayName() {
        return "ICENet Server";
    }
    
}
