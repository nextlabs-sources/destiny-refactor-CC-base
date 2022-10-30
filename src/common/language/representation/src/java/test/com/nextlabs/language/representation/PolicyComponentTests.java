package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/PolicyComponentTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the Policy Component class.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyComponentTests {

    private static final Path TEST_PATH1 = new Path(
        "component", "test", "path", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "component", "test", "path", "two"
    );

    private static final IReference<IContextType> TEST_REF1 =
        IReferenceFactory.DEFAULT.create(123, IContextType.class);

    private static final IReference<IContextType> TEST_REF2 =
        IReferenceFactory.DEFAULT.create(new Path("a"), IContextType.class);

    private PolicyComponent pc1;

    private IPolicyComponent pc2;

    @Before
    public void prepare() {
        pc1 = new PolicyComponent(TEST_PATH1, TEST_REF1, IExpression.FALSE);
        pc2 = new PolicyComponent(TEST_PATH2, TEST_REF2, IExpression.TRUE);
    }

    @Test(expected=NullPointerException.class)
    public void nullPath() {
        new PolicyComponent(null, TEST_REF1, IExpression.FALSE);
    }

    @Test(expected=NullPointerException.class)
    public void nullType() {
        new PolicyComponent(TEST_PATH1, null, IExpression.FALSE);
    }

    @Test(expected=NullPointerException.class)
    public void nullPredicate() {
        new PolicyComponent(TEST_PATH1, TEST_REF1, null);
    }

    @Test
    public void getType() {
        assertEquals(TEST_REF1, pc1.getType());
        assertEquals(TEST_REF2, pc2.getType());
    }

    @Test
    public void getPredicate() {
        assertEquals(IExpression.FALSE, pc1.getPredicate());
        assertEquals(IExpression.TRUE, pc2.getPredicate());
    }

    @Test
    public void toStringWorks() {
        assertEquals(
            "component "+TEST_PATH1+" : "+TEST_REF1+" = "+IExpression.FALSE
        ,   pc1.toString()
        );
    }

    @Test
    public void equality() {
        assertFalse(pc1.equals(pc2));
        pc2 = new PolicyComponent(TEST_PATH1, TEST_REF2, IExpression.FALSE);
        assertFalse(pc1.equals(pc2));
        pc2 = new PolicyComponent(TEST_PATH1, TEST_REF1, IExpression.TRUE);
        assertFalse(pc1.equals(pc2));
        pc2 = new PolicyComponent(TEST_PATH1, TEST_REF1, IExpression.FALSE);
        assertTrue(pc1.equals(pc2));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(pc1.equals(""));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(pc1.equals(null));
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        pc1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitPolicyComponent(IPolicyComponent component) {
                assertSame(pc1, component);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void hashCodeWorks() {
        assertTrue(pc1.hashCode() != pc2.hashCode());
    }

}
