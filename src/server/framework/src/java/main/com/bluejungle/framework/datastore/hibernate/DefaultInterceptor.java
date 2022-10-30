package com.bluejungle.framework.datastore.hibernate;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/DefaultInterceptor.java#1 $
 */

import java.io.Serializable;
import java.util.Iterator;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.type.Type;

/**
 * Represents an empty Hibernate interceptor.
 * Extend this class if you would like to override some of the methods
 * of the interceptor without having to deal with the rest of them.
 * All methods of this class are empty, returning a value to Hibernate
 * that indicates that the interceptor has done no work.
 */
public class DefaultInterceptor implements Interceptor {
    /**
     * @see net.sf.hibernate.Interceptor#onDelete(Object, Serializable id, Object[], String[], Type[])
     */
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
    }

    /**
     * @see net.sf.hibernate.Interceptor#onFlushDirty(Object, Serializable, Object[], Object[], String[], Type[])
     */
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        return false;
    }

    /**
     * @see net.sf.hibernate.Interceptor#onLoad(Object, Serializable, Object[], String[], Type[])
     */
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        return false;
    }

    /**
     * @see net.sf.hibernate.Interceptor#onSave(Object, Serializable, Object[], String[], Type[])
     */
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        return false;
    }

    /**
     * @see net.sf.hibernate.Interceptor#postFlush(Iterator)
     */
    public void postFlush(Iterator entities) throws CallbackException {
    }

    /**
     * @see net.sf.hibernate.Interceptor#preFlush(Iterator)
     */
    public void preFlush(Iterator entities) throws CallbackException {
    }

    /**
     * @see net.sf.hibernate.Interceptor#isUnsaved(java.lang.Object)
     */
    public Boolean isUnsaved(Object entity) {
        return null;
    }

    /**
     * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, java.io.Serializable)
     */
    public Object instantiate(Class clazz, Serializable id) throws CallbackException {
        return null;
    }

    /**
     * @see net.sf.hibernate.Interceptor#findDirty(Object, Serializable, Object[], Object[], String[], Type[])
     */
    public int[] findDirty(
        Object entity,
        Serializable id,
        Object[] currentState,
        Object[] previousState,
        String[] propertyNames,
        Type[] types) {
        return null;
    }

}
