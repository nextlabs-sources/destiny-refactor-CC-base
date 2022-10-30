/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.DictionarySizeCount;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;


/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnroller.java#1 $
 */

public class TestActiveDirectoryEnroller extends BaseEnrollerSharedTestCase {
   
    private static final String AD_DOMAIN                 = "test.ActiveDirectoryEnroller01";
    private static final String I388_DOMAIN_NAME          = "TEST_AD_I388_ENROLL";
    private static final String AD_DOMAIN_PROPERTY_FILE   = ENROLL_DIR + "ad.sample.default.def";
    private static final String AD_DOMAIN_CONNECTION_FILE = ENROLL_DIR + "ad.sample.default.conn";
    private static final String AD_MINI_DEF_FILE          = ENROLL_DIR + "test/ad.mini.def";
    private static final String AD_MINI_CONN_FILE         = ENROLL_DIR + "test/ad.mini.conn";
    private static final String I388_DEF_FILE             = ENROLL_DIR + "test/ad.i388.def";
    private static final String I388_CONN_FILE            = ENROLL_DIR + "test/ad.i388.conn";
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestActiveDirectoryEnroller.class);
    }

    protected String getPasswordPropertyKey(){
        return ActiveDirectoryEnrollmentProperties.PASSWORD;
    }
    
    private static int[] dbRows = new int[7];
    
    /**
     * Clean all enrollments
     * @throws DictionaryException
     * @throws SQLException 
     * @throws HibernateException 
     */
    public void testDeleteAllEnrollments() throws DictionaryException, HibernateException,
            SQLException {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        for (IEnrollment enrollment : enrollments) {
            session.deleteEnrollment(enrollment);
        }
        session.commit();
        session.close();
        
        dbCount.checkDatabaseCount(dbRows );
    }
    
    public void testIncorrectFilter() throws Exception{
        Map<String, String[]> overrideProperties = new HashMap<String, String[]>();
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.FILTER, new String[]{"objectclass=("});
        
        IRealmData data = convert(
                AD_DOMAIN + "_IncorrectFilter",
                EnrollmentTypeEnumType.DIRECTORY,
                overrideProperties,
                AD_DOMAIN_PROPERTY_FILE,
                AD_DOMAIN_CONNECTION_FILE);
        
        try {
            enrollmentManager.createRealm(data);
            fail("should fail on invalid filter");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
    
    
    /**
     * Enroll local domains
     * @throws DictionaryException 
     * @throws SQLException 
     * @throws HibernateException 
     * @throws Exception
     */
    public void testLocalEnrollments() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(0, enrollments.size());
        
        IRealmData data = convert(
                AD_DOMAIN,
                EnrollmentTypeEnumType.DIRECTORY,
                null,   //no override 
                AD_DOMAIN_PROPERTY_FILE,
                AD_DOMAIN_CONNECTION_FILE);
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());
        IEnrollment enrollment = enrollments.iterator().next();
        IUpdateRecord records = enrollment.getStatus();
        assertNotNull(records);
        assertTrue(records.isSuccessful());
        
        //people are adding new entity into the test root.
        //The count checking becomes meaningless
//        dbRows = DictionarySizeCount.add(dbRows, new int[] { 92, 0, 19, 7, 0, 69, 4 });
//        dbCount.checkDatabaseCount(dbRows);
    }
  
    public void testADUsers() throws DictionaryException {
        IDictionaryIterator<IMElement> users = dictionaryQueryEquals(
                UserReservedFieldEnumType.PRINCIPAL_NAME,
                "jimmy.carter@linuxtest.bluejungle.com");
        try {
            assertTrue( users.hasNext() );
            IElement user = users.next();
            String sid = getStringFromElement(user, UserReservedFieldEnumType.WINDOWS_SID);
            assertEquals("S-1-5-21-3473659527-1420930692-2131300604-1380", sid);         
        } finally {
            users.close();
        }
    }
    
    //FIXME broken after moved from cuba to linuxad01
//    public void testADContacts() throws DictionaryException {
//        IDictionaryIterator<IMElement> contacts = dictionaryQueryEquals(
//                ContactReservedFieldEnumType.PRINCIPAL_NAME, 
//                "lin.robert@gmail.com");
//        try {
//            assertTrue( contacts.hasNext() );
//            IElement contact = contacts.next();
//            String displayName = getStringFromElement(contact, 
//                    ContactReservedFieldEnumType.DISPLAY_NAME); 
//            assertEquals( "robert i. lin", displayName);
//        } finally {
//            contacts.close();
//        }
//    }
    
    public void testADHosts() throws DictionaryException {
        IDictionaryIterator<IMElement> hosts = dictionaryQueryEquals(
                ComputerReservedFieldEnumType.DNS_NAME, 
                "40LOVE.linuxtest.bluejungle.com");
        try {
            assertTrue( hosts.hasNext() );
            IElement host = hosts.next();
            assertEquals( "40LOVE", host.getDisplayName());
            assertEquals( "40LOVE.linuxtest.bluejungle.com", host.getUniqueName());
        } finally {
            hosts.close();
        }
    }
    
    public void testStructualGroups() throws DictionaryException {
        String[] bluejungle = new String[] {
                "dc=com", "dc=bluejungle", "dc=linuxtest", "ou=presidents", "ou=users"};
        IMGroup group1 = dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
        assertNotNull(group1);
        IDictionaryIterator<IMGroup> subGroups = group1.getAllChildGroups();
        assertNotNull(subGroups);
        int count = 0;
        while(subGroups.hasNext()) {
            subGroups.next();
            count++;
        }
        subGroups.close();
        assertEquals(0, count);
        IDictionaryIterator<IMElement> ids = group1.getAllChildElements();
        count = count(ids);
        //assertEquals(44, count);
    }
    
    public void testEnumeratedGroups() throws DictionaryException {
        String[] bluejungle = new String[] {"dc=com", "dc=bluejungle", "dc=linuxtest", 
                                             "ou=presidents", "ou=groups", "cn=b2b sales"};
        IMGroup group1 = dictionary.getGroup(new DictionaryPath( bluejungle ), new Date());
        assertNotNull(group1);
        IDictionaryIterator<IMElement> members = group1.getAllChildElements();
        assertNotNull(members);
        int count = count(members);
        assertEquals(1, count);
    }

    public void testEnrollmentStatus() throws DictionaryException {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertNotNull(enrollments);
        assertFalse( enrollments.isEmpty());
        IEnrollment enrollment = enrollments.iterator().next();
        assertNotNull(enrollment);
        IUpdateRecord record = enrollment.getStatus();
        assertNotNull(record);
        assertNotNull(record.getStartTime());
        assertNotNull(record.getEndTime());
        assertNotNull(record.getErrorMessage());
    }
    
    /**
     * Enroll local domains
     * @throws DictionaryException 
     * @throws SQLException 
     * @throws HibernateException 
     * @throws Exception
     */
    public void testLocalMiniEnrollments() throws Exception {
        if(true){
          //TODO confirm this is out-of-date test
            //skipped, this test is out of date
            return;
        }
        
        IRealmData data = convert(
                "TEST_AD_MINI_ENROLL",
                EnrollmentTypeEnumType.DIRECTORY,
                null,   //no override
                AD_MINI_DEF_FILE,
                AD_MINI_CONN_FILE);
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        dbRows = DictionarySizeCount.add(dbRows, new int[] { 304, 0, 100, 200, 0, 200, 4 });
        dbCount.checkDatabaseCount(dbRows);
    }
   
    public void testI388Enrollment() throws Exception {
        if(true){
            //TODO confirm this is out-of-date test
            //skipped, this test is out of date. 
            return;
        }
        
        //enroll
        
        IRealmData data = convert(
                I388_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                null,   //no override 
                I388_DEF_FILE,
                I388_CONN_FILE);
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        dbRows = DictionarySizeCount.add(dbRows, new int[] { 5014, 0, 9, 3500, 0, 5001, 4 });
        dbCount.checkDatabaseCount(dbRows);
        
        IEnrollment enrollment = dictionary.getEnrollment(I388_DOMAIN_NAME);
        assertNotNull(enrollment);
        assertNotNull(enrollment.getStatus());
        assertTrue(enrollment.getStatus().isSuccessful());
        
        enrollmentManager.enrollRealm(data);
        
//        enroller.enroll(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, I388_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        enroller.enroll(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, I388_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        enroller.enroll(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, I388_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        enroller.enroll(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, I388_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        enroller.enroll(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, I388_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
        
        //update
//        enroller.update(I388_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, LONELY_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        dbRows = add(dbRows, new int[] { 3, 0, 0, 0, 0, 1, 2 });
//        checkDatabaseCount(dbRows);
//        
//        //delete
//        assertTrue(enrollment.getStatus().isSuccessful());
//
//        enroller.delete(I388_DOMAIN_NAME);
//
//        dbRows = add(dbRows, new int[] { 0, 0, 0, 0, 0, 0, 0 });
//        checkDatabaseCount(dbRows);
    }
    
    
//    public void testAdChanges() throws Exception{
//        if(true){
//            return;
//        }
//        removeAllUnitTestData(true, true);
//        addDataToData(10, 430);
//        
//        Map<String, String[]> encode_password = encodePassword("jimmy.carter"); 
//        this.enroller.enroll(UNITTEST_DOMAIN_NAME, 
//                new String[] {I388_DEF_FILE, UNITTEST_CONN_FILE },  
//                EnrollmentTypeEnumType.DIRECTORY, encode_password);
//        
//        IEnrollment enrollment = dictionary.getEnrollment(UNITTEST_DOMAIN_NAME);
//        assertNotNull(enrollment);
//        assertNotNull(enrollment.getStatus());
//        assertTrue(enrollment.getStatus().isSuccessful());
//        
//        connection.disconnect();
//        
//        LDAPSearchResults rs = connection.search(UNIT_TEST_AD_GROUPS_ROOT, 
//                LDAPConnection.SCOPE_ONE, "", new String[] {"dn"}, false);
//
//        while (rs.hasMore()) {
//            LDAPModification mod = new LDAPModification(LDAPModification.REPLACE, 
//                    new LDAPAttribute("member",    new String[] {}));
//            connection.modify(rs.next().getDN(), mod);
//        }
//        
//         this.enroller.enroll(UNITTEST_DOMAIN_NAME, 
//                    new String[] {I388_DEF_FILE, UNITTEST_CONN_FILE },  
//                    EnrollmentTypeEnumType.DIRECTORY, encode_password);
//    }
//    
//    protected void addDataToData(int numberOfGroups, int usersPerGroup) throws LDAPException {
//        String dn = null;
//        try {
//            connection = getConnection();
//            for(int groupCount = 0; groupCount < numberOfGroups ; groupCount++){
//                LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet();
//                List<String> dns = new LinkedList<String>();
//                for(int i =0 ; i < usersPerGroup; i++){
//                    LDAPAttributeSet attributeSet = new LDAPAttributeSet();
//                    
//                    String username = String.format("%s%04d-%04d", "lark", groupCount, i);
//                    
//                    attributeSet.add(new LDAPAttribute("objectclass", "inetOrgPerson"));
//                    attributeSet.add(new LDAPAttribute("mail", String.format("%s@unitest.nextlabs.com",
//                            username)));
//                    attributeSet.add(new LDAPAttribute("cn", username));
//                    String format = "CN=%s,OU=Users,OU=unittest,OU=horkan,DC=test,DC=bluejungle,DC=com";
//                    dn = String.format(format, username);
//                    
//                    dns.add(dn);
//                    LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
//                    connection.add(newEntry);
//                }
//                
//                groupAttributeSet.add(new LDAPAttribute("member", dns.toArray(new String[]{})));
//                groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
//                dn = "CN=group%04d,OU=Groups,OU=unittest,OU=horkan,DC=test,DC=bluejungle,DC=com";
//                LDAPEntry newEntry = new LDAPEntry(String.format(dn, groupCount), groupAttributeSet);
//                connection.add(newEntry);
//            }
//        } catch (LDAPException e) {
//            System.out.println("current dn = " + dn);
//            throw e;
//        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
//    }
//    
//    protected void removeAllUnitTestData(boolean users, boolean groups) throws LDAPException {
//        try {
//            connection = getConnection();
//            LDAPSearchResults rs;
//            
//            if (users) {
//                boolean hasMore = true;
//                while (hasMore) {
//                    rs = connection.search(UNIT_TEST_AD_USERS_ROOT, 
//                            LDAPConnection.SCOPE_ONE, "", new String[] {"dn"}, false);
//        
//                    try {
//                        while (rs.hasMore()) {
//                            connection.delete(rs.next().getDN());
//                        }
//                        hasMore = false;
//                    } catch (LDAPException e) {
//                        if (e.getResultCode() == 4) {
//                            hasMore = true;
//                        } else {
//                            throw e;
//                        }
//                    }
//                }
//            }
//            
//            if (groups) {
//                boolean hasMore = true;
//                while (hasMore) {
//                    rs = connection.search(UNIT_TEST_AD_GROUPS_ROOT, 
//                            LDAPConnection.SCOPE_ONE, "", new String[] {"dn"}, false);
//        
//                    try {
//                        while (rs.hasMore()) {
//                            connection.delete(rs.next().getDN());
//                        }
//                        hasMore = false;
//                    } catch (LDAPException e) {
//                        if (e.getResultCode() == 4) {
//                            hasMore = true;
//                        } else {
//                            throw e;
//                        }
//                    }
//                }
//            }
//        } finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }
//    }
//    
//    private static final String LOGIN_PASSWORD = "jimmy.carter";
//    private static final String LOGIN_DN = "cn=Jimmy Carter,ou=Users,ou=Fixed,dc=test,dc=bluejungle,dc=com";
//
//    protected static LDAPConnection connection = new LDAPConnection();
//    
//    protected LDAPConnection getConnection() throws LDAPException {
//        LDAPConnection newConnection = null;
//        if (!connection.isConnected()) {
//            initializeConnection();
//        }
//        newConnection = (LDAPConnection) connection.clone();
//
//        // Enable referral handling:
//        LDAPSearchConstraints searchConstraints = newConnection.getSearchConstraints();
//        searchConstraints.setReferralFollowing(true);
//        
//        
//        searchConstraints.setReferralHandler(new LDAPBindHandlerImpl(
//                LOGIN_DN, 
//                LOGIN_PASSWORD));
//        searchConstraints.setMaxResults(0);
//        searchConstraints.setTimeLimit(300000);
//        searchConstraints.setServerTimeLimit(30000000);
//        newConnection.setConstraints(searchConstraints);
//
//        return newConnection;
//    }
//    
//    
//    
//    private void initializeConnection() throws LDAPException {
//        // Create a connection:
//        byte[] passwd = null;
//        passwd = LOGIN_PASSWORD.getBytes();
//
//        connection.connect("cuba.test.bluejungle.com", 389);
//        connection.bind(LDAPConnection.LDAP_V3, LOGIN_DN, passwd);
//    }
    
    /**
     * Clean existing enrollment
     * @throws DictionaryException
    public void testChangeTrackingEnrollment() throws Exception {
        // Delete existing enrollment
        IConfigurationSession session = this.dictionary.createSession();
        session.beginTransaction();
        IEnrollment enrollment = this.dictionary.getEnrollment(AD_DOMAIN);
        session.deleteEnrollment(enrollment);
        session.commit();
        session.close();
        
        // Create a new AD change tracking enrollment
        Map propertyChanges = new HashMap();
        propertyChanges.put(ActiveDirectoryEnrollmentProperties.DIRSYNC_ENABLED, "true");
        IRealmData realm = setupLocalDomain(AD_DOMAIN, 
                new String[] {AD_DOMAIN_PROPERTY_FILE, AD_DOMAIN_CONNECTION_FILE },  
                EnrollmentTypeEnumType.ACTIVE_DIRECTORY,  propertyChanges);
        assertNotNull(realm);
        
        // Test sync change tracking enrollment
        this.enrollmentManager.enrollRealm(realm);  
        +.
        3a 
        
        Thread.sleep(1000);
        
        // Test second sync 
        this.enrollmentManager.enrollRealm(realm);
        
        Thread.sleep(1000);
        
        // Test third sync 
        this.enrollmentManager.enrollRealm(realm);
    }
     */
    
}
