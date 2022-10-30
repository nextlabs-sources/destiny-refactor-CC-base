/*
 * Created on Apr 14, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.pf.engine.destiny.EvaluationResult;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/AgentNotifyObligation.java#1 $:
 */

public class AgentNotifyObligation extends NotifyObligation {

    private static final long serialVersionUID = 1L;

    private ICommandExecutor executor;

    /**
     * Constructor
     * @param emailAddresses
     * @param body
     */
    AgentNotifyObligation(String emailAddresses, String body, ICommandExecutor executor) {
        super(emailAddresses, body);
        this.executor = executor;
    }

    public ICommandExecutor getExecutor() {
        return executor;
    }
}
