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
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestEnroller;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
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
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * Enroll initial data with local bluejungle data sources
 * 
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/tools/LocalDestinyDataEnroller.java#1 $
 */
public class LocalDestinyDataEnroller extends BaseContainerSharedTestCase {

    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();

    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    private IDictionary dictionary;
    private TestEnroller enroller;
    
    private static String DATA_DIR = SRC_ROOT_DIR + "/server/tools/enrollment/etc/";
    
    private static final String BLUEJUNGLE_DOMAIN  = "bluejungle.com";
    private static String BLUEJUNGLE_DOMAIN_PROPERTY_FILE  = DATA_DIR + "bluejungle.com.destiny.def";
    private static String BLUEJUNGLE_DOMAIN_LDIF_FILE  = DATA_DIR + "bluejungle.com.destiny.ldif";
    
    private static final String BLUEJUNGLE_TEST_DOMAIN = "test.bluejungle.com";
    private static String BLUEJUNGLE_TEST_DOMAIN_PROPERTY_FILE = DATA_DIR + "ad.sample.default.def";
    private static String BLUEJUNGLE_TEST_DOMAIN_CONNECTION_FILE = DATA_DIR + "ad.sample.default.conn";

    private static final String BLUEJUNGLE_LINUXTEST_DOMAIN = "linuxtest.bluejungle.com";
    private static String BLUEJUNGLE_LINUXDOMAIN_PROPERTY_FILE  = DATA_DIR + "linuxtest.bluejungle.com.def";
    private static String BLUEJUNGLE_LINUXDOMAIN_LDIFFILE  = DATA_DIR + "linuxtest.bluejungle.com.ldif";
    
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LocalDestinyDataEnroller.class);
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
    	this.enroller = new TestEnroller();
        this.dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
    }
 
    /**
     * Clean all enrollments
     * @throws DictionaryException
     */
    public void testDeleteAllEnrollments() throws DictionaryException {
    	Collection<IEnrollment> enrollments = this.dictionary.getEnrollments(); 
    	IConfigurationSession session = this.dictionary.createSession();
        try {
            session.beginTransaction();
            for (IEnrollment enrollment : enrollments) {
                session.deleteEnrollment(enrollment);
            }
            session.commit();
        }
        finally {
            session.close();
        }
    }

    /**
     * Enroll local domains
     * @throws Exception
     */
    public void testLocalEnrollments() throws Exception {
        // enroll bluejungle.com domain (LDIF file)
        Map<String, String[]> ldif_file1 = new HashMap<String, String[]>(); 
        ldif_file1.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {BLUEJUNGLE_DOMAIN_LDIF_FILE} );
        this.enroller.enroll(BLUEJUNGLE_DOMAIN, 
    			new String [] {BLUEJUNGLE_DOMAIN_PROPERTY_FILE },  
    			EnrollmentTypeEnumType.LDIF, ldif_file1);
        
        // enroll test.blujungle.com domain (AD server)
        this.enroller.enroll(BLUEJUNGLE_TEST_DOMAIN, 
    			new String[] { BLUEJUNGLE_TEST_DOMAIN_PROPERTY_FILE, BLUEJUNGLE_TEST_DOMAIN_CONNECTION_FILE },  
    			EnrollmentTypeEnumType.DIRECTORY, ldif_file1);
        
        // enroll linuxtest.bluejungle.com domain (LDIF file) 
        Map<String, String[]> ldif_file2 = new HashMap<String, String[]>(); 
        ldif_file2.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {BLUEJUNGLE_LINUXDOMAIN_LDIFFILE} );
        this.enroller.enroll(BLUEJUNGLE_LINUXTEST_DOMAIN, 
                new String[] { BLUEJUNGLE_LINUXDOMAIN_PROPERTY_FILE },  
                EnrollmentTypeEnumType.LDIF, ldif_file2);
    }
  
    public void testLdifUsers() throws DictionaryException {
        IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        IElementField uniqueName = userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
        IDictionaryIterator<IMElement> users = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, uniqueName, Constant.build("rlin@bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( users.hasNext() );
            IElement keni = users.next();
            String sid = (String) keni.getValue(userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-2628", sid);
        }
        finally {
            users.close();
        }
    }
    
    public void testADUsers() throws DictionaryException {
        IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        IElementField uniqueName = userType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
        IDictionaryIterator<IMElement> users = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, uniqueName, Constant.build("jimmy.carter@linuxtest.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( users.hasNext() );
            IElement jimmy = users.next();
            String sid = (String) jimmy.getValue(userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals( "S-1-5-21-3473659527-1420930692-2131300604-1380", sid );
        }
        finally {
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
            String displayName = (String) keng.getValue(contactType.getField(ContactReservedFieldEnumType.DISPLAY_NAME.getName()));
            assertEquals( "keng lim (blackberry)", displayName);
        }
        finally {
            contacts.close();
        }
    }
    
    public void testLdifHosts() throws DictionaryException {
        IElementType hostType = this.dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        IElementField dnsName = hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        IDictionaryIterator<IMElement> hosts = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, dnsName, Constant.build("evia.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( hosts.hasNext() );
            IElement evia = hosts.next();
            String sid = (String) evia.getValue(hostType.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-1198", sid);
        }
        finally {
            hosts.close();
        }
        
    }
    
    public void testADHosts() throws DictionaryException {
        IElementType hostType = this.dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        IElementField dnsName = hostType.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
        IDictionaryIterator<IMElement> hosts = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, dnsName, Constant.build("40LOVE.linuxtest.bluejungle.com")), 
        		new Date(), null, null);
        try {
            assertTrue( hosts.hasNext() );
            IElement host3mile= hosts.next(); 
            String sid = (String) host3mile.getValue(hostType.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName()));
            assertEquals( "S-1-5-21-3473659527-1420930692-2131300604-1489", sid );
        }
        finally {
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
        }
        finally {
            subGroups.close();
        }
    	assertEquals(10, count);
    	IDictionaryIterator<IMElement> ids = group1.getAllChildElements();
    	count = this.enroller.count(ids);
    	assertEquals(185, count);
    }
    
    public void testEnumeratedGroups() throws DictionaryException {
    	String [] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=customerdata", "cn=bluejungle.com", "cn=groups", "cn=finance" };
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
        }
        finally {
            subGroups.close();
        }
        assertEquals(7, count);
        IDictionaryIterator<IMElement> ids = group1.getAllChildElements();
        count = this.enroller.count(ids);
        
        //people are adding entity to the test root.
        // the count check is not correct now
//        assertEquals(81, count);
    }
    
}
