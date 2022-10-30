package com.bluejungle.destiny.container.dps;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the DPS context listener class.
 * 
 * @author sergey
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dps/src/java/main/com/bluejungle/destiny/container/dps/DPSContextListener.java#1 $
 */

public class DPSContextListener extends DCCContextListener {

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DPS;
    }

    @Override
    public String getTypeDisplayName() {
        return "Policy Management Server";
    }
}