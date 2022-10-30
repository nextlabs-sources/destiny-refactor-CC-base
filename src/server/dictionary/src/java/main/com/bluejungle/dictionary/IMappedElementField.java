/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/IMappedElementField.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * This interface adds the getMapping method to let HQL formatter
 * process built-in and user-defined element fields in the same way.
 */
public interface IMappedElementField extends IElementField {
    /**
     * Obtains the name of the field to which this attribute is mapped.
     * @return the name of the field to which this attribute is mapped.
     */
    String getMapping();

}
