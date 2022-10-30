/*
 * Created on Feb 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;


/**
 * This exception is fired if an already executed deployment request is being
 * executed again.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/RequestAlreadyExecutedException.java#1 $
 */

public class RequestAlreadyExecutedException extends SingleErrorBlueJungleException {
    
    /**
     * Constructor
     * 
     */
    public RequestAlreadyExecutedException(String requestID) {
        super();
        this.addNextPlaceholderValue(requestID);
    }

    /**
     * Constructor
     * 
     * @param nestedEx
     */
    public RequestAlreadyExecutedException(String requestID, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(requestID);
    }
}