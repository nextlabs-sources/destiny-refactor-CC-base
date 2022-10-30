/*
 * Created on Feb 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.DeploymentEvents;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.RequestAlreadyExecutedException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.UnknownDeploymentIdException;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;

/**
 * @author safdar, ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/components/deployment/PolicyDeploymentManagerImpl.java#1 $
 */

public class PolicyDeploymentManagerImpl implements IPolicyDeploymentManager, IManagerEnabled,
		IInitializable, ILogEnabled, IConfigurable, IDisposable, IDestinyEventListener {

	private static final int PUSH_THREAD_POOL_SIZE = 5;
	private static final int MAX_HIBERNATE_ITERATIONS = 30;
    private static Log log;

    private IConfiguration configuration;
    private IComponentManager manager;
    private String dabsName;
    private IHibernateRepository mgmtDataSource;
    private IThreadPool pushWorkerPool;
    private IDestinyEventManager eventMgr;

    /**
     * Commits the list of push results to the database
     * 
     * @param results
     *            list of results to commit
     * @throws HibernateException
     *             if database operation fails.
     */
    protected void commitPushResults(List<ITargetAgent> results) throws HibernateException {
        Session session = this.mgmtDataSource.getCountedSession();
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
			HibernateUtils.rollbackTransation(t, getLog());
            HibernateUtils.closeSession(this.mgmtDataSource, getLog());
		}
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        unRegisterForDeploymentEvents();
        this.pushWorkerPool = null;
        this.configuration = null;
        this.dabsName = null;
    }

    /**
     * 
     * In DABS, the execution of the deployment request consists in contacting
     * agents on a given port. This function fetches the list of agents that
     * this DABS instance needs to contact, and then it will process this list.
     * 
     * @param deploymentId
     *            deployment request id
     * @throws RequestAlreadyExecutedException
     *             if the deployment already took place
     * @throws UnknownDeploymentIdException
     *             if the deployment id is unknown
     */
    public void executeDeploymentRequest(Long deploymentId) throws RequestAlreadyExecutedException,
			UnknownDeploymentIdException {
        if (deploymentId == null) {
            throw new NullPointerException("Deployment id cannot be null for DABS policy deployment manager");
        }

        try {
			List<ITargetAgent> agentList = getAgentsToContact(deploymentId);
			agentList = processPushAssignments(agentList);
			commitPushResults(agentList);
		} catch (HibernateException e) {
			log.error("Processing of deployment '" + deploymentId + "' failed", e);
		}
    }

    /**
     * This function queries the database and returns the list of agents that
     * have to be contacted by this particular DABS instance.
     * 
     * @return a collection of agent objects. Each agent needs to be reached.
     */
    protected List<ITargetAgent> getAgentsToContact(Long deploymentId) throws HibernateException {
        Session session = this.mgmtDataSource.getCountedSession(); 
        //We may have to reach a pretty large number of agents. Hopefully,
        // there are enough DABS involved so that each DABS does not have a huge
        // list to process. Therefore, we fetch the data in one shot.
        List<ITargetAgent> result;
        try {
            String queryString = "select targetAgent from TargetAgentDO targetAgent " +
                    "where targetAgent.processor.name = :dabsName " +
                    "AND targetAgent.host.deploymentRequest.id = :deployId";
            Query query = session.createQuery(queryString);
            query.setString("dabsName", this.dabsName);
            query.setLong("deployId", deploymentId);

            result = query.list();
        } finally {
            HibernateUtils.closeSession(this.mgmtDataSource, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * This function flushes the local policy cache. The policy cache is
     * accessible through the Policy framework library.
     */
    protected void flushLocalPolicyCache() {
        log.trace("Policy cache flushed");
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.mgmtDataSource = (IHibernateRepository) this.configuration.get(MGMT_DATA_SOURCE_CONFIG_PARAM);
        if (this.mgmtDataSource == null) {
            throw new NullPointerException("The management data source needs to be set in the configuration for the deployment request Mgr.");
        }

        this.dabsName = (String) this.configuration.get(DABS_COMPONENT_NAME);
        if (this.dabsName == null) {
            throw new NullPointerException("The DABS component name needs to be specified in the DABS policy deployment manager configuration.");
        }

        //Prepares a thread pool for push requests
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, new Integer(PUSH_THREAD_POOL_SIZE));
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, PushWorkerThreadImpl.class.getName());
        config.setProperty(IThreadPool.THREADPOOL_NAME, "PushPolicyDeployment");
        ComponentInfo threadPoolCompInfo = new ComponentInfo(
        		"PushWorkerThreadPool",
				ThreadPool.class.getName(), 
				IThreadPool.class.getName(),
				LifestyleType.SINGLETON_TYPE, 
				config);
		this.pushWorkerPool = (IThreadPool) getManager().getComponent(threadPoolCompInfo);

        // Obtain a handle to the event mgr:
        IDestinySharedContextLocator locator = 
        	(IDestinySharedContextLocator) getManager().getComponent(IDestinySharedContextLocator.COMP_NAME);
		this.eventMgr = locator.getSharedContext().getEventManager();

        //Now, the component is ready to accept push / update requests
        registerForDeploymentEvents();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        if (DeploymentEvents.POLICY_UPDATES_AVAILABLE.equals(event.getName())) {
            flushLocalPolicyCache();
        } else if (DeploymentEvents.POLICY_PUSH_AVAILABLE.equals(event.getName())) {
            String sId = event.getProperties().getProperty(DeploymentEvents.POLICY_PUSH_ID_PROP);
            if (sId != null && sId.length() > 0) {
            	Long deploymentId = new Long(sId);
                try {
					//Execute the deployment. If it fails, we simply flush the
					// policy cache.
					executeDeploymentRequest(deploymentId);
				} catch (RequestAlreadyExecutedException e) {
					log.error("deployment id:'" + deploymentId + "' already processed.");
					flushLocalPolicyCache();
				} catch (UnknownDeploymentIdException e) {
					log.error("unknown deployment id:'" + deploymentId + "'.");
					flushLocalPolicyCache();
				}
            }else{ 
				//We don't know where to push to. At least, flush the cache.
				flushLocalPolicyCache();
				log.error("Invalid deployment id received, cancelling push for DABS instance.");
			}
        } else {
            log.debug("DABS policy deployment manager received an event that it cannot handle");
        }
    }

    /**
     * This function registers the policy deployment manager as a listener to
     * events related to policy deployment. The policy deployment manager
     * listens to the push deployment event and the simple deployment event.
     */
    protected void registerForDeploymentEvents() {
        this.eventMgr.registerForEvent(DeploymentEvents.POLICY_UPDATES_AVAILABLE, this);
        this.eventMgr.registerForEvent(DeploymentEvents.POLICY_PUSH_AVAILABLE, this);
    }

    /**
     * Processes the list of push assignments. The list contains the set of
     * agents that need to be contacted. The assumption here is that the list is
     * not too long. If it becomes really big (500 items+), either we need more
     * DABS, or we need to make this API scrollable.
     * 
     * @param targetAgentList
     *            list of agents that should be contacted.
     * @return the updated list of agents with a status about the push
     *         operation.
     */
    protected List<ITargetAgent> processPushAssignments(List<ITargetAgent> targetAgentList) {
        if (targetAgentList == null) {
			throw new NullPointerException("Push request list cannot be null!");
		}

        int agentListSize = targetAgentList.size();
		if (agentListSize > 0) {
			PushRequestCounter completedTasks = new PushRequestCounter(agentListSize);
			for (ITargetAgent targetAgent : targetAgentList) {
				PushRequest request = new PushRequest(targetAgent, completedTasks);
				this.pushWorkerPool.doWork(request);
			}

			//Wait for all requests to be processed
			synchronized (completedTasks) {
				try {
					completedTasks.wait();
				} catch (InterruptedException e) {
					log.info("Push policy deployment was interrupted.");
				}
			}
		}

        //Even if the task was interrupted, we can still commit whatever has
        //already been processed.
        return targetAgentList;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }

    /**
     * This function unregisters the policy deployment manager as a listener to
     * events related to policy deployment.
     */
    protected void unRegisterForDeploymentEvents() {
        this.eventMgr.unregisterForEvent(DeploymentEvents.POLICY_UPDATES_AVAILABLE, this);
        this.eventMgr.unregisterForEvent(DeploymentEvents.POLICY_PUSH_AVAILABLE, this);
    }
}