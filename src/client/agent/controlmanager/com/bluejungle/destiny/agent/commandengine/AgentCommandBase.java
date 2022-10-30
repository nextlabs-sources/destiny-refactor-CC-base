/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ILogEnabled;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public abstract class AgentCommandBase implements IAgentCommand, ILogEnabled {

    protected Log log;

    /**
     * 
     * Sets the member paramArray to the paramArray passed in.
     * 
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#init(java.util.ArrayList)
     */
    public void init(ArrayList paramArray) {
    }

    /**
     * 
     * This method needs to be overriden by Commands that can be called from the
     * server
     * 
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#init(com.bluejungle.destiny.agent.commandengine.CommandSpecBase)
     */
    public void init(CommandSpecBase commandSpec) {
        getLog().error("Init method not implemented.");
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public CommunicationType getCommunicationType() {
     // everyone use the NETWORK unless specified
        return CommunicationType.NETWORK;
    }
}
