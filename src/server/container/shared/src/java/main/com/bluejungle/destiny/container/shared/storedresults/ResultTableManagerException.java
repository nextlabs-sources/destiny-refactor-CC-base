/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/ResultTableManagerException.java#3 $
 */

public class ResultTableManagerException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     */
    public ResultTableManagerException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public ResultTableManagerException(Throwable cause) {
        super(cause);
    }
}