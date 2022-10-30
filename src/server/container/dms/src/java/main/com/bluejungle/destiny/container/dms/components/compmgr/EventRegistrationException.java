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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/compmgr/EventRegistrationException.java#1 $
 */

public class EventRegistrationException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     *  
     */
    public EventRegistrationException(String eventName, String callbackURL) {
        super();
        this.addNextPlaceholderValue(eventName);
        this.addNextPlaceholderValue(callbackURL);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public EventRegistrationException(String eventName, String callbackURL, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(eventName);
        this.addNextPlaceholderValue(callbackURL);
    }
}