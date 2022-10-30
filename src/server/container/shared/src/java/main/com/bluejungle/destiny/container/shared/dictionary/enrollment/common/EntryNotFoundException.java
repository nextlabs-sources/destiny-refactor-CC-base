/*
 * Created on Feb 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * When enrollment or column can't be found
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EntryNotFoundException.java#1 $
 */

public class EntryNotFoundException extends EnrollmentException {

    /**
     * Constructor
     * @param arg0
     */
    public EntryNotFoundException(String type, String name) {
        super(String.format("The %s, %s, doesn't exist. Please supply a correct value.", type, name));
    }
}
