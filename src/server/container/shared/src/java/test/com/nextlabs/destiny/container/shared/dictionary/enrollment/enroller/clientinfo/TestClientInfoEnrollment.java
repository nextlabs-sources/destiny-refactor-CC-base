/*
 * Created on Apr 8, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.SAXException;

import static com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo.ClientInfoTag.*;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/TestClientInfoEnrollment.java#1 $
 */

public class TestClientInfoEnrollment extends TestClientInfoEnrollmentBase {
	static final String CLIENT_INFO_DOMAIN  = "TestClientInfoEnrollment";
	
	@Override
	String getTestDomainName() {
		return CLIENT_INFO_DOMAIN;
	}
	
	private IEnrollment enrollment;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupBlugjunleEnrollment();
		enrollment = dictionary.getEnrollment(getTestDomainName());
	}
	
	private void expectedSAXException(String xml, String expectedExceptionMesssge)
			throws Exception {
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;

			session = enrollment.createSession();
			//don't care pre-cache and fetch size here
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 1, 80);

			cie.startEnrollment();
			fail();
		} catch(EnrollmentValidationException e){
			assertNotNull(e);
			assertEquals(expectedExceptionMesssge, e.getMessage());
		} finally {
			close(cie, session);
		}
	}
	
	private void expectedOK(String xml, int size, String warning) throws Exception {
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;
		
			session = enrollment.createSession();
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 50, 80);
		
			cie.startEnrollment();
			
			assertEquals(1, cie.allElements.size());
			assertNotNull(cie.allElements.get(0));
			assertEquals(size, cie.allElements.get(0).size());
		
			assertEquals(warning, cie.getWarningMessage());
			assertEquals(size, cie.getTotalCount());
		} finally {
			close(cie, session);
		}
	}
	
	public void testInvalidClientFetchSize() throws Exception{
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 0, 80);
			fail();
		}catch(EnrollmentValidationException e){
			assertNotNull(e);
			assertEquals("clientFetchSize must be a positive number.", e.getMessage());
		} finally {
			close(cie, session);
		}
		
		try {
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, -10, 80);
			fail();
		}catch(EnrollmentValidationException e){
			assertNotNull(e);
			assertEquals("clientFetchSize must be a positive number.", e.getMessage());
		} finally {
			close(cie, session);
		}
		
	}
	
	public void testInvalidUserFetchSize() throws Exception{
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 50, 0);
			fail();
		}catch(EnrollmentValidationException e){
			assertNotNull(e);
			assertEquals("userFetchSize must be a positive number.", e.getMessage());
		} finally {
			close(cie, session);
		}
		
		try {
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 50, -10);
			fail();
		}catch(EnrollmentValidationException e){
			assertNotNull(e);
			assertEquals("userFetchSize must be a positive number.", e.getMessage());
		} finally {
			close(cie, session);
		}
		
		try {
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, true, 50, -10);
		} finally {
			close(cie, session);
		}
	}
	
	public void testNoClients() throws Exception {
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			String xml = XML_HEADER + "<" + TAG_CLIENT_INFORMATION + ">" + "</"
				+ TAG_CLIENT_INFORMATION + ">";
		
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;
		
			session = enrollment.createSession();
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 50, 80);
		
			cie.startEnrollment();
			
			assertEquals(0, cie.allElements.size());
		
			assertEquals(null, cie.getWarningMessage());
			assertEquals(0, cie.getTotalCount());
		} finally {
			close(cie, session);
		}
	}

	public void testEmptyFile() throws Exception {
		expectedSAXException("", "Premature end of file.");
	}
		
	public void testEmptyXml() throws Exception {
		expectedSAXException(XML_HEADER, "Premature end of file.");
	}
	
	public void testNoClientsOpenTagTypo() throws Exception {
		String xml = XML_HEADER + "<" + TAG_CLIENT_INFORMATION + "typo>" + "</"
					+ TAG_CLIENT_INFORMATION + ">";

		expectedSAXException(xml, "Unknown tag: \"" + TAG_CLIENT_INFORMATION + "typo\"");
	}
	
	public void testNoClientsCloseTagTypo() throws Exception {
		String xml = XML_HEADER + "<" + TAG_CLIENT_INFORMATION + ">" + "</"
				+ TAG_CLIENT_INFORMATION + "typo>";

		String expected = "The end-tag for element type \"" + TAG_CLIENT_INFORMATION
				+ "\" must end with a '>' delimiter.";
		
		expectedSAXException(xml, expected);
	}
	
	
	
	public void testOneClientAllFieldPresent() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		expectedOK(xml, 1, null);
	}
	
	public void testOneClientAllFieldButMissId() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "\"" + ATTR_IDENTIFIER + "\" is not present from \"" + TAG_CLIENT + "\".";
		
		expectedSAXException(xml, expected);
	}
		
	public void testOneClientAllFieldButEmptyId() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "An empty string is not a valid value for \"" + ATTR_IDENTIFIER + "\"";
		
		expectedSAXException(xml, expected);
	}
	
	public void testOneClientAllFieldButMissShortName() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "\"" + ATTR_SHORT_NAME + "\" is not present from \"" + TAG_CLIENT + "\".";
		
		expectedSAXException(xml, expected);
	}
	
	public void testOneClientAllFieldButMissLongName() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "\"" + ATTR_LONG_NAME + "\" is not present from \"" + TAG_CLIENT + "\".";
		
		expectedSAXException(xml, expected);
	}
	
	public void testOneClientAllFieldButMissDomain() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testOneClientAllFieldButMissDomainName() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "\"" + ATTR_EMAIL_TEMPLATE_NAME + "\" is not present from \"" + TAG_EMAIL_TEMPLATE + "\".";
		
		expectedSAXException(xml, expected);
	}
	
	public void testOneClientAllFieldButMissUser() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testOneClientAllFieldButMissUserName() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		String expected = "\"" + ATTR_USER_NAME + "\" is not present from \"" + TAG_USER + "\".";
		
		expectedSAXException(xml, expected);
	}
	
	public void testOneClientAllFieldButExtraClientAttr() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextlabs.com\"/>"
		+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testOneClientAllFieldButNoUserDomain() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testTwoClientNoUserDomain() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "
				+ATTR_LONG_NAME+"=\"3lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"2B$\" "+ATTR_SHORT_NAME+"=\"22shOrt!\" "
				+ATTR_LONG_NAME+"=\"33lOng$\"/>"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 2, null);
	}
	
	public void testDuplicatedClientId() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"sameId\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "
				+ATTR_LONG_NAME+"=\"3lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"sameId\" "+ATTR_SHORT_NAME+"=\"22shOrt!\" "
				+ATTR_LONG_NAME+"=\"33lOng$\"/>"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;
		
			session = enrollment.createSession();
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 50, 80, true);
		
			cie.startEnrollment();
			fail();
		} catch(DictionaryException e ){
			assertNotNull(e);
			System.out.println(e.getClass());
			assertEquals(
					"elements[] contains values with identical paths:TestClientInfoEnrollment,client,sameId]",
					e.getMessage());

		} finally {
			close(cie, session);
		}
	}
	
	public void testClientFetch() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"0Z$\" "+ATTR_SHORT_NAME+"=\"0shOrt!\" "+ATTR_LONG_NAME+"=\"0lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"1shOrt!\" "+ATTR_LONG_NAME+"=\"1lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"2B$\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"2lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"3C$\" "+ATTR_SHORT_NAME+"=\"3shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"4D$\" "+ATTR_SHORT_NAME+"=\"4shOrt!\" "+ATTR_LONG_NAME+"=\"4lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"5E$\" "+ATTR_SHORT_NAME+"=\"5shOrt!\" "+ATTR_LONG_NAME+"=\"5lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"6F$\" "+ATTR_SHORT_NAME+"=\"6shOrt!\" "+ATTR_LONG_NAME+"=\"6lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"7G$\" "+ATTR_SHORT_NAME+"=\"7shOrt!\" "+ATTR_LONG_NAME+"=\"7lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"8H$\" "+ATTR_SHORT_NAME+"=\"8shOrt!\" "+ATTR_LONG_NAME+"=\"8lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"9I$\" "+ATTR_SHORT_NAME+"=\"9shOrt!\" "+ATTR_LONG_NAME+"=\"9lOng$\"/>"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;
		
			session = enrollment.createSession();
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 4, 80);
		
			cie.startEnrollment();
			
			assertEquals(3, cie.allElements.size());
			assertNotNull(cie.allElements.get(0));
			assertEquals(4, cie.allElements.get(0).size());
			
			assertNotNull(cie.allElements.get(1));
			assertEquals(4, cie.allElements.get(1).size());
			
			assertNotNull(cie.allElements.get(2));
			assertEquals(2, cie.allElements.get(2).size());
			
			assertEquals(null, cie.getWarningMessage());
			assertEquals(10, cie.getTotalCount());
		} finally {
			close(cie, session);
		}
	}
	
	public void testClientFetchNoRemainder() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"0Z$\" "+ATTR_SHORT_NAME+"=\"0shOrt!\" "+ATTR_LONG_NAME+"=\"0lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"1shOrt!\" "+ATTR_LONG_NAME+"=\"1lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"2B$\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"2lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"3C$\" "+ATTR_SHORT_NAME+"=\"3shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"4D$\" "+ATTR_SHORT_NAME+"=\"4shOrt!\" "+ATTR_LONG_NAME+"=\"4lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"5E$\" "+ATTR_SHORT_NAME+"=\"5shOrt!\" "+ATTR_LONG_NAME+"=\"5lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"6F$\" "+ATTR_SHORT_NAME+"=\"6shOrt!\" "+ATTR_LONG_NAME+"=\"6lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"7G$\" "+ATTR_SHORT_NAME+"=\"7shOrt!\" "+ATTR_LONG_NAME+"=\"7lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"8H$\" "+ATTR_SHORT_NAME+"=\"8shOrt!\" "+ATTR_LONG_NAME+"=\"8lOng$\"/>"
			+ "<"+TAG_CLIENT+" "+ATTR_IDENTIFIER+"=\"9I$\" "+ATTR_SHORT_NAME+"=\"9shOrt!\" "+ATTR_LONG_NAME+"=\"9lOng$\"/>"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		MockClientInfoEnrollment cie = null;
		IEnrollmentSession session = null;
		try {
			StringReader stringReader = new StringReader(xml);
			MockClientInfoEnrollment.reader = stringReader;
		
			session = enrollment.createSession();
			cie = new MockClientInfoEnrollment(session, dictionary, enrollment, false, 5, 80);
		
			cie.startEnrollment();
			
			assertEquals(2, cie.allElements.size());
			assertNotNull(cie.allElements.get(0));
			assertEquals(5, cie.allElements.get(0).size());
			
			assertNotNull(cie.allElements.get(1));
			assertEquals(5, cie.allElements.get(1).size());
			
			assertEquals(null, cie.getWarningMessage());
			assertEquals(10, cie.getTotalCount());
		} finally {
			close(cie, session);
		}
	}
	
	public void testDuplicatedUsersInSameClient() throws Exception {
		String xml = XML_HEADER 
			+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
			+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
			+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
			+ "</"+TAG_CLIENT+">"			
			+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testDuplicatedUsersInDifferentClient() throws Exception {
		String xml = XML_HEADER 
			+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
			+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
			+ "</"+TAG_CLIENT+">"	
			+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"2B#\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
			+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"hchan@bluejungle.com\"/>"
			+ "</"+TAG_CLIENT+">"
			+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 2, null);
	}
	
	public void testUnenrolledUser() throws Exception {
		String xml = XML_HEADER 
			+ "<"+TAG_CLIENT_INFORMATION+">" 
			+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
			+ "<"+TAG_USER+" "+ATTR_USER_NAME+"=\"nosuchuser@nosuchdomain.com\"/>"
			+ "</"+TAG_CLIENT+">"	
			+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 unenrolled user(s).");
	}
	
	//TODO test same unenroll users happen multiple time, such warn only once
	
	public void testUnsuppportedFormatDomain1() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"!h.nextlabs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 unsupported format domain(s).");
	}
	
	public void testUnsuppportedFormatDomain2() throws Exception {
		//no caught problem
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"@@@@@@\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testUnsuppportedFormatDomain3() throws Exception {
		//no caught problem
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\".-@_\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testDomainWildCard() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"*@nextlabs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testDomainWildCard2() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"**h.nextlabs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 unsupported format domain(s).");
	}
	
	public void testDomainWildCard3() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"*h.nextl*abs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 unsupported format domain(s).");
	}
	
	public void testDomainWildCard4() throws Exception {
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\" unknwon-attr=\"1\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\"h.nextl*abs.com\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 unsupported format domain(s).");
	}
	
	
	//TODO duplicated domains on same client
	
	//TODO duplicated domains on different client
	
	public void testDomainLength() throws Exception {
		char[] domainNameChars = new char[254];
		Arrays.fill(domainNameChars, 'c');
		String domainName = new String(domainNameChars);
	
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domainName+"\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, null);
	}
	
	public void testDomainLengthMultiDomains() throws Exception {
		List<String> domains = new ArrayList<String>();
		for(int i=0; i < 5; i++){
			char[] domainNameChars = new char[i == 4 ? 49 : 50];
			Arrays.fill(domainNameChars, (char)('a' + i));
			domains.add(new String(domainNameChars));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(XML_HEADER 
				+"<" + TAG_CLIENT_INFORMATION + ">" 
				+"<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
				+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">");
		
		for(String domain : domains){
			sb.append("<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domain+"\"/>");
		}
				
		sb.append("</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">");
		
		expectedOK(sb.toString(), 1, null);
	}
	
	public void testDomainLengthOneOverLimit() throws Exception {
		char[] domainNameChars = new char[255];
		Arrays.fill(domainNameChars, 'c');
		String domainName = new String(domainNameChars);
	
		String xml = XML_HEADER 
		+ "<"+TAG_CLIENT_INFORMATION+">" 
		+ "<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
		  +ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">"
		+ "<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domainName+"\"/>"
		+ "</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">";
		
		expectedOK(xml, 1, "1 client(s) excessed the max length of domain names.");
	}
	public void testDomainLengthMultiDomainsTooLong() throws Exception {
		List<String> domains = new ArrayList<String>();
		for(int i=0; i < 5; i++){
			char[] domainNameChars = new char[50];
			Arrays.fill(domainNameChars, (char)('a' + i));
			domains.add(new String(domainNameChars));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(XML_HEADER 
				+"<" + TAG_CLIENT_INFORMATION + ">" 
				+"<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
				+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">");
		
		for(String domain : domains){
			sb.append("<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domain+"\"/>");
		}
				
		sb.append("</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">");
		
		expectedOK(sb.toString(), 1, "1 client(s) excessed the max length of domain names.");
	}
	
	public void testDomainLengthMultiDomainsTooLong2() throws Exception {
		List<String> domains = new ArrayList<String>();
		for(int i=0; i < 6; i++){
			char[] domainNameChars = new char[i == 5 ? 1 : 50];
			Arrays.fill(domainNameChars, (char)('a' + i));
			domains.add(new String(domainNameChars));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(XML_HEADER 
				+"<" + TAG_CLIENT_INFORMATION + ">" 
				+"<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
				+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">");
		
		for(String domain : domains){
			sb.append("<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domain+"\"/>");
		}
				
		sb.append("</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">");
		
		expectedOK(sb.toString(), 1, "1 client(s) excessed the max length of domain names.");
	}
	
	public void testDomainLengthMultiDomainsTooLong3() throws Exception {
		List<String> domains = new ArrayList<String>();
		for(int i=0; i < 24; i++){
			char[] domainNameChars = new char[20];
			Arrays.fill(domainNameChars, (char)('a' + i));
			domains.add(new String(domainNameChars));
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(XML_HEADER 
				+"<" + TAG_CLIENT_INFORMATION + ">" 
				+"<"+TAG_CLIENT+ " "+ATTR_IDENTIFIER+"=\"1A#\" "
				+ATTR_SHORT_NAME+"=\"2shOrt!\" "+ATTR_LONG_NAME+"=\"3lOng$\">");
		
		for(String domain : domains){
			sb.append("<"+TAG_EMAIL_TEMPLATE+" "+ATTR_EMAIL_TEMPLATE_NAME+"=\""+domain+"\"/>");
		}
				
		sb.append("</"+TAG_CLIENT+">"
		+ "</"+TAG_CLIENT_INFORMATION+">");
		
		expectedOK(sb.toString(), 1, "1 client(s) excessed the max length of domain names.");
	}
}
