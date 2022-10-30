/*
 * Created on Nov 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import com.bluejungle.destiny.server.shared.events.impl.DestinyEventManagerRemoteImpl;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.destiny.server.shared.internal.IInternalSharedContext;
import com.bluejungle.destiny.server.shared.registration.impl.DestinyRegistrationManagerImpl;

/**
 * Factory for the Destiny Shared Context. Change in the near future to have the
 * factories be loaded from a config file in the META-INF directory of a jar
 * file
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/context/DestinySharedContextFactory.java#2 $:
 */
public class DestinySharedContextFactory {

    private static final String SHARED_CONTEXT_CLASS_NAME = DestinySharedContextImpl.class.getName();
    private static final DestinySharedContextFactory SINGLETON_INSTANCE = new DestinySharedContextFactory();

    private final Object LOCK = new Object();
    private IInternalSharedContext sharedContext;
    private boolean destroyed = false;

    /**
     * Retrieve an instance of DestinySharedContextFactory
     * 
     * @return an instance of DestinySharedContextFactory
     */
    public static final DestinySharedContextFactory getInstance() {
        return SINGLETON_INSTANCE;
    }

    /**
     * Retrieve the Destiny Shared Context.
     * 
     * @return the Destiny Shared Context
     * @throws FactoryInitException
     *             if factory initialization fails
     */
    public IDestinySharedContext getSharedContext() throws FactoryInitException {
        synchronized (this.LOCK) {
            // if the object has already been created, return it immediately
            if (sharedContext == null) {

                if (destroyed) {
                    throw new FactoryInitException("Attempting to get shared context after it has been destroyed");
                }

                sharedContext = instantiateContext(SHARED_CONTEXT_CLASS_NAME);
                sharedContext.init(DestinyEventManagerRemoteImpl.class.getName(), DestinyRegistrationManagerImpl.class.getName());
            }
        }

        return sharedContext;
    }

    /**
     * Destroy the shared context
     */
    public void destroySharedContext() {
        synchronized (this.LOCK) {
            if (!destroyed) {
                sharedContext.destroy();
                sharedContext = null;
            }
            destroyed = true;
        }
    }

    /**
     * Instantiates a new context instance
     * 
     * @param className
     *            name of the class to instantiate
     * @return a new instance of this object
     * @throws FactoryInitException
     *             if the context cannot be instantiated
     */
    private IInternalSharedContext instantiateContext(String className) throws FactoryInitException {
        IInternalSharedContext objectToReturn = null;
        try {
            Class clasz = Class.forName(className);
            objectToReturn = (IInternalSharedContext) (clasz.newInstance());
        } catch (ClassNotFoundException classNotFound) {
            throw new FactoryInitException(classNotFound);
        } catch (IllegalAccessException illegalAccess) {
            throw new FactoryInitException(illegalAccess);
        } catch (InstantiationException instantiationException) {
            throw new FactoryInitException(instantiationException);
        }

        return objectToReturn;
    }
}
