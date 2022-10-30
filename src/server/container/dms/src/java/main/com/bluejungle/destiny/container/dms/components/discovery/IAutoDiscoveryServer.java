/*
 * Created on Aug 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.discovery;

/**
 * This is the auto discovery server interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/discovery/IAutoDiscoveryServer.java#1 $
 */

public interface IAutoDiscoveryServer {

    /**
     * Component name
     */
    public static final String COMP_NAME = "autoDiscoveryServer";

    /**
     * Configuration parameter for certificate location
     */
    public static final String CERT_LOCATION_CONFIG_PARAM = "certLoc";

    /**
     * Configuration parameter for DCC component manager
     */
    public static final String DCC_COMP_MGR_CONFIG_PARAM = "AutoDiscDCCCompMgr";

    /**
     * Configuration parameter for port number
     */
    public static final String PORT_CONFIG_PARAM = "AutoDiscPort";
    
    /**
     * Configuration parameter for DCC profile manager
     */
    public static final String PROFILE_MGR_CONFIG_PARAM = "AutoDiscProfileMgr";

    /**
     * Configuration parameter for agent manager
     */    
    public static final String AGENT_MGR_CONFIG_PARAM = null;

}