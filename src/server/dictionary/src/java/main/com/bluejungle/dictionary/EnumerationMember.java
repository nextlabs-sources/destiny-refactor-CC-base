package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnumerationMember.java#1 $
 */

import com.bluejungle.framework.utils.TimeRelation;

/**
 * Instances of this class represent links between enumerated groups
 * and their leaf members.
 *
 * Objects of this class are used only to insert the data into the database -
 * they are neither accessed programmatically nor retrieved from the database,
 * except to close their TimeRelation.
 */
class EnumerationMember {
    /** The primary key of this element. */
    Long id;

    /** The version of this item for the optimistic locking purposes. */
    int version;

    /** An original ID of the group. */
    long groupId;

    /** An original ID of the member of the group. */
    long memberId;

    /** An enrollment ID */
    long enrollmentId;

    /**
     * This fiels stores the type of the element. Although this value
     * can be obtained through a join, storing it lets us optimize
     * queries restricting groups by element type.
     *
     * Once set, this field is never accessed through the code.
     * It is marked package-private to avoid Java warnings
     * about private fields that are never accessed from the class. 
     */
    Long elementTypeId;

    /** The active-from and active-to fields for this entity. */
    TimeRelation timeRelation;

    /**
     * Package-private constructor for Hibernate.
     */
    EnumerationMember() {
    }

    /**
     * Creates a new <code>EnumerationMamber</code> with the specific
     * source and destination groups, and other parameters.
     *
     * @param groupId the <code>ID</code> of the group.
     * @param memberId the <code>ID</code> of the destination group.
     * @param elementTypeId the type of the element or null if the element is a group.
     * @param timeRelation a time relation for this link.
     */
    public EnumerationMember(long groupId, long memberId, long enrollmentId, Long elementTypeId, TimeRelation timeRelation) {
        if (elementTypeId == null) {
            throw new NullPointerException("elementTypeId");
        }
        if (timeRelation == null) {
            throw new NullPointerException("timeRelation");
        }
        this.groupId = groupId;
        this.memberId = memberId;
        this.enrollmentId = enrollmentId;
        this.elementTypeId = elementTypeId;
        this.timeRelation = timeRelation;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementTypeId == null) ? 0 : elementTypeId.hashCode());
		result = prime * result + (int) (enrollmentId ^ (enrollmentId >>> 32));
		result = prime * result + (int) (groupId ^ (groupId >>> 32));
		result = prime * result + (int) (memberId ^ (memberId >>> 32));
		result = prime * result + ((timeRelation == null) ? 0 : timeRelation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnumerationMember other = (EnumerationMember) obj;
		if (elementTypeId == null) {
			if (other.elementTypeId != null)
				return false;
		} else if (!elementTypeId.equals(other.elementTypeId))
			return false;
		if (enrollmentId != other.enrollmentId)
			return false;
		if (groupId != other.groupId)
			return false;
		if (memberId != other.memberId)
			return false;
		if (timeRelation == null) {
			if (other.timeRelation != null)
				return false;
		} else if (!timeRelation.equals(other.timeRelation))
			return false;
		return true;
	}
}
