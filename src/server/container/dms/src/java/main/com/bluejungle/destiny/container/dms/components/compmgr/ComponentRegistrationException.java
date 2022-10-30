/*
 * Created on Mar 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.Collections;
import java.util.List;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;
import com.bluejungle.framework.utils.CollectionUtils;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/compmgr/ComponentRegistrationException.java#1 $
 */

public class ComponentRegistrationException extends SingleErrorBlueJungleException {
    
    private final List<String> errorMessages;
    
    /**
     * Constructor
     * 
     */
//    public ComponentRegistrationException(String componentName) {
//        this(componentName, (List<String>)null);
//    }
    
    public ComponentRegistrationException(String componentName, List<String> errorMessages) {
        super();
        this.errorMessages = Collections.unmodifiableList(errorMessages);
        this.addNextPlaceholderValue(componentName);
    }
    
    public ComponentRegistrationException(String componentName, String errorMessage) {
        this(componentName, Collections.singletonList(errorMessage));
    }

    /**
     * Constructor
     * @param cause
     */
//    public ComponentRegistrationException(String componentName, Throwable cause) {
//        this(componentName, null, cause);
//    }
    
    /**
     * Constructor
     * @param cause
     */
    public ComponentRegistrationException(String componentName, Throwable cause) {
        super(cause);
        this.errorMessages = Collections.singletonList(cause.getMessage());
        this.addNextPlaceholderValue(componentName);
    }
    
    
    
    @Override
    public String toString() {
        return "ComponentRegistrationException [errorMessages=" 
                + CollectionUtils.asString(errorMessages, " ") + "]";
    }

    /**
     * never null
     * @return
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
}
