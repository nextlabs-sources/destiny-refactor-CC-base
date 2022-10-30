/*
 * Created on Nov 5, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnrollerLargeGroup.java#1 $
 */

public class TestActiveDirectoryEnrollerLargeGroup extends BaseEnrollerSharedTestCase {
    private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
    private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "/test/ad.unittest.def";
    private static final String UNITTEST_CONN_FILE = ENROLL_DIR + "test/ad.unittest.conn";

    private static final String ROOT_DN = "OU=flat2 large group,OU=unit,DC=qapf1,DC=qalab01,DC=nextlabs,DC=com";
    
    private static final ActiveDirectoryTestHelper AD_HELPER =
            new ActiveDirectoryTestHelper("ts01.bluejungle.com", 33389, ROOT_DN, 
                    "DC=qapf1,DC=qalab01,DC=nextlabs,DC=com", "Administrator", "123blue!");

    private static final String MEMBER_GROUPNAME_TEMPLATE = "memberGroup%03d-%05d";
    private static final String GROUPNAME_TEMPLATE = "flatGroup%03d-%3d";
    
    protected String getPasswordPropertyKey() {
        return ActiveDirectoryEnrollmentProperties.PASSWORD;
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
    
    public void testAddUserIntoAD() throws LDAPException {
        final int numberOfGroupSet = 2;
        
        final int numberOfParent = 1;
        final int sizeOfGroup = 1000;
    
//        if (true) {
//            return;
//        }
        
        AD_HELPER.createOus();
        
        final LDAPAttributeSet emptySet = new LDAPAttributeSet();    
        emptySet.add(new LDAPAttribute("objectclass", "group"));

        LDAPEntry newEntry;
        String dn = null;
        try {
            Collection<LDAPEntry> groups = new ArrayList<LDAPEntry>(numberOfGroupSet * numberOfParent);
            for (int groupCount = 0; groupCount < numberOfGroupSet; groupCount++) {
                String[] dns = new String[sizeOfGroup];
                for (int i = 0; i < sizeOfGroup; i++) {
                    String memberGroupName =
                            String.format(MEMBER_GROUPNAME_TEMPLATE, groupCount, i);
                    dn = "cn=" + memberGroupName + "," + AD_HELPER.getGroupsRootDn();
                    dns[i] = dn;
                    LDAPAttributeSet attrSet = new LDAPAttributeSet();
                    attrSet.add(new LDAPAttribute("objectclass", "group"));
                    newEntry = new LDAPEntry(dn, attrSet);
                    AD_HELPER.add(newEntry);
                }
                
                for (int i = 0; i < numberOfParent; i++) {
                    String memberGroupName =
                            String.format(GROUPNAME_TEMPLATE, groupCount, i);
                    dn = "cn=" + memberGroupName + "," + AD_HELPER.getGroupsRootDn();
                    LDAPAttributeSet attrSet = new LDAPAttributeSet();
                    attrSet.add(new LDAPAttribute("objectclass", "group"));
                    attrSet.add(new LDAPAttribute("member", dns));
                    newEntry = new LDAPEntry(dn, attrSet);
//                    groups.add(newEntry);
                    AD_HELPER.add(newEntry);
                }
            }
            
            for(LDAPEntry e : groups){
              AD_HELPER.add(e);
          }
            
            
            
            
//            Collection<LDAPEntry> groups = new ArrayList<LDAPEntry>(numberOfGroup);
//            
//            for (int groupCount = 0; groupCount < numberOfGroup; groupCount++) {
////                String groupname = String.format(GROUPNAME_TEMPLATE, groupCount);
////                String groupDn = "cn=" + groupname + "," + AD_HELPER.getGroupsRootDn();
////                if (AD_HELPER.isExist(groupDn)) {
////                    continue;
////                }
//
//                String previousDn = null;
//                for (int i = 0; i < sizeOfGroup; i++) {
//                    String memberGroupName = String.format(MEMBER_GROUPNAME_TEMPLATE, groupCount, i);
//                    dn = "cn=" + memberGroupName + "," + AD_HELPER.getGroupsRootDn();
//                    LDAPAttributeSet attrSet = new LDAPAttributeSet();    
//                    attrSet.add(new LDAPAttribute("objectclass", "group"));
//                    if(previousDn!=null){
//                        attrSet.add(new LDAPAttribute("member", previousDn));
//                    }
//                    previousDn = dn;
//                    newEntry = new LDAPEntry(dn, attrSet);
//                    AD_HELPER.add(newEntry);
//                }
//                
////                LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet();
////                groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
////                groupAttributeSet.add(new LDAPAttribute("member", dns));
////                dn = groupDn;
////                newEntry = new LDAPEntry(dn, groupAttributeSet);
//////                AD_HELPER.add(newEntry);
////                groups.add(newEntry);
//            }
//            
//            for(LDAPEntry e : groups){
//                AD_HELPER.add(e);
//            }
        } catch (LDAPException e) {
            System.out.println("current dn = " + dn);
            throw e;
        }
    }
    
    
    private Map<String, String[]> getOverrideProperties() {
        Map<String, String[]> overrideProperties = encodePassword("123blue!");
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.ROOTS,
                new String[] { AD_HELPER.getUsersRootDn(), 
                AD_HELPER.getGroupsRootDn(), 
                AD_HELPER.getHostsRootDn() });
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.SERVER, new String[] { "ts01.bluejungle.com" });
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.PORT, new String[] { "33389" });
        overrideProperties.put(ActiveDirectoryEnrollmentProperties.LOGIN, new String[] { "Administrator" });
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
//        checkDatabaseCount(dictElements, dictEnumGroupMembers, dictEnumGroups, dictEnumMembers,
//                dictEnumRefMembers, dictLeafElements, dictStructGroups);
        
    }
}
