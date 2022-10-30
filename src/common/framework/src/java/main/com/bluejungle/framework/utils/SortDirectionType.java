/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.framework.utils;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is an enum for the sort direction. This enum exposes the two direction
 * that a sort can have (up and down!).
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/utils/SortDirection.java#1 $
 */

public class SortDirectionType extends EnumBase {

    public static final SortDirectionType ASCENDING = new SortDirectionType("Ascending");
    public static final SortDirectionType DESCENDING = new SortDirectionType("Descending");

    /**
     * Constructor
     * 
     * @param name
     *            direction of the sort
     */
    private SortDirectionType(String name) {
        super(name);
    }
    
    /**
     * Retrieve a SortDirectionType instance by name
     * 
     * @param name
     *            the name of the SortDirectionType
     * @return the SortDirectionType associated with the provided name
     * @throws IllegalArgumentException
     *             if no SortDirectionType exists with the specified name
     */
    public static SortDirectionType getSortDirectionType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        return getElement(name, SortDirectionType.class);
    }
}