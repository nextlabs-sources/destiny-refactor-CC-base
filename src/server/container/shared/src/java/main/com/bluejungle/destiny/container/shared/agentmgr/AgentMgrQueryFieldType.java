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
 * This is the enumeration class for the agent fields that can be queried.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/AgentMgrQueryFieldType.java#1 $
 */

public class AgentMgrQueryFieldType extends EnumBase {

    /**
     * Constants
     */
    public static AgentMgrQueryFieldType COMM_PROFILE_ID = new AgentMgrQueryFieldType("COMM_PROFILE_ID");
    public static AgentMgrQueryFieldType HOST = new AgentMgrQueryFieldType("HOST");
    public static AgentMgrQueryFieldType ID = new AgentMgrQueryFieldType("ID");
    public static AgentMgrQueryFieldType LAST_POLICY_UPDATE = new AgentMgrQueryFieldType("POLICY_UP_TO_DATE");
    public static AgentMgrQueryFieldType ONLINE = new AgentMgrQueryFieldType("ONLINE");
    public static AgentMgrQueryFieldType REGISTERED = new AgentMgrQueryFieldType("REGISTERED");
    public static AgentMgrQueryFieldType TYPE = new AgentMgrQueryFieldType("TYPE");

    /**
     * Constructor
     * 
     * @param name
     */
    public AgentMgrQueryFieldType(String name) {
        super(name);
    }

    /**
     * Returns an enum based on its name
     * 
     * @param name
     *            name of the enumerated object
     * @return the enumeration corresponding to the name
     */
    public static AgentMgrQueryFieldType fromString(String name) {
        return AgentMgrQueryFieldType.getElement(name, AgentMgrQueryFieldType.class);
    }

}
