package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/test/com/nextlabs/expression/util/ExpressionAccuracyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.Relation;
import com.nextlabs.expression.representation.RelationOperator;
import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.IPolicyParserFactory;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.language.parser.PolicyParserFactory;

/**
 * Accuracy tests for predicate transformation part of expression utilities.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={ExpressionAccuracyTests.class})

public class ExpressionAccuracyTests {

    /**
     * This is the parser used for parsing test expressions.
     */
    private static IPolicyLanguageParser parser;

    @BeforeClass
    public static void prepare() throws PolicyLanguageException {
        IPolicyParserFactory ppf = new PolicyParserFactory();
        parser = ppf.getParser(1);
    }

    private static final IExpressionTransformer ZERO_FOR_NULL =
    new DefaultExpressionTransformer() {
        @Override
        public IExpression transformConstant(Constant constant) {
            if (constant == IExpression.NULL) {
                return Constant.makeInteger(0L);
            } else {
                return constant;
            }
        }
    };

    private static final IExpressionDetector FIND_NULL =
    new DefaultExpressionDetector() {
        @Override
        public boolean checkConstant(Constant constant) {
            return constant == IExpression.NULL;
        }
    };

    /**
     * Detects relations with "greater than"
     */
    private static final IExpressionDetector HAS_GT =
    new DefaultExpressionDetector() {
        @Override
        public boolean checkRelation(IRelation relation) {
            return relation.getOperator() == RelationOperator.GREATER_THAN;
        }
    };

    private static final IExpressionTransformer INVERT =
    new DefaultExpressionTransformer() {
        @Override
        public IExpression transformConstant(Constant constant) {
            if (constant == IExpression.TRUE) {
                return IExpression.FALSE;
            } else {
                return IExpression.TRUE;
            }
        }
    };

    private static final IExpressionTransformer DEL_FALSE =
        new DefaultExpressionTransformer() {
            @Override
            public IExpression transformConstant(Constant constant) {
                if (constant == IExpression.FALSE) {
                    return null;
                } else {
                    return constant;
                }
            }
        };

    private static final IExpressionTransformer FLIP =
    new DefaultExpressionTransformer() {
        @Override
        public IExpression transformRelation(IRelation orig, IRelation tx) {
            return new Relation(orig.getOperator(), tx.getRHS(), tx.getLHS());
        }
    };


    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList( new Object[][] {
            // Detectors
            {null, false, FIND_NULL, null}
        ,   {"1+2", false, FIND_NULL, null}
        ,   {"1+null", true, FIND_NULL, null}
        ,   {"1+null-3", true, FIND_NULL, null}
        ,   {"-(1+2)", false, FIND_NULL, null}
        ,   {"-(1+null)", true, FIND_NULL, null}
        ,   {"f(1+null)", true, FIND_NULL, null}
        ,   {"f(a=1,b=null,c=4,d=5)", true, FIND_NULL, null}
        ,   {"f(a=1,b=c.d)", false, FIND_NULL, null}
        ,   {"false", false, HAS_GT,  null}
        ,   {null, false, HAS_GT, null}
        ,   {"true OR false", false, HAS_GT, null}
        ,   {"a < 0", false, HAS_GT, null}
        ,   {"a > 0", true, HAS_GT, null}
        ,   {"a > 0 AND NOT (b < 0)", true, HAS_GT, null}
        ,   {"1 < 0 AND NOT (1 < 0)", false, HAS_GT,null}
        ,   {"1 > 0 AND NOT (1 < 0)", true, HAS_GT, null}
        ,   {"1 != 0 AND NOT (id 1 > a.x)", true, HAS_GT, null}
        ,   {"id 0 or a", false, HAS_GT, null}
            // Transformers
        ,   {null, null, ZERO_FOR_NULL, null}
        ,   {"1+2", "1+2", ZERO_FOR_NULL, null}
        ,   {"1+null", "1+0", ZERO_FOR_NULL, null}
        ,   {"-(1+null)", "-(1+0)", ZERO_FOR_NULL, null}
        ,   {"-(1+2)", "-(1+2)", ZERO_FOR_NULL, null}
        ,   {"f(1+null,null)", "f(1+0,0)", ZERO_FOR_NULL, null}
        ,   {"f(a=1+null,b=null)", "f(a=1+0,b=0)", ZERO_FOR_NULL, null}
        ,   {"f(1,2)", "f(1,2)", ZERO_FOR_NULL, null}
        ,   {"f(a=1,b=2)", "f(a=1,b=2)", ZERO_FOR_NULL, null}
        ,   {"id 3(1+null,null)", "id 3(1+0,0)", ZERO_FOR_NULL, null}
        ,   {"id 4(a=1+null,b=null)", "id 4(a=1+0,b=0)", ZERO_FOR_NULL, null}
        ,   {"id 5(1,2)", "id 5(1,2)", ZERO_FOR_NULL, null}
        ,   {"id -6(a=1,b=2)", "id -6(a=1,b=2)", ZERO_FOR_NULL, null}
        ,   {"2*(3+(4%5))", "2*(3+(4%5))", ZERO_FOR_NULL, null}
        ,   {"2*(3+(null%5))", "2*(3+(0%5))", ZERO_FOR_NULL, null}
        ,   {"2*(3+(n.u%null))", "2*(3+(n.u%0))", ZERO_FOR_NULL, null}
        ,   {null, null, INVERT, false}
        ,   {null, null, INVERT, true}
        ,   {"true", "false", INVERT, false}
        ,   {"true", "false", INVERT, true}
        ,   {"false", "true", INVERT, false}
        ,   {"false", "true", INVERT, true}
        ,   {"true or false or false" , "false or true or true", INVERT, false}
        ,   {"true or false or false", "true", INVERT, true}
        ,   {"a > b", "b > a", FLIP, true}
        ,   {"a > b or false", "b > a or false", FLIP, false}
        ,   {"a > b or false", "b > a", FLIP, true}
        ,   {"false and a > b", "false", FLIP, true}
        ,   {"false and a > b", "a > b", DEL_FALSE, true}
        ,   {"not false and not false", "true", FLIP, true}
        ,   {"not a>b", "not b>a", FLIP, true}
        ,   {"false or false", "false", FLIP, true}
        ,   {"not false and not false", "not false and not false", FLIP, false}
        ,   {"not false and not true", "false", DEL_FALSE, true}
        ,   {"a > b or c > d or false", "a > b or c > d", DEL_FALSE, true}
        ,   {"a > null", "a > 0", ZERO_FOR_NULL, true}
        });
    }

    private final IExpression from;

    private final IExpression to;

    private final IExpressionDetector detector;

    private final IExpressionTransformer transformer;

    private final boolean expected;

    private final Boolean optimize;

    public ExpressionAccuracyTests(
        String from
    ,   Object to
    ,   Object eConv
    ,   Object optimize
    ) throws Exception {
        if (from != null) {
            this.from = parser.parseExpression(new StringReader(from));
        } else {
            this.from = null;
        }
        if (to == null || to instanceof String) {
            if (to != null) {
                this.to = parser.parseExpression(new StringReader((String)to));
            } else {
                this.to = null;
            }
            assertTrue(eConv instanceof IExpressionTransformer);
            transformer = (IExpressionTransformer)eConv;
            detector = null;
            expected = false;
            this.optimize = (Boolean)optimize;
        } else {
            this.to = null;
            assertTrue(to instanceof Boolean);
            expected = (Boolean)to;
            transformer = null;
            detector = (IExpressionDetector)eConv;
            this.optimize = false;
        }
    }

    @Test
    public void test() {
        if (transformer != null) {
            testTransformExpression();
        } else {
            testDetectExpression();
        }
    }

    private void testTransformExpression() {
        if (optimize == null) {
            assertEquals(to, Expressions.transform(from, transformer));
        } else {
            assertEquals(
                to
            ,   Expressions.transform(from, transformer, optimize)
            );
        }
    }

    private void testDetectExpression() {
        assertEquals(expected, Expressions.find(from, detector));
    }

}
