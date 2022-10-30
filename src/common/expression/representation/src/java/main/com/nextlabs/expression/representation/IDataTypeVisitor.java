package com.nextlabs.expression.representation;


/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IDataTypeVisitor.java#1 $
 */

/**
 * This interface defines the contract for visitors of IDataType objects.
 *
 * @author Sergey Kalinichenko
 */
public interface IDataTypeVisitor {

    /**
     * Visit the null data type.
     */
    void visitNull();

    /**
     * Visit the boolean data type.
     */
    void visitBoolean();

    /**
     * Visit the Date data type.
     */
    void visitDate();

    /**
     * Visit the double-precision data type.
     */
    void visitDouble();

    /**
     * Visit the 64-bit integer data type.
     */
    void visitInteger();

    /**
     * Visit the string data type.
     *
     * @param caseSensitive an argument that indicates whether or not the
     * corresponding string is case-sensitive or not.
     */
    void visitString(boolean caseSensitive);

    /**
     * Visit the code data type.
     *
     * @param code the code data type being visited.
     */
    void visitCode(ICodeDataType code);

    /**
     * Visit the reference data type.
     *
     * @param refDataType the reference data type being visited.
     */
    void visitReference(IReferenceDataType refDataType);

    /**
     * Visit the multivalue data type.
     *
     * @param multivalueDataType the multivalue data type being visited
     */
    void visitMultivalue(IMultivalueDataType multivalueDataType);

    /**
     * Visit the unknown data type.
     *
     * @param dataType the unknown data type being visited.
     */
    void visitUnknown(IDataType dataType);

}
