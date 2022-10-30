/*
 * Created on Apr 3, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/TestClientInfoEnroller.java#1 $
 */
public class TestClientInfoEnroller extends TestClientInfoEnrollmentBase {
	private static final String CLIENT_INFO_DOMAIN = "TestClientInfoEnroller";
	
	static final String TEST_DATA_DIR = SRC_ROOT_DIR
			+ "/server/container/shared/src/java/test/"
			+ TestClientInfoEnroller.class.getPackage().getName().replace(".", "/") + "/";

	static final String BLUEJUNGLE_CLIENT_INFO_FILE = ENROLLMENT_DATA_DIR
			+ "client-info.file";

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestClientInfoEnroller.class);
	}

	@Override
	String getTestDomainName() {
		return CLIENT_INFO_DOMAIN;
	}

	/**
	 * Enroll local domains
	 * @throws DictionaryException 
	 * @throws Exception
	 */
	public void testFileDoesNotExist() throws Exception {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		properties.put(ClientInfoEnroller.CLIENT_INFO_FILE_KEY, new String[] { "nosuchfile" });
		try {
			this.enroller.enroll(getTestDomainName(), new String[] {},
					EnrollmentTypeEnumType.CLIENT_INFO, properties);
			fail("Should fail if the client-info doesn't not exist");
		} catch (Exception e) {
			assertNotNull(e);
			Throwable rootCause = getRootCause(e);
			assertTrue(rootCause.getMessage().contains("java.io.FileNotFoundException"));
		} finally {
			IEnrollment enrollment = this.dictionary.getEnrollment(getTestDomainName());
			assertNull(enrollment);
			deleteClientInfoEnrollemnt();
		}
	}

	public void testInvalidFile() throws Exception {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		properties.put(ClientInfoEnroller.CLIENT_INFO_FILE_KEY, new String[] { TEST_DATA_DIR
				+ "client-info.invalidFormat.file" });
		try {
			this.enroller.enroll(getTestDomainName(), new String[] {},
					EnrollmentTypeEnumType.CLIENT_INFO, properties);

			IEnrollment enrollment = this.dictionary.getEnrollment(getTestDomainName());
			assertNotNull(enrollment);
			assertNotNull(enrollment.getStatus());
			assertFalse(enrollment.getStatus().isSuccessful());
			assertEquals("Content is not allowed in prolog.", enrollment.getStatus()
					.getErrorMessage());
		} finally {
			deleteClientInfoEnrollemnt();
		}
	}

	public void testInvalidXmlFormat() throws Exception {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		properties.put(ClientInfoEnroller.CLIENT_INFO_FILE_KEY, new String[] { 
				TEST_DATA_DIR + "client-info.invalidXmlFormat.file" });
		try {
			this.enroller.enroll(getTestDomainName(), new String[] {},
					EnrollmentTypeEnumType.CLIENT_INFO, properties);

			IEnrollment enrollment = this.dictionary.getEnrollment(getTestDomainName());
			assertNotNull(enrollment);
			assertNotNull(enrollment.getStatus());
			assertFalse(enrollment.getStatus().isSuccessful());
			assertEquals("\"" + ClientInfoTag.ATTR_IDENTIFIER + "\" is not present from \""
					+ ClientInfoTag.TAG_CLIENT + "\".", enrollment.getStatus()
					.getErrorMessage());
		} finally {
			deleteClientInfoEnrollemnt();
		}
	}

	/**
	 * Enroll local domains
	 * @throws DictionaryException 
	 * @throws Exception
	 */
	public void testLocalEnrollment() throws Exception {
		Map<String, String[]> properties = new HashMap<String, String[]>();
		properties.put(ClientInfoEnroller.CLIENT_INFO_FILE_KEY,
				new String[] { BLUEJUNGLE_CLIENT_INFO_FILE });
		this.enroller.enroll(getTestDomainName(), new String[] {},
				EnrollmentTypeEnumType.CLIENT_INFO, properties);
		IEnrollment enrollment = this.dictionary.getEnrollment(getTestDomainName());
		assertNotNull(enrollment);
		assertNotNull(enrollment.getStatus());
		assertTrue(enrollment.getStatus().isSuccessful());
		assertEquals("4 unenrolled user(s). And 29 duplicated domain(s).", enrollment.getStatus()
				.getErrorMessage());
	}

	//TODO more test cases on retrieving the data.

	public void testDeleteClientInfoEnrollments() throws DictionaryException {
		deleteClientInfoEnrollemnt();
	}
}
