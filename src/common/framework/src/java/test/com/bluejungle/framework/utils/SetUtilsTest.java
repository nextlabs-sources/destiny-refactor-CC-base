/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.SetUtils;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/utils/test/SetUtilsTest.java#2 $:
 */

public class SetUtilsTest extends BaseDestinyTestCase {

    private Set[] sets;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SetUtilsTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        sets = new Set[3];
        sets[0] = new HashSet(5);
        sets[0].add(new Long(1));
        sets[0].add(new Long(5));
        sets[0].add(new Long(37));
        sets[0].add(new Long(18));
        sets[0].add(new Long(30057));

        sets[1] = new HashSet(3);
        sets[1].add(new Long(18));
        sets[1].add(new Long(24));
        sets[1].add(new Long(30057));

        sets[2] = new HashSet(10);
        sets[2].add(new Long(389));
        sets[2].add(new Long(24));
        sets[2].add(new Long(18));
        sets[2].add(new Long(99999));
        sets[2].add(new Long(9089));
        sets[2].add(new Long(3567));
        sets[2].add(new Long(32340));
        sets[2].add(new Long(7654));
        sets[2].add(new Long(560));
        sets[2].add(new Long(30057));

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for SetUtilsTest.
     * 
     * @param arg0
     */
    public SetUtilsTest(String arg0) {
        super(arg0);
    }

    public final void testIntersection() {
        Set result = SetUtils.intersection(sets);
        assertTrue(result.contains(new Long(18)));
        assertTrue(result.contains(new Long(30057)));
        assertEquals(2, result.size());

        result = SetUtils.intersection(null);
        assertEquals(0, result.size());

        result = SetUtils.intersection(new Set[0]);
        assertEquals(0, result.size());

    }

    public final void testUnion() {
        Set result = SetUtils.union(sets);

        assertTrue(result.contains(new Long(1)));
        assertTrue(result.contains(new Long(5)));
        assertTrue(result.contains(new Long(37)));
        assertTrue(result.contains(new Long(18)));
        assertTrue(result.contains(new Long(30057)));
        assertTrue(result.contains(new Long(24)));
        assertTrue(result.contains(new Long(99999)));
        assertTrue(result.contains(new Long(9089)));
        assertTrue(result.contains(new Long(3567)));
        assertTrue(result.contains(new Long(32340)));
        assertTrue(result.contains(new Long(7654)));
        assertTrue(result.contains(new Long(560)));
        assertTrue(result.contains(new Long(389)));
        assertEquals(13, result.size());

        result = SetUtils.union(null);
        assertEquals(0, result.size());

        result = SetUtils.union(new Set[0]);
        assertEquals(0, result.size());

    }

    public final void testMinus() {
        Set result = SetUtils.minus(sets[0], sets[1]);

        assertTrue(result.contains(new Long(1)));
        assertTrue(result.contains(new Long(5)));
        assertTrue(result.contains(new Long(37)));
        assertEquals(3, result.size());
    }

}