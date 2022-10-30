/*
 * Created on Apr 13, 2011
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2011 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.util.Collections;
import java.util.Date;

import junit.framework.TestSuite;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestDictionaryNormalization.java#1 $
 */

public class TestDictionaryNormalization extends AbstractDictionaryTest{
    
    public static TestSuite suite() {
        return new TestSuite(TestDictionaryNormalization.class);
    }

    @Override
    public void testSetupDictionary() throws Exception {
        init();
    }
    
    private static final String FIRST_NAME_VALUE = "first";
    private static final String LAST_NAME_VALUE = "last";
    
    public void testNormalization() throws DictionaryException {
        updateEnrollmentProperty(enrollments[0]);
        updateEnrollmentProperty(enrollments[1]);
        
        final String uniqueName = "veryUniqueName";
        
        final DictionaryKey dictionaryKey1 = new DictionaryKey(new byte[] { 1 });
        final DictionaryPath dictionaryPath1 = new DictionaryPath(new String[]{"1"});
        final DictionaryKey dictionaryKey2 = new DictionaryKey(new byte[] { 2 });
        final DictionaryPath dictionaryPath2 = new DictionaryPath(new String[]{"2"});
        Long key1, key2;
        
        IMElement element1 = enrollments[0].makeNewElement(
                dictionaryPath1
              , userStruct.type
              , dictionaryKey1
        );
        element1.setUniqueName(uniqueName);
        element1.setValue(userStruct.firstName, FIRST_NAME_VALUE);
        saveElement(enrollments[0], element1);
        key1 = element1.getInternalKey();
        
        //check enrollment1
        
        Date asOf = null;
        
        checkNoNormalize1(dictionary.getByUniqueName(uniqueName, asOf));
        checkNoNormalize1(dictionary.getByKey(key1, asOf));
        checkNoNormalize1(dictionary.getElement(key1, asOf));
        checkNoNormalize1(dictionary.getElement(uniqueName, asOf));
        checkNoNormalize1(enrollments[0].getByKey(dictionaryKey1, asOf));
        checkNoNormalize1(enrollments[0].getElement(dictionaryKey1, asOf));
        checkNoNormalize1(dictionary.getElement(uniqueName, asOf));
        IDictionaryIterator<IMElement> iter = dictionary.query(
                dictionary.condition(enrollments[0])
              , asOf
              , null // order
              , null // page
        );
        assertNotNull(iter);
        try {
            assertTrue(iter.hasNext());
            checkNoNormalize1(iter.next());
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
        
        
        
        
        // second enrollment
        IMElement element2 = enrollments[1].makeNewElement(
                dictionaryPath2
              , userStruct.type
              , dictionaryKey2
        );
        element2.setUniqueName(uniqueName);
        element2.setValue(userStruct.lastName, LAST_NAME_VALUE);
        saveElement(enrollments[1], element2);
        key2 = element2.getInternalKey();
        
        try {
            dictionary.getByUniqueName(uniqueName, asOf);
            fail();
        } catch (DictionaryException e) {
            // can't find by unique name anymore since there is more than one
        }
        
        try {
            dictionary.getElement(uniqueName, asOf);
            fail();
        } catch (DictionaryException e) {
            // can't find by unique name anymore since there is more than one
        }
        
        checkNormalizated(dictionary.getByKey(key1, asOf));
        checkNormalizated(dictionary.getElement(key1, asOf));
        checkNoNormalize1(enrollments[0].getByKey(dictionaryKey1, asOf));
        checkNoNormalize1(enrollments[0].getElement(dictionaryKey1, asOf));
        iter = dictionary.query(
                dictionary.condition(enrollments[0])
              , asOf
              , null // order
              , null // page
        );
        assertNotNull(iter);
        try {
            assertTrue(iter.hasNext());
            checkNormalizated(iter.next());
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
        
        checkNormalizated(dictionary.getByKey(key2, asOf));
        checkNormalizated(dictionary.getElement(key2, asOf));
        checkNoNormalize2(enrollments[1].getByKey(dictionaryKey2, asOf));
        checkNoNormalize2(enrollments[1].getElement(dictionaryKey2, asOf));
        iter = dictionary.query(
                dictionary.condition(enrollments[1])
              , asOf
              , null // order
              , null // page
        );
        assertNotNull(iter);
        try {
            assertTrue(iter.hasNext());
            checkNormalizated(iter.next());
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
    }
    
    private void updateEnrollmentProperty( IEnrollment enrollment ) throws DictionaryException {
        enrollment.setStrProperty(userStruct.firstName.getName(), "external.firstname");
        enrollment.setStrProperty(userStruct.lastName.getName(), "external.lastname");
        enrollment.setExternalName(userStruct.firstName, "external.firstname");
        enrollment.setExternalName(userStruct.lastName, "external.lastname");
        
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveEnrollment(enrollment);
            session.commit();
        } finally {
            session.close();
        }
    }
    
    private void saveElement(IEnrollment enrollment, IMElement element)
            throws DictionaryException {
        IEnrollmentSession session = enrollment.createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(Collections.singleton(element));
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
    }
    
    private void checkNoNormalize1(IMElementBase element) {
        assertNotNull(element);
        assertTrue(element instanceof LeafElement);
        
        assertEquals(FIRST_NAME_VALUE, ((LeafElement)element).getValue(userStruct.firstName));
        assertNull(((LeafElement)element).getValue(userStruct.lastName));
    }
    
    private void checkNoNormalize2(IMElementBase element) {
        assertNotNull(element);
        assertTrue(element instanceof LeafElement);
        
        assertNull(((LeafElement)element).getValue(userStruct.firstName));
        assertEquals(LAST_NAME_VALUE, ((LeafElement)element).getValue(userStruct.lastName));
    }
    
    private void checkNormalizated(IMElementBase element) {
        assertNotNull(element);
        assertTrue(element instanceof LeafElement);
        
        assertEquals(FIRST_NAME_VALUE, ((LeafElement)element).getValue(userStruct.firstName));
        assertEquals(LAST_NAME_VALUE, ((LeafElement)element).getValue(userStruct.lastName));
    }

}
