/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/UpdateRecord.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;

import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * Implementation of the <code>IUpdateRecord</code> interface.
 * This package-private class is a straightforward data holder.
 */
class UpdateRecord implements IUpdateRecord {
    private static final int SCHEMA_MESSAGE_LENGTH = 1024;

    /** Package-private ID for use by Hibernate. */
    Long id;

    /** Package-private version for optimistic locking. */
    int version;

    /**  The enrollment with which the record is associated. */
    private IEnrollment enrollment;

    /** The start time of the update. */
    private Date startTime;

    /** The end time of the update. */
    private Date endTime;

    /** The error message of the update */
    private String errorMessage; 
    
    /** The next sync time of the update */
    private String nextSyncTime;
    
    /**
     * The time relation of this update record.
     * This package-private field is used only by hibernate.
     */
    TimeRelation timeRelation;

    /** The flag indicating if the update was successful. */
    private boolean successful;

    /**
     * Package-private constructor for Hibernate.
     */
    UpdateRecord() {
    }

    /**
     * This constructor is used by the enrollment session when a commit
     * or a rollback operation is performed.
     * @param enrollment the enrollment with which the record is associated.
     */
    public UpdateRecord(IEnrollment enrollment) {
        this.enrollment = enrollment;
        this.startTime =
        this.endTime = new Date();
        this.timeRelation = null;
        this.successful = false;
        this.errorMessage = null;
        this.nextSyncTime = null;
    }

    /**
     * @see IUpdateRecord#getEnrollment()
     */
    public IEnrollment getEnrollment() {
        return enrollment;
    }

    /**
     * @see IUpdateRecord#getStartTime()
     */
    public Date getStartTime() {
        return UnmodifiableDate.forDate(startTime);
    }

    /**
     * @see IUpdateRecord#getEndTime()
     */
    public Date getEndTime() {
        return UnmodifiableDate.forDate(endTime);
    }

    /**
     * @see IUpdateRecord#isSuccessful()
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * This package-private method is for use by the
     * <code>EnrollmentSession</code>.
     * @param successful the new value of the <code>successful</code> field.
     */
    void setIsSuccessful(boolean successful) {
        this.successful = successful;
    }

    /* (non-Javadoc)
	 * @see com.bluejungle.dictionary.IUpdateRecord#getNextSyncTime()
	 */
	@Override
	public String getNextSyncTime() {
		return this.nextSyncTime;
	}

	/**
	 * @param nextSyncTime the nextSyncTime to set
	 */
	public void setNextSyncTime(String nextSyncTime) {
		this.nextSyncTime = nextSyncTime;
	}

	/**
     * This package-private method is for use by the
     * <code>EnrollmentSession</code>.
     * @param endTime the new value of the <code>endTime</code> field.
     */
    void setEndTime(Date endTime) {
        this.endTime = UnmodifiableDate.forDate(endTime);
    }

    /**
     * @see IUpdateRecord#getErrorMessage()
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * This package-private method is for use by the
     * <code>EnrollmentSession</code>.
     * @param endTime the new value of the <code>errorMessage</code> field.
     */
    void setErrorMessage(String msg) {
        this.errorMessage = msg != null && msg.length() > SCHEMA_MESSAGE_LENGTH 
                ? msg.substring(0, SCHEMA_MESSAGE_LENGTH) 
                : msg;
    }
    
}
