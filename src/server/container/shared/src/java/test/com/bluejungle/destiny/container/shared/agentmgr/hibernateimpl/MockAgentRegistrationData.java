/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IActionType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;

/**
 * Mock agent registration data object
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/MockAgentRegistrationData.java#1 $
 */

public class MockAgentRegistrationData implements IAgentRegistrationData {

    private String host;
    private IAgentType type;
    private IVersion version;

    public MockAgentRegistrationData(String host, IAgentType type) {
        this.host = host;
        this.type = type;
        //TODO: Robert, this is a hack, need to fix this
        int major = 2;
        int minor = 12;
        int maintenance = 1;
        int patch = 3;
        int build = 329;
        this.version = new VersionDefaultImpl(major, minor, maintenance, patch, build);
    }

    /**
     * Sets the host
     * 
     * @param host
     *            The host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getHost()
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets the type
     * 
     * @param type
     *            The type to set.
     */
    public void setType(IAgentType type) {
        this.type = type;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getType()
     */
    public IAgentType getType() {
        return this.type;
    }

    /**
     * Sets the version
     * 
     * @param version
     *            The version to set.
     */
    public void setVersion(IVersion version) {
        this.version = version;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData#getVersion()
     */
    public IVersion getVersion() {
        return this.version;
    }
}
