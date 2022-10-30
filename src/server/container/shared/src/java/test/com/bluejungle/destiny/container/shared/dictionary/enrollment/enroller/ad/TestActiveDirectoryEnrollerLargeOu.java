/*
 * Created on May 14, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.utils.Pair;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnrollerLargeOu.java#1 $
 */

public class TestActiveDirectoryEnrollerLargeOu extends BaseEnrollerSharedTestCase {
	private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
	private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "/test/ad.unittest.def";
	private static final String UNITTEST_CONN_FILE = ENROLL_DIR + "test/ad.unittest.conn";

	private static final String ROOT_DN = "OU=unittest large,OU=horkan,"
		+ ActiveDirectoryTestHelper.BASE_DN;
	
	private static final ActiveDirectoryTestHelper AD_HELPER =
			new ActiveDirectoryTestHelper(ROOT_DN);

	private static final String USERNAME_PREFIX_TEMPLATE = "willet%04d-";
	private static final String USERNAME_TEMPLATE = USERNAME_PREFIX_TEMPLATE + "%04d";

	private static final String GROUPNAME_TEMPLATE = "group%04d";

	private static final String USER_DN_TEMPLATE = "CN=%s," + AD_HELPER.getUsersRootDn();
	private static final String GROUP_DN_TEMPLATE =
			"CN=" + GROUPNAME_TEMPLATE + "," + AD_HELPER.getGroupsRootDn();

	private static final int NUMBER_OF_GROUPS = 3;

	private static final int NUMBER_OF_STRUCTURE_GROUP = AD_HELPER.getAllOus().length;
	
	private static Map<String, Map<String, Pair<Date, Date>>> dnToTimeRelation =
			new HashMap<String, Map<String, Pair<Date, Date>>>();
	static {
		for (String dn : AD_HELPER.getAllOus()) {
			dnToTimeRelation.put(dn, new HashMap<String, Pair<Date, Date>>());
		}
	}

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

		//remove and create this large data could be slow. Try to reuse if possible.
//		AD_HELPER.removeAllUnitTestData();
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
	 *   - group0002 
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *    - lark0001-0000
	 * 
	 * @throws DictionaryException 
	 * @throws SQLException 
	 * @throws HibernateException 
	 * @throws Exception
	 */
	public void testAddUserIntoAD() throws LDAPException {
		String dn = null;
		
		//creat ou first
		AD_HELPER.createOus();
		
		try {
			for (int groupCount = 1; groupCount <= NUMBER_OF_GROUPS; groupCount++) {
				LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet();
				List<String> dns = new LinkedList<String>();
				for (int i = 0; i < groupCount * 1000; i++) {
					String username = String.format(USERNAME_TEMPLATE, groupCount, i);
					LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, i);
					dn = String.format(USER_DN_TEMPLATE, username);

					dns.add(dn);
                    if (!AD_HELPER.isExist(dn)) {
                        LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
                        AD_HELPER.add(newEntry);
                    }
				}

				if (!dns.isEmpty()) {
					groupAttributeSet
							.add(new LDAPAttribute("member", dns.toArray(new String[] {})));
				}
				groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
				dn = String.format(GROUP_DN_TEMPLATE, groupCount);
                if (!AD_HELPER.isExist(dn)) {
                    LDAPEntry newEntry = new LDAPEntry(dn, groupAttributeSet);
                    AD_HELPER.add(newEntry);
                }
			}
		} catch (LDAPException e) {
			System.out.println("current dn = " + dn);
			throw e;
		}
	}

	private int getNumberOfUserIn_OuGroups() {
		return (NUMBER_OF_GROUPS + 1) * (NUMBER_OF_GROUPS) / 2 * 1000;
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
		//		attributeSet.add(new LDAPAttribute("name", username));
		attributeSet.add(new LDAPAttribute("givenName", String.format("%s%04d", "given", i)));
		attributeSet.add(new LDAPAttribute("sn", String.format("%s%04d", "sn", i)));
		attributeSet.add(new LDAPAttribute("title", String.format("%s%02d", "given", i % 100)));
		attributeSet.add(new LDAPAttribute("company", "Nextlabs"));
		attributeSet.add(new LDAPAttribute("department", String.format("%s%01d", "given", i % 10)));
		attributeSet.add(new LDAPAttribute("co", "United State"));
		attributeSet.add(new LDAPAttribute("c", "US"));
		attributeSet.add(new LDAPAttribute("CountryCode", "0"));

		//random generated by Windows 2003 and not windows 2000
		//		attributeSet.add(new LDAPAttribute("SAMAccountName", username));
		return attributeSet;
	}

	private Map<String, String[]> getOverrideProperties() {
		Map<String, String[]> overrideProperties = encodePassword(ActiveDirectoryTestHelper.PASSWORD);
		//		overrideProperties.put(ActiveDirectoryEnrollmentProperties.DIRSYNC_ENABLED,
		//				new String[] { "true" });
		overrideProperties.put(ActiveDirectoryEnrollmentProperties.ROOTS,
				new String[] { ROOT_DN });
		return overrideProperties;
	}

	public void testSyncCp1() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(0, enrollments.size());

		dbCount.checkDatabaseCount();

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

		dbCount.dictElements += getNumberOfUserIn_OuGroups() 
				+ NUMBER_OF_GROUPS 
				+ NUMBER_OF_STRUCTURE_GROUP;
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += NUMBER_OF_GROUPS;
		dbCount.dictEnumMembers += getNumberOfUserIn_OuGroups();
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += getNumberOfUserIn_OuGroups();
		dbCount.dictStructGroups += NUMBER_OF_STRUCTURE_GROUP;
		dbCount.checkDatabaseCount();
	}

    private void sync() throws DictionaryException, InvalidConfigurationException,
            EntryNotFoundException, EnrollmentValidationException, EnrollmentSyncException,
            EnrollmentThreadException {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());
        
        enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
	}
    
    public void testsync2() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
    
    public void testsync3() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
    
    public void testsync4() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
    
    public void testsync5() throws Exception {
        sync();
        dbCount.checkDatabaseCount();
    }
	
	public void testLastTest() throws Exception {
		
		AD_HELPER.disconnect();
	}
}