/*
 * Created on Sep 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnrollerGroup.java#1 $
 */

public class TestActiveDirectoryEnrollerGroup extends BaseEnrollerSharedTestCase {
    private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
    private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "/test/ad.unittest.def";
    private static final String UNITTEST_CONN_FILE = ENROLL_DIR + "test/ad.unittest.conn";

    private static final String ROOT_DN = ActiveDirectoryTestHelper.generateRootDn("unittest groups");
    
    private static final ActiveDirectoryTestHelper AD_HELPER =
            new ActiveDirectoryTestHelper(ROOT_DN);

    private static final String USERNAME_TEMPLATE = "phoebe%03d-%02d";
    private static final String GROUPNAME_TEMPLATE = "group%04d";
    
    
    protected String getPasswordPropertyKey() {
        return ActiveDirectoryEnrollmentProperties.PASSWORD;
    }
    
    
    @Override
    protected boolean clearDatabaseBeforeStart() {
        return true;
    }


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
     * Enroll local domains
     * 
     * - Users
     *   - lark0001-0000
     *   - lark0002-0000
     *   - lark0002-0001
     * - Groups
     *   - group0000 
     *   - group0001 
     *     ~ lark0001-0000
     *     ~ group0000
     *     ~ TheOnlyOther
     *   - group0002 
     *     ~ lark0002-0000
     *     ~ lark0002-0001
     *     ~ group0000
     *     ~ group0001
     *     ~ TheOnlyOther
     *  - Others
     *    - TheOnlyOther
     *   
     */
    public void testAddUserIntoAD() throws LDAPException {
        AD_HELPER.createOus();

        LDAPAttributeSet attributeSet;
        LDAPEntry newEntry;
        
        String dn = null;
        try {
            List<String> otherAndGroupDns = new LinkedList<String>();
            
            //the other user with the exactly same name but in different ou
            attributeSet = setupLDAPUserAttributes("TheOnlyOther", 0);
            String format = "CN=%s," + AD_HELPER.getOthersRootDn();
            dn = String.format(format, "TheOnlyOther");
            newEntry = new LDAPEntry(dn, attributeSet);
            AD_HELPER.add(newEntry);
            otherAndGroupDns.add(dn);
            
            
            for (int groupCount = 0; groupCount < 3; groupCount++) {
                LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet();
                List<String> dns = new LinkedList<String>();
                for (int i = 0; i < groupCount; i++) {
                    String username = String.format(USERNAME_TEMPLATE, groupCount, i);
                    attributeSet = setupLDAPUserAttributes(username, i);
                    dn = "cn=" + username + "," + AD_HELPER.getUsersRootDn();

                    dns.add(dn);
                    newEntry = new LDAPEntry(dn, attributeSet);
                    AD_HELPER.add(newEntry);
                }

                if (!dns.isEmpty()) {
                    dns.addAll(otherAndGroupDns);
                    groupAttributeSet.add(new LDAPAttribute("member", dns.toArray(new String[] {})));
                }
                
                groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
                String groupname = String.format(GROUPNAME_TEMPLATE, groupCount);
                groupAttributeSet.add(new LDAPAttribute("name", groupname));
                dn = "cn=" + groupname + "," + AD_HELPER.getGroupsRootDn();
                otherAndGroupDns.add(dn);
                newEntry = new LDAPEntry(dn, groupAttributeSet);
                AD_HELPER.add(newEntry);
            }

            // alien!!!
            attributeSet = new LDAPAttributeSet();
            attributeSet.add(new LDAPAttribute("objectclass", new String[] { "top", "leaf",
                    "connectionPoint", "volume" }));
            attributeSet.add(new LDAPAttribute("cn", "SharedFolder1"));
            attributeSet.add(new LDAPAttribute("name", "name_SharedFolder1"));
            attributeSet.add(new LDAPAttribute("uNCname", "\\\\path\\folder"));

            String unknownDn = "CN=SharedFolder1," + AD_HELPER.getOthersRootDn();
            newEntry = new LDAPEntry(unknownDn, attributeSet);
            AD_HELPER.add(newEntry);

            
        } catch (LDAPException e) {
            System.out.println("current dn = " + dn);
            throw e;
        }
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
                new String[] { AD_HELPER.getUsersRootDn(), 
                AD_HELPER.getGroupsRootDn(), 
                AD_HELPER.getHostsRootDn() });
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
        
        dbCount.dictElements         += 9;      //3 groups, 3 users
        dbCount.dictEnumGroupMembers += 3;
        dbCount.dictEnumGroups       += 3; 
        dbCount.dictEnumMembers      += 3;
        dbCount.dictEnumRefMembers   += 2;
        dbCount.dictLeafElements     += 3;
        dbCount.dictStructGroups     += 3;
        dbCount.checkDatabaseCount();
    }
    
    private void sync() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());

        Date startTime = new Date();
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
        postCheck(startTime);
    }
    
    public void testSyncCp2() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
    
    public void testSyncCp3() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
    
    
    /**
     * Enroll local domains
     * 
     * - Users
     *   - lark0001-0000
     *   - lark0002-0000
     *   - lark0002-0001
     * - Groups
     *   - group0000 
     *     ~ group0002
     *   - group0001 
     *     ~ lark0001-0000
     *     ~ TheOnlyOther
     *   - group0002 
     *     ~ lark0002-0000
     *     ~ lark0002-0001
     *     ~ group0001
     *     ~ TheOnlyOther
     *  - Others
     *    - TheOnlyOther
     * @throws LDAPException 
     *   
     */
    public void testRemoveGroupMember() throws LDAPException{
        String group0 = "cn=" + String.format(GROUPNAME_TEMPLATE, 0) + "," + AD_HELPER.getGroupsRootDn();
        String group1 = "cn=" + String.format(GROUPNAME_TEMPLATE, 1) + "," + AD_HELPER.getGroupsRootDn();
        String group2 = "cn=" + String.format(GROUPNAME_TEMPLATE, 2) + "," + AD_HELPER.getGroupsRootDn();
        
        AD_HELPER.modify(group1, new LDAPModification(LDAPModification.DELETE, new LDAPAttribute("member", group0)));
        AD_HELPER.modify(group2, new LDAPModification(LDAPModification.DELETE, new LDAPAttribute("member", group0)));
        
        AD_HELPER.modify(group0, new LDAPModification(LDAPModification.ADD, new LDAPAttribute("member", group2)));
    }
    
    public void testSyncCp4() throws Exception {
        sync();
        dbCount.dictElements         += 3;      //3 group changed
        dbCount.dictEnumGroupMembers += 2;
        dbCount.dictEnumGroups       += 3; 
        dbCount.dictEnumMembers      += 0;
        dbCount.dictEnumRefMembers   += 0;
        dbCount.dictLeafElements     += 0;
        dbCount.dictStructGroups     += 0;
        dbCount.checkDatabaseCount();
    }
}
