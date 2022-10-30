package com.bluejungle.framework.datastore.hibernate.usertypes;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/EnumUserType.java#1 $
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class provides support for retrieving enums from
 * Hibernate and persisting them back to hibernate.
 *  
 * Derive from this class to build a user type for your
 * specific enumeration.
 * 
 * This class assumes mapping to a fixed-length character field.
 * The size of the field must equal the name of the longest code
 * specified in the constructor, or the lenght of the longest name
 * if the codes are not specified explicitly.
 */
public abstract class EnumUserType<T extends EnumBase> implements UserType {

    private static int[] SQL_TYPES = { Types.VARCHAR };

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    private final Class<T> returnedClass;
    private final Map<String, T> code2type;
    private final Map<T, String> type2code;

    protected EnumUserType( T[] types, String[] codes, Class<T> clazz ) {
        if ( types == null ) {
            throw new NullPointerException("types");
        }
        if ( codes == null ) {
            throw new NullPointerException("codes");
        }
        if ( codes.length != types.length ) {
            throw new IllegalArgumentException("codes.length != types.length");
        }
        if ( clazz == null ) {
            throw new NullPointerException("clazz");
        }

        code2type = new HashMap<String, T>(3*types.length/2);
        type2code = new HashMap<T, String>(3*types.length/2);

        for ( int i = 0 ; i != types.length ; i++ ) {
            if ( types[i] == null ) {
                throw new NullPointerException("types["+i+"]");
            }
            if ( !clazz.isInstance( types[i] ) ) {
                throw new IllegalArgumentException("types["+i+"] is of the wrong class");
            }
            if ( codes[i] == null ) {
                throw new NullPointerException("codes["+i+"]");
            }
            code2type.put( codes[i], types[i] );
            type2code.put( types[i], codes[i] );
        }

        returnedClass = clazz;
    }

    protected EnumUserType( T[] types, Class<T> clazz ) {
        this( types, typesToCodeNames(types), clazz );
    }

    public Class<T> returnedClass() {
        return returnedClass;
    }

    public void addEnum(T type, String code, Class<T> clazz) {
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        if ( code == null ) {
            throw new NullPointerException("code");
        }
        if ( clazz == null ) {
            throw new NullPointerException("clazz");
        }
        if ( clazz != returnedClass ) {
            throw new IllegalArgumentException("clazz does not match");
        }
        if (!clazz.isInstance(type)) {
            throw new IllegalArgumentException("type is of the wrong class");
        }
        code2type.put(code, type);
        type2code.put(type, code);
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        // Identities of enum objects are unique -
        // it is OK to compare the references:
        return x == y;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
        throws HibernateException, SQLException {

        Object code = Hibernate.STRING.nullSafeGet( rs, names[0] );

        if (code == null) {
            return null;
        }

        T type = code2type.get(code);

        if (type == null) {
            return getDefaultValue();
        }

        return type;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index)
        throws HibernateException, SQLException {

        Hibernate.STRING.nullSafeSet(
            st, (value != null) ? (String)type2code.get( value ) : null, index
        );
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }
    
    public String getCodeByType( T value ) {
        return (String)type2code.get( value );
    }
    
    public T getTypeByCode( String code ) {
        return code2type.get( code );
    }

    public T getDefaultValue() {
        return null;
    }

    private static String[] typesToCodeNames( EnumBase[] types ) {
        if ( types == null ) {
            throw new NullPointerException("types");
        }
        String[] res = new String[types.length];
        for ( int i = 0 ; i != res.length ; i++ ) {
            if ( types[i] == null ) {
                throw new NullPointerException("types["+i+"]");
            }
            res[i] = types[i].getName();
        }
        return res;
    }
}
