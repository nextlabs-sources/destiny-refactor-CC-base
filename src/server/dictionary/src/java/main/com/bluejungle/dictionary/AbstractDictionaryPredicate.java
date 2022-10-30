/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/AbstractDictionaryPredicate.java#1 $
 */

package com.bluejungle.dictionary;

import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IPredicateVisitor.Order;

/**
 * This is the base class for all dictionary predicates,
 * the anonymous classes implementing the 
 * <code>accept(IDictionaryPredicateVisitor)</code> method.
 */
abstract class AbstractDictionaryPredicate implements IPredicate, IDictionaryPredicate {

    /**
     * @see IPredicate#match(IArguments)
     */
    public boolean match(IArguments arguments) {
        throw new UnsupportedOperationException("dictionary predicates do not support the match operation.");
    }

    /**
     * @see IPredicate#accept(IPredicateVisitor, IPredicateVisitor.Order)
     */
    public void accept(IPredicateVisitor visitor, Order order) {
        visitor.visit(this);
    }

}
