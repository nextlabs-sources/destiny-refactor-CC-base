/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

/**
 * This is the interface for the summary query data object.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQuerySummaryResult.java#1 $
 */

public interface IStoredQuerySummaryResult extends IStoredQueryResult {

    /**
     * Returns the summary count
     * 
     * @return the summary count
     */
    public Long getCount();

    /**
     * Returns the summary value
     * 
     * @return the summary value
     */
    public String getValue();
}