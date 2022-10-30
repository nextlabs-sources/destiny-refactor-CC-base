/*
 * Created on Feb 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dem;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the context listener class for the DEM server component
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dem/src/java/main/com/bluejungle/destiny/container/dem/DEMContextListener.java#1 $
 */

public class DEMContextListener extends DCCContextListener {

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DEM;
    }

    @Override
    public String getTypeDisplayName() {
        return "Enrollment Manager";
    }

}
