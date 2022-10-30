/*
 * Created on Jun 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/SQLHelper.java#1 $
 */

public class SQLHelper {
    /**
     * sort of copy from MassDML.java
     * 
     * Makes a string of the form "(?,?,...,?)" with <code>size</code>
     * question marks.
     * @param size The number of question marks to insert.
     * The value of this argument must be a positive number.
     * @return A string of <code>size</code> comma-separated
     * question marks suitable for use as an in-list in a query.
     */
    public static String makeInList(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size may not be negative: "+size);
        }
        StringBuilder res = new StringBuilder("(");
        for (int i = 0; i != size; i++) {
            if (i != 0) {
                res.append(',');
            }
            res.append('?');
        }
        res.append(')');
        return res.toString();
    }

    /**
     * Standard SQL state
     * A violation of the constraint imposed by a unique index or a unique constraint occurred. 
     */
    private static final String POSTGRESQL_DUPLICATE_ID_SQL_STATE = "23505";
    private static final String CONSTRAINT_VIOLATION_SQL_STATE = "23000";
    
    private static final int ORACLE_UNIQUE_ID_VIOLATION_ERR_CODE = 1;
    private static final int MS_SQL_UNIQUE_ID_VIOLATION_ERR_CODE = 2627;
    
    /**
     * copy from HibveranteLogWriter
     * @param e
     * @return
     */
    public static boolean isDuplicateIdException(SQLException e) {
        if (e instanceof BatchUpdateException) {
            SQLException temp = ((BatchUpdateException) e).getNextException();
            if (temp != null) {
                e = temp;
            }
        }
        
        if (CONSTRAINT_VIOLATION_SQL_STATE.equals(e.getSQLState()) && 
             e.getErrorCode() == ORACLE_UNIQUE_ID_VIOLATION_ERR_CODE) {
            return true;
        } else if (POSTGRESQL_DUPLICATE_ID_SQL_STATE.equals(e.getSQLState())) {
            return true;
        } else if (CONSTRAINT_VIOLATION_SQL_STATE.equals(e.getSQLState()) &&
            e.getErrorCode() == MS_SQL_UNIQUE_ID_VIOLATION_ERR_CODE) {
            return true;
        }
        return false;
    }
}
