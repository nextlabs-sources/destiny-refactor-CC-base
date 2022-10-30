/*
 * Created on Mar 18, 2009
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

import javax.naming.NamingException;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.DictionaryTestHelper;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.utils.Pair;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;

/**
 * This test is focus on some basic action in a small ou.
 * Move a user, move ou... but one thing at a time.
 * More complicated test should go to <code>TestActiveDirectoryEnrollerSmallOuComplex</code>
 * 
 * Formatting idea
 * 1. always start from zero
 * 2. group number == # of members
 *    for example, group0219 should have 219 members
 * 3. user name format <name><group id>-<user id in this group>
 *    for example, lark0002-0001, is under the 3rd group (group0002) 
 *      and it is the second (0001) member in that group
 * 
 * 
 *  - On each active directory update, you will see a tree diagram 
 *  - I may touch up to group0002, that's why the tree only shows up to group0002
 *  - number of groups must be at leat 3, more is ok but going to slow down the test.
 * 
 * @see TestActiveDirectoryEnrollerSmallOuComplex
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryEnrollerSmallOuBasic.java#1 $
 */

public class TestActiveDirectoryEnrollerSmallOuBasic extends BaseEnrollerSharedTestCase {
	private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
	private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "/test/ad.unittest.def";
	private static final String UNITTEST_CONN_FILE = ENROLL_DIR + "test/ad.unittest.conn";

    private static final String ROOT_DN = ActiveDirectoryTestHelper.generateRootDn("unittest small");
	
	private static final ActiveDirectoryTestHelper AD_HELPER =
			new ActiveDirectoryTestHelper(ROOT_DN);
	
	
	
	private static final String USERNAME_PREFIX_TEMPLATE = "lark%04d-";
	private static final String USERNAME_TEMPLATE = USERNAME_PREFIX_TEMPLATE + "%04d";
	private static final String USER_DN_TEMPLATE = "CN=%s," + AD_HELPER.getUsersRootDn();

	private static final String GROUPNAME_TEMPLATE = "group%04d";
	private static final String GROUP_DN_TEMPLATE =
			"CN=" + GROUPNAME_TEMPLATE + "," + AD_HELPER.getGroupsRootDn();

	
	
	
	private static final int NUMBER_OF_GROUPS = 4; //change to any number you want but at least 3
	static{
		if (NUMBER_OF_GROUPS < 3) {
			throw new ExceptionInInitializerError("NUMBER_OF_GROUPS must be >= 3.");
		}
	}
	
//	private static final int NUMBER_OF_STRUCTURE_GROUP = AD_HELPER.getAllOus().length;
	
	private static final boolean IS_UNKNOWN_A_STRUCTURAL_GROUP = true;
	
	private static final int NUMBER_OF_STRUCTURE_GROUP =
			AD_HELPER.getAllOus().length + (IS_UNKNOWN_A_STRUCTURAL_GROUP ? 1 : 0);
	private static final int NUMBER_OF_OTHER_ENTRIES = 1;
	private static final int NUMBER_OF_UNKNOWN_ENTRIES = 1;
	
	
	
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
		
		AD_HELPER.removeAllUnitTestData();
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
			for (int groupCount = 0; groupCount < NUMBER_OF_GROUPS; groupCount++) {
				LDAPAttributeSet groupAttributeSet = new LDAPAttributeSet();
				List<String> dns = new LinkedList<String>();
				for (int i = 0; i < groupCount; i++) {
					String username = String.format(USERNAME_TEMPLATE, groupCount, i);
					LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, i);
					dn = String.format(USER_DN_TEMPLATE, username);

					dns.add(dn);
					LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
					AD_HELPER.add(newEntry);
				}

				if (!dns.isEmpty()) {
					groupAttributeSet
							.add(new LDAPAttribute("member", dns.toArray(new String[] {})));
				}
				groupAttributeSet.add(new LDAPAttribute("objectclass", "group"));
				LDAPEntry newEntry =
						new LDAPEntry(String.format(GROUP_DN_TEMPLATE, groupCount),
								groupAttributeSet);
				AD_HELPER.add(newEntry);
			}

			// alien!!!
			LDAPAttributeSet attributeSet = new LDAPAttributeSet();
			attributeSet.add(new LDAPAttribute("objectclass", new String[] { "top", "leaf",
					"connectionPoint", "volume" }));
			attributeSet.add(new LDAPAttribute("cn", "SharedFolder1"));
			attributeSet.add(new LDAPAttribute("name", "name_SharedFolder1"));
			attributeSet.add(new LDAPAttribute("uNCname", "\\\\path\\folder"));

			String unknownDn = "CN=SharedFolder1," + AD_HELPER.getOthersRootDn();
			LDAPEntry newEntry = new LDAPEntry(unknownDn, attributeSet);
			AD_HELPER.add(newEntry);

			//the other user with the exactly same name but in different ou
			String username = String.format(USERNAME_TEMPLATE, 1, 0);
			attributeSet = setupLDAPUserAttributes(username, 0);
			String format = "CN=%s," + AD_HELPER.getOthersRootDn();
			dn = String.format(format, username);
			newEntry = new LDAPEntry(dn, attributeSet);
			AD_HELPER.add(newEntry);
		} catch (LDAPException e) {
			System.out.println("current dn = " + dn);
			throw e;
		}
	}

	private int getNumberOfUserIn_OuGroups() {
		return (NUMBER_OF_GROUPS) * (NUMBER_OF_GROUPS - 1) / 2;
	}

	private boolean isUnderOuUsers(DictionaryPath path) {
		String[] paths = path.getPath();
		return paths[paths.length - 2].equalsIgnoreCase("OU=USERS");
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
		Map<String, String[]> overrideProperties = encodePassword(AD_HELPER.PASSWORD);
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
				+ NUMBER_OF_STRUCTURE_GROUP // + 4 structure groups 
				+ NUMBER_OF_OTHER_ENTRIES; // 1 member in ou=others
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += NUMBER_OF_GROUPS;
		dbCount.dictEnumMembers += getNumberOfUserIn_OuGroups();
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += getNumberOfUserIn_OuGroups() + NUMBER_OF_OTHER_ENTRIES;
		dbCount.dictStructGroups += NUMBER_OF_STRUCTURE_GROUP;
		dbCount.checkDatabaseCount();
	}

	private void sync() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Date startTime = new Date();
		enrollmentManager.enrollRealm(convert(UNITTEST_DOMAIN_NAME));
		postCheck(startTime);
	}

	/**
	 * check lark0001-0000, there are two user with the same name, 
	 *    one in OU=users, the other one in OU=others
	 * @throws Exception
	 */
	public void testCp1CheckUser_lark0001_0000() throws DictionaryException {
		String username = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);
		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				if (isUnderOuUsers(user.getPath())) {
					TimeRelation tr = DictionaryTestHelper.extractTimeRelation(user);
					assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
					assertTrue(tr.getActiveFrom().before(new Date()));

					dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).put(username,
							new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
				}
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}

		//the displayname really is the CN
		users =
				dictionaryQueryEquals(UserReservedFieldEnumType.DISPLAY_NAME, String.format(
						USERNAME_TEMPLATE, 1, 0));

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				assertNotNull(user);
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	/**
	 * check all user except lark0001-0000 because it is special duplicated user
	 * @throws Exception
	 */
	public void testCp1CheckUser_lark000x() throws DictionaryException {
		for (int groupIndex = 2; groupIndex < NUMBER_OF_GROUPS; groupIndex++) {
			for (int userIndex = 0; userIndex < groupIndex; userIndex++) {
				String username = String.format(USERNAME_TEMPLATE, groupIndex, userIndex);
				IDictionaryIterator<IMElement> users1 =
						dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);
				IElement user1;
				try {
					assertTrue(users1.hasNext());
					user1 = users1.next();
					TimeRelation tr = DictionaryTestHelper.extractTimeRelation(user1);
					assertEquals(username, UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
					assertTrue(username, tr.getActiveFrom().before(new Date()));
					dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).put(username,
							new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
					assertFalse(username, users1.hasNext());
				} finally {
					users1.close();
				}

				//the displayname really is the CN
				IDictionaryIterator<IMElement> users2 =
						dictionaryQueryEquals(UserReservedFieldEnumType.DISPLAY_NAME, username);

				try {
					assertTrue(username, users2.hasNext());
					IElement user2 = users2.next();
					assertNotNull(username, user2);
					assertFalse(username, users2.hasNext());
					assertEquals(username, user1, user2);
				} finally {
					users2.close();
				}
			}
		}
	}

	public void testCp1CheckGroup_group000x() throws DictionaryException {
		for (int groupIndex = 0; groupIndex < NUMBER_OF_GROUPS; groupIndex++) {
			String groupName = String.format(GROUP_DN_TEMPLATE, groupIndex);
			IMGroup group =
					dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupName),
							new Date());
			assertNotNull(group);
			TimeRelation tr = DictionaryTestHelper.extractTimeRelation(group);
			assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
			assertTrue(tr.getActiveFrom().before(new Date()));
			dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).put(groupName,
					new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
		}

		assertMemebership(0);
	}

	private void assertMemebership(int startFrom) throws DictionaryException {
		assertMemebership(startFrom, NUMBER_OF_GROUPS);
	}

	private void assertMemebership(int startFrom, int endAt) throws DictionaryException {
		for (int groupIndex = startFrom; groupIndex < endAt; groupIndex++) {
			String groupName = String.format(GROUP_DN_TEMPLATE, groupIndex);
			IMGroup group =
					dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupName),
							new Date());
			assertNotNull(groupName, group);

			IDictionaryIterator<IMElement> enumChildIterator = group.getAllChildElements();
			assertNotNull(enumChildIterator);
			try {
				int count = 0;
				while (enumChildIterator.hasNext()) {
					IElement child = enumChildIterator.next();
					assertTrue(child.getUniqueName().startsWith(
							String.format(USERNAME_PREFIX_TEMPLATE, groupIndex)));
					count++;
				}
				assertEquals(groupIndex, count);
			} finally {
				enumChildIterator.close();
			}
		}
	}

	/**
	 * check group AD_HELPER.getGroupsRootDn()
	 * @throws Exception
	 */
	public void testCp1CheckStructuralGroup_root() throws DictionaryException {
		IMGroup structGroup =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(AD_HELPER
						.getRootDn()), new Date());
		assertNotNull(structGroup);
		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		assertTrue(tr.getActiveFrom().before(new Date()));
		dnToTimeRelation.get(AD_HELPER.getRootDn()).put(AD_HELPER.getRootDn(),
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));

		IDictionaryIterator<IMElement> structChildIterator = structGroup.getAllChildElements();
		assertNotNull(structChildIterator);
		try {
			int count = 0;
			while (structChildIterator.hasNext()) {
				IMElement element = structChildIterator.next();
				assertNotNull(element);
				count++;
			}
			//users in ou=users
			int totalUsers = getNumberOfUserIn_OuGroups();

			//users in other;
			totalUsers++;

			//4 users
			// ou=others,cn=lark0001-0000, ou=users,cn=lark0002-0000, 
			// ou=users,cn=lark0002-0001, ou=users,cn=lark0001-0000
			assertEquals(totalUsers, count);
		} finally {
			structChildIterator.close();
		}

		IDictionaryIterator<IMGroup> structGroupIterator = structGroup.getAllChildGroups();
		assertNotNull(structGroupIterator);
		try {
			int count = 0;
			while (structGroupIterator.hasNext()) {
				IMGroup element = structGroupIterator.next();
				assertNotNull(element);
				count++;
			}
			//the root doesn't count
			assertEquals(NUMBER_OF_STRUCTURE_GROUP - 1, count);
		} finally {
			structGroupIterator.close();
		}
	}

	/**
	 * check group AD_HELPER.getGroupsRootDn()
	 * @throws Exception
	 */
	public void testCp1CheckStructuralGroup_groups() throws DictionaryException {
		IMGroup structGroup =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(AD_HELPER
						.getGroupsRootDn()), new Date());
		assertNotNull(structGroup);
		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		assertTrue(tr.getActiveFrom().before(new Date()));
		dnToTimeRelation.get(AD_HELPER.getRootDn()).put(AD_HELPER.getGroupsRootDn(),
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));

		IDictionaryIterator<IMElement> structChildIterator = structGroup.getAllChildElements();
		assertNotNull(structChildIterator);
		try {
			assertFalse(structChildIterator.hasNext());
		} finally {
			structChildIterator.close();
		}

		IDictionaryIterator<IMGroup> structGroupIterator = structGroup.getAllChildGroups();
		assertNotNull(structGroupIterator);
		try {
			assertFalse(structGroupIterator.hasNext());
		} finally {
			structGroupIterator.close();
		}
	}

	/**
	 * check group AD_HELPER.getGroupsRootDn()
	 * @throws Exception
	 */
	public void testCp1CheckStructuralGroup_users() throws DictionaryException {
		IMGroup structGroup =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(AD_HELPER
						.getUsersRootDn()), new Date());
		assertNotNull(structGroup);
		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		assertTrue(tr.getActiveFrom().before(new Date()));
		dnToTimeRelation.get(AD_HELPER.getRootDn()).put(AD_HELPER.getUsersRootDn(),
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));

		IDictionaryIterator<IMElement> structChildIterator = structGroup.getAllChildElements();
		assertNotNull(structChildIterator);
		try {
			int count = 0;
			while (structChildIterator.hasNext()) {
				IMElement element = structChildIterator.next();
				assertNotNull(element);
				count++;
			}

			//users in ou=users
			int totalUsers = getNumberOfUserIn_OuGroups();
			//3 users
			// ou=users,cn=lark0002-0000, ou=users,cn=lark0002-0001, ou=users,cn=lark0001-0000
			assertEquals(totalUsers, count);
		} finally {
			structChildIterator.close();
		}

		IDictionaryIterator<IMGroup> structGroupIterator = structGroup.getAllChildGroups();
		assertNotNull(structGroupIterator);
		try {
			assertFalse(structGroupIterator.hasNext());
		} finally {
			structGroupIterator.close();
		}
	}

	/**
	 * check group AD_HELPER.getGroupsRootDn()
	 * @throws Exception
	 */
	public void testCp1CheckStructuralGroup_others() throws DictionaryException {
		IMGroup structGroup =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(AD_HELPER
						.getOthersRootDn()), new Date());
		assertNotNull(structGroup);
		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		assertTrue(tr.getActiveFrom().before(new Date()));
		dnToTimeRelation.get(AD_HELPER.getRootDn()).put(AD_HELPER.getOthersRootDn(),
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));

		IDictionaryIterator<IMElement> structChildIterator = structGroup.getAllChildElements();
		assertNotNull(structChildIterator);
		try {
			assertTrue(structChildIterator.hasNext());
			IMElement element = structChildIterator.next();
			assertNotNull(element);
			assertFalse(structChildIterator.hasNext());
		} finally {
			structChildIterator.close();
		}

		IDictionaryIterator<IMGroup> structGroupIterator = structGroup.getAllChildGroups();
		assertNotNull(structGroupIterator);
		try {
			assertEquals(IS_UNKNOWN_A_STRUCTURAL_GROUP, structGroupIterator.hasNext());
		} finally {
			structGroupIterator.close();
		}
	}
	
	public void testCp1CheckStructuralGroup_hosts() throws DictionaryException {
		IMGroup structGroup =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(AD_HELPER
						.getHostsRootDn()), new Date());
		assertNotNull(structGroup);
		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		assertTrue(tr.getActiveFrom().before(new Date()));
		dnToTimeRelation.get(AD_HELPER.getRootDn()).put(AD_HELPER.getHostsRootDn(),
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));

		IDictionaryIterator<IMElement> structChildIterator = structGroup.getAllChildElements();
		assertNotNull(structChildIterator);
		try {
			assertFalse(structChildIterator.hasNext());
		} finally {
			structChildIterator.close();
		}

		IDictionaryIterator<IMGroup> structGroupIterator = structGroup.getAllChildGroups();
		assertNotNull(structGroupIterator);
		try {
			assertFalse(structGroupIterator.hasNext());
		} finally {
			structGroupIterator.close();
		}
	}
	
	public void testCheckOtherUser(){
		
	}

	
	
	
	
	
	/**
	 * sync again without any changes
	 * @throws Exception
	 */
	public void testSyncCp2() throws Exception {
		sync();

		//no changes
		dbCount.checkDatabaseCount();
	}

	public void testCp2CheckUserTime_lark0001_0000() throws DictionaryException {
		String username = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				TimeRelation userTr = DictionaryTestHelper.extractTimeRelation(user);
				if (isUnderOuUsers(user.getPath())) {
					Pair<Date, Date> date =
							dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).get(username);
					assertNotNull(date);
					assertEquals(date.first(), userTr.getActiveFrom());
					assertEquals(date.second(), userTr.getActiveTo());
				}
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	public void testCp2CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(2);
	}

	private void assertUsersTimeNoChange(int startFromGroup) throws DictionaryException {
		assertUsersTimeNoChange(startFromGroup, NUMBER_OF_GROUPS);
	}

	private void assertUsersTimeNoChange(int startFromGroup, int endGroup)
			throws DictionaryException {
		for (int groupIndex = startFromGroup; groupIndex < endGroup; groupIndex++) {
			for (int userIndex = 0; userIndex < groupIndex; userIndex++) {
				String username = String.format(USERNAME_TEMPLATE, groupIndex, userIndex);
				IDictionaryIterator<IMElement> users =
						dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);
				try {
					assertTrue(username + " doesn't exist", users.hasNext());
					IElement user = users.next();
					TimeRelation tr = DictionaryTestHelper.extractTimeRelation(user);
					assertFalse(username + " has more than one", users.hasNext());
					Pair<Date, Date> date =
							dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).get(username);
					assertNotNull(username, date);
					assertEquals(username + "'s activeFrom is changed", date.first(), tr
							.getActiveFrom());
					assertEquals(username + "'s activeTo is changed", date.second(), tr
							.getActiveTo());
				} finally {
					users.close();
				}
			}
		}
	}

	public void testCp2CheckGroupTime000x() throws DictionaryException {
		assertGroupsTimeNoChange(2);
	}

	private void assertGroupsTimeNoChange(int startFrom) throws DictionaryException {
		assertGroupsTimeNoChange(startFrom, NUMBER_OF_GROUPS);
	}

	private void assertGroupsTimeNoChange(int startIndex, int endIndex) throws DictionaryException {
		for (int groupIndex = startIndex; groupIndex < endIndex; groupIndex++) {
			String groupName = String.format(GROUP_DN_TEMPLATE, groupIndex);
			IMGroup enumGroup =
					dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupName),
							new Date());
			assertNotNull(enumGroup);
			TimeRelation tr = DictionaryTestHelper.extractTimeRelation(enumGroup);

			Pair<Date, Date> date =
					dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).get(groupName);
			assertNotNull(groupName, date);
			assertEquals(groupName, date.first(), tr.getActiveFrom());
			assertEquals(groupName, date.second(), tr.getActiveTo());
		}
	}

	public void testCp2CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}

	private void assertStructGroupsTimeNoChange() throws DictionaryException {
		assertStructGroupsTimeNoChange(AD_HELPER.getAllOus());
	}

	private void assertStructGroupsTimeNoChange(String... dns) throws DictionaryException {
		for (String dn : dns) {
			IMGroup structGroup =
					dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn),
							new Date());
			assertNotNull(structGroup);
			TimeRelation tr = DictionaryTestHelper.extractTimeRelation(structGroup);

			Pair<Date, Date> date = dnToTimeRelation.get(AD_HELPER.getRootDn()).get(dn);
			assertNotNull(dn, date);
			assertEquals(dn + ", " + date.first() + " vs " + tr.getActiveFrom(), 
					date.first(), tr.getActiveFrom());
			assertEquals(dn + ", " + date.second() + " vs " + tr.getActiveTo(), 
					date.second(), tr.getActiveTo());
		}
	}

	
	
	
	
	/**
	 * delete and re-create CN=lark0001-0000,OU=Users
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000 (different guid)
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001 (empty group now)
	 *   - group0002 
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *    - lark0001-0000
	 *    
	 * @throws Exception
	 */
	public void testRecreateUser() throws LDAPException {
		String username = String.format(USERNAME_TEMPLATE, 1, 0);

		String format = "CN=%s," + AD_HELPER.getUsersRootDn();
		String dn = String.format(format, username);
		AD_HELPER.delete(dn);

		LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, 0);
		LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
		AD_HELPER.add(newEntry);
	}

	/**
	 * sync again afer re-create
	 * @throws Exception
	 */
	public void testSyncCp3() throws Exception {
		sync();

		dbCount.dictElements += 2; // new one group, one user
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; // new group
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // new user
		dbCount.dictStructGroups += 0;
		dbCount.checkDatabaseCount();
	}

	/**
	 * check group0001 which used to have one member but now is empty.
	 * The member lark0001-0000 was there but since it was deleted. The membership relation is gone. 
	 * @throws Exception
	 */
	public void testCp3CheckGroup0001() throws DictionaryException {
		String groupname = String.format(GROUP_DN_TEMPLATE, 1);
		IMGroup enumGroup1 =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupname),
						new Date());
		assertNotNull(enumGroup1);

		IDictionaryIterator<IMElement> enumChildIterator = enumGroup1.getAllChildElements();
		assertNotNull(enumChildIterator);
		try {
			assertFalse(enumChildIterator.hasNext());
		} finally {
			enumChildIterator.close();
		}

		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(enumGroup1);
		Pair<Date, Date> date = dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).get(groupname);

		assertTrue(tr.getActiveFrom().after(date.first()));
		assertTrue(tr.getActiveFrom().before(new Date()));
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).put(groupname,
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
	}

	/**
	 * user lark0001-0000 is still there, and there two lark0001-0000, one in OU=users, one in OU=others
	 * @throws Exception
	 */
	public void testCp3CheckUser_lark0001_0000() throws DictionaryException {
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, String.format(
						USERNAME_TEMPLATE, 1, 0));
		try {
			int count = 0;
			while (users.hasNext()) {
				users.next();
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	public void testCp3CheckUserTime_lark0001_0000() throws DictionaryException {
		String username = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				TimeRelation userTr = DictionaryTestHelper.extractTimeRelation(user);
				if (isUnderOuUsers(user.getPath())) {
					Pair<Date, Date> date =
							dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).get(username);

					assertTrue(userTr.getActiveFrom().after(date.first()));
					assertTrue(userTr.getActiveFrom().before(new Date()));
					assertEquals(UnmodifiableDate.END_OF_TIME, userTr.getActiveTo());
					dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).put(username,
							new Pair<Date, Date>(userTr.getActiveFrom(), userTr.getActiveTo()));
				}
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	public void testCp3CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(2);
	}

	public void testCp3CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 1);
		assertGroupsTimeNoChange(2);
	}

	public void testCp3CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}

	
	
	
	
	/**
	 * delete and re-create CN=lark0001-0000,OU=Users
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000 (different guid)
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
	 * @throws Exception
	 */
	public void testRecreateUserWithMembership() throws LDAPException {
		String groupDN = String.format(GROUP_DN_TEMPLATE, 1);

		String username = String.format(USERNAME_TEMPLATE, 1, 0);

		String userDN = String.format(USER_DN_TEMPLATE, username);
		AD_HELPER.delete(userDN);

		LDAPAttributeSet attributeSet = setupLDAPUserAttributes(username, 0);
		LDAPEntry newEntry = new LDAPEntry(userDN, attributeSet);
		AD_HELPER.add(newEntry);

		LDAPAttribute membership = new LDAPAttribute("member", new String[] { userDN });
		LDAPModification mod = new LDAPModification(LDAPModification.REPLACE, membership);

		AD_HELPER.modify(groupDN, mod);
	}

	public void testSyncCp4() throws Exception {
		sync();

		dbCount.dictElements += 2; // new one group, one user
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; // new group
		dbCount.dictEnumMembers += 1; // new membership
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // new user
		dbCount.dictStructGroups += 0;
		dbCount.checkDatabaseCount();
	}

	/**
	 * check group0001 which has lark0001-0000 (different that the original lark0001-0000 
	 * @throws Exception
	 */
	public void testCp4CheckGroup1() throws DictionaryException {
		IMGroup enumGroup1 =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(String.format(
						GROUP_DN_TEMPLATE, 1)), new Date());
		assertNotNull(enumGroup1);

		String user1PrincipalName = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> enumChildIterator = enumGroup1.getAllChildElements();
		assertNotNull(enumChildIterator);
		try {
			int count = 0;
			while (enumChildIterator.hasNext()) {
				IElement child = enumChildIterator.next();
				assertEquals(user1PrincipalName, child.getUniqueName());
				count++;
			}
			assertEquals(1, count);
		} finally {
			enumChildIterator.close();
		}
	}

	/**
	 * user lark0001-0000 is still there, and there two lark0001-0000, one in OU=users, one in OU=others
	 * @throws Exception
	 */
	public void testCp4CheckUser() throws DictionaryException {
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, String.format(
						USERNAME_TEMPLATE, 1, 0));
		try {
			int count = 0;
			while (users.hasNext()) {
				users.next();
				count++;
			}
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	public void testCp4CheckUserTime_lark0001_0000() throws DictionaryException {
		String username = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				TimeRelation userTr = DictionaryTestHelper.extractTimeRelation(user);
				if (isUnderOuUsers(user.getPath())) {
					Pair<Date, Date> date =
							dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).get(username);

					assertTrue(userTr.getActiveFrom().after(date.first()));
					assertTrue(userTr.getActiveFrom().before(new Date()));
					assertEquals(UnmodifiableDate.END_OF_TIME, userTr.getActiveTo());
					dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).put(username,
							new Pair<Date, Date>(userTr.getActiveFrom(), userTr.getActiveTo()));
				}
				count++;
			}
			//the other one will same upn is under "ou=others"
			assertEquals(2, count);
		} finally {
			users.close();
		}
	}

	public void testCp4CheckGroup0001() throws DictionaryException {
		String groupname = String.format(GROUP_DN_TEMPLATE, 1);
		IMGroup enumGroup1 =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupname),
						new Date());
		assertNotNull(enumGroup1);

		IDictionaryIterator<IMElement> enumChildIterator = enumGroup1.getAllChildElements();
		assertNotNull(enumChildIterator);
		try {
			assertTrue(enumChildIterator.hasNext());
			IMElement element = enumChildIterator.next();
			assertNotNull(element);
			assertTrue(element.getUniqueName().startsWith(
					String.format(USERNAME_PREFIX_TEMPLATE, 1)));
			assertFalse(enumChildIterator.hasNext());
		} finally {
			enumChildIterator.close();
		}

		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(enumGroup1);
		Pair<Date, Date> date = dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).get(groupname);

		assertTrue(tr.getActiveFrom().after(date.first()));
		assertTrue(tr.getActiveFrom().before(new Date()));
		assertEquals(UnmodifiableDate.END_OF_TIME, tr.getActiveTo());
		dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).put(groupname,
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
	}

	public void testCp4CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(2);
	}

	public void testCp4CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 1);
		assertGroupsTimeNoChange(2);
	}

	public void testCp4CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}

	
	

	
	/**
	 * rename user
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 
	 *     ~ lark0002-0000
	 *     ~ Lark, Horned (renamed)
	 *  - Others 
	 *    - Lark, Horned (renamed from lark0002-0001)
	 *    
	 *    
	 * @throws NamingException 
	 * @throws Exception
	 */
	public void testMoveUserAndDeleteExtraCp4() throws LDAPException, NamingException {
		String username = String.format(USERNAME_TEMPLATE, 2, 1);
		String oldDn = String.format(USER_DN_TEMPLATE, username);

		AD_HELPER.move(oldDn, "CN=Lark\\, Horned," + AD_HELPER.getOthersRootDn());

		AD_HELPER.delete("CN=" + String.format(USERNAME_TEMPLATE, 1, 0) + ","
				+ AD_HELPER.getOthersRootDn());
	}

	public void testSyncCp5() throws Exception {
		sync();

		dbCount.dictElements += 2; // updated one user
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; // updated one group
		dbCount.dictEnumMembers += 1; // that updated group has one user
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // updated user
		dbCount.dictStructGroups += 0;
		dbCount.checkDatabaseCount();
	}

	public void testCheckCp5Group0002() throws DictionaryException {
		String groupname = String.format(GROUP_DN_TEMPLATE, 2);
		IMGroup enumGroup1 =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupname),
						new Date());
		assertNotNull(enumGroup1);

		IDictionaryIterator<IMElement> enumChildIterator = enumGroup1.getDirectChildElements();
		assertNotNull(enumChildIterator);
		try {
			int count = 0;
			while (enumChildIterator.hasNext()) {
				IMElement element = enumChildIterator.next();
				assertNotNull(element);
				String displayName = element.getDisplayName();
				assertTrue(displayName, displayName.equalsIgnoreCase("Lark, Horned")
						|| displayName.equalsIgnoreCase(String.format(USERNAME_TEMPLATE, 2, 0)));
				count++;
			}

			assertEquals(2, count);
		} finally {
			enumChildIterator.close();
		}

		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(enumGroup1);
		dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).put(groupname,
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
	}

	public void testCp5CheckMembership() throws DictionaryException {
		assertMemebership(0, 2);
		assertMemebership(3);
	}

	public void testCp5CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(0, 2);
		assertUsersTimeNoChange(3);

	}

	public void testCp5CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 2);
		assertGroupsTimeNoChange(3);
	}

	public void testCp5CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}

	public void testCp5CheckOtherUser() throws DictionaryException{
		String username = String.format(USERNAME_TEMPLATE, 1, 0);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);
		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				assertTrue(isUnderOuUsers(user.getPath()));
				count++;
			}
			assertEquals(1, count);
		} finally {
			users.close();
		}

		//the displayname really is the CN
		users =
				dictionaryQueryEquals(UserReservedFieldEnumType.DISPLAY_NAME, String.format(
						USERNAME_TEMPLATE, 1, 0));

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				assertNotNull(user);
				count++;
			}
			assertEquals(1, count);
		} finally {
			users.close();
		}
	}
	
	
	
	
	/**
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 
	 *     ~ lark0002-0000
	 *     ~ Lark, Horned (renamed)
	 *  - Others 
	 *    - Lark, Horned (renamed from lark0002-0001)
	 * 
	 * sync again without any changes
	 * @throws Exception
	 */
	public void testSyncCp6() throws Exception {
		sync();

		dbCount.checkDatabaseCount();
	}

	public void testCp6CheckMembership() throws DictionaryException {
		assertMemebership(0, 2);
		assertMemebership(3);
	}

	public void testCp6CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(0, 2);
		assertUsersTimeNoChange(3);

	}

	public void testCp6CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 2);
		assertGroupsTimeNoChange(3);
	}

	public void testCp6CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}

	
	
	
	
	/**
	 * rename the user name back to lark0002-0001
	 * rename the gorup0002 to groupLazuli
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001 (rename back)
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - groupLazuli (renamed)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001 (rename back)
	 *  - Others  (empty)
	 * @throws NamingException 
	 *  
	 *    
	 * @throws Exception
	 */
	public void testMoveUserAndGroup() throws NamingException {
		String username = String.format(USERNAME_TEMPLATE, 2, 1);
		String orginalDn = String.format(USER_DN_TEMPLATE, username);
		AD_HELPER.move("CN=Lark\\, Horned," + AD_HELPER.getOthersRootDn(), orginalDn);

		String oldDn = String.format(GROUP_DN_TEMPLATE, 2);
		AD_HELPER.move(oldDn, "CN=groupLazuli," + AD_HELPER.getGroupsRootDn());
	}

	public void testSyncCp7() throws Exception {
		sync();

		dbCount.dictElements += 2; // updated one user, one group
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; // new group
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // updated user
		dbCount.dictStructGroups += 0;
		dbCount.checkDatabaseCount();
	}
	
	public void testCp7CheckUserTime_lark0001_0000() throws DictionaryException {
		String username = String.format(USERNAME_TEMPLATE, 2, 1);
		IDictionaryIterator<IMElement> users =
				dictionaryQueryEquals(UserReservedFieldEnumType.PRINCIPAL_NAME, username);

		try {
			int count = 0;
			while (users.hasNext()) {
				IElement user = users.next();
				TimeRelation userTr = DictionaryTestHelper.extractTimeRelation(user);
				if (isUnderOuUsers(user.getPath())) {
					Pair<Date, Date> date =
							dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).get(username);

					assertTrue(userTr.getActiveFrom().after(date.first()));
					assertTrue(userTr.getActiveFrom().before(new Date()));
					assertEquals(UnmodifiableDate.END_OF_TIME, userTr.getActiveTo());
					dnToTimeRelation.get(AD_HELPER.getUsersRootDn()).put(username,
							new Pair<Date, Date>(userTr.getActiveFrom(), userTr.getActiveTo()));
				}
				count++;
			}
			assertEquals(1, count);
		} finally {
			users.close();
		}
	}

	public void testCp7Checkmemebership() throws DictionaryException {
		String groupName = "CN=groupLazuli," + AD_HELPER.getGroupsRootDn();
		int groupIndex = 2;

		IMGroup group =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupName),
						new Date());
		assertNotNull(groupName, group);

		IDictionaryIterator<IMElement> enumChildIterator = group.getAllChildElements();
		assertNotNull(enumChildIterator);
		try {
			int count = 0;
			while (enumChildIterator.hasNext()) {
				IElement child = enumChildIterator.next();
				assertTrue(child.getUniqueName().startsWith(
						String.format(USERNAME_PREFIX_TEMPLATE, groupIndex)));
				count++;
			}
			assertEquals(groupIndex, count);
		} finally {
			enumChildIterator.close();
		}
	}

	public void testCp7CheckMembership() throws DictionaryException {
		assertMemebership(0, 2);
		assertMemebership(3);
	}

	public void testCp7CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(0, 2);
		assertUsersTimeNoChange(3);
	}

	public void testCp7CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 2);
		assertGroupsTimeNoChange(3);
	}

	public void testCp7CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	private static String MAGICIAN_USERNAME = "Magician";
	
	
	
	
	
	/**
	 * add a new user but it doesn't meet the user requirement 
	 * fix groupLazuli
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (new, structural group)
	 */
	public void testAddAUserButDoesntMeetUserRequirement() throws LDAPException, NamingException{
		String oldDn = String.format(GROUP_DN_TEMPLATE, 2);
		AD_HELPER.move("CN=groupLazuli," + AD_HELPER.getGroupsRootDn(), oldDn);
		
		LDAPAttributeSet attributeSet = setupLDAPUserAttributes(MAGICIAN_USERNAME, 0);
		attributeSet.remove("userPrincipalName");
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		LDAPEntry newEntry = new LDAPEntry(dn, attributeSet);
		AD_HELPER.add(newEntry);
	}
	
	public void testSyncCp8() throws Exception {
		sync();

		dbCount.dictElements += 2; // new structural group (actually it is a user) and one group
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; //renmaed one group
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 0; //no new leaf element since the new user is misplaced as structural group
		dbCount.dictStructGroups += 1; // new false structural group
		dbCount.checkDatabaseCount();
	}
	
	public void testCp8CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp8CheckUserTime_lark000x() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}
	
	public void testCheckCp8Group0002() throws DictionaryException {
		String groupname = String.format(GROUP_DN_TEMPLATE, 2);
		IMGroup enumGroup1 =
				dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(groupname),
						new Date());
		assertNotNull(enumGroup1);

		IDictionaryIterator<IMElement> enumChildIterator = enumGroup1.getDirectChildElements();
		assertNotNull(enumChildIterator);
		try {
			int count = 0;
			while (enumChildIterator.hasNext()) {
				IMElement element = enumChildIterator.next();
				assertNotNull(element);
				String displayName = element.getDisplayName();
				assertTrue(displayName, 
						displayName.equalsIgnoreCase(String.format(USERNAME_TEMPLATE, 2, 0))
						|| displayName.equalsIgnoreCase(String.format(USERNAME_TEMPLATE, 2, 1)));
				count++;
			}

			assertEquals(2, count);
		} finally {
			enumChildIterator.close();
		}

		TimeRelation tr = DictionaryTestHelper.extractTimeRelation(enumGroup1);
		dnToTimeRelation.get(AD_HELPER.getGroupsRootDn()).put(groupname,
				new Pair<Date, Date>(tr.getActiveFrom(), tr.getActiveTo()));
	}

	public void testCp8CheckGroup000xTime() throws DictionaryException {
		assertGroupsTimeNoChange(0, 2);
		assertGroupsTimeNoChange(3);
	}

	public void testCp8CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp8CheckOtherAsStructGroup() throws DictionaryException {
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		IMGroup structGroup  = dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
		assertNotNull(structGroup);
		assertEquals(structGroup.getType(), IElementType.STRUCT_GROUP_TYPE);
		
		IMGroup group = dictionary.getGroup(MAGICIAN_USERNAME, new Date());
		assertNull(group);
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNull(element);
	}
	
	
	
	
	
	/**
	 * the tree doesn't change but add userPrincipalName to missedUser1
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (attr is modified, user)
	 */
	public void testFromStructGroupToUser() throws LDAPException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		LDAPModification mod = new LDAPModification(LDAPModification.ADD, 
				new LDAPAttribute("userPrincipalName", MAGICIAN_USERNAME));
		AD_HELPER.modify(dn, mod);
	}
	
	public void testSyncCp9() throws Exception {
		sync();

		dbCount.dictElements += 1; // new user (fixed)
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 0;
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // new user
		dbCount.dictStructGroups += 0; 
		dbCount.checkDatabaseCount();
	}
	
	public void testCp9CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp9CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp9CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp9CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp9CheckMagicianAsUser() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		try {
			dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
			fail("should fail since path matched a user");
		} catch (IllegalArgumentException e1) {
			assertNotNull(e1);
		}
		
		try {
			dictionary.getGroup(MAGICIAN_USERNAME, new Date());
			fail("there should be such thing but is not a group, it is an element");
		} catch (DictionaryException e) {
			assertNotNull(e);
		}
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNotNull(element);
		assertEquals(element.getType().getName(), ElementTypeEnumType.USER.getName());
	}
	
	
	
	
	
	/**
	 * the tree doesn't change
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (attr is modified, structural group)
	 */
	public void testUserToStructGroup() throws LDAPException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		LDAPModification mod = new LDAPModification(LDAPModification.DELETE, 
				new LDAPAttribute("userPrincipalName", MAGICIAN_USERNAME));
		AD_HELPER.modify(dn, mod);
	}
	
	public void testSyncCp10() throws Exception {
		sync();

		dbCount.dictElements += 1; // new structural group
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 0;
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 0; 
		dbCount.dictStructGroups += 1; // misclassified structural group again
		dbCount.checkDatabaseCount();
	}
	
	public void testCp10CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp10CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp10CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp10CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp10CheckMagicianAsStructGroup() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		IMGroup structGroup  = dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
		assertNotNull(structGroup);
		assertEquals(structGroup.getType(), IElementType.STRUCT_GROUP_TYPE);
		
		IMGroup group = dictionary.getGroup(MAGICIAN_USERNAME, new Date());
		assertNull(group);
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNull(element);
	}
	
	
	
	
	
	/**
	 * the tree doesn't change
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (user but look like a contact)
	 * @throws LDAPException 
	 */
	public void testStructurGroupToContact() throws LDAPException {
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		LDAPModification mod = new LDAPModification(LDAPModification.ADD, 
				new LDAPAttribute("userPrincipalName", MAGICIAN_USERNAME));
		AD_HELPER.modify(dn, mod);
	}
		
		
	public void testSyncCp11() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Map<String, String[]> props = getOverrideProperties();
		
		props.put(BasicLDAPEnrollmentProperties.USER_REQUIREMENTS, new String[] { 
				"(&(userPrincipalName=*)(!(userPrincipalName="+MAGICIAN_USERNAME+")))" });
		props.put(BasicLDAPEnrollmentProperties.CONTACT_REQUIREMENTS, new String[] { 
				"(|(objectClass=contact)(userPrincipalName="+MAGICIAN_USERNAME+"))" });
		
		props.put(BasicLDAPEnrollmentProperties.CONTACT_SEARCHABLE_PREFIX + "string." 
				+ ContactReservedFieldEnumType.PRINCIPAL_NAME.getName() , new String[] { 
				"userPrincipalName" });

		Date startTime = new Date();
		IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                props, 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        enrollmentManager.updateRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));

        postCheck(startTime);
		
        dbCount.dictElements += 1; // new user
        dbCount.dictEnumGroupMembers += 0;
        dbCount.dictEnumGroups += 0;
        dbCount.dictEnumMembers += 0;
        dbCount.dictEnumRefMembers += 0;
        dbCount.dictLeafElements += 1; // new user
        dbCount.dictStructGroups += 0; 
        dbCount.checkDatabaseCount();
	}
	
	public void testCp11CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp11CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp11CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp11CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp11CheckMagicianAsContact() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		try {
			dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
			fail("should fail since path matched a user");
		} catch (IllegalArgumentException e1) {
			assertNotNull(e1);
		}
		
		try {
			dictionary.getGroup(MAGICIAN_USERNAME, new Date());
			fail("there should be such thing but is not a group, it is an element");
		} catch (DictionaryException e) {
			assertNotNull(e);
		}
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNotNull(element);
		assertEquals(element.getType().getName(), ElementTypeEnumType.CONTACT.getName());
	}
	
	
	
	
	
	/**
	 * the tree doesn't change but change the definition
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (user)
	 */
	public void testSyncCp12FromContactToUser() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Map<String, String[]> props = getOverrideProperties();
		
		Date startTime = new Date();
		IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                props, 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        enrollmentManager.updateRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
		postCheck(startTime);
		
		dbCount.dictElements += 1; // new user
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 0;
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; // new user
		dbCount.dictStructGroups += 0; 
		dbCount.checkDatabaseCount();
	}
	
	public void testCp12CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp12CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp12CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp12CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp12CheckMagicianAsUser() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		try {
			dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
			fail("should fail since path matched a user");
		} catch (IllegalArgumentException e1) {
			assertNotNull(e1);
		}
		
		try {
			dictionary.getGroup(MAGICIAN_USERNAME, new Date());
			fail("there should be such thing but is not a group, it is an element");
		} catch (DictionaryException e) {
			assertNotNull(e);
		}
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNotNull(element);
		assertEquals(element.getType().getName(), ElementTypeEnumType.USER.getName());
	}
	
	
	
	
	
	/**
	 * the tree doesn't change
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (user but look like an enum group)
	 */
	public void testSyncCp13FromUserToEnumGroup() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Map<String, String[]> props = getOverrideProperties();
		
		props.put(BasicLDAPEnrollmentProperties.USER_REQUIREMENTS, new String[] { 
				"(&(userPrincipalName=*)(!(userPrincipalName="+MAGICIAN_USERNAME+")))" });
		props.put(BasicLDAPEnrollmentProperties.GROUP_REQUIREMENTS, new String[] { 
				"(|(objectClass=group)(userPrincipalName="+MAGICIAN_USERNAME+"))" });
		
		Date startTime = new Date();
		IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                props, 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        enrollmentManager.updateRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
		postCheck(startTime);
		
		dbCount.dictElements += 1; // new group
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; //new misclassified group
		dbCount.dictEnumMembers += 0; //no new memebers since it doesn't have member attribute
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 0;
		dbCount.dictStructGroups += 0; 
		dbCount.checkDatabaseCount();
	}
	
	public void testCp13CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp13CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp13CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp13CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp13CheckMagicianAsEnumGroup() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		
		IMGroup group = dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
		assertNotNull(group);
		assertEquals(group.getType().getName(), IElementType.ENUM_GROUP_TYPE.getName());
			
		IMGroup group2 = dictionary.getGroup(MAGICIAN_USERNAME, new Date());
		assertNull(group2);
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNull(element);
	}
	
	
	
	
	
	/**
	 * the tree doesn't change
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (user, back to normal)
	 */
	public void testSyncCp14FromEnumGroupBackToUser() throws Exception {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Map<String, String[]> props = getOverrideProperties();
		
		Date startTime = new Date();
		IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                props, 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        enrollmentManager.updateRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
		postCheck(startTime);
		
		dbCount.dictElements += 1; // new user
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 0; 
		dbCount.dictEnumMembers += 0;
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 1; //new user
		dbCount.dictStructGroups += 0; 
		dbCount.checkDatabaseCount();
	}
	
	public void testCp14CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp14CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp14CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp14CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp14CheckMagicianAsUser() throws DictionaryException{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		try {
			dictionary.getGroup(LDAPEnrollmentHelper.getDictionaryPathFromDN(dn), new Date());
			fail("should fail since path matched a user");
		} catch (IllegalArgumentException e1) {
			assertNotNull(e1);
		}
		
		try {
			dictionary.getGroup(MAGICIAN_USERNAME, new Date());
			fail("there should be such thing but is not a group, it is an element");
		} catch (DictionaryException e) {
			assertNotNull(e);
		}
		
		IMElement element = dictionary.getElement(MAGICIAN_USERNAME, new Date());
		assertNotNull(element);
		assertEquals(element.getType().getName(), ElementTypeEnumType.USER.getName());
	}
	
	
	
	
	
	/**
	 * the tree doesn't change
	 * 
	 * after will be
	 * - Users
	 *   - lark0001-0000
	 *   - lark0002-0000
	 *   - lark0002-0001
	 * - Groups
	 *   - group0000 
	 *   - group0001
	 *     ~ lark0001-0000
	 *   - group0002 (renamed back)
	 *     ~ lark0002-0000
	 *     ~ lark0002-0001
	 *  - Others
	 *     - MAGICIAN_USERNAME (attr is modified, structural group)
	 */
	public void testEnumGroupToStructGroup() throws LDAPException{
		testUserToStructGroup();
	}
	
	public void testSyncCp15() throws Exception {
		testSyncCp10();
	}
	
	public void testCp15CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp15CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp150CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp15CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp15CheckMagicianAsStructGroup() throws DictionaryException{
		testCp10CheckMagicianAsStructGroup();
	}
	
	
	
	
		
	/**
	 * 
	 */
	public void testSyncCp16FromUserToEnumGroup() throws Exception{
		String dn = "CN="+MAGICIAN_USERNAME+"," + AD_HELPER.getOthersRootDn();
		LDAPModification mod = new LDAPModification(LDAPModification.ADD, 
				new LDAPAttribute("userPrincipalName", MAGICIAN_USERNAME));
		AD_HELPER.modify(dn, mod);
		
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		Map<String, String[]> props = getOverrideProperties();
		
		props.put(BasicLDAPEnrollmentProperties.USER_REQUIREMENTS, new String[] { 
				"(&(userPrincipalName=*)(!(userPrincipalName="+MAGICIAN_USERNAME+")))" });
		props.put(BasicLDAPEnrollmentProperties.GROUP_REQUIREMENTS, new String[] { 
				"(|(objectClass=group)(userPrincipalName="+MAGICIAN_USERNAME+"))" });
		
		Date startTime = new Date();
		IRealmData data = convert(
                UNITTEST_DOMAIN_NAME,
                EnrollmentTypeEnumType.DIRECTORY,
                props, 
                AD_DOMAIN_PROPERTY_FILE,
                UNITTEST_CONN_FILE);
        enrollmentManager.updateRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
		postCheck(startTime);
		
		dbCount.dictElements += 1; // new group
		dbCount.dictEnumGroupMembers += 0;
		dbCount.dictEnumGroups += 1; //new misclassified group
		dbCount.dictEnumMembers += 0; //no new memebers since it doesn't have member attribute
		dbCount.dictEnumRefMembers += 0;
		dbCount.dictLeafElements += 0;
		dbCount.dictStructGroups += 0; 
		dbCount.checkDatabaseCount();
	}
	
	public void testCp16CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp16CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp16CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp16CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp16CheckMagicianAsEnumGroup() throws DictionaryException{
		testCp13CheckMagicianAsEnumGroup();
	}
	
	
	
	
	
	/**
	 * 
	 */
	public void testSyncCp17FromEnumGroupToStructGroup() throws Exception {
		testUserToStructGroup();
	}
	
	public void testSyncCp17() throws Exception {
		testSyncCp10();
	}
	
	public void testCp17CheckMembership() throws DictionaryException {
		assertMemebership(0);
	}

	public void testCp17CheckUserTime() throws DictionaryException {
		assertUsersTimeNoChange(0);
	}

	public void testCp170CheckGroupTime() throws DictionaryException {
		assertGroupsTimeNoChange(0);
	}

	public void testCp17CheckStructGroupTime() throws DictionaryException {
		assertStructGroupsTimeNoChange();
	}
	
	public void testCp17CheckMagicianAsStructGroup() throws DictionaryException{
		testCp10CheckMagicianAsStructGroup();
	}
	
	
	
	
	public void testLastTest() throws Exception {
//		for(int i=0; i < 5; i++){
//			sync();
//		}
//		checkDatabaseCount(dictElements, dictEnumGroupMembers, dictEnumGroups, dictEnumMembers,
//				dictEnumRefMembers, dictLeafElements, dictStructGroups);
		
		AD_HELPER.disconnect();
	}
}
