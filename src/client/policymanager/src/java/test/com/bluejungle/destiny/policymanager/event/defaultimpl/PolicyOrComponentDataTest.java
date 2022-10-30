/*
 * Created on Feb 2, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event.defaultimpl;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/test/com/bluejungle/destiny/policymanager/event/defaultimpl/PolicyOrComponentDataTest.java#1 $
 */

public class PolicyOrComponentDataTest extends TestCase {

    public void testConstructors() {
        try {
            new PolicyOrComponentData((IHasId)null);
            fail("testConstructors - First constructor should throw NPE will null Entity");
        } catch (NullPointerException exception) {}
        
        try {
            new PolicyOrComponentData((DomainObjectDescriptor)null);
            fail("testConstructors - Second constructor should throw NPE will null Entity");
        } catch (NullPointerException exception) {}        
    }
    
    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData.getDescriptor()'
     */
    public void testGetDescriptor() {
        // With first constructor
        DomainObjectDescriptor descriptor = new DomainObjectDescriptor(null, null, null, null, null, null, null);
        PolicyOrComponentData dataToTest = new PolicyOrComponentData(descriptor);
        assertEquals("testGetDescriptor - Ensure descriptor as expected", descriptor, dataToTest.getDescriptor());
        
        // Can't test the other constructor
    }

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData.getEntity()'
     */
    public void testGetEntity() {
        // With first constructor
        IHasId entity = new IHasId() {

            /**
             * @see com.bluejungle.framework.domain.IHasId#getId()
             */
            public Long getId() {
                return new Long(85799845);
            }
            
        };
        PolicyOrComponentData dataToTest = new PolicyOrComponentData(entity);
        assertEquals("testGetEntity - Ensure entity as expected", entity, dataToTest.getEntity());
        
        // Can't test the other constructor
    }

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.defaultimpl.PolicyOrComponentData.getEntityUsage()'
     */
    public void testGetEntityUsage() {
        // Can't test
    }

}
