/*
 * Created on Feb 27, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/util/TestLDAPEnrollmentHelper.java#1 $
 */
public class TestLDAPEnrollmentHelper{
	
	@Test
	public void getDictionaryPathFromDNOk(){
		String[] expected, actuals;
		
		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"cn=commonName,ou=eng,dc=nextlabs,dc=com").getPath();
 		expected = new String[]{ "dc=com", "dc=nextlabs", "ou=eng", "cn=commonname" };
 		assertArrayEquals(expected, actuals);
 		
 		//different case
 		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
			"cn=CommonName,ou=ENG,DC=nextlabs,dc=com").getPath();
		expected = new String[]{ "dc=com", "dc=nextlabs", "ou=eng", "cn=commonname" };
		assertArrayEquals(expected, actuals);
 		
		//escaped comma in the front
 		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"\\,cn=commonName,ou=eng,dc=nextlabs,dc=com").getPath();
		expected = new String[]{ "dc=com", "dc=nextlabs", "ou=eng", "\\,cn=commonname" };
		assertArrayEquals(expected, actuals);
 		
		//escaped comma in middle before comma
 		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"cn=commonName\\,,ou=eng,dc=nextlabs,dc=com").getPath();
		expected = new String[]{ "dc=com", "dc=nextlabs", "ou=eng", "cn=commonname\\," };
		assertArrayEquals(expected, actuals);
		
		//escaped comma in middle after comma
		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"cn=commonName,\\,ou=eng,dc=nextlabs,dc=com").getPath();
		expected = new String[]{ "dc=com", "dc=nextlabs", "\\,ou=eng", "cn=commonname" };
		assertArrayEquals(expected, actuals);
		
		//escaped comma at the end
		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"cn=commonName,ou=eng,dc=nextlabs,dc=com\\,").getPath();
		expected = new String[]{ "dc=com\\,", "dc=nextlabs", "ou=eng", "cn=commonname" };
		assertArrayEquals(expected, actuals);
		
		//escape the escaped char 
		actuals = LDAPEnrollmentHelper.getDictionaryPathFromDN(
				"cn=commonName\\\\,ou=eng,dc=nextlabs,dc=com").getPath();
		expected = new String[]{ "dc=com", "dc=nextlabs", "ou=eng", "cn=commonname\\\\" };
		assertArrayEquals(expected, actuals);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getDictionaryPathFromDNStartWithComma() {
		LDAPEnrollmentHelper.getDictionaryPathFromDN(",cn=commonName,ou=eng,dc=nextlabs,dc=com");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getDictionaryPathFromDNDoubleComma() {
		LDAPEnrollmentHelper.getDictionaryPathFromDN("cn=commonName,ou=eng,,dc=nextlabs,dc=com");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void getDictionaryPathFromDNEndWithComma() {
		LDAPEnrollmentHelper.getDictionaryPathFromDN("cn=commonName,ou=eng,,dc=nextlabs,dc=com,");
	}
	
	@Test
	public void getNameFromDNOk(){
		String actuals;
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1Ncommonname", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=commonName\\,,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1Ncommonname\\,", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=\\,commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1N\\,commonname", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=\\,commonName\\\\,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1N\\,commonname\\\\", actuals);
	}
	
	/**
	 * the corner cases are the cases with irregular/invalid dn
	 */
	@Test
	public void getNameFromDNCornerCases(){
		
		String actuals;
		actuals = LDAPEnrollmentHelper.getNameFromDN("dn=cn=commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1Ncn=commonname", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1Ncommonname", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1N", actuals);
		
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=\\\\,commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1N\\\\", actuals);
	}
	
	@Test
	public void getNameFromInvalidDN(){
		String actuals;

		//start with comma
		actuals = LDAPEnrollmentHelper.getNameFromDN(",cn=commonName,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1N", actuals);
		
		//double comma
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=commonName,,ou=eng,dc=nextlabs,dc=com", "D0MA1N");
		assertEquals("D0MA1Ncommonname", actuals);
		
		//end with comma
		actuals = LDAPEnrollmentHelper.getNameFromDN("cn=commonName,ou=eng,dc=nextlabs,dc=com,", "D0MA1N");
		assertEquals("D0MA1Ncommonname", actuals);
	}

}
