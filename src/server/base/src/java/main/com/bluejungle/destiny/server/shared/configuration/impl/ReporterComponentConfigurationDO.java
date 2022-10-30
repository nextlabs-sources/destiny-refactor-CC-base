/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IReporterComponentConfigurationDO;

/**
 * @author sgoldstein
 * @version $Id:
 */

public class ReporterComponentConfigurationDO extends DCCComponentConfigurationDO implements IReporterComponentConfigurationDO {
	private Integer showSharePointReports;
	private Long reportGenerationFrequency;
	
    /**
     * Constructor
     *  
     */
    public ReporterComponentConfigurationDO() {
        super();
    }
    
	public Integer getShowSharePointReports() {
		return showSharePointReports;
	}
	
	public void setShowSharePointReports(Integer showSharePointReports) {
		this.showSharePointReports = showSharePointReports;
	}

	public Long getReportGenerationFrequency() {
		return reportGenerationFrequency;
	}

	public void setReportGenerationFrequency(Long reportGenerationFrequency) {
		this.reportGenerationFrequency = reportGenerationFrequency;
	}
}