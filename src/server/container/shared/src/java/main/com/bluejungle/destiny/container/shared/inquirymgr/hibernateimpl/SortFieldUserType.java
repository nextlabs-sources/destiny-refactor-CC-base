/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.SortFieldType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * This is the user hibernate class for the sort field type. It allows the isort
 * sort field type to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/SortFieldUserType.java#1 $
 */

public class SortFieldUserType extends EnumUserType<SortFieldType> {

    /**
     * Constructor
     */
    public SortFieldUserType() {
        super(new SortFieldType[] { 
	        		SortFieldType.COUNT, 
	        		SortFieldType.DATE,
					SortFieldType.FROM_RESOURCE, 
					SortFieldType.HOST, 
					SortFieldType.NONE,
					SortFieldType.POLICY, 
					SortFieldType.TO_RESOURCE, 
					SortFieldType.USER,
					SortFieldType.LOGGING_LEVEL }, 
				new String[] { "Co", "Da", "Fr", "Ho", "No", "Po", "To", "Us", "LL" }, 
				SortFieldType.class);
    }
}