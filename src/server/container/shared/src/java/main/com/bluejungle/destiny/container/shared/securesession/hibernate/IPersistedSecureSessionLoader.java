/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;

/**
 * IPersistedSecureSessionLoader is responsible for loading a persistent version of a secure session.  It's utilized for lazy loading purposes
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/hibernate/IPersistedSecureSessionLoader.java#1 $
 */
public interface IPersistedSecureSessionLoader {
    /**
     * Retrieve a persisted session by id
     * @param id the ID of the session to retrieve
     * @return a persisted session with the specified id
     * @throws SecureSessionPersistenceException if a failure occurs while retrieving the session
     * @throws SecureSessionNotFoundException is a session with the specified id cannot be found
     */
    IPersistedSecureSession getPersistedSession(Long id) throws SecureSessionPersistenceException, SecureSessionNotFoundException;
}
