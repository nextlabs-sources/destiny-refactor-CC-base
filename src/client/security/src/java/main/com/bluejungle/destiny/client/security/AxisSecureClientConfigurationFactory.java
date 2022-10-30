package com.bluejungle.destiny.client.security;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author sergey
 * @author sgoldstein
 * 
 * @version $ Id: $
 */

import org.apache.axis.EngineConfiguration;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;

/**
 * This implementation returns the configuration from a hard-coded resource
 * stream
 * {@see com.bluejungle.destiny.client.security.AxisSecureClientEngineConfiguration}
 */
public class AxisSecureClientConfigurationFactory extends EngineConfigurationFactoryDefault {

    /**
     * This immutable class is a singleton.
     */
    private static final EngineConfigurationFactory INSTANCE = new AxisSecureClientConfigurationFactory();

    /**
     * @see org.apache.axis.EngineConfigurationFactory#getClientEngineConfig()
     */
    public EngineConfiguration getClientEngineConfig() {
        return new AxisSecureClientEngineConfiguration();
    }

    /**
     * Creates and returns a new EngineConfigurationFactory. If a factory cannot
     * be created, return 'null'.
     * 
     * @see org.apache.axis.configuration.EngineConfigurationFactoryFinder
     */
    public static EngineConfigurationFactory newFactory(Object param) {
        if (param != null) {
            return null; // Intended for another configuration factory
        }
        return INSTANCE;
    }
}
