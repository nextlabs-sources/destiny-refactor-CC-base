/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;


/**
 * Implementation of an ISecureSession
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionImpl.java#1 $
 */

public class SecureSessionImpl implements ISecureSession {
    private static final Log LOG = LogFactory.getLog(SecureSessionImpl.class.getName());
    
    private Long id;
    private Long expirationTime;
    private Long endOfLife;
    private IPersistedSecureSession persistentSession;
    private IPersistedSecureSessionLoader sessionLoader;
    
    /**
     * Create an instance of a SecureSession
     * 
     * @param sessionLoader the persistent session load used to lazy load the persistent portion of the session
     * @param sessionID the id of the session
     * @param expirationTime the expiration time of the session
     * @param endOfLife the end of life time of the session
     */
    public SecureSessionImpl(IPersistedSecureSessionLoader sessionLoader, Long sessionID, Long expirationTime, Long endOfLife) {
        if (sessionLoader == null) {
            throw new NullPointerException("sessionLoader cannot be null.");
        }
        
        if (sessionID == null) {
            throw new NullPointerException("sessionID cannot be null.");
        }

        if (expirationTime == null) {
            throw new NullPointerException("expirationTime cannot be null.");
        }

        if (endOfLife == null) {
            throw new NullPointerException("endOfLife cannot be null.");
        }
        
        this.sessionLoader = sessionLoader;        
        this.id = sessionID;
        this.expirationTime = expirationTime;
        this.endOfLife = endOfLife;
        
        // In this case, the persistent session is lazily loaded
    }

    /**
     * Create an instance of a SecureSession
     * 
     * @param persistentSession the persistent portion of the secure session
     * @param sessionExpirationTime the expiration time of the session
     */
    public SecureSessionImpl(IPersistedSecureSession persistentSession, Long sessionExpirationTime) {
        if (persistentSession == null) {
            throw new NullPointerException("persistentSession cannot be null.");
        }
 
        if (sessionExpirationTime == null) {
            throw new NullPointerException("expirationTime cannot be null.");
        }
                
        this.id = persistentSession.getId();
        this.expirationTime = sessionExpirationTime;
        this.endOfLife = persistentSession.getEndOfLife();        
        this.persistentSession = persistentSession;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSession#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSession#generateKey()
     */
    public String generateKey() {
        SecureSessionKey key = new SecureSessionKey(getId(), getExpirationTime(), getEndOfLife());
        return SecureSessionKeyKeeper.generateKeyString(key);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSession#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        return getProperties().getProperty(name);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSession#getProperties()
     */
    public Properties getProperties() {
        Properties propertiesToReturn = null;
        try {
            IPersistedSecureSession persistedSession = getPersistentSession();
            propertiesToReturn = persistedSession.getProperties();
        } catch (SecureSessionNotFoundException exception) {
            /* Not much that we can do here */
            LOG.error("Failed to load secure session properties", exception);
            throw new IllegalStateException("Persistent session could not be found.");
        } catch (SecureSessionPersistenceException exception) {
            /* Not much that we can do here */
            LOG.error("Failed to load secure session properties", exception);
            throw new IllegalStateException("Persistent session could not be loaded.");
        }

        return propertiesToReturn;
    }

    /**
     * Retrieve the expiration time of this session
     * @return the expiration time of this session
     */
    private Long getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Retrieve the end of life time of this session
     * @return the end of life time of this session
     */
    private Long getEndOfLife() {
        return this.endOfLife;
    }
           
    /**
     * Retrieve the persistent portion of this session
     * @return the persistent portion of this session
     * @throws SecureSessionPersistenceException if a failure occured while retrieving the session
     * @throws SecureSessionNotFoundException if the persisted session could not be found
     */
    private IPersistedSecureSession getPersistentSession() throws SecureSessionPersistenceException, SecureSessionNotFoundException {
        if (this.persistentSession == null) {
            this.persistentSession = this.sessionLoader.getPersistedSession(getId());
        }
        
        return this.persistentSession; 
    }
}