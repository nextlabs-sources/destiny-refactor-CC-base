/*
 * Created on Sep 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

/**
 * A Connection Pool which wraps other connection pool and adds tracking for
 * leaked connections
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/ConnectionTrackingConnectionPoolWrapper.java#1 $
 */

public class ConnectionTrackingConnectionPoolWrapper implements IConnectionPool {


    /**
     * Connection tracking stack trace flag
     */
    public static final String CONNECTION_TRACKER_OUTPUT_STACK_TRACE_PROPERTY_NAME = "com.bluejungle.destiny.connection.tracker.includestack";

    /**
     * State connection time property name
     */
    public static final String CONNECTION_TRACKER_STALE_CONNECTION_TIME_PROPERTY_NAME = "com.bluejungle.destiny.connection.tracker.connectiontimeout";

    /**
     * Default value determine whether or not to include stack trace in debug
     * output
     */
    private static final boolean DEFAULT_INCLUDE_STACK_TRACE = false;

    /**
     * Default stale connection time - Declares when a connection is considered
     * to be leaked
     */
    private static final long DEFAULT_CONNECTION_TRACKER_STALE_CONNECTION_TIME = 10 * 60 * 1000;

    private IConnectionPool wrappedConnectionPool;

    private boolean captureClientStack = false;
    private long staleConnectionTime;
    private final Map connectionTrackingMap = new HashMap();
    private Timer connectionTrackingTimer;
    private Log log = LogFactory.getLog(ConnectionTrackingConnectionPoolWrapper.class.getName());

    /**
     * Create an instance of ConnectionTrackingConnectionPool
     * 
     * @param wrapper
     */
    public ConnectionTrackingConnectionPoolWrapper(IConnectionPool wrappedConnectionPool) {
        if (wrappedConnectionPool == null) {
            throw new NullPointerException("wrappedConnectionPool cannot be null.");
        }

        this.wrappedConnectionPool = wrappedConnectionPool;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#close()
     */
    public void close() throws SQLException {
        this.wrappedConnectionPool.close();
        this.connectionTrackingTimer.cancel();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getConnection()
     */
    public Connection getConnection() throws SQLException {
        Connection conn = this.wrappedConnectionPool.getConnection();

        long currentTime = System.currentTimeMillis();
        ConnectionTrackerTag trackerTag = null;
        if (shouldCaptureClientStack()) {
            StackTraceElement[] clientStack = new Throwable().getStackTrace();
            trackerTag = new ConnectionTrackerTag(currentTime, clientStack);
        } else {
            trackerTag = new ConnectionTrackerTag(currentTime);
        }

        synchronized (this.connectionTrackingMap) {
            this.connectionTrackingMap.put(conn, trackerTag);
        }

        return conn;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#getName()
     */
    public String getName() {
        return this.wrappedConnectionPool.getName();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#initialize(com.bluejungle.destiny.server.shared.repository.IConnectionPoolConfiguration)
     */
    public void initialize(IConnectionPoolConfiguration configuration) throws ConnectionPoolConfigurationException, ConnectionPoolInitializationFailedException {
        this.wrappedConnectionPool.initialize(configuration);

        Properties configProperties = configuration.getProperties();
        String providedCaptureClientStackProperty = configProperties.getProperty(CONNECTION_TRACKER_OUTPUT_STACK_TRACE_PROPERTY_NAME);
        if (providedCaptureClientStackProperty != null) {
            setCaptureClientStack(Boolean.valueOf(providedCaptureClientStackProperty).booleanValue());
        } else {
            setCaptureClientStack(DEFAULT_INCLUDE_STACK_TRACE);
        }

        String providedStaleConnectionTimeProperty = configProperties.getProperty(CONNECTION_TRACKER_STALE_CONNECTION_TIME_PROPERTY_NAME);
        if (providedStaleConnectionTimeProperty != null) {
            setStaleConnectionTime(Long.valueOf(providedStaleConnectionTimeProperty).longValue());
        } else {
            setStaleConnectionTime(DEFAULT_CONNECTION_TRACKER_STALE_CONNECTION_TIME);
        }

        this.connectionTrackingTimer = new Timer("ConnectionLeakTracker", true);
        this.connectionTrackingTimer.scheduleAtFixedRate(new ConnectionTracker(), this.getStaleConnectionTime(), this.getStaleConnectionTime());
    }

    /**
     * @see com.bluejungle.destiny.server.shared.repository.IConnectionPool#releaseConnection(java.sql.Connection)
     */
    public void releaseConnection(Connection c) throws SQLException {
        this.wrappedConnectionPool.releaseConnection(c);

        synchronized (this.connectionTrackingMap) {
            this.connectionTrackingMap.remove(c);
        }
    }

    /**
     * Retrieve the captureClientStack.
     * 
     * @return the captureClientStack.
     */
    private boolean shouldCaptureClientStack() {
        return this.captureClientStack;
    }

    /**
     * Set the captureClientStack
     * 
     * @param captureClientStack
     *            The captureClientStack to set.
     */
    private void setCaptureClientStack(boolean captureClientStack) {
        this.captureClientStack = captureClientStack;
    }

    /**
     * Retrieve the staleConnectionTime.
     * 
     * @return the staleConnectionTime.
     */
    private long getStaleConnectionTime() {
        return this.staleConnectionTime;
    }

    /**
     * Set the staleConnectionTime
     * 
     * @param staleConnectionTime
     *            The staleConnectionTime to set.
     */
    private void setStaleConnectionTime(long staleConnectionTime) {
        this.staleConnectionTime = staleConnectionTime;
    }

    private class ConnectionTrackerTag {

        private StackTraceElement[] ownerStack;
        private long timeAquired;

        /**
         * Create an instance of ConnectionTrackerTag
         * 
         * @param timeAquired
         */
        private ConnectionTrackerTag(long timeAquired) {
            super();
            this.timeAquired = timeAquired;
        }

        /**
         * Create an instance of ConnectionTrackerTag
         * 
         * @param timeAquired
         * @param ownerStack
         */
        private ConnectionTrackerTag(long timeAquired, StackTraceElement[] ownerStack) {
            if (ownerStack == null) {
                throw new NullPointerException("ownerStack cannot be null.");
            }

            this.timeAquired = timeAquired;
            this.ownerStack = ownerStack;
        }

        /**
         * Retrieve the timeAquired.
         * 
         * @return the timeAquired.
         */
        private long getTimeAquired() {
            return this.timeAquired;
        }

        /**
         * Retrieve the ownerStack.
         * 
         * @return the ownerStack.
         */
        private StackTraceElement[] getOwnerStack() {
            if (this.ownerStack == null) {
                throw new IllegalStateException("Owner stack is not available");
            }
            return this.ownerStack;
        }

        /**
         * Determine if the owner stack is available
         * 
         * @return true if the owner stack is availabe; flase otherwise
         */
        private boolean hasOwnerStack() {
            return (this.ownerStack != null);
        }
    }

    private class ConnectionTracker extends TimerTask {

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            long currentTime = System.currentTimeMillis();
            synchronized (ConnectionTrackingConnectionPoolWrapper.this.connectionTrackingMap) {
                Iterator connectionTrackingMapIterator = ConnectionTrackingConnectionPoolWrapper.this.connectionTrackingMap.entrySet().iterator();
                boolean hasLeakedConnections = true;
                while ((hasLeakedConnections) && (connectionTrackingMapIterator.hasNext())) {
                    Map.Entry nextConnectionEntry = (Entry) connectionTrackingMapIterator.next();
                    ConnectionTrackerTag nextTag = (ConnectionTrackerTag) nextConnectionEntry.getValue();
                    long connectionAcquisitionTime = nextTag.getTimeAquired();
                    if ((currentTime - connectionAcquisitionTime) > ConnectionTrackingConnectionPoolWrapper.this.getStaleConnectionTime()) {
                        StringBuffer warningMessage = new StringBuffer("Connection leak detected.\n");
                        if (nextTag.hasOwnerStack()) {
                            warningMessage.append("Connection acquisition stack trace:\n");
                            StackTraceElement[] stackTraceElements = nextTag.getOwnerStack();
                            for (int i = 0; i < stackTraceElements.length; i++) {
                                warningMessage.append("\tat " + stackTraceElements[i] + "\n");
                            }
                        } else {
                            warningMessage.append("To enable output of the connection acquisition stack trace, set the connection pool property, \"");
                            warningMessage.append(CONNECTION_TRACKER_OUTPUT_STACK_TRACE_PROPERTY_NAME);
                            warningMessage.append("\", to \"true\".");
                        }

                        ConnectionTrackingConnectionPoolWrapper.this.log.warn(warningMessage.toString());

                        connectionTrackingMapIterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return wrappedConnectionPool.isClosed();
    }
    
    public void reset() {
        synchronized (connectionTrackingMap) {
            connectionTrackingMap.clear();
        }
    }
}
