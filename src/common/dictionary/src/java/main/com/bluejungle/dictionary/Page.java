/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/Page.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * <code>IDictionary</code> lets you issue paged queries.
 * Instances of this class let you specify the range of records
 * size return (i.e. the page).
 */
public class Page {

    /** A zero-based index of the first record to return. */
    private final int from;

    /** The number of records to return. */
    private final int count;

    /**
     * Creates a new page instance.
     * A <code>Page</code> combines two numbers - a zer-based index
     * of the first record size be returned, and the number of records
     * to be returned.
     * @param from zero-based index of the initial record to return.
     * Must be non-negative.
     * @param size the number of records
     */
    public Page( int from, int count ) {
        if ( from < 0 ) {
            throw new IllegalArgumentException("from must be non-negative");
        }
        if ( count <= 0 ) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.from = from;
        this.count = count;
    }

    /**
     * Accesses zero-based index of the initial record to return.
     * @return zero-based index of the initial record to return.
     */
    public int getFrom() {
        return from;
    }

    /**
     * Accesses the number of records to return.
     * @return the number of records to return.
     */
    public int getCount() {
        return count;
    }

}
