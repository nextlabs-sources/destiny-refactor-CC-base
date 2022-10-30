/*
 * Created on Oct 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * This is the DMS registration manager class. The DMS registration manager
 * registers various DCC components with the DMS. It collects registration
 * requests from DCC component and keeps retrying until the DMS can be reached
 * and the registration request can be processed. The DMS registration manager
 * uses a thread pool to register several DCC component with DMS in parallel.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DMSRegistrationMgrImpl.java#4 $:
 */
public class DMSRegistrationMgrImpl implements IDMSRegistrationMgr, IConfigurable, IDisposable, IInitializable {

    /**
     * Default size for the thread pool
     */
    private static final Integer DEFAULT_THREADPOOL_SIZE = new Integer(3);

    /**
     * Default class name for the worker class
     */
    private static final String DEFAULT_WORKER_CLASSNAME = DMSRegistrationWorkerImpl.class.getName();
    private Log log;
    private IConfiguration config;
    private IThreadPool workerThreadPool;
    private URL dmsLocation;
    private URL dcsfLocation;
    private Integer threadPoolSize;
    private String workerClassName;

    /**
     * Initialization method. This method initializes the thread pool object
     * that takes care of each registration request.
     */
    public void init() {
        HashMapConfiguration config = new HashMapConfiguration();
        HashMapConfiguration workerThreadconfig = new HashMapConfiguration();
        workerThreadconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsLocation);
        config.setProperty(IThreadPool.THREADPOOL_SIZE, this.threadPoolSize);
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, this.workerClassName);
        config.setProperty(IThreadPool.THREADPOOL_CONFIG, workerThreadconfig);
        config.setProperty(IThreadPool.THREADPOOL_NAME, "DMSRegistrationWorkerPool");
        ComponentInfo threadPoolCompInfo = new ComponentInfo("DMSRegistratioMgrThreadPool", ThreadPool.class.getName(), IThreadPool.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        this.workerThreadPool = (IThreadPool) compMgr.getComponent(threadPoolCompInfo);
    }

    /**
     * Cleanup method. This is called when the component is going to be
     * detroyed.
     */
    public void dispose() {
    }

    /**
     * Register a DCC component with the DMS. This function passes the DMS
     * registration request to the thread pool for processing. However, since
     * DCSF is the only application that knows where the DCSF and DMS are
     * located, it is this function's responsibility to add the correct DCSF
     * location in the registration request.
     * 
     * @param regInfo
     *            DCC component registration information
     * @param callback
     *            callback URL for events
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
        regInfo.setEventListenerURL(this.dcsfLocation);
        DMSRegistrationRequest workRequest = new DMSRegistrationRequest(regInfo, callback);
        this.workerThreadPool.doWork(workRequest);
    }

    /**
     * Unregisters a component with DMS. This function passes the unregistration
     * information to the threadpool. The work request specifies that this is an
     * unregistration.
     * 
     * @param unregInfo
     *            unregistration information (this is a partial registration
     *            information filled with component name and type only)
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo unregInfo) {
        DMSRegistrationRequest workRequest = new DMSRegistrationRequest(unregInfo);
        this.workerThreadPool.doWork(workRequest);
    }

    /**
     * Sends a heartbeat to the DMS component service.
     * 
     * @param heartbeat
     *            heartbeat information
     * @return updates for the DCC component
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        IComponentHeartbeatResponse dummyUpate = new ComponentHeartbeatResponseImpl();
        ComponentServiceLocator locator = new ComponentServiceLocator();
        locator.setComponentServiceIFPortEndpointAddress(this.dmsLocation.toString());
        try {
            ComponentServiceIF compService = locator.getComponentServiceIFPort();
            return WebServiceHelper.convertComponentHeartbeatResponse(compService.checkUpdates(WebServiceHelper.convertComponentHeartbeatInfo(heartbeat)));
        } catch (ServiceException e) {
            getLog().debug("Unable to send heartbeat for component '" + heartbeat.getComponentName() + "'", e);
            return dummyUpate;
        } catch (RemoteException e) {
            getLog().debug("Unable to send heartbeat for component '" + heartbeat.getComponentName() + "'", e);
            return dummyUpate;
        }
    }

    /**
     * Sets the configuration for the registration manager
     * 
     * @param conf
     *            configuration object
     */
    public void setConfiguration(IConfiguration conf) {
        this.config = conf;
        this.dmsLocation = (URL) conf.get(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM);
        if (this.dmsLocation == null) {
            throw new IllegalArgumentException("DMS location needs to be specified");
        }
        this.dcsfLocation = (URL) conf.get(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM);
        if (this.dcsfLocation == null) {
            throw new IllegalArgumentException("DCSF location needs to be specified");
        }

        Integer poolSize = (Integer) config.get(IThreadPool.THREADPOOL_SIZE);
        if (poolSize != null) {
            this.threadPoolSize = poolSize;
        } else {
            this.threadPoolSize = DEFAULT_THREADPOOL_SIZE;
        }

        String className = (String) config.get(IThreadPool.WORKER_CLASS_NAME);
        if (className != null) {
            this.workerClassName = className;
        } else {
            this.workerClassName = DEFAULT_WORKER_CLASSNAME;
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
     * Sets the log object
     * 
     * @param newLog
     *            new log object
     */
    public void setLog(Log newLog) {
        log = newLog;
    }

    /**
     * Returns the log for the component
     * 
     * @return the log for the component
     */
    public Log getLog() {
        return log;
    }
}