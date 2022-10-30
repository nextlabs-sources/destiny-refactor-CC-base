/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * SecureSessionNotFoundException is thrown when an attempt is made to retrieve
 * a session by id which does not exists in the persistence store
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/security/securesession/hibernate/SecureSessionNotFoundException.java#1 $
 */

public class SecureSessionNotFoundException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     * 
     * @param sessionID
     */
    public SecureSessionNotFoundException(String sessionID) {
        super();
        this.addNextPlaceholderValue(sessionID);
    }

    /**
     * Constructor
     * 
     * @param sessionID
     * @param cause
     */
    public SecureSessionNotFoundException(String sessionID, Throwable cause) {
        super(cause);
        this.addNextPlaceholderValue(sessionID);
    }
}