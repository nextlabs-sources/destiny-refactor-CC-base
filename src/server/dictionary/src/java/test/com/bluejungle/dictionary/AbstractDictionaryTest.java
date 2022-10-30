/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/AbstractDictionaryTest.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.HashSet;
import java.util.Set;

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

import junit.framework.TestCase;

/**
 * This is the base class for dictionary server tests that require
 * a fixed dictionary setup. The class provides protected access to
 * the dictionary, user, application, and host types, and to several
 * enrollments. The setup method takes care of preparing the dictionary
 * schema items and verifying that the setup was successful.
 */
abstract class AbstractDictionaryTest extends TestCase {

    private static final String[] ENROLLMENT_DOMAINS = new String[] {
        "bluejungle.com"
    ,   "att.com"
    ,   "windowsupdate.microsoft.com"
    ,   "www.corriere.it"
    ,   "gov"
    };
    
    protected abstract class TestElementType{
        public final IMElementType type;
        
        private final boolean isCreate;
        
        private final Set<IElementField> allFeilds;
        
        protected TestElementType(String name, IDictionary dictionary, boolean isCreate)
                throws DictionaryException {
            this.isCreate = isCreate;
            type = getOrCreateType(name, dictionary);
            allFeilds = new HashSet<IElementField>();
        }
        
        protected IMElementType getOrCreateType(String name, IDictionary dictionary)
                throws DictionaryException {
            return isCreate 
                    ? dictionary.makeNewType(name) 
                    : dictionary.getType(name);
        }
        
        protected IElementField getOrCreateField(String name, ElementFieldType fieldType){
            IElementField field = isCreate
                    ? type.addField(name, fieldType)
                    : type.getField(name);
            allFeilds.add(field);
            return field;
                    
        }
        
        protected void selfCheck(){
            assertNotNull(type);
        }
        
        public Set<IElementField> getAllFields() {
            return allFeilds;
        }
    }

    protected class TestUserType extends TestElementType {
        private static final String USER_TYPE = "USER";
        
        public final IElementField firstName;
        public final IElementField lastName;
        public final IElementField dateOfBirth;
        public final IElementField employeeCode;
        public final IElementField email;
        public final IElementField aliases;
        public final IElementField numbers;
        public final IElementField whenStarted;
        public final IElementField addr1;
        public final IElementField addr2;
        public final IElementField addr3;
        public final IElementField addrCountry;
        public final IElementField addrPcode;
        
        private TestUserType(IDictionary dictionary, boolean isCreate) throws DictionaryException{
            super(USER_TYPE, dictionary, isCreate);
            firstName       = getOrCreateField("first name",      ElementFieldType.CS_STRING);
            lastName        = getOrCreateField("last name",       ElementFieldType.STRING);
            dateOfBirth     = getOrCreateField("date of birth",   ElementFieldType.DATE);
            employeeCode    = getOrCreateField("employee number", ElementFieldType.NUMBER);
            email           = getOrCreateField("e-mail",          ElementFieldType.STRING);
            aliases         = getOrCreateField("aliases",         ElementFieldType.STRING_ARRAY);
            numbers         = getOrCreateField("user numbers",    ElementFieldType.NUM_ARRAY);
            whenStarted     = getOrCreateField("when started",    ElementFieldType.DATE);
            addr1           = getOrCreateField("address line 1",  ElementFieldType.STRING);
            addr2           = getOrCreateField("address line 2",  ElementFieldType.STRING);
            addr3           = getOrCreateField("address line 3",  ElementFieldType.STRING);
            addrCountry     = getOrCreateField("country",         ElementFieldType.STRING);
            addrPcode       = getOrCreateField("postal code",     ElementFieldType.STRING);
        }
        
        @Override
        protected void selfCheck(){
            super.selfCheck();
            assertNotNull(firstName);
            assertNotNull(lastName);
            assertNotNull(dateOfBirth);
            assertNotNull(employeeCode);
            assertNotNull(email);
            assertNotNull(aliases);
            assertNotNull(numbers);
            assertNotNull(whenStarted);
            assertNotNull(addr1);
            assertNotNull(addr2);
            assertNotNull(addr3);
            assertNotNull(addrCountry);
            assertNotNull(addrPcode);
        }
    }
    
    protected class TestContactType extends TestElementType {
        private static final String CONTACT_TYPE = "CONTACT";
        
        public final IElementField contactFirstName;
        public final IElementField contactLastName;
        public final IElementField contactEmail;
        public final IElementField contactAliases;
        
        private TestContactType(IDictionary dictionary, boolean isCreate) throws DictionaryException{
            super(CONTACT_TYPE, dictionary, isCreate);
            contactFirstName    = getOrCreateField("contact first name", ElementFieldType.CS_STRING);
            contactLastName     = getOrCreateField("contact last name",  ElementFieldType.STRING);
            contactEmail        = getOrCreateField("contact e-mail",     ElementFieldType.STRING);
            contactAliases      = getOrCreateField("contact aliases",    ElementFieldType.STRING_ARRAY);
            
        }
        
        @Override
        protected void selfCheck() {
            super.selfCheck();
            assertNotNull(contactFirstName);
            assertNotNull(contactLastName);
            assertNotNull(contactEmail);
            assertNotNull(contactAliases);
        }
    }
    
    protected class TestHostType extends TestElementType {
        private static final String HOST_TYPE = "HOST";
        public final IElementField hostName;
        public final IElementField hostDomain;
        public final IElementField hostOs;
        public final IElementField cpuCount;
        public final IElementField nicCount;
        public final IElementField controlPort;
        public final IElementField assetNumber;
        public final IElementField patchLevel;
        public final IElementField location;
        public final IElementField hostCountry;
        
        private TestHostType(IDictionary dictionary, boolean isCreate) throws DictionaryException{
            super(HOST_TYPE, dictionary, isCreate);
            hostName        = getOrCreateField("host name",         ElementFieldType.STRING);
            hostDomain      = getOrCreateField("host domain",       ElementFieldType.STRING);
            hostOs          = getOrCreateField("OS code",           ElementFieldType.CS_STRING);
            cpuCount        = getOrCreateField("CPU count",         ElementFieldType.NUMBER);
            nicCount        = getOrCreateField("NIC count",         ElementFieldType.NUMBER);
            controlPort     = getOrCreateField("control port",      ElementFieldType.NUMBER);
            assetNumber     = getOrCreateField("asset ctrl number", ElementFieldType.CS_STRING);
            patchLevel      = getOrCreateField("patch level",       ElementFieldType.NUMBER);
            location        = getOrCreateField("location code",     ElementFieldType.CS_STRING);
            hostCountry     = getOrCreateField("country",           ElementFieldType.STRING);
        }
        
        @Override
        protected void selfCheck() {
            super.selfCheck();
            assertNotNull(hostName);
            assertNotNull(hostDomain);
            assertNotNull(hostOs);
            assertNotNull(cpuCount);
            assertNotNull(nicCount);
            assertNotNull(controlPort);
            assertNotNull(assetNumber);
            assertNotNull(patchLevel);
            assertNotNull(location);
            assertNotNull(hostCountry);
        }
    }
    
    protected class TestApplicationType extends TestElementType {
        private static final String APP_TYPE = "APPLICATION";
        public final IElementField appName;
        public final IElementField appImage;
        public final IElementField appManufacturer;
        public final IElementField appVersion;
        public final IElementField appFingerprint;
        public final IElementField appImportDate;
        
        private TestApplicationType(IDictionary dictionary, boolean isCreate) throws DictionaryException{
            super(APP_TYPE, dictionary, isCreate);
            appName         = getOrCreateField("application name", ElementFieldType.STRING);
            appImage        = getOrCreateField("image name",       ElementFieldType.STRING);
            appManufacturer = getOrCreateField("manufacturer",     ElementFieldType.CS_STRING);
            appVersion      = getOrCreateField("version",          ElementFieldType.NUMBER);
            appFingerprint  = getOrCreateField("fingerprint",      ElementFieldType.CS_STRING);
            appImportDate   = getOrCreateField("imported on",      ElementFieldType.DATE);
        }
        
        @Override
        protected void selfCheck() {
            super.selfCheck();
            assertNotNull(appName);
            assertNotNull(appImage);
            assertNotNull(appManufacturer);
            assertNotNull(appVersion);
            assertNotNull(appFingerprint);
            assertNotNull(appImportDate);
        }
    }

    protected IDictionary dictionary; 
    protected IEnrollment[] enrollments;
    
    protected TestUserType        userStruct;
    protected TestContactType     contactStruct;
    protected TestHostType        hostStruct;
    protected TestApplicationType appStruct;
    

    /**
     * All subclasses must call this method from their
     * <code>testSetupDictionary</code> method
     * @throws Exception when something unexpected happens.
     */
    protected void init() throws Exception {
        
    	IComponentManager cm = ComponentManagerFactory.getComponentManager();
        cm.registerComponent(Dictionary.COMP_INFO, true);
        this.dictionary = cm.getComponent(Dictionary.COMP_INFO);
        
        assertEquals(0, ((Dictionary)dictionary).getCountedSessionCount());

        Session hs = null;
        Transaction tx = null;
        boolean isSuccess = false;
        try {
            hs = ((Dictionary)dictionary).getCountedSession();
            tx = hs.beginTransaction();
            hs.delete("from UpdateRecord");
            hs.delete("from DictionaryElementBase");
            hs.delete("from Enrollment");
            hs.delete("from ElementType");
            hs.delete("from EnumerationMember");
            hs.delete("from EnumerationGroupMember");
            hs.delete("from EnumerationProvisionalMember");
            tx.commit();
            isSuccess = true;
        } finally {
            try {
                if (!isSuccess && tx != null) {
                    tx.rollback();
                }
            } finally {
                if (hs != null) {
                    ((Dictionary)dictionary).closeCurrentSession();
                }
            }
        }
        userStruct = new TestUserType(dictionary, true);
        contactStruct = new TestContactType(dictionary, true);
        hostStruct = new TestHostType(dictionary, true);
        appStruct = new TestApplicationType(dictionary, true);

        enrollments = new IEnrollment[ENROLLMENT_DOMAINS.length];
        for ( int i = 0 ; i != enrollments.length ; i++ ) {
            enrollments[i] = dictionary.makeNewEnrollment(ENROLLMENT_DOMAINS[i]);
        }

        IConfigurationSession session = dictionary.createSession();

        try {
            session.beginTransaction();
            session.saveType(userStruct.type);
            session.saveType(contactStruct.type);
            session.saveType(hostStruct.type);
            session.saveType(appStruct.type);
            for ( int i = 0 ; i != enrollments.length ; i++ ) {
                session.saveEnrollment(enrollments[i]);
            }
            session.commit();
        } finally {
            session.close();
        }
        assertNotNull(enrollments);
        assertTrue(enrollments.length != 0);
        for ( int i = 0 ; i != enrollments.length ; i++ ) {
            assertNotNull(enrollments[i]);
        }
        
        userStruct.selfCheck();
        contactStruct.selfCheck();
        hostStruct.selfCheck();
        appStruct.selfCheck();
    }

    /**
     * All classes must override this method.
     * This ensures that TestSuite finds and runs the test.
     * Inside this method the subclasses must call the init() method.
     */
    protected abstract void testSetupDictionary() throws Exception;

    protected void setUp() throws Exception {
        super.setUp();
        try {
        	if ( this.dictionary == null ) {
        	   	IComponentManager cm = ComponentManagerFactory.getComponentManager();
            	ComponentInfo<IDictionary> dictInfo = 
            	    new ComponentInfo<IDictionary>( 
            	        Dictionary.COMP_INFO.getName(), 
            	        Dictionary.class, 
            			IDictionary.class, 
            			LifestyleType.SINGLETON_TYPE);
            	cm.registerComponent(dictInfo, true);
            	this.dictionary = cm.getComponent( dictInfo );
        	}
        	
        	userStruct = new TestUserType(dictionary, false);
            contactStruct = new TestContactType(dictionary, false);
            hostStruct = new TestHostType(dictionary, false);
            appStruct = new TestApplicationType(dictionary, false);

            enrollments = new IEnrollment[ENROLLMENT_DOMAINS.length];
            for ( int i = 0 ; i != enrollments.length ; i++ ) {
                enrollments[i] = dictionary.getEnrollment(ENROLLMENT_DOMAINS[i]);
            }
        } catch ( DictionaryException expected ) {
            // setUp fails before the first test which sets up
            // the components to which the setUp attaches.
        }
    }

    /**
     * Counts users and hosts in the <code>IElementIterator</code>.
     * @param ei the <code>IElementIterator</code> containing the
     * elements to be counted. The iterator is closed when the method
     * exits.
     * @return an array of two <code>int</code> values
     * (user count at index zero, host count at index 1).
     * @throws DictionaryException if the operation fails.
     */
    protected int[] countUsersContactsHostsAndApps(IDictionaryIterator<IMElement> ei) throws DictionaryException {
        assertNotNull(ei);
        try {
            int users = 0, contacts = 0, hosts = 0, apps = 0;
            while (ei.hasNext()) {
                IElement element = ei.next();
                assertNotNull(element);
                if (element.getType().equals(userStruct.type)) {
                    users++;
                } else if (element.getType().equals(contactStruct.type)) {
                    contacts++;
                } else if (element.getType().equals(hostStruct.type)) {
                    hosts++;
                } else if (element.getType().equals(appStruct.type)) {
                    apps++;
                } else {
                    fail("Elements must be users, hosts, or applications.");
                }
            }
            return new int[] {users, contacts, hosts, apps};
        } finally {
            ei.close();
        }
    }

    /**
     * Counts groups in the <code>IGroupIterator</code>.
     * @param gi the <code>IGroupIterator</code> containing the groups
     * to be counted. The iterator is closed when the method exits.
     * @return the number of groups in the iterator.
     * @throws DictionaryException if the operation fails.
     */
    protected int countGroups(IDictionaryIterator<IMGroup> gi, Class<? extends IMGroup> expectedClass) throws DictionaryException {
        assertNotNull(gi);
        try {
            int groups = 0;
            while (gi.hasNext()) {
                groups++;
                IMGroup g = gi.next();
                assertNotNull(g);
                assertTrue(
                    "Expected "+expectedClass.toString()
                ,   expectedClass.isAssignableFrom(g.getClass())
                );
            }
            return groups;
        } finally {
            gi.close();
        }
    }

}
