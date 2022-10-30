/*
 * Created on Jul 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Properties;

import com.bluejungle.destiny.server.shared.configuration.IMessageHandlerConfigurationDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/MessageHandlerConfigurationDO.java#1 $
 */

public class MessageHandlerConfigurationDO implements IMessageHandlerConfigurationDO {
    private String name;
    private String clazz;
    private Properties properties;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return clazz;
    }

    public void setClassName(String clazz) {
        this.clazz = clazz;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(PropertyList props) {
        if (props != null) {
            this.properties = props.getProperties();
        }
    }
}
