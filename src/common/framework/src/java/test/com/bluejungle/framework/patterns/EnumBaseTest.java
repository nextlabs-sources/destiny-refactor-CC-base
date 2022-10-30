/*
 * Created on Feb 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.patterns;

import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.expressions.PredicateConstants;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/patterns/EnumBaseTest.java#1 $:
 */

public class EnumBaseTest extends BaseDestinyTestCase {
    
    public final void testGetElementString() {
        String name = PredicateConstants.TRUE.getName();
        assertTrue("Predicate named TRUE should exist", PredicateConstants.existsElement(name));
        PredicateConstants t = PredicateConstants.getElement(name);
        assertSame("Predicate named TRUE should be the same as TRUE", PredicateConstants.TRUE, t);

        name = PredicateConstants.FALSE.getName();
        assertTrue("Predicate named FALSE should exist", PredicateConstants.existsElement(name));
        t = PredicateConstants.getElement(name);
        assertSame("Predicate named FALSE should be the same as FALSE", PredicateConstants.FALSE, t);
    }
    
    public final void testGetElementInt() {
        int type = PredicateConstants.TRUE.getType();
        assertTrue("Predicate with type TRUE should exist", PredicateConstants.existsElement(type));
        PredicateConstants t = PredicateConstants.getElement(type);
        assertSame("Predicate with type TRUE should be the same as TRUE", PredicateConstants.TRUE, t);

        type = PredicateConstants.FALSE.getType();
        assertTrue("Predicate with type FALSE should exist", PredicateConstants.existsElement(type));
        t = PredicateConstants.getElement(type);
        assertSame("Predicate with type FALSE should be the same as FALSE", PredicateConstants.FALSE, t);
    }
}
