/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * SecureSessionPersistenceException is thrown when a failure occurs while
 * attempting to create, retrieve, or delete a secure session from the
 * persistence store
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/security/securesession/SecureSessionPersistenceException.java#3 $
 */

public class SecureSessionPersistenceException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     */
    public SecureSessionPersistenceException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public SecureSessionPersistenceException(Throwable cause) {
        super(cause);
    }
}