/*
 * Created on Dec 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.pf.engine.destiny.EvaluationResult;

/**
 * @author sasha
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/AgentLogObligation.java#12 $:
 */

public final class AgentLogObligation extends LogObligation {

    private static final long serialVersionUID = 1L;

    private final ICommandExecutor executor;

    AgentLogObligation(ICommandExecutor executor) {
        this.executor = executor;
    }

    public ICommandExecutor getExecutor() {
        return executor;
    }
}
