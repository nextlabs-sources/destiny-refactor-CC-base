package com.nextlabs.framework.messaging.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;
import com.nextlabs.framework.messaging.IMessageHandlerManager;
import com.nextlabs.framework.messaging.MessagingException;

public class MessageHandlerManagerImpl implements IMessageHandlerManager,
        IHasComponentInfo<IMessageHandlerManager>, IInitializable {
    
    public static final ComponentInfo<IMessageHandlerManager> COMP_INFO =
        new ComponentInfo<IMessageHandlerManager>(
                MessageHandlerManagerImpl.class,
                LifestyleType.SINGLETON_TYPE
        );
    
    private Map<String, IMessageHandler> nameToHandlerMap;
    
    public ComponentInfo<IMessageHandlerManager> getComponentInfo() {
        return COMP_INFO;
    }
    
    public void init() {
        nameToHandlerMap = new HashMap<String, IMessageHandler>();
    }

    /**
     * @see com.nextlabs.framework.messaging.IMessageHandlerManager#getMessageHandler(java.lang.String)
     */
    public IMessageHandler getMessageHandler(String name) {
        return nameToHandlerMap.get(name);
    }
    
    /**
     * @see com.nextlabs.framework.messaging.IMessageHandlerManager#defineMessageHandler(java.lang.String, java.lang.Class, com.nextlabs.framework.messaging.IMessageHandlerConfig)
     */
    public IMessageHandler defineMessageHandler(
            String name,
            Class<? extends IMessageHandler> senderClass, 
            IMessageHandlerConfig handlerConfig)
            throws MessagingException {
        IMessageHandler messageHandler;
        try {
            messageHandler = senderClass.newInstance();
        } catch (InstantiationException e) {
            throw new MessagingException(MessagingException.Type.INIT, e);
        } catch (IllegalAccessException e) {
            throw new MessagingException(MessagingException.Type.INIT, e);
        }
        messageHandler.init(handlerConfig, name);
        
        nameToHandlerMap.put(name, messageHandler);
        
        return messageHandler;
    }
    
    public void unregister(IMessageHandler handler) throws NoSuchElementException {
        if (nameToHandlerMap.containsValue(handler)) {
            nameToHandlerMap.remove(handler.getName());
        } else {
            throw new NoSuchElementException(handler.toString());
        }
    }

   
}
 
