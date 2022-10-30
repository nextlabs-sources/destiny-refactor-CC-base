/*
 * Created on <unknown>
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.tools;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestEnroller;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * Enroll initial data with local bluejungle data sources
 * 
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/tools/TestFixedDestinyDataEnroller.java#1 $
 */
public class TestFixedDestinyDataEnroller extends BaseEnrollerSharedTestCase {

    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();

    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    private IDictionary dictionary;
    private TestEnroller enroller;
    
    private static String DATA_DIR = System.getProperty("src.root.dir") + "/server/tools/enrollment/etc/";
    
    private static final String BLUEJUNGLE_DOMAIN  = "bluejungle.com";
    private static String BLUEJUNGLE_DOMAIN_PROPERTY_FILE  = DATA_DIR + "bluejungle.com.destiny.def";
    private static String BLUEJUNGLE_DOMAIN_LDIF_FILE  = DATA_DIR + "bluejungle.com.destiny.ldif";
    
    private static final String BLUEJUNGLE_TEST_DOMAIN = "test.bluejungle.com";
    private static String BLUEJUNGLE_TEST_DOMAIN_PROPERTY_FILE = DATA_DIR + "ldif.sample.default.def";
    private static String BLUEJUNGLE_TEST_DOMAIN_LDIF_FILE = DATA_DIR + "fixed.test.bluejungle.com.ldif";

    private static final String BLUEJUNGLE_LINUXTEST_DOMAIN = "linuxtest.bluejungle.com";
    private static String BLUEJUNGLE_LINUXDOMAIN_PROPERTY_FILE  = DATA_DIR + "linuxtest.bluejungle.com.def";
    private static String BLUEJUNGLE_LINUXDOMAIN_LDIFFILE  = DATA_DIR + "linuxtest.bluejungle.com.ldif";
    
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestFixedDestinyDataEnroller.class);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
    
    /*
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception {
    	super.setUp();
    	enroller = new TestEnroller();
        dictionary = ComponentManagerFactory.getComponentManager().getComponent(
						Dictionary.COMP_INFO);
	}
 
    /**
     * Clean all enrollments
     * @throws DictionaryException
     */
    public void testDeleteAllEnrollments() throws DictionaryException {
    	Collection<IEnrollment> enrollments = dictionary.getEnrollments(); 
    	IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            for(IEnrollment enrollment : enrollments) {
                session.deleteEnrollment(enrollment);
            }
            session.commit();
        } finally {
            session.close();
        }
    }

    /**
     * Enroll local domains
     * @throws Exception
     */
    public void testBluejungleEnrollments() throws Exception {
    	Map<String, String[]> ldifFile;
    	
        // enroll bluejungle.com domain (LDIF file)
    	ldifFile = new HashMap<String, String[]>(); 
    	ldifFile.put(LdifEnrollmentProperties.LDIF_NAME_PROPERTY,
				new String[] { BLUEJUNGLE_DOMAIN_LDIF_FILE });
        enroller.enroll(BLUEJUNGLE_DOMAIN, 
    			new String [] {BLUEJUNGLE_DOMAIN_PROPERTY_FILE },  
    			EnrollmentTypeEnumType.LDIF, 
    			ldifFile);
    }
    
    public void testTestBluejungleEnrollments() throws Exception {
    	Map<String, String[]> ldifFile;
    	
        // enroll test.blujungle.com domain (LDIF file)
        ldifFile = new HashMap<String, String[]>();
		ldifFile.put(LdifEnrollmentProperties.LDIF_NAME_PROPERTY,
				new String[] { BLUEJUNGLE_TEST_DOMAIN_LDIF_FILE });
        enroller.enroll(BLUEJUNGLE_TEST_DOMAIN, 
    			new String[] { BLUEJUNGLE_TEST_DOMAIN_PROPERTY_FILE },  
    			EnrollmentTypeEnumType.LDIF, 
    			ldifFile);
    }
    
    public void testLinuxtestEnrollments() throws Exception {
    	Map<String, String[]> ldifFile;
    	
        // enroll linuxtest.bluejungle.com domain (LDIF file)
        ldifFile = new HashMap<String, String[]>();
		ldifFile.put(LdifEnrollmentProperties.LDIF_NAME_PROPERTY,
				new String[] { BLUEJUNGLE_LINUXDOMAIN_LDIFFILE });
        enroller.enroll(BLUEJUNGLE_LINUXTEST_DOMAIN, 
                new String[] { BLUEJUNGLE_LINUXDOMAIN_PROPERTY_FILE },  
                EnrollmentTypeEnumType.LDIF, 
                ldifFile);
    }
  
    public void testLdifUsers() throws DictionaryException {
        IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        IElementField uniqueName = userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
        IDictionaryIterator<IMElement> users = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, uniqueName, Constant.build("rlin@bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( users.hasNext() );
            IElement rlin = users.next();
            String sid = (String) rlin.getValue(userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-2628", sid);
        } finally {
            users.close();
        }
    }
    
    public void testADUsers() throws DictionaryException {
        IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        IElementField uniqueName = userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
        IDictionaryIterator<IMElement> users = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, uniqueName, Constant.build("jimmy.carter@test.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( users.hasNext() );
            IElement jimmy = users.next();
            String sid = (String) jimmy.getValue(userType.getField(
            		UserReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals( "S-1-5-21-830805687-550985140-3285839444-1164", sid );
            
            String[] emailAddresses = (String[])jimmy.getValue(userType.getField(
            		UserReservedFieldEnumType.MAIL.getName()));
            assertNotNull(emailAddresses);
			assertEquals(1, emailAddresses.length);
			assertEquals("jimmy.carter@test.bluejungle.com", emailAddresses[0]);
        } finally {
            users.close();
        }
    }
    
    public void testLdifContacts() throws DictionaryException {
        IElementType contactType = this.dictionary.getType(ElementTypeEnumType.CONTACT.getName());
        IElementField uniqueName = contactType.getField(ContactReservedFieldEnumType.PRINCIPAL_NAME.getName());
        IDictionaryIterator<IMElement> contacts = this.dictionary.query(
                new Relation(RelationOp.EQUALS, uniqueName, Constant.build("klim@mycingular.blackberry.net")), 
                new Date(), null, null);
        try {
            assertTrue( contacts.hasNext() );
            IElement keng = contacts.next();
            String displayName = (String) keng.getValue(contactType.getField(
            		ContactReservedFieldEnumType.DISPLAY_NAME.getName()));
            assertEquals( "keng lim (blackberry)", displayName);
        } finally {
            contacts.close();
        }
    }

    /**
     * test multi-string column 
     * @throws DictionaryException
     */
    public void testLdifContacts2() throws DictionaryException {
		IElementType userType = this.dictionary.getType(ElementTypeEnumType.CONTACT.getName());
		IElementField uniqueName = userType.getField(ContactReservedFieldEnumType.PRINCIPAL_NAME.getName());
		IDictionaryIterator<IMElement> users = this.dictionary.query(new Relation(RelationOp.EQUALS, uniqueName, 
					Constant.build("jed@dubious.com")), new Date(), null, null);
		try {
			assertTrue(users.hasNext());
			IElement user = users.next();
			String[] emailAddresses = (String[]) user.getValue(userType.getField(
					UserReservedFieldEnumType.MAIL.getName()));
			assertNotNull(emailAddresses);
			assertEquals(7, emailAddresses.length);
			String[] expectedEmailAddresses = new String[]{
					"jed.boulton2@bluejungle.com",
					"jeddubious.com@nextlabs.com",
					"jed.boulton2@nextlabs.com",
					"jboulton2@nextlabs.com",
					"jed@dubious.com",
					"jboulton2@bluejungle.com",
					"jeddubious.com@bluejungle.com",	
			};
			for(String expectedEmailAddress : expectedEmailAddresses){
				assertContains(expectedEmailAddress, emailAddresses);
			}
		} finally {
			users.close();
		}
	}
    
    private void assertContains(Object obj, Object[] objs) {
		for (Object o : objs) {
			if (obj.equals(o)) {
				return;
			}
		}
		fail(obj + " is not inside the array " + ArrayUtils.asString(objs));
	}
    
    public void testLdifHosts() throws DictionaryException {
        IElementType hostType = this.dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        IElementField dnsName = hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        IDictionaryIterator<IMElement> hosts = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, dnsName, Constant.build("baixo.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( hosts.hasNext() );
            IElement evia = hosts.next();
            String sid = (String) evia.getValue(hostType.getField(
            		ComputerReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-2650", sid);
        } finally {
            hosts.close();
        }
    }
    
    public void testADHosts() throws DictionaryException {
        IElementType hostType = this.dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        IElementField dnsName = hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        IDictionaryIterator<IMElement> hosts = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, dnsName, Constant.build("3mile.test.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( hosts.hasNext() );
            IElement host3mile= hosts.next(); 
            String sid = (String) host3mile.getValue(hostType.getField(
            		ComputerReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals( "S-1-5-21-830805687-550985140-3285839444-713265", sid );
        } finally {
            hosts.close();
        }
    }
    
    public void testStructualGroups() throws DictionaryException {
    	String [] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=customerdata", "cn=bluejungle.com" };
    	IMGroup group1 = this.dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
    	assertNotNull(group1);
    	IDictionaryIterator<IMGroup> subGroups = group1.getAllChildGroups();
        int count = 0;
        try {
        	assertNotNull(subGroups);
        	while(subGroups.hasNext()) {
        		subGroups.next();
        		count++;
        	}
        } finally {
            subGroups.close();
        }
    	assertEquals(10, count);
    	IDictionaryIterator<IMElement> ids = group1.getAllChildElements();
    	count = this.enroller.count(ids);
    	assertEquals(185, count);
    }
    
    public void testEnumeratedGroups() throws DictionaryException {
    	String[] bluejungle = new String[] { "dc=com", "dc=bluejungle", "dc=customerdata", 
    				"cn=bluejungle.com", "cn=groups", "cn=finance" };
		IMGroup group1 = this.dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
    	assertNotNull(group1);
    	IDictionaryIterator<IMElement> members = group1.getAllChildElements();
    	assertNotNull(members);
    	int count = this.enroller.count(members);
    	assertEquals(3, count);
    }
    
    public void testLinuxEnrollment() throws DictionaryException {
        String [] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=linuxtest"};
        IMGroup group1 = this.dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
        assertNotNull(group1);
        IDictionaryIterator<IMGroup> subGroups = group1.getAllChildGroups();
        int count = 0;
        try {
            assertNotNull(subGroups);
            while(subGroups.hasNext()) {
                subGroups.next();
                count++;
            }
        } finally {
            subGroups.close();
        }
        assertEquals(3, count);
        IDictionaryIterator<IMElement> ids = group1.getAllChildElements();
        count = this.enroller.count(ids);
        assertEquals(12, count);
    }
    
//    public void testLargeLocalEnrollemnt() throws Exception{
//    	 Map<String, String[]> ldif_file3 = new HashMap<String, String[]>();
// 		ldif_file3.put(LdifEnrollmentProperties.LDIF_NAME_PROPERTY,
// 				new String[] { GROUP5K_LDIFFILE });
// 		this.enroller.enroll(GROUP5K_DOMAIN, new String[] { GROUP5K_PROPERTY_FILE },
// 				EnrollmentTypeEnumType.LDIF, ldif_file3);
//    }
//    
//    public void testUpdaateLargeLocalEnrollment() throws Exception{
//    	 Map<String, String[]> ldif_file3 = new HashMap<String, String[]>();
//  		ldif_file3.put(LdifEnrollmentProperties.LDIF_NAME_PROPERTY,
//  				new String[] { GROUP5K_B_LDIFFILE });
//  		this.enroller.update(GROUP5K_DOMAIN, new String[] { GROUP5K_PROPERTY_FILE },
//  				EnrollmentTypeEnumType.LDIF, ldif_file3);
//    }
    
}
