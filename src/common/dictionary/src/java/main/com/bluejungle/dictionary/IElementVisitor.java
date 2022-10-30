package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IElementVisitor.java#1 $
 */

/**
 * This interface defines a contract for visiting dictionary
 * elements of different types, allowing to dispatch on their
 * type in a type-safe way.
 */
public interface IElementVisitor {

    /**
     * Visits a leaf element.
     * @param element the leaf element to visit.
     */
    void visitLeaf(IMElement element);

    /**
     * Visits a group.
     * @param group the group to visit.
     */
    void visitGroup(IMGroup group);

    /**
     * Visits a provisional reference.
     * @param ref the provisional reference to visit.
     */
    void visitProvisionalReference(IReferenceable ref);

}
