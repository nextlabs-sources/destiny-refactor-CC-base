/*
 * Created on Jul 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;

/**
 * Make sure the basic test cases pass first.
 * 
 * @see TestActiveDirectoryEnrollerSmallOuBasic
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnrollerSmallOuComplex.java#1 $
 */
public class TestActiveDirectoryEnrollerSmallOuComplex extends BaseEnrollerSharedTestCase {
    private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
    private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "/test/ad.unittest.def";
    private static final String UNITTEST_CONN_FILE  = ENROLL_DIR + "test/ad.unittest.conn";

    private static final String ROOT_DN = ActiveDirectoryTestHelper.generateRootDn("unittest small complex");
    
    private static final ActiveDirectoryTestHelper AD_HELPER =
            new ActiveDirectoryTestHelper(ROOT_DN);

    private static final String USERNAME_TEMPLATE = "titmouse%03d-%02d";
    private static final String USER_DN_TEMPLATE = "cn=%s,ou=users%d," + ROOT_DN;

    protected String getPasswordPropertyKey() {
        return ActiveDirectoryEnrollmentProperties.PASSWORD;
    }
    
    private static final String USERS1_DN = "ou=users1," + ROOT_DN;
    private static final String USERS2_DN = "ou=users2," + ROOT_DN;
    private static final String USERS3_DN = "ou=users3," + ROOT_DN;
    private static final String GROUP_DN  = "ou=groups," + ROOT_DN;
    
    /**
     * Clean all enrollments
     * @throws DictionaryException
     * @throws SQLException 
     * @throws HibernateException 
     * @throws LDAPException 
     */
    public void testCheckCleanEnrolment() throws DictionaryException, HibernateException,
            SQLException, LDAPException {
        reset();
        dbCount.checkDatabaseCount();
        AD_HELPER.removeAllUnitTestData(ROOT_DN);
    }
    
    /**
     * - users1
     *   - titmouse001-00
     *   - titmouse001-01
     * - users2
     *   - titmouse002-00
     *   - titmouse002-01
     * - users3
     *   - titmouse003-00
     *   - titmouse003-01
     * - groups
     *   - group
     *     * platyrhychos
     *   
     */
    public void testAddUserIntoAD() throws LDAPException {
        final String[] usersDns = new String[] { USERS1_DN, USERS2_DN, USERS3_DN, GROUP_DN };
        
        String dn = null;
        
        AD_HELPER.createOuRecursively(ROOT_DN);
        for(String usersDn : usersDns){
            AD_HELPER.createOu(usersDn);
        }
        try {
            for (int dnCount = 0; dnCount < 3; dnCount++) {
                for (int i = 0; i < 2; i++) {
                    String username = String.format(USERNAME_TEMPLATE, dnCount + 1, i);
                    LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, i);
                    dn = String.format(USER_DN_TEMPLATE, username, dnCount + 1);
    
                    LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
                    AD_HELPER.add(newEntry);
                }
            }
        } catch (LDAPException e) {
            System.out.println("current dn = " + dn);
            throw e;
        }
        
        
        //add reference
        String username = "platyrhychos";
        dn = "cn="+username+",OU=" + ActiveDirectoryTestHelper.OU_NAME + "," + ActiveDirectoryTestHelper.BASE_DN;
        LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, 0);
        if (!AD_HELPER.isExist(dn)) {
            AD_HELPER.add(new LDAPEntry(dn, attributeSet));
        }
        
        LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet(); 
        groupAttributeSet.add(new LDAPAttribute("member", dn));
        groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
        dn = "cn=group," + GROUP_DN;
        AD_HELPER.add(new LDAPEntry(dn, groupAttributeSet));
    }
    
    private LDAPAttributeSet setupLDAPUserAttributes(String username, int i) {
        LDAPAttributeSet attributeSet = new LDAPAttributeSet();

        attributeSet.add(new LDAPAttribute("objectclass", "inetOrgPerson"));
        attributeSet.add(new LDAPAttribute("proxyAddresses", new String[] {
                String.format("%s@unitest.nextlabs.com", username),
                String.format("Smtp:%s@unitest.bluejungle.com", username) }));
        attributeSet.add(new LDAPAttribute("cn", username));
        attributeSet.add(new LDAPAttribute("userPrincipalName", username));

        //the "name" is setted automatically
        //      attributeSet.add(new LDAPAttribute("name", username));
        attributeSet.add(new LDAPAttribute("givenName", String.format("%s%04d", "given", i)));
        attributeSet.add(new LDAPAttribute("sn", String.format("%s%04d", "sn", i)));
        attributeSet.add(new LDAPAttribute("title", String.format("%s%02d", "given", i % 100)));
        attributeSet.add(new LDAPAttribute("company", "Nextlabs"));
        attributeSet.add(new LDAPAttribute("department", String.format("%s%01d", "given", i % 10)));
        attributeSet.add(new LDAPAttribute("co", "United State"));
        attributeSet.add(new LDAPAttribute("c", "US"));
        attributeSet.add(new LDAPAttribute("CountryCode", "0"));

        //random generated by Windows 2003 and not windows 2000
        //      attributeSet.add(new LDAPAttribute("SAMAccountName", username));
        return attributeSet;
    }
    
    private Map<String, String[]> getOverrideProperties() {
        Map<String, String[]> overrideProperties = encodePassword(AD_HELPER.PASSWORD);
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.ROOTS,
                new String[] { ROOT_DN });
        return overrideProperties;
    }
    
    public void testSyncCp1() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(0, enrollments.size());

        Date startTime = new Date();
        IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                getOverrideProperties(), 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        postCheck(startTime);
        
        dbCount.printDatabaseCount();
    }
    
    /**
     * - users2
     *   - titmouse002-00
     *   - titmouse001-01
     *   - users1
     *     - titmouse001-00
     *     - titmouse002-01
     * - users3
     *   - titmouse003-00
     *   - titmouse003-01
     * - groups
     *   - group
     *     * platyrhychos
     *   
     */
    public void testMoveOu() throws Exception {
        int groupId = 1; 
        String username = String.format(USERNAME_TEMPLATE, groupId, 1);
        String oldDn = String.format(USER_DN_TEMPLATE, username, groupId);
        String newDn = String.format(USER_DN_TEMPLATE, username, 2);
        AD_HELPER.move(oldDn, newDn);

        
        AD_HELPER.move(USERS1_DN, "ou=users1," +USERS2_DN);
        
        
        groupId = 2;
        username = String.format(USERNAME_TEMPLATE, groupId, 1);
        oldDn = String.format(USER_DN_TEMPLATE, username, groupId);
        newDn = "cn=" + username + ",ou=users1,ou=users2," + ROOT_DN;
        AD_HELPER.move(oldDn, newDn);
    }
    
    public void testSyncCp2() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());

        Date startTime = new Date();
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
        postCheck(startTime);
        
        dbCount.printDatabaseCount();
    }
    
    public void testSyncCp3() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());

        Date startTime = new Date();
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
        postCheck(startTime);
        
        dbCount.printDatabaseCount();
    }
    
    /**
     * remove reference member
     * 
     * - users2
     *   - titmouse002-00
     *   - titmouse001-01
     *   - users1
     *     - titmouse001-00
     *     - titmouse002-01
     * - users3
     *   - titmouse003-00
     *   - titmouse003-01
     * - groups
     *   - group
     *     
     *   
     */
    public void testDeleteReferenceMember() throws Exception {
        String username = String.format(USERNAME_TEMPLATE, 3, 0);
        String dn = String.format(USER_DN_TEMPLATE, username, 3);
        LDAPModification mods = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute("member", dn));
        AD_HELPER.modify("cn=group," + GROUP_DN, mods);
    }

    public void testSyncCp4() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());

        Date startTime = new Date();
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
        postCheck(startTime);
        
        dbCount.printDatabaseCount();
    }
    
    public void testMoveRootOu() throws NamingException{
        AD_HELPER.move(USERS2_DN, "ou=newUsers2," + ROOT_DN);
    }
    
    public void testSyncCp5() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());

        Date startTime = new Date();
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
        postCheck(startTime);
        
        dbCount.printDatabaseCount();
    }
}
