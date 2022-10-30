/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;


/**
 * This interface manages all the target status items in the target status
 * enumeration.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/ITargetStatusManager.java#1 $
 */

public interface ITargetStatusManager {

    /**
     * Retrieve a target status by name
     * 
     * @param name
     *            name of the target status
     * @return the ITargetStatus corresponding to the specified name.
     * @throws IllegalArgumentException
     *             if no ITargetStatus corresponds to the name
     */
    public ITargetStatus getTargetStatus(String name);

    /**
     * Returns whether a target status with the specified name exists.
     * 
     * @param name
     *            name of the target status
     * @return true if a target status with this name exists, false otherwise.
     */
    public boolean targetStatusExists(String name);
}