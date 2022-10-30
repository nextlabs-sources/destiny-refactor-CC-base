/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.destiny.services.management.types.Component;
import com.bluejungle.destiny.services.policy.PolicyEditorIF;
import com.bluejungle.destiny.services.policy.PolicyEditorLocator;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.usertypes.CalendarToLongUserType;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;

import javax.xml.rpc.ServiceException;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Concrete implementation of the IAgentStatistics collector interfaces
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentStatisticsCollectorImpl.java#2 $
 */
public class AgentStatisticsCollectorImpl implements IAgentStatisticsCollector, ILogEnabled, IConfigurable {

    static final String HEARTBEAT_RECORDER_PROPERTY_NAME = "HeartbeatRecorder";
    static final String AGENT_MANAGER_PROPERTY_NAME = "AgentManager";
    
    private static final String REGISTERED_QUERY_PARAM_NAME = "registered";
    private static final String LAST_DAY_QUERY_PARAM_NAME = "lastDay";
    private static final String AGENTS_NOT_CONNECTED_IN_LAST_DAY_QUERY = "select count(*) from AgentDO where registered=:" +  REGISTERED_QUERY_PARAM_NAME + " and lastHeartbeat < :" + LAST_DAY_QUERY_PARAM_NAME;
    private static final String LAST_POLICY_DEPLOYMENT_PARAM_NAME = "lastPolicyDeployment";
    private static final String AGENTS_WITH_OUT_OF_DATE_POLICIES_QUERY = "select count(*) from AgentDO where registered=:" +  REGISTERED_QUERY_PARAM_NAME + " and DEPLOYMENT_BUNDLE_TS < :" + LAST_POLICY_DEPLOYMENT_PARAM_NAME;
    private static final String POLICY_SERVICE_LOCATION_SERVLET_PATH = "/services/PolicyEditorIFPort";
    private static final String COMPONENT_SERVICE_LOCATION_SERVLET_PATH = "/services/ComponentServiceIFPort";

    private PolicyEditorIF policyEditorService;
    private ComponentServiceIF componentService;
    private Log log;
    private IConfiguration config;

    /**
     * Create an instance of AgentStatisticsCollectorImpl
     *  
     */
    public AgentStatisticsCollectorImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.IAgentStatisticsCollector#collectStatistics()
     */
    public IAgentStatistics collectStatistics() throws PersistenceException {
        // FIX ME - Should be refactored to be more OO
        AgentStatisticsImpl stats = new AgentStatisticsImpl();
        populateAgentCounts(stats);
        populateHeartbeatCount(stats);
        populateAgentsNotConnectedCount(stats);
        populateAgentsWithOutOfDatePolicies(stats);

        return stats;
    }

    /**
     * Populate the agent count statistics within the provided AgentStatistics
     * instance
     * 
     * @param stats
     *            the statistics to populate
     * @throws PersistenceException
     *             if an error occurs while retrieving the data
     */
    private void populateAgentCounts(AgentStatisticsImpl stats) throws PersistenceException {
        // First, prepoluate all agent types with 0
        IAgentManager agentManager = (IAgentManager) this.getConfiguration().get(AGENT_MANAGER_PROPERTY_NAME);
        List<IAgentType> agentTypes = agentManager.getAgentTypes();
        for (IAgentType nextAgentType : agentTypes) {
            stats.setAgentCount(nextAgentType, 0);
        }
        
        // Now, get the actual values
        Session session = null;
        try {
            session = getSession();
            Query q = session.createQuery("select a.type, count(a) from AgentDO a where registered=:registered group by a.type");
            q.setParameter("registered", Boolean.TRUE);
            List list = q.list();

            //Parse the results
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object[] row = (Object[]) it.next();
                IAgentType agentType = (IAgentType) row[0];
                Integer agentTypeCount = (Integer) row[1];
                stats.setAgentCount(agentType, agentTypeCount.longValue());
            }
        } catch (HibernateException exception) {
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }

    /**
     * Populate the heartbeat count statistics within the provided
     * AgentStatistics instance
     * 
     * @param stats
     *            the statistics to populate
     */
    private void populateHeartbeatCount(AgentStatisticsImpl stats) throws PersistenceException {
        IHeartbeatRecorder heartbeatRecorder = (IHeartbeatRecorder) this.getConfiguration().get(HEARTBEAT_RECORDER_PROPERTY_NAME);
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.DATE, -1);
        long numHeartbeats = heartbeatRecorder.getNumHeartbeatsSinceTime(beginTime);
        stats.setHeartbeatsInLastDayCount(numHeartbeats);
    }

    /**
     * Populate the stat depicting the number of agents not connected within the
     * last day
     * 
     * @param stats
     *            the statistics to populate
     * @throws PersistenceException
     */
    private void populateAgentsNotConnectedCount(AgentStatisticsImpl stats) throws PersistenceException {
        long disconnectedAgentCount = 0;

        Session session = null;
        try {
            session = getSession();
            Query hqlQuery = session.createQuery(AGENTS_NOT_CONNECTED_IN_LAST_DAY_QUERY);
            Calendar currentDate = Calendar.getInstance();
            currentDate.add(Calendar.DATE, -1);
            hqlQuery.setParameter(LAST_DAY_QUERY_PARAM_NAME, currentDate, CalendarToLongUserType.TYPE);
            hqlQuery.setParameter(REGISTERED_QUERY_PARAM_NAME, Boolean.TRUE);
            
            Iterator results = hqlQuery.iterate();

            // We may lose some precision here, but not much we can do.
            // Hibernate returns count as an Integer. We should be okay given
            // the expected amounts
            disconnectedAgentCount = ((Integer) results.next()).intValue();
        } catch (HibernateException exception) {
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }

        stats.setAgentsDisconnectedInLastDayCount(disconnectedAgentCount);
    }

    /**
     * Populate the stat depicting the number of agents with out of date
     * policies
     * 
     * @param stats
     *            the statistics to populate
     * @throws PersistenceException
     */
    private void populateAgentsWithOutOfDatePolicies(AgentStatisticsImpl stats) throws PersistenceException {
        long agentsWithOutOfDatePoliciesCount = 0;

        Session session = null;
        try {
            session = getSession();
            Query hqlQuery = session.createQuery(AGENTS_WITH_OUT_OF_DATE_POLICIES_QUERY);

            PolicyEditorIF policyService = getPolicyEditorService();
            Calendar lastDeploymentTime = policyService.getLatestDeploymentTime();
            hqlQuery.setParameter(LAST_POLICY_DEPLOYMENT_PARAM_NAME, lastDeploymentTime, CalendarToLongUserType.TYPE);
            hqlQuery.setParameter(REGISTERED_QUERY_PARAM_NAME, Boolean.TRUE);
            
            Iterator results = hqlQuery.iterate();

            // We may lose some precision here, but not much we can do.
            // Hibernate returns count as an Integer. We should be okay given
            // the expected amounts
            agentsWithOutOfDatePoliciesCount = ((Integer) results.next()).intValue();
        } catch (RemoteException exception) {
            getLog().warn("Failed to pull last deployment time from policy service", exception);
        } catch (ServiceException exception) {
            getLog().warn("Failed to pull last deployment time from policy service", exception);
        } catch (HibernateException exception) {
            throw new PersistenceException(exception);
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }

        stats.setAgentsWithOutOfDatePolicies(agentsWithOutOfDatePoliciesCount);
    }

    /**
     * Retrieve the PolicyEditor Service interface.
     * 
     * Note - Protected for unit test purposes. Yes, not ideal, but it's also
     * better to test with a stub service rather than an actual service
     * 
     * @return the PolicyEditor Service interface
     * @throws ServiceException
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     * @throws ServiceNotReadyFault
     */
    protected PolicyEditorIF getPolicyEditorService() throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
        if (this.policyEditorService == null) {
            String location = getPolicyServerURL();
            location += POLICY_SERVICE_LOCATION_SERVLET_PATH;
            PolicyEditorLocator locator = new PolicyEditorLocator();
            locator.setPolicyEditorIFPortEndpointAddress(location);

            this.policyEditorService = locator.getPolicyEditorIFPort();
        }

        return this.policyEditorService;
    }

    private String getPolicyServerURL() throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
        ComponentServiceIF componentService = getComponentServiceInterface();
        Component[] policyServerComponents = componentService.getComponentsByType(ServerComponentType.DPS.getName()).getComp();
        if (policyServerComponents == null || policyServerComponents.length < 1) {
            /*
             * Couldn't find a policy server. Throw exception
             * ServiceNotReadyFault is not the ideal exception to throw
             * here, but it's somewhat appropriate
             */
            throw new ServiceNotReadyFault();
        }

        
        //Pick the first one active and return the load balancing url
        for(Component policyServerComponent : policyServerComponents){
        	if(policyServerComponent.isActive()){
        		 return policyServerComponent.getLoadBalancerURL();
        	}
        }
        
        //FIXME not ideal throw exception
        throw new ServiceNotReadyFault();
    }

    /**
     * Retrieve the Component Service interface.
     * 
     * @return the Component Service interface
     * @throws ServiceException
     * @throws ServiceException
     *             if the component service interface could not be located
     */
    private ComponentServiceIF getComponentServiceInterface() throws ServiceException {
        if (this.componentService == null) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            IConfiguration mainConfig = (IConfiguration) compMgr.getComponent(IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME);
            String location = (String) mainConfig.get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);
            if (location == null) {
                // We may be in DMS - This is a bit of a hack, but I'm not sure
                // what else to do
                location = (String) mainConfig.get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM);
            }
            location += COMPONENT_SERVICE_LOCATION_SERVLET_PATH;
            ComponentServiceLocator locator = new ComponentServiceLocator();
            locator.setComponentServiceIFPortEndpointAddress(location);

            this.componentService = locator.getComponentServiceIFPort();
        }

        return this.componentService;
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private static Session getSession() throws HibernateException {
        IHibernateRepository dataSource = getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private static IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized.");
        }

        return dataSource;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log logToSet) {
        this.log = logToSet;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

}
