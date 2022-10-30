/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Date;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;
import com.bluejungle.destiny.container.shared.inquirymgr.ISortSpec;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;

/**
 * This is the "on the fly" report implementation class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportImpl.java#1 $
 */

public class ReportImpl implements IReport {

    private Date asOf;
    private IInquiry inquiry;
    private ISortSpec sortSpec;
    private ReportSummaryType summaryType;
    private IReportTimePeriod timePeriod;

    /**
     * Constructor
     */
    public ReportImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#getAsOf()
     */
    public Date getAsOf() {
        return this.asOf;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#getInquiry()
     */
    public IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#getSummaryType()
     */
    public ReportSummaryType getSummaryType() {
        return this.summaryType;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#getTimePeriod()
     */
    public IReportTimePeriod getTimePeriod() {
        return this.timePeriod;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#setInquiry(com.bluejungle.destiny.container.shared.inquirymgr.IInquiry)
     */
    public void setInquiry(IInquiry inquiry) {
        this.inquiry = inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#setSummaryType(com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryType)
     */
    public void setSummaryType(ReportSummaryType summary) {
        this.summaryType = summary;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#getSortSpec()
     */
    public ISortSpec getSortSpec() {
        return this.sortSpec;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#setAsOf(java.util.Date)
     */
    public void setAsOf(Date newAsOf) {
        this.asOf = newAsOf;
    }

    /**
     * Sets the sort specification
     * 
     * @param sortSpec
     *            sort specification to set
     */
    public void setSortSpec(ISortSpec sortSpec) {
        this.sortSpec = sortSpec;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReport#setTimePeriod(com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod)
     */
    public void setTimePeriod(IReportTimePeriod timePeriod) {
        this.timePeriod = timePeriod;
        if (this.timePeriod == null) {
            //Guarantees not null time period for callers. This happens because
            // there are no required field within this component and hibernate
            // therefore decides to make it null.
            this.timePeriod = new TimePeriodImpl();
        }
    }
}
