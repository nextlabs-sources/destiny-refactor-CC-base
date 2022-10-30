/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;

import java.sql.Connection;

/**
 * This interface specifies APIs to obtain Hibernate Session objects
 * corresponding to a given data source (or repository). It also specifies
 * methods to create and persist a session object in order to implement a
 * 'transaction' without having to pass the transaction/session object as a
 * parameter to different method invocations.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IHibernateRepository extends SessionFactory, IConfigurable, ILogEnabled, IDisposable, IInitializable, IManagerEnabled {
    enum DbType {
        POSTGRESQL,
        ORACLE,
        MS_SQL,
    }
    
    /**
     * Returns a thread-specific 'current' session object. Creates one if one
     * doesn't exist. This method is thread-safe, and does not change the
     * "open" counter of the session.
     * 
     * TODO: consider deprecating this method in favor of getCountedSession.
     * 
     * @return 'Current' Session
     */
    Session getCurrentSession() throws HibernateException;

    /**
     * Returns a thread-specific 'current' session object. Creates one if one
     * doesn't exist. This method is thread-safe, and does not change the
     * "open" counter of the session.
     * 
     * TODO: consider deprecating this method in favor of getCountedSession. 
     * 
     * @param interceptor the hibernate interceptor.
     * 
     * @return 'Current' Session
     */
    Session getCurrentSession(Interceptor interceptor) throws HibernateException;

    int getCountedSessionCount();
    
    /**
     * Returns a thread-specific 'current' session object, and increments its use count.
     * Creates a session if it doesn't exist. This method is thread-safe.
     * 
     * @return 'Current' Session
     */
    Session getCountedSession() throws HibernateException;

    /**
     * Returns a thread-specific 'current' session object, and increments its use count.
     * Creates a session if it doesn't exist. This method is thread-safe.
     * 
     * @param interceptor the hibernate interceptor.
     * 
     * @return 'Current' Session
     */
    Session getCountedSession(Interceptor interceptor) throws HibernateException;

    /**
     * Destroys the current thread-local session object. This method is
     * thread-safe.
     */
    void closeCurrentSession() throws HibernateException;

    /**
     * Creates a new session object. Lifecycle of this object is the
     * responsibility of the caller, not the callee. Every time this method is
     * called, a new object will be returned. This method is thread-safe.
     * 
     * @return A new Hibernate Session object
     */
    Session getSession() throws HibernateException;

    /**
     * Creates a new session from the SQL connection. The caller of this
     * function has to close the session once it does not need it anymore.
     * 
     * @param connection
     *            SQL connection (cannot be null)
     * @return a new Hiberante Session object
     */
    Session getSession(Connection connection);
    
    /**
     * get Dialect of this hibernate repository
     * @return dialect
     * @throws IllegalArgumentException if the dialect can't be determined
     */
    Dialect getDialect() throws IllegalArgumentException;
    
    /**
     * 
     * @return
     * @throws IllegalArgumentException if the type can't be determined
     */
    DbType getDatabaseType() throws IllegalArgumentException;
}