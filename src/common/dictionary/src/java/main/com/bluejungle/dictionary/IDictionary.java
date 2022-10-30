package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IDictionary.java#1 $
 */

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;

/**
 * This interface provides contract for read-only interactions
 * with the dictionary.
 *
 * @author sergey
 */

public interface IDictionary {

    /**
     * Given a condition and an as-of date, returns an
     * <code>IElementIterator</code> with <code>IElement</code>
     * objects matching the specified condition.
     *
     * @param condition an <code>IPredicate</code> defining the condition.
     * @param asOf the date as of which the caller needs the results.
     * When this argument is null, the current time is used.
     * @param order an array of <code>Order</code> objects specifying
     * the ordering of the records to be returned.
     * This argument may be null, in which case the order is unspecified.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all records are returned.
     * If this argument is non-null, order by ID is used, whether order 
     * is specified, or not.
     * @return a <code>IElementIterator</code> with <code>IElement</code>
     * objects that match the specified condition as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMElement> query(IPredicate condition, Date asOf, Order[] order, Page page) throws DictionaryException;

    /**
     * Given a condition and an as-of date, returns an
     * <code>IElementIterator</code> with <code>IElement</code>
     * objects matching the specified condition.
     *
     * @param condition an <code>IPredicate</code> defining the condition.
     * @param asOf the date as of which the caller needs the results.
     * When this argument is null, the current time is used.
     * @param order an array of <code>Order</code> objects specifying
     * the ordering of the records to be returned.
     * This argument may be null, in which case the order is unspecified.
     * @param limit A non-zero limit for max rows to return. Rows are not 
     * ordered, unless order argument is provided.
     * @return a <code>IElementIterator</code> with <code>IElement</code>
     * objects that match the specified condition as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMElement> query(IPredicate condition, Date asOf, Order[] order, int limit) throws DictionaryException;

    /**
     * Given a condition, an as-of date, and a list of fields, 
     * returns an <code>IFieldsIterator</code> with data
     * for the specified fields of objects matching the specified condition.
     *
     * @param condition an <code>IPredicate</code> defining the condition.
     * @param fields an array of <code>IElementField</code> objects
     * describing the fields that need to be retrieved.
     * @param asOf the date as of which the caller needs the results.
     * When this argument is null, the current time is used.
     * @param order an array of <code>Order</code> objects specifying
     * the ordering of the records to be returned.
     * This argument may be null, in which case the order is unspecified.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all records are returned.
     * If this argument is non-null, order by ID is used, whether order 
     * is specified, or not.
     * @return a <code>IFieldsIterator</code> with field data fof
     * objects that match the specified condition as of the specific date.
     *
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<ElementFieldData> queryFields(IElementField[] fields, IPredicate condition, Date asOf, Order[] order, Page page) throws DictionaryException;

    /**
     * Given a condition and an as-of date, returns an
     * <code>IInternalKeyIterator</code> with internal keys of
     * elements matching the specified condition.
     *
     * @param condition an <code>IPredicate</code> defining the condition.
     * @param asOf asOf the date as of which the caller needs the results.
     * When this argument is null, the current time is used.
     * @return an <code>IInternalKeyIterator</code> with internal keys
     * of elements matching the specified condition.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<Long> queryKeys(IPredicate condition, Date asOf) throws DictionaryException;

    /**
     * Retrieves the list of all structural groups
     * starting at the specific path and satisfying
     * the specific name template.
     *
     * This is a facade method on top of the more general
     * <code>getStructuralGroups(IPredicate, IElementType, Date, Page)</code>
     * method.
     *
     * @param path the path from which to start the search.
     * This argument may not be null, but it may be set to
     * the root dictionary path.
     * @param filter A filter string with optional '%' characters.
     * Only groups with names matching the filter will be returned.
     * This argument may not be null.
     * @param type If set, this argument specifies that the group
     * must contain at least one element of the specified type.
     * This argument may be null, in which case all groups
     * matching the filter are returned.
     * @param asOf the asOf time for the query.
     * When this argument is null, the current time is used.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all groups are returned.
     * @return the list of all groups starting at the specific path
     * and satisfying the specific name template, as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getStructuralGroups( DictionaryPath path, String filter, IElementType type, Date asOf, Page page ) throws DictionaryException;

    /**
     * Retrieves the list of all structural groups
     * satisfying the specified condition.
     *
     * @param condition an <code>IPredicate</code> defining a constraint
     * for all groups this method is to return.
     * @param type If set, this argument specifies that the group
     * must contain at least one element of the specified type.
     * This argument may be null, in which case all groups
     * matching the filter are returned.
     * @param asOf the asOf time for the query.
     * When this argument is null, the current time is used.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all groups are returned.
     * @return the list of all groups starting at the specific path
     * and satisfying the specific name template, as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getStructuralGroups( IPredicate condition, IElementType type, Date asOf, Page page ) throws DictionaryException;

    /**
     * Retrieves the list of all enumerated groups
     * satisfying the specific name template.
     *
     * This method is a facade for a more generic
     * <code>getEnumeratedGroups(IPredicate, IElementType, Date, Page)</code>
     * method.
     *
     * @param condition an <code>IPredicate</code> defining a constraint
     * for all groups this method is to return.
     * @param type If present, this argument specifies that the group
     * must contain at least one element of the specified type.
     * This argument may be null, in which case all groups
     * matching the filter are returned.
     * @param asOf the asOf time for the query.
     * When this argument is null, the current time is used.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all groups are returned.
     * @return the list of all groups starting at the specific path
     * and satisfying the specific name template, as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getEnumeratedGroups( String filter, IElementType type, Date asOf, Page page ) throws DictionaryException;

    /**
     * Retrieves the list of all enumerated groups
     * satisfying the specific condition.
     *
     * @param filter A filter string with optional '%' characters.
     * Only groups with names matching the filter will be returned.
     * @param type If present, this argument specifies that the group
     * must contain at least one element of the specified type.
     * This argument may be null, in which case all groups
     * matching the filter are returned.
     * @param asOf the asOf time for the query.
     * When this argument is null, the current time is used.
     * @param page The page of the results to be returned.
     * This argument may be null, in which case all groups are returned.
     * @return the list of all groups starting at the specific path
     * and satisfying the specific name template, as of the specific date.
     * @throws DictionaryException when the operation cannot complete.
     */
    IDictionaryIterator<IMGroup> getEnumeratedGroups( IPredicate condition, IElementType type, Date asOf, Page page ) throws DictionaryException;

    /**
     * Returns an <code>IGroup</code> object corresponding to the
     * specified path. A group could be a node in a hierarchy, or
     * an enumerated group. In either case a single path string
     * is sufficient to identify the group uniquely.
     *
     * @param path a <code>DictionaryPath</code> to the group.
     * @param asOf the <code>Date</code> as of which to query the group.
     * When this argument is null, the current time is used.
     * @return the <code>IGroup</code> corresponding to the path.
     * @throws DictionaryException when the operation cannot complete.
     */
    IMGroup getGroup(DictionaryPath path, Date asOf) throws DictionaryException;

    /**
     * Given a domain name, finds and returns the corresponding enrollment.
     * @param domainName the name of the enrollment's domain.
     * @return the enrollment corresponding to the given domain.
     * @throws DictionaryException when the operation cannot complete.
     */
    IEnrollment getEnrollment( String domainName ) throws DictionaryException;

    /**
     * Makes a new enrollment with the specified domain.
     * The enrollments needs to be saved to the dictionary
     * using IConfigurationSession.saveEnrollment.
     *
     * This method will not check if the enrollment for the specified
     * domain already exists. Domains are expected to be unique -
     * in situations when they are not, an exception will be thrown
     * from the IConfigurationSession.saveEnrollment method.
     *
     * @param domainName the name of the enrollment's domain.
     * @return a newly created IEnrollmentS with the specified name.
     * @throws DictionaryException when the operation cannot complete.
     */
    IEnrollment makeNewEnrollment( String domainName ) throws DictionaryException;

    /**
     * Obtains a list of all known enrollments.
     * @return a list of all known enrollments.
     * @throws DictionaryException when the operation cannot complete.
     */
    Collection<IEnrollment> getEnrollments() throws DictionaryException;

    /**
     * Retrieves all dictionary update records.
     * @param startDate the update records returned will be as of this date, inclusive.
     * @param endDate the update records returned will be up to this date, exclusive.
     * @return all dictionary update records.
     * @throws DictionaryException when the operation cannot complete.
     */
    List<IUpdateRecord> getUpdateRecords( Date startTime, Date endTime ) throws DictionaryException;

    /**
     * Given an internal key, gets the corresponding group or element.
     * @param key the key of the group or element to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the group or element corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMElementBase getByKey(Long key, Date asOf) throws DictionaryException;

    /**
     * Given an internal key, gets the corresponding element.
     * @param key the key of the element to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the element corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMElement getElement(Long key, Date asOf) throws DictionaryException;

    /**
     * Given an internal key, gets the corresponding group.
     * @param key the key of the group to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the group corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMGroup getGroup(Long key, Date asOf) throws DictionaryException;

    /**
     * Given a unique name, gets the corresponding group or element.
     * If the specified name does not identify a group or an element
     * uniquely, an exception is thrown.
     * @param uniqueName the unique name of the group or element to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the group or element corresponding to the given unique name.
     * @throws DictionaryException when the operation cannot be completed,
     * or the specified name does not identify an element uniquely.
     */
    IMElementBase getByUniqueName(String uniqueName, Date asOf) throws DictionaryException;

    /**
     * Given a unique name, gets the corresponding element.
     * @param uniqueName the unique name of the element to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the element corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed,
     * or when the the specified unique name does not correspond to
     * a single <code>IMElement</code>.
     */
    IMElement getElement(String uniqueName, Date asOf) throws DictionaryException;

    /**
     * Given a unique name, gets the corresponding group.
     * @param uniqueName the unique name of the group to retrieve.
     * @param asOf the as-of date for the query.
     * When this argument is null, the current time is used.
     * @return the group corresponding to the unique name.
     * @throws DictionaryException when the operation cannot be completed,
     * or when the the specified unique name does not correspond to
     * a single <code>IMGroup</code>.
     */
    IMGroup getGroup(String uniqueName, Date asOf) throws DictionaryException;

    /**
     * Given a <code>Collection</code> of IDs and an as-of date,
     * returns an <code>IElementBaseIterator</code> with the elements
     * or groups that have the corresponding IDs.
     * @param ids the IDs the elements or groups for which to return.
     * All elements of this <code>Collection</code> must be of type <code>Long</code>.
     * @param asOf the as-of <code>Date</code> for the query.
     * When this argument is null, the current time is used.
     * @return an <code>IElementBaseIterator</code> with the elements
     * or groups corresponding to the IDs specified in the <code>Collection</code>.
     * @throws DictionaryException
     */
    IDictionaryIterator<IMElementBase> getElementsById(Collection<Long> ids, Date asOf) throws DictionaryException;
    
    /**
     * 
     * 
     * @param groupId
     * @return
     */
    IDictionaryIterator<DictionaryPath> queryReferenceMembersByGroupId(long groupId) throws DictionaryException;

    /**
     * Retrieves the earliest time strictly after the <code>startTime</code>
     * at which the dictionary was in a consistent state. A new state is considered
     * consistent if at least one enrollment update has succeeded, and no enrollments
     * have failed.
     * @param startTime the time after which we're looking for a consistent state.
     * @return the earliest time strictly after the <code>startTime</code>
     * at which the dictionary was in a consistent state.
     * @throws DictionaryException when the operation cannot complete.
     */
    Date getEarliestConsistentTimeSince( Date startTime ) throws DictionaryException;

    /**
     * Retrieves the latest time that may be used in as-of queries.
     * @return the latest time that may be used in as-of queries.
     * @throws DictionaryException when the operation cannot complete.
     */
    Date getLatestConsistentTime() throws DictionaryException;

    /**
     * Are there at least one failed enrollment attempts in the dict_updates database?
     * Special case: if the input domainName matches with the domain name of the failed enrollment, 
     * that enrollment is excluded from this query.
     * 
     * @param domainName Domain name to be excluded from query.  If this is null, no domain name is excluded from query.
     * @return true if there is a failed enrollment that is still active.  false if not.
     */
    public boolean isThereFailedEnrollment(String domainName) throws DictionaryException;
    
    /**
     * Opens a new session for updating the dictionary configuration.
     * @return a new session for updating the dictionary configuration.
     * @throws DictionaryException when the operation cannot complete.
     */
    IConfigurationSession createSession() throws DictionaryException;

    /**
     * Obtains a <code>Collection</code> of all types
     * defined in the dictionary.
     *
     * @return a <code>Collection</code> of all types
     * defined in the dictionary.
     * @throws DictionaryException when the operation cannot complete.
     */
    Collection<IElementType> getAllTypes() throws DictionaryException;

    /**
     * Gets a mutable element type if it exists.
     * @param name the name of the type.
     * @return the mutable element type.
     * @throws IllegalArgumentException if the element with the
     * specified name does not exist.
     * @throws DictionaryException when the operation cannot complete.
     */
    IMElementType getType( String name ) throws DictionaryException;

    /**
     * Makes a new element type with the specified name.
     * The type needs to be saved to the dictionary
     * using IConfigurationSession.saveTypes.
     *
     * This method will not check if the type with the specified name
     * already exists. Names are expected to be unique - in situations
     * when they are not, an exception will be thrown from the
     * IConfigurationSession.saveTypes method.
     *
     * @param name The name of the new type.
     * @return a newly created type with the specified name.
     * @throws DictionaryException when the operation cannot complete.
     */
    IMElementType makeNewType( String name ) throws DictionaryException;

    /**
     * Cleans up the resources associated with this dictionary.
     * @throws DictionaryException if the operation cannot complete.
     */
    void close() throws DictionaryException;

    /**
     * Implementations of this method return a query condition for equality
     * to the specified <code>DictionaryPath</code>.
     * @param path the <code>DictionaryPath</code> with which to query.
     * @param direct a flag indicating whether only direct (<code>true</code>)
     * or both direct and indirect (<code>false</code>) elements
     * need to be included.
     * @return a query condition for equality to the specified
     * <code>DictionaryPath</code>.
     */
    IPredicate condition(DictionaryPath path, boolean direct);

    /**
     * Implementations of this method return a query condition for equality
     * to the specified <code>IElementType</code>.
     * @param elementType the <code>IElementType</code> with which to query.
     * @return a query condition for equality to the specified
     * <code>IElementType</code>.
     */
    IPredicate condition(IElementType elementType);

    /**
     * Implementations of this method return a query condition for equality
     * to the specified <code>IEnrollment</code>.
     * @param enrollment the <code>IEnrollment</code> with which to query.
     * @return a query condition for equality to the specified
     * <code>IEnrollment</code>.
     */
    IPredicate condition(IEnrollment enrollment);

    /**
     * Implementations of this method return a query condition for
     * querying for elements or groups that have changed between
     * the two specific dates.
     *
     * @param startDate the changes returned based on this condition
     * will be as of this date, inclusive.
     * @param endDate the changes returned will be up to this date,
     * exclusive.
     * @return a query condition for querying for elements or groups
     * that have changed on or after the <code>startDate</code>
     * and strictly before the <code>endDate</code>.
     */
    IPredicate changedCondition(Date startDate, Date endDate);

    /**
     * Implementations of this method return an <code>IAttribute</code>
     * for use in relations constraining the internal key.
     *
     * @return an <code>IAttribute</code> for use in relations
     * constraining the internal key.
     */
    IAttribute internalKeyAttribute();

    /**
     * Implementations of this method return an <code>IAttribute</code>
     * for use in relations constraining the display name.
     *
     * @return an <code>IAttribute</code> for use in relations
     * constraining the display name.
     */
    IAttribute displayNameAttribute();

    /**
     * Implementations of this method return an <code>IAttribute</code>
     * for use in relations constraining the unique name.
     *
     * @return an <code>IAttribute</code> for use in relations
     * constraining the unique name.
     */
    IAttribute uniqueNameAttribute();
    
    /**
     * This constant represents the path field. It is used for
     * unification purposes in cases when you try to sort by path
     * or get the value of the path using metadata-driven approaches.
     * This field is of limited use otherwise: for example, you cannot
     * set the path by using this field.
     */
    IElementField PATH_FIELD = new IElementField() {
        /**
         * @see IAttribute#getObjectSubTypeName()
         */
        public String getObjectSubTypeName() {
            throw new UnsupportedOperationException("getObjectSubTypeName");
        }
        /**
         * @see IAttribute#getObjectTypeName()
         */
        public String getObjectTypeName() {
            throw new UnsupportedOperationException("getObjectTypeName");
        }
        /**
         * @see IExpression#acceptVisitor(IExpressionVisitor, IExpressionVisitor.Order)
         */
        public void acceptVisitor(IExpressionVisitor visitor, IExpressionVisitor.Order order) {
            visitor.visit(this);
        }
        /**
         * @see IExpression#buildRelation(RelationOp, IExpression)
         */
        public IRelation buildRelation(RelationOp op, IExpression rhs) {
            throw new UnsupportedOperationException("buildRelation");
        }
        /**
         * @see IExpression#evaluate(IArguments)
         */
        public IEvalValue evaluate(IArguments arg) {
            throw new UnsupportedOperationException("evaluate");
        }
        /**
         * @see IElementField#getLabel()
         */
        public String getLabel() {
            return null;
        }
        /**
         * @see IElementField#getName()
         */
        public String getName() {
            throw new UnsupportedOperationException("getName");
        }
        /**
         * @see IElementField#getType()
         */
        public ElementFieldType getType() {
            return ElementFieldType.CS_STRING;
        }
        /**
         * @see IElementField#getValue(IElement)
         */
        public Object getValue(IElement element) {
            return element.getPath().toString();
        }
        /**
         * @see IElementField#setValue(IElement, java.lang.Object)
         */
        public void setValue(IElement element, Object value) {
            throw new UnsupportedOperationException("setValue");
        }
    };

}
