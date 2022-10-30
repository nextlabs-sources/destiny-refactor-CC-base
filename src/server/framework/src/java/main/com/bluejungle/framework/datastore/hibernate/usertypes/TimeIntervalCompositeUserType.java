/*
 * Created on Feb 4, 2005
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

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

import com.bluejungle.framework.utils.TimeInterval;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/usertypes/TimeIntervalCompositeUserType.java#1 $
 */

public class TimeIntervalCompositeUserType implements CompositeUserType {

    private static final Type[] PROPERTY_TYPES = { Hibernate.INTEGER, Hibernate.STRING };
    private static final String[] PROPERTY_NAMES = { "time", "timeUnit" };

    /**
     * Constructor
     *  
     */
    public TimeIntervalCompositeUserType() {
        super();
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyNames()
     */
    public String[] getPropertyNames() {
        return PROPERTY_NAMES;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyTypes()
     */
    public Type[] getPropertyTypes() {
        return PROPERTY_TYPES;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#getPropertyValue(java.lang.Object,
     *      int)
     */
    public Object getPropertyValue(Object timeInterval, int propertyIndex) throws HibernateException {
        TimeInterval theTimeInterval = (TimeInterval) timeInterval;
        Object objectToReturn = null;
        switch (propertyIndex) {
        case 0:
            objectToReturn = new Integer(theTimeInterval.getTime());
            break;
        case 1:
            objectToReturn = theTimeInterval.getTimeUnit().getName();
            break;
        default:
            throw new HibernateException("Invalid Property Index: " + propertyIndex);
        }
        
        return objectToReturn;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#setPropertyValue(java.lang.Object,
     *      int, java.lang.Object)
     */
    public void setPropertyValue(Object timeInterval, int propertyIndex, Object propertyValue) throws HibernateException {
        TimeInterval theTimeInterval = (TimeInterval) timeInterval;        
        switch (propertyIndex) {
        case 0:
            theTimeInterval.setTime(((Integer)propertyValue).intValue());
            break;
        case 1:
            theTimeInterval.setTimeUnit(TimeInterval.TimeUnit.getTimeUnit((String)propertyValue));            
            break;
        default:
            throw new HibernateException("Invalid Property Index: " + propertyIndex);
        }
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#returnedClass()
     */
    public Class returnedClass() {
        return TimeInterval.class;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#equals(java.lang.Object,
     *      java.lang.Object)
     */
    public boolean equals(Object timeIntervalOne, Object timeIntervalTwo) throws HibernateException {
        boolean valueToReturn = false;
        
        if (timeIntervalOne == timeIntervalTwo) {
            valueToReturn = true;
        } else if ((timeIntervalOne != null) && (timeIntervalTwo != null)) {
            valueToReturn = timeIntervalOne.equals(timeIntervalTwo);
        }
        
        return valueToReturn;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], net.sf.hibernate.engine.SessionImplementor,
     *      java.lang.Object)
     */
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        Integer time = (Integer)Hibernate.INTEGER.nullSafeGet(resultSet, names[0]);
        String timeUnitName = (String)Hibernate.STRING.nullSafeGet(resultSet, names[1]);
        
        TimeInterval valueToReturn = null;
        if ((time != null) && (timeUnitName != null)) {
            valueToReturn = new TimeInterval(time.intValue(), TimeInterval.TimeUnit.getTimeUnit(timeUnitName));
        }
        
        return valueToReturn;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int, net.sf.hibernate.engine.SessionImplementor)
     */
    public void nullSafeSet(PreparedStatement preparedStatement, Object timeInterval, int startColumnIndex, SessionImplementor arg3) throws HibernateException, SQLException {
        TimeInterval theTimeInterval = (TimeInterval) timeInterval;
        Integer timeToSet = null;
        String timeUnitNameToSet = null;
        if (theTimeInterval != null) {
            timeToSet = new Integer(theTimeInterval.getTime());
            timeUnitNameToSet = new String(theTimeInterval.getTimeUnit().getName());
        }
                
        Hibernate.INTEGER.nullSafeSet(preparedStatement, timeToSet, startColumnIndex);
        Hibernate.STRING.nullSafeSet(preparedStatement, timeUnitNameToSet, startColumnIndex+1);
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object timeInterval) throws HibernateException {
        TimeInterval theTimeInterval = (TimeInterval) timeInterval;
        TimeInterval timeIntervalToReturn = null;
        if (theTimeInterval != null) {
            timeIntervalToReturn = new TimeInterval(theTimeInterval.getTime(), theTimeInterval.getTimeUnit());
        }
        
        return timeIntervalToReturn;
    }

    /**
     * @see net.sf.hibernate.CompositeUserType#isMutable()
     */
    public boolean isMutable() {
        return true;
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
        return deepCopy(cached);        
    }

}