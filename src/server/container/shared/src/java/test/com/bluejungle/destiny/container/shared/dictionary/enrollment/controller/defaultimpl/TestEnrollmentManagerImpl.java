/*
 * Created on Apr 26, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.ColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.RealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.tools.MockSharedContextLocator;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author safdar
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/defaultimpl/TestEnrollmentManagerImpl.java#1 $
 */

public class TestEnrollmentManagerImpl extends BaseContainerSharedTestCase {

    private static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }

    private static final String TEST_DOMAIN_NAME_1 = "tesT1";
    private static final String TEST_DOMAIN_NAME_2 = "TeST2";
    
    private static final ColumnData TEST_COLUMN_1 = new ColumnData(
            "NumberColumn", 
            ElementTypeEnumType.USER.getName(), 
            "NumberColumn", 
            ElementFieldType.NUMBER.getName());
    
    private static final ColumnData TEST_COLUMN_2 = new ColumnData(
            "DateColumn", 
            ElementTypeEnumType.COMPUTER.getName(), 
            "DateColumn", 
            ElementFieldType.DATE.getName());
    
    private static final ColumnData TEST_COLUMN_3 = new ColumnData(
            "CStringColumn", 
            ElementTypeEnumType.APPLICATION.getName(), 
            "CStringColumn", 
            ElementFieldType.CS_STRING.getName());
    
    private static final ColumnData[] TEST_COLUMNS = new ColumnData[]{
        TEST_COLUMN_1,
        TEST_COLUMN_2,
        TEST_COLUMN_3,
    };

    /*
     *
     */
    private IDictionary dictionary;
    private IEnrollerFactory enrollerFactory;
    private IEnrollmentManager enrollmentManager;
    
    private static int existingNum;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestEnrollmentManagerImpl.class);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getComponentName()
     */
    protected String getComponentName() {
        return "dem";
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }

    /**
     * Constructor for TestEnrollmentManagerImpl.
     *
     * @param arg0
     */
    public TestEnrollmentManagerImpl(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.enrollerFactory = new MockEnrollerFactoryImpl();

        // Setup the mock shared context locator:
        ComponentInfo<MockSharedContextLocator> locatorInfo = 
        	new ComponentInfo<MockSharedContextLocator>(
        		IDestinySharedContextLocator.COMP_NAME,
                MockSharedContextLocator.class,
                IDestinySharedContextLocator.class,
                LifestyleType.SINGLETON_TYPE);
        ComponentManagerFactory.getComponentManager().registerComponent(locatorInfo, true);
        ComponentManagerFactory.getComponentManager().getComponent(locatorInfo);

        this.dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
        HashMapConfiguration enrollmentMgrConfig = new HashMapConfiguration();
        enrollmentMgrConfig.setProperty(EnrollmentManagerImpl.DICTIONARY, dictionary);
        enrollmentMgrConfig.setProperty(EnrollmentManagerImpl.ENROLLER_FACTORY, enrollerFactory);
        this.enrollmentManager = ComponentManagerFactory.getComponentManager()
        			.getComponent(EnrollmentManagerImpl.class, enrollmentMgrConfig);
    }

    public void testCountExistingEnrollments() throws Exception {
        Collection<IEnrollment> realms = this.enrollmentManager.getRealms();
        assertNotNull( realms );
        existingNum = realms.size();
        if ( existingNum >=2 ) {
            try {
            	IEnrollment test1= this.enrollmentManager.getRealm(TEST_DOMAIN_NAME_1);
                if (test1 != null) {
                    this.enrollmentManager.deleteRealm(new RealmData(TEST_DOMAIN_NAME_1, 
                    		null, EnrollmentTypeEnumType.LDIF));
                    existingNum --;
                }
                IEnrollment test2 = this.enrollmentManager.getRealm(TEST_DOMAIN_NAME_2);
                if (test2 != null) {
                    this.enrollmentManager.deleteRealm(new RealmData(TEST_DOMAIN_NAME_2, 
                            null, EnrollmentTypeEnumType.LDIF));
                    existingNum --;
                }
            } catch ( EntryNotFoundException e ) {
                System.out.println("First time");
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    public void testCreateRealm() throws Exception {
        // Negative test with null data:
        try {
            this.enrollmentManager.createRealm(null);
            fail("Realm creation should have failed");
        } catch (InvalidConfigurationException ignore) {
            assertNotNull(ignore.getMessage());
        }

        // Negative test with invalid data:
        try {
            RealmData data = new RealmData(TEST_DOMAIN_NAME_1, null, null);
            this.enrollmentManager.createRealm(data);
            fail("Realm creation should have failed");
        } catch (InvalidConfigurationException ignore) {
            assertNotNull(ignore.getMessage());
        }
        try {
            RealmData data = new RealmData(null, null, EnrollmentTypeEnumType.LDIF);
            this.enrollmentManager.createRealm(data);
            fail("Realm creation should have failed");
        } catch (InvalidConfigurationException ignore) {
            assertNotNull(ignore.getMessage());
        }

        // Test that active directory realm gets created:
        try {
            RealmData data = new RealmData(TEST_DOMAIN_NAME_1, null, EnrollmentTypeEnumType.DIRECTORY);
            this.enrollmentManager.createRealm(data);
            IEnrollment enrollment = this.enrollmentManager.getRealm(TEST_DOMAIN_NAME_1);
            assertNotNull(enrollment);
            assertEquals(enrollment.getDomainName(), TEST_DOMAIN_NAME_1);
            assertTrue(enrollment.getBinPropertyNames() == null || enrollment.getBinPropertyNames().length == 0);
            assertTrue(enrollment.getNumPropertyNames() == null || enrollment.getNumPropertyNames().length == 0);
            assertTrue(enrollment.getStrPropertyNames() == null || enrollment.getStrPropertyNames().length == 0);
            assertTrue(enrollment.getStrArrayPropertyNames() == null || enrollment.getStrArrayPropertyNames().length == 0);
            assertSame(EnrollmentTypeEnumType.getByClassName(enrollment.getType()), EnrollmentTypeEnumType.DIRECTORY);
        } catch (InvalidConfigurationException ignore) {
            fail("Realm creation should not have failed");
        }

        // Test that LDIF realm gets created:
        try {
            RealmData data = new RealmData(TEST_DOMAIN_NAME_2, null, EnrollmentTypeEnumType.LDIF);
            this.enrollmentManager.createRealm(data);
            IEnrollment enrollment = this.enrollmentManager.getRealm(TEST_DOMAIN_NAME_2);
            assertNotNull(enrollment);
            assertEquals(enrollment.getDomainName(), TEST_DOMAIN_NAME_2);
            assertTrue(enrollment.getBinPropertyNames() == null || enrollment.getBinPropertyNames().length == 0);
            assertTrue(enrollment.getNumPropertyNames() == null || enrollment.getNumPropertyNames().length == 0);
            assertTrue(enrollment.getStrPropertyNames() == null || enrollment.getStrPropertyNames().length == 0);
            assertTrue(enrollment.getStrArrayPropertyNames() == null || enrollment.getStrArrayPropertyNames().length == 0);
            assertSame(EnrollmentTypeEnumType.getByClassName(enrollment.getType()), EnrollmentTypeEnumType.LDIF);
        } catch (InvalidConfigurationException ignore) {
            fail("Realm creation should not have failed");
        }

    }

    /**
     *
     * @throws Exception
     */
    public void testUpdateRealm() throws Exception {
        // Test that realm gets created:
        final String NAME = "update";

        // First create the realm:
        RealmData data = new RealmData(NAME, null, EnrollmentTypeEnumType.DIRECTORY);
        this.enrollmentManager.createRealm(data);
        IEnrollment enrollment = this.enrollmentManager.getRealm(NAME);
        assertNotNull(enrollment);
        assertEquals(enrollment.getDomainName(), NAME);
        assertTrue(enrollment.getBinPropertyNames() == null || enrollment.getBinPropertyNames().length == 0);
        assertTrue(enrollment.getNumPropertyNames() == null || enrollment.getNumPropertyNames().length == 0);
        assertTrue(enrollment.getStrPropertyNames() == null || enrollment.getStrPropertyNames().length == 0);
        assertTrue(enrollment.getStrArrayPropertyNames() == null || enrollment.getStrArrayPropertyNames().length == 0);
        assertSame(EnrollmentTypeEnumType.getByClassName(enrollment.getType()), EnrollmentTypeEnumType.DIRECTORY);

        // Try to update by changing enrollment type. Should give error.
        data = new RealmData(NAME, null, EnrollmentTypeEnumType.LDIF);
        try {
            this.enrollmentManager.updateRealm(data);
            fail("Realm update should have failed");
        } catch (InvalidConfigurationException ignore) {
        }

        // Try to update by adding some properties.
        Map<String, String[]> props = new HashMap<String, String[]>();
        final String PROP1 = "TestProperty1";
        final String PROP2 = "TestProperty2";
        props.put(PROP1, new String[] { "a", "b" });
        props.put(PROP2, new String[] { "c", "d", "e" });
        data = new RealmData(NAME, props, EnrollmentTypeEnumType.DIRECTORY);
        this.enrollmentManager.updateRealm(data);
        enrollment = this.enrollmentManager.getRealm(NAME);
        assertNotNull(enrollment);
        assertEquals(enrollment.getDomainName(), NAME);
        assertSame(EnrollmentTypeEnumType.getByClassName(enrollment.getType()), EnrollmentTypeEnumType.DIRECTORY);
        assertTrue(enrollment.getBinPropertyNames() == null || enrollment.getBinPropertyNames().length == 0);
        assertTrue(enrollment.getNumPropertyNames() == null || enrollment.getNumPropertyNames().length == 0);
        assertTrue(enrollment.getStrPropertyNames() == null || enrollment.getStrPropertyNames().length == 0);
        assertTrue(enrollment.getStrArrayPropertyNames() != null && enrollment.getStrArrayPropertyNames().length == 2);
        String[] prop1Arr = enrollment.getStrArrayProperty(PROP1);
        assertNotNull(prop1Arr);
        assertTrue(prop1Arr.length==2);
        assertEquals(prop1Arr[0],"a");
        assertEquals(prop1Arr[1],"b");
        String[] prop2Arr = enrollment.getStrArrayProperty(PROP2);
        assertNotNull(prop2Arr);
        assertTrue(prop2Arr.length==3);
        assertEquals(prop2Arr[0],"c");
        assertEquals(prop2Arr[1],"d");
        assertEquals(prop2Arr[2],"e");
    }

    /**
     *
     * @throws Exception
     */
    public void testGetRealms() throws Exception {
        // Query:
        Collection<IEnrollment> realms = this.enrollmentManager.getRealms();
        assertNotNull(realms);
        assertEquals(realms.size(), 3 + existingNum);
        assertNotNull(this.dictionary.getEnrollment(TEST_DOMAIN_NAME_1));
        assertNotNull(this.dictionary.getEnrollment(TEST_DOMAIN_NAME_2));
    }

    /**
     * test add column
     *
     */
    public void testAddColumn() throws Exception {
        Collection<IElementField> columns = enrollmentManager.getColumns();
        
        Set<String> newColumnNames = new HashSet<String>(TEST_COLUMNS.length);
        for(ColumnData data : TEST_COLUMNS){
            newColumnNames.add(data.getLogicalName());
        }
        
        for (IElementField column : columns) {
        	String existedName = column.getName();
        	
        	assertFalse("column \"" + existedName + "\" already existed", 
        	        newColumnNames.contains(existedName));
		}

        enrollmentManager.addColumn(TEST_COLUMN_1);
        enrollmentManager.addColumn(TEST_COLUMN_2);
        enrollmentManager.addColumn(TEST_COLUMN_3);
    }

    /**
     * Test list columns
     *
     */
    public void testGetColumns() throws Exception {
        Set<String> existingColumnNames = new HashSet<String>();
        for (IElementField column : enrollmentManager.getColumns()) {
            existingColumnNames.add(column.getName());
        }
        
        for(ColumnData data : TEST_COLUMNS){
            String name = data.getLogicalName();
            assertTrue("column \"" + name + "\" already existed", 
                    existingColumnNames.contains(name));
        }
    }

    /**
     * Clean all enrollments
     * @throws DictionaryException
     */
    public void testDeleteRealms() throws Exception {
        Collection<IEnrollment>  enrollments = this.enrollmentManager.getRealms();
        for (IEnrollment enrollmentToDelete : enrollments) {
            RealmData realmToDelete = new RealmData(
                    enrollmentToDelete.getDomainName(),
                    null,
                    EnrollmentTypeEnumType.DIRECTORY
            );
            this.enrollmentManager.deleteRealm(realmToDelete);
        }
    }

    /**
     * Clean all columns
     */
    public void testDeleteColumns() throws Exception {
        for( ColumnData column : TEST_COLUMNS ) {
            this.enrollmentManager.delColumn(column.getLogicalName(), column.getElementType());
        }
        Collection<IElementField> columns = this.enrollmentManager.getColumns();
        for( ColumnData column : TEST_COLUMNS ) {
            assertFalse( columns.contains(column) );
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // Shutdown the component manager:
        ComponentManagerFactory.getComponentManager().shutdown();
        super.tearDown();
    }
}
