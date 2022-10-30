package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/AttributeReferenceTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the AttributeReference class.
 *
 * @author Sergey Kalinichenko
 */
public class AttributeReferenceTests {

    private static final String REFERENCED_NAME = "abc";

    private AttributeReference attrReference;

    @Before
    public void prepare() {
        attrReference = new AttributeReference(
            IExpression.NULL
        ,   REFERENCED_NAME
        );
    }

    @Test(expected=NullPointerException.class)
    public void createNullBase() {
        new AttributeReference(null, REFERENCED_NAME);
    }

    @Test(expected=NullPointerException.class)
    public void createNullAttribute() {
        new AttributeReference(IExpression.NULL, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createEmptyAttribute() {
        new AttributeReference(IExpression.NULL, "");
    }

    @Test(expected=IllegalArgumentException.class)
    public void createNotTrimmedAttribute() {
        new AttributeReference(IExpression.NULL, " "+REFERENCED_NAME);
    }

    @Test
    public void correctBase() {
        assertEquals(IExpression.NULL, attrReference.getBase());
    }

    @Test
    public void correctAttribute() {
        assertEquals(REFERENCED_NAME, attrReference.getAttributeName());
    }

    @Test
    public void accept() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        attrReference.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitAttributeReference", ev.getMethods()[0]);
        assertSame(attrReference, ev.getArguments()[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals("null."+REFERENCED_NAME, attrReference.toString());
    }

    @Test
    public void equality() {
        assertEquals(attrReference
        ,   new AttributeReference(
                IExpression.NULL
            ,   REFERENCED_NAME
            )
        );
    }

    @Test
    public void equalityToSelf() {
        assertEquals(attrReference, attrReference);
    }

    @Test
    public void inequalityByExpression() {
        assertFalse(attrReference.equals(
            new AttributeReference(
                IExpression.FALSE
            ,   REFERENCED_NAME
            )
        ));
    }

    @Test
    public void inequalityByName() {
        assertFalse(attrReference.equals(
            new AttributeReference(
                IExpression.NULL
            ,   REFERENCED_NAME+"x"
            )
        ));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(attrReference.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(attrReference.equals(""));
    }

    @Test
    public void hashCodeInequalityByName() {
        assertTrue(attrReference.hashCode() !=
            new AttributeReference(
                IExpression.NULL
            ,   REFERENCED_NAME+"x"
            ).hashCode()
        );
    }

    @Test
    public void hashCodeInequalityByExpression() {
        assertTrue(attrReference.hashCode() !=
            new AttributeReference(
                IExpression.FALSE
            ,   REFERENCED_NAME
            ).hashCode()
        );
    }

}
