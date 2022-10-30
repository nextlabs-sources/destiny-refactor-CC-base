/*
 * Created on Apr 19, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IGroupChanges;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.datastore.hibernate.utils.MassDMLUtils;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;

/**
 * The dictionary resource cache implementation class takes care of maintaining
 * tables based on the the dictionary content. Upon refresh, the resource cache
 * attempts to see if any major dictionary update(s) have occurred. If updates
 * have occured, then each major update is translated to the cache table.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/BaseDictionaryResourceCacheImpl.java#3 $
 */

public abstract class BaseDictionaryResourceCacheImpl extends BaseResourceCacheImpl {

    protected static final String CACHE_TABLE_SEQUENCE = "cached_table_sequence";
    
    private static final String UNKNOWN_FIELD = "<unknown>";

    private IDictionary dictionary;

    /**
     * Close an element in the element history. This function updates the
     * element that is currently valid until the end of time, and close it as of
     * the close date specified.
     * 
     * @param con
     *            connection to use to close the element
     * @param elementToClose
     *            element to be closed
     * @param closeDate
     *            date as of which the element should be closed
     * @throws SQLException
     *             if the query fails
     */
    protected abstract void closeElement(Connection con, IElement elementToClose, Date closeDate)
            throws SQLException;

    /**
     * Adds the latest version on an element in the element history. The element
     * is valid from the "fromDate" until the end of time.
     * 
     * @param con
     *            connection to use to insert the new row
     * @param elementToCreate
     *            element to add the the element history
     * @param fromDate
     * @param sequenceSQL
     * @throws SQLException
     *             if the query fails
     */
    protected abstract void createNewElement(Connection con, IElement elementToCreate, Date fromDate,
            IMassDMLFormatter formatter) throws SQLException;

    /**
     * Close a a group element in the element history. This function updates the
     * group element that is currently valid until the end of time, and close it
     * as of the close date specified.
     * Implement thid method if isSupportingGroups() return true
     * 
     * @param con
     *            connection to use to close the element
     * @param elementToClose
     *            group element to be closed
     * @param closeDate
     *            date as of which the group element should be closed
     * @throws SQLException
     *             if the query fails
     */
    protected void closeGroup(Connection con, IMGroup elementToClose, Date closeDate)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the latest version on a group element in the element history. The
     * group element is valid from the "fromDate" until the end of time.
     * Implement thid method if isSupportingGroups() return true
     * 
     * @param con
     *            connection to use to insert the new row
     * @param elementToCreate
     *            group element to add the group element history
     * @param fromDate
     * @param sequenceSQL
     *            TODO
     * @throws SQLException
     *             if the query fails
     */
    protected void createNewGroup(Connection con, IMGroup elementToCreate, Date fromDate,
            IMassDMLFormatter formatter) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Close a group member element in the element history. This function
     * updates the group element member that is currently valid until the end of
     * time, and close it as of the close date specified.
     * Implement thid method if isSupportingGroups() return true
     * 
     * @param con
     *            connection to use to close the element
     * @param parentGroup
     *            the parent of member
     * @param memberKey
     *            the member to close
     * @param closeDate
     *            date as of which the group member element should be closed
     * @throws SQLException
     *             if the query fails
     */
    protected void closeGroupMember(Connection con, IMGroup parentGroup, Long memberKey,
            Date closeDate) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the latest version on a group element in the element history. The
     * group element is valid from the "fromDate" until the end of time.
     * Implement thid method if isSupportingGroups() return true
     * 
     * @param con
     *            connection to use to insert the new row
     * @param parentGroup
     *            the parent of the member to create
     * @param memberKey
     *            the member to create
     * @param fromDate
     * @throws SQLException
     *             if the query fails
     */
    protected void createNewGroupMember(Connection con, IMassDMLFormatter formatter,
            IMGroup parentGroup, Long memberKey, Date fromDate) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the dictionary object
     * 
     * @return the dictionary object
     */
    protected final IDictionary getDictionary() {
        return this.dictionary;
    }

    /**
     * Returns the type of element handled by the implementation
     * 
     * @return the type of element handled by the implementation
     */
    protected abstract IElementType getElementType();

    /**
     * @see IInitializable#init()
     */
    public void init() {
        super.init();
        setDictionary(getManager().getComponent(Dictionary.COMP_INFO));
    }

    /**
     * Returns true if the dictionary also stores groups for the element type
     * handled by the implementation, false otherwise.
     * 
     * @return true if the dictionary stores groups for the element type.
     */
    protected boolean isSupportingGroups() {
        return false;
    }

    /**
     * 
     * @param it
     * @param lastDate
     * @param newDate
     * @param s
     * @param t
     * @throws HibernateException
     * @throws DictionaryException
     * @throws SQLException
     */
    protected void processChangedElements(IDictionaryIterator<IMElement> it, Date lastDate, Date newDate,
            Session s, Transaction t) throws HibernateException, DictionaryException, SQLException {
        IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);
        final Connection con = s.connection();
        while (it.hasNext()) {
            final IElement newElement = it.next();
            closeElement(con, newElement, newDate);
            createNewElement(con, newElement, newDate, formatter);
        }
    }

    /**
     * @param it
     * @param lastDate
     * @param newDate
     * @param s
     * @param t
     * @throws HibernateException
     * @throws DictionaryException
     * @throws SQLException
     */
    protected void processChangedGroups(IDictionaryIterator<IMGroup> it, Date lastDate, Date newDate, Session s,
            Transaction t) throws HibernateException, DictionaryException, SQLException {
        IMassDMLFormatter formatter = MassDMLUtils.makeFormatter(s);
        final Connection con = s.connection();
        con.setAutoCommit(false);
        while (it.hasNext()) {
            final IMGroup newGroup = it.next();
            closeGroup(con, newGroup, newDate);
            createNewGroup(con, newGroup, newDate, formatter);

            IDictionaryIterator<Long> membersRemoved = null;
            IDictionaryIterator<Long> membersAdded = null;
            IGroupChanges groupChanges = null;
            try {
                groupChanges = newGroup.getChanges(getElementType(), lastDate, newDate);
                membersRemoved = groupChanges.getKeysOfRemovedMembers();
                while (membersRemoved.hasNext()) {
                    closeGroupMember(con, newGroup, membersRemoved.next(), newDate);
                }
                membersAdded = groupChanges.getKeysOfAddedMembers();
                while (membersAdded.hasNext()) {
                    createNewGroupMember(con, formatter, newGroup, membersAdded.next(), newDate);
                }
            } finally {
                if (groupChanges != null) {
                    groupChanges.close();
                }
            }
        }
        t.commit();
    }

    /**
     * @see IResourceCache#refresh()
     */
    public void refresh() {
        Session session = null;
        Transaction transaction = null;
        final IDictionary dico = getDictionary();
        try {
            session = getDataSource().getSession();
            session.connection().setAutoCommit(false);

            Date lastProcessedUpdate = getLastUpdateTime();
            Date newUpdateTime = dico.getEarliestConsistentTimeSince(lastProcessedUpdate);
            
            while (newUpdateTime != null && newUpdateTime.after(lastProcessedUpdate)) {
                transaction = session.beginTransaction();
                IPredicate changedCondiction = dico.changedCondition(lastProcessedUpdate, newUpdateTime);
                IPredicate typePredicate = dico.condition(getElementType());
                CompositePredicate queryPredicate = new CompositePredicate(BooleanOp.AND, changedCondiction);
                queryPredicate.addPredicate(typePredicate);

                IDictionaryIterator<IMElement> changedElementIt = dico.query(queryPredicate, newUpdateTime, null, null);
                try {
                    processChangedElements(changedElementIt, lastProcessedUpdate, newUpdateTime, session, transaction);
                } finally {
                    changedElementIt.close();
                }

                if (isSupportingGroups()) {
                    IDictionaryIterator<IMGroup> changedGroupIt = dico.getEnumeratedGroups(changedCondiction, getElementType(), newUpdateTime, null);
                    try {
                        processChangedGroups(changedGroupIt, lastProcessedUpdate, newUpdateTime, session, transaction);
                    } finally {
                        changedGroupIt.close();
                    }

                    changedGroupIt = dico.getStructuralGroups(changedCondiction, getElementType(), newUpdateTime, null);
                    try {
                        processChangedGroups(changedGroupIt, lastProcessedUpdate, newUpdateTime, session, transaction);
                    } finally {
                        changedGroupIt.close();
                    }
                }
                transaction.commit();
                setLastUpdateTime(newUpdateTime);

                // Prepare for the next round
                lastProcessedUpdate = newUpdateTime;
                newUpdateTime = dico.getEarliestConsistentTimeSince(lastProcessedUpdate);
                
                getLog().debug("lastProcessedUpdate = " + printFormat(lastProcessedUpdate));
                getLog().debug("newUpdateTime = " + printFormat(newUpdateTime));
            }
        } catch (Throwable e) {
            HibernateUtils.rollbackTransation(transaction, getLog());
        } finally {
            HibernateUtils.closeSession(session, getLog());
        }
    }
    
    private String printFormat(Date date){
        if(date == null){
            return null;
        }
        return date.getTime() + ", " + date;
    }

    /**
     * Sets the dictionnary object
     * 
     * @param dictionary
     *            dictionary object to set
     */
    protected final void setDictionary(IDictionary dictionary) {
        this.dictionary = dictionary;
    }
    
    protected String getStringValue(IElement element, IElementField field, String defaultValue) {
        String value = (String) element.getValue(field);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    protected String getStringValue(IElement element, IElementField field) {
        return getStringValue(element, field, UNKNOWN_FIELD);
    }
}
