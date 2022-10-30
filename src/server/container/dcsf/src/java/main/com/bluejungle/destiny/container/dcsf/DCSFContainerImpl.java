/*
 * Created on Oct 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.bluejungle.destiny.container.dcc.DefaultContainerImpl;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dcc.IHeartbeatMgr;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationInfoImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;

/**
 * This is the DCSF container class. The DCSF container extends the default
 * container. The DCSF registration process with DMS is special. It does not use
 * the underlying registration manager in the Destiny shared context, but calls
 * DMS directly through a temporary registration manager at the web application
 * level. Once the DCSF has registered successfully with DMS, then it registers
 * locally in the Destiny shared context and can start performing work for other
 * DCC components.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DCSFContainerImpl.java#8 $:
 */
public class DCSFContainerImpl extends DefaultContainerImpl implements IDCCContainer {

    private static final Integer DEFAULT_THREADPOOL_SIZE = new Integer(3);
    private static final String DEFAULT_REGISTRATION_MGR_CLASS_NAME = DMSRegistrationMgrImpl.class.getName();
    private IDMSRegistrationMgr dmsRegistrationMgr;
    private String dcsfComponentName;
    private URL dcsfURL;
    private URL dmsURL;
    private IVersion dcsfVersion;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.dmsRegistrationMgr = null;
        this.dcsfComponentName = null;
        this.dcsfURL = null;
        this.dmsURL = null;
        this.dcsfVersion = null;
    }

    /**
     * Initialization function. Extra objects are initialized for the DCSF
     * container.
     */
    protected void doInit() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IConfiguration config = getConfiguration();
        this.dcsfComponentName = config.get(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM);

        //Finds the location
        String location = config.get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM);
        //Finds the DMS location
        IConfiguration compConfig = config.get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME);
        String dmsLocation = compConfig.get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);

        String regMgrClassName = (String) getConfiguration().get(IRegisteredDCSFComponent.REGISTRATION_MGR_CLASS_NAME);
        if (regMgrClassName == null) {
            regMgrClassName = DEFAULT_REGISTRATION_MGR_CLASS_NAME;
        }

        try {
            this.dcsfURL = new URL(location + "/services/DCSFServiceIFPort");
            this.dmsURL = new URL(dmsLocation + "/services/ComponentServiceIFPort");
        } catch (MalformedURLException e) {
            getLog().fatal("Bad URL provided for DCSF or DMS location in component " + this.dcsfComponentName + " configuration");
            throw new RuntimeException(e);
        }
        
        try {
            this.dcsfVersion = (new VersionFactory()).getVersion();
        } catch (IOException e){
            getLog().fatal("Version file read error in component " + this.dcsfComponentName);
            throw new RuntimeException(e);
        } catch (InvalidVersionException e){
            getLog().fatal("Invalid version in component " + this.dcsfComponentName);
            throw new RuntimeException(e);
        }
        
        //Initializes the registration manager
        HashMapConfiguration regMgrConfig = new HashMapConfiguration();
        regMgrConfig.setProperty(IThreadPool.THREADPOOL_SIZE, DEFAULT_THREADPOOL_SIZE);
        regMgrConfig.setProperty(IThreadPool.WORKER_CLASS_NAME, DMSRegistrationWorkerImpl.class.getName());
        regMgrConfig.setProperty(IDMSRegistrationMgr.DMS_LOCATION_CONFIG_PARAM, this.dmsURL);
        regMgrConfig.setProperty(IDMSRegistrationMgr.DCSF_LOCATION_CONFIG_PARAM, this.dcsfURL);
        ComponentInfo<IDMSRegistrationMgr> regMgrCompInfo = 
            new ComponentInfo<IDMSRegistrationMgr>(
                IDMSRegistrationMgr.COMP_NAME, 
                regMgrClassName, 
                IDMSRegistrationMgr.class.getName(), 
                LifestyleType.SINGLETON_TYPE, 
                regMgrConfig);
        this.dmsRegistrationMgr = compMgr.getComponent(regMgrCompInfo);
    }

    /**
     * Returns the class name to be used for the heartbeat manager. The DCSF
     * heartbeat manager is a special implementation.
     * 
     * @return the class name to be used for the heartbeat manager.
     */
    protected Class<? extends IHeartbeatMgr> getHeartbeatMgrClassName() {
        return DCSFHeartBeatMgrImpl.class;
    }

    /**
     * Register the DCSF component with DMS. For DCSF, the registration is done
     * directly from the web application, and does not go through the shared
     * context, because the DCSF web application is not yet registered with the
     * DCSF shared context.
     */
    protected void registerWithDMS() {
        //Prepares the registration information
        DCCRegistrationInfoImpl registrationInfo = new DCCRegistrationInfoImpl();
        registrationInfo.setComponentName(this.dcsfComponentName);
        registrationInfo.setComponentType(ServerComponentType.DCSF);
        registrationInfo.setComponentTypeDisplayName("Communication Server");
        registrationInfo.setEventListenerURL(this.dcsfURL);
        registrationInfo.setComponentURL(this.dcsfURL);
        registrationInfo.setComponentVersion(this.dcsfVersion);

        //Performs the registration
        this.dmsRegistrationMgr.registerComponentWithDMS(registrationInfo, getComponentStarter());
    }
}
