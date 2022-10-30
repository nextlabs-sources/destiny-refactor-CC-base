/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.common.discovery;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the discovery type class. The discovery type represents the various
 * types of objects that a discovery client can discover on the network.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/common/src/java/main/com/bluejungle/destiny/client/common/discovery/DiscoveryType.java#1 $
 */

public class DiscoveryType extends EnumBase {

    /**
     * Instances of servers
     */
    public static final DiscoveryType SERVER_INSTANCES = new DiscoveryType("ServerInstances");

    /**
     * Instances of policy servers
     */
    public static final DiscoveryType POLICY_SERVERS = new DiscoveryType("PolicyServers");

    /**
     * Constructor
     * 
     * @param name
     *            name of the report field to query on
     */
    private DiscoveryType(String name) {
        super(name);
    }
}