package com.nextlabs.expression.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/util/src/java/main/com/nextlabs/expression/util/IExpressionDetector.java#1 $
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
 * Provides an interface for use in the find() method of expression utilities.
 *
 * @author Sergey Kalinichenko
 */
public interface IExpressionDetector {

    /**
     * This detector is used in calls to methods that require non-null
     * expression detectors.
     */
    public static final IExpressionDetector DEFAULT =
        new DefaultExpressionDetector();

    /**
     * Checks a composite expression. This checker is not responsible for
     * checking subexpressions - the find method will call other methods
     * for it.
     * @param composite the composite expression to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkComposite(ICompositeExpression composite);

    /**
     * Checks a unary expression. This checker is not responsible for
     * checking the operand - the find method will call other methods for it.
     * @param unary the unary expression to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkUnary(IUnaryExpression unary);

    /**
     * Checks a relation. The checker is not responsible for calling
     * the operands - the find method will call other methods for it.
     * @param relation the relation to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkRelation(IRelation relation);

    /**
     * Checks a constant.
     * @param constant the constant to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkConstant(Constant constant);

    /**
     * Checks an attribute reference.
     * @param attributeReference the attribute reference to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkAttributeReference(IAttributeReference attributeReference);

    /**
     * Checks a function call. The checker is not responsible for checking the
     * function arguments - the find method will call other methods for it.
     * @param functionCall the function call to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkFunction(IFunctionCall functionCall);

    /**
     * Checks an expression reference.
     * @param reference the reference to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkReference(IExpressionReference reference);

    /**
     * Checks a custom expression.
     * @param expression the expression to check.
     * @return true if the expression the detector looks for is found;
     * otherwise false.
     */
    boolean checkExpression(IExpression expression);

}
