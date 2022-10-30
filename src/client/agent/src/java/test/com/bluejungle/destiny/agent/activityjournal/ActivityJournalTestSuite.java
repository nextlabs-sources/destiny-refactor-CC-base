/*
 * Created on Mar 29, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.activityjournal;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author fuad
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/client/agent/src/java/test/com/bluejungle/destiny/agent/activityjournal/ActivityJournalTestSuite.java#1 $:
 */

public class ActivityJournalTestSuite {

    /**
     * Main method
     * 
     * @param args
     *            no arguments
     */
    public static void main(String[] args) {
        junit.swingui.TestRunner.run(ActivityJournalTestSuite.class);
    }

    /**
     * Returns the activity journal test suite
     * 
     * @return the activity journal test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.agent.activityjournal");
        suite.addTestSuite(ActivityJournalTest.class);
        return suite;
    }
}
