/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.security.AxisSocketFactory;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.AgentServiceLocator;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;

import com.nextlabs.destiny.interfaces.log.v5.LogServiceIF;
import com.nextlabs.destiny.services.log.v5.LogServiceV5Locator;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CommunicationManager implements ILogEnabled, IInitializable, IConfigurable, IHasComponentInfo<CommunicationManager>, ICommunicationManager {
	public static final PropertyKey<CommProfileDTO> COMM_PROFILE_CONFIG_KEY = 
	        new PropertyKey<CommProfileDTO>("COMM_PROFILE_CONFIG_KEY");

    private Log log = null;
    private IConfiguration config = null;
    private URI dabsLocation = null;
    private boolean isPushEnabled = false;
    private int defaultPort = 0;
    private IPushListener pushListener = null;
    private CommProfileDTO currentCommProfile = null;
    
    String agentServicePortAddress;
    String logServicePortAddress;

    private static final ComponentInfo<CommunicationManager> COMP_INFO =
			new ComponentInfo<CommunicationManager>(
					NAME, 
					CommunicationManager.class,
					LifestyleType.SINGLETON_TYPE);
	public static final String AGENT_SERVICE_SUFFIX = "/services/AgentServiceIFPort";
    public static final String LOG_SERVICE_SUFFIX = "/services/LogServiceIFPort.v5";
    private static final String SOCKET_FACTORY_PROPERTY = "axis.socketSecureFactory";

    protected static final int REQUEST_TIMEOUT = Integer.parseInt(System.getProperty("com.bluejungle.agent.sockettimeout", "1800000"));  
    protected static final int LOG_REQUEST_TIMEOUT = Integer.parseInt(System.getProperty("com.bluejungle.agent.sockettimeout", "120000"));  

    /**
     * The Communication Manager is a singleton. It will be instantiated when
     * the agent starts up. Components that need it can get it from the
     * component manager.
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<CommunicationManager> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * Initializes the service locators and gets the service interfaces from
     * them.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.log.info("Communication Manager Initialized.");
        System.setProperty(SOCKET_FACTORY_PROPERTY, AxisSocketFactory.class.getName());

        CommProfileDTO commProfile = getCommunicationProfile();
        this.dabsLocation = commProfile.getDABSLocation();
        
        

        this.setupWebServiceInterfaces();
        this.setupPushListener(commProfile);
    }

    /**
     * Reinitialize the Communication Manager. Reset service interfaces if DABS
     * location has changed. Reset push listener if configuration has changed.
     */
    public void reinit() {
        this.log.info("Communication Manager Reinitialized.");

        CommProfileDTO commProfile = getCommunicationProfile();
        if (!this.dabsLocation.equals(commProfile.getDABSLocation())) {
            this.dabsLocation = commProfile.getDABSLocation();
            setupWebServiceInterfaces();
        }

        boolean pushEnabled = commProfile.isPushEnabled();
        if (!pushEnabled) {
            if (this.isPushEnabled) {
                this.pushListener.stop();
                this.isPushEnabled = false;
            }
        } else if (this.isPushEnabled && this.defaultPort != commProfile.getDefaultPushPort().intValue()) {
            this.pushListener.stop();
            this.setupPushListener(commProfile);
        } else { //if pushInfo != null and !isPushEnabled
            this.setupPushListener(commProfile);
        }
    }

    /**
     * Gets the Communication Profile from the control module and returns it.
     * 
     * @return the communication profile.
     */
    private CommProfileDTO getCommunicationProfile() {
        return this.currentCommProfile;
    }

    /**
     * Sets up web service interfaces based on dabs location.
     */
    private void setupWebServiceInterfaces() {
        this.agentServicePortAddress = this.dabsLocation.toString() + AGENT_SERVICE_SUFFIX;
        this.logServicePortAddress = this.dabsLocation.toString() + LOG_SERVICE_SUFFIX;        
    }

    /**
     * Set up the push listener
     * 
     * @param commProfile
     *            communication profile
     */
    private void setupPushListener(CommProfileDTO commProfile) {
        this.isPushEnabled = commProfile.isPushEnabled();
        if (this.isPushEnabled) {
            this.defaultPort = commProfile.getDefaultPushPort().intValue();
            ComponentInfo<PushListener> pushListenerInfo = new ComponentInfo<PushListener>(PushListener.class, LifestyleType.SINGLETON_TYPE);
            this.pushListener = ComponentManagerFactory.getComponentManager().getComponent(pushListenerInfo, this.config);
        }
    }

    /**
     * @return port number that push listener is waiting on.
     */
    public int getPort() {
        if (this.isPushEnabled && this.pushListener != null) {
            return (this.pushListener.getPort());
        } else {
            return -1;
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Returns the agentServiceIF.
     * 
     * @return the agentServiceIF.
     * @throws ServiceException
     */
    public AgentServiceIF getAgentServiceIF() throws ServiceException {
        AgentServiceLocator agentServiceLocator = this.getAgentServiceLocator();
        agentServiceLocator.setAgentServiceIFPortEndpointAddress(this.agentServicePortAddress);
        
        AgentServiceIF agentServiceIF = agentServiceLocator.getAgentServiceIFPort();
        ((Stub) agentServiceIF).setTimeout(REQUEST_TIMEOUT); 
        return agentServiceIF;
    }

    /**
     * @return
     */
    protected AgentServiceLocator getAgentServiceLocator() {
        return new AgentServiceLocator();
    }

    /**
     * Returns the logServiceIF.
     * 
     * @return the logServiceIF.
     * @throws ServiceException
     */
    public LogServiceIF getLogServiceIF() throws ServiceException {
        LogServiceV5Locator logServiceLocator = this.getLogServiceLocator();
        logServiceLocator.setLogServiceIFPortV5EndpointAddress(this.logServicePortAddress);
        LogServiceIF logServiceIF = logServiceLocator.getLogServiceIFPortV5();
        ((Stub) logServiceIF).setTimeout(LOG_REQUEST_TIMEOUT);
        return logServiceIF;
    }

    /**
     * @return
     */
    protected LogServiceV5Locator getLogServiceLocator() {
        return new LogServiceV5Locator();
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
        this.currentCommProfile = config.get(COMM_PROFILE_CONFIG_KEY);

        if (this.currentCommProfile == null) {
            // the currentCommProfile is required, otherwise it will fail on init or reinit
            throw new NullPointerException("The communication profile was not provided");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }
}
