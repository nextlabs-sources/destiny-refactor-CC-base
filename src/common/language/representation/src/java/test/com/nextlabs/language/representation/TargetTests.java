package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/TargetTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;

/**
 * Tests for the Target class.
 *
 * @author Sergey Kalinichenko
 */
public class TargetTests {

    private static final String SECTION1 = "on";

    private static final String SECTION2 = "by";

    private IExpression ALL_TRUE =
        new UnaryExpression(UnaryOperator.ALL, IExpression.TRUE);

    private IExpression ANY_FALSE =
        new UnaryExpression(UnaryOperator.ANY, IExpression.FALSE);

    Target t1;

    @Before
    public void prepare() {
        t1 = new Target();
    }

    @Test(expected=NullPointerException.class)
    public void addContextNullCondition() {
        t1.addContext("a", null);
    }

    @Test(expected=NullPointerException.class)
    public void addContextNullSection() {
        t1.addContext(null, ANY_FALSE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextNotTrimmedSection() {
        t1.addContext(" aaa", ANY_FALSE);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextEmptySection() {
        t1.addContext("", ANY_FALSE);
    }

    @Test(expected=NullPointerException.class)
    public void hasContextNullSection() {
        t1.hasContext(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void hasContextNonTrimmedSection() {
        t1.hasContext("aaa ");
    }

    @Test(expected=IllegalArgumentException.class)
    public void hasContextEmptySection() {
        t1.hasContext("");
    }

    @Test
    public void addContext() {
        assertFalse(t1.hasContext(SECTION1));
        ITarget.IContext c0 =
            t1.addContext(SECTION1, ALL_TRUE);
        assertNull(c0);
        assertTrue(t1.hasContext(SECTION1));
        c0 = t1.addContext(SECTION1, ANY_FALSE);
        assertNotNull(c0);
        assertEquals(SECTION1, c0.getSection());
        assertSame(t1, c0.getTarget());
        assertEquals(ALL_TRUE, c0.getCondition());
        c0 = t1.getContext(SECTION1);
        assertNotNull(c0);
        assertEquals(SECTION1, c0.getSection());
        assertSame(t1, c0.getTarget());
        assertEquals(ANY_FALSE, c0.getCondition());
        Iterator<String> ci = t1.getSections().iterator();
        assertTrue(ci.hasNext());
        assertEquals(SECTION1, ci.next());
        assertFalse(ci.hasNext());
    }

    @Test
    public void getContextsOrdered1() {
        t1.addContext(SECTION1, ALL_TRUE);
        t1.addContext(SECTION2, ANY_FALSE);
        assertTrue(t1.hasContext(SECTION1));
        assertTrue(t1.hasContext(SECTION2));
        Iterator<ITarget.IContext> i = t1.getContexts().iterator();
        assertTrue(i.hasNext());
        ITarget.IContext c = i.next();
        assertNotNull(c);
        assertFalse(c.equals(null));
        assertFalse(c.equals(""));
        int h = c.hashCode();
        assertSame(t1, c.getTarget());
        assertEquals(SECTION1, c.getSection());
        assertEquals(ALL_TRUE, c.getCondition());
        assertTrue(i.hasNext());
        c = i.next();
        assertNotNull(c);
        assertFalse(c.hashCode() == h);
        assertEquals(c, c);
        assertSame(t1, c.getTarget());
        assertEquals(SECTION2, c.getSection());
        assertEquals(ANY_FALSE, c.getCondition());
        assertFalse(i.hasNext());
    }

    @Test
    public void getContextsOrdered2() {
        t1.addContext(SECTION2, ANY_FALSE);
        t1.addContext(SECTION1, ALL_TRUE);
        Iterator<ITarget.IContext> i = t1.getContexts().iterator();
        assertTrue(i.hasNext());
        ITarget.IContext c = i.next();
        assertNotNull(c);
        assertSame(t1, c.getTarget());
        assertEquals(SECTION2, c.getSection());
        assertEquals(ANY_FALSE, c.getCondition());
        assertTrue(i.hasNext());
        c = i.next();
        assertNotNull(c);
        assertSame(t1, c.getTarget());
        assertEquals(SECTION1, c.getSection());
        assertEquals(ALL_TRUE, c.getCondition());
        assertFalse(i.hasNext());
    }

    @Test
    public void getSectionsOrdered1() {
        t1.addContext(SECTION1, ALL_TRUE);
        t1.addContext(SECTION2, ANY_FALSE);
        Iterator<String> i = t1.getSections().iterator();
        assertTrue(i.hasNext());
        assertEquals(SECTION1, i.next());
        assertTrue(i.hasNext());
        assertEquals(SECTION2, i.next());
        assertFalse(i.hasNext());
    }

    @Test
    public void getSectionsOrdered2() {
        t1.addContext(SECTION2, ANY_FALSE);
        t1.addContext(SECTION1, ALL_TRUE);
        Iterator<String> i = t1.getSections().iterator();
        assertTrue(i.hasNext());
        assertEquals(SECTION2, i.next());
        assertTrue(i.hasNext());
        assertEquals(SECTION1, i.next());
        assertFalse(i.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void removeContextNullSection() {
        t1.removeContext(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextNonTrimmedSection() {
        t1.removeContext(" aaa");
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextEmptySection() {
        t1.removeContext("");
    }

    @Test
    public void removeContext() {
        t1.addContext(SECTION1, ALL_TRUE);
        assertTrue(t1.hasContext(SECTION1));
        ITarget.IContext c = t1.removeContext(SECTION1);
        assertFalse(t1.hasContext(SECTION1));
        assertNotNull(c);
        assertEquals(ALL_TRUE, c.getCondition());
    }

    @Test
    public void inequalityToNull() {
        assertFalse(t1.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(t1.equals(123));
    }

    @Test
    public void equalityEmpty() {
        Target t2 = new Target();
        assertEquals(t1, t2);
        assertEquals(t2, t1);
    }

    @Test
    public void equalityNotEmpty() {
        t1.addContext("a", ALL_TRUE);
        Target t2 = new Target();
        t2.addContext("a", ALL_TRUE);
        assertEquals(t1, t2);
        assertEquals(t2, t1);
    }

    @Test
    public void equalityDifferent() {
        Target t2 = new Target();
        t2.addContext("a", ALL_TRUE);
        assertFalse(t1.equals(t2));
    }

    @Test
    public void isEmptyWorks() {
        assertTrue(t1.isEmpty());
        t1.addContext("a", ALL_TRUE);
        assertFalse(t1.isEmpty());
    }

    @Test
    public void hashCodeEmpty() {
        Target t2 = new Target();
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void hashCodeNotEmpty() {
        t1.addContext("a", ALL_TRUE);
        Target t2 = new Target();
        t2.addContext("a", ALL_TRUE);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void hashCodeDifferent() {
        Target t2 = new Target();
        t2.addContext("a", ALL_TRUE);
        assertFalse(t1.hashCode() == t2.hashCode());
    }

}
