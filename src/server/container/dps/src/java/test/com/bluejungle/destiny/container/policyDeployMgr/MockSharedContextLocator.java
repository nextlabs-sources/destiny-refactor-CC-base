/**
 * 
 */
package com.bluejungle.destiny.container.policyDeployMgr;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * A Mock Shared Context Locator to return the MockSharedContext
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dps/src/java/test/com/bluejungle/destiny/container/policyDeployMgr/MockSharedContextLocator.java#1 $
 */

public class MockSharedContextLocator implements IDestinySharedContextLocator {
    
    IDestinySharedContext sharedContext;
    
    /**
     * Constructor
     */
    public MockSharedContextLocator(){
        super();
        this.sharedContext = new MockSharedContext();
    }
    
    /**
     * @return the shared context
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.sharedContext;
    }
    
    public class MockSharedContext implements IDestinySharedContext {

        private IDestinyEventManager eventMgr;

        /**
         * Constructor
         * 
         */
        public MockSharedContext(){
            super();
            this.eventMgr = new MockEventManager();
        }

        /**
         * @return the event manager
         * @see com.bluejungle.destiny.server.shared.context.IDestinySharedContext#getEventManager()
         */
        public IDestinyEventManager getEventManager() {
            return this.eventMgr;
        }
        /**
         * @return the registration manager
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
}
