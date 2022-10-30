/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.Calendar;

/**
 * This is the report time period interface. This interface allows to set the
 * begin and end date that should be used when executing a report.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportTimePeriod.java#1 $
 */

public interface IReportTimePeriod {

    /**
     * Gets the begin date for the report data.
     * 
     * @return the begin date for the report data
     */
    public Calendar getBeginDate();

    /**
     * Returns the end date for the report data.
     * 
     * @return the end date for the report data
     */
    public Calendar getEndDate();

    /**
     * Sets the degin date for the report data.
     * 
     * @param beginDate
     *            the new begin date
     */
    public void setBeginDate(Calendar beginDate);

    /**
     * Sets the end date for the report data.
     * 
     * @param endDate
     *            the new end date
     */
    public void setEndDate(Calendar endDate);
}