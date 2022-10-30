/*
 * Created on Dec 3, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;

import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.framework.comp.IConfiguration;

/**
 * This is the worker class that registers / unregisters events with the DMS.
 * Only one thread talks to the DMS component service at a time. Once the DMS
 * confirmed that the event has been registered or unregistred, the event name
 * can be taken off the registration / unregistration list.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/RemoteEventRegistrationWorkerImpl.java#1 $:
 */

public class RemoteEventRegistrationWorkerImpl extends Thread implements IRemoteEventRegistrationWorker {

    /*
     * Log related constants:
     */
    private static final MessageFormat CONFIGURATION_MSG = new MessageFormat("Configured instance of '" + RemoteEventRegistrationWorkerImpl.class.getName() + "' with dcsfLocation: ''{0}'', dmsLocation: ''{1}''");
    private static final MessageFormat PREPARING_TO_START_WORK_MSG = new MessageFormat("Preparing to register for ''{0}'' event(s) and unregister for ''{1}'' event(s)");
    private static final MessageFormat REGISTERING_MSG = new MessageFormat("Registering for event: ''{0}'' with callback: ''{1}'' on dms: ''{2}''.");
    private static final MessageFormat REGISTERED_MSG = new MessageFormat("Succesfully registered for event: ''{0}'' with callback: ''{1}'' on dms: ''{2}''");
    private static final MessageFormat REGISTRATION_ERROR_MSG = new MessageFormat("Error when registering for event: ''{0}'' with callback: ''{1}'' on dms: ''{2}''");
    private static final MessageFormat UNREGISTERING_MSG = new MessageFormat("Un-registering event: ''{0}'' with callback: ''{1}'' from dms: ''{2}''.");
    private static final MessageFormat UNREGISTERED_MSG = new MessageFormat("Succesfully un-registered for event: ''{0}'' with callback: ''{1}'' from dms: ''{2}''");
    private static final MessageFormat UNREGISTRATION_ERROR_MSG = new MessageFormat("Error when un-registering event: ''{0}'' with callback: ''{1}'' from dms: ''{2}''");
    private static final String SLEEPING_MSG = "Remote event registration worker is sleeping/waiting for tasks ...";
    private static final String WOKE_UP_MSG = "Remote event registration worker was woken up to perform tasks ...";

    private final Object internalLock = new Object();
    protected static Log log;
    private URI dmsLocation;
    private URI dcsfLocation;
    private IConfiguration config;
    private ComponentServiceIF componentService;
    private Set<String> registrationSet;
    private Set<String> unregistrationSet;

    public RemoteEventRegistrationWorkerImpl() {
        super("RemoteEventRegistrationWorker");
    }

    /**
     * This is called when the thread is started
     */
    public void run() {
        try {
            super.run();
            mainLoop();
        } catch (Throwable e) {
            if(log != null){
                log.error("Exception occured in '" + RemoteEventRegistrationWorkerImpl.class.getName() + "' thread run() method representing DCSF at location '" + this.dcsfLocation + "'", e);
            }
        }
    }

    /**
     * This method waits for the notification to come. Then it processes the
     * registration or unregistration and goes back to wait
     */
    private void mainLoop() {
        while (!this.isInterrupted()) {
            try {
                synchronized (this.internalLock) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug(SLEEPING_MSG);
                    }
                    internalLock.wait();

                    if (getLog().isDebugEnabled()) {
                        getLog().debug(WOKE_UP_MSG);
                    }
                }
            } catch (InterruptedException interruptedEx) {
                log.trace("Remote event registration worker stopped");
                return;
                //Thread is interrupted, no more event processing occurs.
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug(
                        PREPARING_TO_START_WORK_MSG.format(new Object[] { this.registrationSet != null ? new Integer(this.registrationSet.size()) : new Integer(0),
                                this.unregistrationSet != null ? new Integer(this.unregistrationSet.size()) : new Integer(0) }));
            }

            synchronized (this.registrationSet) {
                boolean continueWork = true;
                //pick the registration requests first
                Iterator it = this.registrationSet.iterator();
                Set throwAwayEvents = new HashSet();
                while (it.hasNext() && continueWork) {
                    String eventToRegister = (String) it.next();
                    try {
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(REGISTERING_MSG.format(new Object[] { eventToRegister, this.dcsfLocation, this.dmsLocation }));
                        }
                        this.componentService.registerEvent(eventToRegister, this.dcsfLocation);
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(REGISTERED_MSG.format(new Object[] { eventToRegister, this.dcsfLocation, this.dmsLocation }));
                        }
                        throwAwayEvents.add(eventToRegister);
                    } catch (RemoteException remoteEx) {
                        //The call to the remote service has failed. Keep the
                        // list of requests and retry later. The items that have
                        // already been processed can be deleted.
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(REGISTRATION_ERROR_MSG.format(new Object[] { eventToRegister, this.dcsfLocation, this.dmsLocation }));
                        }
                        continueWork = false;
                    }
                }

                Iterator throwAwayEventsIt = throwAwayEvents.iterator();
                while (throwAwayEventsIt.hasNext()) {
                    String eventName = (String) throwAwayEventsIt.next();
                    this.registrationSet.remove(eventName);
                }

                //Then, process the unregistration requests
                it = this.unregistrationSet.iterator();
                throwAwayEvents = new HashSet();
                while (it.hasNext() && continueWork) {
                    String eventToUnregister = (String) it.next();
                    try {
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(UNREGISTERING_MSG.format(new Object[] { eventToUnregister, this.dcsfLocation, this.dmsLocation }));
                        }
                        this.componentService.unregisterEvent(eventToUnregister, this.dcsfLocation);
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(UNREGISTERED_MSG.format(new Object[] { eventToUnregister, this.dcsfLocation, this.dmsLocation }));
                        }
                        throwAwayEvents.add(eventToUnregister);
                    } catch (RemoteException remoteEx) {
                        //The call to the remote service has failed. Keep the
                        // list
                        // of requests and retry later. The items that have
                        // already
                        // been processed can be deleted.
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(UNREGISTRATION_ERROR_MSG.format(new Object[] { eventToUnregister, this.dcsfLocation, this.dmsLocation }));
                        }
                        continueWork = false;
                    }
                }

                throwAwayEventsIt = throwAwayEvents.iterator();
                while (throwAwayEventsIt.hasNext()) {
                    String eventName = (String) throwAwayEventsIt.next();
                    this.unregistrationSet.remove(eventName);
                }
            } //synchronized
        }
    }

    /**
     * Sets the configuration
     * 
     * @param configuration
     *            configuration object
     */
    public void setConfiguration(IConfiguration configuration) {
        this.config = configuration;
        this.dmsLocation = (URI) configuration.get(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM);
        this.dcsfLocation = (URI) configuration.get(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM);

        if (getLog().isDebugEnabled()) {
            getLog().debug(CONFIGURATION_MSG.format(new Object[] { this.dcsfLocation, this.dmsLocation }));
        }
    }

    /**
     * Returns the configuration for the component
     * 
     * @return the configuration for the component
     */
    public IConfiguration getConfiguration() {
        return (this.config);
    }

    /**
     * Cleanup function
     */
    public void dispose() {
        this.interrupt();
        this.config = null;
        this.dcsfLocation = null;
        this.dmsLocation = null;
    }

    /**
     * Initialization function. This function starts the thread. The thread
     * starts waiting for requests.
     */
    public void init() {
        ComponentServiceLocator locator = getComponentServiceLocator();
        locator.setComponentServiceIFPortEndpointAddress(this.dmsLocation.toString());
        try {
            this.componentService = locator.getComponentServiceIFPort();
            this.setDaemon(true);
            this.start();
        } catch (ServiceException ex) {
            log.fatal("Unable to initialize the Remote Event Registration Worker", ex);
        }
    }

    /**
     * This method starts the event registration / unregistration process. The
     * thread picks the event names in the sets and calls the DMS component
     * service. If the call fails, the list of events to register / unregister
     * remains the same.
     * 
     * @param registrationList
     *            list of events to register
     * @param unRegistrationList
     *            list of events to unregister
     */
    public void processRequests(Set<String> registrationList, Set<String> unRegistrationList) {
        synchronized (registrationList) {
            this.registrationSet = registrationList;
            this.unregistrationSet = unRegistrationList;
            synchronized (this.internalLock) {
                this.internalLock.notify();
            }
        }
    }

    /**
     * This method is implemented to help the JUNIT test. It returns the
     * Component Service locator
     * 
     * @return the component service locator
     */
    protected ComponentServiceLocator getComponentServiceLocator() {
        return new ComponentServiceLocator();
    }

    /**
     * Sets the log object for the component
     * 
     * @param newLog
     *            new log object
     */
    public void setLog(Log newLog) {
        log = newLog;
    }

    /**
     * Returns the log object for the component
     * 
     * @return the log object for the component
     */
    public Log getLog() {
        return log;
    }

}