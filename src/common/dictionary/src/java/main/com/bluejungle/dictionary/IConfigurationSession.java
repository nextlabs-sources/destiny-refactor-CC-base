package com.bluejungle.dictionary;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IConfigurationSession.java#1 $
 */

/**
 * This interface defines the contract for sessions
 * used for updating dictionaries. 
 *
 * @author sergey
 */

public interface IConfigurationSession {

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
     * Saves or updates the specified type.
     *
     * @param type The type to save or update.
     */
    void saveType( IMElementType type ) throws DictionaryException;

    /**
     * Deletes the specified type.
     *
     * @param type the type to be deleted.
     */
    void deleteType( IMElementType type ) throws DictionaryException;

    /**
     * Saves or updates the specified enrollment.
     *
     * @param enrollment The enrollment to save or update.
     */
    void saveEnrollment( IEnrollment enrollment ) throws DictionaryException;

    /**
     * Deletes the specified enrollment.
     *
     * @param enrollment the enrollment to be deleted.
     */
    void deleteEnrollment( IEnrollment enrollment ) throws DictionaryException;
    
    /**
     * Purge the enrollment history before a specified date. 
     * Any other activity should be stopped manually.
     * This method should start on the other thread. If the process is taking too long, 
     *   the user can interrupt by change the value of <code>isInterrupted</code> to false.
     * However if the input of <code>isInterrupted</code> is null, the interruption is ignored.
     *  
     * @param enrollment
     * @param clearBeforeDate
     * @param isInterrupted
     * @return # of deleted items
     * @throws DictionaryException
     * @throws IllegalStateException if enrollment is never synced 
     * @throws IllegalArgumentException if the data is after the enrollment latest time.
     */
    Map<String, Integer> purgeHistory(IEnrollment enrollment, Date clearBeforeDate, AtomicBoolean isInterrupted)
            throws DictionaryException, IllegalStateException, IllegalArgumentException;
    
    /**
     * 
     * @param enrollment
     */
    Map<String, Integer> rollbackLatestFailedEnrollment(IEnrollment enrollment) throws DictionaryException;

    /**
     * This method closes the session and deallocates
     * the resources associated with it.
     */
    void close() throws DictionaryException;

}
