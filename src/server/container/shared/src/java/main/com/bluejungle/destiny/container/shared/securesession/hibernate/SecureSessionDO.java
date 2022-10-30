/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A persistent portion of a secure session
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/security/securesession/hibernate/SecureSessionDO.java#2 $
 */
public class SecureSessionDO implements IPersistedSecureSession {

    private Long id;
    private long endOfLife;
    private Map<String, String> properties;

    /**
     * Empty constructor for Hibernate
     *  
     */
    public SecureSessionDO() {
        super();
    }

    /**
     * Create an instance of SecureSessionDO
     * 
     * @param id
     *            the id of the secure session
     * @param endOfLife
     *            the end of life time of this secure session
     * @param properties
     *            a set of properties to associate with this secure session
     */
    public SecureSessionDO(Long id, Long endOfLife, Properties properties) {
        this(endOfLife, properties);
        this.id = id;
    }

    /**
     * Create a SecureSessionDO instance
     * 
     * @param endOfLife
     *            the end of life time of this secure session
     * @param sessionProperties
     *            a set of properties to associate with this secure session
     */
    public SecureSessionDO(Long endOfLife, Properties sessionProperties) {
        this();

        if (sessionProperties == null) {
            throw new NullPointerException("sessionProperties cannot be null.");
        }

        if (endOfLife == null) {
            throw new NullPointerException("endOfLife cannot be null.");
        }

        // Convert the Properties Object to a Map for Hibernate persistence
        // purposes
        this.properties = new HashMap(sessionProperties);

        this.endOfLife = endOfLife;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.ISecureSession#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set this session ID
     * 
     * @param id
     *            the ID to set
     */
    void setId(Long id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }

        this.id = id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.hibernate.IPersistedSecureSession#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        return (String) properties.get(name);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.hibernate.IPersistedSecureSession#getProperties()
     */
    public Properties getProperties() {
        Properties propertiesToReturn = new Properties();

        if (this.properties != null) {
            for(Map.Entry<String, String> nextProperty : this.properties.entrySet()) {
                propertiesToReturn.setProperty(nextProperty.getKey(), nextProperty.getValue());
            }
        }

        return propertiesToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.hibernate.IPersistedSecureSession#getEndOfLife()
     */
    public long getEndOfLife() {
        return this.endOfLife;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.securesession.hibernate.IPersistedSecureSession#setEndOfLife(java.lang.Long)
     */
    public void setEndOfLife(Long endOfLife) {
        if (endOfLife == null) {
            throw new NullPointerException("endOfLife cannot be null.");
        }

        this.endOfLife = endOfLife;
    }

    /**
     * Retrieve the set of properties associated with this secure session.
     * Current only exposed to Hibernate
     * 
     * @return the set of properties associated with this secure session.
     */
    Map<String, String> getPropertiesAsMap() {
        return this.properties;
    }

    /**
     * Set the set of properties associated with this secure session. Current
     * only exposed to Hibernate
     * 
     * @param properties
     *            the set of properties associated with this secure session.
     */
    void setPropertiesAsMap(Map<String, String> properties) {
        if (properties == null) {
            throw new NullPointerException("properties cannot be null.");
        }

        this.properties = properties;
    }
}