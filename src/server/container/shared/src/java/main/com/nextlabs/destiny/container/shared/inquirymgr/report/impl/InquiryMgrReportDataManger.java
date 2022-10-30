/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.report.impl;

import java.sql.Connection;
import java.sql.SQLException;

import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;
import com.nextlabs.destiny.container.shared.inquirymgr.report.IReportValueConverter;
import com.nextlabs.report.datagen.IReportDataManager;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/report/impl/InquiryMgrReportDataManger.java#1 $
 */

public class InquiryMgrReportDataManger extends BaseReportDataManager implements IReportDataManager {
    private static final IReportValueConverter VALUE_CONVERTER = new InquiryMgrReportValueConverter();
    private static final Log LOG = LogFactory.getLog(InquiryMgrReportDataManger.class);
    
    private final IHibernateRepository activityDataSrc;

    private Session session;
    private Connection connection;
    
    public InquiryMgrReportDataManger() {
        activityDataSrc = SharedLib.getActivityDataSource();
        
    }

    @Override
    public byte getDatabaseType() {
        IHibernateRepository.DbType dbType = activityDataSrc.getDatabaseType();
        switch (dbType) {
        case POSTGRESQL:
            return DB_TYPE_POSTGRESQL;
        case ORACLE:
            return DB_TYPE_ORACLE;
        case MS_SQL:
            return DB_TYPE_MS_SQL;
        default:
            throw new IllegalArgumentException("Unknown dbtype : " + dbType);
        }
    }

    @Override
    public String getMappedAction(String name) {
        return VALUE_CONVERTER.getActionDisplayName(name);
    }

    @Override
    protected Connection getConnection() throws Exception {
        session = activityDataSrc.getSession();
        connection = session.connection();
        return connection;
    }

    protected void closeConnection() throws Exception {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ex) {
            LOG.warn("Could not close connection for InquiryMgrReportDataManger.");
        }
        try {
            if (session != null) session.close();
        } catch (Exception e) {
            LOG.warn("Could not close session for InquiryMgrReportDataManger.");
        }
    }
    
    @Override
    protected void logExcpetion(Throwable t) {
        LOG.error("", t);
    }
}
