package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/test/com/nextlabs/language/formatter/PredicateFormatterTests.java#1 $
 */

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.BinaryOperator;
import com.nextlabs.expression.representation.CompositeExpression;
import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.ExpressionReference;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.Relation;
import com.nextlabs.expression.representation.RelationOperator;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.language.parser.PolicyLanguageException;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the predicate formatting functionality of the ACPL formatter.
 *
 * @author Sergey Kalinichenko
 */
public class PredicateFormatterTests {

    private IPolicyLanguageFormatter f;

    @Before
    public void prepare() throws PolicyLanguageException {
        IPolicyFormatterFactory factory = new PolicyFormatterFactory();
        f = factory.getFormatter("acpl", 1);
    }

    @Test
    public void constantPredicateFalse() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, IExpression.FALSE);
        assertEquals("false", w.toString());
    }

    @Test
    public void constantPredicateTrue() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, IExpression.TRUE);
        assertEquals("true", w.toString());
    }

    @Test
    public void compositePredicate() throws IOException {
        StringWriter w = new StringWriter();
        ICompositeExpression cp = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.AND}
        ,   new IExpression[] {IExpression.TRUE, IExpression.TRUE}
        );
        f.formatExpression(w, cp);
        assertEquals("(true AND true)", w.toString());
    }

    @Test
    public void ExpressionReferenceByName() throws IOException {
        StringWriter w = new StringWriter();
        IExpressionReference ref = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(
                new Path("a", "b", "c")
            ,   IExpression.class
            )
        );
        f.formatExpression(w, ref);
        assertEquals("a/b/c", w.toString());
    }

    @Test
    public void ExpressionReferenceById() throws IOException {
        StringWriter w = new StringWriter();
        IExpressionReference ref = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(123, IExpression.class)
        );
        f.formatExpression(w, ref);
        assertEquals("id 123", w.toString());
    }

    @Test
    public void negation() throws IOException {
        StringWriter w = new StringWriter();
        IExpressionReference ref = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(123, IExpression.class)
        );
        IExpression neg = new UnaryExpression(UnaryOperator.NOT, ref);
        f.formatExpression(w, neg);
        assertEquals("NOT id 123", w.toString());
    }


    @Test
    public void relation() throws IOException {
        StringWriter w = new StringWriter();
        IExpression lhs = Constant.makeDouble(123);
        IExpression rhs = Constant.makeInteger(456);
        IRelation rel = new Relation(RelationOperator.LESS_THAN, lhs, rhs);
        f.formatExpression(w, rel);
        assertEquals("123.0 < 456", w.toString());
    }

}
