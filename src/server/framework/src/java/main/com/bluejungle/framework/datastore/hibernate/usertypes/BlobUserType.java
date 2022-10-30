/*
 * Created on Sep 8, 2011
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2011 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.engine.Mapping;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.lob.BlobImpl;
import net.sf.hibernate.type.AbstractType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/BlobUserType.java#1 $
 */

public class BlobUserType extends AbstractType {

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public Object fromString(String xml) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnSpan(Mapping mapping) throws MappingException {
        return 1;
    }

    @Override
    public String getName() {
        return "blob";
    }

    @Override
    public Class<?> getReturnedClass() {
        return Blob.class;
    }

    @Override
    public boolean hasNiceEquals() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        return nullSafeGet(rs, names[0], session, owner);
    }
    
    private static volatile Field field = null;

    @Override
    public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        
        boolean isPostgre = false;
        if (rs instanceof org.postgresql.core.BaseResultSet) {
            isPostgre = true;
        } else if (rs instanceof com.mchange.v2.c3p0.impl.NewProxyResultSet) {
            if (field == null) {
                synchronized (this.getClass()) {
                    try {
                        field = com.mchange.v2.c3p0.impl.NewProxyResultSet.class.getDeclaredField("inner");
                    } catch (SecurityException e) {
                        throw new HibernateException(e);
                    } catch (NoSuchFieldException e) {
                        throw new HibernateException(e);
                    }
                    field.setAccessible(true);
                }
            }
            ResultSet inner;
            try {
                inner = (ResultSet)field.get(rs);
            } catch (IllegalArgumentException e) {
                throw new HibernateException(e);
            } catch (IllegalAccessException e) {
                throw new HibernateException(e);
            }
            if (inner instanceof org.postgresql.core.BaseResultSet) {
                isPostgre = true;
            }
        }
        
        Object value;
        if(isPostgre) {
            try {
                InputStream is = rs.getBinaryStream(name);
                if (!rs.wasNull() && is != null) {
                    value = Hibernate.createBlob(is);
                } else {
                    value = null;
                }
            } catch (IOException e) {
                throw new HibernateException(e);
            }
        } else {
            value = rs.getBlob(name);
            if (rs.wasNull()) {
                value = null;
            }
        }
        return value;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
            SessionImplementor session) throws HibernateException, SQLException {
        if (value instanceof BlobImpl) {
            BlobImpl blob = (BlobImpl) value;
            st.setBinaryStream( index, blob.getBinaryStream(), (int) blob.length() );
        }
        else {
            if (value != null) {
                st.setBlob(index, (Blob) value);
            } else {
                st.setBytes(index, null);
            }
        }
    }

    @Override
    public int[] sqlTypes(Mapping mapping) throws MappingException {
        return new int[] {  Types.BLOB };
    }

    @Override
    public String toString(Object value, SessionFactoryImplementor factory)
            throws HibernateException {
        return String.valueOf(value);
    }
   
}
