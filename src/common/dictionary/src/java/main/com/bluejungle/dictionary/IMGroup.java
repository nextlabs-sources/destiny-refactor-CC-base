package com.bluejungle.dictionary;

import java.util.Date;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IMGroup.java#1 $
 */

/**
 * This interface defines the contract for mutable groups.
 */

public interface IMGroup extends IGroup, IMElementBase {

    /**
     * Sets the new name for this group.
     * @param name the new name for this group.
     */
    void setName(String name);

    /**
     * Lets the caller to iterate over a set of all groups
     * that are direct or indirect children of this group.
     *
     * @return an <code>IGroupIterator</code> for iterating over
     * all direct child groups of the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getAllChildGroups() throws DictionaryException;

    /**
     * Lets the caller to iterate over a set of all groups
     * that are direct children of this group.
     *
     * @return an <code>IGroupIterator</code> for iterating over
     * all direct child groups of the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getDirectChildGroups() throws DictionaryException;

    /**
     * Lets the caller to iterate over a set of all leaf elements
     * that are direct or indirect children of this group.
     *
     * @return an <code>IElementIterator</code> for iterating over
     * all direct and indirect child elements of the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    IDictionaryIterator<IMElement> getAllChildElements() throws DictionaryException ;

    /**
     * Lets the caller to iterate over a set of all leaf elements
     * that are direct children of this group.
     *
     * @return an <code>IElementIterator</code> for iterating over
     * all direct child elements of the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    IDictionaryIterator<IMElement> getDirectChildElements() throws DictionaryException ;
    
    /**
     * 
     * @return
     * @throws DictionaryException
     */
    IDictionaryIterator<DictionaryPath> getAllReferenceMemebers() throws DictionaryException;

    /**
     * Adds a child element to the group. The element may be a leaf
     * or another group.
     *
     * @param element the <code>IReferenceable</code> to be added to the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    void addChild(IReferenceable element )throws DictionaryException;

    /**
     * Removes a child element from the group. The element may be a leaf
     * or another group.
     *
     * @param element the <code>IReferenceable</code> to be removed from the group.
     * @throws DictionaryException if the operation cannot complete.
     */
    void removeChild(IReferenceable element ) throws DictionaryException;

    /**
     * Reports the changes in group membership between the two specific dates.
     * This API returns only changes in membership for leaf elements
     * of the specified type. IDs of added or removed groups are not returned.
     * Each ID is returned only once, even if an element has been added, removed,
     * and then added again during the specified time interval. In such cases
     * the element is reported as both an addition and a removal.
     *
     * @param type The type of leaf elements to return.
     * @param startDate the changes retruned based on this condition
     * will be as of this date, inclusive.
     * @param endDate the changes returned will be up to this date,
     * exclusive.
     * @return the changes in group membership between the two specific dates.
     * @throws DictionaryException if the operation cannot complete.
     * TODO (sergey) Remove this API when we start using views for reporting.
     */
    IGroupChanges getChanges(IElementType type, Date startDate, Date endDate) throws DictionaryException;

}
