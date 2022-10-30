package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/Utils.java#1 $
 */

import java.util.Collection;
import java.util.Iterator;

/**
 * This is a collection of utility methods used for parameter checking,
 * equality checking, etc.
 *
 * @author Sergey Kalinichenko
 */
final class Utils {

    /**
     * Private constructor prevents instantiation.
     */
    private Utils() {
    }

    /**
     * Compares the content of two same-type iterators for equality.
     *
     * @param li the left-hand side iterator.
     * @param ri the right-hand side iterator.
     * @return true if iterators have the same number of elements,
     * and elements are pairwise identical.
     */
    public static <T>
    boolean compareIterables(Iterable<T> li, Iterable<T> ri) {
        if (li == null && ri == null) {
            return true;
        }
        if (li == null || ri == null) {
            return false;
        }
        Iterator<T> lIter = li.iterator();
        Iterator<T> rIter = ri.iterator();
        while (lIter.hasNext() && rIter.hasNext()) {
            T lhs = lIter.next();
            T rhs = rIter.next();
            if (((lhs == null) != (rhs == null))
              || (lhs != null && !lhs.equals(rhs))) {
                return false;
            }
        }
        return lIter.hasNext() == rIter.hasNext();
    }

    /**
     * Extends Collection.containsAll definition to Iterables.
     *
     * @param <T> the type of collection element.
     * @param c the collection to check.
     * @param i the iterable - all its values must be in the collection
     * for this method to return 'true'.
     * @return true if all values from the iterable are in the collection;
     * false otherwise.
     */
    public static <T> boolean containsAll(Collection<T> c, Iterable<T> i) {
        for (T v : i) {
            if (!c.contains(v)) {
                return false;
            }
        }
        return true;
    }

}
