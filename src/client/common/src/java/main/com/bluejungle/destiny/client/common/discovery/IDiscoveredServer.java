/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.common.discovery;

/**
 * This interface represent an instance of the CompliantEnterprise server that
 * has been discovered.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/common/src/java/main/com/bluejungle/destiny/client/common/discovery/IDiscoveredServer.java#1 $
 */

public interface IDiscoveredServer extends IDiscoveredItem {

    /**
     * Returns the location where the server certificates are installed
     * 
     * @return the location where the server certificates are installed
     */
    public String getCertificatesLocation();
}