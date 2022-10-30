/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class AgentCommandSet implements IAgentCommandSet {
    private final Queue<IAgentCommand> commands;
    
    private boolean bCommandsDependent;
    
    
    public AgentCommandSet() {
        commands = new LinkedList<IAgentCommand>();
        bCommandsDependent = false;
    }
    
    public AgentCommandSet(IAgentCommand command) {
        this();
        this.commands.add(command);
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommandSet#getCommandsDependent()
     */
    public boolean getCommandsDependent() {
        return this.bCommandsDependent;
    }

    /**
     * init sets up the AgentCommandSet based on the commandSetSpec passed in
     * 
     * @param commandSetSpec
     *            input commandSetSpec that is sent by server
     */
    void init(CommandSetSpec commandSetSpec) {
        // TODO: write code once commandSetSpec is available
    }

    /**
     * Adds a command to the command set
     * 
     * @param command
     *            command to add to the command set
     */
    void addCommand(IAgentCommand command) {
        this.commands.add(command);
    }

    /**
     * Sets bCommandsDependent
     * 
     * @param commandsDependent
     *            bCommandsDependent to set.
     */
    public void setCommandsDependent(boolean bCommandsDependent) {
        this.bCommandsDependent = bCommandsDependent;
    }

    /**
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommandSet#execute()
     */
    public int execute() {
        int result = ErrorCode.SUCCESS;
        for (IAgentCommand command : commands) {
            int currentResult = command.execute();

            // If one of them fail, the whole command fail. 
            // Hopfully there is SUCCESS and FAIL
            // otherwise the last one will override the result.
            if (currentResult != ErrorCode.SUCCESS) {
                result = currentResult;
                if (this.bCommandsDependent) {
                    break;
                }
            }
        }
        return result;
    }
}