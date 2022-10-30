/*
 * Created on Mar 29, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.commandengine;

import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.framework.comp.ComponentManagerFactory;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/commandengine/UploadLogsCommand.java#1 $:
 */

public class UploadLogsCommand extends AgentCommandBase {

    /**
     * calls ActivityJournal component to upload logs.
     * 
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#execute()
     */
    public int execute() {
        
        IActivityJournal activityJournal = (IActivityJournal) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournal.NAME);
        activityJournal.uploadActivityLogs();
        
        return ErrorCode.SUCCESS;
    }

}
