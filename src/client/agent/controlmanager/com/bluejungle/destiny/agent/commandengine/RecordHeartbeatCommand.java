/*
 * Created on Jan 20, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * This class implements the heartbeat record command.
 * 
 * @author fuad
 * @version $Id: //depot/main/Destiny_3.5/main/src/client/agent.controlmanager.com/bluejungle/destiny/agent/commandengine/RecordHeartbeatCommand.java#1
 */

public class RecordHeartbeatCommand extends AgentCommandBase {
    public int execute() {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            this.log.fatal("Cannot record heartbeat information. Control Manager not available.");
            return ErrorCode.FAILURE;
        }
        
        controlManager.recordHeartbeatStateInformation();
        
        return ErrorCode.SUCCESS;
    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.LOCAL;
    }
}
