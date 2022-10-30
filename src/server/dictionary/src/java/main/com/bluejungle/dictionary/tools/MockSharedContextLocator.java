package com.bluejungle.dictionary.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.repository.IConnectionPoolFactory;
import com.bluejungle.destiny.server.shared.repository.c3p0impl.C3P0ConnectionPoolWrapperFactory;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/tools/MockSharedContextLocator.java#1 $
 */
public class MockSharedContextLocator implements IDestinySharedContextLocator {
	
    private IDestinySharedContext mockSharedContext = new IDestinySharedContext() {
    	
        private IDestinyEventManager eventManager = new IDestinyEventManager() {
        	
            private Map<String, Set<IDestinyEventListener>> eventListenerMap= new HashMap<String, Set<IDestinyEventListener>>();
            
            public void fireEvent(IDCCServerEvent event) {
                Set<IDestinyEventListener> listeners = null;
                if (eventListenerMap.get(event.getName()) != null) {
                    listeners = eventListenerMap.get(event.getName());
                    for(IDestinyEventListener listener : listeners){
                    	 listener.onDestinyEvent(event);
                    }
                }
            }
            
            public void registerForEvent(String eventName, IDestinyEventListener listener) {
                Set<IDestinyEventListener> listeners = null;
                if (eventListenerMap.get(eventName) == null) {
                    listeners = new HashSet<IDestinyEventListener>();
                } else {
                    listeners = eventListenerMap.get(eventName);
                }
                listeners.add(listener);
                eventListenerMap.put(eventName, listeners);
            }
            
            public void unregisterForEvent(String eventName, IDestinyEventListener listener) {
            }

            public void shutdown() {
            }
        };

        private IConnectionPoolFactory connectionPoolFactory = new C3P0ConnectionPoolWrapperFactory();

        public IDestinyEventManager getEventManager() {
            return eventManager;
        }

        public IDestinyRegistrationManager getRegistrationManager() {
            return null;
        }

        public IConnectionPoolFactory getConnectionPoolFactory() {
            return connectionPoolFactory;
        }
    }; 
    public IDestinySharedContext getSharedContext() {
        return mockSharedContext;
    }
}

