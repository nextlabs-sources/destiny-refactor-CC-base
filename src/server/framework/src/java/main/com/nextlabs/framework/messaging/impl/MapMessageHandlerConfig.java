package com.nextlabs.framework.messaging.impl;

import java.util.Map;

import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.framework.messaging.IMessageHandlerConfig;

public class MapMessageHandlerConfig extends HashMapConfiguration implements
        IMessageHandlerConfig {
    
    public MapMessageHandlerConfig() {
        super();
    }

    public MapMessageHandlerConfig(int initialSize) {
        super(initialSize);
    }
    
    public MapMessageHandlerConfig(IMessageHandlerConfig config) {
        super();

        for (PropertyKey key : config.propertyKeySet()) {
            Object value = config.getProperty(key);
            setProperty(key, value);
        }
    }

    public MapMessageHandlerConfig(Map<?, Object> properties) {
        super(properties);
    }

    public <T> T getProperty(PropertyKey<T> key) {
        return super.get(key);
    }

    public <T> T getProperty(PropertyKey<T> key, T defaultValue) {
        return super.get(key, defaultValue);
    }
}
 
