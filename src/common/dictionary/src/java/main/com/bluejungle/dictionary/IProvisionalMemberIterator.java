/*
 * Created on Sep 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IProvisionalMemberIterator.java#1 $
 */

public interface IProvisionalMemberIterator {


    /**
     * This is a type-safe version of <code>Iterator</code>'s
     * next() method.
     *
     * @return The next element of this iterator. Calling this method
     * when hasNext() returned false results in an exception.
     */
    DictionaryPath next() throws DictionaryException;

    /**
     * Closes the iterator and releases the resources
     * if it is necessary.
     */
    void close() throws DictionaryException;

    /**
     * Checks if the iterator's sequence has more elements.
     * @return true if there are more elements to be retruned
     * from the iterator; false otherwise.
     * @throws DictionaryException if an error in the underlying
     * implementation does not let the method run to completion.
     */
    boolean hasNext() throws DictionaryException;

}

