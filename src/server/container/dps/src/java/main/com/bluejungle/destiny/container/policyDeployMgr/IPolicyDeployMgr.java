/*
 * Created on Jan 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.policyDeployMgr;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestExecutor;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dps/com/bluejungle/destiny/container/policyDeployMgr/IPolicyDeployMgr.java#1 $
 */

public interface IPolicyDeployMgr extends IDeploymentRequestExecutor {
    public static final PropertyKey<IHibernateRepository> MGMT_DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("MgmtDataSource");
    public static final String COMP_NAME = "policyDeployMgr";

}
