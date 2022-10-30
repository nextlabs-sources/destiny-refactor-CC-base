/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatInfoImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the implementation of the heartbeat manager. The heartbeat manager
 * runs in its own thread. There is one instance of the heartbeat manager per
 * DCC component. The role of the heartbeat manager is to periodically send a
 * heartbeat to the DMS, and collect updates for the DCC component. The
 * heartbeat manager does not call DMS directly. It goes through the shared
 * context, using the registration manager, to pass requests to DMS.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/HeartbeatMgrImpl.java#1 $:
 */
public class HeartbeatMgrImpl extends Thread implements IHeartbeatMgr {

    private static final Long DEFAULT_RATE = new Long(300000);
    protected Log log;
    private IConfiguration configuration;
    private IDestinyRegistrationManager registrationMgr;
    private Long rate;
    protected String componentName;
    protected ServerComponentType componentType;
    private boolean needRandomRate = false;
    protected IComponentHeartbeatInfo heartbeat;
    protected IDestinySharedContext sharedCtx;
    protected IComponentManager manager;
	private static volatile boolean stopping = false;

    public HeartbeatMgrImpl() {
        super("HeartbeatMgr");
    }
    
    /**
     * Initialization code
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        log.debug("Initializing heartbeat Manager");
        this.rate = DEFAULT_RATE;
        //Check to see if a custom heartbeat rate is mentioned
        Long newRate = (Long) this.configuration.get(IHeartbeatMgr.HEARTBEAT_RATE_CONFIG_PARAM);
        long lrate = newRate.longValue() * 1000;
        //The new rate is given in seconds, convert to it milliseconds
        if (newRate != null) {
            this.rate = new Long(lrate);
        }
        log.trace("Setting heartbeat rate to: " + this.rate);

        //Retrieves the registration manager
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        this.sharedCtx = locator.getSharedContext();
        this.registrationMgr = this.sharedCtx.getRegistrationManager();
        if (this.registrationMgr == null) {
            throw new IllegalStateException("shared context registration manager does not exist");
        }

        // Initialize the heartbeat information:
        this.heartbeat = new ComponentHeartbeatInfoImpl();
        this.heartbeat.setComponentName(this.componentName);
        this.heartbeat.setComponentType(this.componentType);
        makeNextHeartbeatRandom();
        start();
    }

    /**
     * Sets the configuration for the component
     * 
     * @param config
     *            configuration
     * @throws IllegalArgumentException
     *             if COMPONENT_ID_CONFIG_PARAM is missing from the
     *             configuration
     */
    public void setConfiguration(IConfiguration config) {
        this.componentName = (String) config.get(COMPONENT_ID_CONFIG_PARAM);
        if (this.componentName == null) {
            throw new IllegalArgumentException("The component Id needs to be given to the heartbeat manager");
        }

        this.componentType = (ServerComponentType) config.get(COMPONENT_TYPE_CONFIG_PARAM);
        if (this.componentType == null) {
            throw new IllegalArgumentException("The component Type needs to be given to the heartbeat manager");
        }

        this.configuration = config;
    }

    /**
     * Returns the configuration for the component
     * 
     * @return the configuration for the component
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Cleans up the component
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
     public void dispose() {
    	stopping=true;
        this.interrupt();
     }

    /**
     * Starts the heartbeat component. After this method is called, the
     * component starts sending heartbeats on a regular basis.
     */
    public void run() {
        try {
            super.run();
            log.debug("Starting heartbeat Manager ...");
            doMainLoop();
            log.debug("Stopped heartbeat Manager");
        } catch (Throwable e) {
            log.error("Exception occured in the heartbeat mgr thread run() method for component '" + this.componentName + "'", e);
        }
    }

    /**
     * This is the main loop for the heartbeat manager. Until the component is
     * asked to stop, it sends notifications to the registration manager in the
     * shared DCSF context. The call to the DCSF shared context is blocking, so
     * until the heartbeat has been received or has failed, the heartbeat manager
     * thread is blocked.
     */
    protected void doMainLoop() {
        while (!this.isInterrupted()) {
        	synchronized (this) {
        		if (stopping) return;
	    		try {
	    			// Wait for the predetermined interval before sending a
	    			// heartbeat:
	    			this.log.info("Waiting for heartbeat interval to expire for '" + this.componentName + "' ...");
	    			this.wait(getHeartbeatRate());
	    		
	    		} catch (InterruptedException e) {
	    			log.info("Heartbeat Manager for component '" + this.componentName + "' was interrupted. Exiting...");
	    			return;
	    		}  
	    		
        		if (stopping) return;
    			// Perform heartbeat operation and process updates:
    			this.log.info("Sending heartbeat for '" + this.componentName + "' ...");
    			prepareNextHeartbeat(this.heartbeat);
        		if (stopping) return;
    			IComponentHeartbeatResponse update = this.registrationMgr.sendHeartbeat(this.heartbeat);
        		if (stopping) return;
    			processHeartBeatUpdate(update);        	}
    	}
    }

    /**
     * This can be implemented by sub-classes to set additional information on
     * the heartbeat
     * 
     * @param heartbeat
     */
    protected void prepareNextHeartbeat(IComponentHeartbeatInfo heartbeat) {
        return;
    }

    /**
     * Handles the update event that is fired by the DMS to solicit an immediate
     * heartbeat for updates - signals the thread to send another heartbeat, if
     * it is currently waiting/sleeping
     * 
     * @param event
     *            object
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.services.dcsf.types.DestinyEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        synchronized (this) {
            this.log.info("Received '" + event.getName() + "' event.");
            this.notify();
            makeNextHeartbeatRandom();
        }
    }

    /**
     * This function processes the updates given to the component during the
     * heartbeat. By default, the heartbeat manager does not process updates.
     * Only the heartbeat manager for the DCSF component typically processes
     * updates.
     * 
     * @param update
     *            update returned by DMS
     */
    protected void processHeartBeatUpdate(IComponentHeartbeatResponse update) {
        // Do nothing.
    }

    /**
     * Sets the log object for the component
     * 
     * @param newLog
     *            new log object to use
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * This function is called when the next heartbeat needs to happen at a
     * random time. Sometimes, heartbeats need to be random to guarantee that
     * not all components in DCC are going to send their heartbeats exactly at
     * the same time to DMS. This prevents a high load on DMS.
     */
    private void makeNextHeartbeatRandom() {
        this.needRandomRate = true;
    }

    /**
     * Returns the heartbeat rate. If the heartbeat rate should be random this
     * time, then this function perform the calculation of a random heartbeat
     * duration.
     * 
     * @return the current heartbeat rate
     */
    protected int getHeartbeatRate() {
        if (this.needRandomRate) {
            this.needRandomRate = false;
            //Secure Random could be slower than Random. Depends on JVM implementation.
            Random rand = new SecureRandom();
            int newRate = rand.nextInt(this.rate.intValue() + 1);
            if (log.isTraceEnabled()) {
                log.trace("Setting random heartbeat to " + newRate);
            }
            return (newRate);
        } else {
            return this.rate.intValue();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }
}