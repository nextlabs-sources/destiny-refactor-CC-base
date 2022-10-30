/*
 * Created on Jan 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.environment.webapp;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.environment.IResourceLocator;

/**
 * The Web application resource locator class allows retrieving resourcing
 * contained within a web application.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/environment/webapp/WebAppResourceLocatorImpl.java#1 $
 */

public class WebAppResourceLocatorImpl implements IResourceLocator {
    /*
     * Configuration parameters
     */
    public static final String CONTAINER_CTX_CONFIG_PARAM = "ContainerContext";

    /*
     * Private variables:
     */
    protected IConfiguration config;
    protected Log log;
    protected ServletContext containerCtx;

    /**
     * This method returns an input stream to the file described in the path
     * 
     * @param relativePath
     *            relative path to the file within the application
     * @return input stream to the file (if it exists), null otherwise
     */
    public InputStream getResourceAsStream(String relativePath) {
        if (relativePath != null && !relativePath.startsWith("/")) {
            log.warn("relativePath should start with '/', see apache bug 43241");
        }
        InputStream stream = this.containerCtx.getResourceAsStream(relativePath);
        if (stream!=null) {
            if (this.log.isTraceEnabled()) {
                this.log.trace("Constructed web app resource stream for (" + relativePath + ")");
            }
        }
        else {
            this.log.error("Requested web app resource - (" + relativePath + ") was not found");
        }
        return stream;
    }
    
    /**
     * @see com.bluejungle.framework.environment.IResourceLocator#exists(java.lang.String)
     */
    public boolean exists(String relativePath) {
        if (relativePath != null && !relativePath.startsWith("/")) {
            log.warn("relativePath should start with '/', see apache bug 43241");
        }
        return (this.containerCtx.getResourceAsStream(relativePath)!=null);
    }

    /**
     * Initialization function. To function properly, this component needs a
     * container context properly set in the configuratio object.
     */
    public void init() {
        Object ctx = this.config.get(WebAppResourceLocatorImpl.CONTAINER_CTX_CONFIG_PARAM);
        if (ctx == null || !(ctx instanceof ServletContext)) {
            throw new IllegalArgumentException("The WebApp Resource locator needs a container context in its configuration.");
        }

        //Sets the servlet context
        this.containerCtx = (ServletContext) ctx;
    }

    /**
     * This method is called before the component gets destroyed.
     */
    public void dispose() {
        this.config = null;
        this.log = null;
    }

    /**
     * Sets the configuration object
     * 
     * @param newConfig
     *            new configuration object
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.config = newConfig;
    }

    /**
     * Returns the configuration for the component
     * 
     * @return the configuration for the component
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Set the log object for the component
     * 
     * @param newLog
     *            new log object
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * Returns the log object for the component
     * 
     * @return the log object for the component
     */
    public Log getLog() {
        return this.log;
    }
}