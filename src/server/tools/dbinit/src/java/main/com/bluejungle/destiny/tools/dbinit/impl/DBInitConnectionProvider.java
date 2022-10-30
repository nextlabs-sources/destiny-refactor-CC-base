/*
 * Created on Jul 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.connection.ConnectionProvider;
import net.sf.hibernate.connection.ConnectionProviderFactory;
import net.sf.hibernate.util.JDBCExceptionReporter;
import net.sf.hibernate.util.PropertiesHelper;
import net.sf.hibernate.util.ReflectHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a very simple connection provider class for the DBInit tool
 * specifically. Since the connection provider only requires one connection at a
 * time, this provider is a very provider with only one session.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/impl/DBInitConnectionProvider.java#1 $
 */

public class DBInitConnectionProvider implements ConnectionProvider {

    /**
     * Log file
     */
    private static final Log LOG = LogFactory.getLog(DBInitConnectionProvider.class);

    /**
     * Properties that have values that should be hidden from the log file
     */
    private static final String PASSWORD_HIBERNATE_PROP = "password";
    private static final String SECRET_DISPLAY = "***************";
    private static final Set<String> HIDDEN_PROPS = new HashSet<String>();
    static {
        HIDDEN_PROPS.add(PASSWORD_HIBERNATE_PROP);
    }

    private Properties connectionProps;
    private Integer isolation;
    private String url;

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#close()
     */
    public void close() throws HibernateException {
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#closeConnection(java.sql.Connection)
     */
    public void closeConnection(Connection conn) throws SQLException {
        getLog().debug("closing JDBC connection");
        try {
            conn.close();
        } catch (SQLException sqle) {
            JDBCExceptionReporter.logExceptions(sqle);
            throw sqle;
        }
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#configure(java.util.Properties)
     */
    public void configure(Properties props) throws HibernateException {
        final String driverClass = props.getProperty(Environment.DRIVER);

        this.isolation = PropertiesHelper.getInteger(Environment.ISOLATION, props);
        if (isolation != null)
            getLog().info("JDBC isolation level: " + Environment.isolationLevelToString(isolation.intValue()));

        if (driverClass == null) {
            getLog().warn("no JDBC Driver class was specified by property " + Environment.DRIVER);
        } else {
            try {
                // trying via forName() first to be as close to DriverManager's
                // semantics
                Class.forName(driverClass);
            } catch (ClassNotFoundException cnfe) {
                try {
                    ReflectHelper.classForName(driverClass);
                } catch (ClassNotFoundException e) {
                    String msg = "JDBC Driver class not found: " + driverClass;
                    getLog().fatal(msg);
                    throw new HibernateException(msg);
                }
            }
        }

        this.url = props.getProperty(Environment.URL);
        if (this.url == null) {
            String msg = "JDBC URL was not specified by property " + Environment.URL;
            getLog().fatal(msg);
            throw new HibernateException(msg);
        }

        this.connectionProps = ConnectionProviderFactory.getConnectionProperties(props);
        getLog().info("using driver: " + driverClass + " at URL: " + this.url);
        Iterator propIt = this.connectionProps.keySet().iterator();
        while (propIt.hasNext()) {
            String propName = (String) propIt.next();
            String propValue = null;
            if (HIDDEN_PROPS.contains(propName)) {
                propValue = SECRET_DISPLAY;
            } else {
                propValue = this.connectionProps.getProperty(propName);
            }
            getLog().info("Setting '" + propName + "' as '" + propValue + "'");
        }
    }

    /**
     * @see net.sf.hibernate.connection.ConnectionProvider#getConnection()
     */
    public Connection getConnection() throws SQLException {
        Connection result = DriverManager.getConnection(this.url, connectionProps);
        Integer isol = getIsolation();
        if (isol != null) {
            result.setTransactionIsolation(isol.intValue());
        }
        if (result.getAutoCommit()) {
            result.setAutoCommit(false);
        }
        getLog().debug("created connection to: " + url + ", Isolation Level: " + result.getTransactionIsolation());
        return result;
    }

    /**
     * Returns the isolation level
     * 
     * @return the isolation level
     */
    protected Integer getIsolation() {
        return this.isolation;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }
}