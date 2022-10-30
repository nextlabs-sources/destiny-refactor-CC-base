/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

/**
 * This is the query builder interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/IQuery.java#1 $
 */

public interface IQuery {

    /**
     * Adds a new query element to the query
     * 
     * @param newElement
     *            new query element to add
     */
    public void addQueryElement(IQueryElement newElement);

    /**
     * Returns a copy of the query object.
     * 
     * @return a copy of the query object.
     */
    public IQuery copy();

    /**
     * Returns the current variable name associated with a data object name. For
     * example, if the query already specifies "UserDO u", calling this API with
     * "UserDO" will return "u", or null if "UserDO" was never used so far.
     * 
     * @param dataObjectName
     *            data object name to use.
     * @return the variable name if data Object name already exists, null
     *         otherwise.
     */
    public String findVarName(String dataObjectName);

    /**
     * Returns an hibernate query object based on the query definition
     * 
     * @param session
     *            Hibernate session
     * 
     * @return an hibernate query object
     * @throws HibernateException
     *             if creating the query fails
     */
    public Query getHQLQuery(Session session) throws HibernateException;

    /**
     * Returns the HQL query string
     * 
     * @return the HQL query string
     */
    public String getHQLQueryString();

    /**
     * Returns a map of all the HQL query parameters
     * 
     * @return a map of all the HQL query parameters
     */
    public Map getHQLQueryParameters();
}