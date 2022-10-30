/*
 * Created on Jan 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dcc.DCCEvents;
import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.container.dms.data.EventRegistration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository;
import com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.HibernateApplicationUserRepository;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatCookieImpl;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationStatusImpl;
import com.bluejungle.destiny.server.shared.registration.impl.EventRegistrationInfoImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.IDestinyConfigurationManager;

/**
 * This class serves to keep the event database and the component database in
 * synch with each other. The synch logic is implemented by this class.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class DCCRegistrationBrokerImpl implements IDCCRegistrationBroker,
        IInitializable, ILogEnabled, IConfigurable, IDisposable,
        IManagerEnabled {

    // Not sure where this constant belongs.
    private static final String NOT_YET_INITIALIZED_PROFILE_HOST = "not_initialized";

    private IConfiguration configuration;
    private Log log;
    private IComponentManager manager;
    private IDCCComponentMgr componentMgr;
    private IDCCEventRegistrationMgr eventRegMgr;
    private IDestinyConfigurationManager configMgr;
    private IHibernateRepository repository;
    private IDestinySharedContextLocator sharedCtxLocator;
    private LifecycleManager lifecycleManager;
    private IApplicationUserRepository userRepository;

    /**
     * Constructor
     *  
     */
    public DCCRegistrationBrokerImpl() {
        super();
    }

    /**
     * Appends DCSF updates onto the heartbeat response packet. It extracts the
     * cookie from the heartbeat and uses the embedded timestamp to determine
     * whether there are any new event registrations that have occured since
     * that time. This list is appended onto the heartbeat response update
     * packet.
     * 
     * @param component
     *            the dcsf component for which the updates are intended
     * @param heartbeat
     *            containing a cookie and other identifying information for the
     *            component
     * @param update
     *            response packet for the heartbeat containing any updates
     * @throws DataSourceException
     */
    protected void appendDCSFUpdates(ComponentDO component, IComponentHeartbeatInfo heartbeat,
            ComponentHeartbeatResponseImpl update) throws DataSourceException {
        // Extract the cookie from the heartbeat. If not, create one:
        Calendar updateTimestamp = Calendar.getInstance();
        IComponentHeartbeatCookie cookie = heartbeat.getHeartbeatCookie();
        if (cookie == null) {
            cookie = new ComponentHeartbeatCookieImpl(updateTimestamp.getTimeInMillis());
        }

        List<EventRegistration> regList = this.eventRegMgr.getRegistrationsSince(cookie.getUpdateTimestamp(), component);
        if (regList != null) {
            // Loop through and create a EventRegistrationInfo array from the
            // registration data returned:
            getLog().debug("Sending following registration updates to '" + heartbeat.getComponentName() + "':");
            for (EventRegistration regData : regList) {
                URL callbackURL = null;
                try {
                    callbackURL = new URL(regData.getCallbackURL());
                } catch (MalformedURLException e) {
                    // This should never happen since the URL was initially
                    // persisted from a strongly typed URI instance.
                    getLog().error("URI for component" + regData.getConsumer().getName() + " is malformed.", e);
                }
                IEventRegistrationInfo regInfo = new EventRegistrationInfoImpl(regData.getEvent().getName(), callbackURL, regData.getIsActive());
                getLog().debug(" (" + regInfo.getName() + "," + regInfo.getCallbackURL() + "," + regInfo.isActive() + ")");
                update.addEventRegistrationInfo(regInfo);
            }
        }

        // Add the timestamp to the cookie so that next time we only return
        // the registrations occuring after this time:
        update.setCookie(cookie);
    }

    /**
     * Begins a transaction if one didn't previously exist on the session
     * object.
     * 
     * @param session
     */
    protected void beginTransaction(Session session) {
        try {
            Transaction longTransaction = session.beginTransaction();
        } catch (HibernateException e) {
            getLog().error("Failed to begin hibernate transaction", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the last heartbeat date of the referenced component and returns
     * the event registrations that have occurred since the last heartbeat
     * update was received by the corresponding component.
     * 
     * @param heartbeat
     *            containing a cookie and other identifying information for the
     *            component
     * @return ComponentHeartbeatUpdate response packet for the heartbeat
     *         containing any updates
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentAndEventMgrFacade#checkUpdates(com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse checkUpdates(IComponentHeartbeatInfo heartbeat) {
        Session currSession = null;
        ComponentHeartbeatResponseImpl response = new ComponentHeartbeatResponseImpl();
        try {
            currSession = this.repository.getCurrentSession();
            this.beginTransaction(currSession);

            ComponentDO component = this.componentMgr.getComponentByName(heartbeat.getComponentName());

            // We only register if it is present in the database. Otherwise we
            // do nothing.
            if (component != null) {
                // Register the heartbeat with the component manager:
                this.componentMgr.confirmActive(heartbeat);

                // If it is a DCSF component, we send some updates:
                if (ServerComponentType.DCSF.getName().equals(component.getType().getName())) {
                    appendDCSFUpdates(component, heartbeat, response);
                }
            }

            this.commitTransaction(currSession);
        } catch (DataSourceException e) {
            getLog().error("Error occurred processing a heartbeat", e);
        } catch (HibernateException e) {
            getLog().error("Error occurred processing a heartbeat", e);
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException exception) {
                getLog().warn("Failed to close current session", exception);
            }
        }

        return response;
    }

    /**
     * Cleans the database tables associated with component and event
     * registration. Currently the only use for this is with JUnit testing.
     *  
     */
    public void clearAll() {
        Session currSession = null;
        try {
            currSession = this.repository.getCurrentSession();
            this.beginTransaction(currSession);

            this.eventRegMgr.clearAll();
            this.componentMgr.clearAll();

            this.commitTransaction(currSession);
        } catch (DataSourceException e) {
            getLog().error(e.getMessage());
        } catch (HibernateException e) {
            getLog().error(e.getMessage());
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException ignore) {
            }
        }

    }

    /**
     * Commits the transaction that is currently pending on the session object.
     * 
     * @param session
     */
    protected void commitTransaction(Session session) {
        try {
            if (session != null) {
                Transaction longTransaction = session.beginTransaction();
                longTransaction.commit();
            }
        } catch (HibernateException e) {
            getLog().error("Failed to commit hibernate transaction - rolling back ...", e);
            this.rollbackTransaction(session);
            throw new RuntimeException(e);
        }
    }

    /**
     * Dispose the object
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.componentMgr = null;
        this.eventRegMgr = null;
        this.configuration = null;
        this.repository = null;
        this.log = null;
    }

    /**
     * This method fires an "update" event that will solicit a heartbeat from
     * all the DCC components (i.e. DCSFs) that are registered as listeners to
     * updates.
     *  
     */
    protected void fireEventRegistrationChangeEvent() {
        getLog().info("Firing an update event to solicit a heartbeat from all components.");
        // Create and fire event:
        getSharedContextLocator().getSharedContext().getEventManager().fireEvent(
                new DCCServerEventImpl(DCCEvents.EVENT_REGISTRATION_UPDATES_AVAILABLE));
    }

    /**
     * Get the configuration.
     * 
     * @return IConfiguration configuration object
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Get the logger
     * 
     * @return log
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCRegistrationBroker#getRegisteredComponents()
     */
    public List<IDCCComponentDO> getRegisteredComponents() throws DataSourceException {
        List<IDCCComponentDO> itemsToReturn = componentMgr.getComponents();
        return itemsToReturn;
    }

    /**
     * @param type
     * @return
     * @throws DataSourceException
     */
    public List<IDCCComponentDO> getRegisteredComponentsByType(DCCComponentEnumType type) throws DataSourceException {
        List<IDCCComponentDO> itemsToReturn = componentMgr.getComponentByType(type);
        return itemsToReturn;
    }

    /**
     * Returns the shared context locator object
     * 
     * @return the shared context locator object
     */
    protected IDestinySharedContextLocator getSharedContextLocator() {
        return this.sharedCtxLocator;
    }

    /**
     * Initialize
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        getLog().debug("Initializing instance of DCCComponentAndEventManagerFacade...");

        // Initialize the data source:
        try {
            this.repository = (IHibernateRepository) getManager().getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        } catch (RuntimeException e) {
            throw new RuntimeException("Required repository '" + DestinyRepository.MANAGEMENT_REPOSITORY + "' is not initialized.", e);
        }

        if (this.repository == null) {
            String message = "Required repository '" + DestinyRepository.MANAGEMENT_REPOSITORY + "' is not initialized.";
            getLog().fatal("Cannot initialize instance of DCCComponentAndEventManagerFacade. " + message);
            throw new RuntimeException(message);
        }

        //Initializes the component registration manager
        ComponentInfo<IDCCComponentMgr> regMgrCompInfo = 
            new ComponentInfo<IDCCComponentMgr>(
                IDCCComponentMgr.COMP_NAME, 
                DCCComponentMgrImpl.class, 
                IDCCComponentMgr.class, 
                LifestyleType.TRANSIENT_TYPE);
        this.componentMgr = getManager().getComponent(regMgrCompInfo);

        //Initializes the DCC event registration manager
        ComponentInfo<IDCCEventRegistrationMgr> eventMgrCompInfo = 
            new ComponentInfo<IDCCEventRegistrationMgr>(
                IDCCEventRegistrationMgr.COMP_NAME, 
                DCCEventRegistrationMgrImpl.class, 
                IDCCEventRegistrationMgr.class, 
                LifestyleType.TRANSIENT_TYPE);
        this.eventRegMgr = getManager().getComponent(eventMgrCompInfo);

        //Obtain a handle to the Configuration manageter
        this.configMgr = (IDestinyConfigurationManager) getManager().getComponent(IDestinyConfigurationManager.COMP_NAME);
        if (this.configMgr == null) {
            getLog().fatal("Could not initialize Destiny Configuration Manager");
            throw new RuntimeException("Could not initialize the Destiny Configuration Manager");
        }

        this.sharedCtxLocator = (IDestinySharedContextLocator) getManager().getComponent(IDestinySharedContextLocator.COMP_NAME);
        
        this.lifecycleManager = getManager().getComponent(LifecycleManager.COMP_INFO);
        
        this.userRepository = new HibernateApplicationUserRepository();
        // I don't need initialize.
        // this.userRepository.initialize(properties, dataSource, externalDomainManager)
        
        getLog().debug("Initialization of instance of DCCComponentAndEventManagerFacade is complete.");
    }
    
    private void registerApplicationResourceComponents(Set<String> appResources)
            throws ApplicationUserRepositoryAccessException, PQLException,
            EntityManagementException {
        Long superAdminId = userRepository.getSuperUser().getDestinyId();
        if(superAdminId == null){
            throw new NullPointerException("The super user id is null.");
        }
        
        List<DevelopmentEntity> developmentEntities = new LinkedList<DevelopmentEntity>();
        
        for (String appResource : appResources) {
            // check if the resource exists
            DevelopmentEntity appEntity;
            try {
                appEntity = lifecycleManager.getEntityForName(
                        EntityType.COMPONENT
                      , appResource
                      , LifecycleManager.MUST_EXIST);
            } catch (EntityManagementException e) {
                appEntity = null;
            }
            
            // if not exist, create and deploy
            if (appEntity == null) {
                developmentEntities.add(createComponentPql(superAdminId.longValue(), appResource));
            }
        }
         
        if (!developmentEntities.isEmpty()) {
            lifecycleManager.saveEntities(
                    developmentEntities
                  , LifecycleManager.MAKE_EMPTY
                  , null
            );
        }
    }
        
    private void registerComponent(IDCCRegistrationInfo regInfo,
            IDCCComponentConfigurationDO componentConfigDO)
            throws DataSourceException, ComponentRegistrationException {
        getLog().debug("Creating database entry for component '" + regInfo.getComponentName() + "'.");
        ComponentDO componentToEnable = this.componentMgr.getComponentByName(regInfo.getComponentName());
        if (componentToEnable == null) {
            componentToEnable = new ComponentDO();
        }
        componentToEnable.setCallbackURL(regInfo.getEventListenerURL().toString());
        componentToEnable.setComponentURL(regInfo.getComponentURL().toString());
        componentToEnable.setLastHeartbeat(Calendar.getInstance());
        componentToEnable.setHeartbeatRate(componentConfigDO.getHeartbeatInterval());
        componentToEnable.setName(regInfo.getComponentName());
        componentToEnable.setTypeDisplayName(regInfo.getComponentTypeDisplayName());
        componentToEnable.setType(DCCComponentEnumType.getServerComponentTypeEnum(regInfo.getComponentType().getName()));
        componentToEnable.setVersion(regInfo.getComponentVersion());
        
        this.componentMgr.enableComponent(componentToEnable);
        
        // Update default agent profiles if necessary
        try {
            updateMatchingProfiles(componentToEnable);
        } catch (URISyntaxException exception) {
            getLog().error("Component registration succeeded. "
                           + "Failed, however, to initialize default profile. "
                           + "Component URL of component is not in proper URL format",
                           exception);
            throw new ComponentRegistrationException(regInfo.getComponentName(), exception);
        } catch (DataSourceException exception) {
            getLog().error("Component registration succeeded. " 
                           + "Failed, however, to initialize default profile.", exception);
            throw new ComponentRegistrationException(regInfo.getComponentName(), exception);
        }
    }
        
    private boolean requiresRegistration(IDCCRegistrationInfo regInfo,
            ComponentDO existingComp) throws ComponentRegistrationException {
        boolean needToRegister;
        getLog().debug("Component '" + regInfo.getComponentName() + "' has registered with DMS in the past.");
        
        // If component exists (compare by name) and the callback URL has changed, 
        // we need to un-register the component - this will happen as a separate
        // transaction, independent of registerComponent()'s
        // transaction.
        if (!existingComp.getCallbackURL().equals(regInfo.getEventListenerURL().toString())) {
            getLog().info("Component '" + regInfo.getComponentName() 
                    + "' has a different callback URL now - '" 
                    + regInfo.getEventListenerURL().toString() 
                    + "'. It was '" + existingComp.getCallbackURL()
                    + "' before. Need to unregister it first and then re-register.");
            this.unregisterComponent(regInfo);
            needToRegister = true;
        }
        // Else if the component was previously in-active, we need to
        // register it:
        else if (!existingComp.isActive()) {
            needToRegister = true;
        } else {
            // There's no need to re-register an existing component:
            needToRegister = false;
        }
        return needToRegister;
    }
        
    private static final String DEFAULT_RESOURCE_PQL = 
            "ID null STATUS APPROVED CREATOR \"%1$d\" ACCESS_POLICY"
            + " ACCESS_CONTROL ALLOWED_ENTITIES"
            + " HIDDEN COMPONENT \"%2$s\" = PRINCIPAL.APPLICATION.NAME = \"%2$s\"";
        
    private DevelopmentEntity createComponentPql(long id, String name) throws PQLException {
        String pql = String.format(DEFAULT_RESOURCE_PQL, id, name);
        DevelopmentEntity devEntity = new DevelopmentEntity(pql);
        return devEntity;
    }

    /**
     * Registers a component with the DMS. Also ensures that if the new
     * component is a DCSF, it is kept in synch with all events being listened
     * by other DCSF components in the system.
     * 
     * @param regInfo
     * @return registration status
     * @throws DCCRegistrationBrokerException
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentAndEventMgrFacade#registerComponent(com.bluejungle.destiny.services.management.types.DCCRegistrationInformation)
     */
    public IDCCRegistrationStatus registerComponent(IDCCRegistrationInfo regInfo)
            throws ComponentRegistrationException, ConfigNotFoundException {
        getLog().info("DMS Received registration from " + regInfo.getComponentName());

        // Check if all required fields exist. If not we fail the registration:
        List<String> errors = verifyRequiredFields(regInfo);
        if (!errors.isEmpty()) {
            throw new ComponentRegistrationException(regInfo.getComponentName(), errors);
        }

        // Check if the configuration exists. If not we fail the registration.
        IDCCComponentConfigurationDO componentConfigDO =
                this.configMgr.getDCCConfiguration(regInfo.getComponentType());
        
        if( componentConfigDO == null){
            getLog().error("No configuration exists for component type - " + regInfo.getComponentType().getName());
            throw new ConfigNotFoundException(regInfo.getComponentName(), regInfo.getComponentType().getName());
        }
        
        // Create a persistent session/transaction:
        Session currSession = null;
        try {
            ComponentDO existingComp = this.componentMgr.getComponentByName(regInfo.getComponentName());
            boolean needToRegister = existingComp != null 
                    ? needToRegister = requiresRegistration(regInfo, existingComp) 
                    : true;

            currSession = this.repository.getCurrentSession();
            // If we need to explicitly register the component:
            if (needToRegister) {
                beginTransaction(currSession);
                registerComponent(regInfo, componentConfigDO);
                commitTransaction(currSession);
            }
            
            Set<String> appResources = regInfo.getApplicationResources();
            if (appResources != null) {
                this.beginTransaction(currSession);
                registerApplicationResourceComponents(appResources);
                this.commitTransaction(currSession);
            }
            
        } catch (Exception e) {
            // Rollback the transaction:
            if (currSession != null) {
                this.rollbackTransaction(currSession);
            }
            getLog().error("Component registration failed - transaction was rolled back.", e);
            throw new ComponentRegistrationException(regInfo.getComponentName(), e);
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException ignore) {
            }
        }
        
        
        
        RepositoryConfigurationList list = new RepositoryConfigurationList();
        Set<? extends IRepositoryConfigurationDO> repList = this.configMgr.getRepositories();
        for(IRepositoryConfigurationDO repositoryConfig : repList){
            list.addRepository((RepositoryConfigurationDO)repositoryConfig);
        }

        return new DCCRegistrationStatusImpl(
                this.configMgr.getApplicationUserConfiguration(),
                this.configMgr.getMessageHandlersConfiguration(),
                this.configMgr.getCustomObligationsConfiguration(),
                this.configMgr.getActionListConfig(),
                componentConfigDO, 
                list,
                DMSRegistrationResult.SUCCESS
        );
    }

    /**
     * Registers a callback as a listener to an event.
     * 
     * @param eventName
     * @param callback
     *            URL
     * @throws EventRegistrationException
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentAndEventMgrFacade#registerEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void registerEvent(String eventName, URL callback) throws EventRegistrationException {
        getLog().info("DMS Received event-registration for event '" + eventName + "' from '" + callback + "'.");
        Session currSession = null;

        try {
            currSession = this.repository.getCurrentSession();
            this.beginTransaction(currSession);

            ComponentDO dcsf = this.componentMgr.getDCSFByCallbackURL(callback.toString());

            // If component doesn't exist, we can't register it with an event
            if (dcsf != null) {
                // We set this dcsf as a listener for the given event:
                this.eventRegMgr.registerConsumerForEvent(eventName, dcsf);

                this.commitTransaction(currSession);

                this.fireEventRegistrationChangeEvent();
            } else {
                this.rollbackTransaction(currSession);
            }
        } catch (DataSourceException e) {
            this.rollbackTransaction(currSession);
            getLog().error("Event registration failed - transaction was rolled back.", e);
            throw new EventRegistrationException(eventName, callback.toString());
        } catch (HibernateException e) {
            this.rollbackTransaction(currSession);
            getLog().error("Event registration failed - transaction was rolled back.", e);
            throw new EventRegistrationException(eventName, callback.toString());
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException ignore) {
            }
        }
    }

    /**
     * Rolls back a transaction on the given session object.
     * 
     * @param session
     */
    protected void rollbackTransaction(Session session) {
        try {
            if (session != null) {
                Transaction longTransaction = session.beginTransaction();
                longTransaction.rollback();
            }
        } catch (HibernateException e) {
            getLog().error("Failed to rollback hibernate transaction - database state may be corrupt.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the configuration.
     * 
     * @param config
     *            config object
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * Set the logger
     * 
     * @param log
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Unregisters a component from the DMS
     * 
     * @param unregInfo
     * @throws DCCRegistrationBrokerException
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentAndEventMgrFacade#unregisterComponent(com.bluejungle.destiny.services.management.types.DCCRegistrationInformation)
     */
    public void unregisterComponent(IDCCRegistrationInfo unregInfo)
            throws ComponentRegistrationException {
        getLog().info("DMS Received un-registration from " + unregInfo.getComponentName());
        Session currSession = null;

        // Check if all required fields exist. If not we fail the registration:
        List<String> errors = verifyRequiredFields(unregInfo);
        if (!errors.isEmpty()) {
            throw new ComponentRegistrationException(unregInfo.getComponentName(), errors);
        }

        try {
            currSession = this.repository.getCurrentSession();
            this.beginTransaction(currSession);

            ComponentDO comp = this.componentMgr.getComponentByName(unregInfo.getComponentName());
            if (comp != null) {
                // Unregister component with DMS:
                getLog().debug("Unregistering component '" + unregInfo.getComponentName() + "'.");
                this.componentMgr.disableComponent(comp);

                // If this is a dcsf, we need to unregister it as a listener
                // from all events in the system first:
                if (ServerComponentType.DCSF.getName().equals(comp.getType().getName())) {
                    getLog().debug("Unregistering all events for component '" + unregInfo.getComponentName() + "'.");
                    this.eventRegMgr.unregisterConsumerForAllEvents(comp);

                    this.fireEventRegistrationChangeEvent();
                }
            }

            this.commitTransaction(currSession);
        } catch (DataSourceException e) {
            // Rollback the transaction:
            this.rollbackTransaction(currSession);
            getLog().error("Component unregistration failed - transaction was rolled back.", e);
            throw new ComponentRegistrationException(unregInfo.getComponentName(), e);
        } catch (HibernateException e) {
            // Rollback the transaction:
            this.rollbackTransaction(currSession);
            getLog().error("Component unregistration failed - transaction was rolled back.", e);
            throw new ComponentRegistrationException(unregInfo.getComponentName(), e);
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException ignore) {
            }
        }
    }

    /**
     * Unregisters an event from the system.
     * 
     * @param eventName
     * @param callback
     *            URL
     * @throws EventRegistrationException
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentAndEventMgrFacade#unregisterEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void unregisterEvent(String eventName, URL callback) throws EventRegistrationException {
        getLog().info("DMS Received event-un-registration for event '" + eventName + "' from '" + callback + "'.");
        Session currSession = null;

        try {
            currSession = this.repository.getCurrentSession();
            this.beginTransaction(currSession);

            ComponentDO dcsf = this.componentMgr.getDCSFByCallbackURL(callback.toString());
            if (dcsf != null) {
                this.eventRegMgr.unregisterConsumerForEvent(eventName, dcsf);

                this.commitTransaction(currSession);

                this.fireEventRegistrationChangeEvent();
            } else {
                this.rollbackTransaction(currSession);
            }
        } catch (DataSourceException e) {
            this.rollbackTransaction(currSession);
            getLog().error("Event unregistration failed - transaction was rolled back.", e);
            throw new EventRegistrationException(eventName, callback.toString(), e);
        } catch (HibernateException e) {
            this.rollbackTransaction(currSession);
            getLog().error("Event unregistration failed - transaction was rolled back.", e);
            throw new EventRegistrationException(eventName, callback.toString(), e);
        } finally {
            try {
                this.repository.closeCurrentSession();
            } catch (HibernateException ignore) {
            }
        }
    }
    
    
    /**
     *  Component Type Display Name and component display name are the same thing.
     */
    private static final Pattern TYPE_DISPLAY_NAME_PATTERN = Pattern.compile("[\\p{Graph} ]*");
    
    private static final Pattern TYPE_PATTERN = Pattern.compile("[\\p{Alnum}_]*");

    /**
     * Verifies that all the required fields exist for the registration
     * information.
     * 
     * @param regInfo
     * @return boolean
     */
    protected List<String> verifyRequiredFields(IDCCRegistrationInfo regInfo) {
        List<String> errors = new LinkedList<String>();
        
        checkString(regInfo.getComponentName(), 1, 128, "Component Name", errors);
        
        if (regInfo.getComponentType() == null) {
            errors.add("The 'Component Type' can't be null.");
        } else {
            checkString(regInfo.getComponentType().getName()
                    , 2, 32
                    , TYPE_PATTERN
                    , "Component Type", errors);
        }
        
        checkString(regInfo.getComponentTypeDisplayName()
                , 1, 128
                , TYPE_DISPLAY_NAME_PATTERN
                , "Component Type Display Name", errors);
        
        if (regInfo.getEventListenerURL() == null) {
            errors.add("The 'Event Listener URL' can't be null.");
        } else {
            checkString(regInfo.getEventListenerURL().toString(), 0, 128, "Event Listener URL", errors);
        }
        
        if (regInfo.getComponentURL() == null) {
            errors.add("The 'Component URL' can't be null.");
        } else {
            checkString(regInfo.getComponentURL().toString(), 0, 128, "Component URL", errors);
        }
        
        return errors;
    }
        
        
    private void checkString(String string, int min, int max, Pattern p, String objName, List<String> errors){
        if (string == null) {
            errors.add("The '" + objName + "' can't be null.");
        } else {
            if (string.length() < min || string.length() > max) {
                errors.add("The length of '" + objName + "', " + string.length() 
                        + ", is out of range. It must be between " + min + " and " + max + " characters.");
            }
            
            if (p != null) {
                Matcher m = p.matcher(string);
                if (!m.matches()) {
                    errors.add("The format of '" + objName + "' is invalid. It does not match '" + p.pattern() + "." );
                }
            }
        }
    }
    
    private void checkString(String string, int min, int max, String objName, List<String> errors){
        checkString(string, min, max, null, objName, errors);
    }
    
    /**
     * Update profiles with uninitialized url locations to point to the initializing component (assuming they match)
     * 
     * @param initializingComponent
     * @throws DataSourceException
     * @throws URISyntaxException
     */
    private void updateMatchingProfiles(ComponentDO thisComponent) throws DataSourceException, URISyntaxException {
        IProfileManager profileManager = getProfileManager();
        IAgentManager agentManager = getAgentManager();
        List<IAgentType> agentTypes = agentManager.getAgentTypes();
        for (IAgentType agentType : agentTypes) {
            ICommProfileDO nextCommProfile = profileManager.getDefaultCommProfile(agentType);
            updateMatchingProfile(nextCommProfile, thisComponent);
        }
    }

    /**
     * If the profile has an uninitialized url location and we are initializing a component of that type,
     * update the profile to point to this component
     *
     * Internally this is still called the "DABS" component, but it could be either DABS or DDAC
     * 
     * @param profileToUpdate
     * @param initializingComponent
     * @throws URISyntaxException
     * @throws DataSourceException
     */
    private void updateMatchingProfile(ICommProfileDO profileToUpdate,
                                       ComponentDO thisComponent) throws URISyntaxException, DataSourceException {
        java.net.URI profileComponentLocation = profileToUpdate.getDABSLocation();
        if (uninitializedLocation(profileComponentLocation) && profileAndComponentMatch(profileComponentLocation, thisComponent.getComponentURL())) {

            profileToUpdate.setDABSLocation(new java.net.URI(thisComponent.getComponentURL()));
            IProfileManager profileManager = getProfileManager();
            profileManager.updateCommProfile(profileToUpdate);
        }
    }

    private boolean uninitializedLocation(java.net.URI location) {
        return location.toString().indexOf(NOT_YET_INITIALIZED_PROFILE_HOST) != -1;
    }

    private boolean profileAndComponentMatch(java.net.URI profileLocation, String componentURL) {
        String profileURL = profileLocation.toString();

        String profileSuffix = profileURL.substring(profileURL.lastIndexOf("/")).toLowerCase();
        String componentSuffix = componentURL.substring(componentURL.lastIndexOf("/")).toLowerCase();

        return (profileSuffix.equals(componentSuffix));
    }
    /**
     * Retrieve the profile manager
     * 
     * @return the profile manager
     */
    private IProfileManager getProfileManager() {
        IComponentManager componentManager = getManager();
        if (!componentManager.isComponentRegistered(IProfileManager.COMP_NAME)) {
            throw new IllegalStateException("Profile manager not available.");
        }

        return (IProfileManager) getManager().getComponent(IProfileManager.COMP_NAME);
    }
    
    /**
     * Retrieve the agent manager
     * 
     * @return the agent manager
     */
    private IAgentManager getAgentManager() {
        IComponentManager componentManager = getManager();
        if (!componentManager.isComponentRegistered(IAgentManager.COMP_NAME)) {
            throw new IllegalStateException("Agent manager not available.");
        }

        return (IAgentManager) getManager().getComponent(IAgentManager.COMP_NAME);
    }
}
