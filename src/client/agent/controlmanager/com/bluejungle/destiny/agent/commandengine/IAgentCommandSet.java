/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import com.bluejungle.framework.threading.ITask;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IAgentCommandSet extends ITask {

    /**
     * 
     * Executes all commands in the command set in order. If commands are
     * dependent and one of the commands fails, stops executing and returns the
     * error code for the failed command
     * 
     * @return error code
     */
    int execute();

}