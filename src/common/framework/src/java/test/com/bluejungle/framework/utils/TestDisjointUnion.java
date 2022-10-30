package com.bluejungle.framework.utils;

/*
 * Created on Feb 21, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/utils/TestDisjointUnion.java#1 $:
 */


import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.bluejungle.framework.utils.DisjointUnion;

public class TestDisjointUnion extends TestCase {

    public static TestSuite suite() {
        return new TestSuite(TestDisjointUnion.class);
    }

    public void testAll() {
        DisjointUnion.Left<Integer, String> lval = DisjointUnion.makeLeft(42);

        assertTrue(lval.isLeft());
        assertFalse(lval.isRight());
        assertEquals((int)lval.getLeft(), 42);

        DisjointUnion.Right<Integer, String> rval = DisjointUnion.makeRight("banana");
        assertFalse(rval.isLeft());
        assertTrue(rval.isRight());
        assertEquals(rval.getRight(), "banana");

        List<DisjointUnion<Integer, String>> unions = new ArrayList<DisjointUnion<Integer, String>>();
        unions.add(lval);
        unions.add(rval);

        // Or create the item directly. The edge cases of Java Generics syntax are quite confusing, IMHO
        unions.add(DisjointUnion.<Integer, String>makeLeft(42));

        for (DisjointUnion<Integer, String> union : unions) {
            if (union.isLeft()) {
                assertEquals((int)union.getLeft(), 42);
            } else {
                assertEquals(union.getRight(), "banana");
            }
        }
    }

}
