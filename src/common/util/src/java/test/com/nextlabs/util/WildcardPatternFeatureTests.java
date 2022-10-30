package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/WildcardPatternFeatureTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * This is the test suite for wildcard patterns,
 * verifying features not tested in the accuracy suite.
 *
 * @author Sergey Kalinichenko
 */
public class WildcardPatternFeatureTests {

    WildcardPattern pa1 = WildcardPattern.compile("a");
    WildcardPattern pa2 = WildcardPattern.compile("a");
    WildcardPattern pb1 = WildcardPattern.compile("b");
    WildcardPattern pb2 = WildcardPattern.compile(
        "b"
    ,   WildcardPattern.CASE_SENSITIVE
    );

    @Test
    public void creationWorked() {
        assertNotNull(pa1);
        assertNotNull(pa2);
        assertNotNull(pb1);
        assertNotNull(pb2);
    }

    @Test
    public void quality() {
        assertEquals(pa1, pa2);
        assertFalse(pa1.equals(pb1));
        assertFalse(pb1.equals(pb2));
        assertFalse(pb2.equals(pb1));
    }

    @Test
    public void hashCodeWorks() {
        assertEquals(pa1.hashCode(), pa2.hashCode());
        assertFalse(pa1.hashCode() == pb1.hashCode());
        assertFalse(pb1.hashCode() == pb2.hashCode());
        assertFalse(pa1.hashCode() == pb2.hashCode());
    }

    @Test
    public void toStringWorks() {
        assertEquals("[\"b\"]", pb1.toString());
        assertEquals("[CS:\"b\"]", pb2.toString());
    }

    @Test(expected=NullPointerException.class)
    public void nullTextDefaultCompile() {
        WildcardPattern.compile(null);
    }

    @Test(expected=NullPointerException.class)
    public void nullTextCompileCaseSensitive() {
        WildcardPattern.compile(null, WildcardPattern.CASE_INSENSITIVE);
    }

    @Test(expected=NullPointerException.class)
    public void nullCaseSensitivity() {
        WildcardPattern.compile("abc", null);
    }

    @Test(expected=NullPointerException.class)
    public void nullTextIsMatch() {
        pb1.isMatch(null);
    }

    @Test
    public void inequalityNull() {
        assertFalse(pb1.equals(null));
    }

    @Test
    public void inequalityUnknown() {
        assertFalse(pb1.equals("b"));
    }

    @Test
    public void testCaseSensitivity() {
        assertNotNull(WildcardPattern.CaseSensitivity.values());
        assertNotNull(WildcardPattern.CaseSensitivity.valueOf("SENSITIVE"));
        assertNotNull(WildcardPattern.CaseSensitivity.valueOf("INSENSITIVE"));
    }

}
