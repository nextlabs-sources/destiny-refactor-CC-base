/*
 * Created on Mar 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the agent manager sort field type. This enumeration class represents
 * the various sort fields that the caller can sort on.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/AgentMgrSortFieldType.java#1 $
 */

public class AgentMgrSortFieldType extends EnumBase {

    /**
     * Constants
     */
    public static AgentMgrSortFieldType HOST = new AgentMgrSortFieldType("HOST");
    public static AgentMgrSortFieldType LAST_HEARTBEAT = new AgentMgrSortFieldType("LAST_HEARTBEAT");
    public static AgentMgrSortFieldType LAST_POLICY_UPDATE = new AgentMgrSortFieldType("LAST_POLICY_UPDATE");
    public static AgentMgrSortFieldType PROFILE_NAME = new AgentMgrSortFieldType("PROFILE");
    public static AgentMgrSortFieldType TYPE = new AgentMgrSortFieldType("TYPE");

    /**
     * Constructor
     * 
     * @param name
     */
    public AgentMgrSortFieldType(String name) {
        super(name);
    }

    /**
     * Returns the AgentMgrSortFieldType instance associated with a name
     * 
     * @param name
     *            name of the AgentMgrSortFieldType instance
     * @return the corresponding AgentMgrSortFieldType instance
     */
    public static AgentMgrSortFieldType fromString(String name) {
        return AgentMgrSortFieldType.getElement(name, AgentMgrSortFieldType.class);
    }

}
