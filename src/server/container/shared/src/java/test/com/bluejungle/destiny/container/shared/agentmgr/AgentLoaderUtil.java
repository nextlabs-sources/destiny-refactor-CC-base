/* Created on May 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import org.apache.commons.lang.RandomStringUtils;

import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.MockAgentRegistrationData;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * AgentLoaderUtil adds test agents to the database
 * 
 * Note that we extends the base container shared test case in order to load the
 * data sources. Not ideal, but it saves a lot of time that we don't have at the
 * moment
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/AgentLoaderUtil.java#1 $
 */
public class AgentLoaderUtil extends BaseContainerSharedTestCase {
    private static final int DEFAULT_NUM_AGENTS = 100;
    private AgentManager agentManager;
    
    /**
     * Create an instance of AgentLoaderUtil
     * @throws Exception
     *  
     */
    public AgentLoaderUtil() throws Exception {
        super();
        super.setUp();
        
        //      Initialize the necessary manager objects:
        // Initialize the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentManager = (AgentManager) compMgr.getComponent(agentMgrCompInfo);
        
        // Initialize the profile manager:
        ComponentInfo profileMgrCompInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        compMgr.registerComponent(profileMgrCompInfo, true);

        // Setup the mock shared context
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(locatorInfo);

    }

    /**
     * @param numAgents 
     * @throws PersistenceException
     * 
     */
    private void loadAgents(int numAgents) throws PersistenceException {
        // Load 50 desktop agents
        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());
        for (int i = 0; i < numAgents; i++) {
            String randomString = RandomStringUtils.random(5, true, false);
            MockAgentRegistrationData desktopAgent = new MockAgentRegistrationData(randomString + "-DesktopAgent" + i + "foo.com", desktopAgentType);
            this.agentManager.registerAgent(desktopAgent);            
        }

        // Load 50 file server agents
        IAgentType fileServerAgentType = this.agentManager.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        for (int i = 0; i < numAgents; i++) {
            String randomString = RandomStringUtils.random(5, true, false);
            MockAgentRegistrationData fileServerAgent = new MockAgentRegistrationData(randomString + "-FileServerAgent" + i + "foo.com", fileServerAgentType);
            this.agentManager.registerAgent(fileServerAgent);
        }
    }
    
    public static void main(String[] args) throws Exception {
        AgentLoaderUtil agentLoader = new AgentLoaderUtil();
        
        int numAgents = DEFAULT_NUM_AGENTS;
        if (args.length > 0) {
            numAgents = Integer.parseInt(args[0]);
        }
        agentLoader.loadAgents(numAgents);
    }

}
