/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.util.Collection;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/bluejungle/dictionary/EnrollmentSessionMock.java#1 $
 */

public class EnrollmentSessionMock implements IEnrollmentSession{
    private boolean hasActiveTrnsaction = false;
    
    public void beginTransaction() throws DictionaryException {
        hasActiveTrnsaction = true;
    }

    public void close(boolean success, String errorMessage) throws DictionaryException {
        hasActiveTrnsaction = false;
    }

    public void commit() throws DictionaryException {
        hasActiveTrnsaction = false;
    }

    public void deleteElements(Collection<? extends IElementBase> elements)
            throws DictionaryException {
        //do nothing
    }

    public boolean hasActiveTransaction() {
        return hasActiveTrnsaction;
    }

    public void rollback() throws DictionaryException {
        hasActiveTrnsaction = false;
    }

    public void saveElements(Collection<? extends IElementBase> elements)
            throws DictionaryException {
        //do nothing
    }

}
