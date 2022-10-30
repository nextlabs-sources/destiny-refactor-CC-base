/*
 * Created on Apr 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.usertypes;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

/**
 * This user types decomposes a regular Calendar object into multiple columns to
 * extract more detailed date information. This allows Hibernate queries to
 * group by components of the date (Month, week, day, etc.). The Calendar value
 * is decomposed to a Long (time in milliseconds), a month number (1 to 12), and
 * a day number (1 to 31).
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/TimeDetailsUserType.java#1 $
 */

public class TimeDetailsUserType implements CompositeUserType {

    //Property names are time in milliseconds, month #, and day #
    private static final String[] PROP_NAMES = { "time", "monthNb", "dayNb" };
    private static final Type[] PROP_TYPES = { Hibernate.LONG, Hibernate.LONG, Hibernate.LONG };

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyNames()
     */
    public String[] getPropertyNames() {
        return PROP_NAMES;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyTypes()
     */
    public Type[] getPropertyTypes() {
        return PROP_TYPES;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyValue(java.lang.Object,
     *      int)
     */
    public Object getPropertyValue(Object obj, int property) throws HibernateException {
        Calendar calendar = (Calendar) obj;
        Long result = null;
        switch (property) {
        case 0:
            result = new Long(calendar.getTimeInMillis());
            break;
        case 1:
            result = new Long(trimToMonth(calendar).getTimeInMillis());
            Date date = new Date(result.longValue());
            break;
        case 2:
            result = new Long(trimToDay(calendar).getTimeInMillis());
            date = new Date(result.longValue());
            break;
        default:
            throw new HibernateException("Cannot retrieve property: " + property);
        }

        return result;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#setPropertyValue(java.lang.Object,
     *      int, java.lang.Object)
     */
    public void setPropertyValue(Object component, int property, Object value) throws HibernateException {
        Calendar calendar = (Calendar) component;
        switch (property) {
        case 0:
            calendar.setTimeInMillis(((Long) value).longValue());
            break;
        case 1:
        case 2:
            //Nothing to do here
            break;
        default:
            throw new HibernateException("Unable to set property: " + property);
        }
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#returnedClass()
     */
    public Class returnedClass() {
        return Calendar.class;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#equals(java.lang.Object,
     *      java.lang.Object)
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        return (x == y);
    }

    /**
     * When the value is received, the month number and day numbers do not
     * matter. Only the calendar value gets returned from the time in
     * milliseconds.
     * 
     * @see net.sf.hibernate.CompositeUserType#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], net.sf.hibernate.engine.SessionImplementor,
     *      java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        Long timeInMillis = (Long) Hibernate.LONG.nullSafeGet(rs, names[0]);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis.longValue());
        return calendar;
    }

    /**
     * When the value is set, the calendar value is expanded in time in
     * milliseconds, month number and day number
     * 
     * @see net.sf.hibernate.CompositeUserType#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int, net.sf.hibernate.engine.SessionImplementor)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        Calendar calendar = (Calendar) value;
        Long timeInMillis = null;
        Long month = null;
        Long day = null;
        if (calendar != null) {
            timeInMillis = new Long(calendar.getTimeInMillis());
            month = new Long(((Calendar) trimToMonth(calendar)).getTimeInMillis());
            day = new Long(((Calendar) trimToDay(calendar)).getTimeInMillis());
        }

        Hibernate.LONG.nullSafeSet(st, timeInMillis, index);
        Hibernate.LONG.nullSafeSet(st, month, index + 1);
        Hibernate.LONG.nullSafeSet(st, day, index + 2);
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
        /*
         * Calendar existingCal = (Calendar) value; Calendar copyCal =
         * Calendar.getInstance();
         * copyCal.setTimeInMillis(existingCal.getTimeInMillis()); return
         * copyCal;
         */
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#disassemble(java.lang.Object,
     *      net.sf.hibernate.engine.SessionImplementor)
     */
    public Serializable disassemble(Object value, SessionImplementor session) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#assemble(java.io.Serializable,
     *      net.sf.hibernate.engine.SessionImplementor, java.lang.Object)
     */
    public Object assemble(Serializable cached, SessionImplementor session, Object owner) throws HibernateException {
        return (Serializable) deepCopy(cached);
    }

    /**
     * Returns a calendar containing a date at the beginning of the day. For
     * example, April 15th 2005 at 3 PM becomes April 15th 2005.
     * 
     * @param fullCalendar
     *            calendar with the complete date
     * @return a calendar which values below the day unit have been taken out.
     */
    protected Calendar trimToDay(final Calendar fullCalendar) {
        Calendar result = trimToMonth(fullCalendar);
        result.set(Calendar.DAY_OF_MONTH, fullCalendar.get(Calendar.DAY_OF_MONTH));
        return result;
    }

    /**
     * Returns a calendar containing a date at the beginning of the month. For
     * example, April 15th 2005 at 3 PM becomes April 1st 2005.
     * 
     * @param fullCalendar
     *            calendar with the complete date
     * @return a calendar which values below the month unit have been taken out.
     */
    protected Calendar trimToMonth(final Calendar fullCalendar) {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(0);
        result.set(Calendar.YEAR, fullCalendar.get(Calendar.YEAR));
        result.set(Calendar.MONTH, fullCalendar.get(Calendar.MONTH));
        result.set(Calendar.DAY_OF_MONTH, 1);
        result.set(Calendar.HOUR_OF_DAY, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.SECOND, 0);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }
}