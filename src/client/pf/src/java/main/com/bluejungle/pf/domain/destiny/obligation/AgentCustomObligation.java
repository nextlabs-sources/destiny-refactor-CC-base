/*
 * Created on Feb 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Next Labs Inc.,
 * Redwood City CA, Ownership remains with Next Labs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import java.util.List;

/**
 * @author alan morgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/AgentCustomObligation.java#1 $:
 */

public final class AgentCustomObligation extends CustomObligation {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * @param command
     */
    AgentCustomObligation(String command, List<String> args) {
        super(command, args);
    }
}
