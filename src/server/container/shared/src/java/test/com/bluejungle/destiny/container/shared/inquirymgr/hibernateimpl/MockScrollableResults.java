/*
 * Created on Apr 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.type.Type;

/**
 * This is a dummy scrollable result class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/MockScrollableResults.java#1 $
 */

class MockScrollableResults implements ScrollableResults {

    private boolean beforeFirst = false;
    private boolean first = false;
    private boolean last = false;
    private boolean next = false;
    Object[] nextGet = null;

    /**
     * Sets the next "next" response;
     * 
     * @param newNext
     *            response to set
     */
    public void setNext(boolean newNext) {
        this.next = newNext;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#next()
     */
    public boolean next() throws HibernateException {
        return this.next;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#previous()
     */
    public boolean previous() throws HibernateException {
        return false;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#scroll(int)
     */
    public boolean scroll(int i) throws HibernateException {
        return false;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#last()
     */
    public boolean last() throws HibernateException {
        return this.last;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#first()
     */
    public boolean first() throws HibernateException {
        return this.first;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#beforeFirst()
     */
    public void beforeFirst() throws HibernateException {
        this.beforeFirst = true;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#afterLast()
     */
    public void afterLast() throws HibernateException {
    }

    /**
     * Returns true if beforeFirst was called
     * 
     * @return true if beforeFirst was called
     */
    public boolean isBeforeFirst() {
        return this.beforeFirst;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#isFirst()
     */
    public boolean isFirst() throws HibernateException {
        return false;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#isLast()
     */
    public boolean isLast() throws HibernateException {
        return false;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#close()
     */
    public void close() throws HibernateException {
    }

    /**
     * Sets the result for the next first operation
     * 
     * @param newFirst
     *            result for the next first operation
     */
    public void setFirst(boolean newFirst) {
        this.first = newFirst;
    }

    /**
     * Sets the result for the next last operation
     * 
     * @param newLast
     *            result for the next last operation
     */
    public void setLast(boolean newLast) {
        this.last = newLast;
    }

    /**
     * Sets the next thing that should be returned on get
     * 
     * @param nextGet
     *            next object to return
     */
    public void setNextGet(Object[] nextGet) {
        this.nextGet = nextGet;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#get()
     */
    public Object[] get() throws HibernateException {
        return this.nextGet;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#get(int)
     */
    public Object get(int i) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getType(int)
     */
    public Type getType(int i) {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getInteger(int)
     */
    public Integer getInteger(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getLong(int)
     */
    public Long getLong(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getFloat(int)
     */
    public Float getFloat(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getBoolean(int)
     */
    public Boolean getBoolean(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getDouble(int)
     */
    public Double getDouble(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getShort(int)
     */
    public Short getShort(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getByte(int)
     */
    public Byte getByte(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getCharacter(int)
     */
    public Character getCharacter(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getBinary(int)
     */
    public byte[] getBinary(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getText(int)
     */
    public String getText(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getBlob(int)
     */
    public Blob getBlob(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getClob(int)
     */
    public Clob getClob(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getString(int)
     */
    public String getString(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getBigDecimal(int)
     */
    public BigDecimal getBigDecimal(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getDate(int)
     */
    public Date getDate(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getLocale(int)
     */
    public Locale getLocale(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getCalendar(int)
     */
    public Calendar getCalendar(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getTimeZone(int)
     */
    public TimeZone getTimeZone(int col) throws HibernateException {
        return null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#getRowNumber()
     */
    public int getRowNumber() throws HibernateException {
        return 0;
    }

    /**
     * Resets mock class
     */
    public void reset() {
        this.beforeFirst = false;
        this.first = false;
        this.last = false;
        this.next = false;
        this.nextGet = null;
    }

    /**
     * @see net.sf.hibernate.ScrollableResults#setRowNumber(int)
     */
    public boolean setRowNumber(int rowNumber) throws HibernateException {
        return false;
    }

}