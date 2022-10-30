/*
 * Created on Apr 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller;

import java.util.Collection;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.framework.messaging.IMessageHandler;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/IEnrollmentManager.java#1 $
 */

public interface IEnrollmentManager {

    PropertyKey<IEnrollerFactory> ENROLLER_FACTORY =
            new PropertyKey<IEnrollerFactory>("EnrollerFactory");

    PropertyKey<IDictionary> DICTIONARY =
            new PropertyKey<IDictionary>("Dictionary");
    
    PropertyKey<IMessageHandler> MESSAGE_HANDLER =
            new PropertyKey<IMessageHandler>("enrollment message handler");

    
	/**
	 * create a new enrollment, this is "enroll" command from enrollmgr.
	 * 
	 * @param data
	 * @throws InvalidConfigurationException if the <code>data</code> is missing information 
	 * 				or invalid information
	 * @throws DuplicateEnrollmentException if the realm is already created
	 * @throws EnrollerCreationException if the enroller can't be created
	 * @throws EnrollmentValidationException if the enrollment information is invalid
	 * @throws DictionaryException if any problem from dictionary
	 */
    public void createRealm(IRealmData data) throws 
		    InvalidConfigurationException, 
		    DuplicateEntryException,
			EnrollerCreationException,
			EnrollmentValidationException,
			EnrollmentThreadException,
			DictionaryException;

    /**
     * update a already exist enrollment
     * 
     * @param data
     * @throws InvalidConfigurationException if the <code>data</code> is missing information 
     *              or invalid information
     * @throws EntryNotFoundException if the realm doesn't exist
     * @throws EnrollerCreationException if the enroller can't be created
     * @throws EnrollmentValidationException if the enrollment information is invalid
     * @throws DictionaryException if any problem from dictionary
     */
	public void updateRealm(IRealmData data) throws 
			InvalidConfigurationException,
			EntryNotFoundException,
			EnrollerCreationException,
			EnrollmentValidationException,
			EnrollmentThreadException,
			DictionaryException;

	/**
	 * delete a existing enrollment
	 * 
	 * @param data
	 * @throws InvalidConfigurationException if the <code>data</code> is missing information 
     *              or invalid information
	 * @throws EntryNotFoundException if the realm doesn't exist
	 * @throws DictionaryException if any problem from dictionary
	 */
	public void deleteRealm(IRealmData data) throws 
			InvalidConfigurationException,
			EntryNotFoundException,
			EnrollmentThreadException,
			DictionaryException;

	/**
	 * pull the data from outside, this is "sync" command from enrollmgr.
	 * 
	 * @param realm
	 * @throws InvalidConfigurationException if the <code>data</code> is missing information 
     *              or invalid information
     * @throws EntryNotFoundException if the realm doesn't exist
     * @throws EnrollmentFailedException if any problem while pulling the data from outside
     * @throws DictionaryException if any problem from dictionary
	 */
	public void enrollRealm(IRealmData data) throws 
			InvalidConfigurationException,
			EntryNotFoundException,
			EnrollmentValidationException,
			EnrollmentSyncException,
			EnrollmentThreadException,
			DictionaryException;
	
	/**
	 * get a list of all enrollment
	 * @return
	 * @throws DictionaryException if any problem from dictionary
	 */
	public Collection<IEnrollment> getRealms() throws 
			DictionaryException;

	/**
	 * get a specific enrollment that the name matches <code>name</code>
	 * @param name
	 * @return
	 * @throws InvalidConfigurationException if the name is missing (null)
	 * @throws EntryNotFoundException if there is no matching enrollment
	 * @throws DictionaryException if any problem from dictionary
	 */
	public IEnrollment getRealm(String name) throws
			InvalidConfigurationException,
			EntryNotFoundException,
			DictionaryException;

	public void addColumn(IColumnData data) throws 
			InvalidConfigurationException,
			DuplicateEntryException,
			DictionaryException;

	public void delColumn(String logicalName, String elementType) throws 
			InvalidConfigurationException,
			EntryNotFoundException,
			DictionaryException;

	public Collection<IElementField> getColumns() throws 
			DictionaryException;

}
