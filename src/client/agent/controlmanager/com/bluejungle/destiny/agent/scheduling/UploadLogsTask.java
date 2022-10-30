/*
 * Created on Dec 15, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.scheduling;

import java.util.TimerTask;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * The Upload Logs task calls uploadLogs on the command executor when the task
 * is executed
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class UploadLogsTask extends TimerTask {

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        commandExecutor.uploadLogs();
    }

}