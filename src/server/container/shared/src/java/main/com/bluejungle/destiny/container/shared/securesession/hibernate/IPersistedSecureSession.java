/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.Properties;

/**
 * A persistent version of a secure session
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/hibernate/IPersistedSecureSession.java#1 $
 */
public interface IPersistedSecureSession {
    
    /**
     * Retrieve the persistence session ID
     * @return the persistence session ID
     */
    public Long getId();
    
    /**
     * Retrieve a secure session property by name
     * @param name the name of the property to retrieve
     * @return the property value of the property with the specified name or null if the property does not exist
     */
    public String getProperty(String name);
    
    /**
     * Retrieve the properties associated with this session 
     * @return the properties associated with this session 
     */
    public Properties getProperties();
    
    /**
     * Retrieve the end of life time, the time at which this session can be deleted from the persistence store
     * @return the end of life time in milliseconds
     */
    public long getEndOfLife();  
    
    /**
     * Retrieve the end of life time, the time at which this session can be deleted from the persistence store
     * @param endOfLife the end of life time in milliseconds
     */
    public void setEndOfLife(Long endOfLife);
}
