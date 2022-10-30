/*
 * Created on May 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * This exception is thrown if the report definition contains arguments that
 * don't make sense.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/InvalidReportArgumentException.java#1 $
 */

public class InvalidReportArgumentException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     *  
     */
    public InvalidReportArgumentException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public InvalidReportArgumentException(Throwable cause) {
        super(cause);
    }
}