package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

/**
 * This utility class has only static methods for analyzing
 * <code>DatabaseMetaData</code> instances.
 */
public class DatabaseMetadataUtilities {

    public static boolean isPostgres( DatabaseMetaData metadata ) throws SQLException {
        if ( metadata != null ) {
            String rdbmsName = metadata.getDatabaseProductName();
            if ( rdbmsName != null ) {
                return rdbmsName.toLowerCase().indexOf( "postgres" ) != -1;
            }
        }
        return false;
    }

    public static boolean isDb2( DatabaseMetaData metadata ) throws SQLException {
        if ( metadata != null ) {
            String rdbmsName = metadata.getDatabaseProductName();
            if ( rdbmsName != null ) {
                return rdbmsName.toLowerCase().indexOf( "db2" ) != -1;
            }
        }
        return false;
    }

    public static boolean isSqlServer( DatabaseMetaData metadata ) throws SQLException {
        if ( metadata != null ) {
            String rdbmsName = metadata.getDatabaseProductName();
            if ( rdbmsName != null ) {
                return rdbmsName.toLowerCase().indexOf( "sql server" ) != -1;
            }
        }
        return false;
    }

}
