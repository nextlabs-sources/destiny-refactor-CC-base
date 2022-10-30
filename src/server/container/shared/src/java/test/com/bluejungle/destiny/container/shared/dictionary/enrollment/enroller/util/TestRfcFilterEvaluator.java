/*
 * Created on Apr 6, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPAttribute;

import junit.framework.TestCase;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/util/TestRfcFilterEvaluator.java#1 $
 */

public class TestRfcFilterEvaluator extends TestCase {

    private LDAPEntry entry1;
    private LDAPEntry entry2;
    private LDAPEntry entry3;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestRfcFilterEvaluator.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        LDAPAttributeSet attrs1 = new LDAPAttributeSet();
        attrs1.add( new LDAPAttribute("cn", "Tom") );
        attrs1.add( new LDAPAttribute("objectclass", "user") );
        attrs1.add( new LDAPAttribute("objectclass", "person") );
        entry1 = new LDAPEntry("cn=Tom,dc=engineer,dc=bluejungle,dc=com", attrs1);

        LDAPAttributeSet attrs2 = new LDAPAttributeSet();
        attrs2.add( new LDAPAttribute("cn", "Jimmy") );
        attrs2.add( new LDAPAttribute("objectclass", "person") );
        entry2 = new LDAPEntry("cn=Jimmy,dc=engineer,dc=bluejungle,dc=com", attrs2);

        LDAPAttributeSet attrs3 = new LDAPAttributeSet();
        attrs3.add( new LDAPAttribute("cn", "Java Printer") );
        attrs3.add( new LDAPAttribute("objectclass", "printer") );
        attrs3.add( new LDAPAttribute("location", "Office 111") );
        entry3 = new LDAPEntry("cn=Java Printer,dc=engineer,dc=bluejungle,dc=com", attrs3);
      
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for TestADEnrollmentWrapperImpl.
     * @param arg0
     */
    public TestRfcFilterEvaluator(String arg0) {
        super(arg0);
        
    }

    public void testEqualMatch() throws Exception {
        try {
            RfcFilterEvaluator filter = new RfcFilterEvaluator("objectclass=user");
            assertTrue( filter.evaluate(entry1) );
            assertFalse( filter.evaluate(entry2) );
        }
        catch ( Exception e ) {
            System.out.println(e);
        }
    }

    public void testPresent() throws Exception {
        try {
            RfcFilterEvaluator filter = new RfcFilterEvaluator("location=*");
            assertTrue( filter.evaluate(entry3) );
            assertFalse( filter.evaluate(entry2) );
        }
        catch ( Exception e ) {
            System.out.println(e);
        }
    }


    public void testAndFilter() throws Exception {
        try {
            RfcFilterEvaluator filter = new RfcFilterEvaluator("(&(objectclass=user)(cn=tom))");
            assertTrue( filter.evaluate(entry1) );
            assertFalse( filter.evaluate(entry2) );
        }
        catch ( Exception e ) {
            System.out.println(e);
        }
    }

    public void testORFilter() throws Exception {
        try {
            RfcFilterEvaluator filter = new RfcFilterEvaluator("(|(objectclass=printer)(cn=Tom))");
            assertTrue( filter.evaluate(entry1) );
            assertFalse( filter.evaluate(entry2) );
            assertTrue( filter.evaluate(entry3) );
        }
        catch ( Exception e ) {
            System.out.println(e);
        }
    }

    public void testNotFilter() throws Exception {
        try {
            RfcFilterEvaluator filter = new RfcFilterEvaluator("(!(objectclass=user))");
            assertTrue( filter.evaluate(entry3) );
            assertTrue( filter.evaluate(entry2) );
            assertFalse( filter.evaluate(entry1) );
        }
        catch ( Exception e ) {
            System.out.println(e);
        }
    }

}
