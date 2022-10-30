package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/EnumeratedGroup.java#1 $
 */

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.TimeRelation;

/**
 * Instances of this class represent enumerated groups.
 */
class EnumeratedGroup extends GroupBase {

    private final Set<IElementBase> addedLeaves = new HashSet<IElementBase>();
    private final Set<IMGroup> addedGroups = new HashSet<IMGroup>();
    private final Set<IReferenceable> addedRefs = new HashSet<IReferenceable>();

    private final Set<IElementBase> removedLeaves = new HashSet<IElementBase>();
    private final Set<IMGroup> removedGroups = new HashSet<IMGroup>();
    private final Set<IReferenceable> removedRefs = new HashSet<IReferenceable>();

    private final IElementVisitor adder = new IElementVisitor() {
        /**
         * @see IElementVisitor#visitGroup(IMGroup)
         */
        public void visitGroup(IMGroup group) {
            if (group instanceof StructuralGroup) {
                // Structural groups are ignored
                return;
            }
            removedGroups.remove(group);
            addedGroups.add(group);
        }
        /**
         * @see IElementVisitor#visitLeaf(IMElement)
         */
        public void visitLeaf(IMElement element) {
            removedLeaves.remove(element);
            addedLeaves.add(element);
        }
        /**
         * @see IElementVisitor#visitProvisionalReference(IReferenceable)
         */
        public void visitProvisionalReference(IReferenceable ref) {
            removedRefs.remove(ref);
            addedRefs.add(ref);
        }
    };

    private final IElementVisitor remover = new IElementVisitor() {
        /**
         * @see IElementVisitor#visitGroup(IMGroup)
         */
        public void visitGroup(IMGroup group) {
            if (group instanceof StructuralGroup) {
                return; 
            }
            addedGroups.remove(group);
            removedGroups.add(group);
        }
        /**
         * @see IElementVisitor#visitLeaf(IMElement)
         */
        public void visitLeaf(IMElement element) {
            addedLeaves.remove(element);
            removedLeaves.add(element);
        }
        /**
         * @see IElementVisitor#visitProvisionalReference(IReferenceable)
         */
        public void visitProvisionalReference(IReferenceable ref) {
            addedRefs.remove(ref);
            removedRefs.add(ref);
        }
    };

    /**
     * This package-private constructor is for Hibernate. 
     */
    public EnumeratedGroup() {
        super();
    }

    /**
     * Constructs an enumerated group at the specific <code>DictionaryPath</code>
     * with the specified <code>DictionaryKey</code> and within the given
     * <code>Enrollment</code>.
     *
     * @param path the path where to create the group.
     * @param key the key of the new group.
     * @param enrollment the enrollment of the new group.
     */
    public EnumeratedGroup( DictionaryPath path, DictionaryKey key, Enrollment enrollment ) {
        super(path, key, enrollment);
    }
    
    public IElementType getType() {
        return IElementType.ENUM_GROUP_TYPE;
    }

    /**
     * @see IMGroup#getAllChildGroups()
     */
    public IDictionaryIterator<IMGroup> getAllChildGroups() throws DictionaryException {
        return getChildGroups(false);
    }

    /**
     * @see IMGroup#getDirectChildElements()
     */
    public IDictionaryIterator<IMElement> getDirectChildElements() throws DictionaryException {
        return getEnrollment().getDictionary().query(
            getDirectMembershipPredicate(), queryAsOf(), null, null
        );
    }

    /**
     * @see IMGroup#getDirectChildGroups()
     */
    public IDictionaryIterator<IMGroup> getDirectChildGroups() throws DictionaryException {
        return getChildGroups(true);
    }

    /**
     * @see IMGroup#getAllChildElements()
     */
    public IDictionaryIterator<IMElement> getAllChildElements() throws DictionaryException {
        return getEnrollment().getDictionary().query(
            getTransitiveMembershipPredicate(), queryAsOf(), null, null
        );
    }

    public IDictionaryIterator<DictionaryPath> getAllReferenceMemebers() throws DictionaryException {
        return getEnrollment().getDictionary().queryReferenceMembersByGroupId(this.getOriginalId());
    }

    /**
     * @see IMGroup#addChild(IMElement)
     */
    public synchronized void addChild(IReferenceable element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        element.accept(adder);
    }

    /**
     * @see IMGroup#removeChild(IMElement)
     */
    public synchronized void removeChild(IReferenceable element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        element.accept(remover);
    }

    /**
     * @see DictionaryElementBase#saveComplete()
     */
    synchronized void saveComplete() {
        super.saveComplete();
        addedLeaves.clear();
        removedLeaves.clear();
        addedGroups.clear();
        removedGroups.clear();
        addedRefs.clear();
        removedRefs.clear();
    }

    @Override
    public String toString() {
        return "e: " + super.toString();
    }

    /**
     * @see DictionaryElementBase#getIndirectUpdate()
     */
    IIndirectUpdate getIndirectUpdate() {
        if (!(addedLeaves.isEmpty() && removedLeaves.isEmpty()
           && addedGroups.isEmpty() && removedGroups.isEmpty()
           && addedRefs.isEmpty() && removedRefs.isEmpty())) {
            // The call to onUpdate happens only now, not when
            // the actual addition or removal of children took place.
            // This is done to avoid situations when adding an element
            // and then removing it without saving creates a new "time slice".
            onUpdate();
        }
        return new IndirectUpdate();
    }

    /**
     * @see GroupBase#deepCopy()
     */
    protected DictionaryElementBase deepCopy() {
        return new EnumeratedGroup();
    }

    /**
     * @see IGroup#getDirectMembershipPredicate()
     */
    public IPredicate getDirectMembershipPredicate() {
        return new AbstractDictionaryPredicate() {
            /**
             * @see IDictionaryPredicate#accept(IDictionaryPredicateVisitor)
             */
            public void accept(IDictionaryPredicateVisitor visitor) {
                visitor.visitDirectMembership(EnumeratedGroup.this);
            }
        };
    }

    /**
     * @see IGroup#getTransitiveMembershipPredicate()
     */
    public IPredicate getTransitiveMembershipPredicate() {
        return new AbstractDictionaryPredicate() {
            /**
             * @see IDictionaryPredicate#accept(IDictionaryPredicateVisitor)
             */
            public void accept(IDictionaryPredicateVisitor visitor) {
                visitor.visitTransitiveMembership(EnumeratedGroup.this);
            }
        };
    }

    /**
     * This private method implements the child group logic.
     * @param onlyDirect a flag indicating that only direct subgroups
     * should be returned.
     * @return an <code>IDictionaryIterator<IMGroup></code> with the specified groups.
     * @throws DictionaryException if the query operation fails.
     */
    private IDictionaryIterator<IMGroup> getChildGroups(boolean onlyDirect) throws DictionaryException {
        Date asOf = queryAsOf();
        if (asOf == null) {
            asOf = new Date();
        }
        return getEnrollment().getDictionary().getEnumeratedGroups(
            onlyDirect ? getDirectMembershipPredicate() : getTransitiveMembershipPredicate()
        ,   null
        ,   asOf
        ,   null
        );
    }

    /**
     * @see GroupBase#addedQueries(Session)
     */
    protected Query[] addedQueries(Session hs) throws HibernateException {
    	Query queries[] = new Query[2];

    	// NI: These two queries used be one combined query.    
    	// We found that the combined query did not perform well on Microsoft SQL Server
    	// We broke it up into two simple queries, resulting in faster execution
    	
        queries[0] = hs.createQuery(
        		"select distinct em.memberId "
        		+   "from EnumerationMember em "
        		+   "where ( "
        		+   "       em.groupId=:group"
        		+   "   and em.timeRelation.activeFrom >=:from"
        		+   "   and em.timeRelation.activeFrom <:to"
        		+   "   and em.elementTypeId=:type"
        		+   ")"
        ).setParameter("group", getInternalKey());
        
        queries[1] = hs.createQuery(
        		"select distinct em.memberId "
            +   "from EnumerationMember em, EnumerationGroupMember xm "
            +   "where ( "
            +   "       xm.fromId=:group"
            +   "   and xm.toId = em.groupId"
            +   "   and xm.timeRelation.activeFrom >=:from"
            +   "   and xm.timeRelation.activeFrom < :to"
            +   "   and em.elementTypeId=:type"
            +   ")"
        ).setParameter("group", getInternalKey());
    	
        return queries;
    }

    /**
     * @see GroupBase#removedQueries(Session)
     */
    protected Query[] removedQueries(Session hs) throws HibernateException {
    	Query queries[] = new Query[2];
    	
    	// NI: These two queries used be one combined query.    
    	// We found that the combined query did not perform well on Microsoft SQL Server
    	// We broke it up into two simple queries, resulting in faster execution
    	
    	queries[0] = hs.createQuery(
    			"select distinct em.memberId "
    			+   "from EnumerationMember em "
    			+   "where ( "
    			+   "       em.groupId=:group"
    			+   "   and em.timeRelation.activeTo >=:from"
    			+   "   and em.timeRelation.activeTo <:to"
    			+   "   and em.elementTypeId=:type"
    			+   ")"
    	).setParameter("group", getInternalKey());
    	
    	queries[1] = hs.createQuery(
    			"select distinct em.memberId "
    			+   "from EnumerationMember em, EnumerationGroupMember xm "
    			+   "where ( "
    			+   "       xm.fromId=:group"
    			+   "   and xm.toId = em.groupId"
    			+   "   and xm.timeRelation.activeTo >=:from"
    			+   "   and xm.timeRelation.activeTo < :to"
    			+   "   and em.elementTypeId=:type"
    			+   ")" 
    	).setParameter("group", getInternalKey());
    	
    	return queries;
    }
    
    

    private class IndirectUpdate implements IIndirectUpdate {
    	private final Log LOG = LogFactory.getLog(IndirectUpdate.class);
        /**
         * @see IIndirectUpdate#isIndependentOf(IIndirectUpdate)
         */
        public boolean isIndependentOf( IIndirectUpdate other ) {
            // Updates of enumerated groups are independent of each other
            // and of other updates, as long as they are for different
            // target groups.
            if (!(other instanceof IndirectUpdate)) {
                return true;
            }
            return !((IndirectUpdate)other).getGroup().equals(getGroup());
        }
        /**
         * @see IIndirectUpdate#postSaveExecute(Session, Set, Set, Set, Set, Date)
         */
        public void postSaveExecute(
                Session session, 
                Set<Long> leavesToClose,
				Set<Long> groupsToClose, 
				Set<Long> refsToRemove, 
				Set<Object> otherToUpdate, 
				final Date now) throws HibernateException {
        	final boolean isDebug = LOG.isDebugEnabled();
        	
            // This method is called post-save, so the original ID
            // must have been assigned by now.
            assert EnumeratedGroup.this.getOriginalId() != null;
            if (!removedLeaves.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("removedLeaves=" + CollectionUtils.asString(removedLeaves, ", "));
            	}
                
                // Mark current element links for closure
            	// duplicated may return but since we add them to a set. It will be fine
                leavesToClose.addAll(HibernateUtils.safeList(removedLeaves, session,
                        new HibernateUtils.SafeQuery3<IElementBase, Long, Long>() {
                            @Override
                            protected String getQueryString(){
                                return "select em.id from EnumerationMember em where"
                                +   " em.groupId = :groupId"
                                +   " and em.timeRelation.activeTo > :asOf"
                                +   " and em.timeRelation.activeFrom <= :asOf"
                                +   " and em.memberId in (:members)";
                            }
                            
                            @Override
                            protected void setQuery(Query q, Collection<Long> values) 
                                    throws HibernateException{
                                q.setParameter("asOf", now, DateToLongUserType.TYPE)
                                 .setParameter("groupId", EnumeratedGroup.this.getOriginalId())
                                 .setParameterList("members", values);
                            }
                            
                            @Override
                            protected Long convert(IElementBase value) {
                                return new Long(value.getInternalKey());
                            }
                        }
                ));
            }
            // Add new element links
            if (!addedLeaves.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("addedLeaves=" + CollectionUtils.asString(addedLeaves, ", "));
            	}
                TimeRelation tr = TimeRelation.open(now);
                for ( IElementBase element : addedLeaves) {
                    Long typeId = null;
                    IElementType type = element.getType();
                    assert type != null;
                    typeId = ((ElementType)type).getId();
                    otherToUpdate.add(
                        new EnumerationMember(
                            EnumeratedGroup.this.getOriginalId().longValue()
                        ,   element.getInternalKey().longValue()
                        ,   ((Enrollment)getEnrollment()).id.longValue()
                        ,   typeId
                        ,   tr
                        )
                    );
                }
            }
            if (!removedGroups.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("removedGroups=" + CollectionUtils.asString(removedGroups, ", "));
            	}
                
                // Mark current group links for closure
            	// duplicated may return but since we add them to a set. It will be fine
            	groupsToClose.addAll(HibernateUtils.safeList(removedGroups, session,
                        new HibernateUtils.SafeQuery3<IMGroup, Long, Long>() {
                            @Override
                            protected String getQueryString(){
                                return "select gm.id from EnumerationGroupMember gm where"
                                +   " gm.fromId = :groupId"
                                +   " and gm.timeRelation.activeTo > :asOf"
                                +   " and gm.timeRelation.activeFrom <= :asOf"
                                +   " and gm.toId in (:members)";
                            }
                            
                            @Override
                            protected void setQuery(Query q, Collection<Long> values) 
                                    throws HibernateException{
                                q.setParameter("asOf", now, DateToLongUserType.TYPE)
                                 .setParameter("groupId", EnumeratedGroup.this.getOriginalId())
                                 .setParameterList("members", values);
                            }
                            
                            @Override
                            protected Long convert(IMGroup value) {
                                return new Long(value.getInternalKey());
                            }
                        }
                ));
            }
            // Add new element links
            if (!addedGroups.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("addedGroups=" + CollectionUtils.asString(addedGroups, ", "));
            	}
                TimeRelation tr = TimeRelation.open(now);
                for (IElementBase element : addedGroups) {
                    otherToUpdate.add(
                        new EnumerationGroupMember(
                            EnumeratedGroup.this.getOriginalId().longValue()
                        ,   element.getInternalKey().longValue()
                        ,   ((Enrollment)getEnrollment()).id.longValue()
                        ,   true
                        ,   tr
                        )
                    );
                }
            }
            if (!removedRefs.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("removedRefs=" + CollectionUtils.asString(removedRefs, ", "));
            	}
                // Prepare a collection of IDs of removed forward references
                Set<Long> removedPathHashCodes = new HashSet<Long>();
                Set<DictionaryPath> removedPaths = new HashSet<DictionaryPath>();
                for (IReferenceable ref : removedRefs) {
                    DictionaryPath path = ref.getPath();
                    removedPaths.add(path);
                    removedPathHashCodes.add((long)path.hashCode());
                }
                
                // Get forward refs by hash code, then filter by path in memory
             // duplicated may return but since we add them to a set. It will be fine
                List<EnumerationProvisionalMember> refs = HibernateUtils.safeList(
                        removedPathHashCodes, session,
                        new HibernateUtils.SafeQuery<Long, EnumerationProvisionalMember>() {
                            @Override
                            protected String getQueryString(){
                                return "from EnumerationProvisionalMember pm where"
                                +   " pm.groupId = :groupId"
                                +   " and pm.path.pathHash in (:pathHash)";
                            }
                            
                            @Override
                            protected void setQuery(Query q, Collection<Long> values) 
                                    throws HibernateException{
                                q.setParameter("groupId", EnumeratedGroup.this.getOriginalId())
                                 .setParameterList("pathHash", values);
                            }
                        }
                );
                
                for (EnumerationProvisionalMember ref : refs) {
                    if (removedPaths.remove(ref.getPath())) {
                        refsToRemove.add(ref.getId());
                    }
                }
            }
            if (!addedRefs.isEmpty()) {
            	if (isDebug) {
            		LOG.debug("addedRefs=" + CollectionUtils.asString(addedRefs, ", "));
            	}
                for (IReferenceable ref : addedRefs) {
                    otherToUpdate.add(
                        new EnumerationProvisionalMember(
                            EnumeratedGroup.this.getOriginalId().longValue()
                        ,   ((Enrollment)getEnrollment()).id.longValue()
                        ,   ref.getPath()
                        )
                    );
                }
            }
        }

        /**
         * @see IIndirectUpdate#preSaveExecute(Session, Map, Date)
         */
        public void preSaveExecute( Session session, Map<IElementBase,IElementBase> updatedObjects, Date now ) throws HibernateException {
            // This method is ignored.
        }

        /**
         * Returns the group for which this is an update.
         * This method is used to determine mutual dependencies
         * among a collection of updates.
         *
         * @return the group for which this is an update.
         */
        private IMGroup getGroup() {
            return EnumeratedGroup.this;
        }

    }

}
