package com.nextlabs.language.parser;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/parser/src/java/test/com/nextlabs/language/parser/AllTests.java#1 $
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
    PolicyParserFeatureTests.class
,   ParseExpressionTests.class
,   ParsePredicateTests.class
,   ParseSetPredicateTests.class
,   PolicyParserAccuracyTests.class
,   PolicyLanguageLexerAccuracyTests.class
,   PolicyLanguageFailureTests.class
})

public class AllTests {
}
