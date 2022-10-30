package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/test/com/nextlabs/language/formatter/ExpressionFormatterTests.java#1 $
 */

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.AttributeReference;
import com.nextlabs.expression.representation.BinaryOperator;
import com.nextlabs.expression.representation.CompositeExpression;
import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.ExpressionReference;
import com.nextlabs.expression.representation.FunctionCall;
import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the predicate formatting functionality of the ACPL formatter.
 *
 * @author Sergey Kalinichenko
 */
public class ExpressionFormatterTests {

    private IPolicyLanguageFormatter f;

    @Before
    public void prepare() throws PolicyLanguageException {
        IPolicyFormatterFactory factory = new PolicyFormatterFactory();
        f = factory.getFormatter("acpl", 1);
    }

    @Test
    public void nullConstant() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, IExpression.NULL);
        assertEquals("null", w.toString());
    }

    @Test
    public void doubleConstant() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, Constant.makeDouble(5.5));
        assertEquals("5.5", w.toString());
    }

    @Test
    public void integerConstant() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, Constant.makeInteger(5));
        assertEquals("5", w.toString());
    }

    @Test
    public void stringConstant() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, Constant.makeString("5"));
        assertEquals("\"5\"", w.toString());
    }

    @Test
    public void compositeExpression() throws IOException {
        StringWriter w = new StringWriter();
        ICompositeExpression cp = new CompositeExpression(
            new BinaryOperator[] {
                BinaryOperator.ADD, BinaryOperator.SUBTRACT
            }
        ,   new IExpression[] {
                Constant.makeInteger(1)
            ,   Constant.makeDouble(2)
            ,   Constant.makeInteger(3)
            }
        );
        f.formatExpression(w, cp);
        assertEquals("(1 + 2.0 - 3)", w.toString());
    }

    @Test
    public void changeSign() throws IOException {
        StringWriter w = new StringWriter();
        IExpression cp = new UnaryExpression(
            UnaryOperator.SIGN
        ,   Constant.makeInteger(1)
        );
        f.formatExpression(w, cp);
        assertEquals("- 1", w.toString());
    }

    @Test
    public void functionCallEmptyByName() throws IOException {
        StringWriter w = new StringWriter();
        IReference<IFunction> R = IReferenceFactory.DEFAULT.create(
            new Path("and", "b")
        ,   IFunction.class
        );
        IFunctionCall fc = new FunctionCall(R);
        f.formatExpression(w, fc);
        assertEquals("[and]/b()", w.toString());
    }

    @Test
    public void functionCallEmptyById() throws IOException {
        StringWriter w = new StringWriter();
        IReference<IFunction> R = IReferenceFactory.DEFAULT.create(
            123
        ,   IFunction.class
        );
        IFunctionCall fc = new FunctionCall(R);
        f.formatExpression(w, fc);
        assertEquals("id 123()", w.toString());
    }


    @Test
    public void functionCallPositional() throws IOException {
        StringWriter w = new StringWriter();
        IReference<IFunction> R =
            IReferenceFactory.DEFAULT.create(new Path("f"), IFunction.class);
        FunctionCall fc = new FunctionCall(R);
        fc.addArgument(IExpression.NULL);
        fc.addArgument(Constant.makeDouble(1));
        f.formatExpression(w, fc);
        assertEquals("f(null, 1.0)", w.toString());
    }

    @Test
    public void functionCallByName() throws IOException {
        StringWriter w = new StringWriter();
        IReference<IFunction> R =
            IReferenceFactory.DEFAULT.create(new Path("f"), IFunction.class);
        FunctionCall fc = new FunctionCall(R);
        fc.addArgument("n", IExpression.NULL);
        fc.addArgument("o", Constant.makeDouble(1));
        f.formatExpression(w, fc);
        assertEquals("f(n=null, o=1.0)", w.toString());
    }

    @Test
    public void expressionReferenceByName() throws IOException {
        StringWriter w = new StringWriter();
        IExpressionReference ref = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(new Path("a", "b", "c")
        ,   IExpression.class)
        );
        f.formatExpression(w, ref);
        assertEquals("a/b/c", w.toString());
    }

    @Test
    public void expressionReferenceById() throws IOException {
        StringWriter w = new StringWriter();
        IExpressionReference ref = new ExpressionReference(
             IReferenceFactory.DEFAULT.create(123, IExpression.class)
        );
        f.formatExpression(w, ref);
        assertEquals("id 123", w.toString());
    }

    @Test
    public void attributeReference() throws IOException {
        StringWriter w = new StringWriter();
        IAttributeReference base = new AttributeReference(
            new ExpressionReference(
                IReferenceFactory.DEFAULT.create(new Path("a")
            ,   IExpression.class)
            )
        ,   "b"
        );
        IAttributeReference ref = new AttributeReference(base, "c");
        f.formatExpression(w, ref);
        assertEquals("a.b.c", w.toString());
    }

}
