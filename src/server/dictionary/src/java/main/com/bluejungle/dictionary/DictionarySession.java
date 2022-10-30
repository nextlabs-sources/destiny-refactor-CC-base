package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/DictionarySession.java#1 $
 */

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an abstract base class for the dictionary sessions.
 */
abstract class DictionarySession {
    private static final Log LOG = LogFactory.getLog(DictionarySession.class.toString());

    private final Session session;

    private Transaction transaction;

    protected final Dictionary dictionary;
    
    DictionarySession(Dictionary dictionary, Session session) {
        checkNull( dictionary, "dictionary" );
        checkNull( session, "session" );
        if ( !session.isOpen() ) {
            throw new IllegalArgumentException("session is closed");
        }
        this.session = session;
        this.dictionary = dictionary;
    }

    public void beginTransaction() throws DictionaryException {
        try {
            if ( transaction == null ) {
                transaction = session.beginTransaction();
            }
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        }
    }

    public void commit() throws DictionaryException {
        checkActiveTransaction();
        try {
            transaction.commit();
        } catch ( HibernateException cause ) {
            try {
                transaction.rollback();
            } catch ( HibernateException ignored ) {
            }
            throw new DictionaryException(cause);
        } finally {
            transaction = null;
        }
    }

    public void rollback() throws DictionaryException {
        // An attempt to commit may clear out the active transaction,
        // so rollback is allowed to proceed
        // even when there is no active transaction.
        if ( transaction == null ) {
            return;
        }
        try {
            transaction.rollback();
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        } finally {
            transaction = null;
        }
    }

    public boolean hasActiveTransaction() {
        return transaction != null;
    }

    public void close() throws DictionaryException {
        if (!session.isOpen()) {
            throw new IllegalStateException("session is already closed");
        }
        try {
            if ( transaction != null ) {
                if ( !transaction.wasCommitted() && !transaction.wasRolledBack() ) {
                    rollback();
                } else {
                    transaction = null;
                }
            }
        } catch ( HibernateException cause ) {
            throw new DictionaryException(cause);
        } finally {
            dictionary.closeCurrentSession();
        }
    }

    protected final void checkActiveTransaction() {
        if ( transaction == null ) {
            throw new IllegalStateException("expected an active transaction");
        }
    }

    protected final void checkNull( Object obj, String message ) {
        if ( obj == null ) {
            throw new NullPointerException(message);
        }
    }

    protected final Session getSession() {
        return session;
    }

}
