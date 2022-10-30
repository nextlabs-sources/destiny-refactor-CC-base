/*
 * Created on Feb 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.framework.datastore.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a set of utility functions for Hibernate related tasks.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/HibernateUtils.java#1 $
 */

public final class HibernateUtils {
    //oracle is 1000, mssql is 2100
    public static final int DATABASE_PARAMETER_LIMIT = 900;

    private static final Log LOG = LogFactory.getLog(HibernateUtils.class.getName());

    /**
     * Convenience method for rolling back a Hibernate Transaction
     * 
     * @param transation
     *            the transaction to roll back
     * @param log
     *            a Logger to which to log a warning message if the rollback
     *            fails
     */
    public static void rollbackTransation(Transaction transation, Log log) {
        if (transation != null) {
            if (log == null) {
                log = LOG;
            }

            try {
                transation.rollback();
            } catch (HibernateException exception) {
                log.warn("Failed to rollback Hibernate transaction", exception);
            }
        }
    }

    /**
     * Convenience method for close Hibernate Session
     * 
     * @param hSession
     *            the Hibernate Session to close
     * @param log
     *            a Logger to which to log a warning message if the session
     *            close fails
     */
    public static void closeSession(Session hSession, Log log) {
        if (hSession != null) {
            if (log == null) {
                log = LOG;
            }

            try {
                hSession.close();
            } catch (HibernateException exception) {
                log.warn("Failed to close HibernateSession", exception);
            }
        }
    }

    /**
     * Convenience method for closing a "counted" Hibernate Session
     * 
     * @param dataSource
     *            the datasource to which the session is bound
     * @param log
     *            a Logger to which to log a warning message if the session
     *            close fails
     */
    public static void closeSession(IHibernateRepository dataSource, Log log) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource cannot be null.");
        }

        if (log == null) {
            log = LOG;
        }

        try {
            dataSource.closeCurrentSession();
        } catch (HibernateException exception) {
            log.warn("Failed to close HibernateSession", exception);
        }
    }

    /**
     * Evicts the selected objects from the session, and clears the list of
     * objects.
     * 
     * @param session
     *            hibernate session object
     * @param objects
     *            list of objects to be evicted.
     */
    public static void evictObjects(Session session, List objects) throws HibernateException {
        if (objects != null && session != null) {
            Iterator it = objects.iterator();
            while (it.hasNext()) {
                session.evict(it.next());
            }
            objects.clear();
        }
    }

    /**
     * Determine if the specified HibernateException occurred as the result of a
     * unique constraint violation
     * 
     * @param exception
     *            the hibernate exception
     * @param log
     *            a log in the context of the calling class
     * @return true if the exception occurred as the result of a unique
     *         constraint violation; false otherwise
     */
    public static boolean isUniqueConstraintViolation(HibernateException exception, Log log2) {
        boolean valueToReturn = false;
        if (exception instanceof JDBCException) {
            String sqlState = ((JDBCException) exception).getSQLState();
            /**
             * FIX ME - When we move to Hibernate 3.0, change to catch
             * ConstraintViolationException and let Hibernate handle the details
             */
            if ((sqlState != null) && (sqlState.startsWith("23"))) {
                /**
                 * It's a pretty safe assumption that this is due to the
                 * name/type constraint. The only other possibility is a
                 * database error
                 */
                valueToReturn = true;
            }
        }

        return valueToReturn;
    }
    
    /**
     * 
     *
     * @param <A> the input type
     * @param <B> the type after convert
     * @param <C> the output type
     */
    public static abstract class SafeQuery3<A,B,C>{
        //the size of the value is limited.
        @SuppressWarnings("unchecked")
        protected List<C> list(Collection<B> values, Session hs) throws HibernateException{
            Query query = hs.createQuery(getQueryString());
            setQuery(query, values);
            return query.list();
        }
        
        protected abstract String getQueryString();
        
        protected abstract void setQuery(Query query, Collection<B> values) throws HibernateException;
        
        protected abstract B convert(A value);
    }
    
    /**
     * 
     *
     * @param <A> the input type
     * @param <C> the output type
     * @see SafeQuery3
     */
    public static abstract class SafeQuery<A, C> extends SafeQuery3<A, A, C> {
        @Override
        protected A convert(A value){
            return value;
        }
    }
    
    /**
     * Database may have a limit that how many parameters the query can have.
     * If the number of the parameters is over the limit, SQLException will occur.
     * If the parameters don't depend on each other, we are trying to do a batch of listing.
     * This can workaround the limit problem. However, the developer needs to make sure 
     *  the query can be batched safely. 
     * 
     * @param <A> the input type
     * @param <B> the type after convert
     * @param <C> the output type
     * @param values
     * @param hs
     * @param limit
     * @param subQuery
     * @return
     * @throws HibernateException
     */
    public static <A, B, C> List<C> safeList(Collection<A> values, Session hs, int limit,
            SafeQuery3<A, B, C> subQuery) throws HibernateException {
        List<C> allOutput = new ArrayList<C>(values.size());
        List<B> partValues = new ArrayList<B>(Math.min(values.size(), limit));
        Iterator<A> iterator = values.iterator();
        while (iterator.hasNext()) {
            partValues.add(subQuery.convert(iterator.next()));
            //flush every DATABASE_PARAMETER_LIMIT
            if (partValues.size() >= limit) {
                allOutput.addAll(subQuery.list(partValues, hs));
                partValues = new ArrayList<B>(limit);
            }
        }
        //flush the remaining
        if (partValues.size() > 0) {
            allOutput.addAll(subQuery.list(partValues, hs));
        }
        return allOutput;
    }

    /**
     * Same as <code>safeList(Collection, Session, int, SafeQuery3)</code> with a database 
     * specific limit already set.
     * 
     * @param <A> the input type
     * @param <B> the type after convert
     * @param <C> the output type
     * @param values
     * @param hs
     * @param subQuery
     * @return
     * @throws HibernateException
     */
    public static <A, B, C> List<C> safeList(Collection<A> values, Session hs,
            SafeQuery3<A, B, C> subQuery) throws HibernateException {
        return safeList(values, hs, DATABASE_PARAMETER_LIMIT, subQuery);
    }
    
    /**
     * Similar to <code>safeList(Collection, Session, SafeQuery3)</code> except there is no middle 
     * conversion required.
     * 
     * @param <A>
     * @param <C>
     * @param values
     * @param hs
     * @param subQuery
     * @return
     * @throws HibernateException
     */
    public static <A, C> List<C> safeList(Collection<A> values, Session hs,
            SafeQuery<A, C> subQuery) throws HibernateException {
        return safeList(values, hs, DATABASE_PARAMETER_LIMIT, subQuery);
    }
}
