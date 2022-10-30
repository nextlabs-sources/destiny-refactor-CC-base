package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/IEvaluationHandler.java#1 $
 */

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;
import com.nextlabs.expression.representation.IExpressionReference;
import com.nextlabs.expression.representation.IFunction;
import com.nextlabs.util.ref.IReference;

/**
 * This interface defines the operations required to supply leaf-level values
 * to the expression evaluator.
 *
 * @author Sergey Kalinichenko
 */
public interface IEvaluationHandler {

    void evaluateCustomExpression(IExpression custom, IEvaluatorCallback eval);

    void evaluateReference(IExpressionReference ref, IEvaluatorCallback eval);

    void evaluateAttribute(
        String name
    ,   Object value
    ,   IDataType type
    ,   IEvaluatorCallback eval
    );

    void evaluateFunction(
        IReference<IFunction> function
    ,   String[] argNames
    ,   Object[] argValues
    ,   IDataType[] argTypes
    ,   int argOffset, IEvaluatorCallback evalHandler
    );

}
