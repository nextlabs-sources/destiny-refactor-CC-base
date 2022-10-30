/*
 * Created on Apr 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.framework;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/test/com/bluejungle/destiny/policymanager/framework/TestBaseTableLabelProvider.java#1 $
 */

public class TestBaseTableLabelProvider extends TestCase {
    private BaseTableLabelProvider labelProviderToTest;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.labelProviderToTest = new BaseTableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                // TODO Auto-generated method stub
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                // TODO Auto-generated method stub
                return null;
            }

            public boolean isLabelProperty(Object element, String property) {
                // TODO Auto-generated method stub
                return false;
            }
            
        };
    }

    /*
     * Test for addListener, removeListener, and fireLabelProviderChanged
     */
    public void testEvents() {
        TestListener testListener = new TestListener();
        LabelProviderChangedEvent labelProviderChangedEvent = new LabelProviderChangedEvent(this.labelProviderToTest);
        
        this.labelProviderToTest.fireLabelProviderChanged(labelProviderChangedEvent);        
        assertFalse("Event was not recieved", testListener.wasEventRecievedAndReset());
        
        this.labelProviderToTest.addListener(testListener);
        this.labelProviderToTest.fireLabelProviderChanged(labelProviderChangedEvent);                
        assertTrue("Event was recieved after adding listener", testListener.wasEventRecievedAndReset());
        
        this.labelProviderToTest.removeListener(testListener);
        this.labelProviderToTest.fireLabelProviderChanged(labelProviderChangedEvent);                
        assertFalse("Event was not recieved after removing listener", testListener.wasEventRecievedAndReset());
        
    }
    
    private class TestListener implements ILabelProviderListener {
        private boolean recievedEvent = false;
        
        /**
         * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
         */
        public void labelProviderChanged(LabelProviderChangedEvent event) {
            this.recievedEvent = true;            
        }
        
        private boolean wasEventRecievedAndReset() {
            boolean valueToReturn = this.recievedEvent;
            this.recievedEvent = false;
            return valueToReturn;
        }
        
    }

}
