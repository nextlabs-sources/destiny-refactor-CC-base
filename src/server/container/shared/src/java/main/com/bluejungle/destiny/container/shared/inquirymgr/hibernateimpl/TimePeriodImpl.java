/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod;

/**
 * This is the report time period implementation class. A time period consists
 * of a start date and an end date.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/TimePeriodImpl.java#1 $
 */

public class TimePeriodImpl implements IReportTimePeriod {

    private Calendar beginDate;
    private Calendar endDate;

    /**
     * Constructor
     */
    public TimePeriodImpl() {
        super();
    }

    /**
     * Constructor
     * 
     * @param timePeriod
     *            timePeriod to copy
     */
    public TimePeriodImpl(IReportTimePeriod timePeriod) {
        if (timePeriod != null) {
            setBeginDate(timePeriod.getBeginDate());
            setEndDate(timePeriod.getEndDate());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod#getBeginDate()
     */
    public Calendar getBeginDate() {
        return this.beginDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod#getEndDate()
     */
    public Calendar getEndDate() {
        return this.endDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod#setBeginDate(java.util.Date)
     */
    public void setBeginDate(Calendar beginDate) {
        this.beginDate = beginDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportTimePeriod#setEndDate(java.util.Date)
     */
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }
}
