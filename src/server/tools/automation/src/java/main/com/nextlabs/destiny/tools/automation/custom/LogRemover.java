/*
 * Created on Feb 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.automation.custom;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.PolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TrackingActivityLogCustomAttributeDO;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/automation/src/java/main/com/nextlabs/destiny/tools/automation/custom/LogRemover.java#1 $
 */

public class LogRemover {
    
    private static String username = "root";
    private static String password = "123blue!"; 
    private static String host = "localhost";
    private static String port = "-1";
    
    private static final String USAGE = "LogRemover -user username -password password -host host -port portnum -databasetype (oracle|postgres|sqlserver|db2) [-instance instance] [-databasename name] [-logtype (policy|document)]";
    private static String CONNECT_STRING_PREFIX = "jdbc:postgresql://";
    private static String connectString = "";
    private static String DIALECT = "net.sf.hibernate.dialect.PostgreSQLDialect";
    private static String DRIVER = "org.postgresql.Driver";
    private static String databaseType = "postgres";
    private static String databaseInstance = "";
    private static String databaseName = "activity";
    private static String logType = "";

    /**
     * starts the program.
     * 
     * @param args
     */
    public static void main(String[] args) throws HibernateException {
        if (!LogRemover.run(args)) {
            displayMessage("Correct Usage: " + USAGE);
            System.exit(1);
        }
        LogRemover.cleanPolicyLogs();
    }

    /**
     * Sets arguments from main to variables.
     * 
     * @param args
     */
    public static boolean run(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches("-user")) {
                username = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-password")) {
                password = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-host")) {
                host = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-port")) {
                port = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-databasetype")) {
                databaseType = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-instance")) {
                databaseInstance = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-databasename")) {
                databaseName = args[i + 1];
                i++;
                continue;
            } else if (args[i].matches("-logtype")) {
                logType = args[i + 1];
                i++;
                continue;
            } else {
                displayMessage("Sorry, " + args[i] + " is not a valid argument.\n");
                return false;
            }
        }
        connectString = CONNECT_STRING_PREFIX + host + ":" + port + "/" + databaseName;
        if (databaseType.equalsIgnoreCase("oracle")){
        	connectString = "jdbc:oracle:thin:@" + host + ":" + port + ":" + databaseInstance;
            DIALECT = "net.sf.hibernate.dialect.Oracle9Dialect";
            DRIVER = "oracle.jdbc.driver.OracleDriver";
        } else if (databaseType.equalsIgnoreCase("sqlserver")){
        	connectString = "jdbc:sqlserver://" + host + ":" + port +";DatabaseName=" + databaseName + ";";
            DIALECT = "com.bluejungle.framework.datastore.hibernate.dialect.SqlServer2000Dialect";
            DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (databaseType.equalsIgnoreCase("db2")){
            connectString = "jdbc:db2://" + host + ":" + port + "/" + databaseInstance + ":currentSchema=ACTIVITY;";
            DIALECT = "net.sf.hibernate.dialect.DB2Dialect";
            DRIVER = "com.ibm.db2.jcc.DB2Driver";
        }
        return true;
    }

    /**
     * Creates hard-coded data to enter into tables. Configures the hibernate
     * session.
     * 
     * @throws Exception
     */
    public static void cleanPolicyLogs() throws HibernateException, MappingException {
        Configuration cfg = new Configuration(); // begin hibernate config
        cfg.addClass(PolicyActivityLogCustomAttributeDO.class);
        cfg.addClass(PolicyActivityLogDO.class);
        cfg.addClass(TrackingActivityLogCustomAttributeDO.class);
        cfg.addClass(TrackingActivityLogDO.class);
        cfg.setProperty("hibernate.connection.url", connectString);
        cfg.setProperty("hibernate.connection.username", username);
        cfg.setProperty("hibernate.connection.password", password);
        cfg.setProperty("hibernate.dialect", DIALECT);
        cfg.setProperty("hibernate.connection.driver_class", DRIVER);
        cfg.setProperty("hibernate.order_updates", "true");
        SessionFactory sessions = cfg.buildSessionFactory();
        Session s = sessions.openSession(); // open hibernate session

        // clear the Activity Logs
        Transaction t = s.beginTransaction();
        if (logType.equalsIgnoreCase("policy") || logType.equals("")){
            s.delete("from PolicyActivityLogCustomAttributeDO");
            s.delete("from PolicyActivityLogDO");
        } 
        if (logType.equalsIgnoreCase("document") || logType.equals("")){
            s.delete("from TrackingActivityLogCustomAttributeDO");
            s.delete("from TrackingActivityLogDO");
        } 
        t.commit();
        s.close();
    }

    /**
     * Display a message to the standard output
     * 
     * @param message
     *            message to display
     */
    private static void displayMessage(final String message) {
        System.out.println(message);
    }
}