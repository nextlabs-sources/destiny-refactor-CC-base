/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/IDictionaryPredicateVisitor.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;

/**
 * This interface defines a way to visit dictionary predicates
 * using the visitor pattern.
 */
interface IDictionaryPredicateVisitor {

    /**
     * The visit method called back by the predicates
     * constraining by an enumerated group membership,
     * direct and indirect.
     * @param group the <code>EnumeratedGroup</code> to membership of
     * which to constraint the results of the query.
     */
    void visitTransitiveMembership(EnumeratedGroup group);

    /**
     * The visit method called back by the predicates
     * constraining by an enumerated group direct membership.
     * @param group the <code>EnumeratedGroup</code> to membership of
     * which to constraint the results of the query.
     */
    void visitDirectMembership(EnumeratedGroup group);

    /**
     * The visit method called back by the predicates
     * constraining by a <code>DictionaryPath</code> and allowing
     * elements with direct or indirect subpaths to be included.
     * @param path the <code>DictionaryPath</code> to which this predicate
     * constraints the result. The path may contain wildcard characters
     * such as '%' and '_'.
     * @param direct a flag indicating whether only direct (<code>true</code>)
     * or both direct and indirect (<code>false</code>) elements
     * need to be included.
     */
    void visitDictionaryPath(DictionaryPath path, boolean direct);

    /**
     * The visit method called back by the predicates
     * constraining by an <code>ElementType</code>.
     * @param type the <code>ElementType</code> to which to constraint
     * the results of the query.
     */
    void visitElementType(ElementType type);

    /**
     * The visit method called back by the predicates
     * constraining by an <code>Enrollment</code>.
     * @param type the <code>Enrollment</code> to which to constraint
     * the results of the query.
     */
    void visitEnrollment(Enrollment enrollment);

    /**
     * The visit method called back by the predicates
     * constraining by <code>Date</code>s between which an entity
     * has been changed.
     * @param startDate represents the earliest <code>Date</code>
     * as of which the changes should be considered, inclusive.
     * @param endDate represents the latest <code>Date</code> as of
     * which the changes should be considered, exclusive.
     */
    void visitChangedCondition(Date startDate, Date endDate);

}
