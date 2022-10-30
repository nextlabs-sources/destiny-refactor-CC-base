/*
 * Created on Apr 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.destiny.lib;

import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/destiny/lib/TestLeafObjectSearchSpec.java#1 $
 */

public class TestLeafObjectSearchSpec extends TestCase {

    /*
     * Test method for
     * 'com.bluejungle.pf.destiny.services.LeafObjectSearchSpec.LeafObjectSearchSpec(LeafObjectType,
     * String, int)'
     */
    public void testLeafObjectSearchSpec() {
        // Test NPE for type
        try {
            new LeafObjectSearchSpec(null, PredicateConstants.FALSE, 6);
            fail("Should throw NPE for null leaf object spec type");
        } catch (NullPointerException expected) {
        }

        // Test NPE for predicate
        try {
            new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, null, 6);
            fail("Should throw NPE for null search string");
        } catch (NullPointerException expected) {
        }

        // Test IllegalArgumentException
        try {
            new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, -1);
            fail("Should throw IllegalArgumentException for negative max search results");
        } catch (IllegalArgumentException expected) {
        }

    }

    /*
     * Test method for
     * 'com.bluejungle.pf.destiny.services.LeafObjectSearchSpec.getLeafObjectType()'
     */
    public void testGetLeafObjectType() {
        LeafObjectSearchSpec searchSpec = new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, 6);
        assertEquals("Leaf Object Type Should be as expected", LeafObjectType.ACCESSGROUP, searchSpec.getLeafObjectType());
    }

    /*
     * Test method for
     * 'com.bluejungle.pf.destiny.services.LeafObjectSearchSpec.getMaxResults()'
     */
    public void testGetMaxResults() {
        LeafObjectSearchSpec searchSpec = new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, 6);
        assertEquals("Max results should be as expected", 6, searchSpec.getMaxResults());   
    }

    /*
     * Test method for
     * 'com.bluejungle.pf.destiny.services.LeafObjectSearchSpec.getSearchString()'
     */
    public void testGetSearchString() {
        LeafObjectSearchSpec searchSpec = new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, 6);
        assertEquals("Search string should be as expected", PredicateConstants.FALSE, searchSpec.getSearchPredicate());  
    }

    /*
     * Test method for
     * 'com.bluejungle.pf.destiny.services.LeafObjectSearchSpec.getNamespaceId()'
     */
    public void testGetNamespaceId() {
        String namespaceId = "foo";
        LeafObjectSearchSpec searchSpec = new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, namespaceId, 6);
        assertEquals("Namespace Id should be as expected", namespaceId, searchSpec.getNamespaceId());
        
        searchSpec = new LeafObjectSearchSpec(LeafObjectType.ACCESSGROUP, PredicateConstants.FALSE, 6);
        assertNull("NamespaceId should be null", searchSpec.getNamespaceId());
    }

}
