/*
 * Created on Mar 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/InvalidIDException.java#1 $
 */

public class InvalidIDException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     *  
     */
    public InvalidIDException(Long agentID) {
        super();
        this.addNextPlaceholderValue(agentID);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public InvalidIDException(Long agentID, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(agentID);
    }
}