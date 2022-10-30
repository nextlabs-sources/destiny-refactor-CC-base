package com.nextlabs.framework.messaging.impl;

import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.framework.messaging.IMessageHandlerInstructions;

public class MapMessageHandlerInstructions extends HashMapConfiguration
        implements IMessageHandlerInstructions {

    public <T> T getProperty(PropertyKey<T> key) {
        return get(key);
    }

    public <T> T getProperty(PropertyKey<T> key, T defaultValue) {
        return get(key, defaultValue);
    }
}
 
