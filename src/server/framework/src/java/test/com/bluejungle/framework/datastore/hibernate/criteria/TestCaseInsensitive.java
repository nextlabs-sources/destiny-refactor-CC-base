package com.bluejungle.framework.datastore.hibernate.criteria;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/test/com/bluejungle/framework/datastore/hibernate/criteria/TestCaseInsensitive.java#1 $
 */

import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the CaseInsensitiveLike class.
 */

public class TestCaseInsensitive extends TestCase {

    public static TestSuite suite() {
        return new TestSuite( TestCaseInsensitive.class );
    }

    public void testTemplate() {
        CaseInsensitiveLike cl = new CaseInsensitiveLike("a","\\%");
        assertEquals( "\\%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%%");
        assertEquals( "%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%\\%");
        assertEquals( "%\\%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%\\%\\%\\%");
        assertEquals( "%\\%\\%\\%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%\\%%%\\%");
        assertEquals( "%\\%%\\%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","\\%%");
        assertEquals( "\\%%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        assertEquals( "%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%b%%");
        assertEquals( "%b%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%%b%");
        assertEquals( "%b%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","%%%%%xyz");
        assertEquals( "%xyz", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","xyz%%%%%%%");
        assertEquals( "xyz%", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","12345");
        assertEquals( "12345", cl.getTemplate() );
        cl = new CaseInsensitiveLike("a","\u4eca\u5929");
        assertEquals( "\u4eca\u5929", cl.getTemplate() );
    }

    public void testBindStrings() {
        CaseInsensitiveLike cl = new CaseInsensitiveLike( "a","\\%" );
        equalArrays( new String[] {"\\%", "\\%" }, cl.getBindStrings() );
        cl = new CaseInsensitiveLike("a", "%" );
        equalArrays( new String[0], cl.getBindStrings() );
        cl = new CaseInsensitiveLike("a", "x" );
        equalArrays( new String[] { "X","x" }, cl.getBindStrings() );
        cl = new CaseInsensitiveLike("a", "xx" );
        equalArrays( new String[] { "xx", "xX", "Xx", "XX" }, cl.getBindStrings() );
        cl = new CaseInsensitiveLike("a", "xxx" );
        equalArrays( new String[] { "xx%", "xX%", "Xx%", "XX%", "xxx" }, cl.getBindStrings() );
        cl = new CaseInsensitiveLike("a", "x%x" );
        equalArrays( new String[] { "x%%", "X%%", "x%x" }, cl.getBindStrings() );
    }

    public void testCondition() {
        CaseInsensitiveLike cl = new CaseInsensitiveLike( "a","\\%" );
        assertEquals( "(a LIKE :x1 OR a LIKE :x2)", cl.getCondition( "a", args, "TO_LOWER" ) );
        cl = new CaseInsensitiveLike("a", "%" );
        assertEquals( "1=1", cl.getCondition( "a", args, "TO_LOWER" ) );
        cl = new CaseInsensitiveLike("a", "x" );
        assertEquals( "(a=:x1 OR a=:x2)", cl.getCondition( "a", args, "TO_LOWER" ) );
        cl = new CaseInsensitiveLike("a", "xx" );
        assertEquals( "(a=:x1 OR a=:x2 OR a=:x3 OR a=:x4)", cl.getCondition( "a", args, "TO_LOWER" ) );
        cl = new CaseInsensitiveLike("a", "xxx" );
        assertEquals( "((a LIKE :x1 OR a LIKE :x2 OR a LIKE :x3 OR a LIKE :x4) AND TO_LOWER(a)=:x5)", cl.getCondition( "a", args, "TO_LOWER" ) );
        cl = new CaseInsensitiveLike("a", "x%x" );
        assertEquals( "((a LIKE :x1 OR a LIKE :x2) AND TO_LOWER(a) LIKE :x3)", cl.getCondition( "a", args, "TO_LOWER" ) );
    }

    private static void equalArrays( Object[] a, Object[] b ) {
        if ( a == null && b == null ) {
            return;
        }
        if ( a == null || b == null ) {
            fail( "A null array is not equal to a non-null array" );
        }
        assertEquals( "Arrays must be of the same length.", a.length, b.length );
        assertEquals( "Arrays must be of the same type.", a.getClass(), b.getClass() );
        for ( int i = 0 ; i != a.length ; i++ ) {
            assertEquals( "Elements at position "+i+" are different", a[i], b[i] );
        }
    }

    private static final String[] args = new String[] { ":x1", ":x2", ":x3", ":x4", ":x5" };
}
