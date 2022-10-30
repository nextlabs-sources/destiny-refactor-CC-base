/*
 * Created on Aug 06, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/components/deployment/PolicyDeploymentManagerImpl.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

import java.lang.Throwable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.DeploymentEvents;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.RequestAlreadyExecutedException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetAgentDO;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Predicates.DefaultTransformer;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.DictionaryHelper;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.policymap.ReferenceResolver;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.domain.destiny.deployment.AgentRequest;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.nextlabs.destiny.container.ddac.DDACAgentServiceImpl;
import com.nextlabs.destiny.container.ddac.configuration.DDACActiveDirectoryConfiguration;
import com.nextlabs.destiny.container.ddac.configuration.DDACConfiguration;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessPolicy;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessRule;
import com.nextlabs.pf.destiny.formatter.DACDomainObjectFormatter;

public class PolicyDeploymentManagerImpl implements IPolicyDeploymentManager, IInitializable, IManagerEnabled, IDestinyEventListener, IConfigurable {
    private static final int MAX_HIBERNATE_ITERATIONS = 30;
    private IConfiguration configuration;
    private IDestinyEventManager eventMgr;
    private IComponentManager manager;
    private static final Log log = LogFactory.getLog(PolicyDeploymentManagerImpl.class.getName());
    private LifecycleManager lm;
    private DDACConfiguration ddacConfig;
    private DDACAgentServiceImpl agentServiceImpl;
    private IHibernateRepository mgmtDataSource;
    private DictionaryHelper dictionaryHelper;
    private String ddacName;
    private static final Constant NULL_SID = Constant.build("S-1-0-0");

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        // Obtain a handle to the event mgr:
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) getManager().getComponent(IDestinySharedContextLocator.COMP_NAME);
        eventMgr = locator.getSharedContext().getEventManager();
        
        lm = (LifecycleManager) getManager().getComponent(LifecycleManager.COMP_INFO);

        getManager().registerComponent(ServerSpecManager.COMP_INFO, true);
        ServerSpecManager serverSpecManager = getManager().getComponent(ServerSpecManager.COMP_INFO);

        getManager().registerComponent(Dictionary.COMP_INFO, true);
        IDictionary dictionary = getManager().getComponent(Dictionary.COMP_INFO);

        dictionaryHelper = new DictionaryHelper(serverSpecManager, dictionary);

        ddacConfig = getConfiguration().get(DDAC_CONFIGURATION);
        if (ddacConfig == null) {
            throw new NullPointerException("The DDAC configuration information must be specified");
        }

        agentServiceImpl = getConfiguration().get(DDAC_AGENT_SERVICE);
        if (agentServiceImpl == null) {
            throw new NullPointerException("The agent service mgr information must be specified");
        }

        mgmtDataSource = getConfiguration().get(MGMT_DATA_SOURCE);
        if (mgmtDataSource == null) {
            throw new NullPointerException("The management data source must be specified");
        }

        ddacName = getConfiguration().get(DDAC_COMPONENT_NAME);
        if (ddacName == null) {
            throw new NullPointerException("The DDAC component name must be specified");
        }

        registerForDeploymentEvents();

        try {
            initializeAllActiveDACPolicies();
        } catch (DDACDeploymentException e) {
            log.error("Unable to initialize active policies by endpoint", e);
        }

        Timer updateTimer = new Timer(true);

        int updateFrequencyInSecs = ddacConfig.getCheckUpdatesFrequency();

        if (updateFrequencyInSecs < 60) {
            updateFrequencyInSecs = 60;
        }

        updateTimer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   log.info("Checking for new DAC policies...");
                                   try {
                                       deployToAllAD();
                                   } catch (Throwable t) {
                                       log.error("Exception when deploying DAC policies: ", t);
                                   }
                               }
                             }, 60 * 1000, updateFrequencyInSecs * 1000);
    }
    
    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        manager = newMgr;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        if (DeploymentEvents.POLICY_UPDATES_AVAILABLE.equals(event.getName())) {
            deployToAllAD();
        } else if (DeploymentEvents.POLICY_PUSH_AVAILABLE.equals(event.getName())) {
            // Push active policies to endpoints
            String sDepId = event.getProperties().getProperty(DeploymentEvents.POLICY_PUSH_ID_PROP);
            if (sDepId != null && sDepId.length() > 0) {
                long depId = new Long(sDepId);
                try {
                    executeDeploymentRequest(depId);
                } catch (RequestAlreadyExecutedException raee) {
                    log.error("deployment id: " + sDepId + " already processed");
                }
            }
        }
    }

    /**
     * Execute the deployment request for the agents covered by the specified deployment
     *
     * @param deploymentId the deployment
     */
    private void executeDeploymentRequest(Long deploymentId) throws RequestAlreadyExecutedException {
        if (deploymentId == null) {
            throw new NullPointerException("Deployment id cannot be null for DDAC policy deployment manager");
        }
        
        try {
            Collection<TargetAgentDO> targetAgents = getAgentsToContact(deploymentId);
            
            for (TargetAgentDO targetAgent : targetAgents) {
                try {
                    deployToAD(targetAgent.getAgent().getHost());
                    targetAgent.setStatus(ITargetStatus.SUCCEEDED);
                } catch (IllegalArgumentException iae) {
                    log.error("Processing of deployment '" + deploymentId + "' failed for host " + targetAgent.getAgent().getHost(), iae);
                } catch (DDACDeploymentException dde) {
                    log.error("Processing of deployment '" + deploymentId + "' failed for host " + targetAgent.getAgent().getHost(), dde);
                }
            }
            
            commitPushResults(targetAgents);
        } catch (HibernateException he) {
            log.error("Processing of deployment '" + deploymentId + "' failed", he);
        }
    }

    /**
     * This function queries the database and returns a collection of agents that
     * have to be contacted by this particular DDAC instance. Code taken directly from DABS
     * 
     * @return a collection of agent objects. Each agent needs to be reached.
     */
    protected Collection<TargetAgentDO> getAgentsToContact(Long deploymentId) throws HibernateException {
        Session session = mgmtDataSource.getCountedSession(); 
        List<TargetAgentDO> result;
        try {
            String queryString = "select targetAgent from TargetAgentDO targetAgent " +
                                 "where targetAgent.processor.name = :ddacName " +
                                 "AND targetAgent.host.deploymentRequest.id = :deployId";
            Query query = session.createQuery(queryString);
            query.setString("ddacName", ddacName);
            query.setLong("deployId", deploymentId);
            
            result = query.list();
        } finally {
            HibernateUtils.closeSession(mgmtDataSource, log);
        }
        return result;
    }

    /**
     * Commits the collection of push results to the database. Code taken directly from DABS
     * 
     * @param results collection of results to commit
     * @throws HibernateException if database operation fails.
     */
    protected void commitPushResults(Collection<? extends ITargetAgent> results) throws HibernateException {
        Session session = mgmtDataSource.getCountedSession();
        int iterationCount = 0;
        List evictionList = new ArrayList();
        Transaction t = null;
        try {
            t = session.beginTransaction();
            for (ITargetAgent targetAgent : results) {
                session.update(targetAgent);
                if (iterationCount % MAX_HIBERNATE_ITERATIONS == 0) {
                    session.flush();
                    HibernateUtils.evictObjects(session, evictionList);
                }
            }
            //All status are updated, commit all records
            t.commit();
            // Commit was successful - do not rollback.
            t = null;
        } finally {
            HibernateUtils.rollbackTransation(t, log);
            HibernateUtils.closeSession(mgmtDataSource, log);
        }
    }

    /**
     * This function registers the policy deployment manager as a listener to
     * events related to policy deployment. The policy deployment manager
     * listens to the push deployment event and the simple deployment event.
     */
    protected void registerForDeploymentEvents() {
        eventMgr.registerForEvent(DeploymentEvents.POLICY_UPDATES_AVAILABLE, this);
        eventMgr.registerForEvent(DeploymentEvents.POLICY_PUSH_AVAILABLE, this);
    }


    /**
     * Get the AD configuration information for the specified host
     *
     * @param hostname the hostname as defined in ddac_config.xml
     * @return the ad config for this host
     */
    private DDACActiveDirectoryConfiguration getADConfigByHost(String hostName) throws DDACDeploymentException {
        for (DDACActiveDirectoryConfiguration config : ddacConfig.getADConfig()) {
            if (config.getHostName().equals(hostName)) {
                return config;
            }
        }

        throw new DDACDeploymentException("AD host " + hostName + " is not configured");
    }

    /**
     * Deploy all active policies for every endpoint
     */
    private void deployToAllAD() {
        log.info("Deploying all active CAR/CAP to all active AD");
        for (DDACActiveDirectoryConfiguration adConfig : ddacConfig.getADConfig()) {
            try {
                deployToAD(adConfig.getHostName());
            } catch (IllegalArgumentException iae) {
                log.error("Deployment of DAC policies to " + adConfig.getHostName() + " failed", iae);
            } catch (DDACDeploymentException dde) {
                log.error("Deployment of DAC policies to " + adConfig.getHostName() + " failed", dde);
            }
        }
    }


    /**
     * Find all active policies for a host, construct the appropriate CARs and CAPs and deploy
     * them to the endpoint.
     *
     * @param hostname the name of the endpont
     * @throws DDACDeploymentException on any error
     */
    private void deployToAD(String hostName) throws DDACDeploymentException {
        Calendar ts = Calendar.getInstance();
        Date now = ts.getTime();

        log.info("Deploying all active CAR/CAP to AD " + hostName);

        DDACActiveDirectoryConfiguration config = getADConfigByHost(hostName);

        // Avoid simultaneous deployment to a single AD
        synchronized(config) {
            Long id = agentServiceImpl.getId(hostName);

            if (id == null) {
                log.error("Unable to find agent information for " + hostName);
                return;
            }
            
            Date lastUpdateTime = getLastUpdate(id);

            try {
                agentServiceImpl.getAgentManager().checkUpdates(id, getDACHeartbeatRecord(ts));
            } catch (PersistenceException pe) {
                log.error("Unable to update heartbeat record for " + hostName + " (agent id = " + id + ")");
            } catch (InvalidIDException iie) {
                log.error("Unable to update heartbeat record for " + hostName + " (agent id = " + id + " is unknown)");
            }

            Date lastDeployed = getLastDeploymentDate(now);
            
            if (lastDeployed.after(lastUpdateTime)) {
                IDACDeployer deployer = new PowerShellDeployer();

                // Modified rules (or rules with changed components)
                Collection<DACCentralAccessRule> cars = buildCARs(id, config, now, lastUpdateTime);

                // All policies
                Collection<DACCentralAccessPolicy> caps = buildCAPs(id, config, now);
                
                String result = deployer.deploy(config, cars, caps);
                
                if (result != null && result.length() != 0) {
                    log.warn(result);
                }

                // Clean up all policies that no longer exist
                result = removeUndeployedPolicies(deployer, id, config, now);

                if (result != null & result.length() != 0) {
                    log.warn(result);
                }
            }
        }
    }

    /**
     * Build the CARs for this endpoint. This will consist of all changed rules (only policies that have changed or have changed components)
     * will be included)
     *
     * @param agentId the agent
     * @param config the configuration information for this agent
     * @return a collection of strings consisting of the CARs, ready to be sent by powershell
     */
    private Collection<DACCentralAccessRule> buildCARs(Long agentId, DDACActiveDirectoryConfiguration config, Date now, Date lastUpdateTime) throws DDACDeploymentException{
        Collection<DACCentralAccessRule> result = new ArrayList<DACCentralAccessRule>();

        Collection<IDPolicy> newCARs = allUpdatedCARs(lastUpdateTime, now);
        
        if (newCARs.size() == 0) {
            log.debug("No policies/components have been deployed since the last update to " + config.getHostName());
            return result;
        }

        Collection<IDPolicy> filteredPolicies = filterPoliciesByEndpoint(convertDIDToSID(config, newCARs), agentId);

        if (filteredPolicies.size() == 0) {
            log.info("No rules need to be deployed to " + config.getHostName());
        } else {
            result = createCARs(filteredPolicies, config);
        }

        return result;
    }

    /**
     * Create CARs from policies
     * 
     * @param policies the policies
     * @param config the ad configuration
     * @return a collection of CARs representing the policies
     */
    private Collection<DACCentralAccessRule> createCARs(Collection<IDPolicy> policies, DDACActiveDirectoryConfiguration config) {
        DACDomainObjectFormatter dof = new DACDomainObjectFormatter(config.getACPLToADMappings(), ddacConfig.getActionMappings());

        return createCARs(dof, policies, config);
    }

    /**
     * Create CARs from policies
     * 
     * @param dof the domain object formatter
     * @param policies a collection of policies
     * @param config the ad configuration
     * @return a collection of CARs representing the policies
     */
    private Collection<DACCentralAccessRule> createCARs(DACDomainObjectFormatter dof, Collection<IDPolicy> policies, DDACActiveDirectoryConfiguration config) {
        List<DACCentralAccessRule> rules = new ArrayList<DACCentralAccessRule>();

        for (IDPolicy policy : policies) {
            rules.add(createCAR(dof, policy, config));
        }

        return rules;
    }

    /**
     * Create a CAR from a policy
     * 
     * @param dof the domain object formatter
     * @param policy the policy
     * @param config the ad configuration
     * @return a CAR representing the policy
     */
    private DACCentralAccessRule createCAR(DACDomainObjectFormatter dof, IDPolicy policy, DDACActiveDirectoryConfiguration config) {
        dof.formatPolicy(policy);

        DACCentralAccessRule car = dof.getCAR();
        car.setIdentity(config.getActiveDirectoryRuleIdentity());
        car.setServer(config.getHostName());

        return car;
    }

    /**
     * Create a minimal CAR from a policy
     * 
     * @param dof the domain object formatter
     * @param policy the policy
     * @param config the ad configuration
     * @return a CAR representing the policy
     */
    private DACCentralAccessRule createCARHeader(DACDomainObjectFormatter dof, IDPolicy policy, DDACActiveDirectoryConfiguration config) {
        dof.formatPolicyMetadata(policy);

        DACCentralAccessRule car = dof.getCAR();
        car.setIdentity(config.getActiveDirectoryRuleIdentity());
        car.setServer(config.getHostName());

        return car;
    }

    /**
     * Build the cap information for the specified host.
     *
     * @param agentId the agent id
     * @param config the AD config for this host
     * @param now as of this time
     */
    private Collection<DACCentralAccessPolicy> buildCAPs(Long agentId, DDACActiveDirectoryConfiguration config, Date now) throws DDACDeploymentException {
        // Get all the active policies for this endpoint. We need all active policies, not just all policies deployed since the last update,
        // because we might have to modify some policies based on their CARs being deactivated
        Collection<IDPolicy> filteredPolicies = filterPoliciesByEndpoint(retrieveCAPs(now), agentId);

        // Find the obsolete policies and remove them from their parent policy
        Collection<String> obsoletePolicies = getObsoletePolicies();

        if (!obsoletePolicies.isEmpty()) {
            log.info("Looking for CAPs with deactivated CARs");

            for (IDPolicy policy : filteredPolicies) {
                List<IPolicyReference> exceptions = policy.getPolicyExceptions().getPolicies();
                
                Set<String> namesOfExceptions = new HashSet<String>();
                
                for (IPolicyReference ref : exceptions) {
                    namesOfExceptions.add(ref.getReferencedName());
                }
                
                if (namesOfExceptions.removeAll(obsoletePolicies)) {
                    log.info("CAP " + policy.getName() + " has deactivated CARs");
                    
                    // Something changed. Build up a new IPolicyReference list
                    List<IPolicyReference> newExceptions = new ArrayList<IPolicyReference>();

                    for (String name : namesOfExceptions) {
                        log.info("CAR " + name + " still exists for CAP " + policy.getName());
                        newExceptions.add(new PolicyReference(name));
                    }
                    
                    policy.getPolicyExceptions().setPolicies(newExceptions);
                }
            }
        }

        Collection<DACCentralAccessPolicy> caps = new ArrayList<DACCentralAccessPolicy>();

        if (filteredPolicies.size() == 0) {
            log.info("No policies need to be deployed to " + config.getHostName());
        } else {
            caps = createCAPs(filteredPolicies, config);
        }

        return caps;
    }

    /**
     * Create a collection of CAPs from policies
     *
     * @param policies the policies
     * @param config the AD configuration
     * @return a collection of CAPs corresponding to the policies
     */
    private Collection<DACCentralAccessPolicy> createCAPs(Collection<IDPolicy> policies, DDACActiveDirectoryConfiguration config) {
        List<DACCentralAccessPolicy> caps = new ArrayList<DACCentralAccessPolicy>();

        for (IDPolicy policy : policies) {
            caps.add(createCAP(policy, config));
        }

        return caps;
    }

    /**
     * Create a CAP from a policy
     *
     * @param policy the policy
     * @param config the AD configuration
     * @return a CAP that corresponds to the policy
     */
    private DACCentralAccessPolicy createCAP(IDPolicy policy, DDACActiveDirectoryConfiguration config) {
        DACCentralAccessPolicy cap = new DACCentralAccessPolicy();

        cap.setName(getBasePolicyName(policy));
        cap.setServer(config.getHostName());
        cap.setIdentity(config.getActiveDirectoryCAPIdentity());
        
        for (IPolicyReference reference : policy.getPolicyExceptions().getPolicies()) {
            cap.addCAR(getBasePolicyName(reference.getReferencedName()));
        }

        return cap;
    }

    /**
     * Takes a collection of policies and an endpoint id and returns just those policies targeted for that endpoint
     *
     * @param policies the initial set of policies
     * @param agentId the agent id
     * @return a collection of policies
     */
    private static Collection<IDPolicy> filterPoliciesByEndpoint(Collection<IDPolicy> policies, Long agentId) {
        Collection<IDPolicy> filteredPolicies = new ArrayList<IDPolicy>();

        final AgentRequest agentRequest = new AgentRequest(agentId, AgentTypeEnumType.ACTIVE_DIRECTORY);

        for (IDPolicy policy : policies) {
            IPredicate dt = policy.getDeploymentTarget();
            
            if (dt == null || dt.match(agentRequest)) {
                filteredPolicies.add(policy);
            }
        }

        return filteredPolicies;
    }

    /**
     * Remove all CAPs/CARs for this endpoint that have been undeployed or just no longer exist
     *
     * @param deployer the deployer
     * @param id the agent id
     * @param config the AD configuration information
     * @param now now
     */
    private String removeUndeployedPolicies(IDACDeployer deployer, Long id, DDACActiveDirectoryConfiguration config, Date now) throws DDACDeploymentException {
        DACDomainObjectFormatter dof = new DACDomainObjectFormatter(config.getACPLToADMappings(), ddacConfig.getActionMappings());

        Collection<IDPolicy> removedPolicies = getRemovedPolicies(id, getActiveDACPolicies(now));
        
        Collection<DACCentralAccessRule> removedCARs = new ArrayList<DACCentralAccessRule>();
        Collection<DACCentralAccessPolicy> removedCAPs = new ArrayList<DACCentralAccessPolicy>();
        
        for (IDPolicy removedPolicy : removedPolicies) {
            log.debug("Policy " + removedPolicy.getName() + " was removed");
            if (isCAR(removedPolicy)) {
                // We only need the name of the CAR and a few other details
                removedCARs.add(createCARHeader(dof, removedPolicy, config));
            } else if (isCAP(removedPolicy)) {
                removedCAPs.add(createCAP(removedPolicy, config));
            } else {
                log.error("Unexpected policy returned from getRemovedPolicies");
            }
        }
        
        return deployer.undeploy(config, removedCARs, removedCAPs);
    }


    private interface IDACFilter {
        boolean matches(IDPolicy policy);
    }

    private static final IDACFilter CAR_FILTER = new IDACFilter() {
        public boolean matches(IDPolicy policy) {
            return isCAR(policy);
        }
    };

    private static final IDACFilter CAP_FILTER = new IDACFilter() {
        public boolean matches(IDPolicy policy) {
            return isCAP(policy);
        }
    };

    /**
     * Returns a collection of all the CARs that have been marked for deployment as of a particular date
     * These policies are not "flattened" (i.e. the may contain unresolved references)
     */
    private Collection<IDPolicy> retrieveCARs(Date asOf) throws DDACDeploymentException {
        return retrieveCARs(UnmodifiableDate.START_OF_TIME, asOf);
    }

    /**
     * Returns a collection of all the CARs that have been marked for deployment as of a particular date
     * These policies are not "flattened" (i.e. the may contain unresolved references)
     */
    private Collection<IDPolicy> retrieveCARs(Date since, Date asOf) throws DDACDeploymentException {
        return retrieveDAC(UnmodifiableDate.START_OF_TIME, asOf, CAR_FILTER);
    }

    /**
     * Returns a collection of all the CAPs that have been marked for deployment as of a particular date
     * These policies are not "flattened" (i.e. they may contain unresolved references)
     *
     * @param asOf show all policies deployed on or before this date
     * @return a collection of IDPolicy objects (with references left unresolved)
     */
    private Collection<IDPolicy> retrieveCAPs(Date asOf) throws DDACDeploymentException {
        return retrieveCAPs(UnmodifiableDate.START_OF_TIME, asOf);
    }

    /**
     * Returns a collection fo all the CAPs that have been marked for deployment between two dates. These policies
     * are not flatteend (i.e. they may contain unresolved references);
     *
     * @param since the lower end of the interval (exclusive)
     * @param asOf the upper end of the interval (inclusive);
     */
    private Collection<IDPolicy> retrieveCAPs(Date since, Date asOf) throws DDACDeploymentException {
        return retrieveDAC(since, asOf, CAP_FILTER);
    }
     
    /**
     * Returns a filtered collection of all the DAC policies that have been marked for deployment between
     * two specified dates These policies are not "flattened" (i.e. they may contain unresolved references)
     *
     * @param since the lower end of the interval (exclusive)
     * @param asOf the upper end of the interval (inclusive)
     * @param filter filter for policies
     * @return a collection of IDPolicy objects (with references left unresolved)
     */
    private Collection<IDPolicy> retrieveDAC(Date since, Date asOf, final IDACFilter filter) throws DDACDeploymentException {
        final Collection<IDPolicy> dacs = new ArrayList<IDPolicy>();

        try {
            final Collection<DeploymentEntity> entities = lm.getAllDeployedEntities(EntityType.POLICY,
                                                                                    since,
                                                                                    asOf,
                                                                                    DeploymentType.PRODUCTION);
            for (final DeploymentEntity entity : entities) {
                try {
                    DomainObjectBuilder.processInternalPQL(entity.getPql(),
                                                           new DefaultPQLVisitor() {
                                                               @Override
                                                               public void visitPolicy(DomainObjectDescriptor desc, IDPolicy policy) {
                                                                   if (filter.matches(policy)) {
                                                                       dacs.add(policy);
                                                                   }
                                                               }
                                                           });
                } catch (PQLException pe) {
                    log.error("Error parsing " + entity.getPql() + "\nSkipping...", pe);
                }
            }
        } catch (EntityManagementException eme) {
            throw new DDACDeploymentException("Error getting all deployed policies", eme);
        }

        return dacs;
    }

    private static final List<EntityType> POLICIES_AND_COMPONENTS_TYPES = Arrays.asList( new EntityType[] { EntityType.POLICY, EntityType.COMPONENT });

    /**
     * Returns a collection of all the CARS that need to be deployed (either because they themselves
     * have been deployed or because an entity on which they depend has been deployed)
     *
     * @param since look for policies changed after this date
     * @param asOf and before this one
     * @return 
     */
    private Collection<IDPolicy> allUpdatedCARs(Date since, Date asOf) throws DDACDeploymentException {
        Map<String, DeploymentEntity> nameToDeployment = new HashMap<String, DeploymentEntity>();

        // A policy needs to be updated if it has been deployed since the last heartbeat or if it
        // component on which it depends has been deployed since the last heartbeat
        try {
            Collection<DeploymentEntity> updatedPolicies = lm.getAllDeployedEntities(EntityType.POLICY,
                                                                                     since,
                                                                                     asOf,
                                                                                     DeploymentType.PRODUCTION);
            // All the recently changed policies
            for (DeploymentEntity depEntity : updatedPolicies) {
                nameToDeployment.put(depEntity.getName(), depEntity);
            }
        } catch (EntityManagementException eme) {
            throw new DDACDeploymentException("Unable to get recently deployed policies\n", eme);
        }

        final List<DeploymentEntity> dacPoliciesAndComponents = new ArrayList<DeploymentEntity>();

        // Next, we need to get all the recently changed components and figure out what policies
        // directly or indirectly depend on them
        try {
            Collection<DeploymentEntity> recentComponents = lm.getAllDeployedEntities(EntityType.COMPONENT,
                                                                                      since,
                                                                                      asOf,
                                                                                      DeploymentType.PRODUCTION);

            // Get the ids of the corresponding dev entities
            Collection<Long> recentIds = new ArrayList<Long>(recentComponents.size());

            for (DeploymentEntity recentComponent : recentComponents) {
                recentIds.add(recentComponent.getDevelopmentEntity().getId());
            }

            
            // Get all the policies and components
            Collection<DeploymentEntity> everything = lm.getAllDeployedEntities(POLICIES_AND_COMPONENTS_TYPES,
                                                                                asOf,
                                                                                DeploymentType.PRODUCTION);

            // Get everything that references, directly or indirectly, these components
            Collection<Long> referencingIds = lm.discoverBFS(recentIds,
                                                             everything,
                                                             LifecycleManager.REVERSE_DEPENDENCY_BUILDER,
                                                             LifecycleManager.DEPLOYMENT_ID_EXTRACTOR,
                                                             false);
            // Convert to DeploymentEntities
            Collection<DeploymentEntity> referencingEntities = lm.getDeploymentEntitiesForIDs(referencingIds,
                                                                                              asOf,
                                                                                              DeploymentType.PRODUCTION);
            
            for (DeploymentEntity referencingEntity : referencingEntities) {
                nameToDeployment.put(referencingEntity.getName(), referencingEntity);
            }

            // Now we should have the names of all policies (and other things) that require update
            // and we can build a collection of all DAC policies.
            for (final DeploymentEntity entity : nameToDeployment.values()) {
                try {
                    DomainObjectBuilder.processInternalPQL(entity.getPql(),
                                                           new DefaultPQLVisitor() {
                                                               @Override
                                                               public void visitPolicy(DomainObjectDescriptor desc, IDPolicy policy) {
                                                                   if (isCAR(policy)) {
                                                                       dacPoliciesAndComponents.add(entity);
                                                                   }
                                                               }
                                                           });
                } catch (PQLException pe) {
                    log.error("Error parsing " + entity.getPql() + "\nSkipping ...", pe);
                }
            }

            // If we don't have any policies then we are done
            if (dacPoliciesAndComponents.isEmpty()) {
                return Collections.emptyList();
            }

            // Now add all the components (not just recently changed ones). We technically only need the ones
            // that are referenced by policies, but there is no harm in having more of them
            for (DeploymentEntity entity : everything) {
                if (entity.getType() == EntityType.COMPONENT) {
                    dacPoliciesAndComponents.add(entity);
                }
            }
        } catch (PQLException pe) {
            throw new DDACDeploymentException("PQL parsing exception", pe);
        } catch (CircularReferenceException cre) {
            throw new DDACDeploymentException("Circular reference found in dependency tree", cre);
        } catch (EntityManagementException eme) {
            throw new DDACDeploymentException("Unable to get deployed components/policies", eme);
        }

        // Done! Now we resolve all references and return the result
        return flattenPolicies(dacPoliciesAndComponents);
    }

    /**
     * Given a collection of policies and components, "flatten" out the policies by inlining all component
     * references.
     *
     * @param entities the policies and components on which they depend
     * @return a collection of policies with all the component references inlined
     */
    private static Collection<IDPolicy> flattenPolicies(Collection<DeploymentEntity> entities) throws DDACDeploymentException {
        ReferenceResolver resolver = new ReferenceResolver(entities);
        
        try {
            resolver.resolve();
        } catch (PQLException pe) {
            log.error("Error resolving policies.", pe );
            
            throw new DDACDeploymentException("Error resolving policies", pe);
        }
        
        return Arrays.asList(resolver.getParsedPolicies());
    }

    private static final long[] EMPTY_IDS = new long[0];

    private static boolean isUserDidExpr(IExpression expr) {
        return (expr instanceof SubjectAttribute &&
                ((SubjectAttribute)expr).getName().equals("did"));
    }

    private static boolean isLDAPExpr(IExpression expr) {
        return (expr instanceof SubjectAttribute &&
                ((SubjectAttribute)expr).getName().equals("ldapgroupid"));
    }
    
    /**
     * Given a collection of policies, find all user.did or user.ldapgroupid expressions, both of which refer to destiny
     * ids, and convert them to refer to actual sids
     */

    private Collection<IDPolicy> convertDIDToSID(DDACActiveDirectoryConfiguration config, Collection<IDPolicy> policies) throws DDACDeploymentException {
        final Map<Long, String> didToSidMap = new HashMap<Long, String>();

        final Set<Long> userIds = new HashSet<Long>();
        final Set<Long> ldapIds = new HashSet<Long>();

        // First, find all the expressions with destiny ids
        for (IDPolicy policy : policies) {
            policy.getTarget().getSubjectPred().accept(new DefaultPredicateVisitor() {
                @Override
                public void visit(IRelation rel) {
                    if (isUserDidExpr(rel.getLHS())) {
                        addId(userIds, rel.getRHS());
                    } else if (isUserDidExpr(rel.getRHS())) {
                        addId(userIds, rel.getLHS());
                    } else if (isLDAPExpr(rel.getLHS())) {
                        addId(ldapIds, rel.getRHS());
                    } else if (isLDAPExpr(rel.getRHS())) {
                        addId(ldapIds, rel.getLHS());
                    }
                }
                    
                private void addId(Set<Long> set, IExpression value) {
                    if (value instanceof Constant && ((Constant)value).getValue().getType() == ValueType.LONG) {
                        set.add((Long)((Constant)value).getValue().getValue());
                    }
                }
            }, IPredicateVisitor.PREORDER);
        }

        // Convert the dids into sids
        try {
            // getLeafObjectsForIds doesn't like it if you don't give it any ids
            if (userIds.size() != 0 || ldapIds.size() != 0) {
                Collection<LeafObject> leafObjs = dictionaryHelper.getLeafObjectsForIds(CollectionUtils.toLong(userIds),
                                                                                        CollectionUtils.toLong(ldapIds),
                                                                                        EMPTY_IDS);

                for (LeafObject leafObj : leafObjs) {
                    if (leafObj.getType() == LeafObjectType.USER) {
                        didToSidMap.put(leafObj.getId(), leafObj.getUid());
                    } else if (leafObj.getType() == LeafObjectType.USER_GROUP) {
                        String groupName = leafObj.getName();
                        String sid = config.getGroupToSIDMappings().get(groupName);

                        if (sid == null) {
                            throw new DDACDeploymentException("Unable to find group->sid mapping for group: " + groupName);
                        }

                        didToSidMap.put(leafObj.getId(), sid);
                    }
                }
            }
        } catch (DictionaryException de) {
            Set<Long> combined = new HashSet<Long>();
            combined.addAll(userIds);
            combined.addAll(ldapIds);
            log.error("Dictionary exception looking for ids: " + CollectionUtils.asString(combined, ", "));
            throw new DDACDeploymentException("Error looking for ids: " + CollectionUtils.asString(combined, ", "), de);
        }

        // Go through all the policies and replace the dids with sids
        for (IDPolicy policy : policies) {
            policy.getTarget().setSubjectPred(Predicates.transform(policy.getTarget().getSubjectPred(),
                                 new DefaultTransformer() {
                                     @Override
                                     public IPredicate transformRelation(IRelation rel) {
                                         // Both ldap group ids and user ids are converted to user sid expressions. This is not
                                         // technically valid, but the formatter treats them the same way (takes the sid and puts
                                         // it into the SDDL), so there is no point in distinguishing between group and user sids
                                         if (isUserDidExpr(rel.getLHS()) ||
                                             isLDAPExpr(rel.getLHS())) {
                                             return new Relation(rel.getOp(),
                                                                 (SubjectAttribute)SubjectAttribute.USER_UID,
                                                                 getMatchingUID(rel.getRHS(), didToSidMap));
                                         } else if (isUserDidExpr(rel.getRHS()) ||
                                                    isLDAPExpr(rel.getRHS())) {
                                             return new Relation(rel.getOp(),
                                                                 getMatchingUID(rel.getLHS(), didToSidMap),
                                                                 (SubjectAttribute)SubjectAttribute.USER_UID);
                                         }
                                         
                                         return rel;
                                     }
                                 }));
        }

        return policies;
    }

    private static IExpression getMatchingUID(IExpression val, Map<Long, String> idMap) {
        Long id = (Long)((Constant)val).getValue().getValue();

        String uid = idMap.get(id);

        if (uid == null) {
            return NULL_SID;
        }

        return Constant.build(uid);
    }
        
    /**
     * Get the date of the last deployment before a certain time
     *
     * @param now now
     * @return the last deployment date
     */
    private Date getLastDeploymentDate(Date now) {
        try {
            return lm.getLatestDeploymentTime(UnmodifiableDate.START_OF_TIME, now, DeploymentType.PRODUCTION);
        } catch (EntityManagementException eme) {
            log.warn("Could not check for deploymend updates.  Falling back to complete update mode.");
            return UnmodifiableDate.forDate(UnmodifiableDate.START_OF_TIME);
        }
    }

    private static final String DAC_POLICY_ATTRIBUTE = "DYNAMIC_ACCESS_CONTROL_POLICY";

    /**
     * Determine if a policy is a dynamic access control policy
     *
     * @param policy the policy
     * @return true if it's a DAC policy, false otherwise
     */
    private static boolean isDACPolicy(IDPolicy policy) {
        return policy.hasAttribute(DAC_POLICY_ATTRIBUTE);
    }

    /**
     * Determine if this is a policy exception or a top-level policy
     *
     * @param policy this policy
     * @return true if it is an exception, false otherwise
     */
    private static boolean isPolicyException(IDPolicy policy) {
        return policy.hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE);
    }

    /**
     * Determine if this deployment entity is a CAR
     *
     * @param dep the deployment entity
     * @return true if it is an CAR, false otherwise
     */
    private static boolean isCAR(DeploymentEntity dep) {
        final AtomicBoolean car = new AtomicBoolean(false);

        try {
            DomainObjectBuilder.processInternalPQL(dep.getPql(),
                                                   new DefaultPQLVisitor() {
                                                       @Override
                                                       public void visitPolicy(DomainObjectDescriptor desc, IDPolicy policy) {
                                                           car.set(isCAR(policy));
                                                       }
                                                   });
        } catch (PQLException pe) {
            log.error("Error parsing " + dep.getPql());
        }

        return car.get();
    }

    /**
     * Determine if this policy represents a CAR
     *
     * @param policy the policy
     * @return true if it is a CAR, false otherwise
     */
    private static boolean isCAR(IDPolicy policy) {
        return isDACPolicy(policy) && isPolicyException(policy);
    }

    /**
     * Determine if this deployment entity is a CAP
     *
     * @param dep the deployment entity
     * @return true if it is an CAP, false otherwise
     */
    private static boolean isCAP(DeploymentEntity dep) {
        final AtomicBoolean cap = new AtomicBoolean(false);

        try {
            DomainObjectBuilder.processInternalPQL(dep.getPql(),
                                                   new DefaultPQLVisitor() {
                                                       @Override
                                                       public void visitPolicy(DomainObjectDescriptor desc, IDPolicy policy) {
                                                           cap.set(isCAP(policy));
                                                       }
                                                   });
        } catch (PQLException pe) {
            log.error("Error parsing " + dep.getPql());
        }

        return cap.get();
    }

    /**
     * Determine if this policy represents a CAP
     *
     * @param policy the policy
     * @return true if it is a CAP, false otherwise
     */
    private static boolean isCAP(IDPolicy policy) {
        return isDACPolicy(policy) && !isPolicyException(policy);
    }

    /**
     * Return fake heartbeat data. We will use this to update the DB to record the fact that the
     * AD received new policies
     *
     * @param deployed the time at which we did the deployment
     * @return (minimal) heartbeat data appropriate for persisting to the database
     */
    private static IAgentHeartbeatData getDACHeartbeatRecord(final Calendar deployed) {
        return new IAgentHeartbeatData() {
            @Override
            public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
                return new IAgentPolicyAssemblyStatusData() {
                    @Override
                    public Calendar getLastCommittedDeploymentBundleTimestamp() {
                        return deployed;
                    }
                };
            }

            @Override
            public IAgentProfileStatusData getProfileStatus() {
                return null;
            }
        };
    }

    /**
     * Returns the last update time for this agent
     *
     * @param agentId the agent id
     * @return the Date we last sent new information to this endpoint
     */
    private Date getLastUpdate(Long agentId) throws DDACDeploymentException {
        try {
            IAgentDO agentDO = agentServiceImpl.getAgentManager().getAgentById(agentId);
            
            return agentDO.getLastHeartbeat() != null ? agentDO.getLastHeartbeat().getTime() : UnmodifiableDate.START_OF_TIME;
        } catch (PersistenceException pe) {
            throw new DDACDeploymentException("Unable to get last \'heartbeat\' time for agent " + agentId, pe);
        }
    }


    /**
     * Get the policy name from the full name (i.e. from A/B/P1 return P1)
     *
     * @param fullName the path name of the policy
     * @return the name
     */
    private String getBasePolicyName(String fullName) {
        String[] policySplit = fullName.split("/");

        if (policySplit.length < 1) {
            throw new IllegalArgumentException("Unable to extract policy name from policy " + fullName);
        }

        return policySplit[policySplit.length - 1];
    }

    /**
     * Get the policy name from the full name (i.e. from A/B/P1 return P1)
     *
     * @param policy the policy
     * @return the name
     */
    private String getBasePolicyName(IDPolicy policy) {
        return getBasePolicyName(policy.getName());
    }


    /**
     * Returns a collection of all active DAC policies (CAPs and CARs)
     *
     * @param now now. As in, "now"
     * @returns a collection of all active DAC policies as of now
     */
    synchronized private Collection<IDPolicy> getActiveDACPolicies(Date now) throws DDACDeploymentException {
        Collection<IDPolicy> activePolicies = new ArrayList<IDPolicy>();

        activePolicies.addAll(retrieveCARs(now));
        activePolicies.addAll(retrieveCAPs(now));

        return activePolicies;
    }

    /**
     * Determine what policies are assigned to which endpoints so that we can detect when something has changed
     */
    private void initializeAllActiveDACPolicies() throws DDACDeploymentException {
        Collection<IDPolicy> activePolicies = getActiveDACPolicies(Calendar.getInstance().getTime());

        for (DDACActiveDirectoryConfiguration adConfig : ddacConfig.getADConfig()) {
            Long id = agentServiceImpl.getId(adConfig.getHostName());

            // We don't care about the result, we just want the cache initialized
            getActivePoliciesByAgent(id, activePolicies);
        }
    }

    private Map<Long, Collection<IDPolicy>> policiesByAgent = new HashMap<Long, Collection<IDPolicy>>();

    /**
     * For a particular agent, given a collection of active policies, determine which of those policies are active
     * on that agent. Update <code>policiesByAgent</code> to reflect this information
     * 
     * @param agentId the agent
     * @param activePolicies all active DAC policies
     *
     */
    synchronized private Collection<IDPolicy> getActivePoliciesByAgent(Long agentId, Collection<IDPolicy> currentlyActive) {
        Collection<IDPolicy> activeForThisAgent = filterPoliciesByEndpoint(currentlyActive, agentId);

        policiesByAgent.put(agentId, new ArrayList<IDPolicy>(activeForThisAgent));

        return activeForThisAgent;
    }

    /**
     * Given a collection of currently active policies and an agent, determine which policies
     * were active, but no longer are, and should be deactivated.
     *
     * We do this by name. The only significant part of the name for CARs (and CAPs too) is the last part of the
     * name (so, for A/B/C, it's C). Compare that bit only
     *
     * @param agentId the agent id
     * @param currentlyActive what policies are currently active
     */
    synchronized private Collection<IDPolicy> getRemovedPolicies(Long agentId, Collection<IDPolicy> currentlyActive) {
        Collection<IDPolicy> previous = policiesByAgent.get(agentId);

        if (previous == null) {
            return Collections.emptySet();
        }

        Collection<IDPolicy> activeForThisAgent = getActivePoliciesByAgent(agentId, currentlyActive);

        List<IDPolicy> removed = new ArrayList<IDPolicy>();

        // Remove currentlyActive from previous. Everything that is left was removed by recent user actions
        // I'd like to use set operations, but we aren't comparing objects here; we are comparing object names
        for (IDPolicy policy : previous) {
            String name = getDACName(policy);

            boolean found = false;
            for (IDPolicy active : activeForThisAgent) {
                if (name.equals(getDACName(active))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                removed.add(policy);
            }
        }

        return removed;
    }

    /**
     * Get the names of all obsolete entities.
     * 
     * @return the names of all obsolete entities
     */

    Collection<String> getObsoletePolicies() {
        Collection<String> ret = new ArrayList<String>();

        try {
            Collection<DomainObjectDescriptor> descs = lm.getAllEntitiesForStatus(DevelopmentStatus.OBSOLETE, LifecycleManager.DIRECT_CONVERTER);

            for (DomainObjectDescriptor dod : descs) {
                if (dod.getType() == EntityType.POLICY) {
                    ret.add(dod.getName());
                }
            }
        } catch (EntityManagementException e) {
        }

        return ret;
    }

    private static String getDACName(IDPolicy policy) {
        String[] policyPath = policy.getName().split("/");
        return policyPath[policyPath.length-1];
    }
}
