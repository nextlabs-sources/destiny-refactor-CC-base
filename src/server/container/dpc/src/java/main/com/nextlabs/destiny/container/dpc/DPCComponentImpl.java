/*
 * Created on Jul 19, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dpc/src/java/main/com/nextlabs/destiny/container/dpc/DPCComponentImpl.java#1 $:
 */

package com.nextlabs.destiny.container.dpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.agent.controlmanager.ControlMngr;
import com.bluejungle.destiny.agent.controlmanager.ControlManagerStub;
import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.PropertyKey;

public class DPCComponentImpl extends BaseDCCComponentImpl {
    private static final String COMPONENT_TYPE_NAME = "DPC";
    public static final ServerComponentType COMPONENT_TYPE = ServerComponentType.fromString(COMPONENT_TYPE_NAME);

    /* These appear in the server.xml file */
    private static final PropertyKey<String> PROPERTY_AGENT_TYPE = new PropertyKey<String>("AgentType");
    private static final PropertyKey<String> PROPERTY_ROOT_DIRECTORY = new PropertyKey<String>("RootDirectory");
    private static final PropertyKey<String> ADDITIONAL_ARGS = new PropertyKey<String>("AdditionalArguments");

    @Override
    public ServerComponentType getComponentType() {
        return COMPONENT_TYPE;
    }

    @Override
    public void init() {
        super.init();

        String agentType = getConfiguration().get(PROPERTY_AGENT_TYPE);

        if (agentType == null) {
            agentType = "FILE_SERVER";
        }

        String rootDirectory = getConfiguration().get(PROPERTY_ROOT_DIRECTORY);

        if (rootDirectory == null) {
            throw new NullPointerException("'" + PROPERTY_ROOT_DIRECTORY + "' is not defined in server.xml for " + COMPONENT_TYPE_NAME);
        }

        // Can be used by anyone who wants to
        System.setProperty("dpc.install.home", rootDirectory);

        final List<String> args = new ArrayList<String>();

        args.add(agentType);
        args.add("SDK");  // Disable IPC stub
        args.add("RootDirectory=" + rootDirectory);

        /*
         * Any additional arguments you want to pass to the PC. These
         * are going to be split into different arguments around
         * spaces, unless the space is inside a quote. So this:
         *
         * a=b c="d e f"
         *
         * Is two arguments: a=b  and c=d e f
         */
        String additionalArgs = getConfiguration().get(ADDITIONAL_ARGS);

        if (additionalArgs != null) {
            for (String additionalArg : split(additionalArgs)) {
                args.add(additionalArg);
            }
        }

        ControlMngr.main(args.toArray(new String[args.size()]));

        // Initialize the thread pool used by queries. This will be initialized as needed, but if it's never initialized
        // then shutdown will be messier, so let's make sure it happens
        ControlManagerStub.getInstance();

        return;
    }

    private static String[] split(String input) {
        ArrayList<String> tokens = new ArrayList<String>();

        boolean inQuote = false;
        boolean skipSpace = true;
        StringBuilder token = new StringBuilder();

        for (char c : input.toCharArray()) {

            if (c == ' ' && skipSpace) {
                continue;
            }

            skipSpace = false;

            if (c == '\"') {
                inQuote = !inQuote;
            }

            if (c == ' ' && !inQuote) {
                skipSpace = true;
                tokens.add(token.toString());
                token.delete(0, token.length());
            } else if (c != '\"') {
                token.append(c);
            }
        }

        tokens.add(token.toString());
        return tokens.toArray(new String[0]);
    }
}
