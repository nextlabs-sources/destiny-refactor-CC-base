package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IGroup.java#1 $
 */

/**
 * This interface defines a contract for groups in the dictionary.
 * Groups may be used in predicates to restrict search
 * to a specific group.
 *
 * @author sergey
 */

import com.bluejungle.framework.expressions.IPredicate;

public interface IGroup extends IElementBase {

    /**
     * Returns the name of this group.
     * @return the name of this group.
     */
    String getName();

    /**
     * Implementations of this method provide a way to query
     * for a direct membership in this group.
     *
     * @return an <code>IPredicate</code> for querying for
     * a direct membership in this group.
     */
    IPredicate getDirectMembershipPredicate();

    /**
     * Implementations of this method provide a way to query
     * for a transitive membership in this group.
     *
     * @return an <code>IPredicate</code> for querying for
     * a transitive membership in this group.
     */
    IPredicate getTransitiveMembershipPredicate();

}
