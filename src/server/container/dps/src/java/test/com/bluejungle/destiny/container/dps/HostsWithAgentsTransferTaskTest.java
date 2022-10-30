/*
 * Created on Nov 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dps;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.MockAgentRegistrationData;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * Unit tests for HostsWithAgentsTransferTask
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dps/src/java/test/com/bluejungle/destiny/container/dps/HostsWithAgentsTransferTaskTest.java#3 $
 */

public class HostsWithAgentsTransferTaskTest extends BaseDPSComponentTest {

    private static final String ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME = "HOST/Computers With Enforcers";
    private static final String HOSTNAME_ONE_A = "HostOneA.foobar.com";
    private static final String HOSTNAME_ONE_B = "HostOneB.foobar.com";
    private static final String HOSTNAME_TWO = "HostTwo.foobar.com";
    private static final String INITIAL_PQL =
        " STATUS APPROVED CREATOR \"0\" \r\n"
    +   "ACCESS_POLICY\r\n"
    +   "ACCESS_CONTROL\r\n"
    +   "    PBAC \r\n"
    +   "        FOR TRUE\r\n"
    +   "        ON TRUE\r\n"
    +   "        BY appuser.did = resource.dso.owner\r\n"
    +   "        DO allow\r\n"
    +   "ALLOWED_ENTITIES \r\n"
    +   "COMPONENT \"HOST/Computers With Enforcers\" = (((FALSE OR TRUE) AND TRUE) AND (TRUE AND TRUE))\r\n";

    private static final String EXPECTED_PQL_ONE =
        " STATUS APPROVED CREATOR \"0\" \r\n"
    +   "ACCESS_POLICY\r\n"
    +   "ACCESS_CONTROL\r\n"
    +   "    PBAC \r\n"
    +   "        FOR TRUE\r\n"
    +   "        ON TRUE\r\n"
    +   "        BY appuser.did = resource.dso.owner\r\n"
    +   "        DO allow\r\n"
    +   "ALLOWED_ENTITIES \r\n"
    +   "COMPONENT \"HOST/Computers With Enforcers\" = (((host.name = \""
    +   HOSTNAME_ONE_A
    +   "\" OR host.name = \""
    +   HOSTNAME_ONE_B
    +   "\") AND TRUE) AND (TRUE AND TRUE))\r\n";

    private static final String EXPECTED_PQL_TWO =
        " STATUS APPROVED CREATOR \"0\" \r\n"
    +   "ACCESS_POLICY\r\n"
    +   "ACCESS_CONTROL\r\n"
    +   "    PBAC \r\n"
    +   "        FOR TRUE\r\n"
    +   "        ON TRUE\r\n"
    +   "        BY appuser.did = resource.dso.owner\r\n"
    +   "        DO allow\r\n"
    +   "ALLOWED_ENTITIES \r\n"
    +   "COMPONENT \"HOST/Computers With Enforcers\" = (((host.name = \""
    +   HOSTNAME_ONE_B
    +   "\" OR host.name = \""
    +   HOSTNAME_TWO
    +   "\") AND TRUE) AND (TRUE AND TRUE))\r\n";

    private HostsWithAgentsTransferTask taskToTest;
    private AgentManager agentManager;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentManager = (AgentManager) componentManager.getComponent(agentMgrCompInfo);
        
        // Initialize the profile manager:
        ComponentInfo profileMgrCompInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        componentManager.getComponent(profileMgrCompInfo);

        this.taskToTest = (HostsWithAgentsTransferTask) componentManager.getComponent(HostsWithAgentsTransferTask.class);

        deleteAllAgents();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteAllAgents();
        super.tearDown();
    }

    /*
     * Test method for
     * 'com.bluejungle.destiny.container.dps.HostsWithAgentsTransferTask.run()'
     */
    public void testRun() throws EntityManagementException, PersistenceException, PQLException {
        // First, ensure that
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        LifecycleManager lm = (LifecycleManager) componentManager.getComponent(LifecycleManager.COMP_INFO);
        DevelopmentEntity allHostsWithAgentsEntity = lm.getEntityForName(EntityType.COMPONENT, ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME, LifecycleManager.MAKE_EMPTY);
        String pql = allHostsWithAgentsEntity.getPql();

        /**
         * The test below belong in a build-schema test. It's being commented
         * out because with it this test cannot be run more than once
         */
        // assertEquals("testRun - Ensure initial pql for computers with agents
        // is as expected.", prependIdPQL(allHostsWithAgentsEntity.getId(),
        // INITIAL_PQL), pql);
        // Run update with no agents. Make sure pql is not updated
        this.taskToTest.run();
        allHostsWithAgentsEntity = lm.getEntityForName(EntityType.COMPONENT, ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME, LifecycleManager.MAKE_EMPTY);
        pql = allHostsWithAgentsEntity.getPql();        
        assertEquals("testRun - Ensure pql for computers with agents is as expected after update with no registerd agents.", prependIdPQL(allHostsWithAgentsEntity.getId(), INITIAL_PQL), pql);

        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        IAgentType fileServerAgentType = this.agentManager.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        MockAgentRegistrationData agentData = new MockAgentRegistrationData(HOSTNAME_ONE_A, desktopAgentType);
        IAgentStartupConfiguration firstAgentRegistered = this.agentManager.registerAgent(agentData);
        agentData = new MockAgentRegistrationData(HOSTNAME_ONE_B, desktopAgentType);
        this.agentManager.registerAgent(agentData);

        // Add a file server to make sure it is not picked up
        agentData = new MockAgentRegistrationData("fileserver.foobar.com", fileServerAgentType);
        this.agentManager.registerAgent(agentData);

        // Now, run update
        this.taskToTest.run();

        allHostsWithAgentsEntity = lm.getEntityForName(EntityType.COMPONENT, ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME, LifecycleManager.MAKE_EMPTY);
        pql = allHostsWithAgentsEntity.getPql();
        assertEquals("testRun - Ensure first update pql for computers with agents is as expected.", prependIdPQL(allHostsWithAgentsEntity.getId(), EXPECTED_PQL_ONE), pql);

        // Add another host
        agentData = new MockAgentRegistrationData(HOSTNAME_TWO, desktopAgentType);
        this.agentManager.registerAgent(agentData);

        // Remove the first one
        this.agentManager.deleteAgent(firstAgentRegistered.getId());

        // Now, run update
        this.taskToTest.run();

        allHostsWithAgentsEntity = lm.getEntityForName(EntityType.COMPONENT, ALL_HOSTS_WITH_AGENTS_COMPONENT_NAME, LifecycleManager.MAKE_EMPTY);
        pql = allHostsWithAgentsEntity.getPql();
        assertEquals("testRun - Ensure second update pql for computers with agents is as expected.", prependIdPQL(allHostsWithAgentsEntity.getId(), EXPECTED_PQL_TWO), pql);
    }

    /**
     * Append the id to form a full pql expression
     * 
     * @param id
     * @return the pql expression with the full id
     */
    private String prependIdPQL(Long id, String pql) {
        return "ID " + id + pql;
    }

    /**
     * Delete all agents
     * 
     * @throws PersistenceException
     */
    private void deleteAllAgents() throws PersistenceException {
        //TODO - That sounds like a lot of deletes!!!
        IAgentMgrQueryTermFactory queryTermFactory = this.agentManager.getAgentMgrQueryTermFactory();
        AgentQuerySpecImpl searchSpec = new AgentQuerySpecImpl();
        searchSpec.setLimit(0);
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        searchSpec.addSearchSpecTerm(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.TYPE, RelationalOp.EQUALS, desktopAgentType));
        IAgentQueryResults agentQueryResults = null;
        try {
            agentQueryResults = this.agentManager.getAgents(searchSpec);
        } catch (InvalidQuerySpecException e) {
            throw new PersistenceException(e);
        }
        if (agentQueryResults != null) {
            IAgentDO[] agentsToDelete = agentQueryResults.getAgents();
            int size = agentsToDelete.length;
            for (int i = 0; i < size; i++) {
                this.agentManager.deleteAgent(agentsToDelete[i].getId());
            }
        }
    }
}
