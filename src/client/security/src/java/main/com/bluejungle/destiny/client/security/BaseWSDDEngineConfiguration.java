/*
 * Created on Feb 26, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.security;

import org.apache.axis.ConfigurationException;
import org.apache.axis.Handler;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.utils.Messages;

import javax.xml.namespace.QName;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A Base WSDD Engine Configuration. Most of this is taken from
 * {@see org.apache.axis.configuration.FileProvider}. Why Axis didn't build
 * this class to allow developers to create custom engine configuration
 * implementations, I have no idea
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/security/src/java/main/com/bluejungle/destiny/client/security/BaseWSDDEngineConfiguration.java#1 $
 */

public abstract class BaseWSDDEngineConfiguration implements WSDDEngineConfiguration {

    private WSDDDeployment deployment;

    /**
     * @see org.apache.axis.WSDDEngineConfiguration#getDeployment()
     */
    public WSDDDeployment getDeployment() {
        return this.deployment;
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getHandler(javax.xml.namespace.QName)
     */
    public Handler getHandler(QName qname) throws ConfigurationException {
        return this.getDeployment().getHandler(qname);
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getService(javax.xml.namespace.QName)
     */
    public SOAPService getService(QName qname) throws ConfigurationException {
        SOAPService service = this.getDeployment().getService(qname);
        if (service == null) {
            throw new ConfigurationException(Messages.getMessage("noService10", qname.toString()));
        }
        return service;
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getServiceByNamespaceURI(java.lang.String)
     */
    public SOAPService getServiceByNamespaceURI(String namespace) throws ConfigurationException {
        return this.getDeployment().getServiceByNamespaceURI(namespace);
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getTransport(javax.xml.namespace.QName)
     */
    public Handler getTransport(QName qname) throws ConfigurationException {
        return this.getDeployment().getTransport(qname);
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getTypeMappingRegistry()
     */
    public TypeMappingRegistry getTypeMappingRegistry() throws ConfigurationException {
        return this.getDeployment().getTypeMappingRegistry();
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getGlobalRequest()
     */
    public Handler getGlobalRequest() throws ConfigurationException {
        return this.getDeployment().getGlobalRequest();
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getGlobalResponse()
     */
    public Handler getGlobalResponse() throws ConfigurationException {
        return this.getDeployment().getGlobalResponse();
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getGlobalOptions()
     */
    public Hashtable getGlobalOptions() throws ConfigurationException {
        Hashtable optionsToReturn = null;

        WSDDGlobalConfiguration globalConfig = this.getDeployment().getGlobalConfiguration();

        if (globalConfig != null) {
            optionsToReturn = globalConfig.getParametersTable();
        }

        return optionsToReturn;
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getDeployedServices()
     */
    public Iterator getDeployedServices() throws ConfigurationException {
        return this.getDeployment().getDeployedServices();
    }

    /**
     * @see org.apache.axis.EngineConfiguration#getRoles()
     */
    public List getRoles() {
        return this.getDeployment().getRoles();
    }

    /**
     * Set the WSDD Deployment instance
     * @param deployment the WSDD deployment instance to set
     */
    protected void setDeployment(WSDDDeployment deployment) {
        if (deployment == null) {
            throw new NullPointerException("deployment cannot be null.");
        }
        
        this.deployment = deployment;
    }
}
