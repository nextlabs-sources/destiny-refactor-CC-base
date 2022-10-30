package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/test/com/nextlabs/expression/util/FeatureTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.nextlabs.expression.representation.AttributeReference;
import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.FunctionCall;
import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionVisitor;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.Relation;
import com.nextlabs.expression.representation.RelationOperator;
import com.nextlabs.language.parser.IPolicyLanguageParser;
import com.nextlabs.language.parser.PolicyParserFactory;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Feature tests for the default implementations of detectors
 * and transformers.
 *
 * @author Sergey Kalinichenko
 */
public class FeatureTests {

    private static final IExpressionTransformer et =
        IExpressionTransformer.DEFAULT;

    private static final IExpressionTransformer pt =
        IExpressionTransformer.DEFAULT;

    private static final IExpression CUSTOM_EXPR = new IExpression() {
        public void accept(IExpressionVisitor visitor) {
            visitor.visitExpression(this);
        }
    };

    private static final IReference<IFunction> REF1 =
        IReferenceFactory.DEFAULT.create(1, IFunction.class);

    private static final IReference<IFunction> REF2 =
        IReferenceFactory.DEFAULT.create(2, IFunction.class);

    @Test
    public void defaultExpressionDetector() {
        IExpressionDetector d = IExpressionDetector.DEFAULT;
        assertNotNull(d);
        assertFalse(d.checkAttributeReference(null));
        assertFalse(d.checkUnary(null));
        assertFalse(d.checkComposite(null));
        assertFalse(d.checkConstant(null));
        assertFalse(d.checkExpression(null));
        assertFalse(d.checkFunction(null));
        assertFalse(d.checkReference(null));

    }

    @Test
    public void defaultPredicateDetector() {
        IExpressionDetector d = IExpressionDetector.DEFAULT;
        assertNotNull(d);
        assertFalse(d.checkComposite(null));
        assertFalse(d.checkConstant(null));
        assertFalse(d.checkUnary(null));
        assertFalse(d.checkReference(null));
        assertFalse(d.checkRelation(null));
    }

    @Test
    public void defaultExpressionTransformer() {
        assertNotNull(et);
    }

    @Test
    public void defaultExpressionTransformerArttributes() {
        IAttributeReference a = new AttributeReference(IExpression.NULL, "a");
        assertSame(a, et.transformAttributeReference(a));
    }

    @Test
    public void defaultExpressionTransformerExpression() {
        assertNull(et.transformExpression(null));
    }

    @Test
    public void defaultExpressionTransformerFunction() {
        IFunctionCall fc1 = new FunctionCall(REF1);
        IFunctionCall fc2 = new FunctionCall(REF2);
        assertSame(fc2, et.transformFunction(fc1, fc2));
        assertSame(fc1, et.transformFunction(fc1, fc1));
    }

    @Test
    public void defaultExpressionTransformerConstant() {
        assertSame(IExpression.TRUE, pt.transformConstant(IExpression.TRUE));
    }

    @Test
    public void defaultExpressionTransformerRelation() {
        IRelation r1 = new Relation(
            RelationOperator.EQUAL, IExpression.TRUE, IExpression.FALSE
        );
        IRelation r2 = new Relation(
            RelationOperator.EQUAL, IExpression.TRUE, IExpression.FALSE
        );
        assertSame(r2, pt.transformRelation(r1, r2));
        assertSame(r1, pt.transformRelation(r1, r1));
    }

    @Test(expected=NullPointerException.class)
    public void nullPredicateDetector() {
        Expressions.find(IExpression.FALSE, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullPredicateAndExpressionDetectors() {
        Expressions.find(IExpression.FALSE, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullExpressionDetector() {
        Expressions.find(IExpression.NULL, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullPredicateAndExpressionTransforms() {
        Expressions.transform(IExpression.FALSE, null, false);
    }

    @Test(expected=NullPointerException.class)
    public void nullExpressionTransformer() {
        Expressions.transform(IExpression.NULL, null);
    }

    @Test
    public void visitExpressionSearch() {
        assertFalse(Expressions.find(CUSTOM_EXPR,IExpressionDetector.DEFAULT));
    }

    @Test
    public void visitExpressionTransform() {
        assertSame(CUSTOM_EXPR, Expressions.transform(CUSTOM_EXPR, et));
    }

    @Test(expected=NullPointerException.class)
    public void nullPredicateInNonOptimizingTransformation() {
        Expressions.transform(
            IExpression.FALSE
        ,   new DefaultExpressionTransformer() {
                @Override
                public IExpression transformConstant(Constant c) {
                    return null;
                }
            }
        ,   false
        );
    }

    @Test(expected=NullPointerException.class)
    public void nullExpressionInTransformation() {
        Expressions.transform(
            new Relation(
                RelationOperator.EQUAL
            ,   IExpression.NULL
            ,   IExpression.NULL
            )
        ,   new DefaultExpressionTransformer() {
                @Override
                public IExpression transformConstant(Constant c) {
                    return null;
                }
        }
        ,   false
        );
    }

    @Test
    public void expressionConstructorCoverage() throws Exception {
        Constructor<Expressions> c=Expressions.class.getDeclaredConstructor();
        assertEquals(Modifier.PRIVATE, c.getModifiers() & Modifier.PRIVATE);
        c.setAccessible(true);
        c.newInstance();
    }

    @Test
    public void functionArgToString() throws Exception {
        IPolicyLanguageParser parser = new PolicyParserFactory().getParser(1);
        IExpressionTransformer tx = new DefaultExpressionTransformer() {
            @Override
            public IExpression transformConstant(Constant constant) {
                if (constant == IExpression.NULL) {
                    return Constant.makeInteger(0L);
                } else {
                    return constant;
                }
            }
        };
        IExpression f = parser.parseExpression(new StringReader("f(null)"));
        IExpression t = Expressions.transform(f, tx);
        assertEquals("#f(0)", t.toString());
        f = parser.parseExpression(new StringReader("f(a=null)"));
        t = Expressions.transform(f, tx);
        assertEquals("#f(a=0)", t.toString());
    }

}
