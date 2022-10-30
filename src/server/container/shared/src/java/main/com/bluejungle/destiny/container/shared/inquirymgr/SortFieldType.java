/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the enumeration class for the sort field type. The sort field type
 * contains the information about what field can be sorted on.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/SortFieldType.java#1 $
 */

public class SortFieldType extends EnumBase {

    public static final SortFieldType ACTION = new SortFieldType("Action");
    public static final SortFieldType APPLICATION = new SortFieldType("Application");
    public static final SortFieldType COUNT = new SortFieldType("Count");
    public static final SortFieldType DATE = new SortFieldType("Date");
    public static final SortFieldType FROM_RESOURCE = new SortFieldType("FromResource");
    public static final SortFieldType HOST = new SortFieldType("Host");
    public static final SortFieldType NONE = new SortFieldType("None");
    public static final SortFieldType POLICY = new SortFieldType("Policy");
    public static final SortFieldType POLICY_DECISION = new SortFieldType("PolicyDecision");
    public static final SortFieldType TO_RESOURCE = new SortFieldType("ToResource");
    public static final SortFieldType USER = new SortFieldType("User");
    public static final SortFieldType LOGGING_LEVEL = new SortFieldType("LoggingLevel");

    /**
     * The constructor is private to prevent unwanted instanciations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private SortFieldType(String name) {
        super(name);
    }

    /**
     * Retrieve a SortFieldType instance by name
     * 
     * @param name
     *            the name of the SortFieldType
     * @return the SortFieldType associated with the provided name
     * @throws IllegalArgumentException
     *             if no SortFieldType exists with the specified name
     */
    public static SortFieldType getSortFieldType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        return getElement(name, SortFieldType.class);
    }
}