/*
 * Created on Jan 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;

/**
 * A Hibernate UserType which Serializes and Deserializes a java.net.URI to and
 * from the DB as a String
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/framework/datastore/hibernate/usertypes/URIToStringUserType.java#1 $
 */

public class URIToStringUserType implements UserType {

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    public URIToStringUserType() {
        super();
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Class returnedClass() {
        return URI.class;
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
        URI valueToReturn = null;

        String URIString = (String) Hibernate.STRING.nullSafeGet(rs, names[0]);

        if (URIString != null) {
            try {
                valueToReturn = new URI(URIString);
            } catch (URISyntaxException exception) {
                // Shouldn't happen
                throw new HibernateException("Failed to deep clone URI", exception);
            }
        }

        return valueToReturn;
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        String valueToSet = null;
        
        if (value != null) {
            valueToSet = value.toString();
        }
        
        Hibernate.STRING.nullSafeSet(st, valueToSet, index);
    }

    public Object deepCopy(Object value) throws HibernateException {
        Object valueToReturn = null;
        if (value != null) {
            try {
                valueToReturn = new URI(value.toString());
            } catch (URISyntaxException exception) {
                // Shouldn't happen
                throw new HibernateException("Failed to deep clone URI", exception);
            }
        }

        return valueToReturn;
    }

    public boolean isMutable() {
        return true;
    }

}