/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportAccessException;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.ReportVisibilityType;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PersistentReportMgrImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportDO;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;

/**
 * This is the user report manager implementation class. This class performs
 * CRUD operations on persistent report objects. It acts as a "data access"
 * layer on top of the persistent report manager, and makes sure that only the
 * right users can perform the operations on the persistent report.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/userreportmgr/hibernateimpl/UserReportMgrImpl.java#1 $
 */

public class UserReportMgrImpl extends PersistentReportMgrImpl implements IUserReportMgr {

    private static final String REPORT_SHARED_FIELDNAME = "owner.isShared";
    private static final String REPORT_OWNERID_FIELDNAME = "owner.ownerId";

    /**
     * Adds new constraints in the criteria to perform access control in the
     * report list. In this implementation, a user can access only a report if
     * the report is marked as "shared" OR if the reportowner id field matches
     * the user id.
     * 
     * @param criteria
     *            original criteria to use
     * @param userId
     *            userId to use for access control
     */
    protected void addAccessControlCriteria(Criteria criteria, final Long userId) {
        if (criteria == null) {
            throw new NullPointerException("criteria cannot be null");
        }
        if (userId == null) {
            throw new NullPointerException("userId cannot be null");
        }
        Criterion accessControl = Expression.or(
        		Expression.eq(REPORT_SHARED_FIELDNAME, new Boolean(true)), 
        		Expression.eq(REPORT_OWNERID_FIELDNAME, userId));
		criteria.add(accessControl);
    }

    /**
     * Adds a criteria to enforce visibility. The visibility criteria does not
     * replace access control, but filters the results further to separate the
     * shared reports from my, or all.
     * 
     * @param criteria
     *            original criteria to use
     * @param visibility
     *            visibility level requested by the user
     * @param userId
     *            userId to use
     */
    protected void addVisibilityCriteria(Criteria criteria, ReportVisibilityType visibility,
			final Long userId) {
        if (criteria == null) {
            throw new NullPointerException("criteria cannot be null");
        }
        if (userId == null) {
            throw new NullPointerException("userId cannot be null");
        }
        if (visibility == null) {
            throw new NullPointerException("visibility cannot be null");
        }

        Criterion visibilityCrit = null;
        if (ReportVisibilityType.MY_REPORTS.equals(visibility)) {
            //For "my", the user id must match
            visibilityCrit = Expression.eq(REPORT_OWNERID_FIELDNAME, userId);
        } else if (ReportVisibilityType.SHARED_REPORTS.equals(visibility)) {
            //For "shared", the user id must not be the current user id
            visibilityCrit = Expression.not(Expression.eq(REPORT_OWNERID_FIELDNAME, userId));
        }

        //"All" does not add further restrictions
        if (visibilityCrit != null) {
            criteria.add(visibilityCrit);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr#deleteReport(java.lang.Long,
     *      java.lang.Long)
     */
    public void deleteReport(final Long reportId, final Long userId) throws ReportAccessException,
			DataSourceException {
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            //Adds the query by id constraint
            Criteria crit = s.createCriteria(ReportDO.class);
            crit.add(Expression.eq("id", reportId));

            //Adds a constraint on the user Id for access control
            addAccessControlCriteria(crit, userId);

            IPersistentReport reportToDelete = (IPersistentReport) crit.uniqueResult();
            if (reportToDelete == null) {
                //Did we miss this report because it does not exist, or because
                // the user does not have access? Let's check!
                ReportDO report = (ReportDO) s.get(ReportDO.class, reportId);
                if (report != null) {
                    throw new ReportAccessException("The report is not accessible for this user",
							new Object[] { reportId, userId });
				}
            } else {
                t = s.beginTransaction();
                s.delete(reportToDelete);
                t.commit();
            }
        } catch (HibernateException e) {
            getLog().error("Failed to delete report", e);
            HibernateUtils.rollbackTransation(t, getLog());
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr#getReport(java.lang.Long,
     *      java.lang.Long)
     */
    public IPersistentReport getReport(final Long reportId, final Long userId)
			throws ReportAccessException {

        Session s = null;
        ReportDO result = null;
        try {
            s = getDataSource().getSession();
            //Adds the query by id constraint
            Criteria crit = s.createCriteria(ReportDO.class);
            crit.add(Expression.eq("id", reportId));

            //Adds a constraint on the user Id for access control
            addAccessControlCriteria(crit, userId);

            result = (ReportDO) crit.uniqueResult();
            if (result == null) {
                //Did we miss this report because it does not exist,or because
                // the user does not have access? Let's check!
                ReportDO report = (ReportDO) s.get(ReportDO.class, reportId);
                if (report != null) {
					throw new ReportAccessException("The report is not accessible for this user",
							new Object[] { reportId, userId });
				}
            } else {
                //Set a sort specification on the report - The sort
                // specification is not persisted.
                result.setSortSpec(getDefaultReportSortSpec());
            }
        } catch (HibernateException e) {
            getLog().error("Failed to retrieve report", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr#getReports(com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IPersistentUserReportMgrQuerySpec,
     *      java.lang.Long)
     */
    public List<IPersistentReport> getReports(final IPersistentUserReportMgrQuerySpec querySpec, final Long userId) {
        Session s = null;
        //Puts a default return value
        List<IPersistentReport> result = new ArrayList<IPersistentReport>();
        try {
            s = getDataSource().getSession();
            Criteria crit = buildQueryCriteria(querySpec, s);

            //Adds a constraint on the user Id for access control
            addAccessControlCriteria(crit, userId);

            //Adds the visibility constraint
            if (querySpec != null) {
                addVisibilityCriteria(crit, querySpec.getVisibility(), userId);
            }

            result = crit.list();
        } catch (HibernateException e) {
            getLog().error("Failed to retrieve reports", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr#insertReport(com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport)
     */
    public IPersistentReport insertReport(IPersistentReport newReport, final Long userId)
			throws UniqueConstraintViolationException, DataSourceException {
        //Converts the persistent report to a report data object
        IPersistentReport result = new ReportDO(newReport);
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            
            Criteria crit = s.createCriteria(ReportDO.class);
			crit.add(Expression.eq("title", newReport.getTitle()));
			crit.add(Expression.eq(REPORT_OWNERID_FIELDNAME, userId));

			ReportDO existingReport =  (ReportDO)crit.uniqueResult();
			if (existingReport != null) {
				throw new UniqueConstraintViolationException(new String[] { "title" });
			}
            
            t = s.beginTransaction();
            s.save(result);
            t.commit();
        } catch (HibernateException e) {
            getLog().error("Failed to insert report", e);
            throw new DataSourceException(e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr#saveReport(com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport,
     *      java.lang.Long)
     */
    public IPersistentReport saveReport(final IPersistentReport updatedReport, final Long userId)
			throws ReportAccessException, UniqueConstraintViolationException, DataSourceException {
		if (userId == null) {
			throw new NullPointerException("userId cannot be null");
		}
		Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            
            Criteria crit = s.createCriteria(ReportDO.class);
			crit.add(Expression.eq("title", updatedReport.getTitle()));
			crit.add(Expression.eq(REPORT_OWNERID_FIELDNAME, userId));
			
			ReportDO existingReport =  (ReportDO)crit.uniqueResult();
			if (existingReport != null && !existingReport.getId().equals(updatedReport.getId())) {
				//find a report with different id
				throw new UniqueConstraintViolationException(new String[] { "title" });
			}
			
			s.clear();

            //First, check that the report that is about to be updated belongs
            // to the appriopriate owner
            Query q = s.createQuery("select report.owner.ownerId from ReportDO report where report.id = :id");
            q.setLong("id", updatedReport.getId().longValue());
            Long currentOwnerId = (Long) q.uniqueResult();
            if (!currentOwnerId.equals(updatedReport.getOwner().getOwnerId())) {
                throw new ReportAccessException("The report is not owned by the caller", null);
            }
            t = s.beginTransaction();
            s.update(updatedReport);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Failed to update report", e);
            throw new DataSourceException(e);
        } catch (ReportAccessException e) {
            getLog().error("Report cannot be updated by a different owner.", e);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return updatedReport;
    }
}
