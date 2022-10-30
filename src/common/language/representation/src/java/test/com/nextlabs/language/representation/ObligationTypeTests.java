package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/ObligationTypeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.Path;

/**
 * Tests for the Obligation Type class.
 *
 * @author Sergey Kalinichenko
 */
public class ObligationTypeTests {

    private static final Path TEST_PATH1 = new Path(
        "test", "path", "oblifation", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "test", "path", "oblifation", "two"
    );

    private ObligationType ot1;

    private ObligationType ot2;

    @Before
    public void prepare() {
        ot1 = new ObligationType(TEST_PATH1);
        ot2 = new ObligationType(TEST_PATH2);
    }

    @Test(expected=NullPointerException.class)
    public void nullPath() {
        new ObligationType(null);
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        ot1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitObligationType(IObligationType obligationType) {
                assertSame(ot1, obligationType);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void toStringWorks() {
        assertEquals(
            "obligation "+TEST_PATH1, ot1.toString()
        );
    }

    @Test
    public void hashCodeWorks() {
        assertFalse(ot1.hashCode() == ot2.hashCode());
    }

    @Test
    public void equality() {
        assertFalse(ot1.equals(ot2));
        assertFalse(ot2.equals(ot1));
        ot2 = new ObligationType(TEST_PATH1);
        assertTrue(ot1.equals(ot2));
        assertTrue(ot2.equals(ot1));
        ot2.addArgument("a", IDataType.BOOLEAN, true, null);
        assertFalse(ot1.equals(ot2));
        assertFalse(ot2.equals(ot1));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(ot1.equals(""));
    }

    @Test
    public void inequalityToNull() {
        assertFalse(ot2.equals(null));
    }

}
