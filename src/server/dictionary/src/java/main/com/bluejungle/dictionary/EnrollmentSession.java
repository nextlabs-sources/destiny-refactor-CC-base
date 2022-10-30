/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnrollmentSession.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.usertypes.BinaryAsString;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.DisjointSets;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.Table;

/**
 * This is an implementation of the <code>IEnrollmentSession</code>.
 */
class EnrollmentSession extends DictionarySession implements IEnrollmentSession {
    /**
     * The dictionary logger.
     */
    private static final Log LOG = LogFactory.getLog(Dictionary.class);

    /**
     * The maximum number of connected groups.
     */
    private static final int MAX_CONNECTED_GROUPS = 1024;
    
    /**
     * The enrollment for which this session is created.
     */
    private final Enrollment enrollment;

    /**
     * The update record for this enrollment session.
     */
    private final UpdateRecord updateRecord;

    private final Collection<IElementBase> deletedElements;

    /**
     * Creates an enrollment session with the specified enrollment.
     * @param session the Hibernate session.
     * @param enrollment the enrollment.
     */
    EnrollmentSession( Dictionary dictionary, Session session, Enrollment enrollment ) {
        super(dictionary, session);
        this.enrollment = enrollment;
        updateRecord = new UpdateRecord(enrollment);
        deletedElements = new LinkedList<IElementBase>();
        LOG.debug("Opening an enrollment session.");
    }

    
    /**
     * @see IEnrollmentSession#saveElements(Collection)
     */
    public void saveElements(Collection<? extends IElementBase> elements) throws DictionaryException {
    	final boolean isDebug = LOG.isDebugEnabled();
        LOG.info("Saving elements. Element count = " + elements.size());

        checkNull(elements, "elements");
        if (elements.isEmpty()) {
            return;
        }
        // Collect all update requests and verify that
        // all updates are independent of each other.
        final Set<IIndirectUpdate> indirectUpdates = new HashSet<IIndirectUpdate>();
        final List<EnumeratedGroup> savedEnumGroups = new ArrayList<EnumeratedGroup>(elements.size());
        
        // The strange size has no magical meaning - feel free to try other sizes.
        // Hash tables perform best when their size is
        Map<IElementBase,IElementBase> allToUpdate = new HashMap<IElementBase,IElementBase>(4*elements.size()+11);
        
        collectUpdateRequests(elements, indirectUpdates, savedEnumGroups, allToUpdate);

        checkActiveTransaction();
        final Date now = updateRecord.getStartTime();
        Session hs = getSession();
        TimeRelation tr = new TimeRelation(now, UnmodifiableDate.END_OF_TIME);
        try {
            // Perform the pre-save updates
            for (IIndirectUpdate update : indirectUpdates) {
                update.preSaveExecute(hs, allToUpdate, now);
            }

            List<DictionaryElementBase> toSave = new ArrayList<DictionaryElementBase>(allToUpdate.size());
            List<Long> toUpdateIds = new ArrayList<Long>(allToUpdate.size());

            Map<DictionaryPath,DictionaryElementBase> unsavedByPath =
                new HashMap<DictionaryPath,DictionaryElementBase>(4*allToUpdate.size()+11);
            
            collectUpdatesAndUnsavedByPathElements(allToUpdate, now, hs, tr, toSave, toUpdateIds, unsavedByPath);
            collectElementsToSaveFromUnsavedByPath(now, hs, toSave,	unsavedByPath);

            // At this point, unsavedByPath contains only new values; save them,
            // and copy their newly created ID to the originalId field.
            for (DictionaryElementBase element : unsavedByPath.values()) {
                element.setOriginalId((Long)hs.save(element));
            }
            hs.flush();

            updateElementEndTime(toUpdateIds, now);

            Set<Long> leavesToClose = new HashSet<Long>();
            Set<Long> groupsToClose = new HashSet<Long>();
            Set<Long> refsToRemove = new HashSet<Long>();
            Set<Object> otherToSave = new HashSet<Object>();
            
            // Perform the post-save updates
            handlePostSaveUpdates(indirectUpdates, now, hs, leavesToClose, groupsToClose, refsToRemove, otherToSave);

            // Get the reachable set of groups for which we need to
            // recalculate the transitive closure links
            Set<Long> enumGroupIds = new HashSet<Long>();
            for (IElementBase eg : savedEnumGroups) {
                enumGroupIds.add(eg.getInternalKey());
            }

            Collection<Set<Long>> reachable = new ArrayList<Set<Long>>(enumGroupIds.size());
            Set<Long> allReachable = new HashSet<Long>();
            for(Long enumGroupId : enumGroupIds){
                if (allReachable.contains(enumGroupId)) {
                    continue;
                }
                Set<Long> thisReachable = getReachableGroups(hs, Collections.singleton(enumGroupId), now); 
                reachable.add(thisReachable);
                allReachable.addAll(thisReachable);
            }
            
            // Set<Long> reachable = getReachableGroups(hs, enumGroupIds, now);
            
            // Change the direct links to correspond to the modifications
            // the callers performed on the set of enumerated groups:

            int updatedRow;
            
            if (!leavesToClose.isEmpty()) {
            	updatedRow = MassDML.updateOrDelete(
                hs
            ,   "UPDATE DICT_ENUM_MEMBERS SET ACTIVE_TO=? WHERE ID IN #"
            ,   new Object[] {now}
            ,   new Type[] {DateToLongUserType.TYPE}
            ,   leavesToClose
            ,   Hibernate.LONG);
            	if (isDebug) {
	            	LOG.debug("Updated " + updatedRow + " DICT_ENUM_MEMBERS in"
							+ CollectionUtils.asString(leavesToClose, ","));
            	}
            }

            if (!groupsToClose.isEmpty()) {
            	updatedRow = MassDML.updateOrDelete(
                hs
            ,   "UPDATE DICT_ENUM_GROUP_MEMBERS SET ACTIVE_TO=? WHERE ID IN #"
            ,   new Object[] {now}
            ,   new Type[] {DateToLongUserType.TYPE}
            ,   groupsToClose
            ,   Hibernate.LONG);
            	if (isDebug) {
	            	LOG.debug("Updated " + updatedRow + " DICT_ENUM_GROUP_MEMBERS in"
						+ CollectionUtils.asString(groupsToClose, ","));
            	}
            }

            if(!refsToRemove.isEmpty()){
            	updatedRow = MassDML.updateOrDelete(
                hs
            ,   "DELETE FROM DICT_ENUM_REF_MEMBERS WHERE ID IN #"
            ,   null
            ,   null
            ,   refsToRemove
            ,   Hibernate.LONG
            );
            	if (isDebug) {
	            	LOG.debug("Deleted " + updatedRow + " DICT_ENUM_REF_MEMBERS in"
						+ CollectionUtils.asString(refsToRemove, ","));
            	}
            }
            
            // save the new elements with known original IDs
            // save the updated elements before materialize the references
            for (DictionaryElementBase e : toSave) {
                hs.save(e);
                e.saveComplete();
            }

            // At this point, certain forward references may have
            // "materialized," so we need to replace the reference
            // links with the corresponding group or element links.
            // Two separate inserts are necessary - for groups and
            // for elements, since the target tables are slightly different.

            updatedRow = MassDML.insert(
                hs
            ,   "DICT_ENUM_GROUP_MEMBERS("
            +   "    $id$VERSION,FROM_ID,TO_ID,ENROLLMENT_ID,IS_DIRECT,ACTIVE_FROM,ACTIVE_TO)"
            ,   String.format(
                    "$hibernate_sequence$f0,f1,f2,f3,f4,%d,%d"
                ,   tr.getActiveFrom().getTime()
                ,   tr.getActiveTo().getTime()
                )
            ,   "(   SELECT DISTINCT 0 AS f0, r.group_id AS f1, e.ORIGINAL_ID AS f2, e.ENROLLMENT_ID AS f3, 'Y' AS f4"
            +   "    FROM DICT_ELEMENTS e"
            +   "    INNER JOIN DICT_ENUM_GROUPS g ON e.ID = g.ELEMENT_ID"
            +   "    INNER JOIN DICT_ENUM_REF_MEMBERS r ON r.PATH_HASH=e.PATH_HASH AND r.PATH=e.PATH"
            +   "    WHERE e.ACTIVE_FROM <= ? AND e.ACTIVE_TO > ?) x"
            ,   new Object[] {now, now}
            ,   new Type[] {DateToLongUserType.TYPE, DateToLongUserType.TYPE} );
            if (isDebug) {
            	LOG.debug("Updated " + updatedRow + " DICT_ENUM_GROUP_MEMBERS");
            }

            updatedRow = MassDML.insert(
                hs
            ,   "DICT_ENUM_MEMBERS ("
            +   "    $id$VERSION,GROUP_ID,MEMBER_ID,ELEMENT_TYPE_ID,ENROLLMENT_ID,ACTIVE_FROM,ACTIVE_TO)"
            ,   String.format(
                    "$hibernate_sequence$f0,f1,f2,f3,f4,%d,%d"
                ,   tr.getActiveFrom().getTime()
                ,   tr.getActiveTo().getTime()
                )
            ,   "(   SELECT DISTINCT 0 AS f0, r.GROUP_ID AS f1, e.ORIGINAL_ID AS f2, l.TYPE_ID AS f3, e.ENROLLMENT_ID AS f4"
            +   "    FROM DICT_ELEMENTS e"
            +   "    INNER JOIN DICT_LEAF_ELEMENTS l ON e.ID = l.ELEMENT_ID"
            +   "    INNER JOIN DICT_ENUM_REF_MEMBERS r ON r.PATH_HASH=e.PATH_HASH AND r.PATH=e.PATH"
            +   "    WHERE e.ACTIVE_FROM <= ? AND e.ACTIVE_TO > ?) x"
            ,   new Object[] {now, now}
            ,   new Type[] {DateToLongUserType.TYPE, DateToLongUserType.TYPE} );
            if (isDebug) {
            	LOG.debug("Updated " + updatedRow + " DICT_ENUM_MEMBERS");
            }

            // Now that all "materialized" forward references are moved
            // to their correct tables, we can safely remove the records
            // representing these references.
            updatedRow = MassDML.updateOrDelete(
                hs
            ,   "DELETE FROM DICT_ENUM_REF_MEMBERS WHERE EXISTS ("
            +   "SELECT * FROM DICT_ELEMENTS e WHERE"
            +   "    e.PATH_HASH=DICT_ENUM_REF_MEMBERS.PATH_HASH "
            +   "AND e.PATH=DICT_ENUM_REF_MEMBERS.PATH "
            +   "AND e.ACTIVE_FROM <= ? AND e.ACTIVE_TO > ?)"
            ,   new Object[] {now, now}
            ,   new Type[] {DateToLongUserType.TYPE, DateToLongUserType.TYPE}
            ,   Collections.EMPTY_LIST
            ,   Hibernate.OBJECT	//this type is not used since the ids is empty.
            );
            if (isDebug) {
            	LOG.debug("Deleted " + updatedRow + " DICT_ENUM_REF_MEMBERS");
            }

            // The direct dependency graph is now complete.
            // Traverse the graph to get the closure of all nodes that
            // might need an update.

            rebuildTransitiveClosure(hs, reachable, now);

            // Signal the completion of save to the previously unsaved elements
            for (DictionaryElementBase element : unsavedByPath.values()) {
                element.saveComplete();
            }

        } catch ( HibernateException cause ) {
            LOG.error("Unable to save elements: "+cause.getMessage());
            throw new DictionaryException("Failed to save elements to database: " + cause.getMessage(), cause);
        } finally {
            LOG.trace("Finished saving elements");
        }
    }


	/**
	 * @param indirectUpdates
	 * @param now
	 * @param hs
	 * @param leavesToClose
	 * @param groupsToClose
	 * @param refsToRemove
	 * @param otherToSave
	 * @throws HibernateException
	 */
	private void handlePostSaveUpdates(
			final Set<IIndirectUpdate> indirectUpdates, final Date now,
			Session hs, Set<Long> leavesToClose, Set<Long> groupsToClose,
			Set<Long> refsToRemove, Set<Object> otherToSave)
			throws HibernateException {
		for (IIndirectUpdate update : indirectUpdates) {
		    update.postSaveExecute(hs, leavesToClose, groupsToClose, refsToRemove, otherToSave, now);
		}

		// Save any objects the update may have produced
		for (Object o : otherToSave) {
			hs.save(o);
		}
	}


	/**
	 * Collect Elements needing saved from the UnsavedByPath populated in collectUpdateRequests() method.
	 * @param now
	 * @param hs
	 * @param toSave
	 * @param unsavedByPath
	 * @throws HibernateException
	 * @throws DictionaryException
	 */
	private void collectElementsToSaveFromUnsavedByPath(final Date now,
			Session hs, List<DictionaryElementBase> toSave,
			Map<DictionaryPath, DictionaryElementBase> unsavedByPath)
			throws HibernateException, DictionaryException 
	{
		if (unsavedByPath.isEmpty()) 
			return;

		List<Object[]> refDatas = HibernateUtils.safeList(unsavedByPath.keySet(), hs,
	        new HibernateUtils.SafeQuery3<DictionaryPath, Long, Object[]>() {

	            protected String getQueryString(){
	                return "select b.id, b.originalId, b.path, b.keyData, b.enrollment.id "
	                + "from DictionaryElementBase b "
	                + "where b.path.pathHash in (:paths) "
	                + "and b.timeRelation.activeTo > :asOf "
	                + "and b.timeRelation.activeFrom <= :asOf";
	            }
	            
	            protected void setQuery(Query q, Collection<Long> values) 
	                    throws HibernateException{
	                q.setParameterList("paths", values)
	                 .setParameter("asOf", now, DateToLongUserType.TYPE);
	            }
	            
	            protected Long convert(DictionaryPath value) {
	                return new Long(value.hashCode());
	            }
	        }
	    );
	    
	    Collection<Long> duplicatedElementIds = new LinkedList<Long>();
	    for (Object[] refData : refDatas) {
	        Object refPath = refData[2];
	        if (!unsavedByPath.containsKey(refPath)) {
	            // We query by hash, so this may be a "stray" entry; ignore it.
	            continue;
	        }
	        Long refOrigId = (Long)refData[1];
	        Object refKey = refData[3];
	        if (refKey != null) {
	            if (refData[4].equals(enrollment.id)) {
	                duplicatedElementIds.add((Long) refData[0]);
	            }else{
	                throw new DictionaryException(
	                    "an attempt to insert a duplicate path is detected: "+refPath
	                );
	            }
	        }
	        DictionaryElementBase element = unsavedByPath.remove(refPath);
	        if (element != null) {
	            element.setOriginalId(refOrigId);
	            toSave.add(element);
	        }
	    }
	    
	    // find all existing element with new Dictionary key. 
	    // If the element new, they should not exist in database.
	    // Except there is one exception, the type is changed and the path is changed too
	    // So it passed the checking above.
	    List<byte[]> newDictionaryKeys = new ArrayList<byte[]>(unsavedByPath.size());
	    for (DictionaryElementBase element : unsavedByPath.values()) {
	        //don't get something that already known as duplicated.
		        if (!duplicatedElementIds.contains(element.getId())) {
		            newDictionaryKeys.add(element.getExternalKey().getKey());
		        }
		    }
		    
		    if(!newDictionaryKeys.isEmpty()){
		        List<Long> existingData = HibernateUtils.safeList(newDictionaryKeys, hs,
		            new HibernateUtils.SafeQuery<byte[], Long>() {
   
		                protected String getQueryString(){
		                    return "select b.id"
	                    + " from DictionaryElementBase b"
	                    + " where b.keyData in (:keyDatas)"
	                    + " and b.timeRelation.activeTo > :asOf"
	                    + " and b.timeRelation.activeFrom <= :asOf"
	                    + " and b.enrollment = :enrollment";
	                }
	                
	                protected void setQuery(Query q, Collection<byte[]> values) 
	                        throws HibernateException{
	                    q.setParameterList("keyDatas", values, BinaryAsString.TYPE)
	                     .setParameter("asOf", now, DateToLongUserType.TYPE)
	                     .setParameter("enrollment", enrollment)
	                    ;
	                }
	            }
	        );
	        duplicatedElementIds.addAll(existingData);
	    }
	    
	    if (!duplicatedElementIds.isEmpty()) {
	        Collection<IElementBase> duplicatedElements = HibernateUtils.safeList(
	                duplicatedElementIds, hs,
	            new HibernateUtils.SafeQuery<Long, IElementBase>() {
	                protected String getQueryString(){
	                    return "from DictionaryElementBase b where b.id in (:ids)";
	                }
	                
	                protected void setQuery(Query q, Collection<Long> values)
	                        throws HibernateException {
	                    q.setParameterList("ids", values);
	                }
	            }
	        );
	        deleteElements(duplicatedElements);
	    }

	}


	/**
	 * Collect Items to update and items to save by Path
	 * @param allToUpdate
	 * @param now
	 * @param hs
	 * @param tr
	 * @param toSave
	 * @param toUpdateIds
	 * @param unsavedByPath
	 * @throws IllegalArgumentException
	 * @throws HibernateException
	 * @throws DictionaryException
	 */
	private void collectUpdatesAndUnsavedByPathElements(
			Map<IElementBase, 
			IElementBase> allToUpdate, 
			final Date now,
			Session hs, 
			TimeRelation tr, 
			List<DictionaryElementBase> toSave,
			List<Long> toUpdateIds,
			Map<DictionaryPath, DictionaryElementBase> unsavedByPath)
		throws IllegalArgumentException, HibernateException, DictionaryException 
	{
		for (IElementBase elementBase : allToUpdate.values()) {
		    DictionaryElementBase element = (DictionaryElementBase)elementBase;
		    assert element != null; // This has been checked above
		    DictionaryElementBase original = element.getOriginal();
		    if ( original != null ) {
		        assert original.getTimeRelation() != null;
		        if ( original.getTimeRelation().getActiveTo().before(now) ) {
		            throw new IllegalArgumentException("Updating inactive elements is not permitted");
		        }
		        toUpdateIds.add(original.getId());
		        toSave.add(element);
		        hs.evict(element);
		        element.setTimeRelation(tr);
		    } else if ( element.isNew() ) {
		        if ( unsavedByPath.containsKey(element.getPath())) {
		            throw new DictionaryException(
		                "elements[] contains values with identical paths:" + element.getPath().toString()
		            );
		        }
		        unsavedByPath.put(element.getPath(), element);
		        element.setTimeRelation(tr);
		    } else {
		    	//the element is not new and didn't update
		        //do nothing
		    }
		   
		}
	}


	/**
	 * Helper method for saveElements.  Collect Update Requests that need saved.
	 * @param elements
	 * @param indirectUpdates
	 * @param savedEnumGroups
	 * @param allToUpdate
	 * @throws DictionaryException
	 * @throws IllegalArgumentException
	 */
	private void collectUpdateRequests(
			Collection<? extends IElementBase> elements,
			final Set<IIndirectUpdate> indirectUpdates,
			final List<EnumeratedGroup> savedEnumGroups,
			Map<IElementBase, IElementBase> allToUpdate)
		throws DictionaryException, IllegalArgumentException 
	{
		for (IElementBase elementBase : elements) {
            final DictionaryElementBase element = (DictionaryElementBase)elementBase;
            checkNull(element, "element[]");
            if (element instanceof EnumeratedGroup) {
                savedEnumGroups.add((EnumeratedGroup)element);
            }
            IIndirectUpdate update = element.getIndirectUpdate();
            if (update != null) {
                for (IIndirectUpdate other : indirectUpdates) {
                    if (!other.isIndependentOf(update)) {
                        throw new DictionaryException("Request contains dependent updates.");
                    }
                }
                indirectUpdates.add(update);
            }
            
            if ( !enrollment.equals(element.getEnrollment()) ) {
                throw new IllegalArgumentException("element[]");
            }
            allToUpdate.put(element, element);
        }
	}

    /**
     * @see IEnrollmentSession#deleteElements(Collection)
     */
    public void deleteElements(Collection<? extends IElementBase> elements) throws DictionaryException {
        checkNull(elements, "elements");
        LOG.debug("Deleting elements. Element count = "+elements.size());
        if (elements.isEmpty()) {
            return;
        }
        checkActiveTransaction();
        try {
            List<Long> toUpdateIds = new ArrayList<Long>(elements.size());
            Date now = updateRecord.getStartTime();
            Session hs = getSession();
            final Set<Long> enumGroupIds = new HashSet<Long>();
            for (IElementBase elementBase : elements) {
                DictionaryElementBase element = (DictionaryElementBase)elementBase;
                checkNull(element, "elements[]");
                if ( !enrollment.equals(element.getEnrollment() ) ) {
                    throw new IllegalArgumentException("elements[]");
                }
                if (element.isNew()) {
                    // Deleting a newly added element is a no-op
                    continue;
                }
                assert element.getTimeRelation() != null;
                if ( element.getTimeRelation().getActiveTo().before(now) ) {
                    LOG.info("Deleting inactive elements \""
							+ element.getInternalKey() + "\"is not permitted. The element active from "
							+ element.getTimeRelation().getActiveFrom() + " to "
							+ element.getTimeRelation().getActiveTo()
							+ ", now = " + now);
					continue;
                }
                toUpdateIds.add(element.getId());
                if (element instanceof EnumeratedGroup) {
                    enumGroupIds.add(element.getInternalKey());
                }
                deletedElements.add(elementBase);
            }

            
            updateElementEndTime(toUpdateIds, now);

//            Set<Long> reachable = getReachableGroups(hs, enumGroupIds, now);
            Collection<Set<Long>> reachable = new ArrayList<Set<Long>>(enumGroupIds.size());
            Set<Long> allReachable = new HashSet<Long>();
            for(Long enumGroupId : enumGroupIds){
                if (allReachable.contains(enumGroupId)) {
                    continue;
                }
                Set<Long> thisReachable = getReachableGroups(hs, Collections.singleton(enumGroupId), now); 
                reachable.add(thisReachable);
                allReachable.addAll(thisReachable);
            }
            
            if(!enumGroupIds.isEmpty()){
            MassDML.updateOrDelete(
                hs
            ,   "UPDATE DICT_ENUM_MEMBERS "
            +   "SET ACTIVE_TO=? "
            +   "WHERE "
            +   "ACTIVE_FROM <= ? AND ACTIVE_TO > ? AND GROUP_ID in #"
            ,   new Object[] {now, now, now}
            ,   new Type[] {DateToLongUserType.TYPE, DateToLongUserType.TYPE, DateToLongUserType.TYPE}
            ,   enumGroupIds
            ,   Hibernate.LONG
            );

            MassDML.updateOrDelete(
                hs
            ,   "UPDATE DICT_ENUM_GROUP_MEMBERS "
            +   "SET ACTIVE_TO=? "
            +   "WHERE ACTIVE_FROM <= ? AND ACTIVE_TO > ? "
            +   "AND (FROM_ID IN # OR TO_ID IN #)"
            ,   new Object[] {now, now, now}
            ,   new Type[] {DateToLongUserType.TYPE, DateToLongUserType.TYPE, DateToLongUserType.TYPE}
            ,   enumGroupIds
            ,   Hibernate.LONG
            );

            MassDML.updateOrDelete(
                hs
            ,   "DELETE FROM DICT_ENUM_REF_MEMBERS "
            +   "WHERE GROUP_ID IN #"
            ,   null
            ,   null
            ,   enumGroupIds
            ,   Hibernate.LONG
            );
            }

            rebuildTransitiveClosure(hs, reachable, now);

        } catch ( HibernateException cause ) {
            LOG.error("Unable to delete elements: "+cause.getMessage());
            throw new DictionaryException(cause);
        } finally {
            LOG.debug("Finished deleting elements");
        }
    }

    /**
     * This method updates the active-to time on multiple
     * dictionary element records (groups or leaf elements).
     *
     * @param ids a <code>Collection</code> of IDs of records to be updated.
     * @param date the as-of date to close the records.
     * @throws HibernateException if the operation cannot complete.
     */
    private void updateElementEndTime(Collection<Long> ids, Date date) throws HibernateException {
    	if (ids.isEmpty()) {
			return;
		}
        checkActiveTransaction();
        MassDML.updateOrDelete(
            getSession()
        ,   "UPDATE DICT_ELEMENTS SET ACTIVE_TO=? WHERE ID IN #"
        ,   new Object[] {date}
        ,   new Type[] {DateToLongUserType.TYPE}
        ,   ids
        ,   Hibernate.LONG);
    }

    /**
     * @see DictionarySession#beginTransaction()
     */
    public void beginTransaction() throws DictionaryException {
        LOG.debug("Beginning an enrollment transaction");
        super.beginTransaction();
    }

    /**
     * @see DictionarySession#commit()
     */
    public void commit() throws DictionaryException {
        LOG.debug("Committing an enrollment transaction");
        DictionaryException reThrow = null;
        try {
            super.commit();
            getSession().clear();
        } catch (DictionaryException caught) {
            reThrow = caught;
        }
        writeUpdateRecord(false, null);
        if ( reThrow != null ) {
            throw reThrow;
        }
    }

    /**
     * @see DictionarySession#rollback()
     */
    public void rollback() throws DictionaryException {
        LOG.debug("Rolling back an enrollment transaction");
        DictionaryException reThrow = null;
        try {
            super.rollback();
            getSession().clear(); // Nao: I need to clear the session in case there was exception in the previous Hibernte action.
			 					  // Without this, the following Hibernate action (e.g. writeUpdateRecord() in close()) would fail. 
        } catch (DictionaryException caught) {
            reThrow = caught;
        }
        if ( reThrow != null ) {
            throw reThrow;
        }
    }

    private String deletedElementsTable(){
    	Table table = new Table();
    	table.addColumn("Element ID", 10);
    	table.addColumn("Display Name", 20);
    	table.addColumn("Dictionary Path", 150);
    	
    	for(IElementBase e : deletedElements){
    		table.addRow(e.getInternalKey(), e.getDisplayName(), e.getPath());
    	}
    	return table.getOutput();
    }

    /**
     * @see IEnrollmentSession#close(boolean)
     */
    public void close(boolean success, String errorMessage) throws DictionaryException {
        LOG.debug("Closing an enrollment session: " + (success ? "success." : "failure."));
        DictionaryException rethrow = null;
        try {
        	writeUpdateRecord(success, errorMessage);
            if ( success ) {
            	((Dictionary)enrollment.getDictionary()).fireDictionaryChangeEvent();
            	if (!deletedElements.isEmpty()) {
					LOG.warn("The following entries have been deleted in the enrollment '"
							+ Enrollment.filterDomainName(enrollment.getDomainName())
							+ "'. Be sure to update any policies that rely on this enrollment."
							+ ConsoleDisplayHelper.NEWLINE
							+ deletedElementsTable());
            }
            	deletedElements.clear();
            }
        } catch (DictionaryException toRethrow) {
            if (success) {
                // If the close is a result of another error, it is possible that
                // the writeUpdateRecord also fails. We do not throw the new exception
                // here to avoid masking the original error by the "secondary" error.
                rethrow = toRethrow;
            }
        } finally {
            super.close();
        }
        
        if (rethrow != null) {
            throw rethrow;
        }
    }

    /**
     * Writes an update resord with the specified parameters.
     * @param success a flag indicating whether or not the record
     * should indicate a success.
     * @param errorMessage an optional error or status message
     * to include with the update record.
     * @throws DictionaryException if the operation fails due to
     * an RDBMS error (not thrown when success is <code>false</code>).
     */
    private void writeUpdateRecord(boolean success, String errorMessage) throws DictionaryException {
        LOG.debug("Writing an update record: "+(success ? "success" : "failure"));
        Transaction tx = null;
        try {
            Session hs = getSession();
            tx = hs.beginTransaction();
            updateRecord.setIsSuccessful(success);
            Date now = new Date();
            updateRecord.setEndTime(now);
            updateRecord.setErrorMessage(errorMessage);
            if (updateRecord.timeRelation == null) {
                // This is the first update - make a new TR...
                updateRecord.timeRelation = TimeRelation.open(now);
                // ...and close the currently active record.
                UpdateRecord toClose = (UpdateRecord)hs.createQuery(
                    "from UpdateRecord ur where"
                +   "    ur.enrollment=:enrollment "
                +   "and ur.timeRelation.activeFrom <= :now "
                +   "and ur.timeRelation.activeTo > :now"
                )
                .setParameter("enrollment", enrollment, Hibernate.entity(Enrollment.class))
                .setParameter("now", now, DateToLongUserType.TYPE)
                .uniqueResult();
                if (toClose != null) {
                    toClose.timeRelation = toClose.timeRelation.close(now);
                    hs.saveOrUpdate(toClose);
                }
            }
            hs.saveOrUpdate(updateRecord);
            tx.commit();
        } catch (HibernateException he) {
            if ( tx != null ) {
                try {
                    tx.rollback();
                } catch (HibernateException ignored) {
                }
            }
            throw new DictionaryException("Unable to write an update record", he);
        } finally {
            LOG.debug("Finished writing an update record");
        }
    }

    /**
     * Given a <code>Set</code> of IDs of <code>EnumeratedGroup</code>s
     * that were modified or deleted in a change, finds IDs of all other groups
     * that may need to be modified as the result of the change.
     *
     * @param hs the Hibernate session to use.
     * @param seedIds the <code>Set</code> of IDs of the
     * <code>EnumeratedGroup</code>s. This argument must not be null,
     * and may not contain nulls, otherwise an exception is thrown.
     * @param now The as-of time for getting the time.
     * @return a <code>Set</code> of IDs of all groups that may need to be
     * updated as the result of changing or deleting the <code>seedIds</code> groups.
     */
    private Set<Long> getReachableGroups(Session hs, Set<Long> seedIds, final Date now) throws HibernateException {
        Set<Long> res = new HashSet<Long>();
        DisjointSets<Long> connectedComponents = new DisjointSets<Long>();
        // Discover all groups by seeding the "border" and then
        // expanding it by repeatedly querying for both incoming
        // and outgoing edges of newly discovered vertices.
        // The worst-case number of queries is equal to the "depth"
        // of the most distant vertex from the seeded "border."
        Set<Long> border = new HashSet<Long>(seedIds);

        hs.flush();
        while (!border.isEmpty()) {
            List<Object[]> edges = HibernateUtils.safeList(border, hs, 
                    HibernateUtils.DATABASE_PARAMETER_LIMIT / 2, 
                    new HibernateUtils.SafeQuery<Long, Object[]>() {
                        protected String getQueryString(){
                            return "select gm.fromId, gm.toId "
                            +   "from EnumerationGroupMember gm "
                            +   "where gm.isDirect='Y'"
                            +   "  and gm.enrollmentId=:enrollmentId"
                            +   "  and gm.timeRelation.activeFrom <= :asOf"
                            +   "  and gm.timeRelation.activeTo > :asOf"
                            +   "  and (gm.fromId in (:border) or gm.toId in (:border))";
                        }
                        
                        protected void setQuery(Query q, Collection<Long> values)
                                throws HibernateException {
                            q.setParameter("asOf", now, DateToLongUserType.TYPE)
                             .setParameter("enrollmentId", enrollment.id)
                             .setParameterList("border", values);
                        }
                    }
            );
            //clean up for next shot
            border = new HashSet<Long>();
            
            for (Object[] edge : edges) {
                Long from = (Long)edge[0];
                Long to = (Long)edge[1];
                connectedComponents.union(from, to);
                
                if (connectedComponents.size(from) > MAX_CONNECTED_GROUPS
                        || connectedComponents.size(to) > MAX_CONNECTED_GROUPS) {
                    LOG.error(
                            "The size of the largest connected set of groups "
                        +   "exceeds the system limit of "+MAX_CONNECTED_GROUPS
                        +   ". Indirect group membership with nesting level of 3 "
                        +   "and above (i.e. grandchildren, grand-grandchildren, etc.) "
                        +   "may be ignored incorrectly for certain large groups."
                        );
                    //stop immediately if the size reaches the limit
                    break;
                }
                if (!res.contains(from)) {
                    res.add(from);
                    border.add(from);
                }
                if (!res.contains(to)) {
                    res.add(to);
                    border.add(to);
                }                    
            }
        }
        return res;
    }
    
    /**
     * How do we rebuild transitive closure
     * 
     * 1. Before we rebuild the closure, we need to calculate how large the graph is. 
     *      We need to get all vertices (nodes). {@link #getReachableGroups(Session, Set, Date)}
     * 2. Finding all direct paths. The direct paths are one-level only.
     * 3. Save and sort all direct paths into a graph which is a SortedMap. The key is the node. 
     *      The values are the nodes that the key can connect to.
     * 4. The directed graph is completed yet. We need to compute the transitive closure.
     * 5. Convert all the paths (directed and indirected) in a list of paired numbers.
     * 6. Get all the paths from database.
     * 7. Compare them, do either keep, delete or insert.
     * 
     * This method is slightly different than {@link #rebuildTransitiveClosure(Session, Set, Date)}.
     * This treats each vertices set as independence. 
     * The other one treats the whole set of vertices as one big graph.
     * 
     * This method will reduce the size of the graph. Some databases has a limit on the number of
     *   the parameters. This will also workaround the problem.
     * http://bugs.bluejungle.com/show_bug.cgi?id=10239
     * 
     * @param hs
     * @param verticesList
     * @param now
     * @throws HibernateException
     * @see {@link #rebuildTransitiveClosure(Session, Set, Date)}
     */
    private void rebuildTransitiveClosure(Session hs, Collection<Set<Long>> verticesList,
            final Date now) throws HibernateException {
        if (verticesList.isEmpty()) {
            return;
        }

        int totalSize = 0;
        for (Set<Long> vertices : verticesList) {
            totalSize += vertices.size();
        }

        if (totalSize > 5000) {
            LOG.warn("Rebuilding transitive closure of " + totalSize + " nodes may take significant time.");
        } else {
            LOG.debug("Starting to rebuild transitive closure of " + totalSize + " nodes.");
        }
        
        long whenStarted = System.currentTimeMillis();
        
        try {
            hs.flush();
            for (Set<Long> vertices : verticesList) {
                rebuildTransitiveClosure(hs, vertices, now);
                hs.flush();
            }
        } finally {
            long duration = System.currentTimeMillis() - whenStarted;
            if (duration > 10000) {
                LOG.warn("Rebuilding transitive closure of " + totalSize 
                        + " from " + verticesList.size() + " groups took "+duration+" mS.");
            }else if(LOG.isDebugEnabled()){
                LOG.debug("Finish transitive closure of " + totalSize 
                        + " from " + verticesList.size() + " groups took "+duration+" mS.");
            }
        }
    }
    

    /**
     * Given a <code>Set</code> of IDs of <code>EnumeratedGroup</code>s
     * that were modified or deleted in a change, finds and updates
     * the calculated links for the transitive closure of the resulting graph.
     *
     * @param hs the Hibernate session to use.
     * @param seedIds the <code>Set</code> of IDs of the
     * <code>EnumeratedGroup</code>s. This argument must not be null,
     * and may not contain nulls, otherwise an exception is thrown.
     * @param now
     * @throws HibernateException
     * @see {@link #rebuildTransitiveClosure(Session, Collection, Date)}
     */
    private void rebuildTransitiveClosure(Session hs, Set<Long> vertices, final Date now) throws HibernateException {
        // If there are no reachable vertices, there is nothing to do:
        if (vertices.isEmpty()) {
            return;
        }

        long whenStarted = System.currentTimeMillis();

        // Build the current dependency graph
        // Graph should be in a sorted map and its components should be in sorted sets
        // because of the way the algorithm traverses and updates the edge ResultSet.
        SortedMap<Long,SortedSet<Long>> graph = new TreeMap<Long,SortedSet<Long>>();

        List<Object[]> fromToList = HibernateUtils.safeList(vertices, hs,
            new HibernateUtils.SafeQuery<Long, Object[]>() {
                
                protected String getQueryString(){
                    return "select gm.fromId, gm.toId "
                    +   "from EnumerationGroupMember gm "
                    +   "where gm.isDirect='Y'"
                    +   "  and gm.enrollmentId=:enrollmentId"
                    +   "  and gm.timeRelation.activeFrom <= :asOf"
                    +   "  and gm.timeRelation.activeTo > :asOf"
                    +   "  and (gm.fromId in (:vertices) or gm.toId in (:vertices))";
                }
                
                protected void setQuery(Query q, Collection<Long> values)
                        throws HibernateException {
                    q.setParameter("asOf", now, DateToLongUserType.TYPE)
                     .setParameter("enrollmentId", enrollment.id)
                     .setParameterList("vertices", values);
                }
            }
        );
        
        
        for (Object[] directEdge : fromToList) {
            Long from = (Long)directEdge[0];
            Long to = (Long)directEdge[1];
            SortedSet<Long> outgoing = graph.get(from);
            if (outgoing == null) {
                outgoing = new TreeSet<Long>();
                graph.put(from, outgoing);
            }
            outgoing.add(to);
        }
        
        // the graph is completely transform from fromToList
        fromToList = null;

        // Compute transitive closure by searching breadth-first
        // from each vertex that has an outgoing edge. This has worst-time complexity
        // of O(V*E), where V is the number of vertices, and E is the number of edges.
        for (Long from : vertices) {
            SortedSet<Long> closure = graph.get(from);
            if (closure == null) {
                //dead end, next vertex
                continue;
            }
            
            Queue<Long> queue = new LinkedList<Long>(closure);
            while (!queue.isEmpty()) {
                SortedSet<Long> reachable = graph.get(queue.remove());
                if (reachable == null) {
                  //dead end, next one in the closure
                    continue;
                }
                for (Long to : reachable) {
                    if (closure.add(to)) {
                        queue.add(to);
                    }
                }
            }
        }

        // Convert the graph to an edge list. This lets us avoid loop nesting
        // in the code navigating the result set to simplify the loging there.
        // Note that the graph map is sorted, and so are the sets inside of it,
        // so we do not need to apply additional sorting after the conversion.
        Collection<long[]> allEdges = new ArrayList<long[]>(graph.size());
        for (Map.Entry<Long,SortedSet<Long>> entry : graph.entrySet()) {
            long from = entry.getKey();
            Set<Long> outgoing = entry.getValue();
            if (outgoing == null) {
                //dead end
                continue;
            }
            
            for (long to : outgoing) {
                if (to == from) {
                    //this is myself, skip
                    continue;
                }
                allEdges.add(new long[] { from, to });
            }
        }
        
        //tranformed the graph to allEdges
        graph = null;

        // Close existing indirect links that no longer exist, and
        // create newly added indirect links. This algorithm traverses
        // the stored set of edges sorted in the same way the current edges
        // are ordered. As the result, the algorithm can quickly decide
        // if a particular edge needs to be added or removed.

        // Keep the statistics for debugging
        int insertedCount = 0;
        int deletedCount = 0;
        int keptCount = 0;

        // Prepare the statements for reading links and for inserting new ones:
        try {
            List<Object[]> existingLinkList = HibernateUtils.safeList(vertices, hs, 450,
                new HibernateUtils.SafeQuery<Long, Object[]>() {
                    
                    protected String getQueryString(){
                        return  "select gm.id, gm.fromId, gm.toId "
                        +   "from EnumerationGroupMember gm "
                        +   "where gm.enrollmentId=:enrollment"
                        +   "  and gm.timeRelation.activeFrom <= :asOf"
                        +   "  and gm.timeRelation.activeTo > :asOf"
                        +   "  and (gm.fromId in (:ids) or gm.toId in (:ids)) ";
                    }
                    
                    protected void setQuery(Query q, Collection<Long> values)
                            throws HibernateException {
                        q.setParameter("asOf", now, DateToLongUserType.TYPE)
                         .setParameter("enrollment", enrollment.id)
                         .setParameterList("ids", values);
                    }
                }
            );
            
            Set<Object[]> existingLinkSet = new TreeSet<Object[]>(new Comparator<Object[]>() {
                public int compare(Object[] o1, Object[] o2) {
                    int r = ((Long)o1[1]).compareTo((Long)o2[1]);
                    return r != 0 ? r : ((Long)o1[2]).compareTo((Long)o2[2]);
                }
            });
            
            existingLinkSet.addAll(existingLinkList);
            
//            Collections.sort(existingLinkList, new Comparator<Object[]>() {
//                public int compare(Object[] o1, Object[] o2) {
//                    int r = ((Long)o1[1]).compareTo((Long)o2[1]);
//                    return r != 0 ? r : ((Long)o1[2]).compareTo((Long)o2[2]);
//                }
//            });
            
            List<Long> toClose = new ArrayList<Long>();
            TimeRelation fromNowOn = TimeRelation.open(now);
            Iterator<long[]> allEdgesIter = allEdges.iterator();
            // Walk through the existing links and the query result,
            // closing the links that no longer exist and inserting
            // the newly added ones.
            long[] stored = null;
            long[] current = null;
            boolean linksHasNext = false;
            Iterator<Object[]> existingLinks = existingLinkSet.iterator();
            while (    stored != null 
                    || (linksHasNext = existingLinks.hasNext()) 
                    || current != null
                    || allEdgesIter.hasNext()) {
                // This loop does a classic two-way merge, advancing
                // one or both iterators per loop iteration:
                long[] data = null;
                if (stored == null && linksHasNext) {
                    Object[] existingLink = existingLinks.next();
                    if (existingLink == null || existingLink.length != 3) {
                        throw new IllegalStateException(
                            "Query for existing links returned an invalid result"
                        );
                    }
                    data = new long[]{(Long)existingLink[0], (Long)existingLink[1], (Long)existingLink[2]};
                    stored = new long[] {data[1], data[2]};
                }
                if (current == null && allEdgesIter.hasNext()) {
                    current = allEdgesIter.next();
                }
                
                // We know that at least one array should not be null
                assert (current!=null) || (stored!=null);

                long compareResult;
                if (stored == null) {
                    compareResult = 1;
                } else if (current == null) {
                    compareResult = -1;
                } else { // stored and current are non-nulls
                    compareResult = (stored[0]==current[0]) 
                            ? stored[1]-current[1] 
                            : stored[0]-current[0];
                }
                if (compareResult == 0) {
                    // The link exists and is current. Leave it in place,
                    // and advance both iterators on the next step:
                    current = null;
                    stored = null;
                    keptCount++;
                } else if (compareResult < 0) {
                    if (data == null) {
                        Object[] existingLink = existingLinks.next();
                        data = new long[]{(Long)existingLink[0], (Long)existingLink[1]};
                    }
                    // Stored is less than current - mark the link for closure
                    toClose.add(data[0]);
                    stored = null;
                    deletedCount++;
                } else { // compareResult > 0
                    hs.save(new EnumerationGroupMember(
                        current[0]
                    ,   current[1]
                    ,   enrollment.id.longValue()
                    ,   false
                    ,   fromNowOn)
                    );
                    current = null;
                    insertedCount++;
                }
            }
            
            if (!toClose.isEmpty()) {
            // Close the links that have been removed
            MassDML.updateOrDelete(
                hs
            ,   "UPDATE DICT_ENUM_GROUP_MEMBERS SET ACTIVE_TO=? WHERE ID IN #"
            ,   new Object[] {now}
            ,   new Type[] {DateToLongUserType.TYPE}
            ,   toClose
            ,   Hibernate.LONG
            );
            }
        } finally {
            long duration = System.currentTimeMillis()-whenStarted;
            if (duration > 10000) {
                LOG.warn("Rebuilding transitive closure of "+vertices.size()+" took "+duration+" mS.");
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Finished building transitive closure in "+duration+" mS.");
            }
            if (LOG.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
                if (insertedCount == 0 && deletedCount == 0) {
                    msg.append("Transitive closure has not changed. ")
                       .append("Number of links: ").append(keptCount);
                } else {
                    msg.append("Transitive closure has changed. ")
                       .append("Existing links kept: ").append(keptCount)
                       .append(", new links added: ").append(insertedCount)
                       .append(", old links removed: ").append(deletedCount)
                       .append('.');
                }
                
                LOG.debug(msg.toString());
            }
        }
    }
}
