/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.pf.engine.destiny.EvaluationResult;

/**
 * @author sergey
 * This is the display obligation for use on the agent.
 */
public class AgentDisplayObligation extends DisplayObligation {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * @param message
     */
    AgentDisplayObligation(String message) {
        super(message);
    }
}
