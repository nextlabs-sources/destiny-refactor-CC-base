/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.services.agent.AgentServiceLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;

import com.nextlabs.destiny.services.log.v5.LogServiceV5Locator;

/**
 * This class extends CommunicationManager for testing purposes. The only
 * behavior that is different is that it returns Mock service locator classes
 * rather than the real ones.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class MockCommunicationManager extends CommunicationManager {

    private static final ComponentInfo<MockCommunicationManager> COMP_INFO =
			new ComponentInfo<MockCommunicationManager>(
    		ICommunicationManager.NAME, 
    		MockCommunicationManager.class, 
    		LifestyleType.SINGLETON_TYPE);

    /**
     * The Communication Manager is a singleton. It will be instantiated when
     * the agent starts up. Components that need it can get it from the
     * component manager.
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see com.bluejungle.destiny.agent.communication.CommunicationManager#getAgentServiceLocator()
     */
    protected AgentServiceLocator getAgentServiceLocator() {
        return new MockAgentServiceLocator();
    }

    /**
     * @see com.bluejungle.destiny.agent.communication.CommunicationManager#getLogServiceLocator()
     */
    protected LogServiceV5Locator getLogServiceLocator() {
        return new MockLogServiceLocator();
    }
    
    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        IControlManager controlManager = (IControlManager)config.get(IControlManager.NAME);
        if(controlManager == null){
            throw new NullPointerException("controlManager, please set config '" + IControlManager.NAME + "'");
        }
        HashMapConfiguration newConfig = new HashMapConfiguration();
        newConfig.override(config);
        
        newConfig.setProperty(CommunicationManager.COMM_PROFILE_CONFIG_KEY, controlManager.getCommunicationProfile());
        
        super.setConfiguration(newConfig);
    }

    @Override
    public void reinit() {
		// the profile is changed, need set the configuration again to get the latest commProfile
        setConfiguration(getConfiguration());
        super.reinit();
    }
    
    
}
