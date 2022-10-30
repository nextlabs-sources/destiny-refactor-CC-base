/*
 * Created on Mar 28, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

/**
 * A Test Synchronizer which will run all runnables synchronously. Useful for
 * unit testing
 * {@see org.eclipse.swt.widgets.Display#setSynchronizer}
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policymanager/src/java/test/com/bluejungle/destiny/policymanager/util/TestSynchronizer.java#1 $
 */

public class TestSynchronizer extends Synchronizer {

    /**
     * Create an instance of TestSynchronizer
     * 
     * @param display
     */
    public TestSynchronizer(Display display) {
        super(display);
    }

    /**
     * @see org.eclipse.swt.widgets.Synchronizer#asyncExec(java.lang.Runnable)
     */
    protected void asyncExec(Runnable runnable) {
        runnable.run();
    }

    /**
     * @see org.eclipse.swt.widgets.Synchronizer#syncExec(java.lang.Runnable)
     */
    protected void syncExec(Runnable runnable) {
        runnable.run();
    }

}
