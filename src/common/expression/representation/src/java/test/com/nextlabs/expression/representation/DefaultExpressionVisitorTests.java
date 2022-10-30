package com.nextlabs.expression.representation;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.util.ref.IReferenceFactory;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/DefaultExpressionVisitorTests.java#1 $
 */

/**
 * Tests for DefaultExpressionVisitor class.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultExpressionVisitorTests {

    private DefaultExpressionVisitor visitor;

    @Before
    public void prepare() {
        visitor = new DefaultExpressionVisitor();
    }

    @Test
    public void visitConstant() {
        visitor.visitConstant(null);
        visitor.visitConstant(Constant.makeDouble(1));
    }

    @Test
    public void visitReference() {
        visitor.visitReference(null);
        IExpressionReference exprRef = new ExpressionReference(
            IReferenceFactory.DEFAULT.create(10, IExpression.class)
        );
        visitor.visitReference(exprRef);
    }

    @Test
    public void visitComposite() {
        visitor.visitComposite(null);
    }

    @Test
    public void visitUnary() {
        visitor.visitUnary(null);
    }

    @Test
    public void visitRelation() {
        visitor.visitRelation(null);
    }

    @Test
    public void visitFunction() {
        visitor.visitFunction(null);
        IFunctionCall fc = new FunctionCall(
            IReferenceFactory.DEFAULT.create(10, IFunction.class)
        );
        visitor.visitFunction(fc);
    }

    @Test
    public void visitAttributeReference() {
        visitor.visitAttributeReference(null);
        visitor.visitAttributeReference(
            new AttributeReference(IExpression.NULL, "a")
        );
    }

    @Test
    public void visitExpression() {
        IExpression expr = new IExpression() {
            public void accept(IExpressionVisitor visitor) {
                visitor.visitExpression(this);
            }
        };
        visitor.visitExpression(null);
        visitor.visitExpression(expr);
    }

}
