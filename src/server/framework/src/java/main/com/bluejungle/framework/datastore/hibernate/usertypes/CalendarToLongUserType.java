/*
 * Created on Jan 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.Type;

/**
 * A Hibernate UserType which Serializes and Deserializes a java.util.Calendar
 * to and from the DB as a long. The long value represents the time in
 * milliseconds since January 1, 1970.
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/CalendarToLongUserType.java#1 $:
 */

public class CalendarToLongUserType implements UserType {

    public static final Type TYPE;

    private static final int[] SQL_TYPES = { Types.BIGINT };
    private static final Log LOG = LogFactory.getLog(CalendarToLongUserType.class.getName());
    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(CalendarToLongUserType.class);
        } catch (HibernateException exception) {
            LOG.error("Failed to create CalendarToLongUserType Type", exception);
            typeCreated = null;
        }
        
        TYPE = typeCreated;
    }
    
    public CalendarToLongUserType() {
        super();
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class returnedClass() {
        return Calendar.class;
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
        Calendar valueToReturn = null;

        Long time = (Long) Hibernate.LONG.nullSafeGet(rs, names[0]);

        if (time != null) {
            valueToReturn = new GregorianCalendar();
            valueToReturn.setTimeInMillis(time.longValue());
        }

        return valueToReturn;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        Long valueToSet = null;
        if (value != null) {
            valueToSet = new Long(((Calendar) value).getTimeInMillis());
        }

        Hibernate.LONG.nullSafeSet(st, valueToSet, index);
    }

    public Object deepCopy(Object value) throws HibernateException {
        GregorianCalendar valueToReturn = null;
        if (value != null) {
            // Not sure if Calendar.clone() will be a deep clone.  Doing it the hard way
            valueToReturn = new GregorianCalendar();
            valueToReturn.setTimeInMillis(((Calendar)value).getTimeInMillis());            
        }

        return valueToReturn;
    }

    public boolean isMutable() {
        return true;
    }
}