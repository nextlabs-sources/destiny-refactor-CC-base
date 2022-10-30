package com.nextlabs.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/AllTests.java#1 $
 */

/**
 * This is the test suite for the util package.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Suite.class)
@SuiteClasses(value={
    MultiDispatcherTests.class
    ,   PathTests.class
    ,   MultipartKeyTests.class
    ,   StringsTests.class
    ,   WildcardPatternAccuracyTests.class
    ,   WildcardPatternFeatureTests.class
    ,   ReferenceTests.class
})

public class AllTests {
}
