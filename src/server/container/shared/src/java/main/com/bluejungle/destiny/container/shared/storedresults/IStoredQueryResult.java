/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

/**
 * The stored query result represents a record in the result table.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQueryResult.java#1 $
 */

public interface IStoredQueryResult {

    /**
     * Returns the id of the record
     * 
     * @return the id of the record
     */
    public Long getId();

    /**
     * Returns the query object associated with this result
     * 
     * @return the query object associated with this result
     */
    public IStoredQuery getQuery();

}