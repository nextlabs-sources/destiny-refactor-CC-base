package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/MultipartKeyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * This class tests the MultipartKey.
 *
 * @author Sergey Kalinichenko
 */
public class MultipartKeyTests {

    @Test(expected=NullPointerException.class)
    public void noNullParts() {
        new MultipartKey((Object[])null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void noEmptyParts() {
        new MultipartKey();
    }

    @Test
    public void hashCodeWorks() {
        MultipartKey key = new MultipartKey("a", "b", "c", null);
        // Make sure the hash code is calculated correctly
        int hashCode = key.hashCode();
        assertEquals("a".hashCode()+"b".hashCode()+"c".hashCode(), hashCode);
        // Make sure the hashCode does not change in subsequent invocations:
        assertEquals(hashCode, key.hashCode());
    }

    @Test
    public void equalsCodeWorks() {
        MultipartKey a1 = new MultipartKey("a", "b", null, "c");
        MultipartKey a2 = new MultipartKey("a", "b", null, "c");
        MultipartKey b = new MultipartKey("a", "b");
        MultipartKey c = new MultipartKey("a", "b", "c", "d");
        Object d = "a,b,c";
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        assertTrue(a1.equals(a1));
        assertFalse(a1.equals(b));
        assertFalse(b.equals(a1));
        assertFalse(a1.equals(c));
        assertFalse(c.equals(a1));
        assertFalse(a1.equals(d));
        assertFalse(d.equals(a1));
    }

    @Test
    public void toStringWorks() {
        MultipartKey key = new MultipartKey("a", "b", null, "c");
        assertEquals("{a, b, null, c}", key.toString());
    }

}
