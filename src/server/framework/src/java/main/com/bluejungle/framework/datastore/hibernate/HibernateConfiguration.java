/*
 * Created on Feb 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Configuration;

/**
 * This class extends the net.sf.hibernate.cfg.Configuration class to be able to
 * set/retrieve an association between an InputStream and a resource name.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/HibernateConfiguration.java#1 $
 */

public class HibernateConfiguration extends Configuration {

    /*
     * Private variables:
     */
    private Map<String, InputStream> namedResourcesAsInputStreams;

    /**
     * Constructor
     *  
     */
    public HibernateConfiguration() {
        super();
        this.namedResourcesAsInputStreams = new HashMap<String, InputStream>();
    }

    /**
     * Sets a resource and configuration stream association
     * 
     * @param resourceName
     * @param configurationStream
     */
    public void setConfigurationInputStream(String resourceName, InputStream configurationStream) {
        this.namedResourcesAsInputStreams.put(resourceName, configurationStream);
    }

    /**
     * @see net.sf.hibernate.cfg.Configuration#getConfigurationInputStream(java.lang.String)
     */
    protected InputStream getConfigurationInputStream(String resourceName) throws HibernateException {
    	InputStream inputStream = this.namedResourcesAsInputStreams.get(resourceName);
        if (inputStream != null) {
            return inputStream;
        } else {
            return super.getConfigurationInputStream(resourceName);
        }
    }
}