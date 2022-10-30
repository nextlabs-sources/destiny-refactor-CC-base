/*
 * Created on Dec 10, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class AgentCommandFactory {

    public static final String LOG_COMMAND = "LOG_COMMAND";
    public static final String NOTIFY_COMMAND = "NOTIFY_COMMAND";
    public static final String HEARTBEAT_COMMAND = "HEARTBEAT_COMMAND";
    public static final String UPLOAD_LOGS_COMMAND = "UPLOAD_LOGS_COMMAND";
    public static final String RECORD_HEARTBEAT_COMMAND = "HEARTBEAT_RECORD_COMMAND";

    private static Map<String, Class<? extends IAgentCommand>> commandNameMap = null;

    static {
        commandNameMap = new HashMap<String, Class<? extends IAgentCommand>>();
        commandNameMap.put(LOG_COMMAND, LogCommand.class);
        commandNameMap.put(NOTIFY_COMMAND, NotifyCommand.class);
        commandNameMap.put(HEARTBEAT_COMMAND, HeartBeatCommand.class);
        commandNameMap.put(UPLOAD_LOGS_COMMAND, UploadLogsCommand.class);
        commandNameMap.put(RECORD_HEARTBEAT_COMMAND, RecordHeartbeatCommand.class);
    }

    /**
     * Instantiate the class associated with the command, Init the instance by
     * passing spec to the command instance and return the command.
     * 
     * @param spec
     *            CommandSpec specifies command to instantiate and its
     *            initialization parameters
     * @return initialized instance of Command
     */
    public static IAgentCommand createCommand(CommandSpecBase spec) {
        IAgentCommand command = createCommand(spec.getName());
        command.init(spec);
        return (command);
    }

    /**
     * Instantiate the class associated with the command and return it.
     * 
     * @param commandName
     *            name of command to instantiate
     * @return new instance of command
     */
    public static IAgentCommand createCommand(String commandName) {
        Class<? extends IAgentCommand> commandClazz = commandNameMap.get(commandName);
        if (commandClazz != null) {
            ComponentInfo<IAgentCommand> info = new ComponentInfo<IAgentCommand>(
            		commandClazz.getName(), 
            		commandClazz, 
            		IAgentCommand.class, 
            		LifestyleType.TRANSIENT_TYPE);
            return ComponentManagerFactory.getComponentManager().getComponent(info);
        }

        return null;
    }
}
