/*
 * Created on Apr 15, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReport;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.InquiryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportDO;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This class simply adds / deletes reports from the database. It is used by
 * test cases to create sample data.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ReportSampleDataMgr.java#1 $
 */

public class ReportSampleDataMgr {

    /**
     * Deletes all records from the data
     * 
     * @param s
     * @throws HibernateException
     */
    public void deleteAllReports(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from ReportDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Inserts identical report records in the database
     * 
     * @param s
     *            Hibernate session to use
     * @param startId
     *            id of the first record to insert
     * @param nbRecords
     *            total number of records to insert
     * @param modelDO
     *            Model report to follow
     * @throws HibernateException
     *             if insertion fails
     */
    public void insertIdenticalReportRecords(Session s, Long startId, int nbRecords, IPersistentReport modelDO) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            for (int i = 0; i < nbRecords; i++) {
                ReportDO reportToSave = ReportDOCloner.clone((ReportDO) modelDO);
                reportToSave.setId(new Long(i));
                s.save(reportToSave);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Dummy class use to clone the model report data object
     * 
     * @author ihanen
     */
    protected static class ReportDOCloner {

        public static ReportDO clone(ReportDO modelDO) {
            ReportDO result = new ReportDO();
            result.setDescription(modelDO.getDescription());
            result.setId(modelDO.getId());
            result.setInquiry(modelDO.getInquiry());
            result.setTitle(modelDO.getTitle());
            result.setOwner(modelDO.getOwner());
            result.setSortSpec(modelDO.getSortSpec());
            result.setSummaryType(modelDO.getSummaryType());
            result.setTimePeriod(modelDO.getTimePeriod());
            return result;
        }
    }
    
    /**
     * Dummy class use to clone the model report data object
     * 
     * @author ihanen
     */
    protected static class InquiryDOCloner {

        public static InquiryDO clone(InquiryDO modelDO) {
            InquiryDO result = new InquiryDO();
            return result;
        }
    }
}