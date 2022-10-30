package com.bluejungle.dictionary;
/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/ServerDictionaryTestSuite.java#1 $
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * This suite tests the dictionary component.
 */
public class ServerDictionaryTestSuite extends DictionaryTestWithDataSource {

    public static Test suite() throws Exception {
        new ServerDictionaryTestSuite().setUp();
        TestSuite suite = new TestSuite("Test for the Server-side Dictionary code");
        TestCase cleanup = new TestCase("Cleanup") {
            protected void runTest() throws Exception {
                IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );
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
                } finally {
                    if ( hs != null ) {
                        hs.close();
                    }
                }
            }
        };
        //$JUnit-BEGIN$
        suite.addTest(cleanup);
        suite.addTest(TestElementTypeSetup.suite());
        suite.addTest(TestEnrollmentSetup.suite());
        suite.addTest(TestPerEnrollmentExternalNames.suite());
        suite.addTest(TestConsistentEnrollmentDates.suite());
        suite.addTest(TestElementTimeRelation.suite());
        suite.addTest(TestStructuralGroups.suite());
        suite.addTest(TestEnumeratedGroups.suite());
        suite.addTest(TestEnrollmentSession.suite());
        suite.addTest(TestEnrollmentHistory.suite()); 
        suite.addTest(TestEnrollmentRollback.suite());
        suite.addTest(TestEnrollmentPurge.suite());
        suite.addTest(TestDictionaryNormalization.suite());
//        suite.addTest(EnrollmentDareDevil.suite());
        
        suite.addTest( new TestCase("Close the Dictionary") {
            protected void runTest() throws Exception {
                IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent( Dictionary.COMP_INFO );
                dictionary.close();
            }            
        });
        //$JUnit-END$
        return suite;
    }

}
