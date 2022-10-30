package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/StringsTests.java#1 $
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;

/**
 * Tests for the Strings class.
 *
 * @author Sergey Kalinichenko
 */
public class StringsTests {

    @Test
    public void nullIsEmpty() {
        assertTrue(Strings.isEmpty(null));
    }

    @Test
    public void emptyIsEmpty() {
        assertTrue(Strings.isEmpty(""));
    }

    @Test
    public void blankIsNotEmpty() {
        assertFalse(Strings.isEmpty(" "));
    }

    @Test
    public void nullIsTrimmed() {
        assertTrue(Strings.isTrimmed(null));
    }

    @Test
    public void emptyIsTrimmed() {
        assertTrue(Strings.isTrimmed(""));
    }

    @Test
    public void blankIsNotTrimmed() {
        assertFalse(Strings.isTrimmed(" "));
    }

    @Test
    public void leftSideNotTrimmed() {
        assertFalse(Strings.isTrimmed(" a"));
    }

    @Test
    public void rightSideNotTrimmed() {
        assertFalse(Strings.isTrimmed("a "));
    }

    @Test
    public void bothSidesNotTrimmed() {
        assertFalse(Strings.isTrimmed(" a "));
    }

    @Test
    public void bothSidesTrimmed() {
        assertTrue(Strings.isTrimmed("abcdef"));
    }

    @Test
    public void otherWhitespaceNotTrimmedLeft1() {
        assertFalse(Strings.isTrimmed("\t"));
    }

    @Test
    public void otherWhitespaceNotTrimmedLeft2() {
        assertFalse(Strings.isTrimmed("\n"));
    }
    @Test
    public void otherWhitespaceNotTrimmedLeft3() {
        assertFalse(Strings.isTrimmed("\r"));
    }
    @Test
    public void otherWhitespaceNotTrimmedRight1() {
        assertFalse(Strings.isTrimmed("a\t"));
    }
    @Test
    public void otherWhitespaceNotTrimmedRight2() {
        assertFalse(Strings.isTrimmed("a\n"));
    }

    @Test
    public void otherWhitespaceNotTrimmedRight3() {
        assertFalse(Strings.isTrimmed("a\r"));
    }

    @Test
    public void privateConstructor() throws Exception {
        Class<Strings> str = Strings.class;
        Constructor<Strings> cs = str.getDeclaredConstructor((Class[])null);
        cs.setAccessible(true);
        cs.newInstance();
    }

}
