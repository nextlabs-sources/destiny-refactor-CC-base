package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/test/com/nextlabs/language/formatter/AllTests.java#1 $
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All tests for the policy languages.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Suite.class)
@SuiteClasses(value={
    DefinitionFormatterTests.class
,   ExpressionFormatterTests.class
,   PredicateFormatterTests.class
,   SetPredicateFormatterTests.class
})

public class AllTests {
}
