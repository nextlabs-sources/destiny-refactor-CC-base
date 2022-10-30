/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.policyDeployMgr;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.rpc.ServiceException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.DeploymentEvents;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.RequestAlreadyExecutedException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.UnknownDeploymentIdException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.AgentProcessorDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.DeploymentRequestMgrImpl;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetAgentDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetHostDO;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.CommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.destiny.services.management.types.Component;
import com.bluejungle.destiny.services.management.types.ComponentList;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the implementation class for the Policy Deployment Manager.
 * 
 * @author ihanen
 */

public class PolicyDeployMgrImpl implements IPolicyDeployMgr, ILogEnabled, IConfigurable,
                                            IInitializable, IDisposable, IStartable {

    private static final int MAX_HIBERNATE_DATA_ITERATION = 25;
    private Log log;
    private IConfiguration config;
    private IHibernateRepository mgmtDataSource;
    private Timer deploymentTimer;
    private Map<Long, String> profileMap;
    private IProfileManager profileManager;
    private AgentHandlerMap agentHandlers = new AgentHandlerMap();

    private void assignAgentProcessors(Long deploymentId) throws HibernateException {
        agentHandlers.refresh();

        for (String component : agentHandlers.getAgentHandlers()) {
            assignAgentProcessors(deploymentId, component, agentHandlers.getProfileIds(component));
        }
    }

    /**
     * This function figures out which DABS component should be assigned to push
     * information to a given agent. The list of active DABS component is pulled
     * from the component manager, and the load of agents to process is evenly
     * distributed among them.
     * 
     * @param deploymentId
     *            id of the deployment request to process
     * @throws HibernateException
     *             if database issue occurs
     */
    private void assignAgentProcessors(Long deploymentId, String componentType, List<Long> profileIds) throws HibernateException {
        Session s = getManagementDataSource().getCurrentSession();

        // Find out the active agent processors in the system. The component
        // manager can give this information.
        List<Component> activeAgentProcessors = new ArrayList<Component>();
        Transaction t = null;
        try {
            final String baseQuery = "from TargetHostDO targetHost, TargetAgentDO targetAgent " +
                                     "where targetAgent.host.id = targetHost.id " +
                                     "AND targetHost.deploymentRequest.id =:deployReqId " +
                                     "AND targetAgent.agent.commProfile in (:commProfileIds)";

            ComponentList componentList = componentService.getComponentsByType(componentType);
            for(Component component : componentList.getComp()){
                activeAgentProcessors.add(component);
                getLog().trace("find activeAgentProcessors " + component.getName());
            }

            //Figure out the number of agents to process
            Query numberAgentsQ = s.createQuery("select count(targetAgent) " +
                                                baseQuery);
            numberAgentsQ.setLong("deployReqId", deploymentId.longValue());
            numberAgentsQ.setParameterList("commProfileIds", profileIds);

            int numberAgents = (Integer) numberAgentsQ.list().get(0);
            getLog().trace("total number of agents = " + numberAgents);

            int numberAgentProcessors = activeAgentProcessors.size();
            getLog().trace("total number of agent processors = " + numberAgentProcessors);
            
            //Now, split the list among all active agent processors. We round up so that
            // the last one gets less.
            double assignmentSize = Math.ceil( numberAgents / numberAgentProcessors);

            //Pull the list of agent. The query has the same number of results
            Query agentQuery = s.createQuery("select targetAgent " +
                                             baseQuery);
            agentQuery.setLong("deployReqId", deploymentId.longValue());
            agentQuery.setParameterList("commProfileIds", profileIds);

            ScrollableResults agents = agentQuery.scroll();
            agents.beforeFirst();
            List<TargetAgentDO> evictionList = new ArrayList<TargetAgentDO>();
            t = s.beginTransaction();
            long iterationCount = 0;
            for(Component activeAgent : activeAgentProcessors){
                iterationCount++;
                AgentProcessorDO currentProcessor = new AgentProcessorDO();
                currentProcessor.setId(activeAgent.getId());
                currentProcessor.setName(activeAgent.getName());
                for (int index = 0; index < assignmentSize; index++) {
                    if (agents.next()) {
                        TargetAgentDO targetAgent = (TargetAgentDO) agents.get(0);
                        targetAgent.setProcessor(currentProcessor);
                        s.update(targetAgent);
                        evictionList.add(targetAgent);
                        if (iterationCount % MAX_HIBERNATE_DATA_ITERATION == 0) {
                            s.flush();
                            HibernateUtils.evictObjects(s, evictionList);
                        }
                    }
                }
            }

            t.commit();
        } catch (RemoteException e){
            getLog().error("Error when assigning agent processors to target agents for push request '" + deploymentId + "'", e);
        } catch (HibernateException e) {
            getLog().error("Error when assigning agent processors to target agents for push request '" + deploymentId + "'", e);
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * This function generates an event notifying that a deployment request with
     * a push has to be processed.
     * 
     * @param id
     *            id of the deployment request to process
     */
    private void notifyPushPolicyUpdate(Long id) {
        getLog().info("Firing push policy update event for deployment request: '" + id + "'");
        final IDCCServerEvent pushEvent = new DCCServerEventImpl(DeploymentEvents.POLICY_PUSH_AVAILABLE);
        pushEvent.getProperties().setProperty(DeploymentEvents.POLICY_PUSH_ID_PROP, id.toString());
        sendEvent(pushEvent);
    }

    /**
     * This function is called when a policy update occured and when updates are
     * not sent (pushed) to each agent.
     * 
     * @see com.bluejungle.destiny.container.policyDeployMgr.IPolicyDeployMgr#notifyPolicyUpdate()
     */
    private void notifySimplePolicyUpdate() {
        getLog().info("Firing simple policy update event.");
        sendEvent(new DCCServerEventImpl(DeploymentEvents.POLICY_UPDATES_AVAILABLE));
    }

    /**
     * Sends an event to the event framework
     * 
     * @param event
     *            event to be sent
     */
    private void sendEvent(IDCCServerEvent event) {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr
                                               .getComponent(IDestinySharedContextLocator.COMP_NAME);
        locator.getSharedContext().getEventManager().fireEvent(event);
    }

    /**
     * This function is called before the component is destroyed. It performs
     * clean up tasks.
     */
    public void dispose() {
        config = null;
    }

    /**
     * Returns the configuration object for the component
     * 
     * @return the configuration object for the component
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * Returns the log object for the component
     * 
     * @return the log object for the component
     */
    public Log getLog() {
        return log;
    }

    /**
     * Returns the deployment request manager
     * 
     * @return the deployment request manager
     */
    private IDeploymentRequestMgr getDeploymentRequestMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDeploymentRequestMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, getManagementDataSource());
        ComponentInfo<IDeploymentRequestMgr> compInfo = 
            new ComponentInfo<IDeploymentRequestMgr>(
                "test",
            DeploymentRequestMgrImpl.class, 
            IDeploymentRequestMgr.class,
            LifestyleType.TRANSIENT_TYPE, config);
        IDeploymentRequestMgr deploymentReqMgr = compMgr.getComponent(compInfo);
        return deploymentReqMgr;
    }

    /**
     * Returns the management repository hibernate data source
     * 
     * @return the management repository hibernate data source
     */
    private IHibernateRepository getManagementDataSource() {
        return mgmtDataSource;
    }

    
    /**
     * @see com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestExecutor#executeDeploymentRequest(java.lang.Long)
     */
    public void executeDeploymentRequest(Long id) throws RequestAlreadyExecutedException,
                                                         UnknownDeploymentIdException {
        if (id == null) {
            throw new NullPointerException("The deployment request id cannot be null.");
        }

        //Make sure the deployment request has not already been processed
        IDeploymentRequestMgr reqMgr = getDeploymentRequestMgr();
        IDeploymentRequest deploymentRequest = reqMgr.getDeploymentRequest(id);

        if (deploymentRequest == null) {
            throw new UnknownDeploymentIdException(id.toString());
        }

        if (deploymentRequest.getExecuted()) {
            throw new RequestAlreadyExecutedException(id.toString());
        }
        
        Date deploymentTime = deploymentRequest.getScheduleTime().getTime();
        log.info("Deployment request " + deploymentRequest.getId()
                 + " received. Scheduled to deploy at " + deploymentTime);

        DeploymentScheduledTask task = new DeploymentScheduledTask(id);
        deploymentTimer.schedule(task, deploymentTime);
    }
    
    private class DeploymentScheduledTask extends TimerTask {
        private Long id;

        public DeploymentScheduledTask(Long id) {
            super();
            this.id = id;
        }

        /**
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
            log.info("Start deployment request " + id);
            try {
                resolveTargetAgents(id);
                updateMissedHostsAsFailed(id);
                assignAgentProcessors(id);
                notifyPushPolicyUpdate(id);
            } catch (HibernateException e) {
                //The assignment to the various DABS agents failed, so the policy
                // push will stop there. At least, let's use the regular deploy.
                getLog().error("policy deployment request'" + id + "' failed, downgrading to simple deployment");
                notifySimplePolicyUpdate();
            }
        }
    }


    private class AgentHandlerMap {
        private Map<String, List<Long>> agentHandlerMap = new HashMap<String, List<Long>>();

        public void refresh() {
            Map<String, String> componentURLTypeMap = new HashMap<String, String>();
            
            try {
                for(Component component : componentService.getComponents().getComp()){
                    componentURLTypeMap.put(component.getComponentURL(), component.getType());
                }
            } catch (RemoteException re) {
                getLog().error("Error when getting components list");

                throw new RuntimeException(re);
            }
            
            try {
                CommProfileQueryTermSet queryTermSet = new CommProfileQueryTermSet();
                
                List<CommProfileDO> commProfiles = (List<CommProfileDO>)profileManager.retrieveCommProfiles(queryTermSet, null, 0, null);
                
                for(CommProfileDO commProfile : commProfiles) {
                    add(componentURLTypeMap.get(commProfile.getDABSLocation().toString()),
                        commProfile.getId());
                }
            } catch (DataSourceException dse) {
                getLog().error("Exception reading comm profiles", dse);
                throw new RuntimeException(dse);
            }
        }

        public Set<String> getAgentHandlers() {
            return agentHandlerMap.keySet();
        }

        public List<Long> getProfileIds(String agentHandler) {
            return agentHandlerMap.get(agentHandler);
        }

        private void add(String component, Long id) {
            List<Long> ids = agentHandlerMap.get(component);

            if (ids == null) {
                ids = new ArrayList<Long>();
            }

            if (!ids.contains(id)) {
                ids.add(id);
                agentHandlerMap.put(component, ids);
            }
        }

    }

    /**
     * Initialization function
     */
    public void init() {
        mgmtDataSource = config.get(MGMT_DATA_SOURCE_CONFIG_PARAM);
        if (mgmtDataSource == null) {
            throw new NullPointerException("The management data source needs to be set in the " +
                                           "configuration for the deployment request Mgr.");
        }
        try {
            getComponentServiceInterface();
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        deploymentTimer = new Timer("PolicyDeploymentTimer");

        ComponentInfo<IProfileManager> profileMgrCompInfo = 
            new ComponentInfo<IProfileManager>(
                IProfileManager.COMP_NAME, 
                HibernateProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.SINGLETON_TYPE
        );

        profileManager = ComponentManagerFactory.getComponentManager().getComponent(profileMgrCompInfo);

    }

    /**
     * This function figures out what agents have to be involved in the
     * deployment process, based on the initial list of hostnames. The list of
     * hostnames is expanded to a list of matching agents that are currently
     * active on the list of hosts specified in the deployment request. The
     * hosts on which agents are not currently active or push enabled are marked
     * as failed in the deployment request status.
     * 
     * @param id
     *            id of the deployment request
     * @throws HibernateException
     *             if database issue occurs
     */
    private void resolveTargetAgents(Long id) throws HibernateException {
        // For now, we assume that the number of agents / hosts involved is
        // reasonnable. If we need to deal with huges sets of data, we'll use
        // raw SQL to produce mass updates. Mass updates are not supported by
        // Hibernate currently.

        // Query for the list of hosts to target, and take out the ones that do
        // not have any valid agents. First, mark the hosts that have agents as started.
        IHibernateRepository dataSource = getManagementDataSource();
        Session s = dataSource.getSession();
        Transaction t = null;

        try {
            Query q = s.createQuery("select targetHost, agent " +
                                    "from TargetHostDO targetHost, AgentDO agent " +
                                    "where agent.isPushReady='1' " +
                                    "AND agent.host= targetHost.hostname " +
                                    "AND targetHost.deploymentRequest.id =:deployReqId");
            q.setLong("deployReqId", id.longValue());
            ScrollableResults validHosts = q.scroll();
            validHosts.beforeFirst();
            List<Object> evictionList = new ArrayList<Object>();
            t = s.beginTransaction();

            long iterationCount = 0;
            while (validHosts.next()) {
                iterationCount++;
                TargetHostDO host = (TargetHostDO) validHosts.get(0);
                AgentDO agent = (AgentDO) validHosts.get(1);

                //Creates a new target agent entry for the valid agent.
                TargetAgentDO newTargetAgent = new TargetAgentDO();
                newTargetAgent.setAgent(agent);
                newTargetAgent.setHost(host);
                newTargetAgent.setStatus(ITargetStatus.NOT_STARTED);
                s.save(newTargetAgent);

                //Sets the host status to be in progress. If several agents sit
                //on the same host, the host status may be updated more than
                //once, but this should not happen very often.
                host.setStatus(ITargetStatus.IN_PROGRESS);
                s.save(host);

                evictionList.add(agent);
                evictionList.add(host);
                evictionList.add(newTargetAgent);
                if (iterationCount % MAX_HIBERNATE_DATA_ITERATION == 0) {
                    s.flush();
                    HibernateUtils.evictObjects(s, evictionList);
                }
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * This function queries for the hosts that are not going to be covered by
     * the push (and that should have) and mark them as failed. At this point,
     * since the target agents have been resolved, all the hosts that have
     * active agent should have a status marked as "In Progress". All the
     * remaining hosts, still marked as "Not Started" will be marked as
     * "Failed".
     * 
     * @param id
     *            id of the deployment request
     * @throws HibernateException
     *             if database issue occurs
     */
    private void updateMissedHostsAsFailed(Long id) throws HibernateException {
        IHibernateRepository dataSource = getManagementDataSource();
        Session s = dataSource.getSession();
        Transaction t = null;
        try {
            Query failedHosts = s.createQuery("select host from TargetHostDO host " +
                                              "where host.statusId='" + ITargetStatus.NOT_STARTED.getId() + "' " +
                                              "AND host.deploymentRequest.id=:deployReqId");
            failedHosts.setLong("deployReqId", id.longValue());
            ScrollableResults failedResults = failedHosts.scroll();
            failedResults.beforeFirst();
            long iterationCount = 0;
            List<TargetHostDO> evictionList = new ArrayList<TargetHostDO>();
            t = s.beginTransaction();
            while (failedResults.next()) {
                TargetHostDO failedHost = (TargetHostDO) failedResults.get(0);
                failedHost.setStatus(ITargetStatus.FAILED);
                s.save(failedHost);
                evictionList.add(failedHost);
                if (iterationCount % MAX_HIBERNATE_DATA_ITERATION == 0) {
                    s.flush();
                    HibernateUtils.evictObjects(s, evictionList);
                }
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * Sets the configuration object for the component
     * 
     * @param newConfig
     *            new configuration
     */
    public void setConfiguration(IConfiguration newConfig) {
        config = newConfig;
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
    
    private ComponentServiceIF componentService;
    private static final String COMPONENT_SERVICE_LOCATION_SERVLET_PATH = "/services/ComponentServiceIFPort";
    
    private ComponentServiceIF getComponentServiceInterface() throws ServiceException {
        if (componentService == null) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            IConfiguration mainCompConfig = (IConfiguration) compMgr.getComponent(IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME);
            String location = (String) mainCompConfig.get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);
            location += COMPONENT_SERVICE_LOCATION_SERVLET_PATH;
            ComponentServiceLocator locator = new ComponentServiceLocator();
            locator.setComponentServiceIFPortEndpointAddress(location);

            componentService = locator.getComponentServiceIFPort();
        }

        return componentService;
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
        //do nothing
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        deploymentTimer.cancel();
    }
}
