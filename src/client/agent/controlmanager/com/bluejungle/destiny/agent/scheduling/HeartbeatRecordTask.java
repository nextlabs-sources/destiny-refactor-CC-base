/*
 * Created on Jan 22, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.scheduling;

import java.util.TimerTask;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * The heartbeat record task calls recordHeartbeatInfo on the command
 * executor when the task is executed
 * 
 * @author fuad
 * @version $Id: //depot/main/Destiny_3.5/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/scheduler/HeartbeatRecordTask.java#1
 */

public class HeartbeatRecordTask extends TimerTask {

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        commandExecutor.recordHeartbeatInfo();
    }
}
