/*
 * Created on Jul 1, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository.c3p0impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.impl.DefaultConnectionTester;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/c3p0impl/ConnectionAlivenessTester.java#1 $
 */

public class ConnectionAlivenessTester extends DefaultConnectionTester {
    
    /**
     * the difference between CONNECTION_IS_INVALID and DATABASE_IS_INVALID is in
     * com.mchange.v2.c3p0.impl.ConnectionEventListenerImpl.connectionErrorOccurred(ConnectionEvent)
     * 
     * CONNECTION_IS_INVALID will mark the connection as broken
     * DATABASE_IS_INVALID will reset the pool.
     * 
     */
    
    @Override
    public int statusOnException(Connection c, Throwable t, String query) {
        boolean isClosedConnection = false;
        if(t instanceof SQLException){
//            //very database driver specific checking
//            if(c instanceof com.microsoft.sqlserver.jdbc.SQLServerConnection){
//                
//                try {
//                    if (c.isClosed()) {
//                        isClosedConnection = true;
//                    }
//                    // should not throw SQLException in SQLServerConnection
//                } catch (SQLException e) {
//                    isClosedConnection = true;
//                }
//            }else if(c instanceof oracle.jdbc.OracleConnection){
//              //very database driver specific checking
//                try {
//                    if (c.isClosed()) {
//                        isClosedConnection = true;
//                    }
//                    // should not throw SQLException in OracleConnection
//                } catch (SQLException e) {
//                    isClosedConnection = true;
//                }
//            }else if(c instanceof org.postgresql.PGConnection){
//                try {
//                    if (c.isClosed()) {
//                        isClosedConnection = true;
//                    }
//                    // should not throw SQLException in OracleConnection
//                } catch (SQLException e) {
//                    isClosedConnection = true;
//                }
//            }
        
            try {
                if (c.isClosed()) {
                    isClosedConnection = true;
                }
                // should not throw SQLException in OracleConnection
            } catch (SQLException e) {
                isClosedConnection = true;
            }
        }
 
        return isClosedConnection 
                ? CONNECTION_IS_INVALID 
                : super.statusOnException(c, t, query);
    }
}
