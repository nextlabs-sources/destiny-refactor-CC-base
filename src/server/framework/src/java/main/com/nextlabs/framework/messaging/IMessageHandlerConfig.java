package com.nextlabs.framework.messaging;

import java.util.Set;

import com.bluejungle.framework.comp.PropertyKey;

/**
 * An instance of IMessageHandlerConfig is used to configure a particular 
 * IMessageHandler instance with a set of static configuration parameters.
 */
public interface IMessageHandlerConfig {
    /**
     * Retrieve a configuration property by name
     * @param <T>
     * @param propertyName
     * @return
     */
	<T> T getProperty(PropertyKey<T> propertyName);
	
	<T> T getProperty(PropertyKey<T> propertyName, T defaultValue);
	
	Set<PropertyKey> propertyKeySet();
    
    boolean contains(PropertyKey<?> propertyName);
}
 
