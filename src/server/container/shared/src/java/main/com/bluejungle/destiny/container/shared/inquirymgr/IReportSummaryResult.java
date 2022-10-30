/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the report summary result interface. In a summary report, each data
 * object in the list of results implements this interface, no matter what type
 * of summary has been used in the report execution.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportSummaryResult.java#1 $
 */

public interface IReportSummaryResult extends IResultData {

    /**
     * Returns the count of a particular summary value. For example, if the
     * summary value is "January", the count returns the number of occurences
     * during the month of January (e.g 5).
     * 
     * @return the count of a particular summary value.
     */
    public Long getCount();
    
    /**
     * Returns the summary value. The summary value, based on the query type,
     * can be the username, the resource name, the month name, week number, etc.
     * 
     * @return the summary value
     */
    public String getValue();
}