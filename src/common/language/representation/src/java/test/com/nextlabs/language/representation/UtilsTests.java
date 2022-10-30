package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/UtilsTests.java#1 $
 */

import static com.nextlabs.language.representation.Utils.compareIterables;
import static com.nextlabs.language.representation.Utils.containsAll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Tests for the Utils class.
 *
 * @author Sergey Kalinichenko
 */
public class UtilsTests {

    @Test
    public void compareIterablesLhsNull() {
        assertFalse(compareIterables(null, Collections.emptySet()));
    }

    @Test
    public void compareIterablesRhsNull() {
        assertFalse(compareIterables(Collections.emptySet(), null));
    }

    @Test
    public void compareIterablesBothNull() {
        assertTrue(compareIterables(null, null));
    }

    @Test
    public void compareIterablesLhsHasNull() {
        assertFalse(compareIterables(
            Collections.singleton((String)null)
        ,   Collections.singleton("")
        ));
    }

    @Test
    public void compareIterablesRhsHasNull() {
        assertFalse(compareIterables(
            Collections.singleton("")
        ,   Collections.singleton((String)null)
        ));
    }

    @Test
    public void compareIterablesBothHaveNull() {
        assertTrue(compareIterables(
            Collections.singleton(null)
        ,   Collections.singleton(null)
        ));
    }

    @Test
    public void compareIterablesLhsShorter() {
        assertFalse(compareIterables(
            Collections.emptySet()
        ,   Collections.singleton(null)
        ));
    }

    @Test
    public void compareIterablesRhsShorter() {
        assertFalse(compareIterables(
            Collections.singleton(null)
        ,   Collections.emptySet()
        ));
    }

    @Test
    public void compareIterablesEmpty() {
        assertTrue(compareIterables(null, null));
    }

    @Test
    public void compareIterablesEqual() {
        assertTrue(compareIterables(
            Arrays.asList(new Integer[] {1, 2, null, 4, 5})
        ,   Arrays.asList(new Integer[] {1, 2, null, 4, 5})
        ));
    }

    @Test
    public void containsAllFalse() {
        assertFalse(
            containsAll(
                Collections.singleton(1)
            ,   Collections.singleton(2)
            )
        );
    }

    @Test
    public void containsAllTrue() {
        assertTrue(
            containsAll(
                Collections.singleton(1)
            ,   Collections.singleton(1)
            )
        );
    }

    @Test
    public void coverage() throws Exception {
        Constructor<Utils> c = Utils.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }

}
