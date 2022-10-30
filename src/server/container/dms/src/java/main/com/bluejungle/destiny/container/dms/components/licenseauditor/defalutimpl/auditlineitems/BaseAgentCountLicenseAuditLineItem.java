/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems;

import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * Base class for license audit line items which interogate agent counts
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/auditlineitems/BaseAgentCountLicenseAuditLineItem.java#1 $
 */

public abstract class BaseAgentCountLicenseAuditLineItem implements ILicenseAuditLineItem {

    /**
     * Retrieve the agent statistics
     * @param agentTypeId TODO
     * 
     * @return the agent statistics
     * @throws PersistenceException
     */
    protected long getAgentCount(String agentTypeId) throws PersistenceException {
        IAgentManager agentManager = (IAgentManager) ComponentManagerFactory.getComponentManager().getComponent(IAgentManager.COMP_NAME);
        IAgentType agentType = agentManager.getAgentType(agentTypeId);
        return agentManager.getAgentStatistics().getAgentCount(agentType);
    }
}