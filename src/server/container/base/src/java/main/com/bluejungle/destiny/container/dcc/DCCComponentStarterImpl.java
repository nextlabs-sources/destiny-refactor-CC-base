/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the DCC component starter class. This object listens to the DMS
 * registration answer and instantiates the DCC component if the registration
 * was successful. The component starter also starts the heartbeat manager
 * component once the registration of the component has been done successfully.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/DCCComponentStarterImpl.java#2 $:
 */

public class DCCComponentStarterImpl extends Thread implements IDCCComponentStarter,
        IInitializable, IDisposable, ILogEnabled, IConfigurable, IManagerEnabled
{

    /**
     * Time after which a warning signal will be issued saying that the
     * registration request still has not been answered.
     */
    private static final long REGISTRATION_WARNING_TIME = 60000;

    private IRegisteredDCCComponent dccComponent;
    private IComponentManager manager;
    private IConfiguration config;
    private Log log;
    private IDCCRegistrationStatus registrationStatus;
    private IDestinySharedContext sharedContext;
    private Class<? extends IRegisteredDCCComponent> dccComponentClassName;
    private Class<? extends IHeartbeatMgr> heartbeatMgrClassName;
    private String dccComponentName;
    private ServerComponentType dccComponentType;
    private IHeartbeatMgr heartbeatMgr;
    private Object lockObject;
    
    public DCCComponentStarterImpl(){
        this("DCCComponentStarter");
    }

    public DCCComponentStarterImpl(String name) {
        super(name);
    }
    /**
     * Performs the initialization
     */
    public void init() {
        this.dccComponentClassName = this.config.get(IDCCComponentStarter.DCC_COMPONENT_CLASSNAME_CONFIG_PARAM);
        if (this.dccComponentClassName == null) {
            getLog().fatal("The DCC component name needs to be provided to the component starter");
            throw new IllegalArgumentException("The DCC component name needs to be provided to the component starter");
        }

        this.dccComponentName = this.config.get(IDCCComponentStarter.DCC_COMPONENT_NAME_CONFIG_PARAM);
        if (this.dccComponentName == null) {
            getLog().fatal("The DCC component name needs to be provided to the component starter");
            throw new IllegalArgumentException("The DCC component name needs to be provided to the component starter");
        }

        this.dccComponentType = this.config.get(IDCCComponentStarter.DCC_COMPONENT_TYPE_CONFIG_PARAM);
        if (this.dccComponentType == null) {
            getLog().fatal("The DCC component type needs to be provided to the component starter");
            throw new IllegalArgumentException("The DCC component name needs to be provided to the component starter");
        }

        this.heartbeatMgrClassName = this.config.get(IDCCComponentStarter.HEARTBEAT_MGR_CLASSNAME_CONFIG_PARAM);
        if (this.heartbeatMgrClassName == null) {
            this.heartbeatMgrClassName = HeartbeatMgrImpl.class;
        }

        this.lockObject = getConfiguration().get(IDCCComponentStarter.LOCK_OBJECT_CONFIG_PARAM);
        if (this.lockObject == null) {
            getLog().fatal("No Lock object provided to the component starter");
            throw new IllegalArgumentException("The lock object needs to be provided to the component starter");
        }

        //The shared context needs to be kept so that the component can be
        // unregistered at shutdown time (though, the locator may
        //already be destroyed).
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) getManager().getComponent(IDestinySharedContextLocator.COMP_NAME);
        this.sharedContext = locator.getSharedContext();
        this.start();
    }

    /**
     * This is the main thread function.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            super.run();
            prepareToStartDCCComponent();
        } catch (Throwable e) {
            getLog().error( "Exception occured in the starter thread run() method for component - '"
                        + this.dccComponentName + "' of type '" + this.dccComponentType.getName() + "'", e);
        }
    }

    protected void giveLockObject() {
        synchronized(this.lockObject) {
            //We need to be within synchronized(this) to guarantee that the
            // notify() call to this object is received even if wait() was
            // not reached yet.
            this.lockObject.notify();
        }
    }

    /**
     * This is the main loop function for the thread. Upon notification and
     * successful DMS registration, the DCC component instance is created. Once
     * the DCC component is created, the component starter can be destroyed, as
     * it becomes useless.
     */
    protected void prepareToStartDCCComponent() {
        synchronized (this) {
            giveLockObject();

            while (this.registrationStatus == null) {
                if (Thread.currentThread().isInterrupted()) {
                    getLog().debug("DCC component starter interrupted without starting the component");
                    return;
                }

                try {
                    wait(REGISTRATION_WARNING_TIME);
                } catch (InterruptedException e) {
                    //Thread is interrupted, the function returns
                    getLog().debug("DCC component starter interrupted without starting the component");
                    return;
                }

                if (this.registrationStatus == null) {
                    getLog().warn("The Management server has still not answered the registration request for '" 
                            + this.dccComponentName + "'. The management server may be down or may have network connectivity issues.");
                }
            }

            //Now, do the main job here
            DMSRegistrationResult registrationResult = this.registrationStatus.getRegistrationResult();
            if (DMSRegistrationResult.SUCCESS.equals(registrationResult)) {
                getLog().debug("Registration succeeded for '" + this.dccComponentName + "'. Proceeding to create the actual component instance.");

                // Save the configuration information that was returned:
                final IDestinyConfigurationStore confStore = getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
                confStore.cacheAuthConfig(this.registrationStatus.getApplicationUserConfiguration());
                confStore.cacheMessageHandlersConfig(this.registrationStatus.getMessageHandlersConfiguration());
                confStore.cacheActionListConfig(this.registrationStatus.getActionListConfig());
                confStore.cacheCustomObligationsConfig(this.registrationStatus.getCustomObligationsConfiguration());
                confStore.cacheComponentConfiguration(this.dccComponentType.getName(), this.registrationStatus.getComponentConfiguration());
                confStore.cacheRepositoryConfigurations(this.registrationStatus.getRepositoryConfigurations());
                
                //Create the component and perform registration with DCSF
                this.dccComponent = createNewDCCComponent();
                this.sharedContext.getRegistrationManager().registerComponent(this.dccComponent);
                startSendingHeartbeats();
            } else {
                getLog().error("DMS registration failed for component " + this.dccComponentName + ", giving up registration.");
            }
            this.registrationStatus = null;
        }
    }

    /**
     * Creates a new DCC component
     */
    private IRegisteredDCCComponent createNewDCCComponent() {
        HashMapConfiguration newDCCCompConfig = new HashMapConfiguration();
        //Transfers the component configuration parameters passed from the
        // contained into the component itself
        IConfiguration componentConfig = getConfiguration().get(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME);
        
        newDCCCompConfig.override(componentConfig);
        newDCCCompConfig.setProperty(IRegisteredDCCComponent.DMS_REGISTRATION_STATUS_CONFIG_PARAM, this.registrationStatus);
        ComponentInfo<IRegisteredDCCComponent> dccCompInfo = 
            new ComponentInfo<IRegisteredDCCComponent>(
                this.dccComponentName, 
                this.dccComponentClassName, 
                IRegisteredDCCComponent.class, 
                LifestyleType.SINGLETON_TYPE,
                newDCCCompConfig);
        IRegisteredDCCComponent component = getManager().getComponent(dccCompInfo);
        return (component);
    }

    /**
     * This function starts the heartbeat manager. The heartbeat rate is
     * extracted from the component configuration.
     */
    private void startSendingHeartbeats() {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration heartbeatConfig = new HashMapConfiguration();

        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_ID_CONFIG_PARAM, this.dccComponentName);
        heartbeatConfig.setProperty(IHeartbeatMgr.COMPONENT_TYPE_CONFIG_PARAM, this.dccComponentType);

        //Extract the hearbeat information from the component configuration
        //if it is there
        IDCCComponentConfigurationDO compConfig = this.registrationStatus.getComponentConfiguration();
        int rate = compConfig.getHeartbeatInterval();
        Long iRate = new Long(rate);
        heartbeatConfig.setProperty(IHeartbeatMgr.HEARTBEAT_RATE_CONFIG_PARAM, iRate);
        ComponentInfo<IHeartbeatMgr> compStarterInfo = 
            new ComponentInfo<IHeartbeatMgr>(
                IHeartbeatMgr.COMP_NAME, 
                this.heartbeatMgrClassName, 
                IHeartbeatMgr.class, 
                LifestyleType.SINGLETON_TYPE, 
                heartbeatConfig);
        heartbeatMgr = this.getManager().getComponent(compStarterInfo);
    }

    /**
     * Disposes the component.
     */
    public void dispose() {
        // Interrupt self:
        this.interrupt();
        this.sharedContext.getRegistrationManager().unregisterComponent(this.dccComponent);
        this.dccComponent = null;
        this.config = null;
        this.dccComponentClassName = null;
        this.registrationStatus = null;
        this.dccComponent = null;
        this.sharedContext = null;
    }

    /**
     * Returns the configuration
     * 
     * @return the configuration
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * This function is called when the DMS registration is complete (correctly
     * or not). If the registration is accepted, the DCC component is created.
     * If the registration is refused, nothing happens and the DCC component is
     * not created.
     * 
     * @see com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener#onDMSRegistration(com.bluejungle.destiny.services.management.types.DCCRegistrationStatus)
     */
    public void onDMSRegistration(IDCCRegistrationStatus status) {
        synchronized (this) {
            this.registrationStatus = status;
            notify();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration configuration) {
        this.config = configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }
}
