/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is a dummy shared context locator class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/deployment/MockSharedContextLocator.java#2 $
 */

public class MockSharedContextLocator implements IDestinySharedContextLocator, IHasComponentInfo<MockSharedContextLocator> {

    private IDestinySharedContext sharedContext = new MockSharedContext();
    private static final ComponentInfo<MockSharedContextLocator> COMPONENT_INFO = new ComponentInfo<MockSharedContextLocator>(COMP_NAME, MockSharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<MockSharedContextLocator> getComponentInfo() {
        return COMPONENT_INFO;
    }

    /**
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.sharedContext;
    }

    protected class MockEventMgr implements IDestinyEventManager {

        protected IDestinyEventListener listener; //We assume only one listener
                                                  // for now
        protected Set subscriptions = new HashSet();

        /**
         * Resets the mock object
         */
        public void reset() {
            this.subscriptions.clear();
        }

        /**
         * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#fireEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
         */
        public void fireEvent(IDCCServerEvent event) {
            this.listener.onDestinyEvent(event);
        }

        /**
         * Returns the size of the subscription set
         * 
         * @return the size of the subscription set
         */
        public int getSubscriptionSize() {
            return subscriptions.size();
        }

        /**
         * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#registerForEvent(java.lang.String,
         *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener)
         */
        public void registerForEvent(String eventName, IDestinyEventListener listener) {
            this.subscriptions.add(eventName);
            this.listener = listener;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#unregisterForEvent(java.lang.String,
         *      com.bluejungle.destiny.server.shared.events.IDestinyEventListener)
         */
        public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
            this.subscriptions.remove(eventName);
        }

        /**
         * @see com.bluejungle.destiny.server.shared.events.IDestinyEventManager#shutdown()
         */
        public void shutdown() {
        }
    }

    protected class MockSharedContext implements IDestinySharedContext {

        private MockEventMgr evtMgr = new MockEventMgr();
        private IConnectionPoolFactory connectionPoolFactory = new C3P0ConnectionPoolWrapperFactory();

        /**
         * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
         */
        public IDestinyEventManager getEventManager() {
            return evtMgr;
        }

        /**
         * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getConfigurationStore()
         */
        public IDestinyConfigurationStore getConfigurationStore() {
            return null;
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
}
