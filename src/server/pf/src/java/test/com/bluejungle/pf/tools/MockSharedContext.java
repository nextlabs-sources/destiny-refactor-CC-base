/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.tools;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/MockSharedContext.java#1 $
 */

public class MockSharedContext implements IDestinySharedContext {

    private IConnectionPoolFactory connectionPoolFactory = new C3P0ConnectionPoolWrapperFactory();
    private MockEventManager mockEventManager = new MockEventManager();;

    /**
     * Constructor
     *  
     */
    public MockSharedContext() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
     */
    public IDestinyEventManager getEventManager() {
        return mockEventManager;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getRegistrationManager()
     */
    public IDestinyRegistrationManager getRegistrationManager() {
        return null;
    }
    
    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConnectionPoolFactory()
     */
    public IConnectionPoolFactory getConnectionPoolFactory() {
        return this.connectionPoolFactory;
    }
}