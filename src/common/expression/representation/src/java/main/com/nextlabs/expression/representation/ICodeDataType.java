package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/ICodeDataType.java#1 $
 */

/**
 * This interface defines the contract for Code data types,
 * i.e. enumerated data types defined as a set of strings.
 * The interface merges the definition of IDataType and
 * an Iterable<String>, and adds the hasCode method.
 *
 * @author Sergey Kalinichenko
 */
public interface ICodeDataType extends IDataType, Iterable<String> {

    /**
     * Determines if this data type contains the specified code.
     *
     * @param code the code to be checked for being present in this data type.
     * @return true if the specified code is included in this code data type;
     * false otherwise.
     */
    boolean contains(String code);

}
