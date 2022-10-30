/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.policyDeployMgr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dps.BaseDPSComponentTest;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.RequestAlreadyExecutedException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.UnknownDeploymentIdException;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.DeploymentRequestDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.DeploymentRequestMgrImpl;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetAgentDO;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetHostDO;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.destiny.services.management.types.ApplicationResourceList;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;
import com.bluejungle.version.types.Version;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/test/DeploymentManagerTest.java#1 $
 */

public class DeploymentManagerTest extends BaseDPSComponentTest {

    private IAgentManager agentMgr;
    private Long deployRequestId;
    private Set<Long> createdAgentIds = new HashSet<Long>();
    private Set<DCCRegistrationInformation> createdDABSs = new HashSet<DCCRegistrationInformation>();

    /**
     * Constructor
     */
    public DeploymentManagerTest() {
        super();
    }


    /**
     * This function creates a set of agents used for the test. Agents have
     * different characteristics. Some are push enabled, some are not, some have
     * different push port number, etc.
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     *             if agent creation fails.
     */
    protected void createTestAgents() throws PersistenceException, InvalidIDException {

        List<Long> desktopAgentIds = new ArrayList<Long>();
        List<Long> fileAgentIds = new ArrayList<Long>();

        IAgentType desktopAgentType = this.agentMgr.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentMgr.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        //Creates 50 desktop agents
        for (int i = 0; i < 50; i++) {
            AgentRegData desktopAgentRegData = new AgentRegData("localhost" + i, desktopAgentType);
            
            //Register an agent
            IAgentStartupConfiguration agentConfig = this.agentMgr.registerAgent(desktopAgentRegData);
            Long agentId = agentConfig.getId();
            desktopAgentIds.add(agentId);
        }

        //Creates 20 file server agents
        for (int i = 0; i < 20; i++) {
            AgentRegData desktopAgentRegData = new AgentRegData("localhost" + i, fileServerAgentType);
            //Register an agent
            IAgentStartupConfiguration agentConfig = this.agentMgr.registerAgent(desktopAgentRegData);
            Long agentId = agentConfig.getId();
            fileAgentIds.add(agentId);
        }

        //start the desktop agents. We assume that :
        //the first 10 desktop agents did not start
        //the next 20 desktop agents have one port number (6666)
        //the next 20 desktop agents do not send a port number
        Iterator<Long> it = desktopAgentIds.iterator();
        for (int i = 0; i < 10; i++) {
            it.next();
        }
        for (int i = 0; i < 20; i++) {
            Long agentId = (Long) it.next();
            this.agentMgr.startupAgent(agentId, new AgentStartupData(new Integer(6666)));
        }
        for (int i = 0; i < 20; i++) {
            Long agentId = (Long) it.next();
            this.agentMgr.startupAgent(agentId, new AgentStartupData(null));
        }
        //Remember the agents we have created for future cleanup
        this.createdAgentIds.addAll(desktopAgentIds);

        //Start the file server agents. We assume that:
        //The first 10 agents do not send a port number
        //The next 10 agents send a port number 6667
        it = fileAgentIds.iterator();
        for (int i = 0; i < 10; i++) {
            Long agentId = (Long) it.next();
            this.agentMgr.startupAgent(agentId, new AgentStartupData(null));
        }
        for (int i = 0; i < 10; i++) {
            Long agentId = (Long) it.next();
            this.agentMgr.startupAgent(agentId, new AgentStartupData(new Integer(6667)));
        }
        this.createdAgentIds.addAll(fileAgentIds);
    }

    /**
     * Creates a few dummy DABS instances
     */
    protected void createTestComponents() {
        //Create 5 DABS instances. One of them is artificially made inactive
        Transaction t = null;
        Session s = null;
        try {
            s = getManagementDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < 5; i++) {
            	
            	
            	/*
            	 * java.lang.String componentName,
           		 * String componentType,
           		 * org.apache.axis.types.URI eventListenerURL,
           		 * org.apache.axis.types.URI componentURL,
           		 * com.bluejungle.version.types.Version version)
           		 * ApplicationResourceList
            	 */
            	DCCRegistrationInformation regData = new DCCRegistrationInformation(
            			"DABS" + i,
            			ServerComponentType.DABS.getName(),
            			"ICENet Server",
            			new org.apache.axis.types.URI("http://dummyHost/listener/" + i),
            			new org.apache.axis.types.URI("http://dummyHost/component/" + i),
            			new Version(),
            			new ApplicationResourceList()
            			);
            	this.createdDABSs.add(regData);
            	componentService.registerComponent(regData);
//                ComponentDO newDabs = new ComponentDO();
//                DCCRegistrationInformation regInfo = new DCCRegistrationInformation();
//                newDabs.setName("DABS" + i);
//                newDabs.setType(DCCComponentEnumType.DABS);
//                newDabs.setHeartbeatRate(3600); //Large heartbeat
//                Calendar now = Calendar.getInstance();
//                if (i == 3) {
//                    //Make one DABS artificially inactive
//                    newDabs.setHeartbeatRate(5);
//                    now.setTimeInMillis(now.getTimeInMillis() - 7200);
//                }
//                newDabs.setLastHeartbeat(now);
//                String url = "http://dummyHost" + i;
//                newDabs.setCallbackURL(url);
//                newDabs.setComponentURL("");
//                newDabs.setVersion(new VersionDefaultImpl(0,0,0,0,0));
//                this.dccCompMgr.enableComponent(newDabs);
//                s.save(newDabs);
            }
            t.commit();
        } catch (Exception e) {
            HibernateUtils.rollbackTransation(t, null);
            assertFalse("Creation of dummy DABS should succeed " + e, true);
		} finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    private ComponentServiceIF componentService;
    private static final String COMPONENT_SERVICE_LOCATION_SERVLET_PATH = "/services/ComponentServiceIFPort";
    
    private ComponentServiceIF getComponentServiceInterface() throws ServiceException {
        if (this.componentService == null) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            IConfiguration mainCompConfig = (IConfiguration) compMgr.getComponent(IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME);
            String location = (String) mainCompConfig.get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);
            location += COMPONENT_SERVICE_LOCATION_SERVLET_PATH;
            ComponentServiceLocator locator = new ComponentServiceLocator();
            locator.setComponentServiceIFPortEndpointAddress(location);

            this.componentService = locator.getComponentServiceIFPort();
        }

        return this.componentService;
    }

    /**
     * Deletes the DABS components created for the test.
     */
    protected void cleanupDABSComponents() {
        Transaction t = null;
        Session session = null;
        try {
            session = getManagementDateSource().getSession();
            t = session.beginTransaction();
            for (DCCRegistrationInformation regInfo : this.createdDABSs) {
//                Query q = session.createQuery("select dabs from ComponentDO dabs where dabs.id=:id");
//                Long idValue = (Long) it.next();
//                q.setLong("id", idValue.longValue());
//                assertNotSame("dabs not found, id=" + idValue, q.list().isEmpty(), 0);
//                ComponentDO dabs = (ComponentDO) q.list().get(0);
//	            session.delete(dabs);
	            componentService.unregisterComponent(regInfo);
            }
            t.commit();
        } catch (Exception e) {
            HibernateUtils.rollbackTransation(t, null);
            assertTrue("Deployment request deletion should work", false);
        } finally {
            HibernateUtils.closeSession(session, null);
        }
    }

    /**
     * Deletes the deployment request created for the test.
     */
    protected void cleanupDeploymentRequest() {
        Transaction t = null;
        Session s = null;
        try {
            s = getManagementDateSource().getSession();
            Query q = s.createQuery("select request from DeploymentRequestDO request where request.id=:deployReqId");
            q.setLong("deployReqId", this.deployRequestId.longValue());
            Iterator it = q.iterate();
            t = s.beginTransaction();
            while (it.hasNext()) {
                DeploymentRequestDO request = (DeploymentRequestDO) it.next();
                s.delete(request);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            assertTrue("Deployment request deletion should work", false);
        } finally {
            HibernateUtils.closeSession(s, null);
            ;
        }
    }

    /**
     * Deletes the target agent records created in the database.
     */
    protected void cleanupTargetAgents() throws HibernateException {
        Session s = getManagementDateSource().getSession();
        Transaction t = null;
        try {
            Query q = s.createQuery("select agent from TargetAgentDO agent, TargetHostDO host where agent.host.id = host.id AND host.deploymentRequest.id = :deployReqId");
            assertNotNull("Query should not be null ", q);
            assertNotNull("Local deployRequestId should not be null", this.deployRequestId);
            q.setLong("deployReqId", this.deployRequestId.longValue());
            Iterator it = q.iterate();
            t = s.beginTransaction();
            while (it.hasNext()) {
                TargetAgentDO agent = (TargetAgentDO) it.next();
                s.delete(agent);
                //This is just test code, we can flush everytime
                s.flush();
                s.evict(agent);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
            ;
        }
    }
    
    /**
     * Deletes the target agent records created in the database.
     */
    protected void cleanupTargetHosts() throws HibernateException {
        Session s = getManagementDateSource().getSession();
        Transaction t = null;
        try {
            Query q = s.createQuery("select host from TargetHostDO host where host.deploymentRequest.id = :deployReqId");
            assertNotNull("Query should not be null ", q);
            assertNotNull("Local deployRequestId should not be null", this.deployRequestId);
            q.setLong("deployReqId", this.deployRequestId.longValue());
            Iterator it = q.iterate();
            t = s.beginTransaction();
            while (it.hasNext()) {
                TargetHostDO host = (TargetHostDO) it.next();
                s.delete(host);
                //This is just test code, we can flush everytime
                s.flush();
                s.evict(host);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
            ;
        }
    }

    /**
     * Delete the agents that the test created in the database
     * 
     * @throws InvalidIDException
     * @throws HibernateException
     */
    protected void cleanupTestAgents() throws InvalidIDException, PersistenceException, HibernateException {
        IHibernateRepository dataSource = getManagementDateSource();
        Session s = dataSource.getCurrentSession();
        Transaction t = null;
        Iterator it = this.createdAgentIds.iterator();
        try {
            t = s.beginTransaction();
            while (it.hasNext()) {
                Long agentId = (Long) it.next();
                IAgentDO agent = this.agentMgr.getAgentById(agentId);
                s.delete(agent);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
            ;
        }
    }

    /**
     * Returns the data source for the management repository
     * 
     * @return the data source for the management repository
     */
    private IHibernateRepository getManagementDateSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        //Initializes agent manager
        ComponentInfo<IAgentManager> agentMgrInfo = 
            new ComponentInfo<IAgentManager>(
                IAgentManager.COMP_NAME, 
                AgentManager.class, 
                IAgentManager.class, 
                LifestyleType.SINGLETON_TYPE);
        this.agentMgr = compMgr.getComponent(agentMgrInfo);
        
        //Initializes profile manager
        ComponentInfo<IProfileManager> profileMgrInfo = 
            new ComponentInfo<IProfileManager>(
                IProfileManager.COMP_NAME, 
                HibernateProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(profileMgrInfo);

        //Initializes the component registration manager
//        ComponentInfo regMgrCompInfo = new ComponentInfo(IDCCComponentMgr.COMP_NAME, DCCComponentMgrImpl.class.getName(), IDCCComponentMgr.class.getName(), LifestyleType.TRANSIENT_TYPE);
//        this.dccCompMgr = (IDCCComponentMgr) compMgr.getComponent(regMgrCompInfo);

        //create dummy configuration
        ComponentInfo<HashMapConfiguration> mainCompInfo = 
            new ComponentInfo<HashMapConfiguration>(
                IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME, 
                HashMapConfiguration.class, 
                IConfiguration.class, 
                LifestyleType.SINGLETON_TYPE);
        HashMapConfiguration mainCompConfig = (HashMapConfiguration) compMgr.getComponent(mainCompInfo);
        mainCompConfig.setProperty(IDCCContainer.DMS_LOCATION_CONFIG_PARAM, "http://localhost:8081/dms");
        

        //IDestinySharedContextLocator
        ComponentInfo<IDestinySharedContextLocator> contextLocatorCompInfo = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
        		MockSharedContextLocator.class, 
        		IDestinySharedContextLocator.class,
        		LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(contextLocatorCompInfo);
        
        getComponentServiceInterface();
        
        createTestAgents();
        createTestComponents();
        
        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
//        if(this.deployRequestId!=null){
//	    	cleanupTargetAgents();
//	    	cleanupTargetHosts();
//	        cleanupTestAgents();
//	        cleanupDABSComponents();
//	        cleanupDeploymentRequest();
//	        this.agentMgr = null;
//	        this.createdAgentIds.clear();
//	        this.createdDABSIds.clear();
//	        this.dccCompMgr = null;
//	        this.deployRequestId = null;
//        }
        super.tearDown();
    }

    /**
     * Sample test code. More tests to come.
     * @throws UnknownDeploymentIdException
     * @throws RequestAlreadyExecutedException
     */
    public void testSampleTest() throws RequestAlreadyExecutedException, UnknownDeploymentIdException, HibernateException {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        
        IHibernateRepository mgmtDataSource = getManagementDateSource();
        
       	HashMapConfiguration config = new HashMapConfiguration();
       	config.setProperty(IDeploymentRequestMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSource);
       	ComponentInfo<IDeploymentRequestMgr> reqMgrCompInfo = 
       	    new ComponentInfo<IDeploymentRequestMgr>(
       	        "test", 
       	        DeploymentRequestMgrImpl.class, 
       	        IDeploymentRequestMgr.class, 
       	        LifestyleType.TRANSIENT_TYPE, 
       	        config);
       	IDeploymentRequestMgr depReqMgr = compMgr.getComponent(reqMgrCompInfo);
        
        config = new HashMapConfiguration();
        config.setProperty(IPolicyDeployMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSource);
        ComponentInfo<IPolicyDeployMgr> deployMgrCompInfo = 
            new ComponentInfo<IPolicyDeployMgr>(
                IPolicyDeployMgr.COMP_NAME, 
                PolicyDeployMgrImpl.class, 
                IPolicyDeployMgr.class, 
                LifestyleType.TRANSIENT_TYPE, 
                config);
        IPolicyDeployMgr deployMgr = compMgr.getComponent(deployMgrCompInfo);
        
        IDeploymentRequest deployRequest = depReqMgr.createDeploymentRequest();
        this.deployRequestId = deployRequest.getId();
        for (int i = 0; i < 55; i++) {
            deployRequest.addTargetHost("localhost" + i);
        }
        depReqMgr.saveDeploymentRequest(deployRequest);
        
        assertTrue(true);

        Session s = mgmtDataSource.getSession();
        s.clear();

        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        MockEventManager mockEventManager = (MockEventManager)locator.getSharedContext().getEventManager();

        assertEquals("Some event has been fired before the deployment request. ", 0, mockEventManager.getNumEventsFired());
        deployMgr.executeDeploymentRequest(this.deployRequestId);
        assertEquals("Event has not been fired. ", 1, mockEventManager.getNumEventsFired());
        HibernateUtils.closeSession(s, null);
        
    }

    /**
     * Test agent startup data. Simply specifies a port number.
     * 
     * @author ihanen
     */
    private class AgentStartupData implements IAgentStartupData {

        private Integer pushPort;

        /**
         * Constructor
         * 
         * @param newPushPort
         *            push port enabled on the agent
         */
        public AgentStartupData(Integer newPushPort) {
            super();
            this.pushPort = newPushPort;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData#getPushPort()
         */
        public Integer getPushPort() {
            return this.pushPort;
        }
    }

    /**
     * Test agent registration data class
     * 
     * @author ihanen
     */
    private class AgentRegData implements IAgentRegistrationData {

        private String hostName;
        private IAgentType hostType;
        private IVersion version;

        /**
         * Constructor
         */
        public AgentRegData(String hostName, IAgentType desktopAgentType) {
            super();
            this.hostName = hostName;
            this.hostType = desktopAgentType;
            this.version = new VersionDefaultImpl(0,0,0,0,0);
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getHost()
         */
        public String getHost() {
            return this.hostName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getType()
         */
        public IAgentType getType() {
            return this.hostType;
        }
        
        /**
         * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getVersion()
         */
        public IVersion getVersion() {
            return this.version;
        }
    }
}
