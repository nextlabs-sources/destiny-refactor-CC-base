/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportOwner;

/**
 * This is the persistent report class implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportDO.java#1 $
 */

public class ReportDO extends ReportImpl implements IPersistentReport {

    private String description;
    private Long id;
    private IReportOwner owner;
    private String title;

    /**
     * Constructor
     */
    public ReportDO() {
        super();
    }

    /**
     * Constructor from another persistent report data
     * 
     * @param report
     *            other persistent report to copy from
     */
    public ReportDO(IPersistentReport report) {
        if (report == null) {
            throw new NullPointerException("report cannot be null");
        }
        this.setDescription(report.getDescription());
        this.setId(report.getId());
        this.setInquiry(new InquiryDO((IPersistentInquiry) report.getInquiry()));
        this.setTitle(report.getTitle());
        this.setOwner(new ReportOwnerDO(report.getOwner()));
        this.setSortSpec(report.getSortSpec());
        this.setSummaryType(report.getSummaryType());
        this.setTimePeriod(new TimePeriodImpl(report.getTimePeriod()));
        
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#getDescription()
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#getTitle()
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#getOwner()
     */
    public IReportOwner getOwner() {
        return this.owner;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the report id
     * 
     * @param newId
     *            new id for the report
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport#setTitle(java.lang.String)
     */
    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Sets the new owner for the report
     * 
     * @param newOwner
     *            new owner to be set
     */
    public void setOwner(IReportOwner newOwner) {
        this.owner = newOwner;
    }
}