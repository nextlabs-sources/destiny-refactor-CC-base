package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/FunctionTypeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.Path;

/**
 * Tests for the Function Type class.
 *
 * @author Sergey Kalinichenko
 */
public class FunctionTypeTests {

    private static final Path TEST_PATH1 = new Path(
        "test", "path", "function", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "test", "path", "function", "two"
    );

    private static final IDataType RET_TYPE1 = IDataType.BOOLEAN;

    private static final IDataType RET_TYPE2 =
        DataType.makeMultivalue(IDataType.DATE);

    private FunctionType ft1;

    private FunctionType ft2;

    @Before
    public void prepare() {
        ft1 = new FunctionType(TEST_PATH1, RET_TYPE1);
        ft2 = new FunctionType(TEST_PATH2, RET_TYPE2);
    }

    @Test
    public void returnType() {
        assertEquals(RET_TYPE1, ft1.getReturnType());
        assertEquals(RET_TYPE2, ft2.getReturnType());
    }

    @Test(expected=NullPointerException.class)
    public void nullPath() {
        new FunctionType(null, RET_TYPE1);
    }

    @Test(expected=NullPointerException.class)
    public void nullReturnType() {
        new FunctionType(TEST_PATH1, null);
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        ft1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitFunctionType(IFunctionType functionType) {
                assertSame(ft1, functionType);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals(
            "function "+TEST_PATH1+" returns "+RET_TYPE1
        ,   ft1.toString()
        );
    }

    @Test
    public void hashCodeWorks() {
        assertFalse(ft1.hashCode() == ft2.hashCode());
    }

    @Test
    public void equality() {
        assertFalse(ft1.equals(ft2));
        assertFalse(ft2.equals(ft1));
        ft2 = new FunctionType(TEST_PATH1, RET_TYPE2);
        assertFalse(ft1.equals(ft2));
        ft2 = new FunctionType(TEST_PATH1, DataType.makeMultivalue(RET_TYPE1));
        assertFalse(ft1.equals(ft2));
        ft2 = new FunctionType(TEST_PATH1, RET_TYPE1);
        assertTrue(ft1.equals(ft2));
        assertTrue(ft2.equals(ft1));
        ft2.addArgument("a", RET_TYPE1, true, null);
        assertFalse(ft1.equals(ft2));
        assertFalse(ft2.equals(ft1));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(ft1.equals(""));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(ft2.equals(null));
    }

}
