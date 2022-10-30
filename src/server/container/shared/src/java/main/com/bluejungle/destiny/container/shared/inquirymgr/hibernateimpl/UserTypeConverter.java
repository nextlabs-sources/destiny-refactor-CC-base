/*
 * Created on Apr 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;

import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;
import com.bluejungle.framework.patterns.EnumBase;

/**
 * This utility class allows converting from an enum type into a user type
 * value. It is useful to build HQL condition based on enum types.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/UserTypeConverter.java#1 $
 */

class UserTypeConverter {

    /**
     * Converts the user type into the real database value.
     * 
     * @param enumeration
     *            original enum value
     * @return a database value to query on
     */
    public static String convertUserType(EnumBase enumeration, EnumUserType userType) {
        String result = null;
        SimplePreparedStatement ps = new SimplePreparedStatement();
        try {
            userType.nullSafeSet(ps, enumeration, 0);
            result = ps.getStringValue();
        } catch (HibernateException e) {
            e.printStackTrace(); //can never happen
        } catch (SQLException e) {
            e.printStackTrace(); //can never happen
        }
        return result;
    }

}