/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.report.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.destiny.container.shared.inquirymgr.report.IReportValueConverter;
import com.nextlabs.report.datagen.IReportDataManager;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/report/impl/ReportDataMangerStandalone.java#1 $
 */

public class ReportDataMangerStandalone extends BaseReportDataManager implements IReportDataManager{
    private static final IReportValueConverter VALUE_CONVERTER = new ReportValueConverterShared();
    
    private static final String DRIVER_KEY   = "driver";
    private static final String URL_KEY      = "database.url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String CONFIG_PATH  = "/db-config.properties";
    
    private final String url;
    private final String username;
    private final String password;
    private final byte dbType;
    
    protected Connection connection;
    private static final Log LOG =  LogFactory.getLog(
                ReportDataMangerStandalone.class.getName());
    
    public ReportDataMangerStandalone() throws IOException, ClassNotFoundException {
        InputStream is = ReportDataMangerStandalone.class.getResourceAsStream(CONFIG_PATH);
        Properties properties = new Properties();
        try {
            properties.load(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        
        String driver =   properties.getProperty(DRIVER_KEY);
        url =      properties.getProperty(URL_KEY);
        username = properties.getProperty(USERNAME_KEY);
        password = properties.getProperty(PASSWORD_KEY);
        
        Class<?> clazz = Class.forName(driver);
        
        
        if(Class.forName("org.postgresql.Driver").isAssignableFrom(clazz)){
            dbType = DB_TYPE_POSTGRESQL;
        }else if(Class.forName("oracle.jdbc.driver.OracleDriver").isAssignableFrom(clazz)){
            dbType = DB_TYPE_ORACLE;
        }else if(Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").isAssignableFrom(clazz)){
            dbType = DB_TYPE_MS_SQL;
        }else{
            throw new IllegalArgumentException("unknown database type: " + clazz);
        }
    }
    
    
    @Override
    protected Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
        return connection;
    }
    
    protected void closeConnection() throws Exception { 
        try {
            if (connection != null) connection.close();
        } catch (SQLException ex) {
            LOG.warn("Could not close connection for ReportDataMangerStandalone.");
        }
    }

    @Override
    public byte getDatabaseType() {
        return dbType;
    }

    @Override
    public String getMappedAction(String name) {
        return VALUE_CONVERTER.getActionDisplayName(name);
    }


    @Override
    protected void logExcpetion(Throwable t) {
        System.err.println(t.toString());
    }
}
