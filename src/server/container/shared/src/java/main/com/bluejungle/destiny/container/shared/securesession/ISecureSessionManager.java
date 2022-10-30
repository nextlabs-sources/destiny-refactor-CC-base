/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession;

import java.util.Properties;

/**
 * A manager used to create and retrieve secure session instances
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/ISecureSessionManager.java#1 $
 */
public interface ISecureSessionManager {

    public static String COMPONENT_NAME = "SecureSessionManager";

    /**
     * Create a secure session
     * 
     * @param sessionProperties a set of properties to associate with the secure session
     * @return the created secure session
     * @throws SecureSessionPersistenceException if an error occurs while persisting the secure session
     */
    public ISecureSession createSession(Properties sessionProperties) throws SecureSessionPersistenceException;

    /**
     * Retrieve a secure session by its associated key.  This key is generated through a call to <code>ISecureSession.generateKey()</code>
     * 
     * @param sessionKey the key by which to retrieve a secure session
     * @return the secure session associated with the specifid key
     * @throws SecureSessionPersistenceException if a failure occurs while accessing the persistenc store
     */
    public ISecureSession getSessionByKey(String sessionKey) throws SecureSessionPersistenceException;

    /**
     * Return the default timeout for sessions
     */
    public long getDefaultTimeout();
}
