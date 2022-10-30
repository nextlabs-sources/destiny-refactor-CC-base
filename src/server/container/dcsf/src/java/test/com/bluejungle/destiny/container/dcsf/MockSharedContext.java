/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockSharedContext.java#1 $:
 */

public class MockSharedContext implements IDestinySharedContext {

    private IDestinyRegistrationManager regMgr;
    private IConnectionPoolFactory connectionPoolFactory = new C3P0ConnectionPoolWrapperFactory();

    /**
     * Constructor
     */
    public MockSharedContext() {
        super();
        this.regMgr = new MockRegistrationManager();
    }

    /**
     * @return the event manager
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
     */
    public IDestinyEventManager getEventManager() {
        return null;
    }

    /**
     * @return the registration manager
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getRegistrationManager()
     */
    public IDestinyRegistrationManager getRegistrationManager() {
        return this.regMgr;
    }

    /**
     * Initializes the shared context
     * 
     * @param confMgr
     *            configuration manager class name
     * @param eventMgr
     *            event manager class name
     * @param logMgr
     *            log manager class name
     * @param regMgr
     *            registration manager class name
     * @throws FactoryInitException
     *             if init fails
     */
    public void init(String confMgr, String eventMgr, String logMgr, String regMgr) throws FactoryInitException {
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConnectionPoolFactory()
     */
    public IConnectionPoolFactory getConnectionPoolFactory() {
        return this.connectionPoolFactory;
    }
}