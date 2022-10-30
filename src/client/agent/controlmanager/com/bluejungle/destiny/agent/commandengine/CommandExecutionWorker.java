/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IWorker;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CommandExecutionWorker implements IWorker {

    protected Log log = null;

    /**
     * Constructor
     *  
     */

    public CommandExecutionWorker() {
        this.log = LogFactory.getLog(this.getClass().getName());
    }

    /**
     * Calls execute on the command set.
     * 
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        try {
            ((IAgentCommandSet) task).execute();
        } catch (Exception e) { //This is the main controller thread in the
                                // agent. It cannot be allowed to die.
            if (this.log != null) {
                this.log.error("An error ocurred while executing a command.", e);
            }
        }
    }

}