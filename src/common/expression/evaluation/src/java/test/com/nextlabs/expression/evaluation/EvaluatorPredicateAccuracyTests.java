package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/test/com/nextlabs/expression/evaluation/EvaluatorPredicateAccuracyTests.java#1 $
 */

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.IPolicyParserFactory;
import com.nextlabs.language.parser.PolicyParserFactory;
import com.nextlabs.util.ref.IReference;

/**
 * Accuracy tests for the predicate part of the evaluator.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={EvaluatorPredicateAccuracyTests.class})

public class EvaluatorPredicateAccuracyTests {

    private static final IPolicyParserFactory pf = new PolicyParserFactory();

    private static final IEvaluationHandler eh = new IEvaluationHandler() {

        public void evaluateAttribute(
            String name
        ,   Object value
        ,   IDataType type
        ,   IEvaluatorCallback eval) {
            fail("Method should not be called: evaluateAttribute");
        }

        public void evaluateCustomExpression(
            IExpression custom
        ,   IEvaluatorCallback eval) {
            fail("Method should not be called: evaluateCustomExpression");
        }

        public void evaluateFunction(
            IReference<IFunction> function
        ,   String[] argNames
        ,   Object[] argValues
        ,   IDataType[] argTypes
        ,   int argOffset
        ,   IEvaluatorCallback c) {
            fail("Method should not be called: evaluateFunction");
        }

        public void evaluateReference(
            IExpressionReference ref
        ,   IEvaluatorCallback eval) {
            eval.addValue(ref.getReference().getPath().get(0).equals("yes"), DataType.BOOLEAN);
        }

    };

    private final IExpression pred;
    private final boolean expected;

    public EvaluatorPredicateAccuracyTests(
        boolean expected
    ,   String predicate
    ) throws Exception {
        IPolicyLanguageParser p = pf.getParser(1);
        pred = p.parseExpression(new StringReader(predicate));
        this.expected = expected;
    }

    @Test
    public void verifyPredicate() {
        assertEquals(expected, Evaluator.getInstance().evaluate(pred, eh).getValue());
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            {true,  "true"}
        ,   {false, "false"}
        ,   {false, "true and false"}
        ,   {true,  "true or false"}
        ,   {true,  "not false"}
        ,   {false, "not true"}
        ,   {true,  "true and (true or false)"}
        ,   {false, "true and (true and not true)"}
        ,   {true,  "yes"}
        ,   {false,  "no"}
        ,   {false, "yes and no"}
        ,   {false, "not yes and not no"}
        ,   {true,  "not yes or not no"}
        ,   {true,  "1 < 2000"}
        ,   {true,  "1 > 0.999 and (true or true or false) and true"}
        });
    }

}
