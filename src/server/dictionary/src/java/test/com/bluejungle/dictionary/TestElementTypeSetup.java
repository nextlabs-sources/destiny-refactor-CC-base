package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestElementTypeSetup.java#1 $
 */

import java.util.Collection;
import java.util.Date;

import com.bluejungle.framework.comp.ComponentManagerFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

/**
 * These tests check the element type setup of the dictionary.
 */
public class TestElementTypeSetup extends TestCase {

    private IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );

    public static TestSuite suite() {
        return new TestSuite(TestElementTypeSetup.class);
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

    public void testObtainSession() throws DictionaryException {
        IConfigurationSession session = dictionary.createSession();
        assertNotNull(session);
        assertFalse( session.hasActiveTransaction() );
        session.beginTransaction();
        assertTrue( session.hasActiveTransaction() );
        session.rollback();
        assertFalse( session.hasActiveTransaction() );
        session.close();
        try {
            session.beginTransaction();
            fail("Operations on closed sessions must result in errors");
        } catch ( DictionaryException expected ) {
        }
    }

    public void testAddFields() throws DictionaryException {
        IMElementType type = dictionary.makeNewType("testType");
        assertNotNull( type );
        IElementField field1 = type.addField("field1", ElementFieldType.STRING );
        assertNotNull( field1 );
        IElementField field2 = type.addField("field2", ElementFieldType.NUMBER );
        assertNotNull( field2 );
        IElementField field3 = type.addField("field3", ElementFieldType.DATE );
        assertNotNull( field3 );
        IElementField field4 = type.addField("field4", ElementFieldType.CS_STRING );
        assertNotNull( field4 );
        IElementField field5 = type.addField("field5", ElementFieldType.STRING_ARRAY );
        assertNotNull( field5 );
        IElementField field6 = type.addField("field6", ElementFieldType.NUM_ARRAY );
        assertNotNull( field6 );
        Collection<IElementField> allFields = type.getFields();
        assertNotNull(allFields);
        assertEquals(6, allFields.size());
        IElementField[] fieldArray = allFields.toArray(new IElementField[allFields.size()]);
        assertEquals( "field1", fieldArray[0].getName() );
        assertEquals( "field2", fieldArray[1].getName() );
        assertEquals( "field3", fieldArray[2].getName() );
        assertEquals( "field4", fieldArray[3].getName() );
        assertEquals( "field5", fieldArray[4].getName() );
        assertEquals( "field6", fieldArray[5].getName() );
        assertEquals( ElementFieldType.STRING, fieldArray[0].getType() );
        assertEquals( ElementFieldType.NUMBER, fieldArray[1].getType() );
        assertEquals( ElementFieldType.DATE, fieldArray[2].getType() );
        assertEquals( ElementFieldType.CS_STRING, fieldArray[3].getType() );
        assertEquals( ElementFieldType.STRING_ARRAY, fieldArray[4].getType() );
        assertEquals( ElementFieldType.NUM_ARRAY, fieldArray[5].getType() );
        String[] allFieldNames = type.getFieldNames();
        assertNotNull( allFieldNames );
        assertEquals( 6, allFieldNames.length );
        assertEquals( "field1", allFieldNames[0] );
        assertEquals( "field2", allFieldNames[1] );
        assertEquals( "field3", allFieldNames[2] );
        assertEquals( "field4", allFieldNames[3] );
        assertEquals( "field5", allFieldNames[4] );
        assertEquals( "field6", allFieldNames[5] );
    }

    public void testSaveFields() throws DictionaryException {
        IMElementType type = dictionary.makeNewType("testType");
        assertNotNull( type );
        type.addField("field1", ElementFieldType.STRING );
        type.addField("field2", ElementFieldType.NUMBER );
        type.addField("field3", ElementFieldType.DATE );
        type.addField("field4", ElementFieldType.CS_STRING );
        type.addField("field5", ElementFieldType.STRING_ARRAY );
        type.addField("field6", ElementFieldType.NUM_ARRAY );
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveType( type );
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testSetLabel() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("field4");
        assertNotNull(field);
        type.setFieldLabel(field,"label");
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveType( type );
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testSavedLabel() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("field4");
        assertNotNull(field);
        assertEquals("label", field.getLabel());
    }

    public void testRenameField() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("field4");
        assertNotNull(field);
        type.renameField( field, "fieldA" );
        type.addField("fieldToDelete", ElementFieldType.DATE);
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveType( type );
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testRenamedFieldSaved() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("fieldA");
        assertNotNull(field);
    }

    public void testIllegalRename() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("fieldA");
        assertNotNull(field);
        try {
            type.renameField(field,"field2");
            fail("The renaming above should have failed.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testConflictWithDeletedUnsaved() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IElementField field = type.getField("fieldToDelete");
        assertNotNull(field);
        type.deleteField(field);
        assertSame( type.addField("fieldToDelete", field.getType()), field );
        type.deleteField(field);
        IElementField toRename = type.addField("toRename", ElementFieldType.DATE);
        try {
            type.renameField( toRename, "fieldToDelete");
            fail("The rename operation above should have failed.");
        } catch ( IllegalArgumentException expected ) {
        }
        type.deleteField(toRename);
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveType( type );
            session.commit();
        } finally {
            session.close();
        }
    }

    public void testConflictWithDeletedSaved() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        try {
            type.getField("fieldToDelete");
            fail("Deleted fields should not be returned.");
        } catch (IllegalArgumentException expected ) {
        }
        IElementField toRename = type.addField("toRename", ElementFieldType.DATE);
        try {
            type.renameField( toRename, "fieldToDelete");
            fail("The rename operation above should have failed.");
        } catch ( IllegalArgumentException expected ) {
        }
    }

    public void testDeleteRestoreCycle() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        IConfigurationSession session = dictionary.createSession();
        try {
            for ( int i = 0 ; i != 10 ; i++ ) {
                IElementField f = type.addField("deleteMe", ElementFieldType.CS_STRING);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                type.deleteField(f);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                IElementField g = type.addField("deleteMe", ElementFieldType.NUMBER);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                type.deleteField(g);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                IElementField h = type.addField("deleteMe", ElementFieldType.DATE);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                type.deleteField(h);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                IElementField k = type.addField("deleteMe", ElementFieldType.NUM_ARRAY);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
                type.deleteField(k);
                session.beginTransaction();
                session.saveType( type );
                session.commit();
            }
        } finally {
            session.close();
        }
    }

    public void testAddDuplicateType() throws DictionaryException {
        IMElementType type = dictionary.makeNewType("testType");
        assertNotNull( type );
        IConfigurationSession session = dictionary.createSession();
        try {
            session.beginTransaction();
            session.saveType( type );
            session.commit();
            fail("One should not be able to save a duplicate type.");
        } catch ( DictionaryException expected ) {
        } finally {
            session.close();
        }
    }

    public void testAddDuplicateFields() throws DictionaryException {
        IMElementType type = dictionary.makeNewType("ThisShouldNeverBeSaved");
        assertNotNull( type );
        type.addField("field1", ElementFieldType.STRING );
        try {
            type.addField("field1", ElementFieldType.STRING );           
        } catch ( IllegalArgumentException expected ) {
            // Fix for bug 4085, support duplicate name mapping
            fail("One should be able to save a type with duplicate fields.");
        }
    }

    public void testRetrieveFields() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        assertNotNull( type );
        Collection<IElementField> allFields = type.getFields();
        assertNotNull( allFields );
        assertEquals( 6, allFields.size() );
        IElementField[] fieldArray = allFields.toArray( new IElementField[allFields.size()] );
        assertEquals( "field1", fieldArray[0].getName() );
        assertEquals( "field2", fieldArray[1].getName() );
        assertEquals( "field3", fieldArray[2].getName() );
        assertEquals( "field5", fieldArray[3].getName() );
        assertEquals( "field6", fieldArray[4].getName() );
        assertEquals( "fieldA", fieldArray[5].getName() );
        assertEquals( ElementFieldType.STRING, fieldArray[0].getType() );
        assertEquals( ElementFieldType.NUMBER, fieldArray[1].getType() );
        assertEquals( ElementFieldType.DATE, fieldArray[2].getType() );
        assertEquals( ElementFieldType.STRING_ARRAY, fieldArray[3].getType() );
        assertEquals( ElementFieldType.NUM_ARRAY, fieldArray[4].getType() );
        assertEquals( ElementFieldType.CS_STRING, fieldArray[5].getType() );
    }

    public void testUnsavedMapping() throws DictionaryException {
        IMElementType type = dictionary.makeNewType("UnsavedType");
        assertNotNull( type );
        IElementField field1 = type.addField("field1", ElementFieldType.STRING );
        assertNotNull( field1 );
        IElementField field2 = type.addField("field2", ElementFieldType.NUMBER );
        assertNotNull( field2 );
        IElementField field3 = type.addField("field3", ElementFieldType.DATE );
        assertNotNull( field3 );
        IElementField field4 = type.addField("fieldA", ElementFieldType.CS_STRING );
        assertNotNull( field4 );
        DictionaryKey key = new DictionaryKey(new byte[] {1, 2, 3, 4, 5, 6, 7});
        LeafElement e = new LeafElement( new DictionaryPath( new String[] {"a"} ), type, key, null);
        e.setString00("sval_1");
        e.setNumber00(new Long(1));
        Date now = new Date();
        e.setDate00(now);
        assertEquals( "sval_1", field1.getValue(e) );
        assertEquals( new Long(1), field2.getValue(e) );
        assertEquals( now, field3.getValue(e) );
    }

    public void testSavedMapping() throws DictionaryException {
        IMElementType type = dictionary.getType("testType");
        // Field retrieval is case-insensitive
        IElementField field1 = type.getField("Field1");
        assertNotNull(field1);
        IElementField field2 = type.getField("FIELD2");
        assertNotNull(field2);
        IElementField field3 = type.getField("fIeLd3");
        assertNotNull(field3);
        IElementField fieldA = type.getField("fieldA");
        assertNotNull(fieldA);
        DictionaryKey key = new DictionaryKey(new byte[] {7, 6, 5, 4, 3, 2, 1});
        LeafElement e = new LeafElement( new DictionaryPath( new String[] {"a"} ), type, key, null);
        e.setString00("sval_1");
        e.setNumber00(new Long(2));
        Date now = new Date();
        e.setDate00(now);
        assertEquals( "sval_1", field1.getValue(e) );
        assertEquals( new Long(2), field2.getValue(e) );
        assertEquals( now, field3.getValue(e) );
    }

}
