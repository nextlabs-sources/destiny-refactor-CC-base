/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * Interface to represent DAC configuration
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IDACComponentConfigurationDO.java#1 $
 */

public interface IDACComponentConfigurationDO extends IDCCComponentConfigurationDO {

	IActivityJournalSettingConfigurationDO getActivityJournalSettingConfiguration();
}