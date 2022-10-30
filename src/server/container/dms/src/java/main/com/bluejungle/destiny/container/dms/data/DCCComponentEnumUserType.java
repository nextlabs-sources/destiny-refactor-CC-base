/*
 * Created on Feb 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;

/**
 * check the old mapping first, the old mapping is using single character only
 * If can't find it in the old mapping, using "lookupOrCreate" in DCCComponentEnumType
 * 
 * When saving, the OLD_MAPPING is not used.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/data/DCCComponentEnumUserType.java#1 $
 */

public class DCCComponentEnumUserType implements UserType {

    private static final int[] SQL_TYPES = { Types.VARCHAR };
    
    public DCCComponentEnumUserType() {
        super();
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class<DCCComponentEnumType> returnedClass() {
        return DCCComponentEnumType.class;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        boolean valueToReturn = false;
        if (x == y) {
            valueToReturn = true;
        } else if ((x != null) && (y != null)) {
            valueToReturn = x.equals(y);
        }

        return valueToReturn;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        DCCComponentEnumType valueToReturn = null;

        String string = (String) Hibernate.STRING.nullSafeGet(rs, names[0]);

        if (string != null) {
            valueToReturn = DCCComponentEnumType.getServerComponentTypeEnum(string);
        }

        return valueToReturn;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        String valueToSet = null;
        
        if (value != null) {
            valueToSet = ((DCCComponentEnumType)value).getName();
        }
        
        Hibernate.STRING.nullSafeSet(st, valueToSet, index);
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }
    
}
