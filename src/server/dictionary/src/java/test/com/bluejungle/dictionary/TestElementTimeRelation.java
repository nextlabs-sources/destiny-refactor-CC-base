package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestElementTimeRelation.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestSuite;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;

/**
 * These tests verify that time-relation implementation works.
 */
public class TestElementTimeRelation extends AbstractDictionaryTest {

    private static final int NUM_USERS = 20;
    private static final int NUM_CONTACTS = 5;
    private static final int NUM_HOSTS = 30;
    private static final int NUM_APPS = 10;
    private static final int NUM_REFS = 100;

    private static final String EMAIL_DOMAIN = "com.bluejungle";

    private static Date beforeUpdate = null;

    public static TestSuite suite() {
        return new TestSuite(TestElementTimeRelation.class);
    }

    public void testSetupDictionary() throws Exception {
        init();
    }

    public void testSeedInitialData() throws DictionaryException {
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);

        List<IElementBase> userList = new ArrayList<IElementBase>(NUM_USERS);
        for ( int i = 0 ; i != NUM_USERS ; i++ ) {
            DictionaryKey key = new DictionaryKey( new byte[] {0, (byte)(i/100), (byte)(i%100)});
            IMElement element = enrollments[0].makeNewElement(
                new DictionaryPath(
                    new String[] {"com", "bluejungle", "user-"+i}
                )
            ,   userStruct.type
            ,   key
            );
            assertNotNull(element);
            element.setValue(userStruct.firstName, "firstname"+i);
            element.setValue(userStruct.lastName, "lastname"+i);
            element.setValue(userStruct.dateOfBirth, new Date(i+1000000));
            element.setValue(userStruct.employeeCode, new Long(100+i*8));
            element.setValue(userStruct.email, "email"+i+"@"+EMAIL_DOMAIN);
            element.setValue(userStruct.aliases, new String[] {"a"+i, "b"+i, "", "c"+i});
            element.setValue(userStruct.numbers, new long[] {i, 2*i+1, 3*i+i, i*i});
            element.setValue(userStruct.whenStarted, new Date(i+2000000));
            element.setValue(userStruct.addr1, "addr1_"+i);
            element.setValue(userStruct.addr2, "addr2_"+i);
            element.setValue(userStruct.addr3, "addr3_"+i);
            element.setValue(userStruct.addrCountry, "country"+i);
            element.setValue(userStruct.addrPcode,"12345-"+i);
            element.setUniqueName("unique-user-"+i);
            element.setDisplayName("Display: User "+i);
            try {
                element.getInternalKey();
                fail("The internal key should have not been assigned");
            } catch (IllegalStateException expected) {
            }
            userList.add(element);
        }
        
        List<IElementBase> contactList = new ArrayList<IElementBase>(NUM_CONTACTS);
        for ( int i = 0 ; i != NUM_CONTACTS ; i++ ) {
            DictionaryKey key = new DictionaryKey( new byte[] {1, (byte)(i/100), (byte)(i%100)});
            IMElement element = enrollments[0].makeNewElement(
                new DictionaryPath(
                    new String[] {"com", "bluejungle", "contact-"+i}
                )
            ,   contactStruct.type
            ,   key
            );
            assertNotNull(element);
            element.setValue(contactStruct.contactFirstName, "contactFirstname"+i);
            element.setValue(contactStruct.contactLastName, "contactLastname"+i);
            element.setValue(contactStruct.contactEmail, "contactEmail"+i+"@"+EMAIL_DOMAIN);
            element.setValue(contactStruct.contactAliases, new String[] {"a"+i, "b"+i, "", "c"+i});
            try {
                element.getInternalKey();
                fail("The internal key should have not been assigned");
            } catch (IllegalStateException expected) {
            }
            contactList.add(element);
        }

        List<IElementBase> hostList = new ArrayList<IElementBase>(NUM_HOSTS);
        for ( int i = 0 ; i != NUM_HOSTS ; i++ ) {
            DictionaryKey key = new DictionaryKey( new byte[] {2, (byte)(i/100), (byte)(i%100)});
            IMElement element = enrollments[0].makeNewElement(
            new DictionaryPath(
                new String[] {"com", "bluejungle", "host-"+i}
            )
            ,   hostStruct.type
            ,   key
            );
            assertNotNull(element);
            element.setValue(hostStruct.hostName, "hostName"+i);
            element.setValue(hostStruct.hostDomain, "hostDomain"+i);
            element.setValue(hostStruct.hostOs, i%2==0 ? "Windows" : "Solaris");
            element.setValue(hostStruct.cpuCount, new Long(i%4+1));
            element.setValue(hostStruct.nicCount, new Long(i%2+1));
            element.setValue(hostStruct.controlPort, new Long(10000+i));
            element.setValue(hostStruct.assetNumber, "ACN#"+i);
            element.setValue(hostStruct.patchLevel, new Long(i*7+1));
            element.setValue(hostStruct.location, "loc_"+i);
            element.setValue(hostStruct.hostCountry, "country"+i);
            try {
                element.getInternalKey();
                fail("The internal key should have not been assigned");
            } catch (IllegalStateException expected) {
            }
            hostList.add(element);
        }

        List<IElementBase> appList = new ArrayList<IElementBase>(NUM_APPS);
        for ( int i = 0 ; i != NUM_APPS ; i++ ) {
            DictionaryKey key = new DictionaryKey( new byte[] {3, (byte)(i/100), (byte)(i%100)});
            IMElement element = enrollments[0].makeNewElement(
            new DictionaryPath(
                new String[] {"com", "bluejungle", "application-"+i}
            )
            ,   appStruct.type
            ,   key
            );
            assertNotNull(element);
            element.setValue(appStruct.appName, "appName"+i);
            element.setValue(appStruct.appImage, "exe-name-"+i);
            element.setValue(appStruct.appManufacturer, "BlueJungle "+i/10);
            element.setValue(appStruct.appVersion, new Long(100+i*8));
            element.setValue(appStruct.appFingerprint, "fingerprint:"+i*13);
            element.setValue(appStruct.appImportDate, new Date(i+3000000));
            try {
                element.getInternalKey();
                fail("The internal key should have not been assigned");
            } catch (IllegalStateException expected) {
            }
            appList.add(element);
        }

        try {
            session.beginTransaction();
            session.saveElements(userList);
            session.saveElements(contactList);
            session.saveElements(hostList);
            session.saveElements(appList);
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }

        for (IElementBase element : userList) {
            assertNotNull(element);
            assertNotNull(element.getInternalKey());
            assertNotNull(element.getExternalKey());
        }
        
        for (IElementBase element : contactList) {
            assertNotNull(element);
            assertNotNull(element.getInternalKey());
            assertNotNull(element.getExternalKey());
        }

        for (IElementBase element : hostList) {
            assertNotNull(element);
            assertNotNull(element.getInternalKey());
            assertNotNull(element.getExternalKey());
        }

        for (IElementBase element : appList) {
            assertNotNull(element);
            assertNotNull(element.getInternalKey());
            assertNotNull(element.getExternalKey());
        }

    }

    public void testReadByType() throws DictionaryException {
        assertNotNull(dictionary);
        IDictionaryIterator<IMElement> userIter = dictionary.query(
            dictionary.condition(userStruct.type), null, null, null
        );
        assertNotNull(userIter);
        IDictionaryIterator<IMElement> contactIter = dictionary.query(
            dictionary.condition(contactStruct.type), null, null, null
        );
        assertNotNull(contactIter);
        IDictionaryIterator<IMElement> hostIter = dictionary.query(
            dictionary.condition(hostStruct.type), null, null, null
        );
        assertNotNull(hostIter);
        IDictionaryIterator<IMElement> appIter = dictionary.query(
            dictionary.condition(appStruct.type), null, null, null
        );
        assertNotNull(appIter);
        int userCount = 0;
        try {
            while ( userIter.hasNext() ) {
                IElement user = userIter.next();
                assertNotNull(user);
                long empCode = (Long)user.getValue(userStruct.employeeCode);
                long seq = (empCode-100)/8;
                String[] aliasData = (String[])user.getValue(userStruct.aliases);
                assertNotNull(aliasData);
                assertEquals(4, aliasData.length);
                assertEquals("a"+seq, aliasData[0]);
                assertEquals("b"+seq, aliasData[1]);
                assertEquals("", aliasData[2]);
                assertEquals("c"+seq, aliasData[3]);
                long[] numData = (long[])user.getValue(userStruct.numbers);
                assertNotNull(numData);
                assertEquals(4, numData.length);
                assertEquals(seq, numData[0]);
                assertEquals(2*seq+1, numData[1]);
                assertEquals(3*seq+seq, numData[2]);
                assertEquals(seq*seq, numData[3]);
                userCount++;
            }
            assertEquals(NUM_USERS, userCount);
        } finally {
            userIter.close();
        }
        int contactCount = 0;
        try {
            while ( contactIter.hasNext() ) {
                IElement contact = contactIter.next();
                assertNotNull(contact);
                String[] contactAliasData = (String[])contact.getValue(contactStruct.contactAliases);
                assertNotNull(contactAliasData);
                contactCount++;
            }
            assertEquals(NUM_CONTACTS, contactCount);
        } finally {
            contactIter.close();
        }
        int hostCount = 0;
        try {
            while ( hostIter.hasNext() ) {
                assertNotNull(hostIter.next());
                hostCount++;
            }
            assertEquals(NUM_HOSTS, hostCount);
        } finally {
            hostIter.close();
        }
        int appCount = 0;
        try {
            while ( appIter.hasNext() ) {
                assertNotNull(appIter.next());
                appCount++;
            }
            assertEquals(NUM_APPS, appCount);
        } finally {
            appIter.close();
        }
    }

    public void testProvisionalReferences() throws DictionaryException {
        List<DictionaryPath> paths = makeProvisionalPaths();
        List<IReferenceable> refs = enrollments[0].getProvisionalReferences(paths);
        assertNotNull(refs);
        assertEquals(NUM_REFS, refs.size());
        for ( IReferenceable element : refs) {
            assertNotNull(element);
            element.accept(checkIsReference);
        }
    }

    public void testReadingReferenceAndElementMix() throws DictionaryException {
        List<DictionaryPath> paths = makeProvisionalAndRealPaths();
        List<IReferenceable> refs = enrollments[0].getProvisionalReferences(paths);
        assertNotNull(refs);
        assertEquals(NUM_REFS+NUM_USERS+NUM_CONTACTS+NUM_HOSTS+NUM_APPS, refs.size());
        final int count[] = new int[2];
        for (IReferenceable element : refs) {
            assertNotNull(element);
            element.accept(new IElementVisitor() {
                public void visitGroup(IMGroup group) {
                    fail("A group is returned where an element or a reference were expected.");
                }
                public void visitLeaf(IMElement element) {
                    count[0]++;
                }
                public void visitProvisionalReference( IReferenceable ref ) {
                    count[1]++;
                }
            });
        }
        assertEquals(NUM_USERS+NUM_CONTACTS+NUM_HOSTS+NUM_APPS, count[0]);
        assertEquals(NUM_REFS, count[1]);
    }

    public void testOverrideProvisionalReferences() throws DictionaryException {
        List<DictionaryPath> paths = makeProvisionalPaths();
        int cnt = 0;
        List<IElementBase> toSave = new ArrayList<IElementBase>(paths.size());
        for (DictionaryPath path  : paths) {
            DictionaryKey key = new DictionaryKey( new byte[] {4, (byte)(cnt/100), (byte)(cnt%100)});
            switch( cnt%4 ) {
            case 0:
                toSave.add( enrollments[0].makeNewElement(path, userStruct.type, key));
                break;
            case 1:
                toSave.add( enrollments[0].makeNewElement(path, contactStruct.type, key));
                break;
            case 2:
                toSave.add( enrollments[0].makeNewElement(path, hostStruct.type, key));
                break;
            case 3:
                toSave.add( enrollments[0].makeNewElement(path, appStruct.type, key));
                break;
            }
            cnt++;
        }
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(toSave);
            session.commit();
        } catch ( DictionaryException problem ) {
            session.rollback();
            fail(problem.getMessage());
        } finally {
            session.close(true, null);
        }
    }

    public void testOverridingPathsSucceeded() throws DictionaryException {
        List<DictionaryPath> paths = makeProvisionalAndRealPaths();
        List<IReferenceable> refs = enrollments[0].getProvisionalReferences(paths);
        assertNotNull(refs);
        assertEquals(NUM_REFS+NUM_USERS+NUM_CONTACTS+NUM_HOSTS+NUM_APPS, refs.size());
        for (IReferenceable element : refs) {
            assertNotNull(element);
            assertTrue(element instanceof DictionaryElementBase);
            assertNotNull(((DictionaryElementBase)element).getInternalKey());
        }
    }

    public void testUpdateExistingElements() throws DictionaryException {
        beforeUpdate = new Date(System.currentTimeMillis()-1);
        IDictionaryIterator<IMElement> userIter = dictionary.query(
            dictionary.condition(userStruct.type), beforeUpdate, null, null
        );
        List<IElementBase> toSave = new ArrayList<IElementBase>(NUM_USERS);
        try {
            int i = 0;
            while( userIter.hasNext() ) {
                IMElement element = userIter.next();
                element.setValue(userStruct.addr1,"UPDATED-addr1:"+i++);
                toSave.add(element);
            }
        } finally {
            userIter.close();
        }
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(toSave);
            session.commit();
        } catch ( DictionaryException problem ) {
            session.rollback();
            fail(problem.getMessage());
        } finally {
            session.close(true, null);
        }
    }

    public void testAsOfQueryInThePast() throws DictionaryException {
        IDictionaryIterator<IMElement> userIter = dictionary.query(
            dictionary.condition(userStruct.type), beforeUpdate, null, null
        );
        try {
            int i = 0;
            while( userIter.hasNext() ) {
                IMElement element = userIter.next();
                String addr = (String)element.getValue(userStruct.addr1);
                if ( addr != null ) {
                    assertEquals(-1, addr.indexOf('U'));
                    i++;
                }
            }
            assertTrue( i != 0 );
        } finally {
            userIter.close();
        }
    }

    public void testUpdateSucceeded() throws DictionaryException {
        IDictionaryIterator<IMElement> userIter = dictionary.query(
            dictionary.condition(userStruct.type), null, null, null
        );
        try {
            int i = 0;
            while( userIter.hasNext() ) {
                IMElement element = userIter.next();
                String addr = (String)element.getValue(userStruct.addr1);
                if ( addr != null ) {
                    assertEquals(0, addr.indexOf('U'));
                    i++;
                }
            }
            assertTrue( i != 0 );
        } finally {
            userIter.close();
        }
    }

    public void testQueryByNumber() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.LESS_THAN_EQUALS
            ,   userStruct.employeeCode
            ,   Constant.build(116)
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue(new Long(116).compareTo((Long) element.getValue(userStruct.employeeCode)) >= 0);
            }
            assertEquals(3, count);
        } finally {
            iter.close();
        }
        iter = dictionary.query(
            new Relation(
                RelationOp.NOT_EQUALS
            ,   userStruct.employeeCode
            ,   Constant.build(116)
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertFalse(new Long(116).equals(element.getValue(userStruct.employeeCode)));
            }
            assertEquals(NUM_USERS-1, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryByDate() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.GREATER_THAN
            ,   userStruct.dateOfBirth
            ,   Constant.build(new Date(1000005))
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue(((Date)element.getValue(userStruct.dateOfBirth)).after(new Date(1000005)));
            }
            assertEquals(NUM_USERS-6, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryMixedFieldType() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new CompositePredicate(
                BooleanOp.OR
            ,   Arrays.asList( new IPredicate[] {
                    new Relation(
                        RelationOp.LESS_THAN_EQUALS
                    ,   Constant.build(10005)
                    ,   hostStruct.controlPort
                    )
                ,   new Relation(
                        RelationOp.GREATER_THAN
                    ,   userStruct.dateOfBirth
                    ,   Constant.build(new Date(1000005))
                    )
               }
            )
        )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                if (element.getType().equals(userStruct.type)) {
                    assertTrue(((Date)element.getValue(userStruct.dateOfBirth)).after(new Date(1000005)));
                } else if (element.getType().equals(hostStruct.type)) {
                    assertTrue(new Long(10005).compareTo((Long) element.getValue(hostStruct.controlPort)) <= 0);
                } else {
                    fail("Query returned an unexpected type");
                }
            }
            assertEquals(NUM_USERS+NUM_HOSTS-11, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryByCaseSensitiveString() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.NOT_EQUALS
            ,   userStruct.firstName
            ,   Constant.build("firstname1")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertFalse("firstname1".equals(element.getValue(userStruct.firstName)));
            }
            assertEquals(NUM_USERS-1, count);
        } finally {
            iter.close();
        }
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.firstName
            ,   Constant.build("FirstName1")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
    }

    public void testQueryByCaseSensitiveStringWithWildcards() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.firstName
            ,   Constant.build("firstname1*")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue(element.getValue(userStruct.firstName).toString().startsWith("firstname1"));
            }
            assertEquals(11, count); // 1, 10..19
        } finally {
            iter.close();
        }
        // % is not a wildcard
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.firstName
            ,   Constant.build("firstname%")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
        // escaped wildcard characters do not work as wildcards
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.firstName
            ,   Constant.build("firstname\\*")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
    }

    public void testQueryByCaseInsensitiveString() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.NOT_EQUALS
            ,   userStruct.lastName
            ,   Constant.build("LaStNaME1")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertFalse("LaStNaME1".equalsIgnoreCase((String)element.getValue(userStruct.lastName)));
            }
            assertEquals(NUM_USERS-1, count);
        } finally {
            iter.close();
        }
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.lastName
            ,   Constant.build("LASTNAME5")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue("LASTNAME5".equalsIgnoreCase((String)element.getValue(userStruct.lastName)));
            }
            assertEquals(1, count);
        } finally {
            iter.close();
        }
        // Now switch the order of operands - the result should be the same
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   Constant.build("LASTNAME5")
            ,   userStruct.lastName
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue("LASTNAME5".equalsIgnoreCase((String)element.getValue(userStruct.lastName)));
            }
            assertEquals(1, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryByCaseInsensitiveStringWithWildcards() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.NOT_EQUALS
            ,   userStruct.lastName
            ,   Constant.build("LaStNaME1*")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertFalse(((String)element.getValue(userStruct.lastName)).toLowerCase().startsWith("lastname1"));
            }
            assertEquals(NUM_USERS-11, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryByCaseSensitiveLike() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   userStruct.firstName
            ,   Constant.build("firstname*1*")
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue(((String)element.getValue(userStruct.firstName)).indexOf('1') != -1);
            }
            int need = 0;
            for ( int i = 0 ; i != NUM_USERS ; i++ ) {
                if ( (""+i).indexOf('1') != -1 ) {
                    need++;
                }
            }
            assertEquals(need, count);
        } finally {
            iter.close();
        }
        // Now switch the order of operands - the result should be the same
        iter = dictionary.query(
            new Relation(
                RelationOp.EQUALS
            ,   Constant.build("firstname*1*")
            ,   userStruct.firstName
            )
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertTrue(((String)element.getValue(userStruct.firstName)).indexOf('1') != -1);
            }
            int need = 0;
            for ( int i = 0 ; i != NUM_USERS ; i++ ) {
                if ( (""+i).indexOf('1') != -1 ) {
                    need++;
                }
            }
            assertEquals(need, count);
        } finally {
            iter.close();
        }
    }

    public void testSortedQuery() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            dictionary.condition(userStruct.type)
        ,   null
        ,   new Order[] { Order.ascending(userStruct.firstName) }
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            String lastKnown = null;
            while ( iter.hasNext() ) {
                IElement element = iter.next();
                if ( element.getValue(userStruct.dateOfBirth) != null ) {
                    count++;
                }
                String fn = (String)element.getValue(userStruct.firstName);
                if ( fn != null ) {
                    assertTrue(
                        "Output must be sorted in ascending order according to first name: "+lastKnown+", "+fn+"; count="+count
                    ,   lastKnown == null || lastKnown.compareTo(fn) <= 0
                    );
                    lastKnown = fn;
                }
            }
            assertEquals(NUM_USERS, count);
        } finally {
            iter.close();
        }
        iter = dictionary.query(
            dictionary.condition(userStruct.type)
        ,   null
        ,   new Order[] { Order.descending(userStruct.firstName) }
        ,   null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            String lastKnown = null;
            while ( iter.hasNext() ) {
                IElement element = iter.next();
                if ( element.getValue(userStruct.dateOfBirth) != null ) {
                    count++;
                }
                String fn = (String)element.getValue(userStruct.firstName);
                assertNotNull("Element at position "+count+" is not expected to be null", fn);
                assertTrue(
                    "Output must be sorted in descending order according to first name: "+lastKnown+", "+fn+"; count="+count
                ,   lastKnown == null || lastKnown.compareTo(fn) >= 0
                );
                lastKnown = fn;
            }
            assertEquals(NUM_USERS, count);
        } finally {
            iter.close();
        }
    }

    public void testPagedQuery() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            dictionary.condition(userStruct.type)
        ,   null
        ,   new Order[] { Order.ascending(userStruct.firstName) }
        ,   new Page(5, 5)
        );
        assertNotNull(iter);
        String[] names = new String[NUM_USERS];
        for ( int i = 0 ; i != NUM_USERS ; i++ ) {
            names[i] = "firstname"+i;
        }
        Arrays.sort(names);
        try {
            int count = 0;
            int pos = 5;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertEquals(
                    "Unexpected element at position "+pos
                ,   names[pos]
                ,   element.getValue(userStruct.firstName)
                );
                pos++;
            }
            assertEquals(5, count);
        } finally {
            iter.close();
        }
    }

    public void testPositiveQueryByEnrollment() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            dictionary.condition(enrollments[0]), null, null, null
        );
        assertNotNull(iter);
        try {
            int count = 0;
            while ( iter.hasNext() ) {
                count++;
                IElement element = iter.next();
                assertNotNull(element);
                assertEquals(enrollments[0], element.getEnrollment());
            }
            assertEquals(NUM_APPS+NUM_HOSTS+NUM_REFS+NUM_USERS+NUM_CONTACTS, count);
        } finally {
            iter.close();
        }
    }

    public void testNegativeQueryByEnrollment() throws DictionaryException {
        IDictionaryIterator<IMElement> iter = dictionary.query(
            new CompositePredicate(
                BooleanOp.NOT
            ,   dictionary.condition(enrollments[0]))
            ,   null
            ,   null
            ,   null
        );
        assertNotNull(iter);
        try {
            assertFalse(iter.hasNext());
        } finally {
            iter.close();
        }
    }

    public void testInternalKeyQuery() throws DictionaryException {
        IDictionaryIterator<Long> iter = dictionary.queryKeys(PredicateConstants.TRUE, null);
        try {
            Long last = new Long(-1);
            int count = 0;
            while (iter.hasNext()) {
                Long key = iter.next();
                assertTrue("Keys must be returned in ascending order", key.compareTo(last)>0);
                count++;
            }
            assertEquals(NUM_APPS+NUM_HOSTS+NUM_REFS+NUM_USERS+NUM_CONTACTS, count);
        } finally {
            iter.close();
        }
    }

    public void testQueryChanges() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMElement> ei = dictionary.query(
            dictionary.changedCondition(null, now)
        ,   now
        ,   null
        ,   null);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(45, counts[0]);
        assertEquals(30, counts[1]);
        assertEquals(55, counts[2]);
        assertEquals(35, counts[3]);
    }

    public void testQueryChangesSinceFirstUpdate() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMElement> ei = dictionary.query(
            dictionary.changedCondition(beforeUpdate, now)
        ,   now
        ,   null
        ,   null);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(45, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(0, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testQueryByUniqueNameTemplate() throws DictionaryException {
        IPredicate cond = dictionary.uniqueNameAttribute().buildRelation(
            RelationOp.EQUALS, Constant.build("unique-user-*0")
        );
        IDictionaryIterator<IMElement> ei = dictionary.query(cond, null, null, null);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(2, counts[0]); // that's user0 and user10
        assertEquals(0, counts[1]);
        assertEquals(0, counts[2]);
    }

    public void testQueryByUniqueName() throws DictionaryException {
        IMElement element = dictionary.getElement("Unique-user-0", null);
        assertNotNull(element);
        assertTrue("unique-user-0".equalsIgnoreCase(element.getUniqueName()));
        element = dictionary.getElement("undefined unique name",null);
        assertNull(element);
    }

    public void testQueryByDisplayName() throws DictionaryException {
        IPredicate cond = dictionary.displayNameAttribute().buildRelation(
            RelationOp.EQUALS, Constant.build("Display: User 5")
        );
        IDictionaryIterator<IMElement> ei = dictionary.query(cond, null, null, null);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(1, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(0, counts[2]);
    }

    public void testQueryNullByDisplayName() throws DictionaryException {
        IPredicate cond = new Relation(
            RelationOp.EQUALS
        ,   dictionary.displayNameAttribute()
        ,   Constant.NULL
        );
        IDictionaryIterator<IMElement> ei = dictionary.query(cond, null, null, null);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals((NUM_REFS+2)/4, counts[0]);
        assertEquals(NUM_CONTACTS+NUM_REFS/4, counts[1]);
        assertEquals(NUM_HOSTS+NUM_REFS/4, counts[2]);
        assertEquals(NUM_APPS+NUM_REFS/4, counts[3]);
    }

    public void testDuplicatePathHash() throws DictionaryException {
        // Variables path1 and path2 are carefully crafted
        // to have identical hash codes wile not being equal.
        DictionaryPath path1 = new DictionaryPath( new String[] {"ref-0", "b", "B"} );
        DictionaryPath path2 = new DictionaryPath( new String[] {"ref-0", "a", "a"} );
        assertEquals("path1 and path2 are expected to have the same hash code."
        ,   path1.hashCode()
        ,   path2.hashCode()
        );
        assertFalse("path1 and path2 are expected to be non-equal.", path1.equals(path2));
        IElement element = enrollments[0].makeNewElement(path1, userStruct.type, new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,9}));
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {element}));
            session.commit();
        } catch ( DictionaryException problem ) {
            session.rollback();
            fail(problem.getMessage());
        } finally {
            session.close(true, null);
        }
        Collection<IReferenceable> refs = enrollments[0].getProvisionalReferences(
            Arrays.asList(new DictionaryPath[] {path2})
        );
        assertNotNull(refs);
        assertEquals(1, refs.size());
        for (IReferenceable ref : refs) {
            assertNotNull(ref);
            assertEquals(path2, ref.getPath());
            ref.accept(checkIsReference);
        }
    }

    private static List<DictionaryPath> makeProvisionalPaths() {
        List<DictionaryPath> res = new ArrayList<DictionaryPath>(NUM_REFS);
        for ( int i = 0 ; i != NUM_REFS ; i++ ) {
            res.add(
                new DictionaryPath(
                    new String[] {"test", "provisional", "ref"+i}
                )
            );
        }
        return res;
    }

    private static List<DictionaryPath> makeProvisionalAndRealPaths() {
        List<DictionaryPath> res = makeProvisionalPaths();
        for ( int i = 0 ; i != NUM_USERS ; i++ ) {
            res.add( new DictionaryPath( new String[] {"com", "bluejungle", "user-"+i} ) );
        }
        
        for ( int i = 0 ; i != NUM_CONTACTS ; i++ ) {
            res.add( new DictionaryPath( new String[] {"com", "bluejungle", "contact-"+i} ) );
        }

        for ( int i = 0 ; i != NUM_HOSTS ; i++ ) {
            res.add( new DictionaryPath( new String[] {"com", "bluejungle", "host-"+i} ) );
        }

        for ( int i = 0 ; i != NUM_APPS ; i++ ) {
            res.add( new DictionaryPath( new String[] {"com", "bluejungle", "application-"+i} ) );
        }
        return res;
    }

    private final IElementVisitor checkIsReference = new IElementVisitor() {
        public void visitGroup(IMGroup group) {
            fail("A group is returned where a provisional reference is expected.");
        }
        public void visitLeaf(IMElement element) {
            fail("An element is returned where a provisional reference is expected.");
        }
        public void visitProvisionalReference( IReferenceable ref ) {
            // Expected
        }
    };
    
    public void testSpecialCharInDictionaryPath() throws DictionaryException {
        
        DictionaryPath spGroup = new DictionaryPath( new String[] {"test", "sp"} );
        DictionaryPath Goodpath = new DictionaryPath( new String[] {"test", "sp", ">ref,-0\\,\\>~"} );
        try {
            new DictionaryPath( new String[] {"r,ef-0", "test", "sp"} );
        }
        catch( Exception e) {
            assertEquals("path[0]", e.getMessage());
        }
        try {
            new DictionaryPath( new String[] {"r>ef-0", "test", "sp"} );
        }
        catch( Exception e) {
            assertEquals("path[0]", e.getMessage());
        }
        
        IMGroup group1 = enrollments[0].makeNewStructuralGroup(spGroup, 
                new DictionaryKey(new byte[] {2,0,3,4,5,6,7,8,9}));
        IElement element1 = enrollments[0].makeNewElement(Goodpath, userStruct.type, 
                new DictionaryKey(new byte[] {2,1,3,4,5,6,7,8,9}));
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {group1, element1}));
            session.commit();
        } catch ( DictionaryException problem ) {
            session.rollback();
            fail(problem.getMessage());
        } finally {
            session.close(true, null);
        }
        Collection<IReferenceable> refs = enrollments[0].getProvisionalReferences(
            Arrays.asList(new DictionaryPath[] {Goodpath})
        );
        assertNotNull(refs);
        assertEquals(1, refs.size());
        for (IReferenceable ref : refs) {
            assertNotNull(ref);
            assertEquals(Goodpath, ref.getPath());
        }
        
        IDictionaryIterator<IMElement> children = group1.getDirectChildElements();
        assertNotNull(children);
        try {
            IMElement element = children.next();
            assertNotNull(element);
        }
        finally {
            children.close();
        }
    }
}
