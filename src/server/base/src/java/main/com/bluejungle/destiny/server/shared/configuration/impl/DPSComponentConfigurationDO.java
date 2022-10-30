/*
 * Created on Feb 9, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IResourceAttributeConfigurationDO;

/**
 * @author safdar
 * @author sergey
 * @version $Id:
 *          //depot/branch/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DPSComponentConfigurationDO.java#1 $
 */

public class DPSComponentConfigurationDO extends DCCComponentConfigurationDO implements IDPSComponentConfigurationDO {

    /**
     * Default grace window duration
     */
    private static final int DEFAULT_LIFECYCLE_MANAGER_GRACE_WINDOW = 1;

    private int lifecycleManagerGraceWindow = DEFAULT_LIFECYCLE_MANAGER_GRACE_WINDOW;

    private String deploymentTime = null;

    private List<IResourceAttributeConfigurationDO> customResourceAttributes = new ArrayList<IResourceAttributeConfigurationDO>();

    /**
     * Constructor
     */
    public DPSComponentConfigurationDO() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO#getLifecycleManagerGraceWindow()
     */
    public int getLifecycleManagerGraceWindow() {
        return this.lifecycleManagerGraceWindow;
    }

    /**
     * Sets the grace window
     *
     * @param interval interval to set
     */
    public void setLifecycleManagerGraceWindow(int window) {
        this.lifecycleManagerGraceWindow = window;
    }

    /**
     * Gets the default deployment time.
     *
     * @return Returns the deploymentTime.
     */
    public String getDeploymentTime() {
        return deploymentTime;
    }

    /**
     * Sets the default deployment time.
     *
     * @param deploymentTime The deploymentTime to set.
     */
    public void setDeploymentTime(String deploymentTime) {
        this.deploymentTime = deploymentTime;
    }

    /**
     * @see IDPSComponentConfigurationDO#getCustomResourceAttributes()
     */
    public IResourceAttributeConfigurationDO[] getCustomResourceAttributes() {
        return (IResourceAttributeConfigurationDO[])
            customResourceAttributes.toArray(
                new IResourceAttributeConfigurationDO[customResourceAttributes.size()]
            );
    }

    /**
     * Adds a custom resource attribute configuration.
     * @param toAdd the configuration to add.
     */
    public void addCustomResourceAttribute(IResourceAttributeConfigurationDO toAdd) {
        customResourceAttributes.add(toAdd);
    }
}
