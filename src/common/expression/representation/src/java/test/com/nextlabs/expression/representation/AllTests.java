package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/AllTests.java#1 $
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This is the test suite for expression representations.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Suite.class)
@SuiteClasses(value={
    TestBinaryOperator.class
,   TestBinaryOperator.FeatureTests.class
,   AttributeReferenceTests.class
,   ConstantTests.class
,   CompositeExpressionTests.class
,   DataTypeTests.class
,   DefaultExpressionVisitorTests.class
,   ExpressionReferenceTests.class
,   FunctionCallTests.class
,   RelationTests.class
,   RelationOperatorTests.class
,   RelationOperatorTests.FeatureTests.class
})

public class AllTests {
}
