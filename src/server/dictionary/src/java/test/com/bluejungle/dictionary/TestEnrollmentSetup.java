/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnrollmentSetup.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * These tests check the enrollment setup of the dictionary.
 */
public class TestEnrollmentSetup extends TestCase {

    private IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );

    private static final byte[] BIN_VALUE;

    private static final String[] STR_VALUE = new String[] {"a", "bb", "ccc", "d:e:f", "g\\h:\\i\\\\\\j:k:L"};

    static {
        BIN_VALUE = new byte[256];
        for ( int i = 0 ; i != 256 ; i++ ) {
            int hi = i%15;
            int lo = i>>4;
            BIN_VALUE[i] = (byte)(hi^lo);
        }
    }

    public static TestSuite suite() {
        return new TestSuite(TestEnrollmentSetup.class);
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

    public void testNewEnrollment() throws DictionaryException {
        IEnrollment enrollment = dictionary.makeNewEnrollment("my.domain.name");
        assertNotNull(enrollment);
    }

    public void testSaveEnrollment() throws DictionaryException {
        IEnrollment enrollment = dictionary.makeNewEnrollment("my.domain.name");
        enrollment.setType("simple_type");
        assertNotNull(enrollment);
        saveEnrollment(enrollment);
    }

    public void testSaveDuplicateEnrollment() throws DictionaryException {
        IEnrollment enrollment = dictionary.makeNewEnrollment("my.domain.name");
        assertNotNull(enrollment);
        try {
            saveEnrollment(enrollment);
            fail("Saving duplicate enrollments should fail");
        } catch ( DictionaryException expected ) {
        }
    }

    public void testRollbackEnrollment() throws DictionaryException {
        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        try {
            session.rollback();
        } finally {
            session.close();
        }
    }

    public void testGetSavedEnrollment() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        assertEquals("my.domain.name", enrollment.getDomainName());
        assertEquals("simple_type", enrollment.getType());
    }

    public void testGetKnownEnrollments() throws DictionaryException {
        Collection<IEnrollment> known = dictionary.getEnrollments();
        assertNotNull(known);
        assertEquals(1, known.size());
        IEnrollment enrollmentObj = known.iterator().next();
        assertNotNull(enrollmentObj);
        assertEquals("my.domain.name", enrollmentObj.getDomainName());
    }

    public void testUnknownPropertyNames() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        try {
            enrollment.getStrProperty("unknown");
        } catch ( IllegalArgumentException expected ) {
            
        }
        try {
            enrollment.getNumProperty("unknown");
        } catch ( IllegalArgumentException expected ) {
            
        }
        try {
            enrollment.getBinProperty("unknown");
        } catch ( IllegalArgumentException expected ) {
            
        }
    }

    public void testAddStringProperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        enrollment.setStrProperty("str", "strVal");
        assertEquals("strVal", enrollment.getStrProperty("str"));
        String[] knownProps = enrollment.getStrPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("str", knownProps[0]);
        assertEquals(0, enrollment.getNumPropertyNames().length);
        assertEquals(0, enrollment.getBinPropertyNames().length);
        saveEnrollment(enrollment);
    }

    public void testReadStringPproperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        assertEquals("strVal", enrollment.getStrProperty("str"));
        String[] knownProps = enrollment.getStrPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("str", knownProps[0]);
        assertEquals(0, enrollment.getNumPropertyNames().length);
        assertEquals(0, enrollment.getBinPropertyNames().length);
    }

    public void testAddStringArrayProperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        enrollment.setStrArrayProperty("str[]", STR_VALUE);
        checkStrArray(STR_VALUE, enrollment.getStrArrayProperty("str[]"));
        String[] knownProps = enrollment.getStrArrayPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("str[]", knownProps[0]);
        saveEnrollment(enrollment);
    }

    public void testReadStringArrayPproperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        checkStrArray(STR_VALUE, enrollment.getStrArrayProperty("str[]"));
        String[] knownProps = enrollment.getStrArrayPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("str[]", knownProps[0]);
        assertEquals(0, enrollment.getNumPropertyNames().length);
        assertEquals(0, enrollment.getBinPropertyNames().length);
    }

    public void testDeleteProperty() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        enrollment.deleteProperty("str[]");
        assertEquals(0, enrollment.getStrArrayPropertyNames().length);
        saveEnrollment(enrollment);
        assertEquals(0, enrollment.getStrArrayPropertyNames().length);
    }

    public void testDeletePropertyHasWorked() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        assertEquals(0, enrollment.getStrArrayPropertyNames().length);
    }

    public void testAddNumProperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        enrollment.setNumProperty("num", 12345);
        assertEquals(12345L, enrollment.getNumProperty("num"));
        String[] knownProps = enrollment.getNumPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("num", knownProps[0]);
        assertEquals(1, enrollment.getStrPropertyNames().length);
        assertEquals(0, enrollment.getBinPropertyNames().length);
        saveEnrollment(enrollment);
    }

    public void testReadNumPproperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        assertEquals(12345L, enrollment.getNumProperty("num"));
        String[] knownProps = enrollment.getNumPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("num", knownProps[0]);
        assertEquals(1, enrollment.getStrPropertyNames().length);
        assertEquals(0, enrollment.getBinPropertyNames().length);
    }

    public void testAddBinProperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        enrollment.setBinProperty("bin", BIN_VALUE);
        checkByteArray(BIN_VALUE, enrollment.getBinProperty("bin"));
        String[] knownProps = enrollment.getBinPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("bin", knownProps[0]);
        assertEquals(1, enrollment.getStrPropertyNames().length);
        assertEquals(1, enrollment.getNumPropertyNames().length);
        saveEnrollment(enrollment);
    }

    public void testReadBinPproperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        checkByteArray(BIN_VALUE, enrollment.getBinProperty("bin"));
        String[] knownProps = enrollment.getBinPropertyNames();
        assertNotNull(knownProps);
        assertEquals(1, knownProps.length);
        assertEquals("bin", knownProps[0]);
        assertEquals(1, enrollment.getStrPropertyNames().length);
        assertEquals(1, enrollment.getNumPropertyNames().length);
    }

    public void testAddDuplicatePproperties() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        try {
            enrollment.setBinProperty("str", BIN_VALUE);
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testDeleteEnrollment() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNotNull(enrollment);
        Date lastestConsistTimeBefore = dictionary.getLatestConsistentTime();
        assertNotNull(lastestConsistTimeBefore);
        IEnrollmentSession es = enrollment.createSession();
        try {
            es.beginTransaction();
            IMGroup g = enrollment.makeNewStructuralGroup(
                new DictionaryPath("x|y|z".split("[|]")),
                new DictionaryKey(new byte[] {1, 2, 3, 4, 5, 6})
            );
            assertNotNull(g);
            es.saveElements(Arrays.asList(new IElementBase[] { g } ));
            es.commit();
        } finally {
            es.close(true, null);
        }
        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        try {
            session.deleteEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }
        
        Date lastestConsistTimeAfter = dictionary.getLatestConsistentTime();
        assertNotNull(lastestConsistTimeAfter);
        assertTrue(lastestConsistTimeBefore.before(lastestConsistTimeAfter));
    }

    public void testDeleteEnrollmentSucceeded() throws DictionaryException {
        IEnrollment enrollment = dictionary.getEnrollment("my.domain.name");
        assertNull(enrollment);
    }

    private void saveEnrollment( IEnrollment enrollment ) throws DictionaryException {
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }
    }

    private static void checkByteArray( byte[] a, byte[] b ) {
        assertNotNull(a);
        assertNotNull(b);
        assertEquals(a.length, b.length);
        for ( int i = 0 ; i != a.length ; i++ ) {
            assertEquals(a[i], b[i]);
        }
    }

    private static void checkStrArray( String[] a, String[] b ) {
        assertNotNull(a);
        assertNotNull(b);
        assertEquals(a.length, b.length);
        for ( int i = 0 ; i != a.length ; i++ ) {
            assertEquals(a[i], b[i]);
        }
    }


    public void testEnrollmentStatus() throws DictionaryException {
    	IEnrollment enrollment = dictionary.makeNewEnrollment("test.enrollment.status");
        assertNotNull(enrollment);
        
        saveEnrollment(enrollment);
        
        // Verify getStatus() method returns null ( not record )
        IUpdateRecord record = enrollment.getStatus();
        assertNull(record);
        
        String msg = "my enrollment succeed";
        IEnrollmentSession es = enrollment.createSession();
        try {
            es.beginTransaction();
            IMGroup g = enrollment.makeNewStructuralGroup(
                new DictionaryPath("x|y|z".split("[|]")),
                new DictionaryKey(new byte[] {1, 2, 3, 4, 5, 6})
            );
            assertNotNull(g);
            es.saveElements(Arrays.asList(new IElementBase[] { g } ));
            es.commit();
        } finally {
            es.close(true, msg);
        }
        
        // Verify getStatus() method returns same msg
        record = enrollment.getStatus();
        assertNotNull(record);
        assertEquals(msg, record.getErrorMessage());

        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        try {
            session.deleteEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }

    }

}
