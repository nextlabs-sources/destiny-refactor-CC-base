/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IMElementBase.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * This is the base interface for the <code>IMGroup</code> and
 * <code>IMElement</code> interfaces. It defines the operations for
 * getting external and internal keys from elements.
 */
public interface IMElementBase extends IElementBase {

    /**
     * This method lets users set the path of this item.
     * @param path the new <code>DictionaryPath</code> of the item.
     * @throws NullPointerException if the path is null.
     * @throws IllegalArgumentException if the path is empty
     * or any of its elements is an empty string.
     * @return true if the path is changed.
     */
    boolean setPath(DictionaryPath path);

    /**
     * This method lets users set the external key of this item.
     * The key may not be null.
     * @param key the new external key of this item.
     * @throws NullPointerException if the key is null.
     * @return true if the external key is changed.
     */
    boolean setExternalKey(DictionaryKey key);

    /**
     * This method lets users set the display name of this item.
     * The name may be empty or null.
     * @param displayName the new display name of this item.
     * @return true if the display name is changed.
     */
    boolean setDisplayName(String displayName);

    /**
     * This method lets users set the unique name of this item.
     * The name may be empty or null. No uniqueness check is performed
     * on the value of the uniqueName parameter.
     * @param uniqueName the new unique name of this item.
     * @return true if the unique name is changed.
     */
    boolean setUniqueName(String uniqueName);

}
