/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.domain.IDomainObject;
import com.bluejungle.framework.patterns.IEnum;

/**
 * This is the target status interface. The status indicates whether a
 * deployment on a given target is started and what is the current outcome of
 * the deployment.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/ITargetStatus.java#1 $
 */

public interface ITargetStatus extends IEnum, IDomainObject {

    public static final ITargetStatus NOT_STARTED = ((ITargetStatusManager) ComponentManagerFactory.getComponentManager().getComponent(TargetStatusLocator.TARGET_STATUS_LOCATOR_COMP_INFO)).getTargetStatus("NotStarted");
    public static final ITargetStatus IN_PROGRESS = ((ITargetStatusManager) ComponentManagerFactory.getComponentManager().getComponent(TargetStatusLocator.TARGET_STATUS_LOCATOR_COMP_INFO)).getTargetStatus("InProgress");
    public static final ITargetStatus SUCCEEDED = ((ITargetStatusManager) ComponentManagerFactory.getComponentManager().getComponent(TargetStatusLocator.TARGET_STATUS_LOCATOR_COMP_INFO)).getTargetStatus("Succeeded");
    public static final ITargetStatus FAILED = ((ITargetStatusManager) ComponentManagerFactory.getComponentManager().getComponent(TargetStatusLocator.TARGET_STATUS_LOCATOR_COMP_INFO)).getTargetStatus("Failed");
}