package com.bluejungle.framework.utils;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/utils/TestDisjointSets.java#1 $
 */

import java.util.Random;

import com.bluejungle.framework.utils.DisjointSets;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the DisjointSets class.
 */
public class TestDisjointSets extends TestCase {

    private static final int COUNT = 100000;
    private static final Long[] V;

    static {
        V = new Long[COUNT];
        for ( int i = 0 ; i != COUNT ; i++ ) {
            V[i] = new Long(i);
        }
    }

    public static TestSuite suite() {
        return new TestSuite(TestDisjointSets.class);
    }

    public void testGet() {
        DisjointSets<Long> s = new DisjointSets<Long>();
        Object a = s.get(V[0]);
        assertNotNull(a);
        Object b = s.get(V[1]);
        assertNotNull(b);
        assertNotSame(a, b);
    }

    public void testSize() {
        DisjointSets<Long> s = new DisjointSets<Long>();
        assertEquals(1, s.size(V[0]));
    }

    /**
     * Links all numbers with the same number of digits together.
     * Linking is done in a random order to make sure that the order
     * in which the links are entered does not matter.
     *
     * Although it is true that the test checks the same order
     * all the time since we seed the random number generator,
     * that order is highly irregular.
     */
    public void testLink() {
        DisjointSets<Long> s = new DisjointSets<Long>();
        Random r = new Random(123456);
        int[] seen = new int[COUNT];
        int remaining = COUNT*2;
        while (remaining != 0) {
            int index = r.nextInt(COUNT);
            if (seen[index]<2) {
                seen[index]++;
                remaining--;
                String image = ""+index;
                int next;
                do {
                    next = r.nextInt(COUNT);
                } while (index==next || (""+next).length() != image.length());
                s.union(V[index], V[next]);
            }
        }
        int[] counts = new int[] {-1, 10, 90, 900, 9000, 90000};
        for (int i = 0 ; i != COUNT ; i++) {
            String image = ""+i;
            assertEquals(counts[image.length()], s.size(V[i]));
        }
    }

}
