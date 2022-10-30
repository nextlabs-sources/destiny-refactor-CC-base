/*
 * Created on Feb 2, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.event.defaultimpl;

import com.bluejungle.destiny.policymanager.event.ContextualEventType;
import com.bluejungle.destiny.policymanager.event.CurrentPolicyOrComponentChangedEvent;
import com.bluejungle.destiny.policymanager.event.EventType;
import com.bluejungle.destiny.policymanager.event.IContextualEvent;
import com.bluejungle.destiny.policymanager.event.IContextualEventListener;
import com.bluejungle.destiny.policymanager.event.IEvent;
import com.bluejungle.destiny.policymanager.event.IEventListener;
import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener;
import com.bluejungle.destiny.policymanager.event.PredicateModifiedEvent;
import com.bluejungle.destiny.policymanager.util.TestSynchronizer;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.IArguments;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IPredicateVisitor.Order;

import org.eclipse.swt.widgets.Display;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/test/com/bluejungle/destiny/policymanager/event/defaultimpl/EventManagerImplTest.java#1 $
 */

public class EventManagerImplTest extends TestCase {

    private EventManagerImpl eventManagerToTest;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.eventManagerToTest = new EventManagerImpl();
        Display createdDisplay = Display.getDefault(); // Create a display
        TestSynchronizer testSynchornizer = new TestSynchronizer(createdDisplay);
        createdDisplay.setSynchronizer(testSynchornizer);
    }

    /**
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.getComponentInfo()'
     */
    public void testGetComponentInfo() {
        ComponentInfo componentInfo = this.eventManagerToTest.getComponentInfo();
        assertEquals("testGetComponentInfo - Ensure component name as expected", IEventManager.COMPONENT_NAME, componentInfo.getName());
        assertEquals("testGetComponentInfo - Ensure class as expected", EventManagerImpl.class.getName(), componentInfo.getClassName());
        assertEquals("testGetComponentInfo - Ensure interface as expected", IEventManager.class.getName(), componentInfo.getInterfaceName());
        assertEquals("testGetComponentInfo - Ensure lifestyle type as expected", LifestyleType.SINGLETON_TYPE, componentInfo.getLifestyleType());
    }

    /**
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.registerListener(IEventListener,
     * EventType)' Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.unregisterListener(IEventListener,
     * EventType)' Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.fireEvent(IEvent)'
     */
    public void testGenericEvents() {
        TestEventListener eventListenerOne = new TestEventListener();
        TestEventListener eventListenerTwo = new TestEventListener();

        this.eventManagerToTest.fireEvent(new CurrentPolicyOrComponentChangedEvent());
        assertEquals("testGenericEvents - Ensure listener one originally not invoked", 0, eventListenerOne.getTimesInvoked());
        assertEquals("testGenericEvents - Ensure listener two originally not invoked", 0, eventListenerTwo.getTimesInvoked());

        // Now, register listener
        this.eventManagerToTest.registerListener(eventListenerOne, EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
        this.eventManagerToTest.fireEvent(new CurrentPolicyOrComponentChangedEvent());
        
        assertEquals("testGenericEvents - Ensure listener one originally invoked after registration", 1, eventListenerOne.getTimesInvoked());
        assertEquals("testGenericEvents - Ensure listener two not invoked after listener one registered", 0, eventListenerTwo.getTimesInvoked());

        // Now register second listener
        this.eventManagerToTest.registerListener(eventListenerTwo, EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
        this.eventManagerToTest.fireEvent(new CurrentPolicyOrComponentChangedEvent());
        assertEquals("testGenericEvents - Ensure listener one originally invoked twice after second is registered", 2, eventListenerOne.getTimesInvoked(), 2);
        assertEquals("testGenericEvents - Ensure listener two invoked after listener two registered", 1, eventListenerTwo.getTimesInvoked());

        // Fire another event and ensure both do not recieve it
        this.eventManagerToTest.fireEvent(new IEvent() {

            /**
             * @see com.bluejungle.destiny.policymanager.event.IEvent#getEventType()
             */
            public EventType getEventType() {
                return EventType.SELECTION_CHANGED_EVENT;
            }
        });
        assertEquals("testGenericEvents - Ensure listener one not invoked for different event type", 2, eventListenerOne.getTimesInvoked());
        assertEquals("testGenericEvents - Ensure listener two not invoked for different event type", 1, eventListenerTwo.getTimesInvoked());

        // Now unregister first listener
        this.eventManagerToTest.unregisterListener(eventListenerOne, EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
        this.eventManagerToTest.fireEvent(new CurrentPolicyOrComponentChangedEvent());
        assertEquals("testGenericEvents - Ensure listener one not invoked after unregistration", 2, eventListenerOne.getTimesInvoked());
        assertEquals("testGenericEvents - Ensure listener two invoked after listener one unregistered", 2, eventListenerTwo.getTimesInvoked());

        // TEST NPE's
        try {
            this.eventManagerToTest.registerListener(null, EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
            fail("testGenericEvents - Should throw NPE when registerEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.registerListener(eventListenerOne, null);
            fail("testGenericEvents - Should throw NPE when registerEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(null, EventType.CURRENT_POLICY_OR_COMPONENT_CHANGED_EVENT);
            fail("testGenericEvents - Should throw NPE when unregisterEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(eventListenerOne, null);
            fail("testGenericEvents - Should throw NPE when unregisterEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.fireEvent((IEvent) null);
            fail("testGenericEvents - Should throw NPE when fireEvent() is called with null event");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.registerListener(IContextualEventListener,
     * ContextualEventType, Object)'<br />
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.unregisterListener(IContextualEventListener,
     * ContextualEventType, Object)' <br />
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.fireEvent(IContextualEvent)'
     * 
     */
    public void testContextualEvents() {
        TestContextualEventListener contextualEventListenerOne = new TestContextualEventListener();
        TestContextualEventListener contextualEventListenerTwo = new TestContextualEventListener();
        final TestPredicate predicateOne = new TestPredicate();
        TestPredicate predicateTwo = new TestPredicate();

        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateOne));
        assertEquals("testContextualEvents - Ensure listener one originally not invoked", 0, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two originally not invoked", 0, contextualEventListenerTwo.getTimesInvoked());

        // Now, register listener
        this.eventManagerToTest.registerListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateOne);
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateOne));
        assertEquals("testContextualEvents - Ensure listener one originally invoked after registration", 1, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two not invoked after listener one registered", 0, contextualEventListenerTwo.getTimesInvoked());

        // Now register second listener
        this.eventManagerToTest.registerListener(contextualEventListenerTwo, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateOne);
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateOne));
        assertEquals("testContextualEvents - Ensure listener one originally invoked twice after second is registered", 2, contextualEventListenerOne.getTimesInvoked(), 2);
        assertEquals("testContextualEvents - Ensure listener two invoked after listener two registered", 1, contextualEventListenerTwo.getTimesInvoked());

        // Try with a differnt object context
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateTwo));
        assertEquals("testContextualEvents - Ensure listener one originally not invoked with second context", 2, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two originally not invoked with second context", 1, contextualEventListenerTwo.getTimesInvoked());

        // Now, register listener one on second context
        this.eventManagerToTest.registerListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateTwo);
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateTwo));
        assertEquals("testContextualEvents - Ensure listener one originally invoked after registration with second context", 3, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two not invoked after listener one registered with second context", 1, contextualEventListenerTwo.getTimesInvoked());

        // Now register second listener on second context
        this.eventManagerToTest.registerListener(contextualEventListenerTwo, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateTwo);
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateTwo));
        assertEquals("testContextualEvents - Ensure listener one originally invoked after second is registered on second context", 4, contextualEventListenerOne.getTimesInvoked(), 2);
        assertEquals("testContextualEvents - Ensure listener two invoked after listener two registered on second context", 2, contextualEventListenerTwo.getTimesInvoked());

        // Fire another ContextualEvent on a different type and ensure both do
        // not recieve it
        this.eventManagerToTest.fireEvent(new IContextualEvent() {

            /**
             * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getContextualEventType()
             */
            public ContextualEventType getContextualEventType() {
                return ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT;
            }

            /**
             * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getEventContext()
             */
            public Object getEventContext() {
                return predicateOne;
            }

        });
        assertEquals("testContextualEvents - Ensure listener one not invoked for different ContextualEvent type", 4, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two not invoked for different ContextualEvent type", 2, contextualEventListenerTwo.getTimesInvoked());

        // Now unregister first listener on both context
        this.eventManagerToTest.unregisterListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateOne);
        this.eventManagerToTest.unregisterListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateTwo);
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateOne));
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateTwo));
        assertEquals("testContextualEvents - Ensure listener one not invoked after unregistration", 4, contextualEventListenerOne.getTimesInvoked());
        assertEquals("testContextualEvents - Ensure listener two invoked after listener one unregistered", 4, contextualEventListenerTwo.getTimesInvoked());

        // TEST NPE's
        try {
            this.eventManagerToTest.registerListener(null, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, new Object());
            fail("testContextualEvents - Should throw NPE when registerEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.registerListener(contextualEventListenerOne, null, new Object());
            fail("testContextualEvents - Should throw NPE when registerEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.registerListener(contextualEventListenerOne, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, null);
            fail("testContextualEvents - Should throw NPE when registerEventListener() is called with null object context");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(null, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, new Object());
            fail("testContextualEvents - Should throw NPE when unregisterEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(contextualEventListenerOne, null, new Object());
            fail("testContextualEvents - Should throw NPE when unregisterEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(contextualEventListenerOne, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT, null);
            fail("testContextualEvents - Should throw NPE when unregisterEventListener() is called with null object context");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.fireEvent((IContextualEvent) null);
            fail("testGenericEvents - Should throw NPE when fireEvent() is called with null event");
        } catch (NullPointerException exception) {
        }
    }

    /**
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.registerListener(IMultiContextualEventListener,
     * ContextualEventType)' <br />
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.unregisterListener(IMultiContextualEventListener,
     * ContextualEventType)' <br />
     * Test method for
     * 'com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl.fireEvent(Set)'
     */
    public void testMultiContextualEvents() {
        TestContextualEventListener contextualEventListenerOne = new TestContextualEventListener();
        TestMultiContextualEventListener multiContextualEventListenerOne = new TestMultiContextualEventListener();
        TestMultiContextualEventListener multiContextualEventListenerTwo = new TestMultiContextualEventListener();
        final TestPredicate predicateOne = new TestPredicate();
        TestPredicate predicateTwo = new TestPredicate();
        
        Set eventsToFire = new HashSet();
        eventsToFire.add(new PredicateModifiedEvent(predicateOne));
        eventsToFire.add(new PredicateModifiedEvent(predicateTwo));
        
        this.eventManagerToTest.fireEvent(eventsToFire);
        assertEquals("testMultiContextualEvents - Ensure contextual listener one originally not invoked", 0, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one originally not invoked", 0, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two originally not invoked", 0, multiContextualEventListenerTwo.getTimesInvoked());
        
        // Now, register one contextual and on multi listener
        this.eventManagerToTest.registerListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateOne);
        this.eventManagerToTest.registerListener(multiContextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT);
        this.eventManagerToTest.fireEvent(eventsToFire);
        assertEquals("testMultiContextualEvents - Ensure contextual listener one invoked after registration", 1, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one invoked after registration", 1, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two not invoked after listener one registered", 0, multiContextualEventListenerTwo.getTimesInvoked());
      
        // Now register second listener
        this.eventManagerToTest.registerListener(multiContextualEventListenerTwo, ContextualEventType.PREDICATE_MODIFIED_EVENT);        
        this.eventManagerToTest.fireEvent(new PredicateModifiedEvent(predicateOne));
        assertEquals("testMultiContextualEvents - Ensure contextual listener one invoked after registration", 2, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one invoked after second is registred", 2, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two invoked after registered", 1, multiContextualEventListenerTwo.getTimesInvoked());
        
        // Try firing in only one context
        Set nextEventsToFire = Collections.singleton(new PredicateModifiedEvent(predicateTwo));
        this.eventManagerToTest.fireEvent(nextEventsToFire);
        assertEquals("testMultiContextualEvents - Ensure contextual listener one not invoked in second context", 2, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one invoked in second context", 3, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two invoked in second context", 2, multiContextualEventListenerTwo.getTimesInvoked());
     
        // Fire another ContextualEvent on a different type and ensure it's not retrieved
        this.eventManagerToTest.fireEvent(Collections.singleton(new IContextualEvent() {

            /**
             * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getContextualEventType()
             */
            public ContextualEventType getContextualEventType() {
                return ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT;
            }

            /**
             * @see com.bluejungle.destiny.policymanager.event.IContextualEvent#getEventContext()
             */
            public Object getEventContext() {
                return predicateOne;
            }
        })); 
        assertEquals("testMultiContextualEvents - Ensure contextual listener one not invoked for second event type", 2, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one invoked for second event type", 3, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two invoked for second event type", 2, multiContextualEventListenerTwo.getTimesInvoked());
     
        // Now, check unregistration
        this.eventManagerToTest.unregisterListener(contextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT, predicateOne);
        this.eventManagerToTest.unregisterListener(multiContextualEventListenerOne, ContextualEventType.PREDICATE_MODIFIED_EVENT);
        this.eventManagerToTest.fireEvent(eventsToFire);
        assertEquals("testMultiContextualEvents - Ensure contextual listener one originally not invoked", 2, contextualEventListenerOne.getTimesInvoked());        
        assertEquals("testMultiContextualEvents - Ensure multi listener one originally not invoked", 3, multiContextualEventListenerOne.getTimesInvoked());
        assertEquals("testMultiContextualEvents - Ensure multi listener two originally not invoked", 3, multiContextualEventListenerTwo.getTimesInvoked());
        
        // TEST NPE
        try {
            this.eventManagerToTest.registerListener(null, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT);
            fail("testMultiContextualEvents - Should throw NPE when registerEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }
        
        try {
            this.eventManagerToTest.registerListener(multiContextualEventListenerOne, null);
            fail("testMultiContextualEvents - Should throw NPE when registerEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }

        try {
            this.eventManagerToTest.unregisterListener(null, ContextualEventType.POLICY_OR_COMPONENT_MODIFIED_EVENT);
            fail("testMultiContextualEvents - Should throw NPE when unregisterEventListener() is called with null listener");
        } catch (NullPointerException exception) {
        }
        
        try {
            this.eventManagerToTest.unregisterListener(multiContextualEventListenerOne, null);
            fail("testMultiContextualEvents - Should throw NPE when unregisterEventListener() is called with null event type");
        } catch (NullPointerException exception) {
        }
        
        try {
            this.eventManagerToTest.fireEvent((Set) null);
            fail("testMultiContextualEvents - Should throw NPE when fireEvent() is called with null event");
        } catch (NullPointerException exception) {
        }
        
        // Try sending an empty Set and make sure it doesn't fail
        this.eventManagerToTest.fireEvent(Collections.EMPTY_SET);
    }

    private class TestEventListener implements IEventListener {

        private int timesInvoked = 0;

        /**
         * @see com.bluejungle.destiny.policymanager.event.IEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IEvent)
         */
        public void onEvent(IEvent event) {
            this.timesInvoked++;
        }

        /**
         * Retrieve the timesInvoked.
         * 
         * @return the timesInvoked.
         */
        private int getTimesInvoked() {
            return this.timesInvoked;
        }
    }

    private class TestContextualEventListener implements IContextualEventListener {

        private int timesInvoked = 0;

        /**
         * @see com.bluejungle.destiny.policymanager.event.IContextualEventListener#onEvent(com.bluejungle.destiny.policymanager.event.IContextualEvent)
         */
        public void onEvent(IContextualEvent event) {
            this.timesInvoked++;
        }

        /**
         * Retrieve the timesInvoked.
         * 
         * @return the timesInvoked.
         */
        private int getTimesInvoked() {
            return this.timesInvoked;
        }
    }

    private class TestMultiContextualEventListener implements IMultiContextualEventListener {
        private int timesInvoked = 0;
        
        /**
         * @see com.bluejungle.destiny.policymanager.event.IMultiContextualEventListener#onEvents(java.util.Set)
         */
        public void onEvents(Set events) {
            this.timesInvoked++;
        }   
        
        /**
         * Retrieve the timesInvoked.
         * 
         * @return the timesInvoked.
         */
        private int getTimesInvoked() {
            return this.timesInvoked;
        }
    }
    
    private class TestPredicate implements IPredicate {

        /**
         * @see com.bluejungle.framework.expressions.IPredicate#accept(com.bluejungle.framework.expressions.IPredicateVisitor,
         *      com.bluejungle.framework.expressions.IPredicateVisitor.Order)
         */
        public void accept(IPredicateVisitor visitor, Order order) {
            // TODO Auto-generated method stub

        }

        /**
         * @see com.bluejungle.framework.expressions.IPredicate#match(com.bluejungle.framework.expressions.IArguments)
         */
        public boolean match(IArguments arguments) {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
