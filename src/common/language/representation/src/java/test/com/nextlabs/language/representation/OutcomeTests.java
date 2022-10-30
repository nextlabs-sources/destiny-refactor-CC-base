package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/OutcomeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Tests for the Outcome enumeration.
 *
 * @author Sergey Kalinichenko
 */

public class OutcomeTests {

    @Test
    public void allowExists() {
        Outcome allow = Outcome.valueOf("ALLOW");
        assertEquals(Outcome.ALLOW, allow);
    }

    @Test
    public void denyExists() {
        Outcome deny = Outcome.valueOf("DENY");
        assertEquals(Outcome.DENY, deny);
    }

    @Test
    public void nothingExists() {
        Outcome nothing = Outcome.valueOf("NOTHING");
        assertEquals(Outcome.NOTHING, nothing);
    }

    @Test
    public void noOtherOutcomes() {
        Outcome[] ocs = Outcome.values();
        assertNotNull(ocs);
        assertEquals(3, ocs.length);
        assertEquals(Outcome.ALLOW, ocs[0]);
        assertEquals(Outcome.DENY, ocs[1]);
        assertEquals(Outcome.NOTHING, ocs[2]);
    }

    @Test
    public void ordinals() {
        assertEquals(1, Outcome.ALLOW.getOrdinal());
        assertEquals(2, Outcome.DENY.getOrdinal());
        assertEquals(0, Outcome.NOTHING.getOrdinal());
    }

    @Test
    public void declaredSizeIsCorrect() {
        assertEquals(Outcome.SIZE, Outcome.values().length);
    }

    @Test
    public void forOrdinal() {
        for (Outcome o : Outcome.values()) {
            assertEquals(o, Outcome.forOrdinal(o.getOrdinal()));
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void forNegativeOrdinal() {
        Outcome.forOrdinal(-1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void forLargeOrdinal() {
        Outcome.forOrdinal(Outcome.SIZE);
    }

}