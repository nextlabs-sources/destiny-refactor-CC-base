/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.Date;

/**
 * This is the report interface. The report interface represents an instance of
 * a report, that can then be executed to return results.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReport.java#1 $
 */

public interface IReport {

    /**
     * Returns the date of directory as of which the report should be run
     * against. If a date is specified, the whole query is run again the
     * directory data as of this date. If the value is NULL, then directory
     * updates are taken into account when matching records.
     * 
     * @return the date of the directory, or NULL if the directory data updates
     *         are taken into account.
     */
    public Date getAsOf();

    /**
     * Returns the inquiry used in the report
     * 
     * @return the inquiry used in the report
     */
    public IInquiry getInquiry();

    /**
     * Returns the sort specification for the report
     * 
     * @return the sort specification for the report
     *  
     */
    public ISortSpec getSortSpec();

    /**
     * Returns the summary chosen for the report
     * 
     * @return the summary chosen for the report
     */
    public ReportSummaryType getSummaryType();

    /**
     * Returns the time period used for this report
     * 
     * @return the time period used for this report
     */
    public IReportTimePeriod getTimePeriod();

    /**
     * Sets the "asOf" date. If this date is not set (default), the report
     * follows directory updates.
     * 
     * @param asOf
     *            as of date to set (null is ok)
     */
    public void setAsOf(Date asOf);

    /**
     * Sets the summary type for the report
     * 
     * @param summary
     *            summary type
     */
    public void setSummaryType(ReportSummaryType summary);
}