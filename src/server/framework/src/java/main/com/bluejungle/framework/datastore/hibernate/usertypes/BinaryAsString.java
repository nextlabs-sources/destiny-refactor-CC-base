/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/BinaryAsString.java#1 $
 */

package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.ImmutableType;
import net.sf.hibernate.type.Type;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * This class lets you save small binary arrays
 * as base64-encoded strings. 
 */
public class BinaryAsString extends ImmutableType implements UserType {

    private static final long serialVersionUID = 1L;

    /** Represents the SQL type of the field to which we map (String). */
    private static final int[] SQL_TYPES = { Types.VARCHAR };

    /** The resulting Hibernate type. */
    public static final Type TYPE;

    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(BinaryAsString.class);
        } catch (HibernateException exception) {
            typeCreated = null;
        }
        TYPE = typeCreated;
    }

    public Object get( ResultSet rs, String name ) throws HibernateException, SQLException {
        String str = rs.getString(name);
        if ( str != null && !rs.wasNull() ) {
            return fromStringValue(str);
        } else {
            return null;
        }
    }

    public void set( PreparedStatement st, Object value, int index ) throws HibernateException, SQLException {
        st.setString(index, toString(value));
    }

    public int sqlType() {
        return Types.VARCHAR;
    }

    public String toString( Object value ) throws HibernateException {
        return new BASE64Encoder().encode((byte[])value);
    }

    public Object fromStringValue( String str ) throws HibernateException {
        try {
            return new BASE64Decoder().decodeBuffer( str );
        } catch ( IOException e ) {
            return null;
        }
    }

    public Class getReturnedClass() {
        return byte[].class;
    }

    public boolean equals( Object x, Object y ) throws HibernateException {
        byte[] lhs = null, rhs = null;
        if ( x instanceof byte[] ) {
            lhs = (byte[])x;
        }
        if ( y instanceof byte[] ) {
            rhs = (byte[])y;
        }
        if ( lhs == null && rhs == null ) {
            return true;
        }
        if ( lhs == null || rhs == null ) {
            return false;
        }
        if ( lhs.length != rhs.length ) {
            return false;
        }
        for ( int i = 0 ; i != lhs.length ; i++ ) {
            if ( lhs[i] != rhs[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see Type#getName()
     */
    public String getName() {
        return "BinaryAsString";
    }

    /**
     * @see UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * @see UserType#returnedClass()
     */
    public Class returnedClass() {
        return getReturnedClass();
    }

    /**
     * @see UserType#nullSafeGet(ResultSet, String[], Object)
     */
    public Object nullSafeGet( ResultSet rs, String[] names, Object owner ) throws HibernateException, SQLException {
        return get(rs, names[0]);
    }

}
