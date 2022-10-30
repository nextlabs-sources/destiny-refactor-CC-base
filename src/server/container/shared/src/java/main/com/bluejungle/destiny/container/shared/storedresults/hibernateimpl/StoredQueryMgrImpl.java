/*
 * Created on Aug 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQuery;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryMgr;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * The stored query manager takes care of cleaning up old stored query results
 * from the result tables. Stored query results expire once their corresponding
 * stored query has not been updated for a certain time.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQueryMgrImpl.java#1 $
 */

public class StoredQueryMgrImpl extends Thread implements IDisposable, IStoredQueryMgr, IConfigurable, IInitializable, ILogEnabled {

    public StoredQueryMgrImpl(){
        super("StoredQueryMgr");
    }
    
    /**
     * SQL query for stored results by id deletion
     */
    private static final String DELETE_ID_RESULTS = "delete from STORED_QUERY_BY_ID_RESULTS WHERE STORED_QUERY_ID = ?";

    /**
     * SQL query for stored summary results deletion
     */
    private static final String DELETE_SUMMARY_RESULTS = "delete from STORED_QUERY_SUMMARY_RESULTS WHERE STORED_QUERY_ID = ?";

    /**
     * Default stored query expiration time in milliseconds.
     */
    protected static final long DEFAULT_DATA_EXPIRATION = 3 * 3600 * 1000;

    /**
     * Default sleep amount between data cleanups in milliseconds.
     */
    protected static final long DEFAULT_SLEEP_AMOUNT = 10 * 60 * 1000;

    /**
     * Session flush threshold
     */
    protected static final int HIBERNATE_SESSION_FLUSH_THRESHOLD = 25;

    private IHibernateRepository activityDataSource;
    private IConfiguration configuration;
    private long dataExpirationAmount;
    private long sleepAmount;
    private Log log;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        interrupt();
    }

    /**
     * Returns the activity data source
     * 
     * @return the activity data source
     */
    protected IHibernateRepository getActivityDataSource() {
        return this.activityDataSource;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the data expiration amount
     * 
     * @return the data expiration amount
     */
    protected long getDataExpirationAmount() {
        return this.dataExpirationAmount;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Returns the sleep amount between data cleanup
     * 
     * @return the sleep amount between data cleanup
     */
    protected long getSleepAmount() {
        return this.sleepAmount;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration config = getConfiguration();
        this.activityDataSource = config.get(ACTIVITY_DATA_SOURCE_CONFIG_PARAM);
        if (this.activityDataSource == null) {
            throw new NullPointerException("Activity data source should be passed in the configuration");
        }

        this.dataExpirationAmount = config.get(DATA_EXPIRATION_AMOUNT_CONFIG_PARAM, DEFAULT_DATA_EXPIRATION);
        this.sleepAmount = config.get(SLEEP_AMOUNT_CONFIG_PARAM, DEFAULT_SLEEP_AMOUNT);
        this.setDaemon(true);
        start();
    }

    /**
     * Main loop function. This function waits for a while, deletes old stored
     * query data, and goes back to sleep.
     */
    protected void mainLoop() {
        try {
			synchronized (this) {
				while (!this.isInterrupted()) {
                    processData(new Date());
                    wait(getSleepAmount());
				}
			}
	    } catch (InterruptedException e) {}
		
	    //Thread is interrupted, leave the main loop
        getLog().trace("Stored query manager was interrupted");
    }

    /**
     * This is the processing data function. It queries for old stored queries,
     * and wipes out data that is too old.
     * 
     * @param asOf
     *            date that should be used for selecting data to be deleted.
     *            This value cannot be null.
     */
    protected void processData(final Date asOf) {
        if (asOf == null) {
            throw new NullPointerException("A date must be provided");
        }
        Session s = null;
        Transaction t = null;
        boolean bMore = true;
        while (bMore) {
            try {
                final IHibernateRepository dataSource = getActivityDataSource();
                s = dataSource.getSession();
                //Finds all the matching queries
                Criteria crit = s.createCriteria(StoredQueryDO.class);
                final long dataCutOffDate = asOf.getTime() - getDataExpirationAmount();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dataCutOffDate);
                crit.add(Expression.lt("creationTime", cal));
                crit.setMaxResults(30);
                final List<IStoredQuery> queriesToDelete = crit.list();
                if (queriesToDelete.size() < 30) {
                    bMore = false;
                }
                Iterator<IStoredQuery> it = queriesToDelete.iterator();
                Connection con = s.connection();
                final boolean oldAutoCommit = con.getAutoCommit();
                con.setAutoCommit(false);

                //First, delete all the child records
                while (it.hasNext()) {
                    final long beginTime = System.currentTimeMillis();
                    IStoredQuery queryToDelete = it.next();
                    long idToDelete = queryToDelete.getId().longValue();
                    //deletes the results associated with each stored query
                    PreparedStatement deleteStatement = con.prepareStatement(DELETE_ID_RESULTS);
                    deleteStatement.setLong(1, idToDelete);
                    deleteStatement.execute();
                    deleteStatement = con.prepareStatement(DELETE_SUMMARY_RESULTS);
                    deleteStatement.setLong(1, idToDelete);
                    deleteStatement.execute();
                    con.commit();
                    final long endTime = System.currentTimeMillis();
                    getLog().debug("Query '" + idToDelete + "' stored results where deleted in " + (endTime - beginTime) + " ms.");
                }
                con.setAutoCommit(oldAutoCommit);
                t = s.beginTransaction();
                final long beginTime = System.currentTimeMillis();

                //Second, delete all the stored query object
                it = queriesToDelete.iterator();
                List<IStoredQuery> evictionList = new ArrayList<IStoredQuery>(HIBERNATE_SESSION_FLUSH_THRESHOLD);
                int iterationCount = 0;
                while (it.hasNext()) {
                    iterationCount++;
                    IStoredQuery queryToDelete = it.next();
                    s.delete(queryToDelete);
                    evictionList.add(queryToDelete);
                    if (iterationCount % HIBERNATE_SESSION_FLUSH_THRESHOLD == 0) {
                        s.flush();
                        HibernateUtils.evictObjects(s, evictionList);
                    }
                }
                t.commit();
                final long endTime = System.currentTimeMillis();
                getLog().debug("Deleted '" + queriesToDelete.size() + "' stored queries in " + (endTime - beginTime) + " ms.");
            } catch (HibernateException e) {
                HibernateUtils.rollbackTransation(t, getLog());
                getLog().error("Error when fetching list of stored queries to delete", e);
                bMore = false;
            } catch (SQLException e) {
                getLog().error("Error when deleting stored query results", e);
                bMore = false;
            } finally {
                HibernateUtils.closeSession(s, getLog());
            }
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        super.run();
        mainLoop();
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }
}
