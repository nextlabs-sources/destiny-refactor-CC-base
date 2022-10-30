package com.nextlabs.expression.representation;

import com.nextlabs.util.ref.IReference;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IExpressionReference.java#1 $
 */

/**
 * This interface defines the contract for expression references.
 *
 * @author Sergey Kalinichenko
 */
public interface IExpressionReference extends IExpression {

    /**
     * Returns the reference embedded into this expression reference.
     *
     * @return the reference embedded into this expression reference.
     */
    IReference<IExpression> getReference();

}
