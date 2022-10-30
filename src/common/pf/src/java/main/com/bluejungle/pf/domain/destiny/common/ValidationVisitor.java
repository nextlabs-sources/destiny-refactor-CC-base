package com.bluejungle.pf.domain.destiny.common;

import java.util.IdentityHashMap;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/common/ValidationVisitor.java#1 $
 */

/**
 * Checks the predicate for circular references.
 * Throws a <code>CircularReferenceException</code> exception when a predicate is invalid. 
 */
public class ValidationVisitor extends DefaultPredicateVisitor {

    public static class CircularReferenceException extends RuntimeException {
        private CircularReferenceException() {
            super("Predicate has an internal circular reference");
        }
    }

    private static final IdentityHashMap seen = new IdentityHashMap();

    /**
     * 
     * @see com.bluejungle.framework.expressions.IPredicateVisitor#visit(com.bluejungle.framework.expressions.ICompositePredicate, boolean)
     */
    public void visit(ICompositePredicate pred, boolean preorder) {
        if ( preorder ) {
            if ( seen.containsKey( pred ) ) {
                throw new CircularReferenceException();
            }
            seen.put( pred, null );
        } else {
            seen.remove( pred );
        }
    }

    /**
     * Prepares the visitor for the next use.
     * Classes that reuse instances of this visitor
     * must call this method between invocations.
     */
    public void reset() {
        seen.clear();
    }

}
