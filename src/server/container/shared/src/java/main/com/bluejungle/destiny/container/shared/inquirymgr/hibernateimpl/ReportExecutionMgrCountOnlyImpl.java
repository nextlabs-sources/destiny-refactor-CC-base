/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Date;

import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.SimpleQueryImpl;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is a special implementation of the report execution manager only to
 * count the number of results returned by a particular query. The query
 * fetching the results is virtually fetching nothing, allowing faster execution
 * time. Callers using this implementation of the execution manager are
 * typically interested on the number of rows returned by a report execution,
 * and not the actual data itself.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionMgrCountOnlyImpl.java#1 $
 */

public class ReportExecutionMgrCountOnlyImpl extends BaseReportExecutionMgr implements IReportExecutionMgr {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr#executeReport(com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    public IReportResultReader executeReport(final IReport report) throws InvalidReportArgumentException, DataSourceException {
        return executeReport(report, -1);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr#executeReport(com.bluejungle.destiny.container.shared.inquirymgr.IReport)
     */
    public IReportResultReader executeReport(final IReport report, final int maxFetchRows) throws InvalidReportArgumentException, DataSourceException {
        if (ReportSummaryType.NONE != report.getSummaryType()) {
            //Report should not have any grouping for this implementation
            throw new InvalidReportArgumentException();
        }

        //Builds a simplified HQL query only for the purpose of counting
        final IQuery query = new SimpleQueryImpl();
        buildQueryElements(report, query);
        Integer totalCount = countResults(report, query);
        return new ReportResultReaderEmptyImpl(totalCount);
    }

    /**
     * In this implementation, the resource class element is not supported, as
     * we assume that the PF APIs may not be available to expand the group
     * definition. For now, ignore this element.
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.BaseReportExecutionMgr#getResourceClassElement(com.bluejungle.destiny.container.shared.inquirymgr.IReport,
     *      java.util.Date)
     */
    protected IQueryElement getResourceClassElement(IReport report, Date asOf) {
        return null;
    }
}