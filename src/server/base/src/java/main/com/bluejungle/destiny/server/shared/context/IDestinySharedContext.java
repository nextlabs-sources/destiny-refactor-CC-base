/*
 * Created on Nov 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;

/**
 * This interface is implemented by the Destiny shared context.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/IDestinySharedContext.java#2 $:
 */
public interface IDestinySharedContext {

    /**
     * Returns the destiny event manager
     * 
     * @return the destiny event manager
     */
    IDestinyEventManager getEventManager();

    /**
     * Returns the destiny registration manager
     * 
     * @return the destiny registration manager
     */
    IDestinyRegistrationManager getRegistrationManager();

    /**
     * Returns the destiny repository connection pool factory
     * 
     * @return repository connection pool factory
     */
    IConnectionPoolFactory getConnectionPoolFactory();
}