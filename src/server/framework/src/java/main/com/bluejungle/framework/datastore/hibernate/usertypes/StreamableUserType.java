package com.bluejungle.framework.datastore.hibernate.usertypes;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/StreamableUserType.java#1 $
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.IStreamable;

/**
 * A Hibernate UserType which moves a <code>IStreamable</code>
 * to and from the DB as a BLOB. Unline the built-in Hibernate types
 * of <code>serializable</code> and <code>binary</code>, this type
 * deals with very large objects.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/StreamableUserType.java#1 $:
 */
public class StreamableUserType implements UserType {

    public static final Type TYPE;
    
    /** Represents the SQL type of the field to which we map (BLOB). */
    private static final int[] SQL_TYPES = { Types.BLOB };

    private static final Log LOG = LogFactory.getLog(DateToLongUserType.class.getName());

    static {
        Type typeCreated = null;
        try {
            typeCreated = Hibernate.custom(DateToLongUserType.class);
        } catch (HibernateException exception) {
            LOG.error("Failed to create LargeImmutableBinaryUserType Type", exception);
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
    public Class<?> returnedClass() {
        return IStreamable.class;
    }

    /**
     * Compares for identity (compating BLOBs for real is too expensive).
     */
    public boolean equals(Object x, Object y) {
        return x==y;
    }

    /**
     * Reads the object from the database.
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        InputStream rawIn = rs.getBinaryStream(names[0]);

        if (rawIn == null) {
            return null;
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(rawIn);
            String className = in.readUTF();
            if (className == null || className.length() == 0) {
                return null;
            }
            Class<?> resClass = Class.forName(className);
            IStreamable res = (IStreamable)resClass.newInstance();
            res.readExternal(in);
            return res;
        } catch (IOException ioe) {
            throw new HibernateException(ioe);
        } catch (ClassNotFoundException cne) {
            throw new HibernateException(cne);
        } catch (InstantiationException ine) {
            throw new HibernateException(ine);
        } catch (IllegalAccessException iae) {
            throw new HibernateException(iae);
        } catch (ClassCastException cce) {
            throw new HibernateException(cce);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                // Ignored
            }
        }
    }

    /**
     * Stores the object in the database.
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value != null) {
            try {
                IStreamable streamable = (IStreamable)value;
                st.setBinaryStream(index, streamable.getStream(), streamable.getSize());
            } catch (ClassCastException cce) {
                throw new HibernateException("Storing incompatible class type", cce);
            }
        } else {
            st.setBinaryStream(index, null, 0);
        }
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
