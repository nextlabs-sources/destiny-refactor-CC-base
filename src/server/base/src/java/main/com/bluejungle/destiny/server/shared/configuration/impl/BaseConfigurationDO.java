package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

public abstract class BaseConfigurationDO {
    private Properties properties;
    
    BaseConfigurationDO() {
    }
    
    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(PropertyList props) {
        if (props != null) {
            this.properties = props.getProperties();
        }
    }
}
