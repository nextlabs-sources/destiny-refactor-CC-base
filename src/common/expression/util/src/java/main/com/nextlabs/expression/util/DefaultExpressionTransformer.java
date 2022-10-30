package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/main/com/nextlabs/expression/util/DefaultExpressionTransformer.java#1 $
 */

import com.nextlabs.expression.representation.Constant;
import com.nextlabs.expression.representation.IAttributeReference;
import com.nextlabs.expression.representation.ICompositeExpression;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IFunctionCall;
import com.nextlabs.expression.representation.IRelation;
import com.nextlabs.expression.representation.IUnaryExpression;

/**
 * The default implementation of the expression transformer. All its methods
 * always return the original expression.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultExpressionTransformer implements IExpressionTransformer {

    /**
     * @see IExpressionTransformer#transformAttributeReference(
     *      IAttributeReference)
     */
    public IExpression transformAttributeReference(
            IAttributeReference attributeReference) {
        return attributeReference;
    }

    /**
     * @see IExpressionTransformer#transformRelation(IRelation, IRelation)
     */
    public IExpression transformRelation(
        IRelation original
    ,   IRelation transformed
    ) {
        return transformed;
    }

    /**
     * @see IExpressionTransformer#transformUnary(IUnaryExpression, IExpression)
     */
    public IExpression transformUnary(
        IUnaryExpression original
    ,   IExpression transformed
    ) {
        return transformed;
    }

    /**
     * @see IExpressionTransformer#transformComposite(
     *      ICompositeExpression, IExpression)
     */
    public IExpression transformComposite(
        ICompositeExpression original
    ,   IExpression transformed
    ) {
        return transformed;
    }

    /**
     * @see IExpressionTransformer#transformConstant(Constant)
     */
    public IExpression transformConstant(Constant constant) {
        return constant;
    }

    /**
     * @see IExpressionTransformer#transformExpression(IExpression)
     */
    public IExpression transformExpression(IExpression expression) {
        return expression;
    }

    /**
     * @see IExpressionTransformer#transformFunction(
     *      IFunctionCall, IFunctionCall)
     */
    public IExpression transformFunction(
        IFunctionCall original
    ,   IFunctionCall transformed
    ) {
        return transformed;
    }

    /**
     * @see IExpressionTransformer#transformReference(IExpressionReference)
     */
    public IExpression transformReference(IExpressionReference reference) {
        return reference;
    }

}
