/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * This is the component heartbeat information interface. This interface holds
 * the data that needs to be passed during a heartbeat request.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IComponentHeartbeatInfo.java#1 $
 */

public interface IComponentHeartbeatInfo {

    /**
     * Returns the component name
     * 
     * @return the component name
     */
    public String getComponentName();

    /**
     * Returns the component type
     * 
     * @return the component type
     */
    public ServerComponentType getComponentType();

    /**
     * Returns the heartbeat cookie
     * 
     * @return the heartbeat cookie
     */
    public IComponentHeartbeatCookie getHeartbeatCookie();

    /**
     * Returns the shared folder cookie
     * 
     * @return the shared folder cookie
     */
    public ISharedFolderCookie getSharedFolderCookie();

    /**
     * Sets the component name
     * 
     * @param name
     *            name to set
     */
    public void setComponentName(String name);

    /**
     * Sets the component type
     * 
     * @param type
     *            component type to set
     */
    public void setComponentType(ServerComponentType type);

    /**
     * Sets the heartbeat cookie
     * 
     * @param cookie
     *            cookie to set
     */
    public void setHeartbeatCookie(IComponentHeartbeatCookie cookie);

    /**
     * Sets the shared folder cookie
     * 
     * @param cookie
     *            cookie to set
     */
    public void setSharedFolderCookie(ISharedFolderCookie cookie);
}
