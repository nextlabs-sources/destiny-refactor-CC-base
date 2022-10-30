/*
 * Created on Apr 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.type.Type;

/**
 * This is a mock query class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/MockQuery.java#1 $
 */

class MockQuery implements Query {

    ScrollableResults scrollResults;

    /**
     * @see net.sf.hibernate.Query#getQueryString()
     */
    public String getQueryString() {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#getReturnTypes()
     */
    public Type[] getReturnTypes() throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#getNamedParameters()
     */
    public String[] getNamedParameters() throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#iterate()
     */
    public Iterator iterate() throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#scroll()
     */
    public ScrollableResults scroll() throws HibernateException {
        return this.scrollResults;
    }

    /**
     * @see net.sf.hibernate.Query#list()
     */
    public List list() throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#uniqueResult()
     */
    public Object uniqueResult() throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setMaxResults(int)
     */
    public Query setMaxResults(int maxResults) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setFirstResult(int)
     */
    public Query setFirstResult(int firstResult) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCacheable(boolean)
     */
    public Query setCacheable(boolean cacheable) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCacheRegion(java.lang.String)
     */
    public Query setCacheRegion(String cacheRegion) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setForceCacheRefresh(boolean)
     */
    public Query setForceCacheRefresh(boolean forceCacheRefresh) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setTimeout(int)
     */
    public Query setTimeout(int timeout) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setFetchSize(int)
     */
    public Query setFetchSize(int fetchSize) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setLockMode(java.lang.String,
     *      net.sf.hibernate.LockMode)
     */
    public void setLockMode(String alias, LockMode lockMode) {
    }

    /**
     * @see net.sf.hibernate.Query#setParameter(int, java.lang.Object,
     *      net.sf.hibernate.type.Type)
     */
    public Query setParameter(int position, Object val, Type type) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameter(java.lang.String,
     *      java.lang.Object, net.sf.hibernate.type.Type)
     */
    public Query setParameter(String name, Object val, Type type) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameter(int, java.lang.Object)
     */
    public Query setParameter(int position, Object val) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameter(java.lang.String,
     *      java.lang.Object)
     */
    public Query setParameter(String name, Object val) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameterList(java.lang.String,
     *      java.util.Collection, net.sf.hibernate.type.Type)
     */
    public Query setParameterList(String name, Collection vals, Type type) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameterList(java.lang.String,
     *      java.util.Collection)
     */
    public Query setParameterList(String name, Collection vals) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameterList(java.lang.String,
     *      java.lang.Object[], net.sf.hibernate.type.Type)
     */
    public Query setParameterList(String name, Object[] vals, Type type) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setParameterList(java.lang.String,
     *      java.lang.Object[])
     */
    public Query setParameterList(String name, Object[] vals) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setProperties(java.lang.Object)
     */
    public Query setProperties(Object bean) throws HibernateException {
        return null;
    }

    public void setScrollableResults(ScrollableResults newResults) {
        this.scrollResults = newResults;
    }

    /**
     * @see net.sf.hibernate.Query#setString(int, java.lang.String)
     */
    public Query setString(int position, String val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCharacter(int, char)
     */
    public Query setCharacter(int position, char val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBoolean(int, boolean)
     */
    public Query setBoolean(int position, boolean val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setByte(int, byte)
     */
    public Query setByte(int position, byte val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setShort(int, short)
     */
    public Query setShort(int position, short val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setInteger(int, int)
     */
    public Query setInteger(int position, int val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setLong(int, long)
     */
    public Query setLong(int position, long val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setFloat(int, float)
     */
    public Query setFloat(int position, float val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setDouble(int, double)
     */
    public Query setDouble(int position, double val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBinary(int, byte[])
     */
    public Query setBinary(int position, byte[] val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setText(int, java.lang.String)
     */
    public Query setText(int position, String val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setSerializable(int, java.io.Serializable)
     */
    public Query setSerializable(int position, Serializable val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setLocale(int, java.util.Locale)
     */
    public Query setLocale(int position, Locale locale) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBigDecimal(int, java.math.BigDecimal)
     */
    public Query setBigDecimal(int position, BigDecimal number) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setDate(int, java.util.Date)
     */
    public Query setDate(int position, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setTime(int, java.util.Date)
     */
    public Query setTime(int position, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setTimestamp(int, java.util.Date)
     */
    public Query setTimestamp(int position, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCalendar(int, java.util.Calendar)
     */
    public Query setCalendar(int position, Calendar calendar) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCalendarDate(int, java.util.Calendar)
     */
    public Query setCalendarDate(int position, Calendar calendar) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setString(java.lang.String, java.lang.String)
     */
    public Query setString(String name, String val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCharacter(java.lang.String, char)
     */
    public Query setCharacter(String name, char val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBoolean(java.lang.String, boolean)
     */
    public Query setBoolean(String name, boolean val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setByte(java.lang.String, byte)
     */
    public Query setByte(String name, byte val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setShort(java.lang.String, short)
     */
    public Query setShort(String name, short val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setInteger(java.lang.String, int)
     */
    public Query setInteger(String name, int val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setLong(java.lang.String, long)
     */
    public Query setLong(String name, long val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setFloat(java.lang.String, float)
     */
    public Query setFloat(String name, float val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setDouble(java.lang.String, double)
     */
    public Query setDouble(String name, double val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBinary(java.lang.String, byte[])
     */
    public Query setBinary(String name, byte[] val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setText(java.lang.String, java.lang.String)
     */
    public Query setText(String name, String val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setSerializable(java.lang.String,
     *      java.io.Serializable)
     */
    public Query setSerializable(String name, Serializable val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setLocale(java.lang.String, java.util.Locale)
     */
    public Query setLocale(String name, Locale locale) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setBigDecimal(java.lang.String,
     *      java.math.BigDecimal)
     */
    public Query setBigDecimal(String name, BigDecimal number) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setDate(java.lang.String, java.util.Date)
     */
    public Query setDate(String name, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setTime(java.lang.String, java.util.Date)
     */
    public Query setTime(String name, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setTimestamp(java.lang.String,
     *      java.util.Date)
     */
    public Query setTimestamp(String name, Date date) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCalendar(java.lang.String,
     *      java.util.Calendar)
     */
    public Query setCalendar(String name, Calendar calendar) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setCalendarDate(java.lang.String,
     *      java.util.Calendar)
     */
    public Query setCalendarDate(String name, Calendar calendar) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setEntity(int, java.lang.Object)
     */
    public Query setEntity(int position, Object val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setEnum(int, java.lang.Object)
     */
    public Query setEnum(int position, Object val) throws MappingException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setEntity(java.lang.String, java.lang.Object)
     */
    public Query setEntity(String name, Object val) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Query#setEnum(java.lang.String, java.lang.Object)
     */
    public Query setEnum(String name, Object val) throws MappingException {
        return null;
    }

}