/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.common.discovery;

/**
 * This is the discovered item interface. This interface represents an item
 * discovered somewhere on the network.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/common/src/java/main/com/bluejungle/destiny/client/common/discovery/IDiscoveredItem.java#1 $
 */

public interface IDiscoveredItem {

    /**
     * Returns the host where the discovered item resides
     * 
     * @return the host where the discovered item resides
     */
    public String getHost();

    /**
     * Returns the discovered item port number
     * 
     * @return the discovered item port number
     */
    public int getPort();
}