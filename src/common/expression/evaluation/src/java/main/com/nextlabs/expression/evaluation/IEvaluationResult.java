package com.nextlabs.expression.evaluation;

import com.nextlabs.expression.representation.IDataType;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/evaluation/src/java/main/com/nextlabs/expression/evaluation/IEvaluationResult.java#1 $
 */

/**
 * Instances of this interface are returned from calls to evaluate expressions.
 *
 * @author Sergey Kalinichenko
 */
public interface IEvaluationResult {

    Object getValue();

    IDataType getType();

}
