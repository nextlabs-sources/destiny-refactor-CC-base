/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.report.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/report/impl/InquiryMgrReportValueConverter.java#1 $
 */

public class InquiryMgrReportValueConverter extends ReportValueConverterShared{
    private static final Log LOG = LogFactory.getLog(InquiryMgrReportValueConverter.class); 
    
    private static boolean isActionsLoaded = false;
    
    @Override
    public String getActionDisplayName(String abbrev) {
        if(!isActionsLoaded){
            synchronized (InquiryMgrReportValueConverter.class) {
                IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
                final IDestinyConfigurationStore configMgr = (IDestinyConfigurationStore) compMgr
                        .getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
                IActionListConfigDO actionListConfig = configMgr.retrieveActionListConfig();
                IActionConfigDO[] actions = actionListConfig.getActions();
                if (actions != null) {
                    for (IActionConfigDO action : actions) {
                        String existing = ACTION_MAP.get(action.getShortName());
                        if (existing == null) {
                            ACTION_MAP.put(action.getShortName(), action.getDisplayName());
                        } else {
                            //ignore
                            LOG.warn("Action '" + action.getShortName()
                                    + "' already exists with name, " + existing
                                    + ". It can not assign with new name, "
                                    + action.getDisplayName() + ".");
                        }
                    }
                    isActionsLoaded = true;
                }
            }
        }
        
        return super.getActionDisplayName(abbrev);
    }
}
