/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the report result state interface. The report result state allows to
 * resume fetching records from a previous executed report. Any class
 * implementing this interface should have store enough information to resume a
 * previously stored query.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IReportResultState.java#1 $
 */

public interface IReportResultState {

    /**
     * Returns the sequence id for the first row of the result set
     * 
     * @return the sequence id for the first row of the result set
     */
    public Long getFirstRowSequenceId();

    /**
     * Returns the sequence id for the last row of the result set
     * 
     * @return the sequence id for the last row of the result set
     */
    public Long getLastRowSequenceId();

    /**
     * Returns the id of the stored query associated with this state
     * 
     * @return the id of the stored query associated with this state
     */
    public Long getQueryId();
}