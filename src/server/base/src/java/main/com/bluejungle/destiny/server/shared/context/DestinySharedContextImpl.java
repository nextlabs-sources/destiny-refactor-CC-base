/*
 * Created on Nov 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.server.shared.context;

import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.destiny.server.shared.internal.IInternalSharedContext;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;

/**
 * This is the implementation of the Destiny Shared context. The shared context
 * instantiates other shared modules. It also sets dependencies between modules,
 * if any dependencies exists.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/DestinySharedContextImpl.java#2 $:
 */
public class DestinySharedContextImpl implements IDestinySharedContext, IInternalSharedContext {

    private IDestinyEventManager eventMgr;
    private IDestinyRegistrationManager registrationMgr;
    private IConnectionPoolFactory connectionPoolFactory;

    private boolean isInitialized = false;

    /**
     * Initialization function. Init attributes contain class names to be used
     * for the various shared context objects. If the shared context is already
     * initialized, no initialization takes place.
     * 
     * @param eventMgr
     *            event manager class name
     * @param logMgr
     *            log manager class name
     * @param regMgr
     *            registration manager class name
     * @throws FactoryInitException
     *             if initialization fails
     */
    public void init(String eventMgr, String regMgr) throws FactoryInitException {
        if (this.isInitialized) {
            return;
        }

        this.eventMgr = (IDestinyEventManager) instantiateManager(eventMgr);
        this.registrationMgr = (IDestinyRegistrationManager) instantiateManager(regMgr);
        this.connectionPoolFactory = (IConnectionPoolFactory) instantiateManager(C3P0ConnectionPoolWrapperFactory.class.getName());

        if (this.eventMgr instanceof IDCSFRegistrationListener) {
            this.registrationMgr.addDCSFRegistrationListener((IDCSFRegistrationListener) this.eventMgr);
        }

        this.isInitialized = true;
    }

    /**
     * Tear down any shared context components
     */
    public void destroy() {
        eventMgr.shutdown();
    }

    /**
     * Returns the event manager
     * 
     * @return the event manager
     */
    public IDestinyEventManager getEventManager() {
        return this.eventMgr;
    }

    /**
     * Returns the registration manager
     * 
     * @return the registration manager
     */
    public IDestinyRegistrationManager getRegistrationManager() {
        return this.registrationMgr;
    }

    /**
     * Instantiates a new object instance
     * 
     * @param className
     *            name of the class to instantiate
     * @return a new instance of this object
     * @throws FactoryInitException
     *             if the manager cannot be initialized
     */
    private Object instantiateManager(String className) throws FactoryInitException {
        try {
            Class clasz = Class.forName(className);
            return (clasz.newInstance());
        } catch (ClassNotFoundException classNotFound) {
            throw new FactoryInitException(classNotFound.getCause());
        } catch (IllegalAccessException illegalAccess) {
            throw new FactoryInitException(illegalAccess.getCause());
        } catch (InstantiationException instantiationException) {
            throw new FactoryInitException(instantiationException.getCause());
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConnectionPoolFactory()
     */
    public IConnectionPoolFactory getConnectionPoolFactory() {
        return this.connectionPoolFactory;
    }
}
