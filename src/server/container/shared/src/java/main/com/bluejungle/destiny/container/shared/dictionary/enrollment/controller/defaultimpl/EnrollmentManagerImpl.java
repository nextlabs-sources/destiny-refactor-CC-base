/*
 * Created on Apr 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentController;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl.DomainGroupEnrollmentWrapperImpl;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.framework.messaging.IMessageHandler;

/**
 * This class manages enrollments. It provides functionality for enrollments to
 * be created, and also manages the periodic synchronization for these
 * enrollments.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/defaultimpl/EnrollmentManagerImpl.java#1 $
 */
public class EnrollmentManagerImpl implements IEnrollmentManager, IInitializable, IConfigurable,
		ILogEnabled, IHasComponentInfo<IEnrollmentManager> {
	
	private static final ComponentInfo<IEnrollmentManager> COMP_INFO = 
    	new ComponentInfo<IEnrollmentManager>(
    		EnrollmentManagerImpl.class, 
    		LifestyleType.SINGLETON_TYPE);

    private IConfiguration configuration;
    private Log log;

    private IDictionary dictionary;
    private IEnrollerFactory enrollerFactory;
    private IEnrollmentController controller;
    
    /**
     * Constructor
     *  
     */
    public EnrollmentManagerImpl() {
        super();
    }
    

    public ComponentInfo<IEnrollmentManager> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @throws IllegalArgumentException
     */
    public void init() throws IllegalArgumentException{
        // Validate all configuration specified:
        if (getConfiguration().get(ENROLLER_FACTORY) == null) {
            throw new IllegalArgumentException("Enroller factory is not specified");
        }
        if (getConfiguration().get(DICTIONARY) == null) {
            throw new IllegalArgumentException("Dictionary is not specified");
        }
     
        // Initialize:
        enrollerFactory = getConfiguration().get(ENROLLER_FACTORY);
        dictionary = getConfiguration().get(DICTIONARY);
        
        IMessageHandler messageHandler = getConfiguration().get(MESSAGE_HANDLER);
        if (messageHandler != null) {
            log.info("Enrollment message handler is enabled. Email will be sent when auto-sync fails");
        } else {
            log.info("Enrollment message handler is disabled.");
        }
        
        // Initialize the enrollment controller:
        try {
        	controller = new EnrollmentController(dictionary, enrollerFactory, messageHandler);
        } catch (DictionaryException e) {
			throw new IllegalArgumentException("Can not create enrollment controller" + e, e);
		}
    }

    public void createRealm(IRealmData realm) throws 
    		InvalidConfigurationException, 
    		DuplicateEntryException,
			EnrollerCreationException,
			EnrollmentValidationException,
			EnrollmentThreadException,
			DictionaryException {
		nullCheck(realm, "realm");
		nullCheck(realm.getName(), "Realm name");
		nullCheck(realm.getType(), "Realm type");

		if (dictionary.getEnrollment(realm.getName()) != null) {
			throw new DuplicateEntryException("enrollment domain name", realm.getName());
		} 
        
        final IConfigurationSession session = dictionary.createSession();
        boolean succeeded = false;
        try {
        	session.beginTransaction();
            IEnrollment newEnrollment = dictionary.makeNewEnrollment(realm.getName());

            // Copy the WS object to the DO:
            newEnrollment.setType(realm.getType().getClassName());
            newEnrollment.setDomainName(realm.getName());

            // Copy the enrollment properties:
            Map<String, String[]> properties = realm.getProperties();

            // Validate enrollment:
            controller.process(newEnrollment, properties);
            session.saveEnrollment(newEnrollment);
            succeeded = true;
        } finally {
        	close(session, succeeded);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#updateRealm(com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData)
     */
    public void updateRealm(IRealmData realm) throws 
    		InvalidConfigurationException,
    		EntryNotFoundException,
    		EnrollerCreationException,
    		EnrollmentValidationException,
    		EnrollmentThreadException,
    		DictionaryException {
    	nullCheck(realm, "realm");
    	nullCheck(realm.getName(), "Realm name");
        
        IEnrollment enrollmentToUpdate = dictionary.getEnrollment(realm.getName());
        if (enrollmentToUpdate == null) {
            throw new EntryNotFoundException("enrollment ", realm.getName());
        }
        
        boolean succeeded = false;
        IConfigurationSession session = dictionary.createSession();
        try {
        	session.beginTransaction();
            // Cleanup the existing enrollment configuration:
        	enrollmentToUpdate.deleteAllProperties();
            // Clear the external name mappings:
        	enrollmentToUpdate.clearAllExternalNames();
        	session.saveEnrollment(enrollmentToUpdate);
            session.commit();
            session.beginTransaction();
            // Verify that the types match. Switching the type is not allowed
            EnrollmentTypeEnumType oldType = EnrollmentTypeEnumType.getByClassName(enrollmentToUpdate.getType());
            if (oldType != realm.getType()) {
				throw new InvalidConfigurationException(
						"The new realm type, " + realm.getType()
                        + ", does not match the exising realm type, " + oldType);
			}

            // Validate enrollment:
            Map<String, String[]> properties = realm.getProperties();
            controller.process(enrollmentToUpdate, properties);
            session.saveEnrollment(enrollmentToUpdate);
            succeeded = true;
        } finally {
        	close(session, succeeded);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#deleteRealm(com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData)
     */
    public void deleteRealm(IRealmData realm) throws 
    		InvalidConfigurationException,
    		EntryNotFoundException,
    		EnrollmentThreadException,
    		DictionaryException {
    	nullCheck(realm, "realm");
    	nullCheck(realm.getName(), "Realm name");
    
        IEnrollment enrollmentToDelete = dictionary.getEnrollment(realm.getName());
        if (enrollmentToDelete == null) {
            throw new EntryNotFoundException("enrollment ", realm.getName());
        }
        
        controller.deleteEnrollmentThread(enrollmentToDelete);
        IConfigurationSession session = dictionary.createSession();
        boolean succeeded = false;
        try {
        	session.beginTransaction();
            session.deleteEnrollment(enrollmentToDelete);
            succeeded = true;
        } finally {
			close(session, succeeded);
		}
    }

    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#enrollRealm(com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData)
     */
    public void enrollRealm(IRealmData realm) throws 
    		InvalidConfigurationException,
    		EntryNotFoundException,
    		EnrollmentValidationException,
    		EnrollmentSyncException,
    		EnrollmentThreadException,
			DictionaryException {
    	nullCheck(realm, "realm");
    	nullCheck(realm.getName(), "Realm name");
        
        IEnrollment enrollment = dictionary.getEnrollment(realm.getName());
        if (enrollment == null) {
            throw new EntryNotFoundException("enrollment ", realm.getName());
        }
        
        controller.sync(enrollment);
		if (realm.getType() == EnrollmentTypeEnumType.DOMAINGROUP) {
	        DomainGroupEnrollmentWrapperImpl wrapper = new DomainGroupEnrollmentWrapperImpl(enrollment, dictionary);
	    	Throwable enrollTaskException = null;
	        for (String subDomainName : wrapper.getSubDomains()) {
	        	enrollment = dictionary.getEnrollment(subDomainName);
	
	        	if (enrollment == null) {
	                throw new EntryNotFoundException("enrollment-subdomain ", subDomainName);
	            }
	        	
	        	// Enroll the sub domain
	        	try {
	        		controller.sync(enrollment);
	        	}
	        	catch (EnrollmentValidationException e) {
	        		enrollTaskException = e;
	        	}
	        	catch (EnrollmentSyncException e) {
	        		enrollTaskException = e;
	        	}
	        	catch (EnrollmentThreadException e) {
	        		enrollTaskException = e;
	        	}
	        	catch (DictionaryException e) {
	        		enrollTaskException = e;
	        	}
	        	catch (RuntimeException e) {
	        		enrollTaskException = e;        		
	        	}
	        }
	        
			if (enrollTaskException != null) {
				if (enrollTaskException instanceof EnrollmentValidationException) {
                	throw (EnrollmentValidationException) enrollTaskException;
				} else if (enrollTaskException instanceof EnrollmentThreadException) {
                	throw (EnrollmentThreadException) enrollTaskException;
                } else if (enrollTaskException instanceof EnrollmentSyncException) {
                	throw (EnrollmentSyncException) enrollTaskException;
                } else if (enrollTaskException instanceof DictionaryException) {
                	throw (DictionaryException) enrollTaskException;
                } else if (enrollTaskException instanceof RuntimeException) {
                	throw (RuntimeException) enrollTaskException;
                }
			
			}
		}
    }

    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#getRealms(java.lang.String)
     */
    public IEnrollment getRealm(String name) throws 
		    InvalidConfigurationException,
			EntryNotFoundException,
		    DictionaryException {
    	nullCheck(name, "name");
    	
        IEnrollment enrollment = dictionary.getEnrollment(name);
        if (enrollment == null) {
            throw new EntryNotFoundException("enrollment", name);
        }
        return enrollment;
    }
    
    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#getRealms()
     */
    public Collection<IEnrollment> getRealms() throws DictionaryException {
		return dictionary.getEnrollments();
	}
    

    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#addColumn(com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData)
     */
	public void addColumn(IColumnData column) throws 
			InvalidConfigurationException, 
			DuplicateEntryException, 
			DictionaryException {
		//TODO throws DuplicateColumnException if the column already exist.
		nullCheck(column, "column");
		nullCheck(column.getDisplayName(), "display name for column");
		nullCheck(column.getLogicalName(), "logical name for column");
		String elementTypeStr = column.getElementType();
		nullCheck(elementTypeStr, "element name for column");

		
		IMElementType elementType = dictionary.getType(elementTypeStr);
        if (elementType == null) {
			throw InvalidConfigurationException.unknown("dictionary type", elementTypeStr);
		} 
        
        final IConfigurationSession session = dictionary.createSession();
        boolean succeeded = false;
        try {
        	session.beginTransaction();
			IElementField field = elementType.addField(column.getLogicalName(), 
					ElementFieldType.getByName(column.getType()));
            elementType.setFieldLabel(field, column.getDisplayName());
            session.saveType(elementType);
            succeeded = true;
        } finally {
        	close(session, succeeded);
        }
    }
	
	/*
	 * (non-Javadoc)
	 * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#delColumn(java.lang.String, java.lang.String)
	 */
    public void delColumn(String logicalName, String elementType) throws 
    		InvalidConfigurationException,
    		EntryNotFoundException,
    		DictionaryException {
    	nullCheck(logicalName, "logicalName");
    	nullCheck(elementType, "elementType");

        IMElementType elementAffected = dictionary.getType(elementType);
        if (elementAffected == null) {
            throw new EntryNotFoundException("element type", elementType);
        }

        IElementField fieldToDelete = elementAffected.getField(logicalName);
        if (fieldToDelete == null) {
            throw new EntryNotFoundException("element field", logicalName);
        } 
        
        IConfigurationSession session = dictionary.createSession();
        boolean succeeded = false;
        try {
        	session.beginTransaction();
            elementAffected.deleteField(fieldToDelete);
            session.saveType(elementAffected);
            succeeded = true;
        } finally {
        	close(session, succeeded);
        }
        
    }
 
    /*
     * (non-Javadoc)
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager#getColumns()
     */
    public Collection<IElementField> getColumns() throws DictionaryException {
        Collection<IElementField> columnsToReturn = new ArrayList<IElementField>();

        Collection<IElementType> elementTypes = dictionary.getAllTypes();
        for (IElementType element : elementTypes) {
            Collection<IElementField> fields = element.getFields();
            for (IElementField field : fields) {
                columnsToReturn.add(field);
            }
        }
        
        return columnsToReturn;
    }
    
    
    /**
     * @see IConfigurable#setConfiguration(IConfiguration)
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @see IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see ILogEnabled#setLog(Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }
    
    private void nullCheck(Object o, String name) throws InvalidConfigurationException {
    	if (o == null) {
    		throw InvalidConfigurationException.missing(name);
		}
    }
    
	private void close(IConfigurationSession session, boolean succeeded) throws DictionaryException{
		if (session == null) {
            return;
        }
	    try {
            if (succeeded) {
                session.commit();
            } else {
                session.rollback();
            }
        } finally {
            session.close();
        }
        
	}
}
