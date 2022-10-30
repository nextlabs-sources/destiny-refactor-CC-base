/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import java.util.List;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the persistent report manager interface. The persistent report
 * manager manipulates report objects on behalf of a user. Its main purpose is
 * to guarantee that inquiries are accessed properly based on the report
 * ownership.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IPersistentReportMgr.java#1 $
 */

public interface IPersistentReportMgr {

    String COMP_NAME = "persistentReportMgr";
    PropertyKey<IHibernateRepository> REPORT_DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("ReportDataSource");

    /**
     * Returns a new persistent report instance
     * 
     * @param ownerId
     *            id of the report owner
     * @return a new persistent report instance
     */
    IPersistentReport createPersistentReport(Long ownerId);

    /**
     * Deletes a persistent report.
     * 
     * @param reportId
     *            id of the report to delete
     * @throws DataSourceException
     *             if the deletion failed on the persistence layer
     */
    void deleteReport(Long reportId) throws DataSourceException;

    /**
     * Returns a persistent report object.
     * 
     * @param reportId
     *            id of the report to retrieve
     * @return the report object (if found), or null (if not found)
     * @throws DataSourceException
     *             if the persistence layer failed
     */
    IPersistentReport getReport(Long reportId) throws DataSourceException;

    /**
     * Return a list of reports based on the query specification
     * 
     * @param querySpec
     *            query specification. If null, all reports are returned.
     * @return the list of persistent reports matching the query specification
     * @throws DataSourceException
     *             if the persistent layer fails
     */
    List<IPersistentReport> getReports(IPersistentReportMgrQuerySpec querySpec) throws DataSourceException;

    /**
     * Saves a report. If the report does not exist yet, it is added. If it
     * already exists, then the query is updated.
     * 
     * @param updatedReport
     *            updated / created report object
     * @return the persisted report
     * @throws DataSourceException
     *             if the persistence failed
     */
    IPersistentReport saveReport(IPersistentReport updatedReport) throws DataSourceException;
}