/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/StructuralGroup.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;

import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.expressions.IPredicate;

/**
 * Instances of this class represent structural groups.
 */
class StructuralGroup extends GroupBase {
	
    /**
     * This field holds the indirect update object.
     * Initially set to null, this field is updated when the
     * <code>setPath</code> method is called.
     */
    private IndirectUpdate update = null;

    /**
     * This package-private constructor is for Hibernate. 
     */
    StructuralGroup() {
        super();
    }

    /**
     * @param path
     * @param key
     * @param enrollment
     */
    public StructuralGroup( DictionaryPath path, DictionaryKey key, Enrollment enrollment ) {
        super( path, key, enrollment );
    }
    
    public IElementType getType() {
		return IElementType.STRUCT_GROUP_TYPE;
	}

    /**
     * @see IMGroup#getAllChildGroups()
     */
    public IDictionaryIterator<IMGroup> getAllChildGroups() throws DictionaryException {
        IDictionary dictionary = getEnrollment().getDictionary();
        return dictionary.getStructuralGroups(
            dictionary.condition(getPath(), false), null, queryAsOf(), null
        );
    }

    /**
     * @see IMGroup#getAllChildElements()
     */
    public IDictionaryIterator<IMElement> getAllChildElements() throws DictionaryException {
        return getEnrollment().getDictionary().query(
            getTransitiveMembershipPredicate(), queryAsOf(), null, null
        );
    }

    /**
     * @see IMGroup#getDirectChildGroups()
     */
    public IDictionaryIterator<IMGroup> getDirectChildGroups() throws DictionaryException {
        IDictionary dictionary = getEnrollment().getDictionary();
        return dictionary.getStructuralGroups(
            dictionary.condition(getPath(), true), null, queryAsOf(), null
        );
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
     * @see IMGroup#addChild(IMElement)
     */
    public void addChild(IReferenceable element) {
        // This is a structural group - adding a child means reparenting.
        element.accept(new IElementVisitor() {
            /**
             * @see IElementVisitor#visitGroup(IMGroup)
             */
            public void visitGroup( IMGroup group ) {
                reparent(group);
            }
            /**
             * @see IElementVisitor#visitLeaf(IMElement)
             */
            public void visitLeaf( IMElement element ) {
                reparent(element);
            }
            /**
             * @see IElementVisitor#visitProvisionalReference(IReferenceable)
             */
            public void visitProvisionalReference( IReferenceable ref ) {
                throw new IllegalArgumentException(
                    "Adding provisional references to structural groups is not supported."
                );
            }
            private void reparent(IMElementBase element) {
                element.setPath(element.getPath().reparent(getPath()));
            }
        });
    }
    

    public IDictionaryIterator<DictionaryPath> getAllReferenceMemebers() throws DictionaryException {
		throw new UnsupportedOperationException();
	}

	/**
     * @see IMGroup#removeChild(IMElement)
     */
    public void removeChild(IReferenceable element) {
        // Removing a child of a structural group is not possible.
        throw new UnsupportedOperationException();
    }

    /**
     * This is a package-private method for Hibernate.
     */
    String getFilter() {
        String res = getPath().toFilterString(false);
        if (res == null || res.length() == 0) {
            return res;
        } else {
            return res.substring(0, res.length()-1);
        }
    }

    /**
     * This is a package-private method for Hibernate.
     */
    int getFilterLength() {
        String res = getPath().toFilterString(false);
        if (res == null || res.length() == 0) {
            return 0;
        } else {
            return res.length()-1;
        }
    }

    /**
     * This is a package-private method for Hibernate.
     */
    void setFilter(String ignored) {
        // The field is calculated, so this call is ignored
    }

    /**
     * This is a package-private method for Hibernate.
     */
    void setFilterLength(int ignored) {
        // The field is calculated, so this call is ignored
    }

    /**
     * @see com.bluejungle.dictionary.DictionaryElementBase#setPath(com.bluejungle.dictionary.DictionaryPath)
     */
    public boolean setPath( DictionaryPath path ) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (getPath().equals(path)) {
            return false;
        }
        update = new IndirectUpdate(
            update == null ? getPath() : update.getFromPath()
        ,   path
        );
        super.setPath(path);
        return true;
    }

    @Override
    public String toString() {
        return "S: " + super.toString();
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
                visitor.visitDictionaryPath(getPath(), true);
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
                visitor.visitDictionaryPath(getPath(), false);
            }
        };
    }

    /**
     * @see DictionaryElementBase#getIndirectUpdate()
     */
    IIndirectUpdate getIndirectUpdate() {
        return update;
    }

    /**
     * @see DictionaryElementBase#deepCopy()
     */
    protected DictionaryElementBase deepCopy() {
        return new StructuralGroup();
    }

    /**
     * @see GroupBase#addedQueries(Session)
     */
    protected Query[] addedQueries(Session hs) throws HibernateException {

    	Query queries[] = new Query[1];
    	queries[0] = hs.createQuery(
            "select distinct e.originalId "
        +   "from LeafElement e "
        +   "where e.path.path like :path"
        +   "  and e.timeRelation.activeFrom>=:from"
        +   "  and e.timeRelation.activeFrom<:to"
        +   "  and e.type=:type"
        +   "  and e.isReparented='Y'")
        .setParameter("path", getPath().toFilterString(false));
        
        return queries;
    }

    /**
     * @see GroupBase#removedQueries(Session)
     */
    protected Query[] removedQueries(Session hs) throws HibernateException {
    	Query queries[] = new Query[1];
    	
        queries[0] = hs.createQuery(
            "select distinct e.originalId "
        +   "from LeafElement e "
        +   "where e.path.path like :path"
        +   "  and e.timeRelation.activeTo>=:from"
        +   "  and e.timeRelation.activeTo<:to"
        +   "  and e.type=:type"
        +   "  and e.isReparented='Y'")
        .setParameter("path", getPath().toFilterString(false));
        
        return queries;
    }

    /**
     * Instances of this class represent updates done to this structural
     * group. Each update object is responsible for performing the update
     * action on the dictionary.
     * @see IIndirectUpdate
     */
    private static class IndirectUpdate implements IIndirectUpdate {

        private final DictionaryPath fromPath;
        private final DictionaryPath toPath;

        /**
         * Builds an indirect update object with the specified
         * <code>fromPath</code> and <code>toPath</code>.
         * @param fromPath the old path of the group from which we rename.
         * @param toPath the new path of the group to which we rename.
         */
        public IndirectUpdate(DictionaryPath fromPath, DictionaryPath toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
        }

        /**
         * Getter for the <code>fromPath</code> field.
         * @return the <code>fromPath</code> field.
         */
        public DictionaryPath getFromPath() {
            return fromPath;
        }

        /**
         * Getter for the <code>toPath</code> field.
         * @return the <code>toPath</code> field.
         */
        public DictionaryPath getToPath() {
            return toPath;
        }

        /**
         * @see IIndirectUpdate#isIndependentOf(IIndirectUpdate)
         */
        public boolean isIndependentOf( IIndirectUpdate other ) {
            // Unknown and null updates are considered independent
            if (!(other instanceof IndirectUpdate)) {
                return true;
            }
            IndirectUpdate update = (IndirectUpdate)other;
            return checkIndependence(this, update) && checkIndependence(update, this);
        }

        /**
         * Checks if paths of <code>a</code> are related to path of <code>b</code>.
         * @param a the first update to check.
         * @param b the second update to check.
         * @return true if the updates are related; false otherwise.
         */
        private static boolean checkIndependence(IndirectUpdate a, IndirectUpdate b) {
            if (a.getFromPath().isParentOf(b.getFromPath())) {
                return a.getToPath().isParentOf(b.getToPath());
            }
            if (a.getFromPath().isParentOf(b.getToPath())) {
                return false;
            }
            if (a.getToPath().isParentOf(b.getFromPath())) {
                return false;
            }
            if (a.getToPath().isParentOf(b.getToPath())) {
                return false;
            }
            return true;
        }

        /**
         * @see IIndirectUpdate#preSaveExecute(Session, Map, Date)
         */
        public void preSaveExecute(Session session, Map<IElementBase,IElementBase> updatedObjects, Date now) throws HibernateException {
            ScrollableResults rs = session
                .createQuery("from DictionaryElementBase eb "
                           + "where eb.path.path like :path "
                           + "and eb.timeRelation.activeTo > :asOf "
                           + "and eb.timeRelation.activeFrom <= :asOf")
                .setParameter("path", fromPath.toFilterString(false))
                .setParameter("asOf", now, DateToLongUserType.TYPE)
                .scroll();
            try {
                while (rs.next()) {
                    DictionaryElementBase e = (DictionaryElementBase)rs.get(0);
                    DictionaryElementBase toUpdate = (DictionaryElementBase)updatedObjects.get(e);
                    if (toUpdate == null) {
                        toUpdate = e;
                        
                        //all changes must be made before putting it into the hashmap
                        //the reason is if the element is updated, the hashCode will be different.
                        toUpdate.setPath(toUpdate.getPath().move(fromPath, toPath), true);
                        updatedObjects.put(toUpdate, toUpdate);
                    } else {
                        toUpdate.setPath(toUpdate.getPath().move(fromPath, toPath), true);
                    }
                    session.evict(toUpdate);
                }
            } finally {
                rs.close();
            }
        }

        /**
         * @see IIndirectUpdate#postSaveExecute(Session, Set, Set, Set, Set, Date)
         */
        public void postSaveExecute( Session session, Set<Long> leavesToClose, Set<Long> groupsToClose, Set<Long> refsToRemove, Set<Object> otherToSave, Date now ) throws HibernateException {
            // This operation is ignored
        }

    }

}
