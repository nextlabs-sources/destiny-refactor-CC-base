package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/main/com/nextlabs/expression/util/DefaultExpressionDetector.java#1 $
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
 * The default implementation of the expression detector. All its methods
 * always return false regardless of their arguments.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultExpressionDetector implements IExpressionDetector {

    /**
     * @see *IExpressionDetector#checkAttributeReference(IAttributeReference)
     */
    public boolean checkAttributeReference(
        IAttributeReference attributeReference
    ) {
        return false;
    }

    /**
     * @see IExpressionDetector#checkRelation(IRelation)
     */
    public boolean checkRelation(IRelation relation) {
        return false;
    }

    /**
     * @see IExpressionDetector#checkUnary(IUnaryExpression)
     */
    public boolean checkUnary(IUnaryExpression unary) {
        return false;
    }

    /**
     * @see *IExpressionDetector#checkComposite(ICompositeExpression)
     */
    public boolean checkComposite(ICompositeExpression composite) {
        return false;
    }

    /**
     * @see *IExpressionDetector#checkConstant(Constant)
     */
    public boolean checkConstant(Constant constant) {
        return false;
    }

    /**
     * @see *IExpressionDetector#checkExpression(IExpression)
     */
    public boolean checkExpression(IExpression expression) {
        return false;
    }

    /**
     * @see *IExpressionDetector#checkFunction(IFunctionCall)
     */
    public boolean checkFunction(IFunctionCall functionCall) {
        return false;
    }

    /**
     * @see *IExpressionDetector#checkReference(IExpressionReference)
     */
    public boolean checkReference(IExpressionReference reference) {
        return false;
    }

}
