/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/IDictionaryPredicate.java#1 $
 */

package com.bluejungle.dictionary;


/**
 * All dictionary-specific predicates implement this interface.
 * It provides a way to visit dictionary predicates in a type-safe way.
 */
interface IDictionaryPredicate {

    /**
     * Implementations of this method call back a type-specific
     * method of the visitor.
     * @param visitor the visitor object on which to call
     * type-specific methods.
     */
    void accept(IDictionaryPredicateVisitor visitor);

}
