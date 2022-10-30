/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.framework.comp.ComponentManagerSuite;
import com.bluejungle.framework.crypt.ReversibleEncryptionTest;
import com.bluejungle.framework.expressions.ExpressionsSuite;
import com.bluejungle.framework.messages.LocalizedMessageRepositoryTest;
import com.bluejungle.framework.patterns.PatternsSuite;
import com.bluejungle.framework.threading.ThreadingTests;
import com.bluejungle.framework.utils.TestDisjointUnion;
import com.bluejungle.framework.utils.PasswordUtilsTest;
import com.bluejungle.framework.utils.SetUtilsTest;
import com.bluejungle.framework.utils.StringUtilsTest;
import com.bluejungle.framework.utils.TimeIntervalTest;

/**
 * This is the test suite for Destiny Common Framework
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/test/CommonFrameworkTestSuite.java#1 $
 */

public class CommonFrameworkTestSuite {

    /**
     * Returns the common framework test suite
     * 
     * @return the common framework test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Common Framework");
        suite.addTest(ComponentManagerSuite.suite());
        suite.addTest(ExpressionsSuite.suite());
        suite.addTest(new TestSuite(SetUtilsTest.class, "Sets Utility Test"));
        suite.addTest(new TestSuite(StringUtilsTest.class, "String Utility Test"));
        suite.addTest(new TestSuite(TimeIntervalTest.class, "Time Interval Test"));
        suite.addTest(new TestSuite(TestDisjointUnion.class, "Disjoint Union Test"));
        suite.addTest(ThreadingTests.suite());
        suite.addTest(PatternsSuite.suite());
        suite.addTest(new TestSuite(LocalizedMessageRepositoryTest.class, "Localized Message Test"));
        suite.addTest(new TestSuite(ReversibleEncryptionTest.class, "Encryption utility Test"));
        suite.addTestSuite(PasswordUtilsTest.class);
        //Fails. Will be enabled when ready
        //suite.addTest(new TestSuite(BlueJungleExceptionTest.class, "Exception test"));
        return suite;
    }

}
