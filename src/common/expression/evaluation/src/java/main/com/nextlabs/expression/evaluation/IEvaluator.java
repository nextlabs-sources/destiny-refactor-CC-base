package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/IEvaluator.java#1 $
 */

import com.nextlabs.expression.representation.IExpression;

/**
 * This interface defines the contract for an evaluator of expressions
 * and predicates.
 *
 * @author Sergey Kalinichenko
 */
public interface IEvaluator {

    /**
     * Evaluates the expression, and returns the result.
     *
     * @param expr the expression to be evaluated.
     * @return The evaluation result containing the value and the type.
     */
    IEvaluationResult evaluate(IExpression expr, IEvaluationHandler handler);

}
