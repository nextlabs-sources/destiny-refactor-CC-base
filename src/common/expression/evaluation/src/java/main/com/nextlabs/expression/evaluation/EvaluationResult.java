package com.nextlabs.expression.evaluation;

import com.nextlabs.expression.representation.IDataType;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/EvaluationResult.java#1 $
 */

/**
 * This is the implementation of the IEvaluationResult interface.
 * It is package-private to avoid suggesting its use in other modules,
 * because instances of IEvaluationResult should be used sparingly.
 *
 * @author Sergey Kalinichenko
 */
class EvaluationResult implements IEvaluationResult {

    /**
     * The value returned from the evaluator.
     */
    private final Object value;

    /**
     * The type returned from the evaluator.
     */
    private final IDataType type;

    /**
     * Constructs an evaluation result with the specified value and type.
     *
     * @param value the value returned from the evaluation result.
     * @param type the type of the value returned from the evaluation result.
     */
    public EvaluationResult(Object value, IDataType type) {
        this.value = value;
        this.type = type;
    }

    /**
     * @see IEvaluationResult#getValue()
     */
    public Object getValue() {
        return value;
    }

    /**
     * @see  IEvaluationResult#getType()
     */
    public IDataType getType() {
        return type;
    }

}
