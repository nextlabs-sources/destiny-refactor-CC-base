/*
 * Created on Feb 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.Calendar;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;

/**
 * This is the DCC component data object interface
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/compmgr/IDCCComponentDO.java#2 $
 */

public interface IDCCComponentDO {

    /**
     * Returns the DCSF callback URL for this component
     * 
     * @return the DCSF callback URL for this component
     */
    public String getCallbackURL();

    /**
     * Returns the component URL
     * 
     * @return the component URL
     */
    public String getComponentURL();
    
    /**
     * Returns the url of the loadbalancer pointed to this component. If a load
     * balancer is not being utilized, this value will be the same as the
     * callback url
     * 
     * @return the url of the load balancer pointed to this component, or the
     *         callback url if a load balancer is not being utilized
     * 
     * Note: Currently, this will always return the component url. It is a
     * placeholder for future use (i.e. when it's actually implemented, clients
     * won't have to be recompiled)
     */
    public String getLoadBalancerURL();

    /**
     * Returns the DCC component id
     * 
     * @return the DCC component id
     */
    public Long getId();

    /**
     * Returns the DCC component name. This is unique in the whole environment.
     * The name is usually hostname_componentType. Such as server.nextlabs.com_DABS
     * 
     * @return the DCC component name.
     */
    public String getName();
    
    /**
     * a display name is the displayable name of the component type. Such as Communication Server for dcsf
     * @return
     */
    public String getTypeDisplayName();

    /**
     * Returns the DCC component type
     * 
     * @return the DCC component type
     */
    public DCCComponentEnumType getType();

    /**
     * Returns whether the DCC component is currently active (i.e. is sending
     * hearbeats regularly).
     * 
     * @return true if the DCC component is active, false otherwise
     */
    public boolean isActive();

    /**
     * Returns the heartbeat rate
     * 
     * @return integer representing the heartbeat interval in seconds
     */
    public int getHeartbeatRate();

    /**
     * Returns last heartbeat timestamp
     * 
     * @return last heartbeat timestamp
     */
    public Calendar getLastHeartbeat();
}