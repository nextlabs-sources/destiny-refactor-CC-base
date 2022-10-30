package com.nextlabs.destiny.container.shared.inquirymgr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.ReporterDataHolderDO;

/**
 * This encapsulates all interactions with the internal report table. The main
 * purpose is to put all modifications to this table at a single location so
 * that other classes can use the APIs here rather than know the internals of
 * the table and the properties that it contains.
 * 
 * The class is instantiated by supplying a Session that it uses for all
 * database lookups. It is the caller's responsibility to close the session.
 * 
 * There is an anomaly in the design for now. The setter methods for 
 * the minimum log time does not do the commit - it expects the client to do the 
 * commit whenever it needs to do it. But the getter methods, if it needs to set
 * the values do the commit and does not expect the client to flush the commit.
 * The reason is the setters are typically used by Archiver or the Sync that
 * decides when to commit the transaction. Hence these methods are public.
 * The getters are typically going to be used by the reporter to check the date
 * and hence needs a commit. These are package private methods that are used
 * by a wrapper method in a shared library.
 * 
 */
public class ReportDataHolderManager implements ILogEnabled, IConfigurable,
        IInitializable, IHasComponentInfo<ReportDataHolderManager> {
    public static final PropertyKey<IHibernateRepository> DATA_SOURCE_PARAM = 
            new PropertyKey<IHibernateRepository>("datasource");
    
    private static final ComponentInfo<ReportDataHolderManager> COMP_INFO =
            new ComponentInfo<ReportDataHolderManager>(
                    ReportDataHolderManager.class, 
                    LifestyleType.SINGLETON_TYPE
            );
    
    private Log log;
    private IConfiguration config;
    private IHibernateRepository datasource;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    public void init() {
        datasource = config.get(DATA_SOURCE_PARAM);
        if (datasource == null) {
            throw new IllegalArgumentException("data source is null. Please set '"
                    + DATA_SOURCE_PARAM + "'");
        }
    }

    public ComponentInfo<ReportDataHolderManager> getComponentInfo() {
       return COMP_INFO;
    }
    
    private void setMinActivityReportDate(String propertyKey, Timestamp time)
            throws HibernateException {
        if (time != null) {
            saveOrUpdate(propertyKey, time.toString());
        } else {
            remove(propertyKey);
        }
    }

    /**
     * Set the minimum timestamp for policy activity logs.
     * 
     * @param session
     * @param time
     * @throws Exception
     */
    public void setMinPolicyActivityReportDate(Timestamp time) throws HibernateException {
        setMinActivityReportDate(SharedLib.POLICY_ACTIVITY_LOG_MIN_TIME, time);
        if (log.isDebugEnabled()) {
            log.debug("Time to set: " + time);
        }
    }

    /**
     * Obtain the minimum date of policy activity log. This will first look at
     * the relevant property in the REPORT_INTERNAL_TABLE. If the property is
     * not present, it will get the minimum date from the report log table and
     * set this property in the internal table before returning the value.
     *  
     * Intentionally package-private.
     * 
     * @return The minimum report Timestamp. May be null.
     * @throws HibernateException
     * @throws SQLException 
     * @see SharedLib.getEarliestReportDate
     */
    Timestamp getMinPolicyActivityReportDate() throws HibernateException, SQLException {
        return getMinActivityReportDate(
                SharedLib.POLICY_ACTIVITY_LOG_MIN_TIME,
                SharedLib.SELECT_PA_MIN_TIME_SQL);
    }
            
    /**
     * Set the minimum timestamp for tracking activity logs.
     * 
     * @param session
     * @param time
     * @throws HibernateException
     */
    public void setMinTrackingActivityReportDate(Timestamp time) throws HibernateException {
        setMinActivityReportDate(SharedLib.TRACKING_ACTIVITY_LOG_MIN_TIME, time);
        if (log.isDebugEnabled()) {
            log.debug("Time to set: " + time);
        }
    }

    /**
     * Obtain the minimum date of policy activity log. This will first look at
     * the relevant property in the REPORT_INTERNAL_TABLE. If the property is
     * not present, it will get the minimum date from the report log table and
     * set this property in the internal table before returning the value.
     *  
     *  Note that in the situation that there is no data in the report table,
     *  this will return a null.
     *  
     * Intentionally package-private.
     * 
     * @return The minimum report Timestamp. May be null.
     * @throws SQLException 
     * @throws HibernateException
     * @see SharedLib.getEarliestReportDate
     */
    Timestamp getMinTrackingActivityReportDate() throws HibernateException, SQLException {
        return getMinActivityReportDate(
                SharedLib.TRACKING_ACTIVITY_LOG_MIN_TIME, 
                SharedLib.SELECT_TA_MIN_TIME_SQL);
    }
    
    private Timestamp getMinActivityReportDate(String propertyKey, String selectQuery)
            throws HibernateException, SQLException {
        final Session s = datasource.getCountedSession();
        
        Timestamp minTime;
        
        try {
            ReporterDataHolderDO dao = getDo(propertyKey);

            if (dao == null) {
                // this is a new property
                dao = new ReporterDataHolderDO();
            }
            
            String existingValue = dao.getValue(); 
            
            if (existingValue == null || existingValue.isEmpty()) {
                minTime = calcuateMinActivityReportDate(propertyKey, selectQuery, s, dao);
            } else {
                minTime = Timestamp.valueOf(dao.getValue());
            }
        } finally {
            datasource.closeCurrentSession();
        }
        return minTime;
    }

    private Timestamp calcuateMinActivityReportDate(String propertyKey, String selectQuery,
            Session s, ReporterDataHolderDO dao) throws HibernateException,
            SQLException {
        Timestamp minTime = null;
        
        Statement st = null;
        ResultSet rs = null;
        Transaction tx = null;
        boolean isSuccess = false;
        
        try {
            st = s.connection().createStatement();
            rs = st.executeQuery(selectQuery);
            if (rs.next()) {
                minTime = rs.getTimestamp(1);
                if (minTime != null) {
                    tx = s.beginTransaction();
                    setDOProperties(dao, propertyKey, minTime.toString());
                    s.save(dao);
                }
            }
            isSuccess = true;
        } finally {
            close(rs);
            close(st);
            commitOrRollback(tx, isSuccess);
        }
        return minTime;
    }
    
    private void setDOProperties(ReporterDataHolderDO dao, String key, String value) {
        dao.setProperty(key);
        dao.setValue(value);
    }
    
    private ReporterDataHolderDO getDo(String key) throws HibernateException{
        return getDo(key, LockMode.NONE);
    }
    
    ReporterDataHolderDO getDo(String key, LockMode lockMode) throws HibernateException{
        final Session s = datasource.getCountedSession();
        Transaction tx = null;
        ReporterDataHolderDO dao;
        boolean isSuccess = false;
        try {
            tx = s.beginTransaction();
            //why session.clear()???
            s.clear();
            dao = (ReporterDataHolderDO) s.get(ReporterDataHolderDO.class, key, lockMode);
            isSuccess = true;
        } finally {
            commitOrRollback(tx, isSuccess);
            datasource.closeCurrentSession();
        }
        return dao;
    }

    public boolean containsKey(String key) throws HibernateException {
        return getDo(key, LockMode.READ) != null;
    }

    /**
     * @param key
     * @return the value of the corresponding key or null if the key is not found, the value is null;
     * @throws HibernateException
     */
    public String get(String key) throws HibernateException {
        ReporterDataHolderDO dao = getDo(key, LockMode.READ);
        return dao != null ? dao.getValue() : null;
    }
    
    /**
     * 
     * @param key
     * @param value
     * @return true if the key and value is added successfully
     * @throws HibernateException
     */
    public boolean add(String key, String value) throws HibernateException {
        final Session s = datasource.getCountedSession();
        
        try {
            if (containsKey(key)) {
                return false;
            }
            
            ReporterDataHolderDO dao = new ReporterDataHolderDO();
            dao.setProperty(key);
            dao.setValue(value);
            
            
            boolean isSuccess = false;
            Transaction tx = s.beginTransaction();
            try {
                s.save(dao);
                isSuccess = true;
            } catch (JDBCException e){
                if (SQLHelper.isDuplicateIdException(e.getSQLException())) {
                    return false;
                }
                throw e;
            } finally {
                commitOrRollback(tx, isSuccess);
            }
        } finally {
            datasource.closeCurrentSession();
        }
        
        return true;
    }

    /**
     * 
     * @param key
     * @param value
     * @return true if the value is updated. If the entry is deleted, it will return false; 
     * @throws HibernateException
     */
    public boolean update(String key, String value) throws HibernateException {
        final Session s = datasource.getCountedSession();
        
        try {
            ReporterDataHolderDO dao = getDo(key, LockMode.READ);

            if (dao == null) {
                return false;
            }

            boolean isSuccess = false;
            Transaction tx = s.beginTransaction();
            try {
                dao.setValue(value);
                s.update(dao);
                isSuccess = true;
            } finally {
                commitOrRollback(tx, isSuccess);
            }
        } finally {
            datasource.closeCurrentSession();
        }
        
        return true;
    }
    
    public boolean saveOrUpdate(String key, String value) throws HibernateException {
        // this unused  counted session is required.
        // this will make sure all subroutine share the same session
        @SuppressWarnings("unused")
        final Session s = datasource.getCountedSession();
        
        boolean result;
        try {
            ReporterDataHolderDO dao = getDo(key, LockMode.READ);

            if (dao == null) {
                getLog().trace("saveOrUpdate -> add: " + key + ", " + value);
                result = add(key, value);
            } else {
                getLog().trace("saveOrUpdate -> update: " + key + ", " + value);
                result = update(key, value);
            }
        } finally {
            datasource.closeCurrentSession();
        }
        return result;
    }

    public String remove(String key) throws HibernateException {
        final Session s = datasource.getCountedSession();

        try {
            ReporterDataHolderDO dao = getDo(key, LockMode.READ);
            if (dao == null) {
                return null;
            }
            
            boolean isSuccess = false;
            Transaction tx = s.beginTransaction();
            try {
                s.delete(dao);
                isSuccess = true;
            } finally {
                commitOrRollback(tx, isSuccess);
            }
            return dao.getValue();
        } finally {
            datasource.closeCurrentSession();
        }
    }
    
    private void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                getLog().warn("fail to close ResultSet", e);
            }
        }
    }
    
    private void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                getLog().warn("fail to close Statement", e);
            }
        }
    }
    
    private void commitOrRollback(Transaction transaction, boolean isSuccess) {
        if (transaction != null) {
            if (isSuccess) {
                try {
                    transaction.commit();
                } catch (HibernateException e) {
                    getLog().error("fail to commit transaction.", e);
                }
            } else {
                try {
                    transaction.rollback();
                } catch (HibernateException e) {
                    getLog().warn("fail to rollback transaction", e);
                }
            }
        }
    }
}
