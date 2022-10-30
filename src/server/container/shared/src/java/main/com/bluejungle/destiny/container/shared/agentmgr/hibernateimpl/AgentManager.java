/*
 * Created on Dec 10, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrSortFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IActionType;
import com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrSortTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentShutdownData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdateAcknowledgementData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.ProfileNotFoundException;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.AgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.CommProfileDO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.search.RelationalOp;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Conjunction;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;
import net.sf.hibernate.type.BooleanType;
import net.sf.hibernate.type.LongType;
import net.sf.hibernate.type.StringType;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the IAgentManager interface and provides CRUD
 * operations for IAgentDO objects in the management repository.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class AgentManager implements IAgentManager, IConfigurable, ILogEnabled, IInitializable {

    private static final int HIBERNATE_SESSION_FLUSH_THRESHOLD = 20;
    private static final String AGENT_VAR_NAME = "a";
    private static final String AGENT_QUERY_MATCHING_AGENTS_PREFIX = "from AgentDO " + AGENT_VAR_NAME;

    /**
     * Wildchar constants
     */
    private static final char WILDCHAR = '*';
    private static final char HQL_WILDCHAR = '%';

    /**
     * Static HQL types
     */
    private static final Type HQL_BOOLEAN_TYPE = new BooleanType();
    private static final Type HQL_LONG_TYPE = new LongType();
    private static final Type HQL_STRING_TYPE = new StringType();

    private static final IAgentMgrQueryTermFactory QUERY_TERM_FACTORY = new HibernateAgentMgrQueryTermFactory();

    /**
     * This map stores the HQL
     */
    private static final Map<AgentMgrSortFieldType, String> SORT_FIELD_TO_PROP_MAP = new HashMap<AgentMgrSortFieldType, String>();
    static {
        SORT_FIELD_TO_PROP_MAP.put(AgentMgrSortFieldType.HOST, "host");
        SORT_FIELD_TO_PROP_MAP.put(AgentMgrSortFieldType.TYPE, "type");
        SORT_FIELD_TO_PROP_MAP.put(AgentMgrSortFieldType.LAST_HEARTBEAT, "lastHeartbeat");
        SORT_FIELD_TO_PROP_MAP.put(AgentMgrSortFieldType.LAST_POLICY_UPDATE, "policyAssemblyStatus.lastAcknowledgedDeploymentBundleTimestamp");
        SORT_FIELD_TO_PROP_MAP.put(AgentMgrSortFieldType.PROFILE_NAME, "commProfile.name");
    }

    public enum Day {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }
        
    private static final List<IAgentType> AGENT_TYPES;
    private static final Map<String, IAgentType> ID_TO_AGENT_TYPES_MAP;
    static {
        Set<IActionType> commonActions = new HashSet<IActionType>();
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN.getName(), "Abnormal Enforcer Shutdown", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ACCESS_AGENT_BINARIES.getName(), "Enforcer Binary File Access", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ACCESS_AGENT_CONFIG.getName(), "Enforcer Configuration File Access", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ACCESS_AGENT_LOGS.getName(), "Enforcer Log File Access", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_START_AGENT.getName(), "Enforcer Startup", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_STOP_AGENT.getName(), "Enforcer Shutdown (normal)", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_INVALID_BUNDLE.getName(), "Policy Bundle Authentication Failed", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_BUNDLE_RECEIVED.getName(), "Policy Bundle Authentication Succeeded", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ACCESS_AGENT_BUNDLE.getName(), "Policy Bundle File Access", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_AGENT_USER_LOGIN.getName(), "User Login", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_AGENT_USER_LOGOUT.getName(), "User Logout", StandardActivityJournalingLevel.MINIMUM));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_MOVE.getName(), "Move", StandardActivityJournalingLevel.DEFAULT));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_DELETE.getName(), "Delete", StandardActivityJournalingLevel.EXTENDED));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_OPEN.getName(), "Open", StandardActivityJournalingLevel.EXTENDED));
        commonActions.add(new ActionTypeImpl(ActionEnumType.ACTION_EDIT.getName(), "Create/Edit", StandardActivityJournalingLevel.EXTENDED));

        
        Set<IActionType> fileServerAgentActions = new HashSet<IActionType>();
        fileServerAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName(), "Change Attributes", StandardActivityJournalingLevel.DEFAULT));
        fileServerAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_CHANGE_SECURITY.getName(), "Change File Permissions", StandardActivityJournalingLevel.DEFAULT));
        fileServerAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_RENAME.getName(), "Rename File", StandardActivityJournalingLevel.DEFAULT));
                
        fileServerAgentActions.addAll(commonActions);
        IAgentType fileServerAgentType = new AgentTypeImpl(AgentTypeEnumType.FILE_SERVER.getName(), "File Server Enforcer", fileServerAgentActions);        
        
        Set<IActionType> desktopAgentActions = new HashSet<IActionType>();
        desktopAgentActions.addAll(fileServerAgentActions);
        desktopAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_COPY.getName(), "Copy / Embed File", StandardActivityJournalingLevel.DEFAULT));
        desktopAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_PRINT.getName(), "Print", StandardActivityJournalingLevel.DEFAULT));
        desktopAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_SEND_EMAIL.getName(), "Email", StandardActivityJournalingLevel.DEFAULT));
        desktopAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_SEND_IM.getName(), "Instant Message", StandardActivityJournalingLevel.DEFAULT));
        desktopAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_RUN.getName(), "Run an application", StandardActivityJournalingLevel.DEFAULT));
        IAgentType desktopAgentType = new AgentTypeImpl(AgentTypeEnumType.DESKTOP.getName(), "Desktop Enforcer", desktopAgentActions);
        
        Set<IActionType> portalAgentActions = new HashSet<IActionType>();
        portalAgentActions.addAll(commonActions);
        portalAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_COPY.getName(), "Copy", StandardActivityJournalingLevel.DEFAULT));
        portalAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_EXPORT.getName(), "Export", StandardActivityJournalingLevel.DEFAULT));
        portalAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_PRINT.getName(), "Print", StandardActivityJournalingLevel.DEFAULT));
        portalAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_ATTACH.getName(), "Attach to Item", StandardActivityJournalingLevel.DEFAULT));
        IAgentType portalAgentType = new AgentTypeImpl(AgentTypeEnumType.PORTAL.getName(), "Portal Enforcer", portalAgentActions);
        
        Set<IActionType> activeDirectoryAgentActions = new HashSet<IActionType>();
        activeDirectoryAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_RUN.getName(), "Run an application", StandardActivityJournalingLevel.DEFAULT));
        activeDirectoryAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_EDIT.getName(), "Create/Edit", StandardActivityJournalingLevel.EXTENDED));
        activeDirectoryAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_OPEN.getName(), "Open", StandardActivityJournalingLevel.EXTENDED));
        activeDirectoryAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_DELETE.getName(), "Delete", StandardActivityJournalingLevel.EXTENDED));
        activeDirectoryAgentActions.add(new ActionTypeImpl(ActionEnumType.ACTION_CHANGE_ATTRIBUTES.getName(), "Change Attributes", StandardActivityJournalingLevel.DEFAULT));
        IAgentType activeDirectoryAgentType = new AgentTypeImpl(AgentTypeEnumType.ACTIVE_DIRECTORY.getName(), "Active Directory", activeDirectoryAgentActions);

        List<IAgentType> agentTypes = new LinkedList<IAgentType>();
        Map<String, IAgentType> agentTypesMap = new HashMap<String, IAgentType>();
        agentTypes.add(desktopAgentType);
        agentTypesMap.put(desktopAgentType.getId(), desktopAgentType);
        agentTypes.add(fileServerAgentType);
        agentTypesMap.put(fileServerAgentType.getId(), fileServerAgentType);
        agentTypes.add(portalAgentType);
        agentTypesMap.put(portalAgentType.getId(), portalAgentType);
        agentTypes.add(activeDirectoryAgentType);
        agentTypesMap.put(activeDirectoryAgentType.getId(), activeDirectoryAgentType);

        AGENT_TYPES = Collections.unmodifiableList(agentTypes);
        ID_TO_AGENT_TYPES_MAP = Collections.unmodifiableMap(agentTypesMap);        
    }

    private IConfiguration configuration;
    private IHibernateRepository dataSource;
    private Log log;
    private IProfileManager profileMgr;
    private IHeartbeatRecorder heartbeatRecorder;

    /**
     * Constructor
     * 
     */
    public AgentManager() {
        super();
    }

    /**
     * Initializes the object.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        getLog().debug("Starting initialization of agent manager ...");
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Obtain a handle to the appropriate datasource
        getLog().debug("Obtaining handle(s) to the required datasource(s)...");
        try {
            this.dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        } catch (RuntimeException e) {
            throw new RuntimeException("Required datasource not initialized for AgentManager.", e);
        }
        if (this.dataSource == null) {
            throw new RuntimeException("Required datasource not initialized for AgentManager.");
        }

        heartbeatRecorder = (IHeartbeatRecorder) compMgr.getComponent(HeartbeatRecorderImpl.COMP_INFO);

        HashMapConfiguration agentStatisticsCollectionConfig = new HashMapConfiguration();
        agentStatisticsCollectionConfig.setProperty(AgentStatisticsCollectorImpl.HEARTBEAT_RECORDER_PROPERTY_NAME,
                                                    this.heartbeatRecorder);
        
        agentStatisticsCollectionConfig.setProperty(AgentStatisticsCollectorImpl.AGENT_MANAGER_PROPERTY_NAME, this);
  
        ComponentInfo agentStatisticsCollectorInfo = new ComponentInfo(IAgentStatisticsCollector.COMP_NAME, 
                                                                       AgentStatisticsCollectorImpl.class.getName(),
                                                                       IAgentStatisticsCollector.class.getName(), 
                                                                       LifestyleType.SINGLETON_TYPE,
                                                                       agentStatisticsCollectionConfig);

        compMgr.registerComponent(agentStatisticsCollectorInfo, false);

        getLog().debug("Finished initialization of agent manager.");
    }

    /**
     * Sets the configuration.
     * 
     * @param config
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * Returns the configuration
     * 
     * @return IConfiguration
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the data source object
     * 
     * @return the data source object
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * Sets the logger
     * 
     * @param logger
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log logger) {
        this.log = logger;
    }

    /**
     * Returns the logger
     * 
     * @return Log
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#getAgentType(java.lang.String)
     */
    public IAgentType getAgentType(String id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        
        if (!ID_TO_AGENT_TYPES_MAP.containsKey(id)) {
            throw new IllegalArgumentException("Agent Type with id," +  id + ", does not exist.");
        }
        
        return ID_TO_AGENT_TYPES_MAP.get(id);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#getAgentTypes()
     */
    public List<IAgentType> getAgentTypes() {
        return AGENT_TYPES;
    }

    /**
     * Returns the profile manager
     * 
     * @return the profile manager
     */
    private IProfileManager getProfileMgr() {
        if (this.profileMgr == null) {
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            this.profileMgr = (IProfileManager) componentManager.getComponent(IProfileManager.COMP_NAME);
        }
        return this.profileMgr;
    }

    /**
     * Register an agent. This is used to establish a new agent installation.
     * 
     * @param registrationData
     * @return startup configuration for the agent
     * @throws PersistenceException
     * @see com.bluejungle.destiny.container.shared.agentmgr.IDABSAgentManager#registerAgent(long,
     *      java.lang.String)
     */
    public IAgentStartupConfiguration registerAgent(IAgentRegistrationData registrationData) throws PersistenceException {
        if (registrationData == null) {
            throw new NullPointerException("registration data is null");
        }

        if (registrationData.getHost() == null) {
            throw new NullPointerException("agent host is null");
        }

        if (registrationData.getType() == null) {
            throw new NullPointerException("agent type is null");
        }

        if (registrationData.getVersion() == null) {
            throw new NullPointerException("agent version is null");
        }

        if (getLog().isInfoEnabled()) {
            getLog().info("Registering agent - (" + registrationData.getHost() + ", " + registrationData.getType() + ")");
        }

        IAgentMgrQueryTermFactory queryTermFactory = this.getAgentMgrQueryTermFactory();
        final AgentQuerySpecImpl agentByHostAndType = new AgentQuerySpecImpl();
        agentByHostAndType.addSearchSpecTerm(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.HOST, RelationalOp.EQUALS, registrationData.getHost()));
        agentByHostAndType.addSearchSpecTerm(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, registrationData.getType()));
        agentByHostAndType.setLimit(1);
        IAgentQueryResults result = null;
        try {
            result = getAgents(agentByHostAndType);
        } catch (InvalidQuerySpecException e) {
            // Should never happen here, the types are valid
            throw new PersistenceException(e);
        }
        AgentDO agent = null;
        if (result != null && result.getAgents() != null && result.getAgents().length > 0) {
            agent = (AgentDO) result.getAgents()[0];
        }
        if (agent != null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Agent registration already exists with id - (" + agent.getId() + "). Updating existing agent record ...");
            }
            agent.resetProfileStatus();
            agent.resetPolicyAssemblyStatus();
            agent.setLastHeartbeat(null);
            
            // don't set the host and type if the agent is already found (what if the type has changed?)
            
            // don't assign a new profile if agent is already registered.

            // but the version number might have changed
            agent.setVersion(registrationData.getVersion());
        } else {
            // Create a new agent with the information we have so far:
            agent = new AgentDO();
            
            // set the host and type
            agent.setHost(registrationData.getHost());
            agent.setType(registrationData.getType());
            agent.setLastHeartbeat(null);
            agent.setIsPushReady(false);
            agent.setPushPort(AgentDO.NO_PUSH_PORT);
            agent.setVersion(registrationData.getVersion());

            //new agent is not registered until I set the agent and comm profiles 
            agent.setRegistered(false);
        }
        
        if(!agent.isRegistered()){
            // Assign the default profiles if the agent hasn't registered
            try {
                final ICommProfileDO defaultCommProfile = getProfileMgr().getDefaultCommProfile(
                    agent.getType());
                final IAgentProfileDO defaultAgentProfile = getProfileMgr()
                                                            .getDefaultAgentProfile();
                agent.setAgentProfile(defaultAgentProfile);
                agent.setCommProfile(defaultCommProfile);
            } catch (DataSourceException e) {
                throw new PersistenceException(e);
            }
            agent.setRegistered(true);
        }
        // Add a registration record to this agent:
        IAgentRegistrationDO newRegistration = agent.addNewRegistration();

        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            s.saveOrUpdate(agent);
            t.commit();
        } catch (JDBCException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            if(SQLHelper.isDuplicateIdException(e.getSQLException())){
                throw new PersistenceException("The agent is already registered. Host = "
                                               + agent.getHost() + ", type = " + agent.getType(), e);
            }else{
                throw new PersistenceException("Failed to store agent", e);
            }
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException("Failed to store agent", e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }

        // Create the configuration to return to the agent - by now we should
        // have a persisted agent record:
        AgentStartupConfiguration configuration = new AgentStartupConfiguration();
        configuration.setAgentProfile(agent.getAgentProfile());
        configuration.setCommProfile(agent.getCommProfile());
        configuration.setId(agent.getId());
        configuration.setRegistrationId(newRegistration.getId());

        if (getLog().isDebugEnabled()) {
            getLog().debug("Succesfully registered agent id - (" + agent.getId() + ")");
        }

        return configuration;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#unregisterAgent(Long)
     */
    public void unregisterAgent(Long id) throws PersistenceException, InvalidIDException {
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            AgentDO agentToUnregister = (AgentDO) s.get(AgentDO.class, id);
            if (agentToUnregister == null) {
                throw new InvalidIDException(id, null);
            }
            agentToUnregister.setRegistered(false);
            // Note that if the agent is active, we keep it that way. This allow
            // the agent to recieve push requests even if explicily unregistered
            // from administrator
            s.update(agentToUnregister);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * Processes the startup of an agent
     * 
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#startupAgent(java.lang.Long,
     *      com.bluejungle.destiny.container.shared.agentmgr.IStartupAgentData)
     */
    public void startupAgent(Long id, IAgentStartupData startupData) throws PersistenceException, InvalidIDException {
        if (id == null) {
            throw new NullPointerException("agent id is null");
        }

        if (startupData == null) {
            throw new NullPointerException("startup data is null");
        }

        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            AgentDO agentToStart = (AgentDO) s.get(AgentDO.class, id);
            if (agentToStart == null) {
                throw new InvalidIDException(id, null);
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("Starting-up agent - (" + id + ") with push port - (" + startupData.getPushPort() + ")");
            }

            if (startupData.getPushPort() != null) {
                agentToStart.setIsPushReady(true);
                agentToStart.setPushPort(startupData.getPushPort());
            } else {
                // Just in case the agent calls startup without calling shutdown
                agentToStart.setIsPushReady(false);
                agentToStart.setPushPort(AgentDO.NO_PUSH_PORT);
            }

            agentToStart.setRegistered(true);
            s.update(agentToStart);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    private static final String OUT_OF_DATE_MESSAGE = "%s profile is out-of-date on agent {%d}. " +
    "Sending updated profile.";
    
    private static final String AGENT_ID_IS_NULL_MESSAGE = "agent id is null";
    
    private static final String ROW_COUNT_NOT_ONE_MESSAGE = "Row count should be 1 but it is %d.";
    
    private static final String AGENT_ID_HQL_KEYWORD = "agentid";
    
    //don't change the order, the order is hardcoded in checkUpdates(Long, IAgentHeartbeatData)
    private static final String CHECK_UPDATE_HQL_SELECT_LITE = 
    "select " 
    + "a.profileStatus, "
    + "a.policyAssemblyStatus, " 
    + "cp.name, " 
    + "cp.modifiedDate, " 
    + "ap.name, "
    + "ap.modifiedDate " 
    + "from AgentDO a " 
    + "join a.commProfile as cp "
    + "join a.agentProfile as ap " 
    + "where a.id = :" + AGENT_ID_HQL_KEYWORD;
    
    
    private static final String HQL_SELECT_PROFILES = 
    "select " 
    + "a.agentProfile, " 
    + "a.commProfile "
    + "from AgentDO a " 
    + "where a.id = :" + AGENT_ID_HQL_KEYWORD;
    
    private static final String HQL_SELECT_AGENT_PROFILE = 
    "select " 
    + "a.agentProfile " 
    + "from AgentDO a " 
    + "where a.id = :" + AGENT_ID_HQL_KEYWORD;
    
    private static final String HQL_SELECT_COMM_PROFILE = 
    "select " 
    + "a.commProfile "
    + "from AgentDO a " 
    + "where a.id = :" + AGENT_ID_HQL_KEYWORD;

    
    /**
     * Returns updated information for an agent - including perhaps updated
     * profiles etc.
     * 
     * @param heartbeatData
     *            containing any updates that the agent wants to tell the server
     * @return updates to the agent
     * @throws InvalidIDException
     */
    public IAgentUpdates checkUpdates(Long id, IAgentHeartbeatData heartbeatData)
        throws PersistenceException, InvalidIDException {
        final boolean debug = getLog().isDebugEnabled();
        final Log log = getLog();
     
        if (id == null) {
            throw new NullPointerException(AGENT_ID_IS_NULL_MESSAGE);
        }

        if (heartbeatData == null) {
            throw new NullPointerException("heartbeat data is null");
        }

        final AgentUpdate update = new AgentUpdate();
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            
            final Query liteQuery = s.createQuery(CHECK_UPDATE_HQL_SELECT_LITE);
            liteQuery.setParameter(AGENT_ID_HQL_KEYWORD, id);
            final Object[] row = (Object[])liteQuery.uniqueResult();
            if (row == null) {
                throw new InvalidIDException(id, null);
            }
            
            AgentProfileStatus profileStatus = (AgentProfileStatus) row[0];
            AgentPolicyAssemblyStatus knownPolicyAssemblyStatus = (AgentPolicyAssemblyStatus) row[1];
            String commProfileName = (String) row[2];
            Calendar commProfileTS = (Calendar) row[3];
            String agentProfileName = (String) row[4];
            Calendar agentProfileTS = (Calendar) row[5];
            
            if(debug){
                log.info("Received heartbeat/checkUpdates from agent - (" + id + ")");
            }

            this.heartbeatRecorder.recordHeartbeat(heartbeatData);
            IAgentProfileStatusData profileStatusData = heartbeatData.getProfileStatus();

            // Obtain the persisted profile status - create if it doesn't already exist:         
            if (profileStatus == null) {
                profileStatus = new AgentProfileStatus();
            }

            if (debug) {
                log.debug("Checking profile status acknowledgement from agent {" + id + "} ...");
            }
            
            boolean putCommProfileToUpdate = false;
            boolean putAgentProfileToUpdate = false;
            
            if (profileStatusData != null) {
                // Handle comm profile updates:
                Calendar lastCommProfileUpdateTS = profileStatusData.getLastCommittedCommProfileTimestamp();
                String lastCommProfileUpdateName = profileStatusData.getLastCommittedCommProfileName();
                // If acknowledgement data is available, we persist and check if
                // it is outdated:
                if ((lastCommProfileUpdateName != null) && (lastCommProfileUpdateTS != null)) {
                    // Persist acknowledgement
                    profileStatus.setLastAcknowledgedCommProfileTimestamp(lastCommProfileUpdateTS);
                    profileStatus.setLastAcknowledgedCommProfileName(profileStatusData
                                                                     .getLastCommittedCommProfileName());

                    // If not up-to-date, update:
                    if (!profileStatus.isCommProfileUpToDateWith(commProfileName, commProfileTS)) {
                        if (debug) {
                            log.debug(String.format(OUT_OF_DATE_MESSAGE, "Comm", id));
                        }
                        putCommProfileToUpdate = true;
                    }
                } else { // Agent is assumed to have outdated comm profile:
                    if (debug) {
                        log.debug(String.format(OUT_OF_DATE_MESSAGE, "Comm", id));
                    }
                    putCommProfileToUpdate = true;
                }

                // Handle agent profile updates:
                Calendar lastAgentProfileUpdateTS = profileStatusData.getLastCommittedAgentProfileTimestamp();
                String lastAgentProfileUpdateName = profileStatusData.getLastCommittedAgentProfileName();
                // If acknowledgement data is available, we persist and check if
                // it is outdated:
                if ((lastAgentProfileUpdateName != null) && (lastAgentProfileUpdateTS != null)) {
                    // Persist acknowledgement
                    profileStatus.setLastAcknowledgedAgentProfileTimestamp(lastAgentProfileUpdateTS);
                    profileStatus.setLastAcknowledgedAgentProfileName(profileStatusData
                                                                      .getLastCommittedAgentProfileName());

                    // If not up-to-date, update:
                    if (!profileStatus.isAgentProfileUpToDateWith(agentProfileName, agentProfileTS)) {
                        if (debug) {
                            log.debug(String.format(OUT_OF_DATE_MESSAGE, "Agent", id));
                        }
                        putAgentProfileToUpdate = true;
                    }
                } else { // Agent is assumed to have outdated agent profile:
                    if (debug) {
                        log.debug(String.format(OUT_OF_DATE_MESSAGE, "Agent", id));
                    }
                    putAgentProfileToUpdate = true;
                }
            } else { // Agent is assumed to have outdated data:
                if (debug) {
                    log.debug("Agent {" + id + "} doesn't seem to have any profiles."
                              + " Sending all profiles.");
                }
                putCommProfileToUpdate = true;
                putAgentProfileToUpdate = true;
            }
            
            // Handle policy assembly update here:
            if (debug) {
                log.debug("Committing policy assembly status acknowledgement from agent {" + id + "}");
            }
            IAgentPolicyAssemblyStatusData policyAssemblyStatusData = heartbeatData.getPolicyAssemblyStatus();
            
            boolean updateAgentPolicyAssemblyStatus = false;
            if (policyAssemblyStatusData != null) {
                Calendar timestamp = policyAssemblyStatusData.getLastCommittedDeploymentBundleTimestamp();

                // Commit the status to the agent record ONLY if a valid timestamp
                // exists - otherwise we'd be overwriting valid status with null:
                if (timestamp != null) {
                    if (debug) {
                        log.debug("Committing policy update timestamp: '" + timestamp
                                  + "' for agent {" + id + "}");
                    }
                    if (knownPolicyAssemblyStatus == null) {
                        knownPolicyAssemblyStatus = new AgentPolicyAssemblyStatus();
                    }
                    knownPolicyAssemblyStatus.setLastAcknowledgedDeploymentBundleTimestamp(timestamp);
                    updateAgentPolicyAssemblyStatus = true;
                }
            }
           

            long lastHeartbeat = Calendar.getInstance().getTimeInMillis();

            StringBuilder updateQuery = new StringBuilder();
            updateQuery.append("UPDATE Agent SET registered = '1', lastHeartbeat = " + lastHeartbeat);
   
            String lastAcknowledgedAgentProfileName = profileStatus.getLastAcknowledgedAgentProfileName();
            if( lastAcknowledgedAgentProfileName != null ){
                updateQuery.append( " ,AGENT_PROFILE_NAME = '" + lastAcknowledgedAgentProfileName +"'");
            }
   
            Calendar lastAcknowledgedAgentProfileTimestamp = profileStatus.getLastAcknowledgedAgentProfileTimestamp();
            if( lastAcknowledgedAgentProfileTimestamp != null ){
                updateQuery.append( " ,AGENT_PROFILE_TS = " + lastAcknowledgedAgentProfileTimestamp.getTimeInMillis());
            }
   
            String lastAcknowledgedCommProfileName = profileStatus.getLastAcknowledgedCommProfileName();
            if( lastAcknowledgedCommProfileName != null ){
                updateQuery.append( " ,COMM_PROFILE_NAME = '" + lastAcknowledgedCommProfileName +"'");
            }
   
            Calendar lastAcknowledgedCommProfileTimestamp = profileStatus.getLastAcknowledgedCommProfileTimestamp();
            if( lastAcknowledgedCommProfileTimestamp != null ){
                updateQuery.append( " ,COMM_PROFILE_TS = " + lastAcknowledgedCommProfileTimestamp.getTimeInMillis());
            }
   
            if(updateAgentPolicyAssemblyStatus){
                long policyAssemblyStatus = knownPolicyAssemblyStatus
                                            .getLastAcknowledgedDeploymentBundleTimestamp().getTimeInMillis();
    
                updateQuery.append( " ,DEPLOYMENT_BUNDLE_TS = " + policyAssemblyStatus);
            }
   
            updateQuery.append(" WHERE id = " + id);
    
            // Register the heartbeat on the agent and save the acknowledgement data
            Connection connection = s.connection();
            Statement statement = connection.createStatement();
            int rowCount = statement.executeUpdate(updateQuery.toString());
            
            if (rowCount != 1) {
                throw new PersistenceException(String.format(ROW_COUNT_NOT_ONE_MESSAGE, rowCount), null);
            }
            
            //make sure the data is committed before get the comm and agent profiles/ 
            connection.commit();
            
            //update agent before putting data into the return object
            if (putCommProfileToUpdate || putAgentProfileToUpdate) {
                //expensive call!!!
                //             AgentDO agent = (AgentDO) s.get(AgentDO.class, id);
             
                if (putCommProfileToUpdate && putAgentProfileToUpdate) {
                    Query profileQuery = s.createQuery(HQL_SELECT_PROFILES);
                    profileQuery.setParameter(AGENT_ID_HQL_KEYWORD, id);
                    Object[] profileRow = (Object[]) profileQuery.uniqueResult();
                    update.setAgentProfileUpdate((AgentProfileDO) profileRow[0]);
                    update.setCommProfileUpdate((CommProfileDO) profileRow[1]);
                } else if (putCommProfileToUpdate) {
                    Query profileQuery = s.createQuery(HQL_SELECT_COMM_PROFILE);
                    profileQuery.setParameter(AGENT_ID_HQL_KEYWORD, id);
                    update.setCommProfileUpdate((CommProfileDO)  profileQuery.uniqueResult());
                } else { //putAgentProfileToUpdate
                    Query profileQuery = s.createQuery(HQL_SELECT_AGENT_PROFILE);
                    profileQuery.setParameter(AGENT_ID_HQL_KEYWORD, id);
                    update.setAgentProfileUpdate((AgentProfileDO)  profileQuery.uniqueResult());
                }
            }
            
            t.commit();
        } catch (Exception e) {
            log.error(e);
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
        return update;
    }


    private static final String ACKNOWLEDGE_UPDATES_HQL_SELECT_LITE = 
    "select " 
    + "a.profileStatus, "
    + "a.policyAssemblyStatus " 
    + "from AgentDO a " 
    + "join a.commProfile as cp "
    + "join a.agentProfile as ap " 
    + "where a.id = :" + AGENT_ID_HQL_KEYWORD;
    
    private static final String  ACKNOWLEDGE_UPDATES_SQL_UPDATE_AGENT = 
    "UPDATE Agent SET "
    + "AGENT_PROFILE_NAME = '%s', "
    + "AGENT_PROFILE_TS = %d, " 
    + "COMM_PROFILE_NAME = '%s', " 
    + "COMM_PROFILE_TS = %d "
    + "WHERE id = %d";

    private static final String  ACKNOWLEDGE_UPDATES_SQL_UPDATE_AGENT_AND_BUNDLE_TS = 
    "UPDATE Agent SET "
    + "AGENT_PROFILE_NAME = '%s', "
    + "AGENT_PROFILE_TS = %d, " 
    + "COMM_PROFILE_NAME = '%s', " 
    + "COMM_PROFILE_TS = %d, "
    + "DEPLOYMENT_BUNDLE_TS = %d " 
    + "WHERE id = %d";
 
    private static final String COMMITTING_PROFILE_MESSAGE = 
    "Committing %s profile update acknowledgement with name: '%s' and timestamp: '%tT' from agent {%d}";
    
    /**
     * Processes the acknowledgement data sent by the agent back to the server
     * 
     * @param acknowledgementData
     */
    public void acknowledgeUpdates(Long id, IAgentUpdateAcknowledgementData acknowledgementData)
        throws PersistenceException, InvalidIDException {
        final boolean debug = getLog().isDebugEnabled();
        final Log log = getLog();
     
        if (id == null) {
            throw new NullPointerException(AGENT_ID_IS_NULL_MESSAGE);
        }

        if (acknowledgementData == null) {
            throw new NullPointerException("acknowledgement data is null");
        }
        
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            
            final Query liteQuery = s.createQuery(ACKNOWLEDGE_UPDATES_HQL_SELECT_LITE);
            liteQuery.setParameter(AGENT_ID_HQL_KEYWORD, id);
            final Object[] row = (Object[])liteQuery.uniqueResult();
            if (row == null) {
                throw new InvalidIDException(id, null);
            }
            
            AgentProfileStatus knownProfileStatus = (AgentProfileStatus) row[0];
            AgentPolicyAssemblyStatus knownPolicyAssemblyStatus = (AgentPolicyAssemblyStatus) row[1];
            
            if (debug) {
                log.info("Received acknowledgement message from agent - (" + id + ")");
            }

            if (knownProfileStatus == null) {
                knownProfileStatus = new AgentProfileStatus();
            }

            // Update profile status:
            IAgentProfileStatusData profileStatusData = acknowledgementData.getProfileStatus();
            if (debug) {
                log.debug("Checking profile update acknowledgement from agent {" + id + "} ...");
            }
            if (profileStatusData != null) {
                Calendar lastCommProfileUpdateTS = profileStatusData.getLastCommittedCommProfileTimestamp();
                String lastCommProfileUpdateName = profileStatusData.getLastCommittedCommProfileName();
                if ((lastCommProfileUpdateTS != null) && (lastCommProfileUpdateName != null)) {
                    String profileName = profileStatusData.getLastCommittedCommProfileName();
                    if (profileName != null) {
                        if (debug) {
                            log.debug(String.format(COMMITTING_PROFILE_MESSAGE, "comm",
                                                    profileName, lastCommProfileUpdateTS, id));
                        }
                        knownProfileStatus.setLastAcknowledgedCommProfileTimestamp(lastCommProfileUpdateTS);
                        knownProfileStatus.setLastAcknowledgedCommProfileName(profileName);
                    }
                }

                Calendar lastAgentProfileUpdateTS = profileStatusData.getLastCommittedAgentProfileTimestamp();
                String lastAgentProfileUpdateName = profileStatusData.getLastCommittedAgentProfileName();
                if ((lastAgentProfileUpdateTS != null) && (lastAgentProfileUpdateName != null)) {
                    String profileName = profileStatusData.getLastCommittedAgentProfileName();
                    if (profileName != null) {
                        if (debug) {
                            log.debug(String.format(COMMITTING_PROFILE_MESSAGE, "agent",
                                                    profileName, lastAgentProfileUpdateTS, id));
                        }
                        knownProfileStatus.setLastAcknowledgedAgentProfileTimestamp(lastAgentProfileUpdateTS);
                        knownProfileStatus.setLastAcknowledgedAgentProfileName(profileName);
                    }
                }
            }

            // Update Policy Assembly status:
            if (debug) {
                log.debug("Committing policy assembly update acknowledgement from agent {" + id + "} ...");
            }
            
            boolean updateAgentPolicyAssemblyStatus = false;
            IAgentPolicyAssemblyStatusData policyAssemblyStatusData = acknowledgementData
                                                                      .getPolicyAssemblyStatus();
            if (policyAssemblyStatusData != null) {
                Calendar timestamp = policyAssemblyStatusData.getLastCommittedDeploymentBundleTimestamp();

                // Commit the status to the agent record ONLY if a valid
                // timestamp
                // exists - otherwise we'd be overwriting valid status with null:
                if (timestamp != null) {
                    if (debug) {
                        log.debug("Committing policy update timestamp: '" + timestamp + "' for agent {" + id + "}");
                    }
                    if (knownPolicyAssemblyStatus == null) {
                        knownPolicyAssemblyStatus = new AgentPolicyAssemblyStatus();
                    }
                    knownPolicyAssemblyStatus.setLastAcknowledgedDeploymentBundleTimestamp(timestamp);
                    updateAgentPolicyAssemblyStatus = true; // Fix for Bug 7918.  
                    // Bundle timestamp must be updated whenever I do setLastAcknowledgedDeploymentBundleTimestamp()
                }
            }
            t = s.beginTransaction();
            
            String lastAcknowledgedAgentProfileName = knownProfileStatus
                                                      .getLastAcknowledgedAgentProfileName();
            long lastAcknowledgedAgentProfileTimestamp = knownProfileStatus
                                                         .getLastAcknowledgedAgentProfileTimestamp().getTimeInMillis();
            String lastAcknowledgedCommProfileName = knownProfileStatus
                                                     .getLastAcknowledgedCommProfileName();
            long lastAcknowledgedCommProfileTimestamp = knownProfileStatus
                                                        .getLastAcknowledgedCommProfileTimestamp().getTimeInMillis();
      
 
            final String updateQuery;
            if(updateAgentPolicyAssemblyStatus){
                long policyAssemblyStatus = knownPolicyAssemblyStatus
                                            .getLastAcknowledgedDeploymentBundleTimestamp().getTimeInMillis();
    
                updateQuery = String.format(ACKNOWLEDGE_UPDATES_SQL_UPDATE_AGENT_AND_BUNDLE_TS, 
                                            lastAcknowledgedAgentProfileName, 
                                            lastAcknowledgedAgentProfileTimestamp,
                                            lastAcknowledgedCommProfileName,
                                            lastAcknowledgedCommProfileTimestamp,
                                            policyAssemblyStatus,
                                            id);
            }else{
                updateQuery = String.format(ACKNOWLEDGE_UPDATES_SQL_UPDATE_AGENT, 
                                            lastAcknowledgedAgentProfileName, 
                                            lastAcknowledgedAgentProfileTimestamp,
                                            lastAcknowledgedCommProfileName,
                                            lastAcknowledgedCommProfileTimestamp,
                                            id);
            }
            
            Connection connection = s.connection();
            Statement statement = connection.createStatement();
            int rowCount = statement.executeUpdate(updateQuery);
            if (rowCount != 1) {
                throw new PersistenceException(String.format(ROW_COUNT_NOT_ONE_MESSAGE, rowCount), null);
            }
            
            connection.commit();
            t.commit();
        } catch (Exception e) {
            log.error(e);
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * Processes the shutdown of an agent
     * 
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#shutdownAgent(java.lang.Long,
     *      com.bluejungle.destiny.container.shared.agentmgr.IShutdownAgentData)
     */
    public void shutdownAgent(Long id, IAgentShutdownData shutdownData) throws PersistenceException, InvalidIDException {
        if (id == null) {
            throw new NullPointerException("agent id is null");
        }

        if (shutdownData == null) {
            throw new NullPointerException("startup data is null");
        }

        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            AgentDO agentToShutdown = (AgentDO) s.get(AgentDO.class, id);
            if (agentToShutdown == null) {
                throw new InvalidIDException(id, null);
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("Shuttind down agent - (" + id + ")");
            }

            // Mark the agent as inactive and reset the push information and
            // logged-in-user status:
            agentToShutdown.setPushPort(AgentDO.NO_PUSH_PORT);
            agentToShutdown.setIsPushReady(false);
            s.update(agentToShutdown);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }

    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#getAgents(com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec)
     */
    public IAgentQueryResults getAgents(IAgentMgrQuerySpec agentSpec) throws PersistenceException, InvalidQuerySpecException {
        if (agentSpec == null) {
            throw new NullPointerException("agentSpec cannot be null.");
        }

        List agentsToReturn = null;

        Conjunction queryTermConjuction = Expression.conjunction();
        IAgentMgrQueryTerm[] queryTerms = agentSpec.getSearchSpecTerms();
        for (int i = 0; i < queryTerms.length; i++) {
            IHibernateAgentMgrQueryTerm nextQueryTerm = (IHibernateAgentMgrQueryTerm) queryTerms[i];
            Criterion queryTermCriterion = nextQueryTerm.getCriterion();
            queryTermConjuction.add(queryTermCriterion);
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getDataSource().getCountedSession();
            transaction = hSession.beginTransaction();
            Criteria agentQueryCriteria = hSession.createCriteria(AgentDO.class);
            agentQueryCriteria.add(queryTermConjuction);

            IAgentMgrSortTerm[] sortTerms = agentSpec.getSortSpecTerms();
            for (int i = 0; i < sortTerms.length; i++) {
                IAgentMgrSortTerm nextSortTerm = sortTerms[i];
                String fieldForSort = SORT_FIELD_TO_PROP_MAP.get(nextSortTerm.getFieldName());
                if (fieldForSort == null) {
                    throw new IllegalArgumentException("Unknown sort field, " + nextSortTerm.getFieldName());
                }
                Order sortOrderToAdd;
                if (nextSortTerm.isAscending()) {
                    sortOrderToAdd = Order.asc(fieldForSort);
                } else {
                    sortOrderToAdd = Order.desc(fieldForSort);
                }

                agentQueryCriteria.addOrder(sortOrderToAdd);
            }

            int maxResults = agentSpec.getLimit();
            if (maxResults > 0) {
                agentQueryCriteria.setMaxResults(maxResults);
            }
            agentsToReturn = agentQueryCriteria.list();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }

        return new AgentQueryResultsImpl(agentsToReturn);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#getAgentMgrQueryTermFactory()
     */
    public IAgentMgrQueryTermFactory getAgentMgrQueryTermFactory() {
        return QUERY_TERM_FACTORY;
    }

    /**
     * Deploys an agent.
     * 
     * @param id
     * @param profileName
     * @throws InvalidIDException
     * @throws ProfileNotFoundException
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#setCommProfile(long,
     *      java.lang.String)
     */
    public void setCommProfile(Long id, String profileName) throws PersistenceException, ProfileNotFoundException, InvalidIDException {
        if (id == null) {
            throw new NullPointerException("id is null");
        }

        if (profileName == null) {
            throw new NullPointerException("profile name is null");
        }

        // Make sure the record exists
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            final AgentDO agent = (AgentDO) s.get(AgentDO.class, id);
            if (agent == null) {
                throw new InvalidIDException(id, null);
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("Setting communication profile message for agent - (" + id + ") to '" + profileName + "'");
            }

            // Set the profile:
            final ICommProfileDO oldProfile = agent.getCommProfile();
            if (!oldProfile.getName().equals(profileName)) {
                final ICommProfileDO newProfile = getProfileMgr().retrieveCommProfileByName(profileName);
                agent.setCommProfile(newProfile);
                t = s.beginTransaction();
                s.update(agent);
                t.commit();
            }
        } catch (DataSourceException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#setCommProfileForAgents(java.util.Set,
     *      java.lang.Long)
     */
    public void setCommProfileForAgents(Set agentIDs, Long profileId) throws ProfileNotFoundException, PersistenceException {
        if (agentIDs == null) {
            throw new NullPointerException("agentIDsAsLongs cannot be null.");
        }

        if (profileId == null) {
            throw new NullPointerException("profileId cannot be null.");
        }

        ICommProfileDO newProfile = null;
        try {
            newProfile = getProfileMgr().retrieveCommProfileByID(profileId.longValue());
        } catch (DataSourceException exception) {
            throw new PersistenceException(exception);
        }

        StringBuffer agentsToUpdateQueryString = new StringBuffer(AGENT_QUERY_MATCHING_AGENTS_PREFIX);

        // Use where and or's to avoid IN limit length in databases.
        Iterator agentIdIterator = agentIDs.iterator();
        for (int i = 0; agentIdIterator.hasNext(); i++) {
            if (i == 0) {
                agentsToUpdateQueryString.append(" where ");
            } else {
                agentsToUpdateQueryString.append(" or ");
            }

            Long nextAgentId = (Long) agentIdIterator.next();
            agentsToUpdateQueryString.append("id = ").append(nextAgentId.longValue());
        }

        Session session = null;
        Transaction transaction = null;
        try {
            session = getDataSource().getCountedSession();
            Query agentsToUpdateQuery = session.createQuery(agentsToUpdateQueryString.toString());
            ScrollableResults agentsToUpdate = agentsToUpdateQuery.scroll();
            List<AgentDO> evictionList = new LinkedList<AgentDO>();

            transaction = session.beginTransaction();
            int iterationCount = 0;
            while (agentsToUpdate.next()) {
                iterationCount++;
                AgentDO nextAgentToUpdate = (AgentDO) agentsToUpdate.get(0);
                nextAgentToUpdate.setCommProfile(newProfile);
                session.saveOrUpdateCopy(nextAgentToUpdate);
                evictionList.add(nextAgentToUpdate);
                if (iterationCount % HIBERNATE_SESSION_FLUSH_THRESHOLD == 0) {
                    session.flush();
                    HibernateUtils.evictObjects(session, evictionList);
                }
            }

            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * Sets an agent profile for an agent
     * 
     * @param id
     * @param profileName
     * @throws InvalidIDException
     * @throws ProfileNotFoundException
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#setAgentProfile(long,
     *      java.lang.String)
     */
    public void setAgentProfile(Long id, String profileName) throws PersistenceException, ProfileNotFoundException, InvalidIDException {
        if (id == null) {
            throw new NullPointerException("id is null");
        }

        if (profileName == null) {
            throw new NullPointerException("profile name is null");
        }

        // Make sure the record exists
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            final AgentDO agent = (AgentDO) s.get(AgentDO.class, id);
            if (agent == null) {
                throw new InvalidIDException(id, null);
            }

            if (getLog().isInfoEnabled()) {
                getLog().info("Setting agent profile message for agent - (" + id + ") to '" + profileName + "'");
            }

            // Set the profile:
            final IAgentProfileDO oldProfile = agent.getAgentProfile();
            if (!oldProfile.getName().equals(profileName)) {
                IAgentProfileDO newProfile = getProfileMgr().retrieveAgentProfileByName(profileName);
                agent.setAgentProfile(newProfile);
                t = s.beginTransaction();
                s.update(agent);
                t.commit();
            }
        } catch (DataSourceException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * Deletes the agent with the given id
     * 
     * @param id
     * @throws PersistenceException
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#removeAgent(long)
     */
    public void deleteAgent(Long id) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id is null");
        }

        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getCountedSession();
            t = s.beginTransaction();
            int nbDeleted = s.delete("from AgentDO a where a.id = " + id);
            t.commit();
            if (nbDeleted == 0) {
                getLog().warn("Did not find an agent with id '" + id + "' to delete");
            }
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new RuntimeException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }
    }

    /**
     * Returns agent information for an agentid
     * 
     * @param id
     * @return IAgentDO
     * @throws PersistenceException
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentManager#getAgentById(long)
     */
    public IAgentDO getAgentById(Long id) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id is null");
        }

        getLog().info("Received query for agent with id - (" + id + ")");

        Session s = null;
        IAgentDO result = null;
        try {
            s = getDataSource().getCountedSession();
            result = (IAgentDO) s.get(AgentDO.class, id);
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        } finally {
            HibernateUtils.closeSession(getDataSource(), getLog());
        }

        if (result == null) {
            getLog().fatal("!!!Agent with id - (" + id + ") does not exist");
        }
        return result;
    }

    /**
     * Returns statistics about the current agents
     * 
     * @return statistics about the current agents
     * @throws PersistenceException
     *             if the statistic calculation fails.
     */
    public IAgentStatistics getAgentStatistics() throws PersistenceException {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();        
        IAgentStatisticsCollector agentStatisticsCollector = (IAgentStatisticsCollector) componentManager.getComponent(IAgentStatisticsCollector.COMP_NAME);
        return agentStatisticsCollector.collectStatistics();
    }

    /**
     * This class holds startup information for an agent that is returned after
     * receiving a registration request from a freshly installed agent.
     * 
     * @author safdar
     */
    private class AgentStartupConfiguration implements IAgentStartupConfiguration {

        private Long id;
        private Long registrationId;
        private ICommProfileDO commProfile;
        private IAgentProfileDO agentProfile;

        /**
         * Returns the agentProfile.
         * 
         * @return the agentProfile.
         */
        public IAgentProfileDO getAgentProfile() {
            return this.agentProfile;
        }

        /**
         * Sets the agentProfile
         * 
         * @param agentProfile
         *            The agentProfile to set.
         */
        public void setAgentProfile(IAgentProfileDO agentProfile) {
            this.agentProfile = agentProfile;
        }

        /**
         * Returns the commProfile.
         * 
         * @return the commProfile.
         */
        public ICommProfileDO getCommProfile() {
            return this.commProfile;
        }

        /**
         * Sets the commProfile
         * 
         * @param commProfile
         *            The commProfile to set.
         */
        public void setCommProfile(ICommProfileDO commProfile) {
            this.commProfile = commProfile;
        }

        /**
         * Returns the id.
         * 
         * @return the id.
         */
        public Long getId() {
            return this.id;
        }

        /**
         * Sets the id
         * 
         * @param id
         *            The id to set.
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration#getRegistrationId()
         */
        public Long getRegistrationId() {
            return this.registrationId;
        }

        /**
         * Sets the registrationId
         * 
         * @param registrationId
         *            The registrationId to set.
         */
        public void setRegistrationId(Long registrationId) {
            this.registrationId = registrationId;
        }
    }

    /**
     * This class holds update information for an agent that is returned after
     * receiving a heartbeat from the agent.
     * 
     * @author safdar
     */
    private class AgentUpdate implements IAgentUpdates {

        /*
         * Private variables
         */
        private ICommProfileDO commProfileUpdate;
        private IAgentProfileDO agentProfileUpdate;

        /**
         * Constructor
         * 
         */
        public AgentUpdate() {
            this.commProfileUpdate = null;
            this.agentProfileUpdate = null;
        }

        /**
         * Sets the comm profile to update on the agent
         * 
         * @param profile
         */
        public void setCommProfileUpdate(ICommProfileDO profile) {
            this.commProfileUpdate = profile;
        }

        /**
         * Returns the comm profile to be sent to the agent
         * 
         * @return profile
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates#getCommProfileUpdate()
         */
        public ICommProfileDO getCommProfileUpdate() {
            return this.commProfileUpdate;
        }

        /**
         * Sets the agent profile to be sent to the agent
         * 
         * @param profile
         */
        public void setAgentProfileUpdate(IAgentProfileDO profile) {
            this.agentProfileUpdate = profile;
        }

        /**
         * Returns the comm profile to be sent to the agent
         * 
         * @return profile
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates#getAgentProfileUpdate()
         */
        public IAgentProfileDO getAgentProfileUpdate() {
            return this.agentProfileUpdate;
        }

        /**
         * Returns whether there are any updates contained in this object
         * 
         * @return whether there are any updates
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates#hasUpdates()
         */
        public boolean hasUpdates() {
            if ((this.commProfileUpdate != null) || (this.agentProfileUpdate != null)) {
                return true;
            } else
                return false;
        }
    }

    private static class AgentTypeImpl implements IAgentType {
        private Set<IActionType> actions;
        private String title;
        private String id;
        
        
        /**
         * Create an instance of AgentTypeImpl
         * @param id
         * @param title
         * @param actions
         */
        private AgentTypeImpl(String id, String title, Set<IActionType> actions) {
            this.id = id;
            this.title = title;
            this.actions = actions;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentType#getActionTypes()
         */
        public Set<IActionType> getActionTypes() {
            return this.actions;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentType#getId()
         */
        public String getId() {
            return this.id;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentType#getTitle()
         */
        public String getTitle() {
            return this.title;
        }       
    }
    
    private static class ActionTypeImpl implements IActionType {
        private String title;
        private String id;
        private IActivityJournalingAuditLevel activityJournalingAuditLevel;
                
        /**
         * Create an instance of ActionTypeImpl
         * @param id
         * @param title
         * @param level
         */
        private ActionTypeImpl(String id, String title, StandardActivityJournalingLevel level) {
            this.id = id;
            this.title = title;
            this.activityJournalingAuditLevel = level;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActionType#getActivityJournalingAuditLevel()
         */
        public IActivityJournalingAuditLevel getActivityJournalingAuditLevel() {
            return this.activityJournalingAuditLevel;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActionType#getId()
         */
        public String getId() {
            return this.id;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActionType#getTitle()
         */
        public String getTitle() {
            return this.title;
        }        
    }
    
    private enum StandardActivityJournalingLevel implements IActivityJournalingAuditLevel {
        EXTENDED ("Extended", "Extended", 2),
            DEFAULT ("Default", "Default", 1),
            MINIMUM ("Minimal", "Minimal", 0);
        
        private String id;
        private String title;
        private int ordinal;
        
        private StandardActivityJournalingLevel(String id, String title, int ordinal) {
            this.id = id;
            this.title = title;
            this.ordinal = ordinal;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel#getId()
         */
        public String getId() {
            return this.id;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel#getOrdinal()
         */
        public int getOrdinal() {
            return this.ordinal;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IActivityJournalingAuditLevel#getTitle()
         */
        public String getTitle() {
            return this.title;
        }
    }
}
