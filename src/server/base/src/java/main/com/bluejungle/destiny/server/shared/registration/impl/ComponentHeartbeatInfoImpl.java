/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the component heartbeat information implementation class. This class
 * contains the implementation for the heartbeat information.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/ComponentHeartbeatInfoImpl.java#1 $
 */

public class ComponentHeartbeatInfoImpl implements IComponentHeartbeatInfo {

    private String componentName;
    private ServerComponentType componentType;
    private IComponentHeartbeatCookie heartbeatCookie;
    private ISharedFolderCookie sharedFolderCookie;

    /**
     * Constructor
     *  
     */
    public ComponentHeartbeatInfoImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo#getComponentName()
     */
    public String getComponentName() {
        return this.componentName;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return this.componentType;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo#getHeartbeatCookie()
     */
    public IComponentHeartbeatCookie getHeartbeatCookie() {
        return this.heartbeatCookie;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo#getSharedFolderCookie()
     */
    public ISharedFolderCookie getSharedFolderCookie() {
        return this.sharedFolderCookie;
    }

    /**
     * Sets the component name
     * 
     * @param componentName
     *            component name to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Sets the component type
     * 
     * @param componentType
     *            component type to set
     */
    public void setComponentType(ServerComponentType componentType) {
        this.componentType = componentType;
    }

    /**
     * Sets the heartbeat cookie
     * 
     * @param heartbeatCookie
     *            heartbeat cookie to set
     */
    public void setHeartbeatCookie(IComponentHeartbeatCookie heartbeatCookie) {
        this.heartbeatCookie = heartbeatCookie;
    }

    /**
     * Sets the shared folder cookie
     * 
     * @param sharedFolderCookie
     *            shared folder cookie to set
     */
    public void setSharedFolderCookie(ISharedFolderCookie sharedFolderCookie) {
        this.sharedFolderCookie = sharedFolderCookie;
    }
}
