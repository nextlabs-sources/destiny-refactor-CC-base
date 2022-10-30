/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.ColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IReferenceable;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ldif/TestLdifEnroller.java#1 $
 */

public class TestLdifEnroller extends BaseEnrollerSharedTestCase {

    private static final String DATA_DIR = SRC_ROOT_DIR + "/server/tools/enrollment/etc/";
    
    private static final String LDIF_DOMAIN         = "TestLdifEnroller";
    private static final String LDIF_PROPERTY_FILE  = DATA_DIR + "bluejungle.com.destiny.def";
    private static final String LDIF_FILE  			= DATA_DIR + "bluejungle.com.destiny.ldif";
    private static final String LDIF_TEST_FILES_DIR = SRC_ROOT_DIR + "/../test_files/com/bluejungle/tools/enrollment/";
    private static final String LDIF_FILE2          = LDIF_TEST_FILES_DIR + "aged_user.bluejungle.com.ldif";
    
    private static final String AGE_COLUMN          = "age";
    private static final String BIRTHDAY_COLUMN     = "birthday";
    
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestLdifEnroller.class);
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
		} finally {
			session.close();
		}
	}

    /**
     * Enroll local domains
     * @throws Exception
     */
    public void testInvalidLDIFFile() throws Exception {
        Map<String, String[]> ldif_file1 = new HashMap<String, String[]>(); 
        ldif_file1.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {"INVALID_LDIF_FILE"} );
        try {
            IRealmData data = convert(
                    LDIF_DOMAIN + "2",
                    EnrollmentTypeEnumType.LDIF,
                    ldif_file1,
                    LDIF_PROPERTY_FILE);
            
            enrollmentManager.createRealm(data);
            enrollmentManager.enrollRealm(convert(data.getName()));
            
            fail("Invalid LDIF file enrollment should have failed");
        } catch (EnrollmentValidationException e) {
        	assertNotNull(e);
        }
    }
    
    /**
     * Enroll local domains
     * @throws Exception
     */
    public void testLocalEnrollments() throws Exception {
        Map<String, String[]> ldif_file1 = new HashMap<String, String[]>(); 
        ldif_file1.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {LDIF_FILE} );
        
        IRealmData data = convert(
                LDIF_DOMAIN,
                EnrollmentTypeEnumType.LDIF,
                ldif_file1,
                LDIF_PROPERTY_FILE);
        
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
    }

    public void testLdifUsers() throws Exception {
        IDictionaryIterator<IMElement> users = dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, 
        		"rlin@bluejungle.com");
        try {
            assertTrue( users.hasNext() );
            IElement user = users.next();
            String sid = getStringFromElement(user, UserReservedFieldEnumType.WINDOWS_SID);
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-2628", sid);
		} finally {
            users.close();
        }
    }
    
    public void testLdifContacts() throws Exception {
        IDictionaryIterator<IMElement> contacts =	dictionaryQueryEquals(ContactReservedFieldEnumType.PRINCIPAL_NAME, 
        		"klim@mycingular.blackberry.net");
        try {
            assertTrue( contacts.hasNext() );
            IElement contact = contacts.next();
            String displayName = getStringFromElement(contact, ContactReservedFieldEnumType.DISPLAY_NAME);
            assertEquals( "keng lim (blackberry)", displayName);
        } finally {
            contacts.close();
        }
    }
    
    public void testLdifHosts() throws Exception {
        IDictionaryIterator<IMElement> hosts = dictionaryQueryEquals(ComputerReservedFieldEnumType.DNS_NAME, 
        		"evia.bluejungle.com");
        try {
            assertTrue( hosts.hasNext() );
            IElement evia = hosts.next();
            String sid = getStringFromElement(evia, ComputerReservedFieldEnumType.WINDOWS_SID);
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-1198", sid);
		} finally {
            hosts.close();
        }
    }
    
    public void testStructualGroups() throws Exception {
    	String [] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=customerdata", 
    			"cn=bluejungle.com" };
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
    	count = count(ids);
    	assertEquals(185, count);
    }
    
    public void testEnumeratedGroups() throws Exception {
    	String [] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=customerdata", 
    			"cn=bluejungle.com", "cn=groups", "cn=finance" };
    	IMGroup group1 = this.dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
    	assertNotNull(group1);
    	IDictionaryIterator<IMElement> members = group1.getAllChildElements();
    	assertNotNull(members);
    	int count = count(members);
    	assertEquals(3, count);
    }
    
    public void testEnrollmentStatus() throws Exception {
        Collection<IEnrollment> enrollments = this.dictionary.getEnrollments();
    	Iterator<IEnrollment> itor = enrollments.iterator();
    	assertTrue( itor.hasNext() );
    	IEnrollment enrollment = itor.next();
    	assertNotNull(enrollment);
    	IUpdateRecord record = enrollment.getStatus();
    	assertNotNull(record);
    	assertNotNull(record.getStartTime());
    	assertNotNull(record.getEndTime());
    	assertNotNull(record.getErrorMessage());
    }
    
    private static final String [] UPDATE_USER_To_DELETE  
        = new String[] {"dc=com", "dc=destiny", "dc=customerdata", "cn=bluejungle.com", 
    	"cn=my_update_user1" };
    
    private static final String [] UPDATE_USER_To_ADD  
    	= new String[] {"dc=com", "dc=destiny", "dc=customerdata", "cn=bluejungle.com", "cn=users", 
    	"cn=jay campan"};

    public void testLocalEnrollmentUpdate() throws Exception {
    	try {
            // add element in enrollment
            List<IElementBase> elements = new ArrayList<IElementBase>();
            IEnrollment enrollment = this.dictionary.getEnrollment(LDIF_DOMAIN);
            IEnrollmentSession session = enrollment.createSession();
            session.beginTransaction();
            IElementType userType = this.dictionary.getType(ElementTypeEnumType.USER.getName());
            IElement element = enrollment.makeNewElement(new DictionaryPath(UPDATE_USER_To_DELETE), userType, 
                        new DictionaryKey(UPDATE_USER_To_DELETE[4].getBytes()));
            List<DictionaryPath> deletes = new ArrayList<DictionaryPath>();
            deletes.add(new DictionaryPath(UPDATE_USER_To_ADD));
            Collection<IReferenceable> e2deletes = enrollment.getProvisionalReferences(deletes);
            assertNotNull(e2deletes); 
            assertTrue(deletes.iterator().hasNext());
            assertNotNull( element );
            elements.add(element);
            session.saveElements(elements);
            session.commit();
            session.close(true, "Update enrollment successful");
            
            // enroll with update
            enrollmentManager.enrollRealm(convert(LDIF_DOMAIN));
            
    	} catch (Exception e) {
    		fail("enrollment update test failed" + e);
    		throw new EnrollmentSyncException(e);
    	}    	
    }
    
    public void testUpdateEnrollmentElementResult() throws Exception {
        IEnrollment enrollment = this.dictionary.getEnrollment(LDIF_DOMAIN);
       
        // make sure the element is deleted
        IMElement element = enrollment.getElement(new DictionaryKey(UPDATE_USER_To_DELETE[4].getBytes()), new Date());
        assertNull(element);

        // make sure the element is added
        IDictionaryIterator<IMElement> users = dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME,
						"sergey@bluejungle.com");
        try {
            assertTrue( users.hasNext() );
            IElement keni = users.next();
            String sid = getStringFromElement(keni, UserReservedFieldEnumType.WINDOWS_SID);
            assertEquals("S-1-5-21-668023798-3031861066-1043980994-1157", sid);
        } finally {
            users.close();
        }
    
    }
    
    public void testUpdatedEnrollmentGroupResult() throws Exception {
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
        count = count(ids);
        assertEquals(185, count);
    }

    public void testEnrollmentWithNewColumn() throws Exception {
        // add new Column
        IColumnData columnData;
        columnData = new ColumnData(
                AGE_COLUMN, 
                ElementTypeEnumType.USER.getName(), 
                AGE_COLUMN, 
                ElementFieldType.NUMBER.getName().toUpperCase());
        enrollmentManager.addColumn(columnData);
        
        columnData = new ColumnData(
                BIRTHDAY_COLUMN, 
                ElementTypeEnumType.USER.getName(), 
                BIRTHDAY_COLUMN, 
                ElementFieldType.DATE.getName().toUpperCase());
        enrollmentManager.addColumn(columnData);
        
        // make sure new  column is added
        IElementType user = this.dictionary.getType(ElementTypeEnumType.USER.getName());
        assertNotNull(user);
        assertTrue(user.getField(AGE_COLUMN) != null );
        assertTrue(user.getField(BIRTHDAY_COLUMN) != null );
        
        Map<String, String[]> ldif_file = new HashMap<String, String[]>(); 
        ldif_file.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {LDIF_FILE2} );
        ldif_file.put( "user.number.age", new String[] {AGE_COLUMN} );
        ldif_file.put( "user.date.birthday", new String[] {BIRTHDAY_COLUMN} );
        IRealmData realmData = convert(
                LDIF_DOMAIN, 
                EnrollmentTypeEnumType.LDIF,
                ldif_file,
                LDIF_PROPERTY_FILE
                ); 
        enrollmentManager.updateRealm(realmData);
        enrollmentManager.enrollRealm(convert(realmData.getName()));
                
        // check the data
        String aged_user_uniqueName = "aged_user@bluejungle.com";
        IElement user2 = this.dictionary.getElement(aged_user_uniqueName, new Date());
        assertNotNull(user2);

        
        IElementField[] ageColumnFields = this.dictionary.getEnrollment(LDIF_DOMAIN)
                .lookupField(user, AGE_COLUMN);
        assertEquals(1, ageColumnFields.length);
        assertEquals("5", user2.getValue(ageColumnFields[0]).toString());
        // TODO: should have time part of java Date ????
        IElementField[] birthdayColumnFields = this.dictionary.getEnrollment(LDIF_DOMAIN)
            .lookupField(user, BIRTHDAY_COLUMN);
        assertEquals(1, birthdayColumnFields.length);
        assertEquals("Sat Nov 04 00:00:00 PST 2000", user2.getValue(birthdayColumnFields[0]).toString());
    }
    
    public void testDeleteColumn() throws Exception{
        // delete columns
        enrollmentManager.delColumn(AGE_COLUMN, ElementTypeEnumType.USER.getName());
        enrollmentManager.delColumn(BIRTHDAY_COLUMN, ElementTypeEnumType.USER.getName());  
    }

}
