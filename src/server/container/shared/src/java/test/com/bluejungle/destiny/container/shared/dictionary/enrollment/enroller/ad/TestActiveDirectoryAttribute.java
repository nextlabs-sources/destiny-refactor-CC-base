/*
 * Created on Apr 1, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.Order;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestActiveDirectoryAttribute.java#1 $
 */

public class TestActiveDirectoryAttribute extends BaseEnrollerSharedTestCase {
	private static final String UNITTEST_DOMAIN_NAME = "unittest.nextlabs.com";
	private static final String AD_DOMAIN_PROPERTY_FILE = ENROLL_DIR + "ad.template.def";
	private static final String UNITTEST_CONN_FILE = ENROLL_DIR + "test/ad.unittest.conn";

	private static final String ROOT_DN = ActiveDirectoryTestHelper.generateRootDn("unittest attribute");
	
	private static final ActiveDirectoryTestHelper AD_HELPER =
			new ActiveDirectoryTestHelper(ROOT_DN);
	
	private static final int NUMBER_OF_STRUCTURE_GROUP = AD_HELPER.getAllOus().length;
	
	private static final String DISPLAY_NAME_PREFIX = "Ascii";
	private static final int TESTED_CHAR = 256 - 3;
	
	private static int createdChars = 0;

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
		AD_HELPER.removeAllUnitTestData();
	}
	
	public void testPerpare() throws LDAPException{
		AD_HELPER.createOus();
	}
	
	public void testAddUserWithDifferentAsciiCN() throws LDAPException {
		final String nameTempalte = DISPLAY_NAME_PREFIX + "%03d%s%cX";
		
		assertEquals(0, createdChars);
		
		// http://www.ietf.org/rfc/rfc1779.txt
		// but microsoft has it format
		// illegal chars / \ [ ] : ; | = , + * ? < > @ "
		for (int i = 0; i < 256; i++) {
			final String rnd;
			switch (i) {
			case 0x00: // NULL (null)
			case 0x0A: // LF (NL line feed, new line)
			case 0x0D: // <CR>	
				continue;
//			case '/':
			case '\\':
//			case '[':
//			case ']':
//			case ':':
			case ';':
//			case '|':
			case '=':
			case ',':
			case '+':
//			case '*':
//			case '?':
			case '<':
			case '>':
//			case '@':		
			case '\"':	
				rnd = String.format(nameTempalte, i, "\\", (char)i);
				break;
			default:
				rnd = String.format(nameTempalte, i, "", (char)i);
				break;
			}
			
			LDAPAttributeSet attributeSet = new LDAPAttributeSet();
			attributeSet.add(new LDAPAttribute("objectclass", "inetOrgPerson"));
			//cn will be created automatically
//			attributeSet.add(new LDAPAttribute("cn", name));
//			attributeSet.add(new LDAPAttribute("2.5.4.3", name));
//			attributeSet.add(new LDAPAttribute("oid.2.5.4.3", name));
			
			String dn = "CN=" + rnd + "," + AD_HELPER.getUsersRootDn();
			LDAPEntry entry = new LDAPEntry(dn, attributeSet);
			try {
				AD_HELPER.add(entry);
				createdChars++;
			} catch (LDAPException e) {
				if(e.getResultCode() == LDAPException.INVALID_DN_SYNTAX){
//					System.out.println(rnd + ", " +i + ", " + (char)i);
					fail(rnd + ", " +i + ", " + (char)i);
				}else{
					throw e;
				}
			}
		}
		
		assertEquals(TESTED_CHAR, createdChars);
	}
	
	private Map<String, String[]> getOverrideProperties() {
		Map<String, String[]> overrideProperties = encodePassword(ActiveDirectoryTestHelper.PASSWORD);
		overrideProperties.put(ActiveDirectoryEnrollmentProperties.ROOTS,
				new String[] { ROOT_DN });
		return overrideProperties;
	}

	public void testSync() throws Exception {
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

		dbCount.dictElements 			+= NUMBER_OF_STRUCTURE_GROUP + createdChars;
		dbCount.dictEnumGroupMembers 	+= 0;
		dbCount.dictEnumGroups 			+= 0;
		dbCount.dictEnumMembers 		+= 0;
		dbCount.dictEnumRefMembers 		+= 0;
		dbCount.dictLeafElements 		+= createdChars;
		dbCount.dictStructGroups 		+= NUMBER_OF_STRUCTURE_GROUP;
		dbCount.checkDatabaseCount();
	}
	
	public void testCheckUsers() throws DictionaryException {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
		assertEquals(1, enrollments.size());

		IElementType type = guessElementType(UserReservedFieldEnumType.class);
		IPredicate predicate1 = dictionary.condition(type);
		IPredicate predicate2 = dictionary.condition(enrollments.iterator().next());

		IDictionaryIterator<IMElement> users1 = dictionary.query(
				new CompositePredicate(BooleanOp.AND, Arrays.asList(predicate1, predicate2)), 
				new Date(), 
				new Order[] { Order.ascending(type.getField(UserReservedFieldEnumType.DISPLAY_NAME.getName())) }, 
				null);

		int currentIndex = Integer.MIN_VALUE;
		int total = 0;
		while (users1.hasNext()) {
			String displayName = users1.next().getDisplayName();
			assertTrue(displayName.startsWith(DISPLAY_NAME_PREFIX));
			int i =	Integer.parseInt(displayName.substring(DISPLAY_NAME_PREFIX.length(),
							DISPLAY_NAME_PREFIX.length() + 3));

			//also test the Order
			assertTrue(currentIndex < i);
			currentIndex = i;
			total++;

			char c = displayName.charAt(DISPLAY_NAME_PREFIX.length() + 3);
			assertEquals((char) i, c);
		}

		assertEquals(total, createdChars);
	}
}
