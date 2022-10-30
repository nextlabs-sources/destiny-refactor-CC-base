/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the base report manager implementation class. This class only
 * contains basic functions to be reused by all implementations.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseReportMgrImpl.java#2 $
 */

abstract class BaseReportMgrImpl implements IConfigurable, IDisposable, IInitializable, ILogEnabled, IManagerEnabled {

    private static Log log;
    private IConfiguration configuration;
    private IComponentManager manager;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Creates a default sort spec. By default, the default sort spec is date
     * ascending.
     * 
     * @return a sortSpec object
     */
    protected ISortSpec getDefaultReportSortSpec() {
        SortSpecImpl sortSpec = new SortSpecImpl();
        sortSpec.setSortField(SortFieldType.DATE);
        sortSpec.setSortDirection(SortDirectionType.DESCENDING);
        return sortSpec;
    }

    /**
     * Returns the default time period for a report. By default, there is not
     * time period (the report looks at all data regardless of the date).
     * 
     * @return the default time period for a report.
     */
    protected IReportTimePeriod getDefaultReportTimePeriod() {
        TimePeriodImpl timePeriod = new TimePeriodImpl();
        return timePeriod;
    }

    /**
     * Returns the default summary type for a new report
     * 
     * @return the default summary type for a new report
     */
    protected ReportSummaryType getDefaultSummaryType() {
        return ReportSummaryType.NONE;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {

    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }
}