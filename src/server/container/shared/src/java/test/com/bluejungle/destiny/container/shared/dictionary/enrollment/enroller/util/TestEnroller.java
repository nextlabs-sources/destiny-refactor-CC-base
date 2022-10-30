package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl.EnrollmentManagerImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerFactoryImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.tools.MockSharedContextLocator;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

public class TestEnroller extends TestCase {
    
    private final IEnrollmentManager enrollmentManager; 
    
    public TestEnroller() {
    	final IComponentManager cm = ComponentManagerFactory.getComponentManager(); 
        // Setup the mock shared context locator:
        ComponentInfo<MockSharedContextLocator> locatorInfo = 
        	new ComponentInfo<MockSharedContextLocator>(
        			IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(locatorInfo, true);
        
        // setup dictionary and enrollment manager
        IEnrollerFactory enrollerFactory = new EnrollerFactoryImpl();
        IDictionary dictionary = cm.getComponent(Dictionary.COMP_INFO);
        HashMapConfiguration enrollmentMgrConfig = new HashMapConfiguration();
        enrollmentMgrConfig.setProperty(EnrollmentManagerImpl.DICTIONARY, dictionary);
        enrollmentMgrConfig.setProperty(EnrollmentManagerImpl.ENROLLER_FACTORY, enrollerFactory);
        
        enrollmentManager = cm.getComponent(EnrollmentManagerImpl.class, enrollmentMgrConfig);
	}
    
    public IEnrollmentManager getEnrollmentManager(){
    	return enrollmentManager;
    }
    
    /**
     * Enroll data sources
     * @param domainName
     * @param propertyFile
     * @param type
     * @throws EnrollmentFailedException
     * @throws IOException 
     * @throws DuplicateEnrollmentException 
     * @throws DictionaryException 
     * @throws InvalidConfigurationException 
     * @throws EntryNotFoundException 
     * @throws FileNotFoundException 
     * @throws EnrollmentValidationException 
     * @throws EnrollerCreationException 
     * @throws DuplicateEntryException 
     * @throws EnrollmentThreadException 
     */
    public void enroll(String domainName, 
    			String[] propertyFiles, 
    			EnrollmentTypeEnumType type,
    			Map<String, String[]> propertyChanges) 
    	throws EnrollmentSyncException,
				FileNotFoundException, 
				EntryNotFoundException, 
				InvalidConfigurationException,
				DictionaryException, 
				IOException, 
				EnrollerCreationException,
				EnrollmentValidationException, 
				DuplicateEntryException, 
				EnrollmentThreadException {
		final IRealmData realm = setupLocalDomain(domainName, propertyFiles, type, propertyChanges, false);
        enrollmentManager.enrollRealm(realm);
    }

    /**
     * Enroll data sources
     * @param domainName
     * @param propertyFile
     * @param type
     * @throws EnrollmentFailedException
     * @throws EntryNotFoundException 
     * @throws IOException 
     * @throws DuplicateEnrollmentException 
     * @throws DictionaryException 
     * @throws InvalidConfigurationException 
     * @throws FileNotFoundException 
     * @throws EnrollmentValidationException 
     * @throws EnrollerCreationException 
     * @throws DuplicateEntryException 
     * @throws EnrollmentThreadException 
     */
    public void update(String domainName, 
	    		String[] propertyFiles, 
	    		EnrollmentTypeEnumType type,
				Map<String, String[]> propertyChanges) 
    	throws EnrollmentSyncException,
				EntryNotFoundException, 
				FileNotFoundException, 
				InvalidConfigurationException,
				DictionaryException, 
				IOException, 
				EnrollerCreationException, 
				EnrollmentValidationException, 
				DuplicateEntryException, EnrollmentThreadException {
		final IRealmData realm;
		if (propertyFiles != null && propertyFiles.length > 0) {
			realm = setupLocalDomain(domainName, propertyFiles, type, propertyChanges, true);
			assertNotNull(realm);
		} else {
			IEnrollment enrollment = enrollmentManager.getRealm(domainName);
			assertNotNull(enrollment);
			realm = new RealmData(domainName, null, null);
		}
		enrollmentManager.enrollRealm(realm);
	}
    
    /**
     * create a local domain
     * @param domainName
     * @param propertyFile
     * @param type
     * @return
     * @throws EnrollmentFailedException 
     * @throws DictionaryException 
     * @throws InvalidConfigurationException 
     * @throws EntryNotFoundException 
     * @throws DuplicateEnrollmentException 
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws EnrollmentValidationException 
     * @throws EnrollerCreationException 
     * @throws DuplicateEntryException 
     * @throws EnrollmentThreadException 
     */
    public IRealmData setupLocalDomain(String domainName, 
    		String[] propertyFiles,
			EnrollmentTypeEnumType type, 
			Map<String, String[]> propertyChanges, 
			boolean update)
		throws EntryNotFoundException, 
			InvalidConfigurationException,
			DictionaryException,
			FileNotFoundException, 
			IOException,
			EnrollerCreationException, 
			EnrollmentValidationException, 
			DuplicateEntryException, EnrollmentThreadException {
		// Test that realm gets created:
		
		Map<String, String[]> properties = new HashMap<String, String[]>();
		for (String propertyFile : propertyFiles) {
			setupProperty(propertyFile, properties);
		}
		if (propertyChanges != null) {
			for (Map.Entry<String, String[]> entry : propertyChanges.entrySet()) {
				properties.put(entry.getKey().toLowerCase(), entry.getValue());
			}
		}
		RealmData data = new RealmData(domainName, properties, type);
		if (update) {
			enrollmentManager.updateRealm(data);
		} else {
			try {
				this.enrollmentManager.getRealm(domainName);
			} catch (EntryNotFoundException e) {
				enrollmentManager.createRealm(data);
			}
		}
		IEnrollment realm = this.enrollmentManager.getRealm(domainName);
		assertNotNull(realm);
		return data;
		
    }
    
    @Deprecated
    private void setupProperty(String propertyFile, Map<String, String[]> properties)
			throws FileNotFoundException, IOException {
        Properties property = new Properties();
        InputStream is = new FileInputStream(propertyFile);
        try {
			property.load(is);
			
			for (Map.Entry<Object, Object> entry : property.entrySet()) {
				String key = ((String) entry.getKey()).trim().toLowerCase();
				String[] value;
//				if ( key.equals(ActiveDirectoryProperties.ROOTS_PROPERTY) ) {
				if ( key.equals(ActiveDirectoryEnrollmentProperties.ROOTS) ) {
	                String[] roots =  entry.getValue().toString().split("\n");
	                value = roots;
				} else if ( key.equals(ActiveDirectoryEnrollmentProperties.PASSWORD) ) {
				    ReversibleEncryptor encryptor = new ReversibleEncryptor();
				    value = new String[] {encryptor.encrypt((String) entry.getValue()).trim()};
	            } else {
					value = new String[] { ((String) entry.getValue()).trim() };
				}
				properties.put(key, value);
			}
		} finally {
			is.close();
		}
    }
    
    public void delete(String domainName) 
	    	throws EntryNotFoundException,
				DictionaryException, 
				InvalidConfigurationException, EnrollmentThreadException {
    	IEnrollment enrollment = enrollmentManager.getRealm(domainName);
		assertNotNull(enrollment);
		
		enrollmentManager.deleteRealm(new RealmData(domainName, null, null));
    }

    /**
     * Add new column 
     * @param column
     * @throws DictionaryException 
     * @throws DuplicateColumnException 
     * @throws InvalidConfigurationException 
     * @throws DuplicateEntryException 
     * @throws Exception
     */
    public void addNewColumn(String displayName, String elementType, String logicalName, String type)
			throws InvalidConfigurationException, DictionaryException, DuplicateEntryException {
		ColumnData column = new ColumnData(displayName, elementType, logicalName, type);
		enrollmentManager.addColumn(column);
	}

    /**
     * Delete column 
     * @param columnName
     * @param elementTypeName
     * @throws DictionaryException 
     * @throws EntryNotFoundException 
     * @throws InvalidConfigurationException 
     * @throws Exception
     */
    public void deleteColumn(String columnName, String elementTypeName)
			throws EntryNotFoundException, DictionaryException, InvalidConfigurationException {
		enrollmentManager.delColumn(columnName, elementTypeName);
	}
    
    
    
    /**
     * Count total element number in the element iter
     * @param iter
     * @return
     * @throws DictionaryException
     */
    public int count(IDictionaryIterator<?> iter) throws DictionaryException {
        int count = 0;
        while( iter.hasNext() ) {
            iter.next();
            count ++;
        }
        iter.close();
        return count;
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception { 
        // Shutdown the component manager:
        ComponentManagerFactory.getComponentManager().shutdown();
        super.tearDown();
    }
    
    
    public RealmData createRealmData(String name, Map<String, String[]> properties,
			EnrollmentTypeEnumType type) {
    	return new RealmData(name, properties, type);
    }
    
    /**
     * 
     * @author safdar
     */
    private class RealmData implements IRealmData {

        private String name;
        private EnrollmentTypeEnumType type;
        private Map<String, String[]> properties;

        /**
         * Constructor
         * 
         * @param name
         * @param type
         * @param properties
         */
        public RealmData(String name, Map<String, String[]> properties, EnrollmentTypeEnumType type) {
            this.name = name;
            this.type = type;
            this.properties = properties;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getName()
         */
        public String getName() {
            return this.name;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getProperties()
         */
        public Map<String, String[]> getProperties() {
            return this.properties;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData#getType()
         */
        public EnrollmentTypeEnumType getType() {
            return this.type;
        }
    }

    /**
     * Column data adapter
     * 
     * @author safdar
     */
    private class ColumnData implements IColumnData {

        private String displayName;
        private String elementType;
        private String logicalName;
        private String type;

        public ColumnData(String displayName, String elementType, String logicalName, String type) {
            this.displayName = displayName;
            this.elementType = elementType;
            this.logicalName = logicalName;
            this.type = type;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getDisplayName()
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getElementType()
         */
        public String getElementType() {
            return this.elementType;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getLogicalName()
         */
        public String getLogicalName() {
            return this.logicalName;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData#getType()
         */
        public String getType() {
            return this.type;
        }
    }
    
}
