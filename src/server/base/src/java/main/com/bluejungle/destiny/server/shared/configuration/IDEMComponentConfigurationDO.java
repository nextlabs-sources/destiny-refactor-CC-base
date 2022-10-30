/*
 * Created on Feb 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

/**
 * This is the DEM component configuration interface
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/configmgr/IDEMComponentConfigurationDO.java#1 $
 */

public interface IDEMComponentConfigurationDO extends IDCCComponentConfigurationDO {
	public Integer getReporterCacheRefreshRate();
}
