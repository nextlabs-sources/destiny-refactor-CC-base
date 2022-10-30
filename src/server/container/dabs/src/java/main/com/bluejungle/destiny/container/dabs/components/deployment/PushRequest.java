/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetAgent;
import com.bluejungle.framework.threading.ITask;

/**
 * This is the push request object. A push request object passes the target
 * agent information that the push worker class can update after a successful
 * (or not) push.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/components/deployment/PushRequest.java#1 $
 */

final class PushRequest implements ITask {

    private ITargetAgent targetAgent;
    private PushRequestCounter counter;

    /**
     * Constructor
     * 
     * @param newAgent
     *            agent data object used in the agent
     */
    public PushRequest(ITargetAgent newAgent, PushRequestCounter counter) {
        super();
        this.targetAgent = newAgent;
        if (this.targetAgent == null) {
            throw new NullPointerException("Agent data object cannot be null");
        }

        this.counter = counter;
        if (this.counter == null) {
            throw new NullPointerException("counter object cannot be null");
        }
    }

    /**
     * Returns the agent data object associated with the push request
     * 
     * @return the agent data object
     */
    public ITargetAgent getAgent() {
        return this.targetAgent;
    }

    /**
     * Returns the counter object
     * 
     * @return the counter object
     */
    public PushRequestCounter getPushRequestCounter() {
        return this.counter;
    }
}