/*
 * Created on Feb 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestExecutor;

/**
 * This interface represents a policy deployment manager.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/components/deployment/IPolicyDeploymentManager.java#2 $
 */

public interface IPolicyDeploymentManager extends IDeploymentRequestExecutor {

    public static final String COMP_NAME = "PolicyDeploymentManager";
    public static final String DABS_COMPONENT_NAME = "DABSName";
    public static final String MGMT_DATA_SOURCE_CONFIG_PARAM = "MgmtDataSource";
}