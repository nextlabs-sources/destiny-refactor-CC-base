/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/DictionaryIterator.java#1 $
 */

package com.bluejungle.dictionary;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * This class provides factory methods for constructing
 * <code>IElementIterator</code> and <code>IGroupIterator</code>
 * instances.
 */
class DictionaryIterator<E> implements IDictionaryIterator<E> {
    /**
     * This is Hibernate's scrollable result set
     * underlying this iterator.
     */
    private final ScrollableResults rs;

    /**
     * This is Hibernate's session that produced the
     * scrollable result set encapsulated by this object.
     */
    protected final Session session;

    /**
     * This is the transaction in the scope of which we are iterating.
     */
    private final Transaction transaction;

    /**
     * This is a flag telling whether or not the scrollable
     * result set has more data. Initially it is set
     * in the constructor. After that, the next() method sets
     * the value used in subsequent calls to hasNext(). */
    private boolean hasNext;

    /** This is a reference to the dictionary that created this iterator. */
    protected final Dictionary dictionary;

    /**
     * This constructor sets the initial values
     * to the internal fields of this iterator to the values
     * passed as parameters.
     * @param rs the <code>ScrollableResultSet</code> underlying
     * this iterator.
     * @param session the <code>Session</code> that produced
     * this iterator.
     * @param transaction the <code>Transaction</code> in the scope of which
     * we are iterating.
     * @throws HibernateException if the supplied result set
     * is not valid.
     */
    public DictionaryIterator(
        ScrollableResults rs
    ,   Session session
    ,   Transaction transaction
    ,   Dictionary dictionay
    ) throws HibernateException {
        this.rs = rs;
        this.session = session;
        this.dictionary = dictionay;
        this.hasNext = rs.next();
        this.transaction = transaction;
    }

    /**
     * Advances the iterator and returns the current object.
     * @return the current object.
     * @throws DictionaryException if the operation cannot complete.
     */
    @SuppressWarnings("unchecked")
    public E next() throws DictionaryException {
        return (E) internalNext();
    }
    
    protected Object internalNext() throws DictionaryException {
        if ( !hasNext ) {
            throw new IllegalStateException(
                "Attempt to advance the iterator past the end."
            );
        }
        try {
            Object[] data = rs.get();
            Object res;
            if (data.length == 1) {
                res = data[0];
                session.evict(res);
            } else {
                res = data;
            }
            hasNext = rs.next();
            return res;
        } catch ( HibernateException cause ) {
            throw new DictionaryException("Unable to retrieve the next value from an iterator", cause);
        }
    }
    
    /**
     * This method implements the close method of the
     * interfaces implemented by the subclasses. 
     */
    public void close() throws DictionaryException {
        if(!session.isOpen()) {
            throw new IllegalStateException("session is already closed");
        }
        HibernateException lastException = null;
        try {
            if (!transaction.wasCommitted() && !transaction.wasRolledBack()) {
                transaction.rollback();
            }
        } catch (HibernateException he) {
            lastException = he;
        }
        try {
            rs.close();
        } catch (HibernateException he ) {
            lastException = he;
        } finally {
            dictionary.closeCurrentSession();
            if ( lastException != null ) {
                throw new DictionaryException(lastException);
            }
        }
    }
    /**
     * This method implements the hasNext method of the
     * interfaces implemented by the subclasses. 
     */
    public boolean hasNext() throws DictionaryException {
        return hasNext;
    }
    
    public static IDictionaryIterator<ElementFieldData> forFields(
        IElementField[] fields
    ,   ScrollableResults rs
    ,   Session session
    ,   Transaction transaction
    ,   Dictionary dictionary) throws HibernateException {
        return new FieldIterator(fields, rs, session, transaction, dictionary);
    }

    private static class FieldIterator extends DictionaryIterator<ElementFieldData> {
        private final IElementField[] fields;
        private final int idIndex;
        private final int typeIndex;
        private final int uniqueNameIndex;
        /**
         * @see DictionaryIterator#DictionaryIterator(ScrollableResults, Session)
         */
        public FieldIterator(
            IElementField[] fields
        ,   ScrollableResults rs
        ,   Session session
        ,   Transaction transaction
        ,   Dictionary dictionary) throws HibernateException {
            super( rs, session, transaction, dictionary );
            if (fields == null) {
                throw new NullPointerException("fields");
            }
            this.fields = fields;
            this.idIndex = fields.length;
            this.typeIndex = fields.length+1;
            this.uniqueNameIndex = fields.length+2;
        }

        public ElementFieldData next() throws DictionaryException {
            Object[] raw = (Object[])internalNext();
            IElementType type = (IElementType)raw[typeIndex];
            if (type == null) {
                throw new IllegalStateException("Query returned a null element type.");
            }
            Object[] data = new Object[fields.length];
            for (int i = 0 ; i != fields.length ; i++) {
                if (type.equals(((ElementField)fields[i]).getParentType())) {
                    data[i] = fields[i].getType().convertFromBase(raw[i]);
                }
            }
            return new ElementFieldData(
                (Long)raw[idIndex]
            ,   type
            ,   (String)raw[uniqueNameIndex]
            ,   fields
            ,   data
            );
        }
        
    }
}
