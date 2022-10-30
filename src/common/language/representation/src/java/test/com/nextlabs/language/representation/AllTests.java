package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/AllTests.java#1 $
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This is the test suite for language representation classes.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Suite.class)
@SuiteClasses(value={
    AbstractDefinitionTests.class
,   CallableTypeTests.class
,   ContextTypeTests.class
,   DefaultDefinitionVisitorTests.class
,   FunctionTypeTests.class
,   ObligationTypeTests.class
,   PolicyTests.class
,   PolicyComponentTests.class
,   PolicySetTests.class
,   PolicyTypeTests.class
,   OutcomeTests.class
,   TargetTests.class
,   UtilsTests.class
})

public class AllTests {
}
