package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/test/com/nextlabs/expression/evaluation/RelationEvaluatorTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.Relation;
import com.nextlabs.expression.representation.RelationOperator;
import com.nextlabs.util.WildcardPattern;
import com.nextlabs.util.ref.IReference;

/**
 * Tests for the RelationEvaluator class.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={RelationEvaluatorTests.class})

public class RelationEvaluatorTests {

    private static final IEvaluationHandler eh = new IEvaluationHandler() {
        public void evaluateAttribute(
            String name, Object value
        ,   IDataType type
        ,   IEvaluatorCallback eval) {
            fail("Unexpected method call: evaluateAttribute");
        }

        public void evaluateCustomExpression(
            IExpression custom
        ,   IEvaluatorCallback eval) {
            fail("Unexpected method call: evaluateCustomExpression");
        }

        public void evaluateFunction(
            IReference<IFunction> function
        ,   String[] argNames
        ,   Object[] argValues
        ,   IDataType[] argTypes
        ,   int argOffset
        ,   IEvaluatorCallback evalHandler) {
            fail("Unexpected method call: evaluateFunction");
        }

        public void evaluateReference(
            IExpressionReference ref
        ,   IEvaluatorCallback eval) {
            fail("Unexpected method call: evaluateReference");
        }

    };

    private final boolean expected;
    private final IExpression pred;

    private static final IExpression DATE1T;
    private static final IExpression DATE2T;
    private static final IExpression DATE1;
    private static final IExpression DATE2;
    private static final IExpression DATE1S;
    private static final IExpression DATE2S;
    private static final IExpression DATE1CSS;
    private static final IExpression DATE2CSS;
    private static final IExpression ZERO_I = Constant.makeInteger(0);
    private static final IExpression ZERO_D = Constant.makeDouble(0);
    private static final IExpression ONE_I = Constant.makeInteger(1);
    private static final IExpression ONE_D = Constant.makeDouble(1);
    private static final IExpression ONE_S = Constant.makeCsString("1");
    private static final IExpression TWO_I = Constant.makeInteger(2);
    private static final IExpression TWO_D = Constant.makeDouble(2);
    private static final IExpression TWO_S = Constant.makeString("2");
    private static final IExpression YES_S1 = Constant.makeString("Yes");
    private static final IExpression YES_S2 = Constant.makeString("YES");
    private static final IExpression Y_S1 = Constant.makeString("Y");
    private static final IExpression Y_S2 = Constant.makeString("y");
    private static final IExpression TRUE_S1 = Constant.makeString("True");
    private static final IExpression TRUE_S2 = Constant.makeString("true");
    private static final IExpression TRUE_S3 = Constant.makeString("tRuE");
    private static final IExpression FALSE_S1 = Constant.makeString("false");
    private static final IExpression FALSE_S2 = Constant.makeString("False");
    private static final IExpression FALSE_S3 = Constant.makeString("FALSE");
    private static final IExpression STR1CSA = Constant.makeCsString("abc");
    private static final IExpression STR2CSA = Constant.makeCsString("xyz");
    private static final IExpression STR1CSB = Constant.makeCsString("ABC");
    private static final IExpression STR2CSB = Constant.makeCsString("ABC");
    private static final IExpression STR1A = Constant.makeString("Abc");
    private static final IExpression STR2A = Constant.makeString("Xyz");
    private static final IExpression STR1B = Constant.makeString("AbC");
    private static final IExpression STR2B = Constant.makeString("XYz");
    private static final IExpression DIGITS_P = Constant.makeString("?D");
    private static final IExpression NO_DIGITS_P = Constant.makeString("!D");
    private static final IExpression DIGITS = Constant.make(
        WildcardPattern.compile("?D")
    ,   IDataType.UNKNOWN
    );
    private static final IExpression NO_DIGITS = Constant.make(
        WildcardPattern.compile("!D")
    ,   IDataType.UNKNOWN
    );

    static {
        DateFormat df = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM
        );
        long now = System.currentTimeMillis();
        Date date1 = new Date(now-now%1000);
        Date date2 = new Date(now-now%1000+100000);
        DATE1 = Constant.makeDate(date1);
        DATE2 = Constant.makeDate(date2);
        DATE1T = Constant.makeInteger(date1.getTime());
        DATE2T = Constant.makeInteger(date2.getTime());
        DATE1S = Constant.makeString(df.format(date1));
        DATE2S = Constant.makeString(df.format(date2));
        DATE1CSS = Constant.makeCsString(df.format(date1));
        DATE2CSS = Constant.makeCsString(df.format(date2));
    }

    private static final IExpression OBJ1 =
        Constant.make(new Object() {
            public int hashCode() {
                return 1;
            }
        }, IDataType.UNKNOWN);
    private static final IExpression OBJ2 =
        Constant.make(new Object() {
            public int hashCode() {
                return 2;
            }
        }, IDataType.UNKNOWN);

    public RelationEvaluatorTests(
        boolean expected
    ,   String op
    ,   IExpression lhs
    ,   IExpression rhs) {
        this.expected = expected;
        pred = new Relation(RelationOperator.forRepresentation(op), lhs, rhs);
    }

    @Test
    public void verifyRelation() {
        assertEquals(expected, Evaluator.getInstance().evaluate(pred, eh).getValue());
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            // Equality
            {true,  "==", IExpression.NULL, IExpression.NULL}
        ,   {true,  "==", IExpression.TRUE, IExpression.TRUE}
        ,   {false, "==", IExpression.TRUE, IExpression.FALSE}
        ,   {false, "==", IExpression.FALSE, IExpression.TRUE}
        ,   {true,  "==", IExpression.FALSE, IExpression.FALSE}
        ,   {true,  "==", IExpression.TRUE, YES_S1}
        ,   {true,  "==", IExpression.TRUE, YES_S2}
        ,   {true,  "==", IExpression.TRUE, Y_S1}
        ,   {true,  "==", IExpression.TRUE, Y_S2}
        ,   {false, "==", IExpression.TRUE, IExpression.NULL}
        ,   {false, "==", IExpression.FALSE, IExpression.NULL}
        ,   {false, "==", IExpression.NULL, IExpression.TRUE}
        ,   {false, "==", IExpression.NULL, IExpression.FALSE}
        ,   {false, "==", IExpression.TRUE, FALSE_S1}
        ,   {false, "==", IExpression.TRUE, FALSE_S2}
        ,   {false, "==", IExpression.TRUE, FALSE_S3}
        ,   {true,  "==", IExpression.FALSE, FALSE_S1}
        ,   {true,  "==", IExpression.FALSE, FALSE_S2}
        ,   {true,  "==", IExpression.FALSE, FALSE_S3}
        ,   {true,  "==", IExpression.TRUE, TRUE_S1}
        ,   {true,  "==", IExpression.TRUE, TRUE_S2}
        ,   {true,  "==", IExpression.TRUE, TRUE_S3}
        ,   {true,  "==", IExpression.TRUE, ONE_D}
        ,   {true,  "==", IExpression.TRUE, ONE_I}
        ,   {true,  "==", IExpression.FALSE, ZERO_D}
        ,   {true,  "==", IExpression.FALSE, ZERO_I}
        ,   {false, "==", IExpression.FALSE, ONE_D}
        ,   {false, "==", IExpression.FALSE, ONE_I}
        ,   {false, "==", IExpression.TRUE, ZERO_D}
        ,   {false, "==", IExpression.TRUE, ZERO_I}
        ,   {false, "==", IExpression.NULL, ZERO_D}
        ,   {false, "==", IExpression.NULL, ZERO_I}
        ,   {false, "==", IExpression.NULL, STR1A}
        ,   {false, "==", STR1A, IExpression.NULL}
        ,   {false, "==", OBJ1, STR1A}
        ,   {false, "==", STR1A, OBJ1}
        ,   {false, "==", IExpression.NULL, STR1CSA}
        ,   {false, "==", STR1CSA, IExpression.NULL}
        ,   {false, "==", OBJ1, STR1CSA}
        ,   {false, "==", STR1CSA, OBJ1}
        ,   {false, "==", IExpression.NULL, DATE1}
        ,   {false, "==", ONE_I, IExpression.NULL}
        ,   {false, "==", ONE_D, IExpression.NULL}
        ,   {false, "==", ONE_I, TWO_I}
        ,   {false, "==", ONE_I, TWO_D}
        ,   {false, "==", ONE_D, TWO_I}
        ,   {false, "==", ONE_D, TWO_D}
        ,   {true,  "==", ONE_I, ONE_I}
        ,   {true,  "==", ONE_D, ONE_D}
        ,   {false, "==", ONE_I, TWO_S}
        ,   {false, "==", ONE_D, TWO_S}
        ,   {true,  "==", ONE_I, ONE_S}
        ,   {true,  "==", ONE_D, ONE_S}
        ,   {false, "==", ONE_S, TWO_I}
        ,   {false, "==", ONE_S, TWO_D}
        ,   {true,  "==", ONE_S, ONE_I}
        ,   {true,  "==", ONE_S, ONE_D}
        ,   {true,  "==", DATE1, DATE1}
        ,   {true,  "==", DATE1, DATE1S}
        ,   {true,  "==", DATE1S, DATE1}
        ,   {false, "==", DATE1, DATE2S}
        ,   {false, "==", DATE1S, DATE2}
        ,   {true,  "==", DATE1, DATE1CSS}
        ,   {true,  "==", DATE1CSS, DATE1}
        ,   {false, "==", DATE1, DATE2CSS}
        ,   {false, "==", DATE1CSS, DATE2}
        ,   {false, "==", STR1A, DATE2}
        ,   {false, "==", DATE1, STR2A}
        ,   {false, "==", STR1CSA, DATE2}
        ,   {false, "==", DATE1, STR2CSA}
        ,   {false, "==", DATE1, DATE2}
        ,   {false, "==", DATE1, IExpression.NULL}
        ,   {false, "==", IExpression.NULL, DATE1}
        ,   {true,  "==", DATE1, DATE1T}
        ,   {true,  "==", DATE1T, DATE1}
        ,   {false, "==", DATE2, DATE1T}
        ,   {false, "==", DATE1T, DATE2}
        ,   {true,  "==", STR1A, STR1A}
        ,   {false, "==", STR1A, STR2A}
        ,   {true,  "==", STR1CSA, STR1CSA}
        ,   {false, "==", STR1CSA, STR2CSA}
        ,   {true,  "==", STR1A, STR1CSA}
        ,   {true,  "==", STR1A, STR1CSB}
        ,   {true,  "==", STR1CSA, STR1A}
        ,   {true,  "==", STR1CSA, STR1B}
        ,   {false, "==", STR1A, STR2CSA}
        ,   {false, "==", STR1CSA, STR2A}
        ,   {false, "==", ONE_I, STR2A}
        ,   {false, "==", ONE_D, STR2A}
        ,   {false, "==", STR1A, ONE_I}
        ,   {false, "==", STR1A, ONE_D}
        ,   {false, "==", OBJ1, OBJ2}
        ,   {false, "==", IExpression.NULL, OBJ1}
        ,   {false, "==", OBJ1, IExpression.NULL}
        ,   {true,  "==", OBJ1, OBJ1}
            // Non-equality
        ,   {false, "!=", IExpression.NULL, IExpression.NULL}
        ,   {false, "!=", IExpression.TRUE, IExpression.TRUE}
        ,   {true,  "!=", IExpression.TRUE, IExpression.FALSE}
        ,   {true,  "!=", IExpression.FALSE, IExpression.TRUE}
        ,   {false, "!=", IExpression.FALSE, IExpression.FALSE}
        ,   {false, "!=", IExpression.TRUE, YES_S1}
        ,   {false, "!=", IExpression.TRUE, YES_S2}
        ,   {false, "!=", IExpression.TRUE, Y_S1}
        ,   {false, "!=", IExpression.TRUE, Y_S2}
        ,   {true,  "!=", IExpression.TRUE, IExpression.NULL}
        ,   {true,  "!=", IExpression.FALSE, IExpression.NULL}
        ,   {true,  "!=", IExpression.NULL, IExpression.TRUE}
        ,   {true,  "!=", IExpression.NULL, IExpression.FALSE}
        ,   {true,  "!=", IExpression.TRUE, FALSE_S1}
        ,   {true,  "!=", IExpression.TRUE, FALSE_S2}
        ,   {true,  "!=", IExpression.TRUE, FALSE_S3}
        ,   {false, "!=", IExpression.FALSE, FALSE_S1}
        ,   {false, "!=", IExpression.FALSE, FALSE_S2}
        ,   {false, "!=", IExpression.FALSE, FALSE_S3}
        ,   {false, "!=", IExpression.TRUE, TRUE_S1}
        ,   {false, "!=", IExpression.TRUE, TRUE_S2}
        ,   {false, "!=", IExpression.TRUE, TRUE_S3}
        ,   {true,  "!=", IExpression.NULL, ZERO_I}
        ,   {true,  "!=", IExpression.NULL, ZERO_D}
        ,   {true,  "!=", IExpression.NULL, STR1A}
        ,   {true,  "!=", STR1A, IExpression.NULL}
        ,   {true,  "!=", IExpression.NULL, STR1CSA}
        ,   {true,  "!=", STR1CSA, IExpression.NULL}
        ,   {true,  "!=", OBJ1, STR1A}
        ,   {true,  "!=", STR1A, OBJ1}
        ,   {true,  "!=", OBJ1, STR1CSA}
        ,   {true,  "!=", STR1CSA, OBJ1}
        ,   {true,  "!=", IExpression.NULL, DATE1}
        ,   {true,  "!=", ONE_I, IExpression.NULL}
        ,   {true,  "!=", ONE_D, IExpression.NULL}
        ,   {true,  "!=", ONE_I, TWO_I}
        ,   {true,  "!=", ONE_I, TWO_D}
        ,   {true,  "!=", ONE_D, TWO_I}
        ,   {true,  "!=", ONE_D, TWO_D}
        ,   {false, "!=", ONE_I, ONE_I}
        ,   {false, "!=", ONE_I, ONE_D}
        ,   {false, "!=", ONE_D, ONE_I}
        ,   {false, "!=", ONE_D, ONE_D}
        ,   {true,  "!=", ONE_I, TWO_S}
        ,   {false, "!=", ONE_D, ONE_S}
        ,   {false, "!=", ONE_I, ONE_S}
        ,   {true,  "!=", ONE_S, TWO_I}
        ,   {true,  "!=", ONE_S, TWO_D}
        ,   {false, "!=", ONE_S, ONE_I}
        ,   {false, "!=", ONE_S, ONE_D}
        ,   {false, "!=", DATE1, DATE1}
        ,   {false, "!=", DATE1, DATE1S}
        ,   {false, "!=", DATE1S, DATE1}
        ,   {true,  "!=", DATE1, DATE2S}
        ,   {true,  "!=", DATE1S, DATE2}
        ,   {false, "!=", DATE1, DATE1CSS}
        ,   {false, "!=", DATE1CSS, DATE1}
        ,   {true,  "!=", DATE1, DATE2CSS}
        ,   {true,  "!=", DATE1CSS, DATE2}
        ,   {true,  "!=", STR1A, DATE2}
        ,   {true,  "!=", DATE1, STR2A}
        ,   {true,  "!=", STR1CSB, DATE2}
        ,   {true,  "!=", DATE1, STR2CSB}
        ,   {true,  "!=", DATE1, DATE2}
        ,   {true,  "!=", DATE1, IExpression.NULL}
        ,   {true,  "!=", IExpression.NULL, DATE1}
        ,   {false, "!=", DATE1, DATE1T}
        ,   {false, "!=", DATE1T, DATE1}
        ,   {true,  "!=", DATE2, DATE1T}
        ,   {true,  "!=", DATE1T, DATE2}
        ,   {false, "!=", STR1A, STR1A}
        ,   {true,  "!=", STR1A, STR2A}
        ,   {false, "!=", STR1CSA, STR1CSA}
        ,   {true,  "!=", STR1CSA, STR2CSA}
        ,   {false, "!=", STR1A, STR1CSA}
        ,   {false, "!=", STR1A, STR1CSB}
        ,   {false, "!=", STR1CSA, STR1A}
        ,   {false, "!=", STR1CSA, STR1B}
        ,   {true,  "!=", STR1A, STR2CSA}
        ,   {true,  "!=", STR1CSA, STR2A}
        ,   {true,  "!=", ONE_I, STR2A}
        ,   {true,  "!=", ONE_D, STR2A}
        ,   {true,  "!=", STR1A, ONE_I}
        ,   {true,  "!=", STR1A, ONE_D}
        ,   {true,  "!=", ONE_I, STR2CSA}
        ,   {true,  "!=", ONE_D, STR2CSA}
        ,   {true,  "!=", STR1CSA, ONE_I}
        ,   {true,  "!=", STR1CSA, ONE_D}
        ,   {true,  "!=", OBJ1, OBJ2}
        ,   {true,  "!=", IExpression.NULL, OBJ1}
        ,   {true,  "!=", OBJ1, IExpression.NULL}
        ,   {false, "!=", OBJ1, OBJ1}
            // Greater Than
        ,   {false, ">", IExpression.NULL, IExpression.NULL}
        ,   {false, ">", IExpression.NULL, ZERO_I}
        ,   {false, ">", IExpression.NULL, ZERO_D}
        ,   {false, ">", IExpression.NULL, STR1A}
        ,   {true,  ">", STR1A, IExpression.NULL}
        ,   {false, ">", OBJ1, STR1B}
        ,   {false, ">", STR1B, OBJ1}
        ,   {false, ">", IExpression.NULL, STR1CSA}
        ,   {true,  ">", STR1CSA, IExpression.NULL}
        ,   {false, ">", OBJ1, STR1CSB}
        ,   {false, ">", STR1CSB, OBJ1}
        ,   {false, ">", IExpression.NULL, DATE1}
        ,   {true,  ">", ONE_I, IExpression.NULL}
        ,   {true,  ">", ONE_D, IExpression.NULL}
        ,   {false, ">", ONE_I, OBJ1}
        ,   {false, ">", ONE_D, OBJ1}
        ,   {true,  ">", TWO_I, ONE_I}
        ,   {true,  ">", TWO_I, ONE_D}
        ,   {true,  ">", TWO_D, ONE_I}
        ,   {true,  ">", TWO_D, ONE_D}
        ,   {false, ">", ONE_I, ONE_I}
        ,   {false, ">", ONE_I, ONE_D}
        ,   {false, ">", ONE_D, ONE_I}
        ,   {false, ">", ONE_D, ONE_D}
        ,   {true,  ">", TWO_I, ONE_S}
        ,   {true,  ">", TWO_D, ONE_S}
        ,   {false, ">", ONE_I, ONE_S}
        ,   {false, ">", ONE_D, ONE_S}
        ,   {true,  ">", TWO_S, ONE_I}
        ,   {true,  ">", TWO_S, ONE_D}
        ,   {false, ">", ONE_S, ONE_I}
        ,   {false, ">", ONE_S, ONE_D}
        ,   {false, ">", DATE1, DATE1}
        ,   {false, ">", DATE1, DATE1S}
        ,   {false, ">", DATE1S, DATE1}
        ,   {true,  ">", DATE2, DATE1S}
        ,   {true,  ">", DATE2S, DATE1}
        ,   {false, ">", DATE1, DATE1CSS}
        ,   {false, ">", DATE1CSS, DATE1}
        ,   {true,  ">", DATE2, DATE1CSS}
        ,   {true,  ">", DATE2CSS, DATE1}
        ,   {false, ">", STR1A, DATE2}
        ,   {false, ">", DATE1, STR2B}
        ,   {false, ">", STR1CSB, DATE2}
        ,   {false, ">", DATE1, STR2CSA}
        ,   {true,  ">", DATE2, DATE1}
        ,   {false, ">", DATE1, DATE2}
        ,   {true,  ">", DATE1, IExpression.NULL}
        ,   {false, ">", DATE1, OBJ1}
        ,   {false, ">", IExpression.NULL, DATE1}
        ,   {false, ">", OBJ1, DATE1}
        ,   {false, ">", DATE1, DATE1T}
        ,   {false, ">", DATE1T, DATE1}
        ,   {true,  ">", DATE2, DATE1T}
        ,   {true,  ">", DATE2T, DATE1}
        ,   {false, ">", STR1A, STR1A}
        ,   {true,  ">", STR2A, STR1A}
        ,   {false, ">", STR1A, STR2A}
        ,   {false, ">", STR1A, STR1CSA}
        ,   {false,  ">", STR1A, STR2CSB}
        ,   {false,  ">", STR2CSB, STR1A}
        ,   {false, ">", STR1CSA, STR2CSB}
        ,   {false, ">", STR2CSB, STR1CSA}
        // TODO: Deal with case-sensitivity
        ,   {false, ">", ONE_I, STR2A}
        ,   {false, ">", ONE_D, STR2A}
        ,   {false, ">", STR1A, ONE_I}
        ,   {false, ">", STR1A, ONE_D}
        ,   {false, ">", ONE_I, STR2CSA}
        ,   {false, ">", ONE_D, STR2CSA}
        ,   {false, ">", STR1CSA, ONE_I}
        ,   {false, ">", STR1CSA, ONE_D}
        ,   {false, ">", OBJ1, OBJ2}
        ,   {false, ">", IExpression.NULL, OBJ1}
        ,   {true,  ">", OBJ1, IExpression.NULL}
        ,   {false, ">", OBJ1, OBJ1}
        // Greater Than or Equals
        ,   {true,  ">=", IExpression.NULL, IExpression.NULL}
        ,   {false, ">=", IExpression.NULL, ZERO_I}
        ,   {false, ">=", IExpression.NULL, ZERO_D}
        ,   {false, ">=", IExpression.NULL, STR1A}
        ,   {true,  ">=", STR1A, IExpression.NULL}
        ,   {false, ">=", OBJ1, STR1A}
        ,   {false, ">=", STR1A, OBJ1}
        ,   {false, ">=", IExpression.NULL, STR1CSA}
        ,   {true,  ">=", STR1CSA, IExpression.NULL}
        ,   {false, ">=", OBJ1, STR1CSA}
        ,   {false, ">=", STR1CSA, OBJ1}
        ,   {false, ">=", IExpression.NULL, DATE1}
        ,   {true,  ">=", ONE_I, IExpression.NULL}
        ,   {true,  ">=", ONE_D, IExpression.NULL}
        ,   {false, ">=", ONE_I, OBJ1}
        ,   {false, ">=", ONE_D, OBJ1}
        ,   {true,  ">=", TWO_I, ONE_I}
        ,   {true,  ">=", TWO_I, ONE_D}
        ,   {true,  ">=", TWO_D, ONE_I}
        ,   {true,  ">=", TWO_D, ONE_D}
        ,   {false, ">=", ONE_I, TWO_I}
        ,   {false, ">=", ONE_I, TWO_D}
        ,   {false, ">=", ONE_D, TWO_I}
        ,   {false, ">=", ONE_D, TWO_D}
        ,   {true,  ">=", TWO_I, ONE_S}
        ,   {true,  ">=", TWO_D, ONE_S}
        ,   {false, ">=", ONE_I, TWO_S}
        ,   {false, ">=", ONE_D, TWO_S}
        ,   {true,  ">=", TWO_S, ONE_I}
        ,   {true,  ">=", TWO_S, ONE_D}
        ,   {false, ">=", ONE_S, TWO_I}
        ,   {false, ">=", ONE_S, TWO_D}
        ,   {true,  ">=", DATE1, DATE1}
        ,   {false, ">=", DATE1, DATE2S}
        ,   {false, ">=", DATE1S, DATE2}
        ,   {true,  ">=", DATE2, DATE1S}
        ,   {true,  ">=", DATE2S, DATE1}
        ,   {false, ">=", DATE1, DATE2CSS}
        ,   {false, ">=", DATE1CSS, DATE2}
        ,   {true,  ">=", DATE2, DATE1CSS}
        ,   {true,  ">=", DATE2CSS, DATE1}
        ,   {false, ">=", STR1A, DATE2}
        ,   {false, ">=", DATE1, STR2A}
        ,   {false, ">=", STR1CSA, DATE2}
        ,   {false, ">=", DATE1, STR2CSA}
        ,   {true,  ">=", DATE2, DATE1}
        ,   {false, ">=", DATE1, DATE2}
        ,   {true,  ">=", DATE1, IExpression.NULL}
        ,   {false, ">=", DATE1, OBJ1}
        ,   {false, ">=", IExpression.NULL, DATE1}
        ,   {false, ">=", OBJ1, DATE1}
        ,   {false, ">=", DATE1, DATE2T}
        ,   {false, ">=", DATE1T, DATE2}
        ,   {true,  ">=", DATE2, DATE1T}
        ,   {true,  ">=", DATE2T, DATE1}
        ,   {true,  ">=", STR1A, STR1A}
        ,   {true,  ">=", STR2A, STR1A}
        ,   {false, ">=", STR1A, STR2A}
        // TODO: Deal with case-sensitivity
        ,   {false, ">=", ONE_I, STR2A}
        ,   {false, ">=", ONE_D, STR2A}
        ,   {false, ">=", STR1A, ONE_I}
        ,   {false, ">=", STR1A, ONE_D}
        ,   {false, ">=", ONE_I, STR2CSA}
        ,   {false, ">=", ONE_D, STR2CSA}
        ,   {false, ">=", STR1CSA, ONE_I}
        ,   {false, ">=", STR1CSA, ONE_D}
        ,   {false, ">=", OBJ1, OBJ2}
        ,   {false, ">=", IExpression.NULL, OBJ1}
        ,   {true,  ">=", OBJ1, IExpression.NULL}
        ,   {true,  ">=", OBJ1, OBJ1}
        // Greater Than or Equals
        ,   {false, "<", IExpression.NULL, IExpression.NULL}
        ,   {true,  "<", IExpression.NULL, ZERO_I}
        ,   {true,  "<", IExpression.NULL, ZERO_D}
        ,   {true,  "<", IExpression.NULL, STR1A}
        ,   {false, "<", STR1A, IExpression.NULL}
        ,   {false, "<", OBJ1, STR1B}
        ,   {false, "<", STR1B, OBJ1}
        ,   {true,  "<", IExpression.NULL, STR1CSA}
        ,   {false, "<", STR1CSA, IExpression.NULL}
        ,   {false, "<", OBJ1, STR1CSB}
        ,   {false, "<", STR1CSB, OBJ1}
        ,   {true,  "<", IExpression.NULL, DATE1}
        ,   {false, "<", ONE_I, IExpression.NULL}
        ,   {false, "<", ONE_D, OBJ1}
        ,   {false, "<", ONE_I, OBJ1}
        ,   {false, "<", OBJ1, ONE_I}
        ,   {false, "<", OBJ1, ONE_D}
        ,   {false, "<", TWO_I, ONE_I}
        ,   {false, "<", TWO_I, ONE_D}
        ,   {false, "<", TWO_D, ONE_I}
        ,   {false, "<", TWO_D, ONE_I}
        ,   {true,  "<", ONE_I, TWO_I}
        ,   {true,  "<", ONE_I, TWO_D}
        ,   {true,  "<", ONE_D, TWO_I}
        ,   {true,  "<", ONE_D, TWO_D}
        ,   {false, "<", TWO_I, ONE_S}
        ,   {false, "<", TWO_D, ONE_S}
        ,   {true,  "<", ONE_I, TWO_S}
        ,   {true,  "<", ONE_D, TWO_S}
        ,   {false, "<", TWO_S, ONE_I}
        ,   {false, "<", TWO_S, ONE_D}
        ,   {true,  "<", ONE_S, TWO_I}
        ,   {true,  "<", ONE_S, TWO_D}
        ,   {false, "<", DATE1, DATE1}
        ,   {true,  "<", DATE1, DATE2S}
        ,   {true,  "<", DATE1S, DATE2}
        ,   {false, "<", DATE2, DATE1S}
        ,   {false, "<", DATE2S, DATE1}
        ,   {true,  "<", DATE1, DATE2CSS}
        ,   {true,  "<", DATE1CSS, DATE2}
        ,   {false, "<", DATE2, DATE1CSS}
        ,   {false, "<", DATE2CSS, DATE1}
        ,   {false, "<", STR1B, DATE2}
        ,   {false, "<", DATE1, STR2B}
        ,   {false, "<", STR1CSB, DATE2}
        ,   {false, "<", DATE1, STR2CSB}
        ,   {false, "<", DATE2, DATE1}
        ,   {true,  "<", DATE1, DATE2}
        ,   {false, "<", DATE1, IExpression.NULL}
        ,   {false, "<", DATE1, OBJ1}
        ,   {true,  "<", IExpression.NULL, DATE1}
        ,   {false, "<", OBJ1, DATE1}
        ,   {true,  "<", DATE1, DATE2T}
        ,   {true,  "<", DATE1T, DATE2}
        ,   {false, "<", DATE2, DATE1T}
        ,   {false, "<", DATE2T, DATE1}
        ,   {false, "<", STR1A, STR1A}
        ,   {false, "<", STR2A, STR1A}
        ,   {true,  "<", STR1A, STR2A}
        // TODO: Deal with case-sensitivity
        ,   {false, "<", ONE_I, STR2A}
        ,   {false, "<", ONE_D, STR2A}
        ,   {false, "<", STR1A, ONE_I}
        ,   {false, "<", STR1A, ONE_D}
        ,   {false, "<", ONE_I, STR2CSA}
        ,   {false, "<", ONE_D, STR2CSA}
        ,   {false, "<", STR1CSA, ONE_I}
        ,   {false, "<", STR1CSA, ONE_D}
        ,   {true, "<", OBJ1, OBJ2}
        ,   {true,  "<", IExpression.NULL, OBJ1}
        ,   {false, "<", OBJ1, IExpression.NULL}
        ,   {false, "<", OBJ1, OBJ1}
        // Greater Than
        ,   {true,  "<=", IExpression.NULL, IExpression.NULL}
        ,   {true,  "<=", IExpression.NULL, ZERO_I}
        ,   {true,  "<=", IExpression.NULL, ZERO_D}
        ,   {true,  "<=", IExpression.NULL, STR1A}
        ,   {false, "<=", STR1A, IExpression.NULL}
        ,   {false, "<=", OBJ1, STR1A}
        ,   {false, "<=", STR1A, OBJ1}
        ,   {true,  "<=", IExpression.NULL, STR1CSA}
        ,   {false, "<=", STR1CSA, IExpression.NULL}
        ,   {false, "<=", OBJ1, STR1CSB}
        ,   {false, "<=", STR1CSB, OBJ1}
        ,   {true,  "<=", IExpression.NULL, DATE1}
        ,   {false, "<=", ONE_I, IExpression.NULL}
        ,   {false, "<=", ONE_D, IExpression.NULL}
        ,   {false, "<=", ONE_I, OBJ1}
        ,   {false, "<=", ONE_D, OBJ1}
        ,   {false, "<=", TWO_I, ONE_I}
        ,   {false, "<=", TWO_I, ONE_D}
        ,   {false, "<=", TWO_D, ONE_I}
        ,   {false, "<=", TWO_D, ONE_D}
        ,   {true,  "<=", ONE_I, ONE_I}
        ,   {true,  "<=", ONE_I, ONE_D}
        ,   {true,  "<=", ONE_D, ONE_I}
        ,   {true,  "<=", ONE_D, ONE_D}
        ,   {false, "<=", TWO_I, ONE_S}
        ,   {false, "<=", TWO_D, ONE_S}
        ,   {true,  "<=", ONE_I, ONE_S}
        ,   {true,  "<=", ONE_D, ONE_S}
        ,   {false, "<=", TWO_S, ONE_I}
        ,   {false, "<=", TWO_S, ONE_D}
        ,   {true,  "<=", ONE_S, ONE_I}
        ,   {true,  "<=", ONE_S, ONE_D}
        ,   {true,  "<=", DATE1, DATE1}
        ,   {true,  "<=", DATE1, DATE1S}
        ,   {true,  "<=", DATE1S, DATE1}
        ,   {false, "<=", DATE2, DATE1S}
        ,   {false, "<=", DATE2S, DATE1}
        ,   {true,  "<=", DATE1, DATE1CSS}
        ,   {true,  "<=", DATE1CSS, DATE1}
        ,   {false, "<=", DATE2, DATE1CSS}
        ,   {false, "<=", DATE2CSS, DATE1}
        ,   {false, "<=", STR1A, DATE2}
        ,   {false, "<=", DATE1, STR2A}
        ,   {false, "<=", STR1CSA, DATE2}
        ,   {false, "<=", DATE1, STR2CSA}
        ,   {false, "<=", DATE2, DATE1}
        ,   {true,  "<=", DATE1, DATE2}
        ,   {false, "<=", DATE1, IExpression.NULL}
        ,   {false, "<=", DATE1, OBJ1}
        ,   {true,  "<=", IExpression.NULL, DATE1}
        ,   {false, "<=", OBJ1, DATE1}
        ,   {true,  "<=", DATE1, DATE1T}
        ,   {true,  "<=", DATE1T, DATE1}
        ,   {false, "<=", DATE2, DATE1T}
        ,   {false, "<=", DATE2T, DATE1}
        ,   {true,  "<=", STR1A, STR1A}
        ,   {false, "<=", STR2A, STR1A}
        ,   {true,  "<=", STR1A, STR2A}
        // TODO: Deal with case-sensitivity
        ,   {false, "<=", ONE_I, STR2A}
        ,   {false, "<=", ONE_D, STR2A}
        ,   {false, "<=", STR1A, ONE_I}
        ,   {false, "<=", STR1A, ONE_D}
        ,   {false, "<=", ONE_I, STR2CSB}
        ,   {false, "<=", ONE_D, STR2CSB}
        ,   {false, "<=", STR1CSB, ONE_I}
        ,   {false, "<=", STR1CSB, ONE_D}
        ,   {true, "<=", OBJ1, OBJ2}
        ,   {true,  "<=", IExpression.NULL, OBJ1}
        ,   {false, "<=", OBJ1, IExpression.NULL}
        ,   {true,  "<=", OBJ1, OBJ1}
        // Matches
        ,   {true,  "matches", ONE_S, DIGITS}
        ,   {false, "matches", YES_S1, DIGITS}
        ,   {true,  "matches", YES_S1, NO_DIGITS}
        ,   {false, "matches", ONE_S, NO_DIGITS}
        ,   {false, "matches", DIGITS, ONE_S}
        ,   {false, "matches", DIGITS, YES_S1}
        ,   {false, "matches", NO_DIGITS, YES_S1}
        ,   {false, "matches", NO_DIGITS, ONE_S}
        ,   {true,  "matches", ONE_S, DIGITS_P}
        ,   {false, "matches", YES_S1, DIGITS_P}
        ,   {true,  "matches", YES_S1, NO_DIGITS_P}
        ,   {false, "matches", ONE_S, NO_DIGITS_P}
        ,   {false, "matches", DIGITS_P, ONE_S}
        ,   {false, "matches", DIGITS_P, YES_S1}
        ,   {false, "matches", NO_DIGITS_P, YES_S1}
        ,   {false, "matches", NO_DIGITS_P, ONE_S}
        // Does not match
        // Is included
        // Is not included
        });
    }

}
