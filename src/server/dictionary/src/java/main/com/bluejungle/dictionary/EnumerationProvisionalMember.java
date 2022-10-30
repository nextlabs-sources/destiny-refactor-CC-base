package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by
 * Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnumerationProvisionalMember.java#1 $
 */

/**
 * Instances of this persistent class represent
 * provisional references inserted into enumerated groups. 
 */
class EnumerationProvisionalMember {
    /** The primary key of this element. */
    Long id;

    /** The version of this item for the optimistic locking purposes. */
    int version;

    /** An original ID of the group. */
    long groupId;

    /** An enrollment ID */
    long enrollmentId;

    /** The referenced <code>DictionaryPath</code>. */
    DictionaryPath path;

    /**
     * This is a package-private constructor for Hibernate.
     *
     */
    EnumerationProvisionalMember() {
    }

    /**
     * Makes a new <code>EnumerationProvisionalMember</code>
     * with the specified attributes.
     * @param groupId the group ID.
     * @param enrollmentId the enrollment id.
     * @param path the referenced path.
     */
    public EnumerationProvisionalMember(
        long groupId
    ,   long enrollmentId
    ,   DictionaryPath path
    ) {
        this.groupId = groupId;
        this.enrollmentId = enrollmentId;
        this.path = path;
    }

    /**
     * Accesses the primary key of this provisional member record.
     * @return the primary key of this provisional member record.
     */
    public Long getId() {
        return id;
    }

    /**
     * Accesses the path of this provisional member record.
     * @return the path of this provisional member record.
     */
    public DictionaryPath getPath() {
        return path;
    }

}
