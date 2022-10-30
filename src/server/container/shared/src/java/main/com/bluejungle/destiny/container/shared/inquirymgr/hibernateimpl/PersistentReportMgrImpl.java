/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.PersistentReportMgrSortFieldType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the persistent report manager implementation class. This class
 * manipulates persistent reports.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentReportMgrImpl.java#1 $
 */

public class PersistentReportMgrImpl extends BaseReportMgrImpl implements IPersistentReportMgr {

    protected static final char SINGLECHAR_WILDCARD = '?';
    protected static final char WILDCARD = '*';
    protected static final char HQL_SINGLECHAR_WILDCARD = '_';
    protected static final char HQL_WILDCARD = '%';
    private IHibernateRepository dataSource;

    /**
     * Adds a query specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param queryTerm
     *            query specification to add
     */
    protected void addSearchTerm(Criteria crit, IPersistentReportMgrQueryTerm queryTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (queryTerm == null) {
            throw new NullPointerException("QuerySpec cannot be null");
        }
        Criterion qs = null;
        if (PersistentReportMgrQueryFieldType.TITLE.equals(queryTerm.getFieldName())) {
            qs = Expression.like("title", queryTerm.getExpression().replace(WILDCARD, HQL_WILDCARD).replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD));
        } else if (PersistentReportMgrQueryFieldType.SHARED.equals(queryTerm.getFieldName())) {
            qs = Expression.eq("owner.isShared", new Boolean(queryTerm.getExpression()));
        } else if (PersistentReportMgrQueryFieldType.DESCRIPTION.equals(queryTerm.getFieldName())) {
            qs = Expression.like("description", queryTerm.getExpression().replace(WILDCARD, HQL_WILDCARD).replace(SINGLECHAR_WILDCARD, HQL_SINGLECHAR_WILDCARD));
        }

        if (qs != null) {
            crit.add(qs);
        }
    }

    /**
     * Adds a sort specification to an existing criteria
     * 
     * @param crit
     *            existing criteria to use
     * @param sortTerm
     *            sort specification to add
     */
    protected void addSortTerm(Criteria crit, IPersistentReportMgrSortTerm sortTerm) {
        if (crit == null) {
            throw new NullPointerException("Criteria cannot be null");
        }
        if (sortTerm == null) {
            throw new NullPointerException("sortTerm cannot be null");
        }
        String fieldName = null;
        if (PersistentReportMgrSortFieldType.DESCRIPTION.equals(sortTerm.getFieldName())) {
            fieldName = "description";
        } else if (PersistentReportMgrSortFieldType.SHARED.equals(sortTerm.getFieldName())) {
            fieldName = "owner.isShared";
        }else if (PersistentReportMgrSortFieldType.TITLE.equals(sortTerm.getFieldName())) {
            fieldName = "title";
        }

        if (fieldName != null) {
            if (SortDirectionType.DESCENDING.equals(sortTerm.getDirection())) {
                crit.addOrder(Order.desc(fieldName));
            } else {
                crit.addOrder(Order.asc(fieldName));
            }
        }
    }

    /**
     * Creates a Hibernate criteria to fetch the report results.
     * 
     * @param querySpec
     *            query specification to use.
     * @param s
     *            Hibernate session to use
     * @return a Hibernate Criteria object
     */
    protected Criteria buildQueryCriteria(IPersistentReportMgrQuerySpec querySpec, Session s) {
        Criteria crit = s.createCriteria(ReportDO.class);

        if (querySpec != null) {
            int size = querySpec.getSearchSpecTerms().length;

            for (int index = 0; index < size; index++) {
                IPersistentReportMgrQueryTerm queryTerm = querySpec.getSearchSpecTerms()[index];
                addSearchTerm(crit, queryTerm);
            }

            size = querySpec.getSortSpecTerms().length;

            for (int index = 0; index < size; index++) {
                IPersistentReportMgrSortTerm sortTerm = querySpec.getSortSpecTerms()[index];
                addSortTerm(crit, sortTerm);
            }
        }
        return crit;
    }

    /**
     * Creates a new persistent report. The creation also sets default values
     * for some of the report parameters.
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr#createPersistentReport()
     */
    public IPersistentReport createPersistentReport(Long userId) {
        ReportDO report = new ReportDO();
        ReportOwnerDO owner = new ReportOwnerDO();
        owner.setIsShared(false); //By default, not shared
        owner.setOwnerId(userId);
        InquiryDO inquiry = new InquiryDO();
        report.setInquiry(inquiry);
        report.setOwner(owner);
        report.setSortSpec(getDefaultReportSortSpec());
        report.setTimePeriod(getDefaultReportTimePeriod());
        report.setSummaryType(getDefaultSummaryType());
        return report;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr#deleteReport(java.lang.Long,
     *      java.lang.String)
     */
    public void deleteReport(Long reportId) {
        if (reportId == null) {
            throw new NullPointerException("reportId cannot be null for delete report");
        }
        Session s = null;
        IPersistentReport report = getReport(reportId);
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            t = s.beginTransaction();
            s.delete(report);
            t.commit();
        } catch (HibernateException e) {
            getLog().error("Error when deleting report with id '" + reportId, e);
        } finally {
            HibernateUtils.rollbackTransation(t, getLog());
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        super.dispose();
        this.dataSource = null;
    }

    /**
     * Returns the Hibernate data source
     * 
     * @return the Hibernate data source
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr#getReport(long,
     *      java.lang.String)
     */
    public IPersistentReport getReport(Long reportId) {
        ReportDO result = null;
        if (reportId == null) {
            throw new NullPointerException("report Id cannot be null in getReport");
        }

        //The session should not be closed
        Session s = null;
        try {
            s = getDataSource().getSession();
            result = (ReportDO) s.get(ReportDO.class, reportId);
        } catch (HibernateException e) {
            getLog().error("Error occured when fetching report by id", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr#getReports(com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec)
     */
    public List<IPersistentReport> getReports(IPersistentReportMgrQuerySpec searchSpec) {
        Session s = null;
        Criteria crit = buildQueryCriteria(searchSpec, s);
        List<IPersistentReport> result = null;
        try {
            s = getDataSource().getSession();
            result = crit.list();
        } catch (HibernateException e) {
            getLog().error("Failed to retrieve reports", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        this.dataSource = getConfiguration().get(REPORT_DATA_SOURCE_CONFIG_PARAM);
        if (getDataSource() == null) {
            throw new NullPointerException("Data source for the reports must be specified for the persistent report manager");
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr#saveReport(com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport,
     *      java.lang.String)
     */
    public IPersistentReport saveReport(IPersistentReport updatedReport) {
        IPersistentReport result = updatedReport;
        if (updatedReport == null) {
            throw new NullPointerException("Updated report cannot be null in saveReport");
        }

        //The session should not be closed
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            t = s.beginTransaction();
            s.update(updatedReport);
            t.commit();
        } catch (HibernateException e) {
            getLog().error("Error occured when saving report", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }
}