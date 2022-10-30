/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData;

/**
 * Mock startup-agent data for JUnit testing purposes
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/MockStartupAgentData.java#1 $
 */

public class MockStartupAgentData implements IAgentStartupData {

    private Integer pushPort;

    /**
     * 
     * Constructor
     * 
     * @param pushPort
     */
    public MockStartupAgentData(Integer pushPort) {
        this.pushPort = pushPort;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData#getPushPort()
     */
    public Integer getPushPort() {
        return this.pushPort;
    }

}