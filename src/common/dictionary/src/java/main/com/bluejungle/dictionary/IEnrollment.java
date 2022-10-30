/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/IEnrollment.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This interface defines a contract for dictionary enrollments.
 * Enrollments let users retrieve elements and groups by key, and
 * create new elements and groups.
 */

public interface IEnrollment {

    /**
     * Returns the <code>IDictionary</code> with which this enrollment
     * is associated.
     * @return  the <code>IDictionary</code> with which this enrollment
     * is associated.
     */
    IDictionary getDictionary();

    /**
     * Opens a new session for updating elements associated with
     * this enrollment. 
     * @return a new session for updating elements associated with
     * this enrollment.  
     * @throws DictionaryException when the operation cannot complete.
     */
    IEnrollmentSession createSession() throws DictionaryException;

    /**
     * Retrieves the domain name of this enrollment.
     *
     * @return the domain name of this enrollment.
     */
    String getDomainName();

    /**
     * Sets the domain name of this enrollment.
     *
     * @param domainName the new domain name for this enrollment.
     */
    void setDomainName( String domainName );

    /**
     * Gets the enrollment type.
     * @return the enrollment type.
     */
    String getType();

    /**
     * Sets the enrollment type.
     * @param type The type of this enrollment.
     */
    void setType( String type );

    /**
     * Deletes the property with the specified name from
     * this enrollment.
     * @param name the name of the property to delete.
     */
    void deleteProperty( String name );

    /**
     * Deletes all properties from this enrollment.
     * Can be used for enrollment deletion and update
     */
    void deleteAllProperties();

    /**
     * Obtains an array of <code>String</code> values representing
     * names of enrollment parameters with binary values.
     * @return an array of <code>String</code> values representing
     * names of enrollment parameters with binary values.
     */
    String[] getBinPropertyNames();

    /**
     * Obtains a value of a binary enrollment parameter,
     * such as an enrollment-specific cookie.
     * @param name the name of the enrollment parameter to return. 
     * @return a value of a binary enrollment parameter.
     */
    byte[] getBinProperty( String name );

    /**
     * Sets a value of a binary enrollment parameter,
     * such as an enrollment-specific cookie.
     * @param name the name of the enrollment parameter to set.
     * @param val the new value of the enrollment parameter.
     */
    void setBinProperty( String name, byte[] val );

    /**
     * Obtains an array of <code>String</code> values representing
     * names of enrollment parameters with string values.
     * @return an array of <code>String</code> values representing
     * names of enrollment parameters with string values.
     */
    String[] getStrPropertyNames();

    /**
     * Obtains a value of a <code>String</code> enrollment parameter,
     * such as host name.
     * @param name the name of the enrollment parameter to return. 
     * @return a value of a <code>String</code> enrollment parameter.
     */
    String getStrProperty( String name );

    /**
     * Sets a value of a <code>String</code> enrollment parameter,
     * such as host name.
     * @param name the name of the enrollment parameter to set.
     * @param val the new value of the enrollment parameter.
     */
    void setStrProperty( String name, String val );

    /**
     * Obtains an array of <code>String[]</code> values representing
     * names of enrollment parameters with string array values.
     * @return an array of <code>String</code> values representing
     * names of enrollment parameters with string array values.
     */
    String[] getStrArrayPropertyNames();

    /**
     * Obtains a value of a <code>String[]</code> enrollment parameter.
     * @param name the name of the enrollment parameter to return. 
     * @return a value of a <code>String</code> enrollment parameter.
     */
    String[] getStrArrayProperty( String name );

    /**
     * Sets a value of a <code>String[]</code> enrollment parameter.
     * @param name the name of the enrollment parameter to set.
     * @param val the new value of the enrollment parameter.
     */
    void setStrArrayProperty( String name, String val[] );

    /**
     * Obtains an array of <code>String</code> values representing
     * names of enrollment parameters with numeric values.
     * @return an array of <code>String</code> values representing
     * names of enrollment parameters with numeric values.
     */
    String[] getNumPropertyNames();

    /**
     * Obtains a value of a numeric enrollment parameter, such as port.
     * @param name the name of the enrollment parameter to return. 
     * @return a value of a numeric enrollment parameter.
     */
    long getNumProperty( String name );

    /**
     * Sets a value of a numeric enrollment parameter, such as port.
     * @param name the name of the enrollment parameter to set.
     * @param val the new value of the enrollment parameter.
     */
    void setNumProperty( String name, long val );

    /**
     * Given a dictionary key, gets the corresponding group or element.
     * @param key the key the group or element for which to retrieve.
     * @param asOf the as-of date for the query.
     * @return the group or element corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMElementBase getByKey(DictionaryKey key, Date asOf) throws DictionaryException;

    /**
     * Makes a new element of the specified type with the given key,
     * and returns its mutable version to the caller. The element
     * needs to be saved to the dictionary using
     * IEnrollmentSession.saveElements.
     *
     * This method will not check if the element with the specified key
     * already exists. Keys are expected to be unique - in situations
     * when they are not, an exception will be thrown from the
     * IConfigurationSession.saveElements method.
     *
     * @param path the path to the element to create.
     * @param type The type of the element to create.
     * @param key the key of the element to create.
     * @return a newly created element with the specified parameters.
     */
    IMElement makeNewElement( DictionaryPath path, IElementType type, DictionaryKey key );

    /**
     * Given a dictionary key, gets the corresponding element.
     * @param key the key the element for which to retrieve.
     * @param asOf the as-of date for the query.
     * @return the element corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMElement getElement(DictionaryKey key, Date asOf) throws DictionaryException;

    /**
     * Makes a new structural group with the given key, and returns
     * its mutable version to the caller. The resulting group
     * needs to be saved to the dictionary using
     * IEnrollmentSession.saveElements.
     *
     * This method will not check if the group with the specified key
     * or the specified path already exists. Group keys are expected
     * to be unique - in situations when they are not, an exception
     * will be thrown from the IEnrollmentSession.saveElements method.
     *
     * @param path path to the group from the root.
     * @param key the key of the group to create.
     * @return a newly created structural group with the specified path.
     */
    IMGroup makeNewStructuralGroup( DictionaryPath path, DictionaryKey key );

    /**
     * Makes a new enumerated group with the given key, and returns
     * its mutable version to the caller. The resulting group
     * needs to be saved to the dictionary using
     * IEnrollmentSession.saveElements.
     *
     * This method will not check if the group with the specified key
     * or the specified path already exists. Group keys are expected
     * to be unique - in situations when they are not, an exception
     * will be thrown from the IEnrollmentSession.saveElements method.
     *
     * @param path path to the group from the root.
     * @param key the key of the group to create.
     * @return a newly created enumerated group with the specified path.
     */
    IMGroup makeNewEnumeratedGroup( DictionaryPath path, DictionaryKey key );

    /**
     * Given a dictionary key, gets the corresponding group.
     * @param key the key the group for which to retrieve.
     * @param asOf the as-of date for the query.
     * @return the group corresponding to the given key.
     * @throws DictionaryException when the operation cannot be completed.
     */
    IMGroup getGroup(DictionaryKey key, Date asOf) throws DictionaryException;

    /**
     * Given a <code>Collection</code>of <code>DictionaryPath</code>
     * objects, this method either retrieves the corresponding
     * elements/groups, or creates placeholder references for them.
     * The process controlling the enrollment could then create references
     * and save the resulting objects.
     *
     * @param paths A <code>Collection</code> of <code>DictionaryPath</code>
     * objects representing the paths for which the references should be retrieved.
     * @return a <code>List</code> of elements of type <code>IReferenceable</code>
     * corresponding to the specified paths.
     * @throws DictionaryException when it is not possible to create
     * a provisional reference.
     */
    List<IReferenceable> getProvisionalReferences(Collection<DictionaryPath> paths) throws DictionaryException;

    /**
     * Looks up a field by its external name.
     * @param type the element type for which to do a lookup.
     * @param externalName the external name of the field.
     * @return the field corresponding to the specified external name.
     */
    IElementField[] lookupField( IElementType type, String externalName );

    /**
     * Gets the external name assigned by this enrollment to the
     * specified field.
     *
     * @param field The field the external name for which is to be
     * retrieved.
     * @return the external name of the specified field, or null if
     * the specified field does not have an external name defined
     * in this enrollment.
     */
    String getExternalName( IElementField field );

    /**
     * Gets an array of <code>String</code> objects representing
     * external names defined in this enrollment for fields of the
     * specified <code>IElementType</code>. If no external names
     * are defined, this method returns an empty array.
     * The returned array is sorted alphabetically.
    *
     * @param type the type for fields of which to get the external names.
     * @return an array of external names defined in this enrollment
     * for fields of the specified type.
     *
     */
    String[] getExternalNames( IElementType type );

    /**
     * Associates a field with an external name.
     * @param field the field for which to set an external name.
     * @param externalName the new external name of the field.
     */
    void setExternalName( IElementField field, String externalName );

    /**
     * Clears the external name of the specified field.
     * @param field the field the external name for which is to be cleared.
     */
    void clearExternalName( IElementField field );

    /**
     * Calling this method clears out all external names added to
     * all fields of a particular type.
     *
     * @param type the type the external names for fields of which
     * are to be removed.
     */
    void clearExternalNames( IElementType type );

    /**
     * Calling this method clears out all external names added to
     * all fields of a particular type.
     * Can be used by enrollment deletion and update
     */
    void clearAllExternalNames();
    
    /**
     * Get the recurring property of Enrollment
     * @return boolean true if isRecurring, or false if it is one time enrollment  
     */ 
    boolean getIsRecurring();

    /**
     * Set the recurring property of Enrollment
     * @param isRecurring the boolean value whether the enrollment is recurring 
     */ 
    void setIsRecurring(boolean isRecurring);

    /**
     * Get the isActive property of Enrollment
     * @return boolean true if active, or false if it is not active  
     */ 
    boolean getIsActive();
        
    /**
     * Set the isActive property of Enrollment
     * @param isActive the boolean value whether the enrollment is active 
     */ 
    void setIsActive(boolean isActive);
    
    /**
     * Get the enrollment status
     * @return the status of current enrollment
     */
    IUpdateRecord getStatus() throws DictionaryException;
    
    void setNextSyncTime(Calendar nextSyncTime);
    
    Calendar getNextSyncTime();
    
}
