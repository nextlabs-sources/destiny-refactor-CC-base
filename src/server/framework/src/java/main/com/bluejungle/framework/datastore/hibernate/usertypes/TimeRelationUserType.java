package com.bluejungle.framework.datastore.hibernate.usertypes;

/*
 * All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author Sergey Kalinichenko
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/TimeRelationUserType.java#1 $
 */

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.utils.TimeRelation;

public class TimeRelationUserType implements CompositeUserType {

    private static final int[] SQL_TYPES = { Types.BIGINT, Types.BIGINT};
    private static final String[] PROPERTY_NAMES = { "activeFrom", "activeTo" };

    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    private static final Type[] PROPERTY_TYPES = { DateToLongUserType.TYPE, DateToLongUserType.TYPE};

    public Type[] getPropertyTypes() {
        return PROPERTY_TYPES;
    }

    public Class returnedClass() {
        return TimeRelation.class;
    }

    public boolean equals(Object lhs, Object rhs) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        return lhs.equals(rhs);
    }

    public Object deepCopy(Object obj) {
        return obj; // immutable
    }

    public boolean isMutable() {
        return false;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor se, Object owner) throws SQLException {
        Date from = null;
        long fromLong = rs.getLong(names[0]);
        if (!rs.wasNull()) {
            from = new Date(fromLong);
        }

        Date to = null;
        long toLong = rs.getLong(names[1]);
        if (!rs.wasNull()) {
            to = new Date(toLong);
        }

        return new TimeRelation(from, to);
    }

    public void nullSafeSet(PreparedStatement ps, Object val, int ind, SessionImplementor se) throws SQLException {
        TimeRelation tr = (TimeRelation) val;
        if (tr != null) {
            Date fromDt = tr.getActiveFrom();
            if (fromDt != null) {
                ps.setLong(ind, fromDt.getTime());
            } else {
                ps.setNull(ind, SQL_TYPES[0]);
            }
            Date toDt = tr.getActiveTo();
            if (toDt != null) {
                ps.setLong(ind + 1, toDt.getTime());
            } else {
                ps.setNull(ind + 1, SQL_TYPES[1]);
            }
        } else {
            ps.setNull(ind, SQL_TYPES[0]);
            ps.setNull(ind + 1, SQL_TYPES[1]);
        }
    }

    public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
        return cached;
    }

    public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
        return (Serializable) value;
    }

    public Object getPropertyValue(Object obj, int ind) {
        if (obj == null || !(obj instanceof TimeRelation)) {
            return null;
        }
        TimeRelation tr = (TimeRelation) obj;
        return (ind == 0) ? tr.getActiveFrom() : tr.getActiveTo();
    }

    public void setPropertyValue(Object obj, int ind, Object val) {
        throw new UnsupportedOperationException("TimeRelation is immutable.");
    }
}