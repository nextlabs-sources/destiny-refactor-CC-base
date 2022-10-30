package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/ExpressionReferenceTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the base reference class.
 *
 * @author Sergey Kalinichenko
 */
public class ExpressionReferenceTests {

    /**
     * The referenced Path.
     */
    private static final Path REF_PATH = new Path(new String[] {"abc", "def"});

    /**
     * The referenced ID.
     */
    private static final long REF_ID = 1;

    private IExpressionReference byPath;

    private IExpressionReference byId;

    @Before
    public void prepare() {
        byPath = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(REF_PATH, IExpression.class)
        );
        byId = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(REF_ID, IExpression.class)
        );
    }

    @Test
    public void createByPath() {
        assertEquals(REF_PATH, byPath.getReference().getPath());
        assertTrue(byPath.getReference().isByPath());
    }

    @Test(expected=NullPointerException.class)
    public void createByPathNull() {
        new ExpressionReference(null);
    }

    @Test
    public void createById() {
        assertEquals(REF_ID, byId.getReference().getId());
        assertFalse(byId.getReference().isByPath());
    }

    @Test(expected=IllegalStateException.class)
    public void getIdInvalid() {
        byPath.getReference().getId();
    }

    @Test(expected=IllegalStateException.class)
    public void getPathInvalid() {
        byId.getReference().getPath();
    }

    @Test
    public void acceptByPath() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        byPath.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitReference", ev.getMethods()[0]);
        assertSame(byPath, ev.getArguments()[0]);
    }

    @Test
    public void acceptById() {
        RecordingExpressionVisitor ev = new RecordingExpressionVisitor();
        byId.accept(ev);
        assertEquals(1, ev.getMethods().length);
        assertEquals("visitReference", ev.getMethods()[0]);
        assertSame(byId, ev.getArguments()[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals("id "+REF_ID, byId.toString());
        assertEquals("#"+REF_PATH, byPath.toString());
    }

    @Test
    public void equality() {
        assertEquals(
            new ExpressionReference(
                IReferenceFactory.DEFAULT.create(REF_PATH, IExpression.class)
            )
        ,   byPath
        );
        assertEquals(
            byId
        ,   new ExpressionReference(
                IReferenceFactory.DEFAULT.create(REF_ID, IExpression.class)
            )
        );
    }

    @Test
    public void equalityToSelf() {
        assertEquals(byPath, byPath);
        assertEquals(byId, byId);
    }

    @Test
    public void inequalityToNull() {
        assertFalse(byPath.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(byPath.equals(REF_PATH));
    }

    @Test
    public void hashCodeWorks() {
        assertTrue(byId.hashCode() != byPath.hashCode());
    }

}
