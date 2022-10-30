/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IUpdateRecord.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;

/**
 * This interface establishes the contract for obtaining
 * information about the changes to enrollments.
 */
public interface IUpdateRecord {

    /**
     * Obtains the <code>IEnrollment</code> with which this record
     * is associated.
     * @return the <code>IEnrollment</code> with which this record
     * is associated.
     */
    IEnrollment getEnrollment();

    /**
     * Obtains the time when the update has been started.
     * @return the time when the update has been started.
     */
    Date getStartTime();

    /**
     * Obtains the time when the update has been finished
     * or abandones.
     * @return the time when the update has been finished
     * or abandoned.
     */
    Date getEndTime();

    /**
     * Obtains the status of this update (true if success, false otherwise).
     * @return the status of this update (true if success, false otherwise).
     */
    boolean isSuccessful();
    
    /**
     * obtains the error message of this update
     * @return String of error message
     */
    String getErrorMessage();
    
    /**
     * obtains the next Scheduled Sync Time of this update
     * @return String of nextSyncTime.
     */
    String getNextSyncTime();

}
