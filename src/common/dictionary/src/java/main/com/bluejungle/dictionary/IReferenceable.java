package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IReferenceable.java#1 $
 */

/**
 * This interface defines common functionality for all
 * objects that can be members of enumerated groups
 * (i.e. elements, groups, and provisional references).
 * There are two things users can do with an <code>IReference</code> -
 * they can get its <code>DictionaryPath</code>, and pass it an
 * <code>IElementVisitor</code> to determine the actual type of
 * the <code>IReference</code>.
 * <code>IEnrollment</code> returns <code>Collection</code>s of
 * <code>IReferenceable</code> objects when you request provisional
 * references. <code>IMGroup</code> of enumerated type take
 * <code>IReferenceable</code> objects in their child manipulation
 * methods. However, methods that return group members always ignore
 * provisional references.
 */

public interface IReferenceable {

    /**
     * This method lets dictionary users obtain the path to the
     * given item in the dictionary.
     * @return the path to the given item in the dictionary.
     */
    DictionaryPath getPath();

    /**
     * Accepts the visitor for visiting.
     * @param visitor the visitor on which to call the corresponding
     * visit[...] method.
     */
    void accept(IElementVisitor visitor);

}
