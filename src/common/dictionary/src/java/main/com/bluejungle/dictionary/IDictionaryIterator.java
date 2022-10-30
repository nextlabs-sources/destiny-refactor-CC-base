/*
 * Created on Nov 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IDictionaryIterator.java#1 $
 */

public interface IDictionaryIterator<E> {
    /**
     * 
     * @return true if the iteration has more elements.
     * @throws DictionaryException if an error in the underlying
     *         implementation does not let the method run to completion.
     */
    boolean hasNext() throws DictionaryException;

    /**
     * 
     * @return the next element in the iteration.
     * 
     */
    E next() throws DictionaryException;
    
    /**
     * Release resources immediately.
     */
    void close() throws DictionaryException;
    
    
}
