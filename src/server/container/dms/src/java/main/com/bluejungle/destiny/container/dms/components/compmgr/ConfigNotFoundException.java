/*
 * Created on Mar 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/compmgr/ConfigNotFoundException.java#1 $
 */

public class ConfigNotFoundException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     *  
     */
    public ConfigNotFoundException(String componentName, String componentType) {
        super();
        this.addNextPlaceholderValue(componentName);
        this.addNextPlaceholderValue(componentType);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public ConfigNotFoundException(String componentName, String componentType, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(componentName);
        this.addNextPlaceholderValue(componentType);
    }
}