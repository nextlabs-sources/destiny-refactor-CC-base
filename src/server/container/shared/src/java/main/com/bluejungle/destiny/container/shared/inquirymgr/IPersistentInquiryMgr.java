/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.List;

import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the interface for the persistent inquiry manager. The persistent
 * inquiry manager stores inquiry object on the persistence layer.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IPersistentInquiryMgr.java#1 $
 */

public interface IPersistentInquiryMgr {

    public static final String REPORT_DATA_SOURCE_CONFIG_PARAM = "ReportDataSource";

    /**
     * Returns a new persistent inquiry instance
     * 
     * @return a new persistent inquiry instance
     */
    public IPersistentInquiry createPersistentInquiry();

    /**
     * Deletes a persistent inquiry
     * 
     * @param reportId
     *            id of the inquiry to delete
     * @throws DataSourceException
     *             if the persistence layer failed
     */
    public void deleteInquiry(Long inquiryId) throws DataSourceException;

    /**
     * Returns a persistent inquiry object.
     * 
     * @param reportId
     *            id of the inquiry to retrieve
     * @return the inquiry object (if found), or null (if not found or not
     *         access to it)
     * @throws DataSourceException
     *             if the persistence layer failed
     */
    public IPersistentInquiry getInquiry(Long inquiryId) throws DataSourceException;

    /**
     * Return a list of inquiries that a given user can see
     * 
     * @param userId
     *            name of the user
     * @param sortSpec
     *            sort specification
     * @return the list of persistent inquiries that a given user can see
     * @throws DataSourceException
     *             if the persistence layer failed
     */
    public List getInquiries(String userId, ISortSpec sortSpec) throws DataSourceException;

    /**
     * Saves an inquiry. If the inquiry does not exist yet, it is added. If it
     * already exists, then the inquiry is updated.
     * 
     * @param updatedInquiry
     *            updated / created inquiry object
     * @return the saved inquiry object
     * @throws DataSourceException
     *             if the persistence layer failed
     */
    public IPersistentInquiry saveInquiry(IPersistentInquiry updatedInquiry) throws DataSourceException;
}