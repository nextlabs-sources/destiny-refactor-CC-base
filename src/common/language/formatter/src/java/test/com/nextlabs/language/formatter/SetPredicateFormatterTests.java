package com.nextlabs.language.formatter;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/formatter/src/java/test/com/nextlabs/language/formatter/SetPredicateFormatterTests.java#1 $
 */

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.BinaryOperator;
import com.nextlabs.expression.representation.CompositeExpression;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.UnaryExpression;
import com.nextlabs.expression.representation.UnaryOperator;
import com.nextlabs.language.parser.PolicyLanguageException;

/**
 * Tests for the set predicate formatting functionality of the ACPL formatter.
 *
 * @author Sergey Kalinichenko
 */
public class SetPredicateFormatterTests {

    private IPolicyLanguageFormatter f;

    private static final IExpression ALL_TRUE = new UnaryExpression(
        UnaryOperator.ALL, IExpression.TRUE
    );

    private static final IExpression ALL_FALSE = new UnaryExpression(
        UnaryOperator.ALL, IExpression.FALSE
    );

    private static final IExpression ANY_TRUE = new UnaryExpression(
       UnaryOperator.ANY, IExpression.TRUE
    );

    private static final IExpression ANY_FALSE = new UnaryExpression(
        UnaryOperator.ANY, IExpression.FALSE
    );

    @Before
    public void prepare() throws PolicyLanguageException {
        IPolicyFormatterFactory factory = new PolicyFormatterFactory();
        f = factory.getFormatter("acpl", 1);
    }

    @Test
    public void allTrue() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, ALL_TRUE);
        assertEquals("ALL true", w.toString());
    }

    @Test
    public void allFalse() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, ALL_FALSE);
        assertEquals("ALL false", w.toString());
    }

    @Test
    public void anyTrue() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, ANY_TRUE);
        assertEquals("ANY true", w.toString());
    }

    @Test
    public void anyFalse() throws IOException {
        StringWriter w = new StringWriter();
        f.formatExpression(w, ANY_FALSE);
        assertEquals("ANY false", w.toString());
    }

    @Test
    public void compositeSetPredicate() throws IOException {
        StringWriter w = new StringWriter();
        ICompositeExpression cp = new CompositeExpression(
            new BinaryOperator[] {BinaryOperator.AND}
        ,   new IExpression[] {ALL_TRUE, ANY_FALSE}
        );
        f.formatExpression(w, cp);
        assertEquals("(ALL true AND ANY false)", w.toString());
    }

}
