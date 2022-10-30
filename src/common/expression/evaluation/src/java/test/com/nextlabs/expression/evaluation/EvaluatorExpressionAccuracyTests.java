package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/test/com/nextlabs/expression/evaluation/EvaluatorExpressionAccuracyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IExpressionVisitor;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.IPolicyParserFactory;
import com.nextlabs.language.parser.PolicyParserFactory;
import com.nextlabs.util.ref.IReference;

/**
 * Accuracy tests for the expression part of the evaluator.
 *
 * @author Sergey Kalinichenko
 */

@RunWith(value=Parameterized.class)
@SuiteClasses(value={EvaluatorExpressionAccuracyTests.class})

public class EvaluatorExpressionAccuracyTests {

    private interface ITestFunction {
        void eval(String[] a, Object[] v, IDataType[] t, IEvaluatorCallback c);
    }

    private static final IPolicyParserFactory pf = new PolicyParserFactory();

    private static final IEvaluationHandler eh = new IEvaluationHandler() {

        public void evaluateAttribute(
            String name
        ,   Object value
        ,   IDataType type
        ,   IEvaluatorCallback eval) {
            Map<?,?> m = (Map<?,?>)value;
            Object val = m.get(name);
            IDataType attrType = null;
            if (val instanceof String) {
                attrType = IDataType.STRING;
            } else if (val instanceof Long) {
                attrType = IDataType.INTEGER;
            } else if (val instanceof Date) {
                attrType = IDataType.DATE;
            } else {
                fail("Unexpected value type: "+val);
            }
            eval.addValue(val, attrType);
        }

        public void evaluateCustomExpression(
            IExpression custom
        ,   IEvaluatorCallback eval) {
            eval.addValue(123L, IDataType.INTEGER);
        }

        public void evaluateFunction(
            IReference<IFunction> function
        ,   String[] argNames
        ,   Object[] argValues
        ,   IDataType[] argTypes
        ,   int argOffset
        ,   IEvaluatorCallback c) {
            String fName = function.getPath().get(0);
            if (fName.equals("custom")) {
                c.addExpression(
                    new IExpression() {
                        public void accept(IExpressionVisitor visitor) {
                            visitor.visitExpression(this);
                        }
                    }
                );
                return;
            }
            int n = argNames.length;
            Object[] v = new Object[n];
            IDataType[] t = new IDataType[n];
            System.arraycopy(argValues, argOffset, v, 0, n);
            System.arraycopy(argTypes, argOffset, t, 0, n);
            functions.get(fName).eval(argNames, v, t, c);
        }

        public void evaluateReference(
            IExpressionReference ref
        ,   IEvaluatorCallback eval) {
            // Ignore the way the data type is constructed - the type
            // is ignored in the remaining tests below.
            eval.addValue(
                vars.get(ref.getReference().getPath().get(0))
            ,   DataType.makeReference(ref.getReference())
            );
        }

    };

    private final IExpression expr;
    private final IDataType expectedType;
    private final Object expectedValue;

    public EvaluatorExpressionAccuracyTests(
        String expression
    ,   Object expectedValue
    ,   IDataType expectedType) throws Exception {
        IPolicyLanguageParser p = pf.getParser(1);
        expr = p.parseExpression(new StringReader(expression));
        this.expectedValue = expectedValue;
        this.expectedType = expectedType;
    }

    @Test
    public void verifyExpression() {
        IEvaluator eval = Evaluator.getInstance();
        IEvaluationResult res = eval.evaluate(expr, eh);
        assertEquals(expectedType, res.getType());
        if (expectedType.isDouble()) {
            // JUnit has a separate method for comparing doubles
            double expectedDouble = (Double)expectedValue;
            double actualDouble = (Double)res.getValue();
            assertEquals(expectedDouble, actualDouble, 1e-9);
        } else {
            assertEquals(expectedValue, res.getValue());
        }
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][] {
            {"null", null, IDataType.NULL}
        ,   {"0", 0L, IDataType.INTEGER}
        ,   {"- 1", -1L, IDataType.INTEGER}
        ,   {"- 1.0", -1.0, IDataType.DOUBLE}
        ,   {"-true", false, IDataType.BOOLEAN}
        ,   {"-false", true, IDataType.BOOLEAN}
        ,   {"null+null", null, IDataType.NULL}
        ,   {"null-null", null, IDataType.NULL}
        ,   {"null*null", null, IDataType.NULL}
        ,   {"null/null", null, IDataType.NULL}
        ,   {"null%null", null, IDataType.NULL}
        ,   {"true+false", true, IDataType.BOOLEAN}
        ,   {"false+false", false, IDataType.BOOLEAN}
        ,   {"true+true", true, IDataType.BOOLEAN}
        ,   {"null+2", 2L, IDataType.INTEGER}
        ,   {"3+null", 3L, IDataType.INTEGER}
        ,   {"true+null", true, IDataType.BOOLEAN}
        ,   {"null+false", false, IDataType.BOOLEAN}
        ,   {"1.3+null", 1.3, IDataType.DOUBLE}
        ,   {"null+3.1", 3.1, IDataType.DOUBLE}
        ,   {"1+1", 2L, IDataType.INTEGER}
        ,   {"2+1.0", 3.0, IDataType.DOUBLE}
        ,   {"1.0+4", 5.0, IDataType.DOUBLE}
        ,   {"1.0+2.0", 3.0, IDataType.DOUBLE}
        ,   {"null-5", -5L, IDataType.INTEGER}
        ,   {"null-5.5", -5.5, IDataType.DOUBLE}
        ,   {"13-null", 13L, IDataType.INTEGER}
        ,   {"2.4-null", 2.4, IDataType.DOUBLE}
        ,   {"true-null", true, IDataType.BOOLEAN}
        ,   {"false-null", false, IDataType.BOOLEAN}
        ,   {"null-true", false, IDataType.BOOLEAN}
        ,   {"null-false", true, IDataType.BOOLEAN}
        ,   {"1-3", -2L, IDataType.INTEGER}
        ,   {"4-1.0", 3.0, IDataType.DOUBLE}
        ,   {"6.0-4", 2.0, IDataType.DOUBLE}
        ,   {"1.0-2.0", -1.0, IDataType.DOUBLE}
        ,   {"true*true", true, IDataType.BOOLEAN}
        ,   {"true*false", false, IDataType.BOOLEAN}
        ,   {"false*true", false, IDataType.BOOLEAN}
        ,   {"false*false", false, IDataType.BOOLEAN}
        ,   {"null*3", 3L, IDataType.INTEGER}
        ,   {"2*null", 2L, IDataType.INTEGER}
        ,   {"null*true", true, IDataType.BOOLEAN}
        ,   {"null*false", false, IDataType.BOOLEAN}
        ,   {"true*null", true, IDataType.BOOLEAN}
        ,   {"false*null", false, IDataType.BOOLEAN}
        ,   {"2.3*null", 2.3, IDataType.DOUBLE}
        ,   {"null*3.2", 3.2, IDataType.DOUBLE}
        ,   {"2*3", 6L, IDataType.INTEGER}
        ,   {"2*2.0", 4.0, IDataType.DOUBLE}
        ,   {"4.0*7", 28.0, IDataType.DOUBLE}
        ,   {"1e3*1e5", 1e8, IDataType.DOUBLE}
        ,   {"3/null", 3L, IDataType.INTEGER}
        ,   {"3.9/null", 3.9, IDataType.DOUBLE}
        ,   {"null/1", 1L, IDataType.INTEGER}
        ,   {"null/0", Double.POSITIVE_INFINITY, IDataType.DOUBLE}
        ,   {"null/2", 0L, IDataType.INTEGER}
        ,   {"null/1.0", 1.0, IDataType.DOUBLE}
        ,   {"null/0.0", Double.POSITIVE_INFINITY, IDataType.DOUBLE}
        ,   {"null/-0.0", Double.NEGATIVE_INFINITY, IDataType.DOUBLE}
        ,   {"null/2.0", .5, IDataType.DOUBLE}
        ,   {"3/2", 1L, IDataType.INTEGER}
        ,   {"2/2.0", 1.0, IDataType.DOUBLE}
        ,   {"4.0/2", 2.0, IDataType.DOUBLE}
        ,   {"1e5/1e3", 1e2, IDataType.DOUBLE}
        ,   {"8%null", 8L, IDataType.INTEGER}
        ,   {"8.3%null", 8.3, IDataType.DOUBLE}
        ,   {"true%null", true, IDataType.BOOLEAN}
        ,   {"false%null", false, IDataType.BOOLEAN}
        ,   {"8%3", 2L, IDataType.INTEGER}
        ,   {"12%5.0", 2.0, IDataType.DOUBLE}
        ,   {"19.0%7", 5.0, IDataType.DOUBLE}
        ,   {"1e3%1e5", 1e3, IDataType.DOUBLE}
        ,   {"1/0", Double.POSITIVE_INFINITY, IDataType.DOUBLE}
        ,   {"-1/0", Double.NEGATIVE_INFINITY, IDataType.DOUBLE}
        ,   {"1e0/0", Double.POSITIVE_INFINITY, IDataType.DOUBLE}
        ,   {"-1e0/0", Double.NEGATIVE_INFINITY, IDataType.DOUBLE}
        ,   {"1%0", Double.NaN, IDataType.DOUBLE}
        ,   {"-1%0", Double.NaN, IDataType.DOUBLE}
        ,   {"1e0%0", Double.NaN, IDataType.DOUBLE}
        ,   {"-1e0%0", Double.NaN, IDataType.DOUBLE}
        ,   {"1+2*3", 7L, IDataType.INTEGER}
        ,   {"(1+2)*3", 9L, IDataType.INTEGER}
        ,   {"1+(2+(3+(4+(5+(6+(7+(8+(9+(10+(11+(12+(13+(14+15)))))))))))))"
            , 120L, IDataType.INTEGER}
        ,   {"3+max(2,3,4,5,6)+1", 10L, IDataType.INTEGER}
        ,   {"3+min((2,3,4,5,6))+1", 6L, IDataType.INTEGER}
        ,   {"(1+2)*(3%4)-(60/16)*(3+11)", -33L, IDataType.INTEGER}
        ,   {"max(1+2,3+4,5+6,7+8,9*10)", 90L, IDataType.INTEGER}
        ,   {"max(1+2,(3+4)*(9+11),5+6,7+8,9*10)", 140L, IDataType.INTEGER}
        ,   {"user.firstName", "Jimmy", IDataType.STRING}
        ,   {"host.installed", new Date(12345), IDataType.DATE}
        ,   {"user.yearsServed*15", 60L, IDataType.INTEGER}
        ,   {"max(user.yearsServed, 1, 2, 3)", 4L, IDataType.INTEGER}
        ,   {"custom()", 123L, IDataType.INTEGER}
        ,   {"max(12,custom())", 123L, IDataType.INTEGER}
        ,   {"max(12,custom(custom(custom())))", 123L, IDataType.INTEGER}
        });
    }

    private static final Map<String,ITestFunction> functions =
        new HashMap<String,ITestFunction>();

    static {
        // Calculates the max of all arguments passed in;
        // expects integer arguments.
        functions.put("max", new ITestFunction() {
            public void eval(
                String[] names
            ,   Object[] values
            ,   IDataType[] types
            ,   IEvaluatorCallback callback) {
                long res = Long.MIN_VALUE;
                for (Object v : values) {
                    res = Math.max(res, (Long)v);
                }
                callback.addValue(res, IDataType.INTEGER);
            }
        });
        // Calculates the min of the values in the set passed in
        // as the first argument of the function.
        functions.put("min", new ITestFunction() {
            public void eval(
                String[] names
            ,   Object[] values
            ,   IDataType[] types
            ,   IEvaluatorCallback callback) {
                long res = Long.MAX_VALUE;
                Iterable<?> set = (Iterable<?>)values[0];
                for (Object v : set) {
                    res = Math.min(res, (Long)v);
                }
                callback.addValue(res, IDataType.INTEGER);
            }
        });
    }

    private static final Map<String,Map<String,Object>> vars =
        new HashMap<String,Map<String,Object>>();

    static {
        Map<String,Object> user = new HashMap<String,Object>();
        user.put("firstName", "Jimmy");
        user.put("yearsServed", 4L);
        vars.put("user", user);
        Map<String,Object> host = new HashMap<String,Object>();
        host.put("installed", new Date(12345));
        vars.put("host", host);
    }

}
