/*
 * Created on Aug 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.discovery;

import java.net.URL;
import java.util.Set;

import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO;

/**
 * This is an internal interface exposed only to classes within the package. It
 * allows the processing thread to call back into the autodiscovery server and
 * query on the appropriate data based on the request nature.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/discovery/IAutoDiscoverServerInternal.java#1 $
 */

interface IAutoDiscoverServerInternal extends IAutoDiscoveryServer {

    /**
     * Returns the SSL certificate location
     * 
     * @return the SSL certificate location
     */
    public String getCertificateLocation();

    /**
     * Returns a list of currently active DABS instances
     * 
     * @return a list of URLs of currently active DABS instances
     */
    public Set<URL> getActiveDABSInstances();

    /**
     * Returns a list of currently active DPS instances
     * 
     * @return a list of currently active DPS instances
     */
    public IDCCComponentDO[] getActiveDPSInstances();

    /**
     * Returns the current server information
     * 
     * @return the current server information
     */
    public IDCCComponentDO getServerInfo();
}