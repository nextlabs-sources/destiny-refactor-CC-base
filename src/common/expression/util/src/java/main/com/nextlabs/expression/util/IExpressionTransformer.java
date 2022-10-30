package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/main/com/nextlabs/expression/util/IExpressionTransformer.java#1 $
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
 * Defines an interface for use in transform() method of expression utilities.
 *
 * @author Sergey Kalinichenko
 */
public interface IExpressionTransformer {

    /**
     * This instance is used in calls to methods that require non-null
     * expression transformers that always return the original value
     * as the result of transformation.
     */
    public static final IExpressionTransformer DEFAULT =
        new DefaultExpressionTransformer();

    /**
     * Transforms a composite expression.
     * @param original the original composite expression.
     * @param transformed the composite of transformed subexpressions.
     * @return the result of the transformation.
     */
    IExpression transformComposite(
        ICompositeExpression original
    ,   IExpression transformed
    );

    /**
     * Transforms a unary expression.
     * @param original the original unary expression.
     * @param transformed the transformed unary expression.
     * @return the result of the transformation.
     */
    IExpression transformUnary(
        IUnaryExpression original
    ,   IExpression transformed
    );

    /**
     * Transforms a relation.
     * @param original the original relation.
     * @param transformed the transformed relation.
     * @return the result of the transformation.
     */
    IExpression transformRelation(IRelation original, IRelation transformed);

    /**
     * Transforms a constant.
     * @param constant the constant to transform.
     * @return the result of the transformation.
     */
    IExpression transformConstant(Constant constant);

    /**
     * Transforms an attribute reference expression.
     * @param attrReference the attribute reference to transform.
     * @return the result of the transformation.
     */
    IExpression transformAttributeReference(IAttributeReference attrReference);

    /**
     * Transforms a function call expression.
     * @param original the original function call.
     * @param transformed the function call with transformed arguments.
     * @return the result of the transformation.
     */
    IExpression transformFunction(
        IFunctionCall original
    ,   IFunctionCall transformed
    );

    /**
     * Transforms an expression reference.
     * @param reference the reference to transform.
     * @return the result of the transformation.
     */
    IExpression transformReference(IExpressionReference reference);

    /**
     * Transforms a custom expression.
     * @param expression the custom expression to transform.
     * @return the result of the transformation.
     */
    IExpression transformExpression(IExpression expression);

}
