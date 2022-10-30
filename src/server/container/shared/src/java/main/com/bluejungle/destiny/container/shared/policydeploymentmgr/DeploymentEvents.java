/*
 * Created on Feb 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

/**
 * This is a set of events specific to the policy deployment
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/DeploymentEvents.java#1 $
 */

public class DeploymentEvents {

    /**
     * This event is fired whenever there are policy updates available to be
     * sent out to the agents.
     */
    public static final String POLICY_UPDATES_AVAILABLE = "PolicyUpdatesAvailable";

    /**
     * This event is fired whenever there are policy updates available to be
     * sent out to the agents.
     */
    public static final String POLICY_PUSH_AVAILABLE = "PolicyPush";
    public static final String POLICY_PUSH_ID_PROP = "I";

}