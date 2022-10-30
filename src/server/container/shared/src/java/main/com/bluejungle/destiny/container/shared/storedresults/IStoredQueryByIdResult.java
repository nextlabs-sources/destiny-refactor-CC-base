/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults;

/**
 * The stored query by id result represents a record in the result table. The
 * result is simply an id pointing to some other record in the database. The
 * location of that record is specified in the query definition
 * (dataObjectName).
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IStoredQueryByIdResult.java#1 $
 */

public interface IStoredQueryByIdResult extends IStoredQueryResult {

    /**
     * Returns the id of the result record
     * 
     * @return the id of the result record
     */
    public Long getResultId();
}