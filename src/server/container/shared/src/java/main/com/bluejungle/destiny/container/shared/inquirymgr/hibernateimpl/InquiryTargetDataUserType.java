/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Types;

import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * This is the user hibernate class for the inquiry target data type. It allows
 * the inquiry target data type to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryTargetDataUserType.java#2 $
 */

public class InquiryTargetDataUserType extends EnumUserType<InquiryTargetDataType> {

    /**
     * Type used to store the target data type
     */
    private static final int[] SQL_TYPES = { Types.CHAR };

    /**
     * Constructor
     */
    public InquiryTargetDataUserType() {
        super(new InquiryTargetDataType[] { 
        			InquiryTargetDataType.POLICY,
        			InquiryTargetDataType.ACTIVITY }, 
				new String[] { "P", "A" },
				InquiryTargetDataType.class);
	}

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}