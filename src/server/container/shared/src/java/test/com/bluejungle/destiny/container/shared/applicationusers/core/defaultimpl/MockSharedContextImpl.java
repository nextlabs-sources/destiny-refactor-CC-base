/*
 * Created on Sep 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockSharedContextImpl.java#1 $
 */

public class MockSharedContextImpl implements IDestinySharedContext {

    private MockEventManagerImpl eventMgr;

    /**
     * Constructor
     *  
     */
    public MockSharedContextImpl() {
        super();
        this.eventMgr = new MockEventManagerImpl();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
     */
    public IDestinyEventManager getEventManager() {
        return this.eventMgr;
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
        return null;
    }
}