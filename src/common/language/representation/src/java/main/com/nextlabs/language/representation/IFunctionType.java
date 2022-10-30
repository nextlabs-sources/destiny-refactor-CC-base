package com.nextlabs.language.representation;

import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.expression.representation.IFunction;


/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/IFunctionType.java#1 $
 */

/**
 * This interface defines the contract for function type definitions.
 *
 * @author Sergey Kalinichenko
 */
public interface IFunctionType
       extends ICallableType<IFunctionType>, IFunction  {

    /**
     * Gets the return type of this function.
     *
     * @return the return type of this function.
     */
    IDataType getReturnType();
}
