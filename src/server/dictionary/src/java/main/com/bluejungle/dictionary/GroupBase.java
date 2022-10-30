package com.bluejungle.dictionary;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.ScrollableResults;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.utils.UnmodifiableDate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class GroupBase extends DictionaryElementBase implements IMGroup {

	private static final Log log = LogFactory.getLog(GroupBase.class);
	
    /**
     * This is a package-private constructor for Hibernate.
     */
    GroupBase() {
    }

    /**
     * @see IGroup#getName()
     */
    public String getName() {
        return getPath().getName();
    }

    /**
     * @see IMGroup#setName()
     */
    public void setName( String name ) {
        setPath(getPath().rename(name));
    }

    /**
     * @see IElementBase#accept(IElementVisitor)
     */
    public void accept(IElementVisitor visitor) {
        visitor.visitGroup(this);
    }

    /**
     * A constructor with the name, the key and the enrollment.
     * @param path the path to this group.
     * @param name name of this group.
     * @param enrollment the enrollment from which this element is created.
     */
    public GroupBase( DictionaryPath path, DictionaryKey key, Enrollment enrollment ) {
        super(path, enrollment, key);
        if ( key == null ) {
            throw new NullPointerException("key");
        }
    }

    /**
     * @see DictionaryElementBase#deepCopy()
     */
    protected DictionaryElementBase deepCopy() {
        throw new UnsupportedOperationException("Subclasses must override GroupBase.deepCopy()");
    }

    /**
     * This method calculates the asOf time for the query
     * based on the state of the <code>timeRelation</code> element.
     * @return the <code>Date</code> for as-of queries.
     */
    protected final Date queryAsOf() {
        Date trFrom = getTimeRelation().getActiveFrom();
        Date trTo = getTimeRelation().getActiveTo();
        if (trTo.equals(UnmodifiableDate.END_OF_TIME)) {
            return null;
        } else {
            Date res = UnmodifiableDate.forTime(trTo.getTime()-1);
            return res.after(trFrom) ? res : trFrom;
        }
    }

    /**
     * Subclasses override this method to provide an HQL-based queries
     * for returning IDs of all leaves of type ":type" that were added
     * to the group between the dates of ":from" and ":to".
     * This method returns an array of queries, rather than one query, 
     * to break up a time-consuming query into multiple faster queries.
     * @param hs the <code>Session</code> on which to create the query.
     * @return an HQL query for querying group additions.
     */
    protected abstract Query[] addedQueries(Session hs) throws HibernateException;

    /**
     * Subclasses override this method to provide an HQL-based queries
     * for returning IDs of all leaves of type ":type" that were removed
     * from the group between the dates of ":from" and ":to".
     * This method returns an array of queries, rather than one query, 
     * to break up a time-consuming query into multiple faster queries.
     * @param hs the <code>Session</code> on which to create the query.
     * @return an HQL query for querying group removals.
     */
    protected abstract Query[] removedQueries(Session hs) throws HibernateException;
    

    /**
     * @see IMGroup#getChanges(IElementType, Date, Date)
     */
    public IGroupChanges getChanges(IElementType type, Date startDate, Date endDate) throws DictionaryException {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (!(type instanceof ElementType)) {
            throw new IllegalArgumentException("type");
        }
        Long typeId = ((ElementType)type).getId();
        if (typeId == null) {
            throw new IllegalStateException("unsaved type");
        }
        if (startDate == null) {
            startDate = UnmodifiableDate.START_OF_TIME;
        }
        if (endDate == null) {
            endDate = new Date();
        }
        Dictionary dict = (Dictionary)getEnrollment().getDictionary();
        Session hs = dict.getCountedSession();
        Transaction tx = null;
        try {
            tx = hs.beginTransaction();
            
            // run multiple queries to obtain added objects
            Query addedQueries[] = addedQueries(hs);
            Set<Long> addedSet = new HashSet<Long>();
            for (int i=0; i<addedQueries.length; i++) {
            	List resultList = addedQueries[i]
            	                              .setParameter("type", typeId)
            	                              .setParameter("from", startDate, DateToLongUserType.TYPE)
            	                              .setParameter("to", endDate, DateToLongUserType.TYPE)
            	                              .list();
            	// combine results from queries into one.  This also eliminates duplicated results.
            	addedSet.addAll(resultList);
            }

            log.trace("GroupBase.getChanges(): addedSet includes " + addedSet.size() + " items");
            
            // run multiple queries to obtain removed objects
            Query removedQueries[] = removedQueries(hs);
            Set<Long> removedSet = new HashSet<Long>();
            for (int i=0; i<removedQueries.length; i++) {
            	List resultList = removedQueries[i]
            	                                 .setParameter("type", typeId)
            	                                 .setParameter("from", startDate, DateToLongUserType.TYPE)
            	                                 .setParameter("to", endDate, DateToLongUserType.TYPE)
            	                                 .list();
            	// combine results from queries into one.  This also eliminates duplicated results.
            	removedSet.addAll(resultList);
            }
            log.trace("GroupBase.getChanges(): removedSet includes " + addedSet.size() + " items");

            return new GroupMembershipChanges(addedSet.iterator(), removedSet.iterator());
        } catch (HibernateException he) {
            throw new DictionaryException(he);
        } finally {
            try {
                tx.commit();
            } catch (HibernateException ignored) {
            }
            if (hs != null) {
                // close the counted session
            	// we can't close the counted session because we didn't get the result yet
            	// however, it was causing a counted session leak.
            	// so either GroupMembershipChanges close the counted or here.
            	// I decided here.
                dict.closeCurrentSession();
            }
        }
    }

    /**
     * This is an implementation of the IGroupChanges
     * interface returned from the dictionary groups.
     *
     * TODO (sergey) this class should be removed when we switch to using views in reporter.
     */
    private static class GroupMembershipChanges implements IGroupChanges {
        /**
         * This is the key iterator for the added elements.
         */
        private final IDictionaryIterator<Long> added;
        /**
         * This is the key iterator for the removed elements.
         */
        private final IDictionaryIterator<Long> removed;

        /**
         * Creates a GroupMembershipChanges with the specified session
         * and scrollable results.
         * @param session the session from which the scrollable results
         * were obtained.
         * @param added the scrollable result with the keys of
         * added elements.
         * @param removed the scrollable results with the keys of
         * removed elements.
         * @throws DictionaryException when the operation cannot complete.
         */
        public GroupMembershipChanges(ScrollableResults added, ScrollableResults removed) throws DictionaryException {
            try {
                this.added = new MemberKeyIterator(added);
                this.removed = new MemberKeyIterator(removed);
            } catch (HibernateException he) {
                throw new DictionaryException(he);
            }
        }
        
        /**
         * Creates a GroupMembershipChanges with Java Iterator.
         * @param added the Iterator with the keys of added Long elements.
         * @param removed the Iterator with the keys of removed Long elements.
         */
        public GroupMembershipChanges(Iterator<Long> added, Iterator<Long> removed) {
        	this.added = new MemberIterator(added);
        	this.removed = new MemberIterator(removed);
        }
        
        /**
         * @see IGroupChanges#close()
         */
        public void close() throws DictionaryException {
            DictionaryException toThrow = null;
            try {
                added.close();
            } catch (DictionaryException de) {
                toThrow = de;
            } finally {
                try {
                    removed.close();
                } catch (DictionaryException de) {
                    toThrow = de;
                } finally {
                    if (toThrow != null) {
                        throw toThrow;
                    }
                }
            }
        }
        /**
         * @see IGroupChanges#getKeysOfAddedMembers()
         */
        public IDictionaryIterator<Long> getKeysOfAddedMembers() {
            return added;
        }
        /**
         * @see IGroupChanges#getKeysOfRemovedMembers()
         */
        public IDictionaryIterator<Long> getKeysOfRemovedMembers() {
            return removed;
        }

        /**
         * This is an implementation of the IDictionaryIterator<Long>
         * that relies on the caller to close the session.
         */
        private static class MemberKeyIterator implements IDictionaryIterator<Long> {
            /**
             * This is Hibernate's scrollable result set
             * underlying this iterator.
             */
            private final ScrollableResults rs;
            /**
             * This is a flag telling whether or not the scrollable
             * result set has more data. Initially it is set
             * in the constructo. After that, the next() method sets
             * the value used in subsequent calls to hasNext(). */
            private boolean hasNext;
            /**
             * Create a MemberKeyIterator on top of the specified
             * ScrollableResults object.
             * @param rs the ScrollableResults to wrap into an iterator.
             * @throws HibernateException when the operation cannot complete.
             */
            public MemberKeyIterator(ScrollableResults rs) throws HibernateException {
                if (rs == null) {
                    throw new NullPointerException("rs");
                }
                this.rs = rs;
                this.hasNext = rs.next();
            }
            /**
             * @see IDictionaryIterator<Long>#close()
             */
            public void close() throws DictionaryException {
                try {
                    rs.close();
                } catch (HibernateException he ) {
                    throw new DictionaryException(he);
                }
            }
            /**
             * @see IDictionaryIterator<Long>#hasNext()
             */
            public boolean hasNext() throws DictionaryException {
                return hasNext;
            }
            /**
             * @see IDictionaryIterator<Long>#nextKey()
             */
            public Long next() throws DictionaryException {
                if ( !hasNext ) {
                    throw new IllegalStateException(
                        "Attempt to advance the iterator past the end."
                    );
                }
                try {
                    Long res = (Long)rs.get(0);
                    hasNext = rs.next();
                    return res;
                } catch ( HibernateException cause ) {
                    throw new DictionaryException(
                        "Unable to retrieve the next internal key", cause
                    );
                }
            }
        }
        
        /**
         * Implementation of IDictionaryIterator<Long> that wraps Java Iterator. 
         */
        private static class MemberIterator implements IDictionaryIterator<Long> {
            
            private Iterator<Long> itr;

            /**
             * Create MemberIterator on top of Java Iterator object.
             * @param itr Iteration to be wrapped.
             */
            public MemberIterator(Iterator<Long> itr) {
            	this.itr = itr;
            }
            /**
             * @see IDictionaryIterator<Long>#close()
             */
            public void close() {
            	itr = null;
            }
            /**
             * @see IDictionaryIterator<Long>#hasNext()
             */
            public boolean hasNext() {
                return itr.hasNext();
            }
            /**
             * @see IDictionaryIterator<Long>#nextKey()
             */
            public Long next() throws DictionaryException {
            	if ( itr.hasNext() == false ) {
            		throw new IllegalStateException(
            				"Attempt to advance the iterator past the end."
            		);
                }
            	
            	return itr.next(); 
            }
        }
    }

}
