/*
 * Created on Mar 5, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.RealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl.EnrollmentManagerImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionarySizeCount;
import com.bluejungle.dictionary.DictionaryTestHelper;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.dictionary.tools.MockSharedContextLocator;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.patterns.EnumBase;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/BaseEnrollerSharedTestCase.java#1 $
 */

public abstract class BaseEnrollerSharedTestCase extends BaseContainerSharedTestCase{
	protected static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
//        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }
	
	protected static final String ENROLL_DIR = SRC_ROOT_DIR + "/server/tools/enrollment/etc/";
	
	protected IDictionary dictionary;
	protected IEnrollmentManager enrollmentManager;
	protected static DictionarySizeCount dbCount;

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
	
    /* -- environment setup begin-- */
    /**
     * if the test doesn't depends on the other test, should return true
     * @return
     */
    protected boolean clearDatabaseBeforeStart(){
    	return true;
    }
    
    protected void clearDictionaryDatabase() throws HibernateException, SQLException, DictionaryException{
    	DictionaryTestHelper.clearDictionary(dictionary);
    }
    
    private static boolean clearDatabase = true;
    
	public void setUp() throws Exception{
		super.setUp();
        
		IComponentManager cm = ComponentManagerFactory.getComponentManager();
		
		ComponentInfo<MockSharedContextLocator> locatorInfo = 
            new ComponentInfo<MockSharedContextLocator>(
                    IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(locatorInfo, true);
        
        dictionary = cm.getComponent(Dictionary.COMP_INFO);
        
        // setup dictionary and enrollment manager
        IEnrollerFactory enrollerFactory = new EnrollerFactoryImpl();
        HashMapConfiguration enrollmentMgrConfig = new HashMapConfiguration();
        enrollmentMgrConfig.setProperty(IEnrollmentManager.DICTIONARY, dictionary);
        enrollmentMgrConfig.setProperty(IEnrollmentManager.ENROLLER_FACTORY, enrollerFactory);
        
        enrollmentManager = cm.getComponent(EnrollmentManagerImpl.class, enrollmentMgrConfig);
		
		if (clearDatabase && clearDatabaseBeforeStart()) {
            clearDictionaryDatabase();
            clearDatabase = false;
        }
		
		if(dbCount == null){
		    dbCount = new DictionarySizeCount(dictionary);
		}
	}
	
    public void reset() throws HibernateException, SQLException, DictionaryException {
        clearDatabase = true;
        if (clearDatabase && clearDatabaseBeforeStart()) {
            clearDictionaryDatabase();
            clearDatabase = false;
        }
        dbCount.fastReset();
    }
    /* -- environment setup end-- */
	
    
    
    /* -- setup helper methods begin-- */
    
    protected Map<String, String[]> encodePassword(String password){
    	Map<String, String[]> properties = new HashMap<String, String[]>();
		ReversibleEncryptor encryptor = new ReversibleEncryptor();
		properties.put(getPasswordPropertyKey(), new String[] { encryptor.encrypt(password) });
		return properties;
    }
    
    protected String getPasswordPropertyKey() {
		throw new UnsupportedOperationException();
	}
    
    protected IDictionaryIterator<IMElement> dictionaryQuery(RelationOp relationOp, EnumBase field, String str)
			throws DictionaryException {
		return dictionaryQuery(relationOp, guessElementType(field.getClass()).getField(field.getName()), str);
	}
    
    protected IDictionaryIterator<IMElement> dictionaryQueryEquals(EnumBase field, String str)
			throws DictionaryException {
		return dictionaryQuery(RelationOp.EQUALS, field, str);
	}
    
    
    
    private static Map<EnumBase, IElementType> elementTypeCache = new HashMap<EnumBase, IElementType>();
    
    protected String getStringFromElement(IElement element, EnumBase field)
			throws DictionaryException {
		final IElementType elementType = guessElementType(field.getClass());
		return (String) element.getValue(elementType.getField(field.getName()));
	}

    protected IElementType guessElementType(Class<? extends EnumBase> field) throws DictionaryException {
		final ElementTypeEnumType elementTypeEnumType;
		if (UserReservedFieldEnumType.class.isAssignableFrom(field)) {
			elementTypeEnumType = ElementTypeEnumType.USER;
		} else if (ContactReservedFieldEnumType.class.isAssignableFrom(field)) {
			elementTypeEnumType = ElementTypeEnumType.CONTACT;
		} else if (ComputerReservedFieldEnumType.class.isAssignableFrom(field)) {
			elementTypeEnumType = ElementTypeEnumType.COMPUTER;
		} else {
			throw new IllegalArgumentException(field.toString());
		}
    	
    	if (!elementTypeCache.containsKey(elementTypeEnumType)) {
			elementTypeCache.put(elementTypeEnumType, dictionary.getType(elementTypeEnumType
					.getName()));
		}

    	final IElementType elementType = elementTypeCache.get(elementTypeEnumType);
		if(elementType == null){
			throw new NullPointerException("elementType, " + elementTypeEnumType);
		}
		return elementType;
	}
    
    protected IDictionaryIterator<IMElement> dictionaryQuery(RelationOp relationOp, IElementField field,
			String str) throws DictionaryException {
		return dictionary.query(
				new Relation(relationOp, field, Constant.build(str)),	// IPredicate condition
				new Date(),	// Date asOf 
				null, 		// Order[] order
				null);		// Page page
	}

    /* -- setup helper methods end-- */
    
   
    /* -- test helper methods begin-- */
    
    protected void postCheck(Date startTime) throws DictionaryException {
		Collection<IEnrollment> enrollments;
		Date endTime = new Date();
		enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());
		IEnrollment enrollment = enrollments.iterator().next();
		IUpdateRecord record = enrollment.getStatus();
		assertNotNull(record);
		assertTrue(record.isSuccessful());
		assertNotNull(record.getStartTime());
		assertFalse(record.getStartTime().before(startTime));
		assertFalse(record.getStartTime().after(endTime));
		assertNotNull(record.getEndTime());
		assertFalse(record.getEndTime().before(startTime));
		assertFalse(record.getEndTime().after(endTime));
		assertFalse(record.getEndTime().before(startTime));
		assertFalse(record.getEndTime().before(record.getStartTime()));
		assertNotNull(record.getErrorMessage());
		assertEquals("The Active Directory enrollment succeeded.", record.getErrorMessage());
	}

    protected IRealmData convert(String domainName, 
            EnrollmentTypeEnumType type, 
            Map<String, String[]> overrideProperties,
            String... propertyFiles) throws FileNotFoundException, IOException{
        Map<String, String[]> properties = new HashMap<String, String[]>();
        for (String propertyFile : propertyFiles) {
            setupProperty(propertyFile, properties);
        }
        
        try {
            String[] password = properties.get(getPasswordPropertyKey());
            if (password != null) {
                ReversibleEncryptor encryptor = new ReversibleEncryptor();
                properties.put(getPasswordPropertyKey(),
                        new String[] { encryptor.encrypt(password[0]) });
            }
        } catch (UnsupportedOperationException e) {
            //ignore
        }
        
        if (overrideProperties != null) {
            for (Map.Entry<String, String[]> entry : overrideProperties.entrySet()) {
                properties.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        RealmData data = new RealmData(domainName, properties, type);
        return data;
    }
    
    protected IRealmData convert(String domainName){
        RealmData data = new RealmData(domainName, null, null);
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
              if ( key.equals(ActiveDirectoryEnrollmentProperties.ROOTS) ) {
//                if ( key.equals("roots") ) {
                    String[] roots =  entry.getValue().toString().split("\n");
                    value = roots;
                } else {
                    value = new String[] { ((String) entry.getValue()).trim() };
                }
                properties.put(key, value);
            }
        } finally {
            is.close();
        }
    }
    
    protected int count(IDictionaryIterator<?> iter) throws DictionaryException {
        int count = 0;
        while( iter.hasNext() ) {
            iter.next();
            count ++;
        }
        iter.close();
        return count;
    }
    /* -- test helper methods end-- */
}
