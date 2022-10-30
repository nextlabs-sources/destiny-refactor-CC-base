/*
 * Created on Jan 24, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * This class implements the heartbeat command.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class HeartBeatCommand extends AgentCommandBase {

    /**
     * Calls sendHeartBeat on the Control Manager to send a heartbeat to the
     * server
     * 
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#execute()
     */
    public int execute() {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            this.log.fatal("Cannot send heartbeat. Control Manager not available.");
            return ErrorCode.FAILURE;
        }
        
        controlManager.sendHeartBeat();
        
        return ErrorCode.SUCCESS;
    }

}