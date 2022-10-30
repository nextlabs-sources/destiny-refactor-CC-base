package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/RelationTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Relation class.
 *
 * @author Sergey Kalinichenko
 */
public class RelationTests {

    private Relation rel;

    @Before
    public void prepare() {
        rel = new Relation(
            RelationOperator.EQUAL
        ,   IExpression.NULL
        ,   Constant.makeString("right")
        );
    }

    @Test(expected=NullPointerException.class)
    public void constructNullOp() {
        new Relation(null, IExpression.NULL, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void constructNullLhs() {
        new Relation(RelationOperator.EQUAL, null, IExpression.NULL);
    }

    @Test(expected=NullPointerException.class)
    public void constructNullRhs() {
        new Relation(RelationOperator.EQUAL, IExpression.NULL, null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullOp() {
        rel.setOperator(null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullLhs() {
        rel.setLHS(null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullRhs() {
        rel.setRHS(null);
    }

    @Test
    public void setOp() {
        rel.setOperator(RelationOperator.NOT_EQUAL);
    }

    @Test
    public void setLhs() {
        rel.setLHS(Constant.makeDouble(0));
    }

    @Test
    public void setRhs() {
        rel.setRHS(Constant.makeDouble(0));
    }

    @Test
    public void getOp() {
        assertSame(RelationOperator.EQUAL, rel.getOperator());
    }

    @Test
    public void getLhs() {
        assertSame(IExpression.NULL, rel.getLHS());
    }

    @Test
    public void getRhs() {
        assertEquals(Constant.makeString("right"), rel.getRHS());
    }

    @Test
    public void accept() {
        RecordingExpressionVisitor pv = new RecordingExpressionVisitor();
        rel.accept(pv);
        assertEquals(1, pv.getMethods().length);
        assertEquals("visitRelation", pv.getMethods()[0]);
        assertSame(rel, pv.getArguments()[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals("null == \"right\"", rel.toString());
    }

    @Test
    public void equality() {
        Object other = new Relation(
            RelationOperator.EQUAL
        ,   IExpression.NULL
        ,   Constant.makeString("right")
        );
        assertEquals(rel, other);
        assertEquals(other, rel);
    }

    @Test
    public void equalityToSelf() {
        assertEquals(rel, rel);
    }

    @Test
    public void inequalityToNull() {
        assertFalse(rel.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(rel.equals(false));
    }

    @Test
    public void inequalityToWrongOperator() {
        Object other = new Relation(
            RelationOperator.NOT_EQUAL
        ,   IExpression.NULL
        ,   Constant.makeString("right")
        );
        assertFalse(rel.equals(other));
        assertFalse(other.equals(rel));
    }

    @Test
    public void inequalityToWrongLhs() {
        Object other = new Relation(
            RelationOperator.EQUAL
        ,   IExpression.TRUE
        ,   Constant.makeString("right")
        );
        assertFalse(rel.equals(other));
        assertFalse(other.equals(rel));
    }

    @Test
    public void inequalityToWrongRhs() {
        Object other = new Relation(
            RelationOperator.EQUAL
        ,   IExpression.NULL
        ,   IExpression.TRUE
        );
        assertFalse(rel.equals(other));
        assertFalse(other.equals(rel));
    }

    @Test
    public void hashCodeWorks() {
        Object other = new Relation(
            RelationOperator.EQUAL
        ,   IExpression.NULL
        ,   Constant.makeString("right")
        );
        assertTrue(rel.hashCode() == other.hashCode());
    }

}
