package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestPerEnrollmentExternalNames.java#1 $
 */

import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * These tests verify that per-enrollment external names work.
 */
public class TestPerEnrollmentExternalNames extends TestCase {

    private IDictionary dictionary = (IDictionary)ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );

    public static TestSuite suite() {
        return new TestSuite(TestPerEnrollmentExternalNames.class);
    }

    public void testObtainDictionary() {
        assertNotNull( dictionary );
    }

    public void testClearDictionary() throws Exception {
        Session hs = null;
        Transaction tx = null;
        try {
            hs = ((Dictionary)dictionary).getCountedSession();
            tx = hs.beginTransaction();
            hs.delete("from UpdateRecord");
            hs.delete("from DictionaryElementBase");
            hs.delete("from Enrollment");
            hs.delete("from ElementType");
            tx.commit();
        } catch ( HibernateException cause ) {
            if ( tx != null ) {
                tx.rollback();
            }
            fail(cause.getMessage());
        } finally {
            if ( hs != null ) {
                hs.close();
            }
        }
    }

    public void testSetupUserAndHostTypes() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.makeNewType("user");
        assertNotNull( userType );
        IElementField firstName = userType.addField("firstName", ElementFieldType.CS_STRING);
        assertNotNull(firstName);
        IElementField lastName = userType.addField("lastName", ElementFieldType.STRING);
        assertNotNull(lastName);
        IElementField dateOfBirth = userType.addField("dateOfBirth", ElementFieldType.DATE);
        assertNotNull(dateOfBirth);
        IElementField whenStarted = userType.addField("whenStarted", ElementFieldType.DATE);
        assertNotNull(whenStarted);
        IElementField employeeCode = userType.addField("employeeCode", ElementFieldType.NUMBER);
        assertNotNull(employeeCode);
        IMElementType hostType = dictionary.makeNewType("host");
        assertNotNull( hostType );
        IElementField hostName = hostType.addField("hostName", ElementFieldType.STRING);
        assertNotNull(hostName);
        IElementField controlPort = hostType.addField("controlPort", ElementFieldType.NUMBER);
        assertNotNull(controlPort);
        IEnrollment attDomain = dictionary.makeNewEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.makeNewEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveType(userType);
            session.saveType(hostType);
            session.saveEnrollment(attDomain);
            session.saveEnrollment(sbcDomain);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testSetExternalNamesOnUnsavedEnrollment() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        IEnrollment enrollment = dictionary.makeNewEnrollment("enrollment.com");
        assertNotNull(enrollment);
        String[] externals = enrollment.getExternalNames(userType);
        assertNotNull(externals);
        assertEquals(0, externals.length);
        enrollment.setExternalName(firstName,"UNSAVED FIRST");
        assertEquals("UNSAVED FIRST", enrollment.getExternalName(firstName));
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }
        assertEquals("UNSAVED FIRST", enrollment.getExternalName(firstName));
    }

    public void testSetExternalOnUnsavedWorked() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IEnrollment enrollment = dictionary.getEnrollment("enrollment.com");
        assertNotNull(enrollment);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        assertEquals("UNSAVED FIRST", enrollment.getExternalName(firstName));
    }

    public void testSetExternalNames() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        attDomain.setExternalName(firstName,"First Name ATT");
        sbcDomain.setExternalName(firstName,"first name sbc");
        IElementField lastName = userType.getField("lastName");
        assertNotNull(lastName);
        attDomain.setExternalName(lastName,"Last Name ATT");
        sbcDomain.setExternalName(lastName,"lastName name sbc");
        IElementField dateOfBirth = userType.getField("dateOfBirth");
        assertNotNull(dateOfBirth);
        attDomain.setExternalName(dateOfBirth,"DOB ATT");
        sbcDomain.setExternalName(dateOfBirth,"date of birth sbc");
        IElementField whenStarted = userType.getField("whenStarted");
        assertNotNull(whenStarted);
        attDomain.setExternalName(whenStarted,"whenStarted(att)");
        sbcDomain.setExternalName(whenStarted,"employment date sbc");
        IElementField employeeCode = userType.getField("employeeCode");
        assertNotNull(employeeCode);
        attDomain.setExternalName(employeeCode,"employee code ATT");
        sbcDomain.setExternalName(employeeCode,"employee number sbc");
        IElementField hostName = hostType.getField("hostName");
        assertNotNull(hostName);
        attDomain.setExternalName(hostName,"computer_name");
        sbcDomain.setExternalName(hostName,"computer_name");
        IElementField controlPort = hostType.getField("controlPort");
        assertNotNull(controlPort);
        attDomain.setExternalName(controlPort,"controlPort");
        sbcDomain.setExternalName(controlPort,"controlPort");
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(attDomain);
            session.saveEnrollment(sbcDomain);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testCheckExternalNames() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        assertEquals("First Name ATT", attDomain.getExternalName(firstName));
        assertEquals("first name sbc", sbcDomain.getExternalName(firstName));
        IElementField lastName = userType.getField("lastName");
        assertNotNull(lastName);
        assertEquals("Last Name ATT", attDomain.getExternalName(lastName));
        assertEquals("lastName name sbc", sbcDomain.getExternalName(lastName));
        IElementField dateOfBirth = userType.getField("dateOfBirth");
        assertNotNull(dateOfBirth);
        assertEquals("DOB ATT", attDomain.getExternalName(dateOfBirth));
        assertEquals("date of birth sbc", sbcDomain.getExternalName(dateOfBirth));
        IElementField whenStarted = userType.getField("whenStarted");
        assertNotNull(whenStarted);
        assertEquals("whenStarted(att)", attDomain.getExternalName(whenStarted));
        assertEquals("employment date sbc", sbcDomain.getExternalName(whenStarted));
        IElementField employeeCode = userType.getField("employeeCode");
        assertNotNull(employeeCode);
        assertEquals("employee code ATT", attDomain.getExternalName(employeeCode));
        assertEquals("employee number sbc", sbcDomain.getExternalName(employeeCode));
        IElementField hostName = hostType.getField("hostName");
        assertNotNull(hostName);
        assertEquals("computer_name", attDomain.getExternalName(hostName));
        assertEquals("computer_name", sbcDomain.getExternalName(hostName));
        IElementField controlPort = hostType.getField("controlPort");
        assertNotNull(controlPort);
        assertEquals("controlPort", attDomain.getExternalName(controlPort));
        assertEquals("controlPort", sbcDomain.getExternalName(controlPort));
    }

    public void testGetExternalNames() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        String[] attNames = attDomain.getExternalNames(userType);
        assertNotNull(attNames);
        assertEquals(5, attNames.length);
        String[] sbcNames = sbcDomain.getExternalNames(userType);
        assertEquals("DOB ATT", attNames[0]);
        assertEquals("First Name ATT", attNames[1]);
        assertEquals("Last Name ATT", attNames[2]);
        assertEquals("employee code ATT", attNames[3]);
        assertEquals("whenStarted(att)", attNames[4]);
        assertNotNull(sbcNames);
        assertEquals(5, sbcNames.length);
        assertEquals("date of birth sbc", sbcNames[0]);
        assertEquals("employee number sbc", sbcNames[1]);
        assertEquals("employment date sbc", sbcNames[2]);
        assertEquals("first name sbc", sbcNames[3]);
        assertEquals("lastName name sbc", sbcNames[4]);
    }

    public void testSetByExternalName() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        DictionaryKey key = new DictionaryKey(new byte[] {11, 21, 31, 41, 51, 61, 71});
        IMElement user = attDomain.makeNewElement( new DictionaryPath( new String[] {"a"} ),  userType, key);
        assertNotNull(user);
        IElementField[] fields = dictionary.getEnrollment("att.com").lookupField(userType, "First Name ATT");
        assertEquals(1, fields.length);
        user.setValue(fields[0], "sample_first_name");
        assertEquals("sample_first_name", user.getValue(firstName));
        user = sbcDomain.makeNewElement( new DictionaryPath( new String[] {"a"} ),userType, key);
        assertNotNull(user);
        
        fields = dictionary.getEnrollment("sbc.com").lookupField(userType, "first name sbc");
        assertEquals(1, fields.length);
        
        user.setValue(fields[0], "other_first_name");
        assertEquals("other_first_name", user.getValue(firstName));
    }

    public void testClearExternalName() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        attDomain.clearExternalName(firstName);
        assertNull(attDomain.getExternalName(firstName));
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(attDomain);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testClearExternalNameWorked() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        assertNull(attDomain.getExternalName(firstName));
    }

    public void testBulkClearExternalNames() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        attDomain.clearExternalNames(userType);
        sbcDomain.clearExternalNames(userType);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        assertNull(attDomain.getExternalName(firstName));
        assertNull(sbcDomain.getExternalName(firstName));
        IElementField lastName = userType.getField("lastName");
        assertNotNull(lastName);
        assertNull(attDomain.getExternalName(lastName));
        assertNull(sbcDomain.getExternalName(lastName));
        IElementField dateOfBirth = userType.getField("dateOfBirth");
        assertNotNull(dateOfBirth);
        assertNull(attDomain.getExternalName(dateOfBirth));
        assertNull(sbcDomain.getExternalName(dateOfBirth));
        IElementField whenStarted = userType.getField("whenStarted");
        assertNotNull(whenStarted);
        assertNull(attDomain.getExternalName(whenStarted));
        assertNull(sbcDomain.getExternalName(whenStarted));
        IElementField employeeCode = userType.getField("employeeCode");
        assertNotNull(employeeCode);
        assertNull(attDomain.getExternalName(employeeCode));
        assertNull(sbcDomain.getExternalName(employeeCode));
        // Only the user type should be cleared, not host type
        IElementField hostName = hostType.getField("hostName");
        assertNotNull(hostName);
        assertNotNull(attDomain.getExternalName(hostName));
        assertNotNull(sbcDomain.getExternalName(hostName));
        IElementField controlPort = hostType.getField("controlPort");
        assertNotNull(controlPort);
        assertNotNull(attDomain.getExternalName(controlPort));
        assertNotNull(sbcDomain.getExternalName(controlPort));
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(attDomain);
            session.saveEnrollment(sbcDomain);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testClearExternalNamesWorked() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        IElementField firstName = userType.getField("firstName");
        assertNotNull(firstName);
        assertNull(attDomain.getExternalName(firstName));
        assertNull(sbcDomain.getExternalName(firstName));
        IElementField lastName = userType.getField("lastName");
        assertNotNull(lastName);
        assertNull(attDomain.getExternalName(lastName));
        assertNull(sbcDomain.getExternalName(lastName));
        IElementField dateOfBirth = userType.getField("dateOfBirth");
        assertNotNull(dateOfBirth);
        assertNull(attDomain.getExternalName(dateOfBirth));
        assertNull(sbcDomain.getExternalName(dateOfBirth));
        IElementField whenStarted = userType.getField("whenStarted");
        assertNotNull(whenStarted);
        assertNull(attDomain.getExternalName(whenStarted));
        assertNull(sbcDomain.getExternalName(whenStarted));
        IElementField employeeCode = userType.getField("employeeCode");
        assertNotNull(employeeCode);
        assertNull(attDomain.getExternalName(employeeCode));
        assertNull(sbcDomain.getExternalName(employeeCode));
        // Fields of the host type should stay unchanged
        IElementField hostName = hostType.getField("hostName");
        assertNotNull(hostName);
        assertNotNull(attDomain.getExternalName(hostName));
        assertNotNull(sbcDomain.getExternalName(hostName));
        IElementField controlPort = hostType.getField("controlPort");
        assertNotNull(controlPort);
        assertNotNull(attDomain.getExternalName(controlPort));
        assertNotNull(sbcDomain.getExternalName(controlPort));
    }

    public void testSetupMaxType() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType type = dictionary.makeNewType("max");
        assertNotNull( type );
        for ( int i = 0 ; i != 20 ; i++ ) {
            if ( i % 2 == 0 ) {
                IElementField s = type.addField("str_"+i, ElementFieldType.STRING);
                assertNotNull(s);
            } else {
                IElementField s = type.addField("cs_str_"+i, ElementFieldType.CS_STRING);
                assertNotNull(s);
            }
            IElementField n = type.addField("num_"+i, ElementFieldType.NUMBER);
            assertNotNull(n);
            IElementField d = type.addField("dd_"+i, ElementFieldType.DATE);
            assertNotNull(d);
        }
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveType(type);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testSetExternalNamesForMaxType() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType type = dictionary.getType("max");
        assertNotNull(type);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        assertNotNull(sbcDomain);
        for ( int i = 0 ; i != 20 ; i++ ) {
            if ( i % 2 == 0 ) {
                IElementField s = type.getField("str_"+i);
                assertNotNull(s);
                attDomain.setExternalName(s, "ATT_str"+i);
                sbcDomain.setExternalName(s, "sbc str"+i);
            } else {
                IElementField s = type.getField("cs_str_"+i);
                assertNotNull(s);
                attDomain.setExternalName(s, "ATT_CSstr"+i);
                sbcDomain.setExternalName(s, "sbc CSstr"+i);
            }
            IElementField n = type.getField("num_"+i);
            assertNotNull(n);
            attDomain.setExternalName(n, "ATT_num"+i);
            sbcDomain.setExternalName(n, "sbc num"+i);
            IElementField d = type.getField("dd_"+i);
            assertNotNull(d);
            attDomain.setExternalName(d, "ATT_dd"+i);
            sbcDomain.setExternalName(d, "sbc dd"+i);
        }
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(attDomain);
            session.saveEnrollment(sbcDomain);
            session.commit();
        } finally {
            session.close();
        }
    }
    
    /**
     * assert it is unique field
     * @param enrollment
     * @return
     */
    private IElementField getField(IEnrollment enrollment, IElementType type,
            String externalName) {
        IElementField[] fields = enrollment.lookupField(type, externalName);
        assertEquals(1, fields.length);
        return fields[0];
    }

    public void testSetByExternalNameForMaxType() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType type = dictionary.getType("max");
        assertNotNull(type);
        IEnrollment attDomain = dictionary.getEnrollment("att.com");
        assertNotNull(attDomain);
        IEnrollment sbcDomain = dictionary.getEnrollment("sbc.com");
        DictionaryKey key = new DictionaryKey(new byte[] {1, 2, 3, 4, 5, 6, 7});
        assertNotNull(sbcDomain);
        IMElement maxAtt = attDomain.makeNewElement( new DictionaryPath( new String[] {"a"} ),type, key);
        assertNotNull(maxAtt);
        IMElement maxSbc = sbcDomain.makeNewElement( new DictionaryPath( new String[] {"a"} ),type, key);
        assertNotNull(maxSbc);
        for ( int i = 0 ; i != 20 ; i++ ) {
            if ( i % 2 == 0 ) {
                IElementField s = type.getField("str_"+i);
                assertNotNull(s);
                
                IElementField s2 = getField(attDomain, type, "ATT_str"+i);
                assertEquals(s, s2);
                maxAtt.setValue(s2, "sample_"+i);
                assertEquals("sample_"+i, maxAtt.getValue(s));
                
                maxSbc.setValue(getField(sbcDomain, type, "sbc str"+i), "complex_"+i);
                assertEquals("complex_"+i, maxSbc.getValue(s));
            } else {
                IElementField s = type.getField("cs_str_"+i);
                assertNotNull(s);
                maxAtt.setValue(getField(attDomain, type, "ATT_CSstr"+i), "sample_"+i);
                assertEquals("sample_"+i, maxAtt.getValue(s));
                
                maxSbc.setValue(getField(sbcDomain, type, "sbc CSstr"+i), "complex_"+i);
                assertEquals("complex_"+i, maxSbc.getValue(s));
            }
            IElementField n = type.getField("num_"+i);
            assertNotNull(n);
            
            maxAtt.setValue(getField(attDomain, type, "ATT_num"+i), new Long(i));
            assertEquals(new Long(i), maxAtt.getValue(n));
            
            maxSbc.setValue(getField(sbcDomain, type, "sbc num"+i), new Long(2*i+1));
            assertEquals(new Long(2*i+1), maxSbc.getValue(n));
            IElementField d = type.getField("dd_"+i);
            assertNotNull(d);
            
            maxAtt.setValue(getField(attDomain, type, "ATT_dd"+i), new Date(i));
            
            assertEquals(new Date(i), maxAtt.getValue(d));
            maxSbc.setValue(getField(sbcDomain, type, "sbc dd"+i), new Date(2*i+1));
            assertEquals(new Date(2*i+1), maxSbc.getValue(d));
        }
    }

    public void testSameExternalNameForDifferentTypes() throws DictionaryException {
        assertNotNull( dictionary );
        IMElementType userType = dictionary.getType("user");
        assertNotNull(userType);
        IMElementType hostType = dictionary.getType("host");
        assertNotNull(hostType);
        IEnrollment enrollment = dictionary.getEnrollment("att.com");
        assertNotNull(enrollment);
        IElementField field1 = userType.getField("firstName");
        assertNotNull(field1);
        IElementField field2 = hostType.getField("hostName");
        assertNotNull(field2);
        enrollment.setExternalName(field1,"NAME");
        enrollment.setExternalName(field2,"NAME");
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }
    }
}
