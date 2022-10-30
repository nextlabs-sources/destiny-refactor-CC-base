package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/CallableTypeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IMultivalueDataType;
import com.nextlabs.util.Path;

/**
 * Tests for the Callable Type class.
 *
 * @author Sergey Kalinichenko
 */
public class CallableTypeTests {

    private static final String TEST_ARG_NAME1 = "arg1";

    private static final IDataType TEST_ARG_TYPE1 = IDataType.BOOLEAN;

    private static final String TEST_ARG_NAME2 = "arg2";

    private static final IDataType TEST_ARG_TYPE2 = IDataType.CS_STRING;

    private CallableType<TestType> ct;

    private static class TestType extends CallableType<TestType> {
        public TestType(Path path) {
            super(path);
        }
        public void accept(IDefinitionVisitor visitor) {
        }
    }

    @Before
    public void prepare() {
        ct = new TestType(new Path("test"));
    }

    @Test(expected=NullPointerException.class)
    public void addArgumentNullName() {
        ct.addArgument(null, TEST_ARG_TYPE1, true, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addArgumentEmptyName() {
        ct.addArgument("", TEST_ARG_TYPE1, true, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addArgumentInvalidName() {
        ct.addArgument(" aaa", TEST_ARG_TYPE1, true, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void addArgumentNullType() {
        ct.addArgument(TEST_ARG_NAME1, null, true, IExpression.NULL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addArgumentDuplicateName() {
        ct.addArgument(TEST_ARG_NAME1, TEST_ARG_TYPE1, true, null);
        ct.addArgument(TEST_ARG_NAME1, TEST_ARG_TYPE2, true, null);
    }

    @Test
    public void iterator() {
        CallableType.IArgument origArg1 = ct.addArgument(
            TEST_ARG_NAME1
        ,   TEST_ARG_TYPE1
        ,   true
        ,   IExpression.NULL
        );

        CallableType.IArgument origArg2 = ct.addArgument(
            TEST_ARG_NAME2
        ,   DataType.makeMultivalue(TEST_ARG_TYPE2)
        ,   false
        ,   null
        );
        Iterator<CallableType.IArgument> iter = ct.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        CallableType.IArgument arg1 = iter.next();
        assertEquals(origArg1, arg1);
        assertNotNull(arg1);
        assertEquals(TEST_ARG_NAME1, arg1.getName());
        assertEquals(TEST_ARG_TYPE1, arg1.getType());
        assertTrue(arg1.isRequired());
        assertTrue(arg1.hasDefault());
        assertEquals(IExpression.NULL, arg1.getDefault());
        CallableType.IArgument arg2 = iter.next();
        assertEquals(origArg2, arg2);
        assertNotNull(arg2);
        assertEquals(TEST_ARG_NAME2, arg2.getName());
        assertEquals(
            TEST_ARG_TYPE2
        ,   ((IMultivalueDataType)arg2.getType()).getInnerType()
        );
        assertFalse(arg2.isRequired());
        assertFalse(arg2.hasDefault());
        assertNull(arg2.getDefault());
        assertFalse(arg1.equals(arg2));
        assertFalse(arg1.hashCode() == arg2.hashCode());
    }

    @Test
    public void toStringArguments() {
        ct.addArgument(
            TEST_ARG_NAME1
        ,   TEST_ARG_TYPE1
        ,   true
        ,  IExpression.NULL
        );
        ct.addArgument(
            TEST_ARG_NAME2
        ,   DataType.makeMultivalue(TEST_ARG_TYPE2)
        ,   false
        ,   null
        );
        StringBuffer buf = new StringBuffer();
        ct.toStringArgList(buf);
        assertEquals(
            "(+"+TEST_ARG_NAME1+":"+TEST_ARG_TYPE1+"=null, "
        +   TEST_ARG_NAME2+":multivalued "+TEST_ARG_TYPE2+")"
        ,   buf.toString());
    }

    @Test
    public void argumentEquality() {
        CallableType<TestType> ct1 =
            new TestType(new Path(new String[] {"test"}));
        CallableType.IArgument a1 = ct1.addArgument(
            TEST_ARG_NAME1
        ,   DataType.makeMultivalue(TEST_ARG_TYPE1)
        ,   true
        ,   IExpression.NULL
        );
        CallableType<TestType> ct2 =
            new TestType(new Path(new String[] {"test"}));
        CallableType.IArgument a2 = ct2.addArgument(
            TEST_ARG_NAME1
        ,   DataType.makeMultivalue(TEST_ARG_TYPE2)
        ,   true
        ,   IExpression.NULL
        );
        CallableType<TestType> ct3 =
            new TestType(new Path(new String[] {"test"}));
        CallableType.IArgument a3 = ct3.addArgument(
            TEST_ARG_NAME1
        ,   DataType.makeMultivalue(TEST_ARG_TYPE1)
        ,   false
        ,   IExpression.NULL
        );
        CallableType<TestType> ct4 =
            new TestType(new Path(new String[] {"test"}));
        CallableType.IArgument a4 = ct4.addArgument(
            TEST_ARG_NAME1
        ,   TEST_ARG_TYPE1
        ,   true
        ,   IExpression.NULL
        );
        CallableType<TestType> ct5 =
            new TestType(new Path(new String[] {"test"}));
        CallableType.IArgument a5 = ct5.addArgument(
            TEST_ARG_NAME1
        ,   DataType.makeMultivalue(TEST_ARG_TYPE2)
        ,   true
        ,   null
        );
        Object[] args = new Object[] {a1, a2, a3, a4, a5};
        for ( int i = 0 ; i != args.length ; i++) {
            assertNotNull(args[i]);
            assertEquals(args[i], args[i]);
            assertFalse(args[i].equals(null));
            for (int j = i+1 ; j != args.length ; j++) {
                assertFalse(args[i].equals(args[j]));
            }
        }
    }

    @Test
    public void remove() {
        CallableType.IArgument arg = ct.addArgument(
            TEST_ARG_NAME1
        ,   TEST_ARG_TYPE1
        ,   true
        ,   IExpression.NULL
        );
        ct.removeArgument(arg);
        assertEquals(0, ct.getArgumentCount());
    }

    @Test(expected=NullPointerException.class)
    public void removeNull() {
        ct.removeArgument((CallableType.IArgument)null);
    }

    @Test
    public void removeByName() {
        CallableType.IArgument origArg = ct.addArgument(
            TEST_ARG_NAME1
        ,   TEST_ARG_TYPE1
        ,   true
        ,   IExpression.NULL
        );
        CallableType.IArgument arg = ct.removeArgument(TEST_ARG_NAME1);
        assertEquals(origArg, arg);
    }

}
