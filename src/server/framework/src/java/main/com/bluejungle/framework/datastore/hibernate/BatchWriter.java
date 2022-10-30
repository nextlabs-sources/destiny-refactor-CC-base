/*
 * Created on Jun 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/BatchWriter.java#1 $
 */

public abstract class BatchWriter<T> implements ILogEnabled {
    protected enum FailState{
        CREATE_PERPARE_STATEMENTS("Fail to create PerpareStatements.")
        , INSERT_BATCH("Fail to insert batch.")
        , INSERT_INDIVIDUALLY("Fail to insert row.")
        , DUPLICATED_ROW("Duplicated entries.")
        , CLOSE_PREPARED_STATEMENT("Fail to close PerpareStatements.")
        ;
        
        final String message;
        FailState(String message){
            this.message = message; 
        }
        
        protected String getMessage(){
            return message;
        }
    }
    
    protected List<T> duplicatedRows;
    protected Log log;
    
    protected void logError(FailState failStatus, Throwable t, Object... objects) {
        StringBuilder sb = new StringBuilder(failStatus.message);
        if (t instanceof SQLException) {
            SQLException e = (SQLException) t;
            sb.append(" ").append( t.getClass())
                .append(", code = ").append(e.getErrorCode())
                .append(", state = ").append(e.getSQLState())
                .append(", reason = ").append(e.getMessage());

            if( t instanceof BatchUpdateException){
                SQLException nextExcpetion = ((BatchUpdateException) t).getNextException();
                SQLException se = nextExcpetion != null
                    ? nextExcpetion
                    : (BatchUpdateException) t;
                
                if (!SQLHelper.isDuplicateIdException(se)) {
                    t = se;
                } else {
                    if (nextExcpetion != null) {
                        sb.append(", next exception = ").append(se.getMessage());
                    }
                    t = null;
                } 
            }else{
                t = null;
            }
        } 
        
        if (objects != null && objects.length > 0) {
            sb.append(toLogString(objects));
        }
        
        // common logging can handle what t is null
        getLog().error(sb.toString(), t);
    }
    
    /**
     * 
     * @param objects can't be null
     * @return
     */
    protected String toLogString(Object[] objects){
        StringBuilder sb = new StringBuilder();
        sb.append("\nRelated to the following ").append(objects.length).append(" object(s):\n");
        for (int i = 0; i < objects.length; i++) {
            sb.append(objects[i] != null ? format(objects[i]) : null);
            
            if (i != objects.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    protected String format(Object obj){
        return obj.getClass().getSimpleName() + ":" + obj;
    }
    
    /**
     * 
     * @param rows
     * @param session
     * @return
     * @throws DataSourceException if any exception occurs other than duplicated log exception
     */
    public int[][] log(Collection<T> rows, Session session) throws DataSourceException {
        final PreparedStatement[] preparedStatements;
        duplicatedRows = new ArrayList<T>();
        try {
            Connection conn = session.connection();
            preparedStatements = createPerpareStatements(conn);
        } catch (SQLException e) {
            logError(FailState.CREATE_PERPARE_STATEMENTS, e);
            throw new DataSourceException(e);
        } catch (HibernateException e) {
            logError(FailState.CREATE_PERPARE_STATEMENTS, e);
            throw new DataSourceException(e);
        } 
        
        //no need to set the FlushMode, hibernate transaction will do it for me.
        
        int[][] results;
        try {
            Transaction t = session.beginTransaction();
            try {
                results = insertBatch(rows, preparedStatements);
                t.commit();
            } catch (Exception e) {
                t.rollback();
                logError(FailState.INSERT_BATCH, e);
                
                insertBatchFailed(rows, session, e);
                
                if( e instanceof SQLException) {
                    results = insertIndividually(rows, preparedStatements, session);
                } else if (e instanceof DataSourceException) {
                    throw (DataSourceException) e;
                } else {
                    throw new DataSourceException(e);
                }
            } finally{
                closeAllStatements(preparedStatements);
                if (!duplicatedRows.isEmpty()) {
                    logError(FailState.DUPLICATED_ROW, null, duplicatedRows.toArray());
                }
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
        
        return results;
    }
    
    /**
     * 
     * @param conn
     * @return 
     * @throws SQLException will interrupt the log process
     * @throws DataSourceException will interrupt the log process
     */
    protected abstract PreparedStatement[] createPerpareStatements(Connection conn)
            throws SQLException, DataSourceException;
    
    /**
     * 
     * @param statement
     * @param row
     * @param indexOfStatement
     * @throws SQLException will cause the insertion fail. 
     *              If doing batch insert, it will try individual insert.
     *              If doing individual insert, it will try next row
     * @throws DataSourceException will interrupt the log process
     */
    protected abstract void setValues(PreparedStatement statement, T row, int indexOfStatement)
            throws SQLException, DataSourceException;
    
    /**
     * You are inside a transaction.
     * @param rows
     * @param preparedStatements
     * @return results
     * @throws SQLException if the value can't be set in the preparedStatements 
     *                      or the preparedStatement can't be executed 
     * @throws DataSourceException will interrupt the log process  
     */
    protected int[][] insertBatch(Collection<T> rows, PreparedStatement[] preparedStatements)
            throws SQLException, DataSourceException {
        int[][] results = new int[preparedStatements.length][];
        
        for (T row : rows) {
            int i = 0;
            for (PreparedStatement preparedStatement : preparedStatements) {
                setValues(preparedStatement, row, i++);
            }
        }
        
        int i = 0;
        for (PreparedStatement preparedStatement : preparedStatements) {
            results[i++] = preparedStatement.executeBatch();
        }
        return results;
    }
    
    /**
     * 
     * @param row
     * @param session
     * @param se
     * @throws DataSourceException will interrupt the log process  
     */
    protected void insertBatchFailed(Collection<T> row, Session session, Exception se)
            throws DataSourceException {
        //do nothing
    }

    /**
     * insert log individually. You are not inside a transaction
     * @param rows
     * @param preparedStatements, don't close them.
     * @return
     * @throws HibernateException if the transaction fails.
     * @throws DataSourceException will interrupt the log process   
     */
    protected int[][] insertIndividually(Collection<T> rows,
            PreparedStatement[] preparedStatements, Session session) throws HibernateException,
            DataSourceException {
        try {
            for (PreparedStatement preparedStatement : preparedStatements) {
                preparedStatement.clearBatch();
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
        
        int size = preparedStatements.length;
        WriterOnlyArrayList[] results = new WriterOnlyArrayList[preparedStatements.length];
        for (int i = 0; i < size; i++) {
            results[i] = new WriterOnlyArrayList(rows.size());
        }

        int logIndex = 0;
        for (T row : rows) {
            Transaction t = session.beginTransaction();
            try {
                int j = 0;
                for (PreparedStatement preparedStatement : preparedStatements) {
                    setValues(preparedStatement, row, j);
                    results[j].add(preparedStatement.executeBatch());
                    j++;
                }
                t.commit();
            } catch(SQLException se){
                t.rollback();
               
                insertIndividuallyFailed(row, session, se);
            }
            logIndex++;
        }
        
        int[][] resultsArray = new int[size][];
        for (int i = 0; i < size; i++) {
            resultsArray[i] = results[i].toArray();
        }
        
        return resultsArray;
    }
    
    /**
     * 
     * @param row
     * @param se
     */
    protected void logDuplicatedRow(T row, SQLException se){
        duplicatedRows.add(row);
    }
    
    /**
     * 
     * @param row
     * @param session
     * @param se
     * @throws DataSourceException will interrupt the log process   
     */
    protected void insertIndividuallyFailed(T row, Session session, SQLException se)
            throws DataSourceException {
        if (SQLHelper.isDuplicateIdException(se)) {
            logDuplicatedRow(row , se);
        }else{
            logError(FailState.INSERT_INDIVIDUALLY, se, row);
            //don't throw DataSourceException, it will block the whole insert process.
        }
    }
    
    protected void closeAllStatements(PreparedStatement[] preparedStatements) {
        for (PreparedStatement preparedStatement : preparedStatements) {
            try {
                preparedStatement.close();
            } catch (SQLException se) {
                logError(FailState.CLOSE_PREPARED_STATEMENT, se, preparedStatement);
            }
        }
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }
    
    private static class WriterOnlyArrayList{
        private int[] data;
        
        /**
         * max size which is the size of data[]
         */
        private int size;
        
        /**
         * last free space index
         */
        private int current;
        
        /**
         * 
         * @param size The size of the Array to begin
         */
        public WriterOnlyArrayList(int size){
            this.size = size;
            data = new int[size];
            current = 0;
        }
        
        public void ensureCapacity(int minCapacity) {
            if (minCapacity > size) {
                int[] oldData = data;
                //the size should match perfectly.
                //if not, that mean each setValue() is add batch more than once.
                int newSize = size * 3 / 2 + 1;
                if (newSize < minCapacity)
                    newSize = minCapacity;
                data = new int[newSize];
                System.arraycopy(oldData, 0, data, 0, size);
                size = newSize;
            }
        }
        
        public void add(int... results) {
            if (results == null) {
                // can results is null?
                return;
            }
            ensureCapacity(current + results.length);
            System.arraycopy(results, 0, data, current, results.length);
            current += results.length;
        }
        
        public int[] toArray() {
            trimToSize();
            return data;
        }
        
        public void trimToSize() {
            int actualSize = data.length;
            if (size < actualSize) {
                int[] oldData = data;
                data = new int[size];
                System.arraycopy(oldData, 0, data, 0, size);
                size = actualSize;
            }
        }
    }
}
