/*
 * Created on Mar 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.PreparedStatement;

/**
 * This is the insert connection interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/IInsertConnection.java#1 $
 */

interface IInsertConnection {

    /**
     * Returns the SQL query that should be executed to insert results in the
     * result table
     * 
     * @return the SQL query that should be executed to insert results in the
     *         result table
     */
    public String getSQLQuery();

    /**
     * Returns the prepared statement generated by Hibernate
     * 
     * @return the prepared statement generated by Hibernate
     */
    public PreparedStatement getPreparedStatement();
}