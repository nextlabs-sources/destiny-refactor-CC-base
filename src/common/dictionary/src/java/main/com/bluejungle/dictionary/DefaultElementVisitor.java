package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/DefaultElementVisitor.java#1 $
 */

/**
 * This is the default implementation of <code>IElementVisitor</code>.
 * All of its methods do nothing. This class is useful when you need to
 * override a subset of methods, leaving the remaining methods unimplemented.
 */
public class DefaultElementVisitor implements IElementVisitor {

    /**
     * @see IElementVisitor#visitGroup(IMGroup)
     */
    public void visitGroup( IMGroup group ) {
    }

    /**
     * @see IElementVisitor#visitLeaf(IMElement)
     */
    public void visitLeaf( IMElement element ) {
    }

    /**
     * @see IElementVisitor#visitProvisionalReference(IReferenceable)
     */
    public void visitProvisionalReference( IReferenceable ref ) {
    }

}
