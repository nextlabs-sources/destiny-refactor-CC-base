/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQuery;
import com.bluejungle.destiny.container.shared.storedresults.IResultTableManager;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.ResultTableManagerException;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the base (abstract) implementation for all the result table manager
 * implementation classes.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/BaseResultTableMgrImpl.java#1 $
 */

abstract class BaseResultTableMgrImpl implements IResultTableManager, ILogEnabled, IConfigurable, IInitializable, IManagerEnabled, IDisposable {

    /**
     * Name of the sequence for the results
     */
    private static final String SEQUENCE_NAME = "stored_results_sequence";

    private IConfiguration configuration;
    private IHibernateRepository dataSource;
    private Log log;
    private IComponentManager manager;
    // Name of the data object to join against from the result table.
    private String resultTableName;

    /**
     * Calculate and save query result statistics for a given stored query
     * 
     * @param s
     *            Hibernate session to use
     * @param storedQuery
     *            stored query object (which results will be analyzed)
     * @throws HibernateException
     *             if calculating statistics or saving results fails.
     */
    protected abstract void buildQueryStatistics(Session s, StoredQueryDO storedQuery, int totalNbOfResults) throws HibernateException;

    /**
     * Creates a prepared statement based on the fake prepared statement that
     * was used to trap generated SQL and arguments.
     * 
     * @param fakeConnection
     *            fake connection used to trap the prepared SQL statement
     * @param realConnection
     *            real connection used to perform the insert in the result table
     * @return real prepared statement to be executed for the insertion.
     */
    protected PreparedStatement createInsertStatement(IInsertConnection fakeConnection, Connection realConnection) throws SQLException {
        PreparedStatement realPreparedStatement = null;
        if (fakeConnection == null) {
            throw new NullPointerException("Insert connection cannot be null");
        }
        if (realConnection == null) {
            throw new NullPointerException("Real connection cannot be null");
        }

        String realSQL = fakeConnection.getSQLQuery();
        InsertPreparedStatement preparedStatement = (InsertPreparedStatement) fakeConnection.getPreparedStatement();
        final Map<Integer,Long> longMap = preparedStatement.getLongMap();
        final Map<Integer,String> stringMap = preparedStatement.getStringMap();
        final Map<Integer,Integer> intMap = preparedStatement.getIntMap();

        //Now, prepare a real SQL query
        realPreparedStatement = realConnection.prepareStatement(realSQL);

        //Processes the int arguments
        for (Map.Entry<Integer,Integer> entry : intMap.entrySet()) {
            realPreparedStatement.setInt(entry.getKey(), entry.getValue());
        }
        //Processes the long arguments
        for (Map.Entry<Integer,Long> entry : longMap.entrySet()) {
            realPreparedStatement.setLong(entry.getKey(), entry.getValue());
        }

        // Decide if we need to double the slashes (Postgres requires it)
        boolean slashesNeedDoubling = false;
        DatabaseMetaData metadata = realConnection.getMetaData();
        slashesNeedDoubling = DatabaseMetadataUtilities.isPostgres( metadata );

        //Processes the String arguments
        for (Map.Entry<Integer,String> entry : stringMap.entrySet()) {
            String value = entry.getValue();
            if (slashesNeedDoubling && value != null && value.indexOf('%') != -1) {
                value = value.replaceAll("\\\\", "\\\\\\\\");
            }
            realPreparedStatement.setString(entry.getKey(), value);
        }

        return realPreparedStatement;
    }

    /**
     * Creates a new stored query object and stores it in the database.
     * 
     * @return the id of the new stored query
     */
    protected StoredQueryDO createNewStoredQuery(Class<?> dataObjectClass, int totalNbOfResults) {
        Session s = null;
        StoredQueryDO newQuery = getNewStoredQueryDO();
        newQuery.setResultObjectName(getResultObjectName());
        newQuery.setDataObjectName(dataObjectClass.getName());
        if (totalNbOfResults > -1) {
            StoredQueryResultsStatisticsDO stats = new StoredQueryResultsStatisticsDO();
            stats.setTotalRowCount(new Long(totalNbOfResults));
            newQuery.setStatistics(stats);
        }
        
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            t = s.beginTransaction();
            s.save(newQuery);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Error when inserting new stored query in the database", e);
            newQuery = null;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return newQuery;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the data source
     * 
     * @return the data source
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * Returns the sequence name for the results
     * 
     * @return the sequence name for the results
     */
    protected String getSequenceName() {
        return SEQUENCE_NAME;
    }

    /**
     * Returns a dummy connection used to trap and transform the SQL generated
     * by Hibernate.
     * 
     * @param realCon
     *            real connection currently used
     * @param tableName
     *            name of the table for query results
     * @param queryId
     *            of the query
     * @return a SQL connection
     */
    protected abstract Connection getNewInsertConnection(Connection realCon, String tableName, Long queryId);

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Returns a new instance of the stored query data object.
     * 
     * @return a new instance of the stored query data object.
     */
    protected StoredQueryDO getNewStoredQueryDO() {
        return new StoredQueryDO();
    }

    /**
     * Returns the name of the result table.
     * 
     * @return the physical name of the result table in the database.
     */
    protected String getResultTableName() {
        return this.resultTableName;
    }

    /**
     * Returns the name of the result data object
     * 
     * @return the name of the result data object
     */
    protected abstract String getResultObjectName();

    /**
     * Returns the DB specific expression to retrieve the sequence number of the
     * row.
     * 
     * @return a DB specific SQL expression to get the next sequence number
     */
    protected String getSequenceExpression() {
        String sequenceExpression = null;
        try {
            Dialect dialect = getDataSource().getDialect();
            if (dialect.supportsSequences()) {
                sequenceExpression = dialect.getSequenceNextValString(getSequenceName());
            }
        } catch (MappingException e) {
            // do nothing
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        return sequenceExpression;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.storedresults.IResultTableManager#getStoredQuery(java.lang.Long)
     */
    public IStoredQuery getStoredQuery(Long queryId) {
        if (queryId == null) {
            throw new NullPointerException("query id cannot be null");
        }

        IStoredQuery result = null;
        Session s = null;
        try {
            s = getDataSource().getSession();
            result = (IStoredQuery) s.get(StoredQueryDO.class, queryId);
        } catch (HibernateException e) {
            getLog().error("Error occured while retrieving stored query with id '" + queryId + "'", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.dataSource = (IHibernateRepository) getConfiguration().get(DATA_SOURCE_CONFIG_PARAM);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source is required for the result table manager configuration");
        }
        this.resultTableName = (String) getConfiguration().get(RESULT_TABLE_NAME_CONFIG_PARAM);
        if (this.resultTableName == null) {
            throw new NullPointerException("result table name is required for the result table manager configuration");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }

    /**
     * This is the main function inserting the results of the query into the
     * result table. This function decorates the current session and connection,
     * so that it lets Hibernate deal with the SQL generation.
     * 
     * @see com.bluejungle.destiny.container.shared.storedresults.IResultTableManager#storeResults(java.lang.String)
     */
    public Long storeResults(IQuery queryToExecute, Class<?> dataObjectClass, int maxResultsToStore, int totalNbOfResults) throws ResultTableManagerException {
        Long queryId = null;
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            Connection con = s.connection();
            StoredQueryDO storedQuery = createNewStoredQuery(dataObjectClass, totalNbOfResults);
            if (storedQuery != null) {
                queryId = storedQuery.getId();
            }

            if (queryId != null) {
                Connection insertCon = getNewInsertConnection(con, getResultTableName(), queryId);
                Session insertSession = getDataSource().getSession(insertCon);

                //Executes the query. Since we have decoracted the connection,
                // the query will not really hit the database. In fact, it will
                // return an empty result set, but the connection will have
                // trapped all the SQL and the prepared statements arguments.
                Query queryById = queryToExecute.getHQLQuery(insertSession);
                if (maxResultsToStore > -1) {
                    queryById.setMaxResults(maxResultsToStore);
                }
                //"executes" the query to trap the generated SQL
                queryById.list();
                
                try {
                    t = s.beginTransaction();
                    PreparedStatement realPreparedStatement = createInsertStatement((IInsertConnection) insertCon, con);
                    realPreparedStatement.execute();
                    t.commit();
                    buildQueryStatistics(s, storedQuery, totalNbOfResults);
                } catch (SQLException e) {
                    HibernateUtils.rollbackTransation(t, getLog());
                    getLog().error("Error occured while string query result for query id '" + queryById + "'", e);
                } finally {
                    HibernateUtils.closeSession(s, getLog());
                }
            } else {
                getLog().error("Unable to store query results");
                throw new ResultTableManagerException(); //TODO - Fix
            }
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw new ResultTableManagerException(e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
        return queryId;
    }
}
