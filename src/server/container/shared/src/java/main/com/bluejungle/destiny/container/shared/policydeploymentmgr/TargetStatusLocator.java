/*
 * Created on Feb 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.policydeploymentmgr;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetStatusManagerImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This is the target status locator. It provides a level of indirection and
 * allows retrieves the various target status in the enumeration.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/policydeploymentmgr/TargetStatusLocator.java#1 $
 */

public class TargetStatusLocator {

    public static final ComponentInfo TARGET_STATUS_LOCATOR_COMP_INFO = new ComponentInfo(ITargetStatus.class.getName(), TargetStatusManagerImpl.class.getName(), ITargetStatusManager.class.getName(), LifestyleType.SINGLETON_TYPE);
}