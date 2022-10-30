/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.DeploymentEvents;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetAgentDO;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IWorker;

/**
 * This is the policy deployment test class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/deployment/PolicyDeploymentTest.java#1 $
 */

public class PolicyDeploymentTest extends BaseDabsComponentTest {

    private IProfileManager profileMgr;
    private IAgentManager agentMgr;

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public PolicyDeploymentTest(String testName) {
        super(testName);
    }

    /**
     * Returns a list of dummy agent with push configured
     * 
     * @param hostName
     *            hostname where the agent is supposed to be
     * @param port
     *            port on which the agent is supposed to listen
     * @param nbAgents
     *            nb of agents to generate
     * @return a list of dummy agent with push configured
     */
    protected List createTaskList(final String hostName, final Integer port, int nbAgents) {
        List taskList = new ArrayList();
        for (int i = 0; i < nbAgents; i++) {
            TargetAgentDO targetAgent = new TargetAgentDO();
            AgentDO agent = new AgentDO();
            agent.setHost(hostName);
            agent.setPushPort(port);
            targetAgent.setAgent(agent);
            targetAgent.setStatus(ITargetStatus.NOT_STARTED);
            taskList.add(targetAgent);
        }
        return taskList;
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
     * Returns the policy deployment manager
     * 
     * @return the policy deployment manager
     */
    private IPolicyDeploymentManager getPolicyDeploymentManager() {
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPolicyDeploymentManager.DABS_COMPONENT_NAME, "dabs-1");
        config.setProperty(IPolicyDeploymentManager.MGMT_DATA_SOURCE_CONFIG_PARAM, getManagementDateSource());
        ComponentInfo compInfo = new ComponentInfo("policyDeploymentMgr", PolicyDeploymentManagerImpl.class.getName(), IPolicyDeploymentManager.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IPolicyDeploymentManager dabsPolicyDeployMgr = (IPolicyDeploymentManager) compMgr.getComponent(compInfo);
        return (dabsPolicyDeployMgr);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(MockSharedContextLocator.class);

        //Initializes agent manager
        ComponentInfo agentMgrInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentMgr = (IAgentManager) compMgr.getComponent(agentMgrInfo);
        
        //Initializes profile manager
        ComponentInfo profileMgrInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.profileMgr = (IProfileManager) compMgr.getComponent(profileMgrInfo);
    }

    /**
     * This test verifies that the class gets created properly and checks for
     * the correct configuration
     */
    public void testPolicyDeploymentComponentInstantiation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        //Test with bad config -- missing component name
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IPolicyDeploymentManager.MGMT_DATA_SOURCE_CONFIG_PARAM, getManagementDateSource());
        ComponentInfo compInfo = new ComponentInfo("temporary", PolicyDeploymentManagerImpl.class.getName(), IPolicyDeploymentManager.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        boolean exThrown = false;
        try {
            compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("DABS Policy deployment manager should not accept invalid config.", exThrown);

        //Another bad config -- missing data source
        config.setProperty(IPolicyDeploymentManager.DABS_COMPONENT_NAME, "dabs-1");
        config.setProperty(IPolicyDeploymentManager.MGMT_DATA_SOURCE_CONFIG_PARAM, null);
        compInfo = new ComponentInfo("anotherBadOne", PolicyDeploymentManagerImpl.class.getName(), IPolicyDeploymentManager.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        exThrown = false;
        try {
            compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("DABS Policy deployment manager should not accept invalid config.", exThrown);
        exThrown = false;

        //Test with good config
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(MockSharedContextLocator.class);
        MockSharedContextLocator.MockEventMgr mockEvtMgr = (MockSharedContextLocator.MockEventMgr) locator.getSharedContext().getEventManager();
        mockEvtMgr.reset();
        IPolicyDeploymentManager policyDeploymentMgr = getPolicyDeploymentManager();
        assertNotNull("DABS policy deployment Manager should be created properly", policyDeploymentMgr);
        assertEquals("DABS policy deployment Manager should register for 2 events upon startup", 2, mockEvtMgr.getSubscriptionSize());
        mockEvtMgr.reset();
    }

    /**
     * Test the deployment sequence
     */
    public void testPushDeploymentSequence() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IPolicyDeploymentManager policyDeployMgr = getPolicyDeploymentManager();
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(MockSharedContextLocator.class);
        MockSharedContextLocator.MockEventMgr mockEvtMgr = (MockSharedContextLocator.MockEventMgr) locator.getSharedContext().getEventManager();
        final IDCCServerEvent updateEvent = new DCCServerEventImpl(DeploymentEvents.POLICY_UPDATES_AVAILABLE);
        mockEvtMgr.fireEvent(updateEvent);
        final IDCCServerEvent pushEvent = new DCCServerEventImpl(DeploymentEvents.POLICY_PUSH_AVAILABLE);
        pushEvent.getProperties().setProperty(DeploymentEvents.POLICY_PUSH_ID_PROP, "25");
        mockEvtMgr.fireEvent(pushEvent);
    }

    /**
     * This test verifies that the thread pool works properly. It directs the
     * push to some dummy location, and makes sure that all results have been
     * processed (and failed).
     */
    public void testPushThreadPool() {
        final int nbTargets = 50;
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        PolicyDeploymentManagerImpl policyDeployMgr = (PolicyDeploymentManagerImpl) getPolicyDeploymentManager();

        //Creates a list of tasks to perform
        List taskList = createTaskList("cuba", new Integer(1234), nbTargets);
        //Performs all the tasks
        List results = policyDeployMgr.processPushAssignments(taskList);

        //Walk through the list of results
        assertEquals("All target agents should be processed by the thread pool", nbTargets, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            ITargetAgent targetAgent = (ITargetAgent) it.next();
            assertEquals("All push requests should have failed", ITargetStatus.FAILED, targetAgent.getStatus());
        }
    }

    /*
     * This test verifies the push request object
     *
     * This test is commented out because push request
     * is not currently an active feature.
     * /
    public void testPushRequest() {
        boolean exThrown = false;
        try {
            PushRequest request = new PushRequest(null, null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Push request should not accept null pointers", exThrown);

        exThrown = false;
        try {
            PushRequest request = new PushRequest(new TargetAgentDO(), null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Push request should not accept null pointers", exThrown);

        exThrown = false;
        try {
            PushRequest request = new PushRequest(null, new PushRequestCounter(25));
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Push request should not accept null pointers", exThrown);

        TargetAgentDO targetAgent = new TargetAgentDO();
        PushRequestCounter counter = new PushRequestCounter(25);
        PushRequest request = new PushRequest(targetAgent, counter);
        assertTrue("Push request should be a thread task", request instanceof ITask);
    }*/

    /**
     * This test verifies that the result status are accurate. Results should be
     * marked as failed if connection failed, or successful if push worked.
     */
    public void testPushResultStatus() {
        final int nbCuba = 30;
        final int nbLocalhost = 20;
        final int goodLocalhostPort = 5555;
        final int badLocalhostPort = 5554;
        final int nbBadLocalhost = 10;
        List allTasks = createTaskList("cuba", new Integer(1234), nbCuba);
        allTasks.addAll(createTaskList("localhost", new Integer(goodLocalhostPort), nbLocalhost));
        allTasks.addAll(createTaskList("localhost", new Integer(badLocalhostPort), nbBadLocalhost));

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        PolicyDeploymentManagerImpl policyDeployMgr = (PolicyDeploymentManagerImpl) getPolicyDeploymentManager();

        MockAgentListener portListener = new MockAgentListener(goodLocalhostPort);
        Thread portListenerThread = new Thread(portListener);
		// In case something goes wrong, make sure the thread will
		// die, even when blocked on IO
		portListenerThread.setDaemon(true);  
        portListenerThread.start();
        List results = policyDeployMgr.processPushAssignments(allTasks);
        portListenerThread.interrupt();

        //Make sure all the results are still there
        assertEquals("No result should be lost in the result list", nbLocalhost + nbCuba + nbBadLocalhost, results.size());

        //Count the results for each status
        Map statusMap = new HashMap();
        statusMap.put(ITargetStatus.FAILED, new Integer(0));
        statusMap.put(ITargetStatus.IN_PROGRESS, new Integer(0));
        statusMap.put(ITargetStatus.NOT_STARTED, new Integer(0));
        statusMap.put(ITargetStatus.SUCCEEDED, new Integer(0));

        Iterator it = results.iterator();
        while (it.hasNext()) {
            ITargetAgent agent = (ITargetAgent) it.next();
            ITargetStatus status = agent.getStatus();
            int count = ((Integer) statusMap.get(status)).intValue();
            statusMap.put(status, new Integer(++count));
        }

        assertEquals("Push should fail when port is not listening", nbCuba + nbBadLocalhost, ((Integer) statusMap.get(ITargetStatus.FAILED)).intValue());
        assertEquals("Push should succeed when port is listening", nbLocalhost, ((Integer) statusMap.get(ITargetStatus.SUCCEEDED)).intValue());
        assertEquals("Push status not be marked an not started", 0, ((Integer) statusMap.get(ITargetStatus.NOT_STARTED)).intValue());
        assertEquals("Push status not be marked an in progress", 0, ((Integer) statusMap.get(ITargetStatus.IN_PROGRESS)).intValue());
    }

    /**
     * Verifies the push worker thread implementation
     */
    public void testPushWorkerThread() {
        PushWorkerThreadImpl workerThread = new PushWorkerThreadImpl();
        assertTrue("Worker thread should implement the right interface", workerThread instanceof IPushWorker);
        assertTrue("Worker thread should implement the right interface", workerThread instanceof IWorker);
        boolean exThrown = false;
        try {
            workerThread.doWork(null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("Push worker thread should not accept null task", exThrown);

        exThrown = false;
        try {
            workerThread.doWork(new DummyTask());
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("Push worker thread should not accept task except push request", exThrown);
    }

    /**
     * Dummy task class
     * 
     * @author ihanen
     */
    private class DummyTask implements ITask {
    }
}
