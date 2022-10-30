package com.nextlabs.expression.representation;

import com.nextlabs.util.ref.IReference;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IReferenceDataType.java#1 $
 */


/**
 * This interface defines the contract for reference data types,
 * extending the IDataType interface with a method to get the reference.
 *
 * @author Sergey Kalinichenko
 */
public interface IReferenceDataType extends IDataType {

    /**
     * Obtains the referenced context.
     *
     * @param <T> The type of the reference to return.
     * @return the referenced context.
     */
    <T> IReference<T> getReferencedContext();

}
