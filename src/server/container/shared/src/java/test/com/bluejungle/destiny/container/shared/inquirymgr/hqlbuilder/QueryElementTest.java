/*
 * Created on Apr 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.Iterator;

import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the test for the HQL builder query element. It test each element
 * separately to make sure each element works fine.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/QueryElementTest.java#1 $
 */

public class QueryElementTest extends BaseDestinyTestCase {

    /**
     * This test verifies that the condition element is working properly. It
     * tests the class basics and the various properties.
     */
    public void testConditionElement() {
        final String myCondition = "someCondition";
        ConditionElementImpl condition = new ConditionElementImpl(myCondition);
        assertTrue("The condition element should implement the correct interface", condition instanceof IConditionElement);
        assertEquals("The condition attribute should be set properly", myCondition, condition.getExpression());
    }

    /**
     * This test verifies the grouping element (for group by). It tests the
     * setter / getter and class basics.
     */
    public void testGroupByElement() {
        final String myGrouping = "someGrouping";
        GroupingElementImpl groupElem = new GroupingElementImpl(myGrouping);
        assertTrue("The grouping element should implement the correct interface", groupElem instanceof IGroupingElement);
        assertEquals("The 'group by' attribute should be set properly", myGrouping, groupElem.getExpression());

    }

    /**
     * This test verifies the orderby element. It tests the setter / getter and
     * the class basics.
     */
    public void testOrderByElement() {
        final String myOrder = "someOrderField";
        final boolean myAscending = true;
        OrderByElementImpl orderByElem = new OrderByElementImpl(myAscending, myOrder);
        assertTrue("The order by element should implement the correct interface", orderByElem instanceof IOrderByElement);
        assertEquals("The 'ascending' attribute should be set properly", myAscending, orderByElem.isAcending());
        assertEquals("The 'order' attribute should be set properly", myOrder, orderByElem.getExpression());
        orderByElem = new OrderByElementImpl(!myAscending, myOrder);
        assertEquals("The 'ascending' attribute should be set properly", !myAscending, orderByElem.isAcending());
    }

    /**
     * This test verifies the basics for the Query element class.
     */
    public void testQueryElementBasics() {
        QueryElementImpl query = new QueryElementImpl();
        assertTrue(query instanceof IQueryElement);
        assertNotNull("Query element members should not be null", query.getConditions());
        assertNotNull("Query element members should not be null", query.getGroupings());
        assertNotNull("Query element members should not be null", query.getNamedParameters());
        assertNotNull("Query element members should not be null", query.getOrderBys());
        assertNotNull("Query element members should not be null", query.getSelects());
    }

    /**
     * This test verifies that the query elements are stored in the order they
     * are received. This is very important to guarantee the order of the
     * elements returned by the HQL query.
     */
    public void testQueryElementsOrdering() {
        QueryElementImpl queryElem = new QueryElementImpl();
        //Verify select order
        SelectElementImpl select1 = new SelectElementImpl();
        SelectElementImpl select2 = new SelectElementImpl();
        SelectElementImpl select3 = new SelectElementImpl();
        queryElem.getSelects().add(select1);
        queryElem.getSelects().add(select2);
        queryElem.getSelects().add(select3);
        Iterator it = queryElem.getSelects().iterator();
        assertEquals("Query element should preserve select order", select1, it.next());
        assertEquals("Query element should preserve select order", select2, it.next());
        assertEquals("Query element should preserve select order", select3, it.next());

        //Verify order by order
        OrderByElementImpl order1 = new OrderByElementImpl(true, "myOrder");
        OrderByElementImpl order2 = new OrderByElementImpl(true, "myOrder");
        OrderByElementImpl order3 = new OrderByElementImpl(true, "myOrder");
        queryElem.getOrderBys().add(order1);
        queryElem.getOrderBys().add(order2);
        queryElem.getOrderBys().add(order3);
        it = queryElem.getOrderBys().iterator();
        assertEquals("Query element should preserve 'order by' order", order1, it.next());
        assertEquals("Query element should preserve 'order by' order", order2, it.next());
        assertEquals("Query element should preserve 'order by' order", order3, it.next());

        //Verify grouping order
        GroupingElementImpl group1 = new GroupingElementImpl("myGrouping");
        GroupingElementImpl group2 = new GroupingElementImpl("myGrouping");
        GroupingElementImpl group3 = new GroupingElementImpl("myGrouping");
        queryElem.getGroupings().add(group1);
        queryElem.getGroupings().add(group2);
        queryElem.getGroupings().add(group3);
        it = queryElem.getGroupings().iterator();
        assertEquals("Query element should preserve grouping order", group1, it.next());
        assertEquals("Query element should preserve grouping order", group2, it.next());
        assertEquals("Query element should preserve grouping order", group3, it.next());

        //Verify conditions order
        ConditionElementImpl cond1 = new ConditionElementImpl("myCond");
        ConditionElementImpl cond2 = new ConditionElementImpl("myCond");
        ConditionElementImpl cond3 = new ConditionElementImpl("myCond");
        queryElem.getConditions().add(cond1);
        queryElem.getConditions().add(cond2);
        queryElem.getConditions().add(cond3);
        it = queryElem.getConditions().iterator();
        assertEquals("Query element should preserve condition order", cond1, it.next());
        assertEquals("Query element should preserve condition order", cond2, it.next());
        assertEquals("Query element should preserve condition order", cond3, it.next());
    }
}