/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.common.discovery;

import java.util.Set;

/**
 * This is the discovery client interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/common/src/java/main/com/bluejungle/destiny/client/common/discovery/IDiscoverClient.java#1 $
 */

public interface IDiscoverClient {
    
    /**
     * 
     * @param type
     *            type of items to discover
     * @param maxSearchDuration
     *            maximum search duration in milliseconds. The search will stop
     *            after this duration is elapsed.
     * @return a set of IDiscoveredItem elements that have been discovered. If
     *         no items have been discovered, the set is empty.
     */
    public Set discover(DiscoveryType type, long maxSearchDuration);
}