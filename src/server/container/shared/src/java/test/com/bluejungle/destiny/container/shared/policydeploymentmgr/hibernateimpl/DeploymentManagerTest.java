/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

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
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.version.IVersion;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/policydeploymentmgr/hibernateimpl/test/DeploymentManagerTest.java#1 $
 */

public class DeploymentManagerTest extends BaseContainerSharedTestCase {

    private IAgentManager agentMgr;
    private Set createdAgentIds = new HashSet();

    /**
     * Constructor
     */
    public DeploymentManagerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public DeploymentManagerTest(String testName) {
        super(testName);
    }

    /**
     * This function creates a set of agents used for the test. Agents have
     * different characteristics. Some are push enabled, some are not, some have
     * different push port number, etc.
     * 
     * @throws PersistenceException
     *             if agent creation fails.
     * @throws InvalidIDException
     */
    protected void createTestAgents() throws PersistenceException, InvalidIDException {

        List desktopAgentIds = new ArrayList();
        List fileAgentIds = new ArrayList();

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
        Iterator it = desktopAgentIds.iterator();
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
     * Delete the agents that the test created in the database
     * 
     * @throws PersistenceException
     * @throws InvalidIDException
     * @throws HibernateException
     */
    protected void cleanupTestAgents() throws InvalidIDException, PersistenceException, HibernateException {
        IHibernateRepository dataSource = getManagementDateSource();
        Session session = dataSource.getCurrentSession();
        Transaction t = null;
        try {
            Iterator it = this.createdAgentIds.iterator();
            t = session.beginTransaction();
            while (it.hasNext()) {
                Long agentId = (Long) it.next();
                IAgentDO agent = this.agentMgr.getAgentById(agentId);
                session.delete(agent);
            }
            t.commit();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        } finally {
            HibernateUtils.rollbackTransation(t, null);
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
        ComponentInfo agentMgrInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentMgr = (IAgentManager) compMgr.getComponent(agentMgrInfo);
        
        //Initializes profile manager
        ComponentInfo profileMgrInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(profileMgrInfo);

        createTestAgents();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        cleanupTestAgents();

        this.agentMgr = null;
        super.tearDown();
    }

    /**
     *  
     */
    public void NoTest() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        IHibernateRepository mgmtDataSource = getManagementDateSource();
        config.setProperty(IDeploymentRequestMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSource);
        ComponentInfo compInfo = new ComponentInfo("test", DeploymentRequestMgrImpl.class.getName(), IDeploymentRequestMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IDeploymentRequestMgr depReqMgr = (IDeploymentRequestMgr) compMgr.getComponent(compInfo);
        IDeploymentRequest deployRequest = depReqMgr.createDeploymentRequest();
        for (int i = 0; i < 55; i++) {
            deployRequest.addTargetHost("localhost" + i);
        }
        depReqMgr.saveDeploymentRequest(deployRequest);
        assertTrue(true);

        Long id = deployRequest.getId();
        Session s = null;
        try {
            s = mgmtDataSource.getSession();
            s.clear();

            Criteria crit = s.createCriteria(DeploymentRequestDO.class);
            List reqs = null;
            reqs = crit.list();
            DeploymentRequestDO result1 = (DeploymentRequestDO) reqs.get(0);
            result1.addTargetHost("localhost_");
            List targets = result1.getTargetHosts();
            int j = targets.size();
            depReqMgr.saveDeploymentRequest(result1);

            Query q = s.createQuery("select targetAgent from AgentDO targetAgent, TargetHostDO targetHost where targetAgent.isPushReady='1' AND targetAgent.host= targetHost.hostname and targetHost.deploymentRequest.id = '"
                    + id + "'");
            //SELECT * FROM dms_agent where isActive=1 AND isPushReady='1'
            List results = q.list();
            int i = 0;
        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            HibernateUtils.closeSession(s, null);
        }
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
        public AgentRegData(String hostName, IAgentType type) {
            super();
            this.hostName = hostName;
            this.hostType = type;
            this.version = null;
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
