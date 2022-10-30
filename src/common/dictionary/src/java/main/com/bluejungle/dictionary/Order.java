/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/Order.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * <code>IDictionary</code> lets you reorder the data you query.
 * Arrays of objects of type <code>Order</code> are used to specify
 * the desired ordering.
 *
 * This class provides two factory methods for creating an ascending
 * and descending ordering specifications from <code>IElementField</code>
 * objects.
 */
public class Order {

    /** This field represents the attribute on which we sort. */
    private final IElementField field;

    /** This field specifies thether the sort is ascending or descending. */
    private final boolean ascending;

    /**
     * This constructor is private to prevent unexpected instantiations.
     */
    private Order( IElementField field, boolean ascending ) {
        this.field = field;
        this.ascending = ascending;
    }

    /**
     * This factory method lets dictionary users create
     * new order specifications of sascending type.
     * @param field the field on which to sort.
     * @return a new ascending <code>Order</code> object
     * for the specified field. 
     */
    static public Order ascending(IElementField field) {
        return new Order(field, true);
    }

    /**
     * This factory method lets dictionary users create
     * new order specifications of sascending type.
     * @param field the field on which to sort.
     * @return a new ascending <code>Order</code> object
     * for the specified field. 
     */
    static public Order descending(IElementField field) {
        return new Order(field, false);
    }

    /**
     * Accesses the field for this sort.
     * @return the field for this sort.
     */
    public IElementField getField() {
        return field;
    }

    /**
     * Accesses the flag indicating whether the order is
     * ascending or descending.
     * @return true if the order is ascending; false otherwise.
     */
    public boolean isAscending() {
        return ascending;
    }

}
