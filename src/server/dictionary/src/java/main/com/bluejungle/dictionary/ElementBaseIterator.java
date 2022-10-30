/*
 * Created on Nov 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/ElementBaseIterator.java#1 $
 */

public class ElementBaseIterator<E extends IMElementBase> extends DictionaryIterator<E>{
    
    public ElementBaseIterator(ScrollableResults rs, Session session, Transaction transaction,
            Dictionary dictionay) throws HibernateException {
        super(rs, session, transaction, dictionay);
    }
    
    @Override
    public E next() throws DictionaryException {
        return next(true);
    }
    
    public E next(boolean normalize) throws DictionaryException {
    	normalize = false;

    	E res = super.next();
        
        final Enrollment enrollment = (Enrollment)res.getEnrollment();
        enrollment.setDictionary(dictionary);
        
        //only do the normalization for user
        if (normalize) {
            dictionary.concatenate(res);
        }        
        
        return res;
    }

}
