/*
 * Created on Nov 30, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.WorkerThread;

/**
 * This is the registration manager for remote events. It calls the DMS
 * component service and registers for event that can be fired from outside of
 * the current JVM instance. This component is used only when the installation
 * of DCC is made on multiple JVM instances. The event registration manager
 * syncs all the event registration / unregistration requests, and has only one
 * thread calling the DMS component service. This approach allows to handle web
 * service issues (e.g. the DMS web service is not available) and guarantees
 * that registration and unregistration requests are not lost.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/RemoteEventRegistrationMgrImpl.java#1 $:
 */

public class RemoteEventRegistrationMgrImpl implements IRemoteEventRegistrationMgr {

    /*
     * Log related constants:
     */
    private static final MessageFormat INITIALIZATION_MSG = new MessageFormat("Initialized instance of '" + RemoteEventRegistrationMgrImpl.class.getName() + "' with dcsfLocation: ''{0}'', dmsLocation: ''{1}'', workerClassName: ''{2}''");
    private static final MessageFormat REGISTERING_FOR_REMOTE_EVENT_MSG = new MessageFormat("Registering for remote event: ''{0}''");
    private static final MessageFormat UNREGISTERING_FOR_REMOTE_EVENT_MSG = new MessageFormat("Unregistering for remote event: ''{0}''");

    private IConfiguration config;
    private Log log;
    private URI dmsLocation;
    private URI dcsfLocation;
    private String workerClassName;
    private Set<String> unregisterEventRequests;
    private Set<String> registerEventRequests;
    private IRemoteEventRegistrationWorker workerThread;

    /**
     * Constructor Sets the default values
     */
    public RemoteEventRegistrationMgrImpl() {
        super();
        this.workerClassName = RemoteEventRegistrationWorkerImpl.class.getName();
    }

    /**
     * Initialization method. This method initializes the thread pool object
     * that takes care of each registration request.
     */
    public void init() {
        this.unregisterEventRequests = new LinkedHashSet<String>();
        this.registerEventRequests = new LinkedHashSet<String>();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        config.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfLocation);
        config.setProperty(WorkerThread.WORKER_NAME, "RemoveEventRegistrationWorker");
        ComponentInfo<IRemoteEventRegistrationWorker> workerThreadCompInfo = 
            new ComponentInfo<IRemoteEventRegistrationWorker>(
                "RemoteEventRegistrationWorkerThread", 
                this.workerClassName, 
                IRemoteEventRegistrationWorker.class.getName(), 
                LifestyleType.SINGLETON_TYPE, 
                config
            );
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        workerThread = compMgr.getComponent(workerThreadCompInfo);

        if (getLog().isDebugEnabled()) {
            getLog().debug(INITIALIZATION_MSG.format(new Object[] { this.dcsfLocation, this.dmsLocation, this.workerClassName }));
        }
    }

    /**
     * Cleanup method. This is called when the component is going to be
     * detroyed.
     */
    public void dispose() {
        workerThread.dispose();
        workerThread = null;
        dmsLocation = null;
    }

    /**
     * Sets the configuration for the registration manager
     * 
     * @param conf
     *            configuration object
     */
    public void setConfiguration(IConfiguration conf) {
        this.config = conf;
        this.dmsLocation = (URI) conf.get(IRemoteEventRegistrationMgr.DMS_LOCATION_CONF_PARAM);
        if (this.dmsLocation == null) {
            throw new IllegalArgumentException("DMS location needs to be specified");
        }
        this.dcsfLocation = (URI) conf.get(IRemoteEventRegistrationMgr.DCSF_LOCATION_CONF_PARAM);
        if (this.dcsfLocation == null) {
            throw new IllegalArgumentException("DCSF location needs to be specified");
        }

        String className = (String) config.get(IThreadPool.WORKER_CLASS_NAME);
        if (className != null) {
            this.workerClassName = className;
        }
    }

    /**
     * Returns the registration manager configuration
     * 
     * @return the registration manager configuration
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Register for a remote event fired on a separate JVM instance. This
     * function is called when the local JVM instance wants to be notified of a
     * particular event (there is one local listener for this event, so we need
     * to receive notifications for this event).
     * 
     * Only one event registration / unregistration can be processed by the
     * worker thread at a time, in order to keep carefully track of which event
     * were really registered / unregistered. Therefore, adding to the queues is
     * possible only when the worker thread is not working on them (the worker
     * thread will lock these queues when trying to contact DMS).
     * 
     * @param eventName
     *            name of the event to register for
     */
    public void registerForRemoteEvent(String eventName) {
        synchronized (this.registerEventRequests) {
            //If that event is still waiting to be unregistered, take if off
            // the
            // unregistration queue as well. This should only happen if the
            // previous unregistration request has not been processed for some
            // reason.

            if (getLog().isDebugEnabled()) {
                getLog().debug(REGISTERING_FOR_REMOTE_EVENT_MSG.format(new Object[] { eventName }));
            }

            if (this.unregisterEventRequests.contains(eventName)) {
                this.unregisterEventRequests.remove(eventName);
            } else {
                this.registerEventRequests.add(eventName);
            }
        }
        //Queues have been updated, notify the worker thread to process them
        notifyNewRequest();
    }

    /**
     * Unregister for a remote event fired on a separate JVM instance. This
     * function is called when the local JVM instance no longer wants to be
     * notified of a particular event (all local listeners are gone, so there is
     * no need to receiving notifications).
     * 
     * Only one event registration / unregistration can be processed by the
     * worker thread at a time, in order to keep carefully track of which event
     * were really registered / unregistered. Therefore, adding to the queues is
     * possible only when the worker thread is not working on them (the worker
     * thread will lock these queues when trying to contact DMS).
     * 
     * @param eventName
     *            name of the event to register for
     */
    public void unregisterForRemoteEvent(String eventName) {
        synchronized (this.registerEventRequests) {
            //If that event is still waiting to be registered, take if off the
            // registration queue as well. This should only happen if the
            // previous registration request has not been processed for some
            // reason.

            if (getLog().isDebugEnabled()) {
                getLog().debug(UNREGISTERING_FOR_REMOTE_EVENT_MSG.format(new Object[] { eventName }));
            }

            if (this.registerEventRequests.contains(eventName)) {
                this.registerEventRequests.remove(eventName);
            } else {
                this.unregisterEventRequests.add(eventName);
            }
        }
        
        //Queues have been updated, notify the worker thread to process them
        notifyNewRequest();
    }

    /**
     * This function notifies the worker thread to process the event
     * registration/unregistration request
     */
    private void notifyNewRequest() {
        workerThread.processRequests(this.registerEventRequests, this.unregisterEventRequests);
    }

    /**
     * Sets the log object
     * 
     * @param newLog
     *            log object
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
        return (this.log);
    }
}
