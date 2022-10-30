/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * An instance of ISecureSessionVault is utilized to store a SecureSession
 * instance on a client application for later submission to the server during a
 * web service call. To specify the implementation to utilize for a specific
 * client application, create a jar with the implementation and ####. Note that
 * an implementation instance may be required to handle multiple threads and
 * multiple users concurrently (e.g. in the case of a web application). One
 * method of achieving this may be through the use of a ThreadLocal.
 * 
 * FUTURE ENHANCEMENT IF NECESSARY - Provide a programmatic method of setting
 * the SecureSessionVault on SecureSessionVaultGateway
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/axis/ISecureSessionVault.java#1 $
 */

public interface ISecureSessionVault {

    /**
     * Store a secure session instance for later retrieval
     * 
     * @param secureSession
     *            the secure session to store
     */
    public void storeSecureSession(SecureSession secureSession);

    /**
     * Retrieve a stored secure session
     * 
     * @return a stored session
     */
    public SecureSession getSecureSession();

    /**
     * Clear a stored session from the vault
     */
    public void clearSecureSession();
}