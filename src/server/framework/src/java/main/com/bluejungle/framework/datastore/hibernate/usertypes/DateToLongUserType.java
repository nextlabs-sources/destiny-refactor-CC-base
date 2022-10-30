package com.bluejungle.framework.datastore.hibernate.usertypes;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/DateToLongUserType.java#1 $
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * A Hibernate UserType which Serializes and Deserializes a java.util.Date
 * to and from the DB as a long. The long value represents the time in
 * milliseconds since January 1, 1970.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/DateToLongUserType.java#1 $:
 */

public class DateToLongUserType implements UserType {
    public static final Type TYPE;
    
    /** Represents the SQL type of the field to which we map (long). */
    private static final int[] SQL_TYPES = { Types.BIGINT };
    private static final Log LOG = LogFactory.getLog(DateToLongUserType.class.getName());
    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(DateToLongUserType.class);
        } catch (HibernateException exception) {
            LOG.error("Failed to create DateToLongUserType Type", exception);
            typeCreated = null;
        }
        
        TYPE = typeCreated;
    }
    
    /**
     * Returns the list of field types to which this user type maps. 
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Returns the type of the object that this user type returns.
     */
    public Class returnedClass() {
        return Date.class;
    }

    /**
     * Returns true when x==y, false otherwise.
     */
    public boolean equals(Object x, Object y) {
        if (x == y) {
            return true;
        } else if ( x != null && y != null ) {
            return x.equals( y );
        } else {
            return false;
        }
    }

    /**
     * Reads the object from the database.
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Long time = (Long) Hibernate.LONG.nullSafeGet(rs, names[0]);
        return (time != null) ? UnmodifiableDate.forTime( time.longValue() ) : null;
    }

    /**
     * Stores the object in the database.
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        Long valueToSet = null;
        if ( value != null ) {
            valueToSet = new Long( ( (Date)value ).getTime() );
        }
        Hibernate.LONG.nullSafeSet(st, valueToSet, index);
    }

    /**
     * Deep copy of the immutable object is the object itself.
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value; // It is immutable
    }

    /**
     * Returns false because the returned value is not mutable.
     */
    public boolean isMutable() {
        return false;
    }

}
