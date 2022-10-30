/*
 *  All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import java.sql.Timestamp;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This contains all constants and APIs that are shared across the inquiry center.
 * It is very important to keep the table name's case match the hibernate mapping file.
 * DatabaseMetadata is very sensitive to the name. Different case won't work. 
 */
public class SharedLib {
    /* cache tables*/
    public static final String CACHED_APPLICATION = "CACHED_APPLICATION";
    
    public static final String CACHED_HOST = "CACHED_HOST";
    
    public static final String CACHED_HOSTGROUP = "CACHED_HOSTGROUP";
    
    public static final String CACHED_POLICY = "CACHED_POLICY";
    
    public static final String CACHED_USER = "CACHED_USER";
    
    public static final String CACHED_USERGROUP = "CACHED_USERGROUP";
    
    public static final String CACHED_USERGROUP_MEMBER = "CACHED_USERGROUP_MEMBER";
    
    
    /* original logs tables */
    public static final String PA_TABLE = "POLICY_ACTIVITY_LOG";
    
    public static final String PA_CUST_ATTR_TABLE = "POLICY_CUSTOM_ATTR";
    
    public static final String PA_OBLIGATION_TABLE = "OBLIGATION_LOG";
    
    public static final String TA_TABLE = "TRACKING_ACTIVITY_LOG";
    
    public static final String TA_CUST_ATTR_TABLE = "TRACKING_CUSTOM_ATTR";
    

    /* report logs tables */
    public static final String REPORT_PA_TABLE = "REPORT_POLICY_ACTIVITY_LOG";
    
    public static final String REPORT_PA_CUST_ATTR_TABLE = 
        "REPORT_POLICY_CUSTOM_ATTR";
    
    public static final String REPORT_PA_OBLIGATION_TABLE = 
        "REPORT_OBLIGATION_LOG";
    
    public static final String REPORT_TA_TABLE = "REPORT_TRACKING_ACTIVITY_LOG";
    
    public static final String REPORT_TA_CUST_ATTR_TABLE = 
        "REPORT_TRACKING_CUSTOM_ATTR";
    
    
    /* archive logs tables */
    public static final String ARCHIVE_PA_TABLE = "ARCHIVE_POLICY_ACTIVITY_LOG";
    
    public static final String ARCHIVE_PA_CUST_ATTR_TABLE =
        "ARCHIVE_POLICY_CUSTOM_ATTR";
    
    public static final String ARCHIVE_PA_OBLIGATION_TABLE = 
        "ARCHIVE_OBLIGATION_LOG";
    
    public static final String ARCHIVE_TA_TABLE = 
        "ARCHIVE_TRACKING_ACTIVITY_LOG";
    
    public static final String ARCHIVE_TA_CUST_ATTR_TABLE = 
        "ARCHIVE_TRACKING_CUSTOM_ATTR";
    
    /* internal table used for various book-keeping tasks for data sync */
    public static final String REPORT_INTERNAL_TABLE = "REPORT_INTERNAL";
    
    /**
     * Shared SQL for obtaining minimum date from log tables.
     */
    public static final String SELECT_PA_MIN_TIME_SQL = 
        "select min(time) from " + SharedLib.REPORT_PA_TABLE;

    public static final String SELECT_TA_MIN_TIME_SQL = 
        "select min(time) from " + SharedLib.REPORT_TA_TABLE;
    
    /**
     * These are the 'keys' to the record in the internal table that
     * stores the minimum time stamp of policy and tracking logs. Note that
     * these keys are used in the external SQL scripts that unarchive the 
     * data. Any change in the key names requires change in the corresponding
     * unarchive SQL scripts.
     */
    public static final String POLICY_ACTIVITY_LOG_MIN_TIME = 
    	"POLICY_ACTIVITY_LOG_MIN_TIME";
    public static final String TRACKING_ACTIVITY_LOG_MIN_TIME = 
    	"TRACKING_ACTIVITY_LOG_MIN_TIME";
    
    /**
     * Returns earliest report date as timestamp based on report type.
     * May return null if no data is available.
     * 
     * @param forPolicyActivity True if the required date is for policy activity
     *                           logs. False for tracking activity logs.
     *                           
     * @return the earliest date as java.sql.Timestamp. May be null. 
     */
    public static Timestamp getEarliestReportDate(boolean forPolicyActivity)
    throws Exception {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ReportDataHolderManager daoMgr = compMgr.getComponent(ReportDataHolderManager.class);
        Timestamp tstamp = forPolicyActivity 
                ? daoMgr.getMinPolicyActivityReportDate() 
                : daoMgr.getMinTrackingActivityReportDate();

        return tstamp;
    }
    
    public static IHibernateRepository getActivityDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository activityDataSrc = (IHibernateRepository) compMgr
            .getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        
        return activityDataSrc;
    }
}
