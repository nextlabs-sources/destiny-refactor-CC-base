/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IEnrollmentSession.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Collection;

/**
 * This interface establishes the contract for updating elements
 * and groups associated with a particular enrollment.
 */
public interface IEnrollmentSession {

    /**
     * Begins a transaction if it hasn't already been started.
     * If a transaction is already in progress, nothing happens.
     */
    void beginTransaction() throws DictionaryException;

    /**
     * Commits the current transaction. If there is no active
     * transaction at this time, an exception is thrown.
     */
    void commit() throws DictionaryException;

    /**
     * Rolls back the current transaction. If there is no active
     * transaction at this time, an exception is thrown.
     */
    void rollback() throws DictionaryException;

    /**
     * Checks if there is an active transaction at this time.
     *
     * @return true if there is an active transaction, false otherwise.
     */
    boolean hasActiveTransaction();

    /**
     * Saves or updates each element of the specified
     * <code>Collection</code> of elements and/or groups. 
     * @param elements The elements that need to be updated.
     */
    void saveElements(Collection<? extends IElementBase> elements) throws DictionaryException;

   /**
    * Deletes each element of the specified <code>Collection</code>
    * of elements and/or groups. 
    * @param elements The elements that need to be deleted.
    */
    void deleteElements(Collection<? extends IElementBase> elements) throws DictionaryException;

    /**
     * This method closes the session and deallocates
     * the resources associated with it.
     * @param success indicates whether the enrollment succeeded
     * or not (<code>true</code> means that the enrollment session
     * completed successfully; <code>false</code> means that it failed).
     */
    void close(boolean success, String errorMessage) throws DictionaryException;

}
