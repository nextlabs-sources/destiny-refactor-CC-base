/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/IIndirectUpdate.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * This interface defines the contract for dictionary elements
 * to supply information about other elements that need to be
 * updated as the result of making an update to the current
 * element. For example, renaming a structural group requires
 * an update to its child elements and groups.
 */
public interface IIndirectUpdate {

    /**
     * This method determines if this update is independent of another
     * update. The rules for isIndependentOf relation must be as follows:
     * - isIndependentOf is symmetric:
     *   if A isIndependentOf B, then B isIndependentOf A.
     * - isIndependentOf is non-transitive and non-reflexive.
     * - isIndependentOf assumes independence of unknown update types.
     *
     * @param other The other update object.
     * @return true if this update is independent of the <code>other</code>
     * update; false otherwise.
     */
    boolean isIndependentOf(IIndirectUpdate other);

    /**
     * Runs the update against the specified session, and maintains
     * the <code>Map</code> of updated objects. This method is called
     * before the save operation.
     *
     * If an object the update needs to alter is in the updated set,
     * then the object from the set is used. Otherwise, the object
     * that needs to be altered is added to the <code>Map</code>.
     *
     * @param session the Hibernate session from which to retrieve
     * the additional objects to be updated.
     * @param updatedObjects a <code>Map</code> of objects
     * that need to be updated. The same object is always used as the key
     * and as the value in this <code>Map</code>.
     * @param now the <code>Date</code> as of which to execute queries
     * if necessary.
     *
     * @throws HibernateException if an operation fails.
     */
    void preSaveExecute(Session session, Map<IElementBase,IElementBase> updatedObjects, Date now) throws HibernateException;

    /**
     * Runs the update against the specified session after other objects
     * in this save request have already been saved.
     *
     * @param session the Hibernate session from which to retrieve
     * the additional objects to be updated.
     * @param leavesToClose a <code>Set</code> of element link IDs
     * that need to be closed. All objects in this <code>Set</code>
     * are of type <code>Long</code>.
     * @param groupsToClose a <code>Set</code> of group link IDs
     * that need to be closed. All objects in this <code>Set</code>
     * are of type <code>Long</code>.
     * @param otherToUpdate a <code>Set</code> with other related objects
     * that may need to be saved.
     * @param now the <code>Date</code> as of which to execute queries
     * if necessary.
     *
     * @throws HibernateException if an operation fails.
     */
    void postSaveExecute(Session session, Set<Long> leavesToClose, Set<Long> groupsToClose, Set<Long> refsToRemove, Set<Object> otherToUpdate, Date now) throws HibernateException;

}
