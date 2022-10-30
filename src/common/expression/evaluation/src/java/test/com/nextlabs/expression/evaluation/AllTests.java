package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/test/com/nextlabs/expression/evaluation/AllTests.java#1 $
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This is the test suite for expression evaluator.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Suite.class)
@SuiteClasses(value={
    EvaluatorExpressionAccuracyTests.class
,   RelationEvaluatorTests.class
,   EvaluatorPredicateAccuracyTests.class
})

public class AllTests {
}
