package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IGroupChanges.java#1 $
 */

/**
 * Implementations of this interface return changes
 * in group membership for a specific group
 */
public interface IGroupChanges {

    /**
     * Returns an <code>IDictionaryIterator<Long></code> with keys
     * of elements that have been added to the group.
     * @return an <code>IDictionaryIterator<Long></code> with keys
     * of elements that have been added to the group.
     */
    IDictionaryIterator<Long> getKeysOfAddedMembers();

    /**
     * Returns an <code>IDictionaryIterator<Long></code> with keys
     * of elements that have been removed from the group.
     * @return an <code>IDictionaryIterator<Long></code> with keys
     * of elements that have been removed from the group.
     */
    IDictionaryIterator<Long> getKeysOfRemovedMembers();

    /**
     * Closes the iterators and releases their resources
     * if it is necessary.
     */
    void close() throws DictionaryException;

}
