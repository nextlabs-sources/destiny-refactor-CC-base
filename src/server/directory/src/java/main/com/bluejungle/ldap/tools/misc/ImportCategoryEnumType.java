/*
 * Created on Oct 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.misc;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * Defines categories for import data that are internal to Destiny - i.e.
 * different from user/host subject imports.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/misc/ImportCategoryEnumType.java#1 $
 */

public class ImportCategoryEnumType extends EnumBase {

    /*
     * Delimiter between the namespace and the relative-id. This has to be a
     * character that CANNOT occur anywhere in the namespace or relative id
     * strings. We have chosen '\' since it is can also serve as a visual aid to
     * distinguish between the two.
     */
    public static final String NAMESPACE_COMPONENT_DELIMITER = "\\";

    /**
     * Enum types:
     */
    public static final ImportCategoryEnumType APPLICATIONS = new ImportCategoryEnumType("Destiny" + NAMESPACE_COMPONENT_DELIMITER + "Applications");

    /**
     * Constructor
     * 
     * @param name
     */
    public ImportCategoryEnumType(String name) {
        super(name);
    }
}