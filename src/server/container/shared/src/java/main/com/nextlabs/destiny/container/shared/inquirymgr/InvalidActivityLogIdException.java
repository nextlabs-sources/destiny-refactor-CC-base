/*
 * Created on Jan 30, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;



/**
 * This exception is thrown if the getLogDetails() API of the report
 * execution manager is supplied an activity log ID that does not 
 * exist.  
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/InvalidActivityLogIdException.java#1 $
 */

public class InvalidActivityLogIdException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     */
    public InvalidActivityLogIdException() {
        super();
    }

    /**
     * Constructor
     * @param cause
     */
    public InvalidActivityLogIdException(Throwable cause) {
        super(cause);
    }

}
