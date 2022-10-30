package com.bluejungle.dictionary;

import com.bluejungle.framework.utils.TimeRelation;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnumerationGroupMember.java#1 $
 */

/**
 * Instances of this class represent links between
 * enumerated groups and their group members.
 */
class EnumerationGroupMember {
    /** The primary key of this element. */
    Long id;

    /** The version of this item for the optimistic locking purposes. */
    int version;

    /** An original ID of the group. */
    long fromId;

    /** An original ID of the member of the group. */
    long toId;

    /** This field indicates whether this link is direct or not. */
    boolean isDirect;

    /** An oenrollment ID */
    long enrollmentId;

    /** The active-from and active-to fields for this entity. */
    TimeRelation timeRelation;

    /**
     * Package-private constructor for Hibernate.
     */
    EnumerationGroupMember() {
    }

    EnumerationGroupMember(long fromId, long toId, long enrollmentId, boolean isDirect, TimeRelation timeRelation) {
        if (timeRelation == null) {
            throw new NullPointerException("timeRelation");
        }
        this.fromId = fromId;
        this.toId = toId;
        this.enrollmentId = enrollmentId;
        this.isDirect = isDirect;
        this.timeRelation = timeRelation;
    }

}
