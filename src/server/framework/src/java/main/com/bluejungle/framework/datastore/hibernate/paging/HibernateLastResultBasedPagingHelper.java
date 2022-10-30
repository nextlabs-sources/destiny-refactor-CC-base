/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.paging;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import com.bluejungle.framework.domain.IHasId;

/**
 * This is a static utility class to augment a Hibernate Criteria object with
 * the necessary Criteria for performing a paged query using a "last result"
 * algorithm. The algorithm bases the start of the page on the "last result"
 * provided. In this case, "last result" is defined as the result found at the
 * end of the previous page. <br />
 * <br />
 * Note that there was intentionally little time spent designing this class. For
 * Destiny 1.0, paging mechanisms were explored, but it was later discovered
 * that very little paging support was necessary. However, rather than throwing
 * this code away, it was placed in this class for safe keeping in case it was
 * later needed. So, in summary, this class is primarily a holding container for
 * this code. As paging support is increased, this class should be redesigned to
 * provide a move robust paging framework.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/paging/HibernateLastResultBasedPagingHelper.java#1 $
 */
public class HibernateLastResultBasedPagingHelper {

    private static final Log LOG = LogFactory.getLog(HibernateLastResultBasedPagingHelper.class.getName());

    private static final String ID_FIELD = "id";

    private static final Map CACHED_FIELD_METHODS_MAP = new HashMap();

    private static final Object[] EMPTY_OBJECT_ARRAY = null;

    /**
     * Augment the provided Hibernate Criteria object to contain the necessary
     * criteria for achieving last result based paging, including the sorting
     * criteria based on the provided sort property
     * 
     * @param lastResult
     *            The last result from the previous page of results
     * @param fetchSize
     *            The page fetch size
     * @param sortProperty
     *            The property on which to sort results. This value may be null
     * @param queryCriteria
     *            The Hibernate Criteria object to which to add the additional
     *            query criteria
     *  
     */
    // FIX ME - Consider throwing exceptions to client
    public static void addPagingCriteria(IHasId lastResult, int fetchSize, String sortProperty, Criteria queryCriteria) {
        // Add sort
        if (sortProperty != null) {
            queryCriteria.addOrder(Order.asc(sortProperty));
        }

        // To ensure we're accurate if returning a list not at the beginning
        queryCriteria.addOrder(Order.asc(ID_FIELD));

        // If last search term is null, make sure our search starts in the
        // correct location
        if (lastResult != null) {
            if (sortProperty != null) {
                try {
                    Map fieldsToMethodsMap = getFieldToMethodMap(lastResult.getClass());
                    Method beanMethodForSortProperty = (Method) fieldsToMethodsMap.get(sortProperty);
                    if (beanMethodForSortProperty != null) {
                        String lastResultSortFieldValue = (String) beanMethodForSortProperty.invoke(lastResult, EMPTY_OBJECT_ARRAY);
                        Criterion greaterThanSortPropertyExpression = Expression.gt(sortProperty, lastResultSortFieldValue);
                        Criterion equalToSortPropertyExpression = Expression.eq(sortProperty, lastResultSortFieldValue);
                        Criterion greaterThanIDPropertyExpression = Expression.gt(ID_FIELD, lastResult.getId());

                        queryCriteria.add(Expression.or(greaterThanSortPropertyExpression, Expression.and(equalToSortPropertyExpression, greaterThanIDPropertyExpression)));
                    } else {
                        LOG.error("Failed to add sort term to profile query.");
                    }
                } catch (IntrospectionException exception) {
                    LOG.error("Failed to add sort term to profile query.  Last Result introspection failed.", exception);
                } catch (InvocationTargetException exception) {
                    LOG.error("Failed to add sort term to profile query.  Exception thrown by profile bean method.", exception);
                } catch (IllegalAccessException exception) {
                    LOG.error("Failed to add sort term to profile query.  Could not access profile bean method.", exception);
                }
            } else {
                // Add ID field criteria
                queryCriteria.add(Expression.gt(ID_FIELD, lastResult.getId()));
            }
        }

        // Now add fetch size
        if (fetchSize > 0) {
            queryCriteria.setMaxResults(fetchSize);
        }
    }

    /**
     * @param lastResult
     * @return
     * @throws IntrospectionException
     */
    private static Map getFieldToMethodMap(Class objectClass) throws IntrospectionException {
        Map fieldsToMethodsMap = (Map) CACHED_FIELD_METHODS_MAP.get(objectClass);
        if (fieldsToMethodsMap == null) {
            fieldsToMethodsMap = buildFieldToMethodMap(objectClass);
            CACHED_FIELD_METHODS_MAP.put(objectClass, fieldsToMethodsMap);
        }
        return fieldsToMethodsMap;
    }

    /**
     * @param objectClass
     * @throws IntrospectionException
     */
    private static Map buildFieldToMethodMap(Class objectClass) throws IntrospectionException {
        Map mapToReturn = new HashMap();

        BeanInfo classbeanInfo = Introspector.getBeanInfo(objectClass);
        PropertyDescriptor[] propertyDescriptors = classbeanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            mapToReturn.put(propertyDescriptors[i].getName(), propertyDescriptors[i].getReadMethod());
        }

        return mapToReturn;
    }
}