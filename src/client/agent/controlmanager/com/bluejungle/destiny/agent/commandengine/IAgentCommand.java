/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public interface IAgentCommand {
    enum CommunicationType{
        NETWORK,
        LOCAL,
    }

    /**
     * @param paramArray
     *            input parameters for the command
     */
    void init(ArrayList paramArray);

    /**
     * @param commandSpec
     *            CommandSpec instance to initialize command
     */
    void init(CommandSpecBase commandSpec);

    /**
     * Execute the command
     * 
     * @return error code. Should be one of the constants defined in class
     *         ErrorCode
     */
    int execute();
    
    // The CommunicationType doesn't have any effect on the actual communication 
    // It only tells the CommandExector which thread pool to use.
    // 
    // Under normal condition, putting a wrong type won't cause any problem
    //
    // However, when the NETWORK thread pool is blocked/hanged. Any command that doesn't need 
    // network can go thru LOCAL thread pool. There is no checking if you return the wrong type.
    // If both thread pools are blocked, you are       .
    CommunicationType getCommunicationType();

}