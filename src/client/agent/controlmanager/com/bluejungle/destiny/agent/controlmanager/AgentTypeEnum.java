/*
 * Created on Oct 24, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/AgentTypeEnum.java#1 $:
 */

public class AgentTypeEnum extends EnumBase {

    public static final AgentTypeEnum DESKTOP= new AgentTypeEnum ("DESKTOP");
    public static final AgentTypeEnum FILE_SERVER = new AgentTypeEnum ("FILE_SERVER");
    public static final AgentTypeEnum PORTAL = new AgentTypeEnum ("PORTAL");
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected AgentTypeEnum(String name) {
        super(name);
    }

    /**
     * Retrieve an AgentTypeEnum instance by name
     * 
     * @param name
     *            the name of the AgentTypeEnum
     * @return the AgentTypeEnum associated with the provided name
     * @throws IllegalArgumentException
     *             if no AgentTypeEnum exists with the specified name
     */
    public static AgentTypeEnum getAgentTypeEnum(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        return getElement(name, AgentTypeEnum.class);
       
    }
}
