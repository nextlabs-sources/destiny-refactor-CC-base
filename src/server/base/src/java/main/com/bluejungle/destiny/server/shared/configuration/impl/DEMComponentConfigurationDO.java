/*
 * Created on Feb 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IDEMComponentConfigurationDO;

/**
 * This is the DEM component configuration data object
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/DEMComponentConfigurationDO.java#1 $
 */

public class DEMComponentConfigurationDO extends DCCComponentConfigurationDO implements IDEMComponentConfigurationDO {
	private Integer reporterCacheRefreshRate;
	
	public void setReporterCacheRefreshRate(Integer reporterCacheRefreshRate){
		this.reporterCacheRefreshRate = reporterCacheRefreshRate;
	}

	public Integer getReporterCacheRefreshRate() {
		return reporterCacheRefreshRate;
	}
}
