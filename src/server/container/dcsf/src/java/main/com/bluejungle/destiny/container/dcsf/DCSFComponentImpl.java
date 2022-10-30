/*
 * Created on Oct 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;

import org.apache.axis.types.URI;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;

/**
 * This is the DCSF component class. This component creates the various
 * sub-components running within the DCSF process. DCSF is the only DCC
 * component aware of the DMS location. The main job of the DCSF container is to
 * register / unregister components with DMS and register / unregister events
 * with remote JVM instance (only when the installation is distributed).
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DCSFContainerImpl.java#8 $:
 */
public class DCSFComponentImpl extends BaseDCCComponentImpl implements IRegisteredDCSFComponent {

    private static final String DCSF_SERVICE_PORT = "/services/DCSFServiceIFPort";
    private static final String COMPONENT_SERVICE_PORT = "/services/ComponentServiceIFPort";
    private static final Integer DEFAULT_THREADPOOL_SIZE = new Integer(3);

    private IDMSRegistrationMgr registrationMgr;
    private IRemoteEventRegistrationMgr eventRegistrationMgr;
    private IRemoteEventDispatchMgr eventDispatchMgr;

    private URI dmsEventServiceLocation;
    private URI dcsfEventServiceLocation;
    private String regMgrClassName = DMSRegistrationMgrImpl.class.getName();
    private String eventRegistrationMgrClassName = RemoteEventRegistrationMgrImpl.class.getName();
    private String eventDispatchMgrClassName = RemoteEventDispatchMgrImpl.class.getName();

    /**
     * This method is called to destroy the DCSF container. The DCSF container
     * does all the necessary cleanup.
     */
    public void dispose() {
        super.dispose();
    }

    /**
     * DCSF container initialization. This function initializes the DCSF
     * container, and all other components running within it.
     */
    public void init() {
        setComponentType(ServerComponentType.DCSF);

        //Retrieve the DCSF service location
        //e.g. http://localhost:8080/DMSService
        // => http://localhost:8080/DMSService/services/DMSDCCServiceIFPort
        String location = (String) getConfiguration().get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM);
        String dmsLocation = (String) getConfiguration().get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);

        try {
            this.dcsfEventServiceLocation = new URI(location + DCSF_SERVICE_PORT);
            this.dmsEventServiceLocation = new URI(dmsLocation + COMPONENT_SERVICE_PORT);
        } catch (URI.MalformedURIException e) {
            final String errMsg = "Wrong URL for DMS and DCC component URL given to DCSF";
            getLog().fatal(errMsg, e);
            throw new IllegalArgumentException(errMsg);
        }

        //Initializes the registration manager
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, DEFAULT_THREADPOOL_SIZE);
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, DMSRegistrationWorkerImpl.class.getName());
        config.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsEventServiceLocation);
        config.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfEventServiceLocation);
        ComponentInfo regMgrCompInfo = new ComponentInfo(IDMSRegistrationMgr.COMP_NAME, this.regMgrClassName, IDMSRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, config);
        this.registrationMgr = (IDMSRegistrationMgr) getManager().getComponent(regMgrCompInfo);

        //Initializes the event registration manager
        HashMapConfiguration eventMgrconfig = new HashMapConfiguration();
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsEventServiceLocation);
        eventMgrconfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfEventServiceLocation);
        eventMgrconfig.setProperty(IThreadPool.WORKER_CLASS_NAME, RemoteEventRegistrationWorkerImpl.class.getName());
        ComponentInfo evtRegMgrCompInfo = new ComponentInfo(IRemoteEventRegistrationMgr.COMP_NAME, this.eventRegistrationMgrClassName, IRemoteEventRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventMgrconfig);
        this.eventRegistrationMgr = (IRemoteEventRegistrationMgr) getManager().getComponent(evtRegMgrCompInfo);

        //Initializes the remote event dispatch mananager
        HashMapConfiguration eventDispatchMgrConfig = new HashMapConfiguration();
        eventDispatchMgrConfig.setProperty(IThreadPool.WORKER_CLASS_NAME, RemoteEventDispatchWorkerImpl.class.getName());
        ComponentInfo evtDispatchMgrCompInfo = new ComponentInfo(IRemoteEventDispatchMgr.COMP_NAME, this.eventDispatchMgrClassName, IRemoteEventDispatchMgr.class.getName(), LifestyleType.SINGLETON_TYPE, eventDispatchMgrConfig);
        this.eventDispatchMgr = (IRemoteEventDispatchMgr) getManager().getComponent(evtDispatchMgrCompInfo);

        //Initializes the remote listener registration manager
        HashMapConfiguration remoteListenerRegMgrConfig = new HashMapConfiguration();
        remoteListenerRegMgrConfig.setProperty(IRemoteListenerRegistrationMgr.PARENT_DCSF_COMPONENT_CONFIG_PARAM, this);
        ComponentInfo remoteListenerRegMgrCompInfo = new ComponentInfo(IRemoteListenerRegistrationMgr.COMP_NAME, RemoteListenerRegistrationMgrImpl.class.getName(), IRemoteListenerRegistrationMgr.class.getName(), LifestyleType.SINGLETON_TYPE,
                remoteListenerRegMgrConfig);
        getManager().getComponent(remoteListenerRegMgrCompInfo);

        //Now, proceed with super class initialization
        super.init();
    }

    /**
     * Register a DCC component with the DMS. The DCSF container delegates this
     * task to the DMS registration manager
     * 
     * @param regInfo
     *            DCC component registration information
     * @param callback
     *            callback URL for events
     */
    public void registerComponentWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
        this.registrationMgr.registerComponentWithDMS(regInfo, callback);
    }

    /**
     * Unregisters a given component with DMS. For now, the DMS is not notified
     * if a component is unregistered. DMS will anyway figure it out once the
     * hearbeat from the component stops coming. If there is a need to actually
     * notify, this will be DCSF responsibility.
     * 
     * @param info
     *            partially filled registration info (name and type)
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#unregisterComponentWithDMS()
     */
    public void unregisterComponentWithDMS(IDCCRegistrationInfo info) {
    }

    /**
     * Register for an event fired in a separate JVM instance. The component
     * service is invoked on DMS to perform the registration.
     * 
     * @param eventName
     *            name of the event to register for.
     */
    public void registerForRemoteEvent(String eventName) {
        this.eventRegistrationMgr.registerForRemoteEvent(eventName);
    }

    /**
     * Sends a heartbeat request to DMS
     * 
     * @param heartbeat
     *            hearbeat information
     * @return heartbeat update
     * @see com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent#sendHeartbeat(com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeat) {
        return (this.registrationMgr.sendHeartbeat(heartbeat));
    }

    /**
     * Unregister for an event fired from a remote instance (separate JVM). The
     * unregistration request needs to be sent to DMS.
     * 
     * @param eventName
     *            name of the event to unregister for
     */
    public void unregisterForRemoteEvent(String eventName) {
        this.eventRegistrationMgr.unregisterForRemoteEvent(eventName);
    }

    /**
     * Fire an event to a remote instance (separate JVM).
     * 
     * @param event
     *            event object to be fired
     * @param remoteLocation
     *            location of the remote listener DCSF service
     */
    public void fireRemoteEvent(IDCCServerEvent event, URL remoteLocation) {
        this.eventDispatchMgr.fireEvent(event, remoteLocation);
    }

    /**
     * Sets the container configuration. In this class, the configuration is not
     * saved. It is used immediately to change default values for some member
     * variables.
     * 
     * @param conf
     *            configuration object
     */
    public void setConfiguration(IConfiguration conf) {
        super.setConfiguration(conf);
        if (conf != null) {
            String newRegMgrClassName = (String) conf.get(REGISTRATION_MGR_CLASS_NAME);
            if (newRegMgrClassName != null) {
                this.regMgrClassName = newRegMgrClassName;
            }

            String newEventRegMgrClassName = (String) conf.get(EVENT_REGISTRATION_MGR_CLASS_NAME);
            if (newEventRegMgrClassName != null) {
                this.eventRegistrationMgrClassName = newEventRegMgrClassName;
            }
        }
    }
}
