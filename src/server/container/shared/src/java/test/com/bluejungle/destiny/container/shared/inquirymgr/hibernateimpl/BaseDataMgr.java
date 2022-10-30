/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Iterator;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * This is the base data manager class. Other data managers extend it and reuse
 * the basic data operations.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/BaseDataMgr.java#1 $
 */

abstract class BaseDataMgr {

    /**
     * Saves the objects places in the set
     * 
     * @param s
     *            Hibernate session to use
     * @param objects
     *            objects to save
     */
    protected static void saveOjects(Session s, Set objects) throws HibernateException {
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            s.save(it.next());
        }
    }
}