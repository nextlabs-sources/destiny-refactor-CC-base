package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/RecordingExpressionVisitor.java#1 $
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This expression visitor is used for testing.
 * It records the calls to visit<...>, and presents them
 * to the caller for examination.
 *
 * @author Sergey Kalinichenko
 */
public class RecordingExpressionVisitor implements IExpressionVisitor {


    private final List<IExpression> args = new ArrayList<IExpression>();

    private final List<String> methods = new ArrayList<String>();


    public String[] getMethods() {
        return methods.toArray(new String[0]);
    }

    public IExpression[] getArguments() {
        return args.toArray(new IExpression[0]);
    }

    /**
     * @see IExpressionVisitor#visitConstant(Constant)
     */
    public void visitConstant(Constant constant) {
        args.add(constant);
        methods.add("visitConstant");
    }

    /**
     * @see IExpressionVisitor#visitAttributeReference(IAttributeReference)
     */
    public void visitAttributeReference(IAttributeReference attrReference) {
        args.add(attrReference);
        methods.add("visitAttributeReference");
    }

    /**
     * @see IExpressionVisitor#visitUnary(IUnaryExpression)
     */
    public void visitUnary(IUnaryExpression unary) {
        args.add(unary);
        methods.add("visitUnary");
    }

    /**
     * @see IExpressionVisitor#visitRelation(IRelation)
     */
    public void visitRelation(IRelation relation) {
        args.add(relation);
        methods.add("visitRelation");
    }

    /**
     * @see IExpressionVisitor#visitComposite(ICompositeExpression)
     */
    public void visitComposite(ICompositeExpression composite) {
        args.add(composite);
        methods.add("visitComposite");
    }

    /**
     * @see IExpressionVisitor#visitReference(IExpressionReference)
     */
    public void visitReference(IExpressionReference reference) {
        args.add(reference);
        methods.add("visitReference");
    }

    /**
     * @see IExpressionVisitor#visitFunction(IFunctionCall)
     */
    public void visitFunction(IFunctionCall function) {
        args.add(function);
        methods.add("visitFunction");
    }

    /**
     * @see IExpressionVisitor#visitExpression(IExpression)
     */
    public void visitExpression(IExpression expr) {
        args.add(expr);
        methods.add("visitExpression");
    }

}
