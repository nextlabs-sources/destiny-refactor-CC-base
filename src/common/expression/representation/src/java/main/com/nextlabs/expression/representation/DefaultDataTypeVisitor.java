package com.nextlabs.expression.representation;


/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/DefaultDataTypeVisitor.java#1 $
 */

/**
 * This is the default implementation of the IDataTypeVisitor interface.
 * All of its methods do nothing.
 *
 * @author Sergey Kalinichenko
 */
public class DefaultDataTypeVisitor implements IDataTypeVisitor {

    /**
     * @see IDataTypeVisitor#visitNull()
     */
    public void visitNull() {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitBoolean()
     */
    public void visitBoolean() {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitCode(ICodeDataType)
     */
    public void visitCode(ICodeDataType codes) {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitDate()
     */
    public void visitDate() {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitDouble()
     */
    public void visitDouble() {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitInteger()
     */
    public void visitInteger() {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitReference(IReferenceDataType)
     */
    public void visitReference(IReferenceDataType refDataType) {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitString(boolean)
     */
    public void visitString(boolean caseSensitive) {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor@visitMultivalue(DataType)
     */
    public void visitMultivalue(IMultivalueDataType dataType) {
        // The default implementation does nothing.
    }

    /**
     * @see IDataTypeVisitor#visitUnknown(DataType)
     */
    public void visitUnknown(IDataType dataType) {
        // The default implementation does nothing.
    }
}
