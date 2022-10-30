/*
 * Created on Apr 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.Iterator;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HQLConstants;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;

/**
 * This is the test class for the query object in the query builder.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/QueryTest.java#1 $
 */

public class QueryTest extends DACContainerSharedTestCase {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * This test verifies the basics for the Query class.
     */
    public void testQueryBasics() {
        QueryImpl query = new QueryImpl();
        assertTrue(query instanceof IQuery);
        assertNotNull("Query element members should not be null", query.getConditionElements());
        assertEquals("Query element members should be empty", 0, query.getConditionElements().size());
        assertNotNull("Query element members should not be null", query.getDoClassNames());
        assertEquals("Query element members should be empty", 0, query.getDoClassNames().size());
        assertNotNull("Query element members should not be null", query.getGroupingElements());
        assertEquals("Query element members should be empty", 0, query.getGroupingElements().size());
        assertNotNull("Query element members should not be null", query.getNamedParameters());
        assertEquals("Query element members should be empty", 0, query.getNamedParameters().size());
        assertNotNull("Query element members should not be null", query.getOrderByElements());
        assertEquals("Query element members should be empty", 0, query.getOrderByElements().size());
        assertNotNull("Query element members should not be null", query.getSelectElements());
        assertEquals("Query element members should be empty", 0, query.getSelectElements().size());
    }

    /**
     * This test verifies that the query copy is performed properly
     */
    public void testQueryCopy() {
        QueryImpl originalQuery = new QueryImpl();
        IQueryElement element = new QueryElementImpl();
        final String originalDOVar = "foo";
        final String originalDOClass = "bar";
        final String originalField = "field";
        final String originalFunction = "func";
        SelectElementImpl originalSelect = new SelectElementImpl();
        originalSelect.setDOClassName(originalDOClass);
        originalSelect.setDOVarName(originalDOVar);
        originalSelect.setFieldName(originalField);
        originalSelect.setFunction(originalFunction);
        element.getSelects().add(originalSelect);

        final String originalGroupBy = "group";
        GroupingElementImpl originalGroup = new GroupingElementImpl(originalGroupBy);
        element.getGroupings().add(originalGroup);

        final String originalOrderBy = "myorderby";
        final boolean originalAsc = true;
        OrderByElementImpl originalOrder = new OrderByElementImpl(originalAsc, originalOrderBy);
        element.getOrderBys().add(originalOrder);

        final String originalCond = "cond";
        ConditionElementImpl originalCondition = new ConditionElementImpl(originalCond);
        final String originalCond2 = "cond2";
        ConditionElementImpl originalCondition2 = new ConditionElementImpl(originalCond2);
        element.getConditions().add(originalCondition);
        element.getConditions().add(originalCondition2);

        final String originalParam1 = "p1";
        final String originalValue1 = "v1";
        final String originalParam2 = "p2";
        final String originalValue2 = "v2";
        originalQuery.getNamedParameters().put(originalParam1, originalValue1);
        originalQuery.getNamedParameters().put(originalParam2, originalValue2);

        originalQuery.addQueryElement(element);

        //Performs the query and test that the copy is indentical
        final IQuery newObj = originalQuery.copy();
        assertTrue(newObj instanceof QueryImpl);
        final QueryImpl newQuery = (QueryImpl) newObj;

        assertNotNull("Query should be copied properly", newQuery.getConditionElements());
        assertEquals("Query should be copied properly", 2, newQuery.getConditionElements().size());
        Iterator it = newQuery.getConditionElements().iterator();
        IConditionElement elem = (IConditionElement) it.next();
        assertEquals(originalCond, elem.getExpression());
        elem = (IConditionElement) it.next();
        assertEquals(originalCond2, elem.getExpression());

        assertNotNull("Query should be copied properly", newQuery.getSelectElements());
        assertEquals("Query should be copied properly", 1, newQuery.getSelectElements().size());
        it = newQuery.getSelectElements().iterator();
        ISelectElement select = (ISelectElement) it.next();
        assertEquals("Query should be copied properly", originalDOClass, select.getDOClassName());
        assertEquals("Query should be copied properly", originalDOVar, select.getDOVarName());
        assertEquals("Query should be copied properly", originalField, select.getFieldName());
        assertEquals("Query should be copied properly", originalFunction, select.getFunction());

        assertNotNull("Query should be copied properly", newQuery.getGroupingElements());
        assertEquals("Query should be copied properly", 1, newQuery.getGroupingElements().size());
        it = newQuery.getGroupingElements().iterator();
        IGroupingElement group = (IGroupingElement) it.next();
        assertEquals("Query should be copied properly", originalGroupBy, group.getExpression());

        assertNotNull("Query should be copied properly", newQuery.getOrderByElements());
        assertEquals("Query should be copied properly", 1, newQuery.getOrderByElements().size());
        it = newQuery.getOrderByElements().iterator();
        IOrderByElement order = (IOrderByElement) it.next();
        assertEquals("Query should be copied properly", originalOrderBy, order.getExpression());
        assertEquals("Query should be copied properly", originalAsc, order.isAcending());

        assertNotNull("Query should be copied properly", newQuery.getNamedParameters());
        assertEquals("Query should be copied properly", 2, newQuery.getNamedParameters().size());
        assertEquals("Query should be copied properly", originalValue1, newQuery.getNamedParameters().get(originalParam1));
        assertEquals("Query should be copied properly", originalValue2, newQuery.getNamedParameters().get(originalParam2));
    }

    /**
     * This test verifies that when adding a new query element, the various
     * pieces of the query are stored in the appropriate member variables within
     * the query object.
     */
    public void testQueryQueryElementAddition() {
        QueryImpl query = new QueryImpl();
        //Checks that the API can support null element
        query.addQueryElement(null);
        assertEquals("By default, no elements should be stored within the query object", 0, query.getConditionElements().size());
        assertEquals("By default, no elements should be stored within the query object", 0, query.getGroupingElements().size());
        assertEquals("By default, no elements should be stored within the query object", 0, query.getOrderByElements().size());
        assertEquals("By default, no elements should be stored within the query object", 0, query.getSelectElements().size());
        assertEquals("By default, no elements should be stored within the query object", 0, query.getNamedParameters().size());

        QueryElementImpl queryElement = new QueryElementImpl();
        ConditionElementImpl condition = new ConditionElementImpl("myCondition");
        GroupingElementImpl group = new GroupingElementImpl("myGroup");
        OrderByElementImpl order = new OrderByElementImpl(true, "myOrder");
        SelectElementImpl select = new SelectElementImpl();
        queryElement.getConditions().add(condition);
        queryElement.getGroupings().add(group);
        queryElement.getOrderBys().add(order);
        queryElement.getSelects().add(select);
        final String paramName = "param1";
        final String valueName = "value1";
        queryElement.getNamedParameters().put(paramName, valueName);
        query.addQueryElement(queryElement);
        assertEquals("Elements should be stored within the query object", 1, query.getConditionElements().size());
        assertEquals("Elements should be stored within the query object", 1, query.getGroupingElements().size());
        assertEquals("Elements should be stored within the query object", 1, query.getOrderByElements().size());
        assertEquals("Elements should be stored within the query object", 1, query.getSelectElements().size());
        assertEquals("Elements should be stored within the query object", 1, query.getNamedParameters().size());

        //Check that the right elements are put in the right place
        Iterator it = query.getConditionElements().iterator();
        assertEquals("Condition elements should be stored appropriately", condition, it.next());
        it = query.getGroupingElements().iterator();
        assertEquals("Group elements should be stored appropriately", group, it.next());
        it = query.getOrderByElements().iterator();
        assertEquals("Order elements should be stored appropriately", order, it.next());
        it = query.getSelectElements().iterator();
        assertEquals("Select elements should be stored appropriately", select, it.next());
        assertEquals(valueName, query.getNamedParameters().get(paramName));
    }

    /**
     * This test verifies that the code checks for invalid variable names based
     * on query elements that have already been submitted.
     */
    public void testQueryIllegalDataObjectVarName() {
        //Create a select element for a particular DO
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        final String doClassName = "myDO";
        final String doVarName = "myDOVar";
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        queryElement.getSelects().add(select);
        query.addQueryElement(queryElement);

        //Now, add a query element with a bad variable name
        QueryElementImpl badQueryElement = new QueryElementImpl();
        SelectElementImpl badSelect = new SelectElementImpl();
        final String badDOVarName = "myDOVar2";
        badSelect.setDOClassName(doClassName);
        badSelect.setDOVarName(badDOVarName);
        badQueryElement.getSelects().add(badSelect);
        boolean exThrown = false;
        try {
            query.addQueryElement(badQueryElement);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("Query should reject elements with bad data object variable name", exThrown);
        assertEquals("When query rejects a bad element, no new element should be stored in the query", 1, query.getSelectElements().size());

        //Now, add a query element with a good variable name
        QueryElementImpl correctedQueryElement = new QueryElementImpl();
        SelectElementImpl correctedSelect = new SelectElementImpl();
        correctedSelect.setDOClassName(doClassName);
        correctedSelect.setDOVarName(doVarName);
        correctedQueryElement.getSelects().add(correctedSelect);
        exThrown = false;
        try {
            query.addQueryElement(correctedQueryElement);
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertFalse("Query should accept elements with correct data object variable name", exThrown);
        assertEquals("When query accepts a valid element, valid elements should be stored in the query", 2, query.getSelectElements().size());
    }

    /**
     * This test verifies that a data object is already used in the query, its
     * HQL variable name is available for other query elements to reuse.
     */
    public void testQueryFindVarName() {
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        SelectElementImpl select2 = new SelectElementImpl();
        final String doClassName = "myDO";
        final String doVarName = "myDOVar";
        final String doClassName2 = "myDO2";
        final String doVarName2 = "myDOVar";
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        select2.setDOClassName(doClassName2);
        select2.setDOVarName(doVarName2);
        queryElement.getSelects().add(select);
        queryElement.getSelects().add(select2);
        query.addQueryElement(queryElement);
        assertEquals("The query object should return the correct variable name for a data object", doVarName, query.findVarName(doClassName));
        assertEquals("The query object should return the correct variable name for a data object", doVarName2, query.findVarName(doClassName2));
    }

    /**
     * This test verifies that the query element generated valid HQL for a
     * 'group by' specification.
     */
    public void testQueryBuildsCorrectGroupByHQLExpression() {
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        final String doClassName = "doClass";
        final String doVarName = "doVar";
        final String fieldName = "field";
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        select.setFieldName(fieldName);
        queryElement.getSelects().add(select);

        //Sets one 'group by'
        final String groupByExpr = "groupByExpr";
        GroupingElementImpl groupBy1 = new GroupingElementImpl(groupByExpr);
        queryElement.getGroupings().add(groupBy1);
        query.addQueryElement(queryElement);
        String hql = query.getHQLQueryString();
        String expectedResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + doVarName;
        expectedResult += HQLConstants.SPACE + HQLConstants.GROUPBY + HQLConstants.SPACE + groupByExpr;
        assertEquals("HQL groupby should be properly generated with one condition", expectedResult, hql);

        //Sets a second 'group by'
        final String groupByExpr2 = "groupByExpr";
        GroupingElementImpl groupBy2 = new GroupingElementImpl(groupByExpr2);
        QueryElementImpl queryElement2 = new QueryElementImpl();
        queryElement2.getGroupings().add(groupBy2);
        query.addQueryElement(queryElement2);
        hql = query.getHQLQueryString();
        expectedResult += HQLConstants.COMMA + HQLConstants.SPACE + groupByExpr2;
        assertEquals("HQL 'group by' should be properly generated with multiple group by", expectedResult, hql);
    }

    /**
     * This test verifies that the query element generated valid HQL for an
     * 'order by' specification.
     */
    public void testQueryBuildsCorrectOrderByHQLExpression() {
        final String doClassName = "doClass";
        final String doVarName = "doVar";
        final String fieldName = "field";
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        select.setFieldName(fieldName);
        queryElement.getSelects().add(select);
        query.addQueryElement(queryElement);

        //Adds one 'order by' element
        QueryElementImpl queryElement1 = new QueryElementImpl();
        final String orderExpr = "myOrderExpr";
        OrderByElementImpl order = new OrderByElementImpl(true, orderExpr);
        queryElement1.getOrderBys().add(order);
        query.addQueryElement(queryElement1);
        String expectedResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + doVarName;
        expectedResult += HQLConstants.SPACE + HQLConstants.ORDERBY + HQLConstants.SPACE + orderExpr + HQLConstants.SPACE + HQLConstants.ASC;
        String hql = query.getHQLQueryString();
        assertEquals("Generated HQL 'order by' statement should be valid with one 'order by'", expectedResult, hql);

        //Adds a second 'order by' element
        QueryElementImpl queryElement2 = new QueryElementImpl();
        final String orderExpr2 = "myOrderExpr2";
        OrderByElementImpl order2 = new OrderByElementImpl(false, orderExpr2);
        queryElement2.getOrderBys().add(order2);
        query.addQueryElement(queryElement2);
        expectedResult += HQLConstants.COMMA + HQLConstants.SPACE + orderExpr2 + HQLConstants.SPACE + HQLConstants.DESC;
        hql = query.getHQLQueryString();
        assertEquals("Generated HQL 'order by' statement should be valid with one 'order by'", expectedResult, hql);
    }

    /**
     * This test verifies that the Query element generates valid HQL select
     * expression once all the query elements have been entered.
     */
    public void testQueryBuildsCorrectSelectHQLExpression() {
        //Verifies that HQL gets generated only if there is at least a select
        // element
        QueryImpl emptyQuery = new QueryImpl();
        QueryElementImpl orderByOnlyQueryElem = new QueryElementImpl();
        OrderByElementImpl order = new OrderByElementImpl(true, "whatever");
        orderByOnlyQueryElem.getOrderBys().add(order);
        emptyQuery.addQueryElement(orderByOnlyQueryElem);
        assertEquals("HQL query should not be generated if there are no select statements in the query", "", emptyQuery.getHQLQueryString());

        //Creates a simple SELECT statement
        final String doClassName = "doClass";
        final String doVarName = "doVar";
        final String fieldName = "field";
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        select.setFieldName(fieldName);
        queryElement.getSelects().add(select);
        query.addQueryElement(queryElement);
        final String expectedSimpleResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + doVarName;
        String hql = query.getHQLQueryString();
        assertEquals("Generated HQL select statement should be valid with one variable", expectedSimpleResult, hql);

        //Creates a select statement with a second item to select
        QueryElementImpl queryElement2 = new QueryElementImpl();
        SelectElementImpl select2 = new SelectElementImpl();
        final String fieldName2 = "field2";
        select2.setDOClassName(doClassName);
        select2.setDOVarName(doVarName);
        select2.setFieldName(fieldName2);
        queryElement2.getSelects().add(select2);
        query.addQueryElement(queryElement2);
        hql = query.getHQLQueryString();
        final String expectedSecondResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.COMMA + HQLConstants.SPACE + doVarName + "." + fieldName2 + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE
                + doClassName + HQLConstants.SPACE + doVarName;
        assertEquals("Generated HQL select statement should be valid with multiple variables", expectedSecondResult, hql);

        //Adds a third select statement with a count function
        QueryElementImpl queryElement3 = new QueryElementImpl();
        SelectElementImpl select3 = new SelectElementImpl();
        final String fieldName3 = "field3";
        final String function3 = "count";
        select3.setDOClassName(doClassName);
        select3.setDOVarName(doVarName);
        select3.setFieldName(fieldName3);
        select3.setFunction(function3);
        queryElement3.getSelects().add(select3);
        query.addQueryElement(queryElement3);
        hql = query.getHQLQueryString();
        String expectedThirdResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.COMMA + HQLConstants.SPACE + doVarName + "." + fieldName2;
        expectedThirdResult += HQLConstants.COMMA + HQLConstants.SPACE + "count(" + doVarName + "." + fieldName3 + ")" + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + doVarName;
        assertEquals("Generated HQL select statement should be valid with function", expectedThirdResult, hql);
    }

    /**
     * This test verifies that the WHERE condition generated properly
     */
    public void testQueryBuildsCorrectWhereHQLExpression() {
        QueryImpl query = new QueryImpl();
        QueryElementImpl queryElement = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        final String doClassName = "doClass";
        final String doVarName = "doVar";
        final String fieldName = "field";
        select.setDOClassName(doClassName);
        select.setDOVarName(doVarName);
        select.setFieldName(fieldName);
        queryElement.getSelects().add(select);

        //Sets one condition
        final String conditionExpr1 = "myConditionExpression";
        ConditionElementImpl condition1 = new ConditionElementImpl(conditionExpr1);
        queryElement.getConditions().add(condition1);
        query.addQueryElement(queryElement);
        String hql = query.getHQLQueryString();
        String expectedResult = HQLConstants.SELECT + HQLConstants.SPACE + doVarName + "." + fieldName + HQLConstants.SPACE + HQLConstants.FROM + HQLConstants.SPACE + doClassName + HQLConstants.SPACE + doVarName;
        expectedResult += HQLConstants.SPACE + HQLConstants.WHERE + HQLConstants.SPACE + conditionExpr1;
        assertEquals("HQL should be properly generated with one condition", expectedResult, hql);

        //Adds a second condition
        final String conditionExpr2 = "myConditionExpression2";
        ConditionElementImpl condition2 = new ConditionElementImpl(conditionExpr2);
        QueryElementImpl queryElement2 = new QueryElementImpl();
        queryElement2.getConditions().add(condition2);
        query.addQueryElement(queryElement2);
        hql = query.getHQLQueryString();
        expectedResult += HQLConstants.SPACE + HQLConstants.AND + HQLConstants.SPACE + conditionExpr2;
        assertEquals("HQL should be properly generated with multiple conditions", expectedResult, hql);
    }

    /**
     * This test verifies that the query is properly converted into an Hibernate
     * query
     */
    public void testHibernateQueryCreation() {
        QueryImpl query = new QueryImpl();
        IQueryElement element = new QueryElementImpl();
        SelectElementImpl select = new SelectElementImpl();
        select.setDOClassName("doClass");
        select.setDOVarName("var");
        select.setFieldName("field");
        select.setFunction("func");
        element.getSelects().add(select);
        element.getGroupings().add(new GroupingElementImpl("myGroup"));
        element.getOrderBys().add(new OrderByElementImpl(true, "myOrder"));
        element.getConditions().add(new ConditionElementImpl("cond1 = :x1"));
        element.getConditions().add(new ConditionElementImpl("cond2"));
        final String x1Val = "val";
        element.getNamedParameters().put("x1", x1Val);
        query.addQueryElement(element);
        Session s = null;
        try {
            s = getActivityDateSource().getSession();
            Query hibQuery = query.getHQLQuery(s);
            final String expectedHQL = "select func(var.field) FROM doClass var WHERE cond1 = :x1 AND cond2 GROUP BY myGroup ORDER BY myOrder ASC";
            assertEquals("Proper HQL should be generated", expectedHQL, hibQuery.getQueryString());
            assertNotNull("Proper HQL should be generated", hibQuery.getNamedParameters());
            assertEquals("Proper HQL should be generated", 1, hibQuery.getNamedParameters().length);
            assertEquals("Proper HQL should be generated", "x1", hibQuery.getNamedParameters()[0]);

        } catch (HibernateException e) {
            fail("Error when creating the HQL query");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
}