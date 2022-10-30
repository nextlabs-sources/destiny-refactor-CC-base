/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import com.bluejungle.framework.utils.SortDirectionType;

/**
 * This is the user hibernate class for the sort direction type. It allows the
 * isort direction type to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/SortDirectionUserType.java#1 $
 */

public class SortDirectionUserType extends EnumUserType<SortDirectionType> {

    /**
     * Constructor
     */
    public SortDirectionUserType() {
        super(new SortDirectionType[] { 
        			SortDirectionType.ASCENDING, 
        			SortDirectionType.DESCENDING }, 
        		new String[] { "U", "D" }, 
        		SortDirectionType.class);
    }
}