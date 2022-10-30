/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IDACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IActivityJournalSettingConfigurationDO;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/DACComponentConfigurationDO.java#1 $
 */

public class DACComponentConfigurationDO extends DCCComponentConfigurationDO implements IDACComponentConfigurationDO {
	private ActivityJournalSettingConfigurationDO activityJournalSettingConfiguration;
	
    /**
     * Constructor
     *  
     */
    public DACComponentConfigurationDO() {
        super();
    }
    
    public IActivityJournalSettingConfigurationDO getActivityJournalSettingConfiguration() {
        return activityJournalSettingConfiguration;
    }
    
    public void setActivityJournalSettingConfiguration(ActivityJournalSettingConfigurationDO activityJournalSettingConfiguration) {
        this.activityJournalSettingConfiguration = activityJournalSettingConfiguration;
    }
}