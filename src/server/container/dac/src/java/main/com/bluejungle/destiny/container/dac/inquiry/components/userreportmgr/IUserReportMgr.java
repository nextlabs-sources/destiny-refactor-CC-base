/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;

/**
 * The user report manager stands in front of the persistent report manager. The
 * role of the user report manager is mostly to be sure that a given user has
 * the right privilege to perform some operations on persisted reports.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/component/reportmgr/IReportMgr.java#1 $
 */

public interface IUserReportMgr {

    String COMP_NAME = "userReportMgr";
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM;

    /**
     * Deletes an report belonging to a given user
     * 
     * @param reportId
     *            id of the report to delete
     * @param userId
     *            destiny id of the user requesting the deletion
     * @throws ReportAccessException
     *             if the query cannot be deleted by this user
     * @throws DataSourceException
     *             if an error occurs in the persistence layer
     */
    void deleteReport(final Long reportId, final Long userId) throws ReportAccessException, DataSourceException;

    /**
     * Returns an report object for a given user.
     * 
     * @param reportId
     *            id of the report to retrieve
     * @return the report object (if found), or null (if not found or not access
     *         to it)
     * @throws ReportAccessException
     *             if the given report is not accessible to this user.
     */
    IPersistentReport getReport(final Long reportId, final Long userId) throws ReportAccessException;

    /**
     * Return a list of reports that a given user can see
     * 
     * @param querySpec
     *            query specification
     * @param userId
     *            name of the user
     * @return the list of persisten reports that a given user can see (the list
     *         can be empty if the user has access to no report)
     */
    List<IPersistentReport> getReports(final IPersistentUserReportMgrQuerySpec querySpec, final Long userId);

    /**
     * Inserts a new report in the database
     * 
     * @param newReport
     *            new report to insert
     * @return the persistent report that has been saved.
     * @throws DataSourceException
     * @throws UniqueConstraintViolationException if the report title is already exist by that user
     */
    IPersistentReport insertReport(final IPersistentReport newReport, final Long userId)
			throws UniqueConstraintViolationException, DataSourceException;

    /**
     * Saves an report. If the report does not exist yet, it is added. If it
     * already exists, then the query is updated.
     * 
     * @param updatedreport
     *            updated / created report object
     * @param userId
     *            id of the user requesting the update
     * @return the saved or updates report object
     * @throws ReportAccessException
     *             if the user cannot update this report *
     * @throws DataSourceException
     *             if an error occurs in the persistence layer
     * @throws UniqueConstraintViolationException if the report title is already exist by that user 
     */
    IPersistentReport saveReport(final IPersistentReport updatedReport, final Long userId)
			throws ReportAccessException, UniqueConstraintViolationException, DataSourceException;
}