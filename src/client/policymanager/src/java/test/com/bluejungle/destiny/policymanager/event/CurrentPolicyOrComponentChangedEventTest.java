/*
 * Created on Feb 3, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event;

import com.bluejungle.framework.domain.IHasId;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/test/com/bluejungle/destiny/policymanager/event/CurrentPolicyOrComponentChangedEventTest.java#1 $
 */

public class CurrentPolicyOrComponentChangedEventTest extends TestCase {

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent.CurrentPolicyOrComponentChangedEvent(IHasId)'
     */
    public void testConstructors() {
        try {
            new CurrentPolicyOrComponentChangedEvent(null);
            fail("testConstructors - Should throw NPE for null entity");
        } catch (NullPointerException exception) {}
    }

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent.getEventType()'
     */
    public void testGetEventType() {
        CurrentPolicyOrComponentChangedEvent eventToTest = new CurrentPolicyOrComponentChangedEvent();
        assertEquals("testGetEventType -  Ensure event type as expected", EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT, eventToTest.getEventType());
    }

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent.getNewCurrentObject()'
     */
    public void testGetNewCurrentObject() {
        IHasId newCurrentObject = new IHasId() {
            /**
             * @see com.bluejungle.framework.domain.IHasId#getId()
             */
            public Long getId() {
                return new Long(5);
            }        
        };
        
        CurrentPolicyOrComponentChangedEvent eventToTest = new CurrentPolicyOrComponentChangedEvent(newCurrentObject);
        assertEquals("testGetNewCurrentObject - Ensure new current object as expected", newCurrentObject, eventToTest.getNewCurrentObject().getEntity());
    }

    /*
     * Test method for 'com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent.currentObjectExists()'
     */
    public void testCurrentObjectExists() {
        CurrentPolicyOrComponentChangedEvent eventToTest = new CurrentPolicyOrComponentChangedEvent();
        assertFalse("testCurrentObjectExists - Ensure current object does not exist", eventToTest.currentObjectExists());
        
        IHasId newCurrentObject = new IHasId() {
            /**
             * @see com.bluejungle.framework.domain.IHasId#getId()
             */
            public Long getId() {
                return new Long(5);
            }        
        };
        
        eventToTest = new CurrentPolicyOrComponentChangedEvent(newCurrentObject);
        assertTrue("testCurrentObjectExists - Ensure current object does exist", eventToTest.currentObjectExists());        
    }

}
