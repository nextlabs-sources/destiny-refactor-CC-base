/*
 * Created on Mar 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult;

/**
 * This is the data object for the report summary result. A report summary
 * result data object is usually created through Hibernate dynamic
 * instantiation, usually from a stored result table record
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportSummaryResultDO.java#2 $
 */

public class ReportSummaryResultDO extends BaseReportResultDO implements IReportSummaryResult {

    private Long count;
    private String value;

    /**
     * Constructor
     */
    public ReportSummaryResultDO() {
        super();
    }

    /**
     * Constructor
     * 
     * @param id
     *            id to set
     * @param count
     *            count to set
     * @param value
     *            value to set
     */
    public ReportSummaryResultDO(Long id, String value, Long count) {
        super();
        setId(id);
        setCount(count);
        setValue(value);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult#getCount()
     */
    public Long getCount() {
        return this.count;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportSummaryResult#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the new count
     * 
     * @param newCount
     *            new count to set
     */
    public void setCount(Long newCount) {
        this.count = newCount;
    }

    /**
     * Sets the new value
     * 
     * @param newValue
     *            the new value to set
     */
    public void setValue(String newValue) {
        this.value = newValue;
    }
}