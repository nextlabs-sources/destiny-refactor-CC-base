/*
 * Created on Feb 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.expressions;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.PredicateConstants;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/expressions/BooleanOpTest.java#1 $:
 */

public class BooleanOpTest extends TestCase {
    
    private final ArrayList l1 = new ArrayList(); 
    private final ArrayList l2 = new ArrayList();
    private final ArrayList l3 = new ArrayList();
    private final ArrayList l4 = new ArrayList();
    private final ArrayList l5 = new ArrayList();
    private final ArrayList l6 = new ArrayList();
    private final ArrayList l7 = new ArrayList();    
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        l1.add(PredicateConstants.TRUE);        
        l2.add(PredicateConstants.FALSE);
        for (int i = 0; i < 7; i++) {
            l3.add(PredicateConstants.TRUE);
            l4.add(PredicateConstants.FALSE);
            l5.add(PredicateConstants.TRUE);
            l6.add(PredicateConstants.FALSE);
        }
        l3.add(PredicateConstants.FALSE);
        l4.add(PredicateConstants.TRUE);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for BooleanOpTest.
     * @param arg0
     */
    public BooleanOpTest(String arg0) {
        super(arg0);
    }

    /*
     * Class under test for BooleanOp getElement(String)
     */
    public final void testGetElementString() {
        assertTrue("BooleanOp named AND should exist", BooleanOp.existsElement(BooleanOp.AND_NAME));
        BooleanOp and = BooleanOp.getElement(BooleanOp.AND_NAME);
        assertSame("BooleanOp named AND should be same as AND", BooleanOp.AND, and);

        assertTrue("BooleanOp named OR should exist", BooleanOp.existsElement(BooleanOp.OR_NAME));
        BooleanOp or = BooleanOp.getElement(BooleanOp.OR_NAME);
        assertSame("BooleanOp named OR should be same as OR", BooleanOp.OR, or);

        assertTrue("BooleanOp named NOT should exist", BooleanOp.existsElement(BooleanOp.NOT_NAME));
        BooleanOp not = BooleanOp.getElement(BooleanOp.NOT_NAME);
        assertSame("BooleanOp named NOT should be same as NOT", BooleanOp.NOT, not);
        
    }

    /*
     * Class under test for BooleanOp getElement(int)
     */
    public final void testGetElementInt() {
        assertTrue("BooleanOp numbered AND should exist", BooleanOp.existsElement(BooleanOp.AND_TYPE));
        BooleanOp and = BooleanOp.getElement(BooleanOp.AND_TYPE);
        assertSame("BooleanOp numbered AND should be same as AND", BooleanOp.AND, and);

        assertTrue("BooleanOp numbered OR should exist", BooleanOp.existsElement(BooleanOp.OR_TYPE));
        BooleanOp or = BooleanOp.getElement(BooleanOp.OR_TYPE);
        assertSame("BooleanOp numbered OR should be same as OR", BooleanOp.OR, or);

        assertTrue("BooleanOp numbered NOT should exist", BooleanOp.existsElement(BooleanOp.NOT_TYPE));
        BooleanOp not = BooleanOp.getElement(BooleanOp.NOT_TYPE);
        assertSame("BooleanOp numbered NOT should be same as NOT", BooleanOp.NOT, not);
    }
    
    public final void testOR() {
        BooleanOp or = BooleanOp.OR;
        
        assertNotNull("OR is not null", or);
        assertTrue("OR of l1 should be true", or.match(l1, null));
        assertFalse("OR of l2 should be false", or.match(l2, null));
        assertTrue("OR of l3 should be true", or.match(l3, null));
        assertTrue("OR of l4 should be true", or.match(l4, null));        
        assertTrue("OR of l5 should be true", or.match(l5, null));                
        assertFalse("OR of l6 should be false", or.match(l6, null));
        assertFalse("OR of l7 should be false", or.match(l7, null));
        assertFalse("OR of null should be false", or.match(null, null));
        
    }
    
    public final void testAND() {
        BooleanOp and = BooleanOp.AND;
        
        assertNotNull("AND is not null", and);
        assertTrue("AND of l1 should be true", and.match(l1, null));
        assertFalse("AND of l2 should be false", and.match(l2, null));
        assertFalse("AND of l3 should be false", and.match(l3, null));
        assertFalse("AND of l4 should be false", and.match(l4, null));        
        assertTrue("AND of l5 should be true", and.match(l5, null));                
        assertFalse("AND of l6 should be false", and.match(l6, null));
        assertFalse("AND of l7 should be false", and.match(l7, null));
        assertFalse("AND of null should be false", and.match(null, null));
        
    }    
    
    public final void testNOT() {
        BooleanOp not = BooleanOp.NOT;
        
        assertNotNull("NOT is not null", not);
        assertFalse("NOT of l1 should be false", not.match(l1, null));
        assertTrue("NOT of l2 should be true", not.match(l2, null));
        assertTrue("NOT of l7 should be true", not.match(l7, null));
        assertTrue("NOT of null should be true", not.match(null, null));
        
    }        

}
