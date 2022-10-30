package com.nextlabs.expression.evaluation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/IEvaluatorCallback.java#1 $
 */

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IExpression;

/**
 * This interface defines the contract of evaluator callbacks, through which
 *
 * @author Sergey Kalinichenko
 */
public interface IEvaluatorCallback {

    void addValue(Object value, IDataType type);

    void addExpression(IExpression expression);

}
