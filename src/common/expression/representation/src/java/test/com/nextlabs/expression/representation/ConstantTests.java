package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/ConstantTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the Constant class.
 *
 * @author Sergey Kalinichenko
 */
public class ConstantTests {

    @Test
    public void intConstant() {
        assertEquals(1, Constant.makeDouble(1).getValue());
    }

    @Test
    public void emptyStringConstant() {
        assertEquals("", Constant.makeString("").getValue());
    }

    @Test
    public void stringConstant() {
        assertEquals("xyz", Constant.makeString("xyz").getValue());
    }

    @Test
    public void emptyCsStringConstant() {
        assertEquals("", Constant.makeCsString("").getValue());
    }

    @Test
    public void csStringConstant() {
        assertEquals("xyz", Constant.makeCsString("xyz").getValue());
    }

    @Test
    public void dateConstant() {
        Date dt = new Date();
        assertEquals(dt, Constant.makeDate(dt).getValue());
    }

    @Test
    public void nullConstant() {
        assertNull(IExpression.NULL.getValue());
    }

    @Test
    public void nullHashCode() {
        assertEquals(0, IExpression.NULL.hashCode());
    }

    @Test
    public void valueHashCode() {
        String value = "xyz";
        Constant cv = Constant.makeString(value);
        assertEquals(value.hashCode(), cv.hashCode());
        assertSame(value, cv.getValue());
    }

    @Test
    public void accept() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        IExpression one = Constant.makeDouble(1);
        one.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitConstant", ev.getMethods()[0]);
        assertSame(one, ev.getArguments()[0]);
    }

    @Test
    public void doubleToString() {
        assertEquals("1.0", Constant.makeDouble(1).toString());
    }

    @Test
    public void intToString() {
        assertEquals("1", Constant.makeInteger(1).toString());
    }

    @Test
    public void nullToString() {
        assertEquals("null", IExpression.NULL.toString());
    }

    @Test
    public void strToString() {
        assertEquals("\"xyz\"",  Constant.makeString("xyz").toString());
    }

    @Test
    public void iterableToString() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(1, 2)
        ,   IDataType.DOUBLE
        );
        assertEquals("(1, 2)", c.toString());
    }

    @Test
    public void equals() {
        IExpression one = Constant.makeDouble(1);
        IExpression xyz =  Constant.makeString("xyz");
        Date date = new Date();
        IExpression now = Constant.makeDate(date);
        assertFalse(one.equals(null));
        assertFalse(one.equals(xyz));
        assertFalse(one.equals(now));
        assertFalse(xyz.equals(null));
        assertFalse(xyz.equals(one));
        assertFalse(xyz.equals(now));
        assertFalse(now.equals(null));
        assertFalse(now.equals(one));
        assertFalse(now.equals(xyz));
        assertTrue(one.equals(Constant.makeDouble(1)));
        assertTrue(xyz.equals(Constant.makeString("xyz")));
        assertTrue(now.equals(Constant.makeDate(date)));
        assertTrue(IExpression.NULL.equals(IExpression.NULL));
        assertTrue(IExpression.NULL.equals(new Constant(null,IDataType.NULL)));
        assertFalse(IExpression.NULL.equals(one));
        assertFalse(IExpression.NULL.equals(xyz));
        assertFalse(IExpression.NULL.equals(now));
    }

    @Test(expected=NullPointerException.class)
    public void nullDateArgument() {
        Constant.makeDate(null);
    }

    @Test(expected=NullPointerException.class)
    public void nullStringArgument() {
        Constant.makeString(null);
    }

    @Test(expected=NullPointerException.class)
    public void nullCsStringArgument() {
        Constant.makeCsString(null);
    }

    @Test(expected=NullPointerException.class)
    public void nullConstantsArgument() {
        Constant.makeMultivalue(null);
    }

    @Test(expected=NullPointerException.class)
    public void nullValuesArgument() {
        Constant.makeMultivalue(null, IDataType.STRING);
    }

    @Test(expected=NullPointerException.class)
    public void nullTypeArgument() {
        Constant.makeMultivalue(Arrays.asList(1), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void incompatibleConstantTypes() {
        Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDate(new Date())
            ,   Constant.makeDouble(0)
            )
        );
    }

    @Test
    public void genericMakeWorks() {
        Constant c = Constant.make(1, IDataType.UNKNOWN);
        assertEquals(1, c.getValue());
        assertSame(IDataType.UNKNOWN, c.getType());
    }

    @Test(expected=NullPointerException.class)
    public void nullGenericValuesArgument() {
        Constant.make(null, IDataType.STRING);
    }

    @Test(expected=NullPointerException.class)
    public void nullGenericTypeArgument() {
        Constant.make(0, null);
    }

    @Test
    public void typeOfEmptyList() {
        List<Constant> empty = Collections.emptyList();
        Constant c = Constant.makeMultivalue(empty);
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.NULL, mt.getInnerType());
    }

    @Test
    public void nullIsCompatible() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDate(new Date())
            ,   IExpression.NULL
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DATE, mt.getInnerType());
    }

    @Test
    public void numbersAreCompatible1() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeInteger(1)
            ,   Constant.makeDouble(1)
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void numbersAreCompatible2() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDouble(1)
            ,   Constant.makeInteger(1)
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void numbersAndNullsAreCompatible1() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDouble(1)
            ,   IExpression.NULL
            ,   Constant.makeInteger(1)
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void numbersAndNullsAreCompatible2() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDouble(1)
            ,   Constant.makeInteger(1)
            ,   IExpression.NULL
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void numbersAndNullsAreCompatible3() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                IExpression.NULL
            ,   Constant.makeDouble(1)
            ,   Constant.makeInteger(1)
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void numbersAndNullsAreCompatible4() {
        Constant c = Constant.makeMultivalue(
            Arrays.asList(
                IExpression.NULL
            ,   Constant.makeInteger(1)
            ,   Constant.makeDouble(1)
            )
        );
        IDataType t = c.getType();
        assertTrue(t instanceof IMultivalueDataType);
        IMultivalueDataType mt = (IMultivalueDataType)t;
        assertEquals(IDataType.DOUBLE, mt.getInnerType());
    }

    @Test
    public void nestedEmptyLists() {
        List<Constant> ev = Collections.emptyList();
        Constant empty = Constant.makeMultivalue(ev);
        Constant numList = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDouble(0)
            ,   Constant.makeDouble(1)
            )
        );
        Constant combined1 = Constant.makeMultivalue(
            Arrays.asList(empty, numList)
        );
        IDataType t1 = combined1.getType();
        assertTrue(t1 instanceof IMultivalueDataType);
        IMultivalueDataType mt1 = (IMultivalueDataType)t1;
        IDataType t2 = mt1.getInnerType();
        assertTrue(t2 instanceof IMultivalueDataType);
        IMultivalueDataType mt2 = (IMultivalueDataType)t2;
        assertEquals(IDataType.DOUBLE, mt2.getInnerType());
        Constant combined2 = Constant.makeMultivalue(
            Arrays.asList(numList, empty)
        );
        t1 = combined2.getType();
        assertTrue(t1 instanceof IMultivalueDataType);
        mt1 = (IMultivalueDataType)t1;
        t2 = mt1.getInnerType();
         assertTrue(t2 instanceof IMultivalueDataType);
        mt2 = (IMultivalueDataType)t2;
        assertEquals(IDataType.DOUBLE, mt2.getInnerType());
    }

    @Test
    public void nesetdListsWithNulls() {
        Constant withNull = Constant.makeMultivalue(
            Arrays.asList(
                IExpression.NULL
            )
        );
        Constant numList = Constant.makeMultivalue(
            Arrays.asList(
                Constant.makeDouble(0)
            ,   Constant.makeDouble(1)
            )
        );
        Constant combined1 = Constant.makeMultivalue(
            Arrays.asList(withNull, numList)
        );
        IDataType t1 = combined1.getType();
        assertTrue(t1 instanceof IMultivalueDataType);
        IMultivalueDataType mt1 = (IMultivalueDataType)t1;
        IDataType t2 = mt1.getInnerType();
        assertTrue(t2 instanceof IMultivalueDataType);
        IMultivalueDataType mt2 = (IMultivalueDataType)t2;
        assertEquals(IDataType.DOUBLE, mt2.getInnerType());
        Constant combined2 = Constant.makeMultivalue(
            Arrays.asList(numList, withNull)
        );
        t1 = combined2.getType();
        assertTrue(t1 instanceof IMultivalueDataType);
        mt1 = (IMultivalueDataType)t1;
        t2 = mt1.getInnerType();
         assertTrue(t2 instanceof IMultivalueDataType);
        mt2 = (IMultivalueDataType)t2;
        assertEquals(IDataType.DOUBLE, mt2.getInnerType());
    }

}
