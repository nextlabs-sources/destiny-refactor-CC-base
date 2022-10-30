package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/main/com/nextlabs/expression/representation/IDataType.java#1 $
 */

/**
 * This interface defines the contract for data types.
 *
 * @author Sergey Kalinichenko
 */
public interface IDataType {

    /**
     * A constant representing an unknown data type.
     */
    public static final IDataType UNKNOWN = new DataType() {
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "<unknown>";
        }
    };

    /**
     * A constant representing the NULL data type.
     */
    public static final IDataType NULL = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitNull();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "null";
        }
        /**
         * @see DataType#isNull()
         */
        @Override
        public boolean isNull() {
            return true;
        }
    };

    /**
     * A constant representing the Boolean data type.
     */
    public static final IDataType BOOLEAN = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitBoolean();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "boolean";
        }
        /**
         * @see DataType#isBoolean()
         */
        @Override
        public boolean isBoolean() {
            return true;
        }
    };

    /**
     * A constant representing the Date data type.
     */
    public static final IDataType DATE = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitDate();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "date";
        }
        /**
         * @see DataType#isDate()
         */
        @Override
        public boolean isDate() {
            return true;
        }
    };

    /**
     * A constant representing the double-precision data type.
     */
    public static final IDataType DOUBLE = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitDouble();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "double";
        }
        /**
         * @see DataType#isDouble()
         */
        @Override
        public boolean isDouble() {
            return true;
        }
    };

    /**
     * A constant representing the 64-bit integer data type.
     */
    public static final IDataType INTEGER = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitInteger();
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "integer";
        }
        /**
         * @see DataType#isInteger()
         */
        @Override
        public boolean isInteger() {
            return true;
        }
    };

    /**
     * A constant representing case-insensitive string data type.
     */
    public static final IDataType STRING = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitString(false);
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "string";
        }
        /**
         * @see DataType#isString()
         */
        @Override
        public boolean isString() {
            return true;
        }
    };

    /**
     * A constant representing case-sensitive string data type.
     */
    public static final IDataType CS_STRING = new DataType() {
        /**
         * @see IDataType#accept(IDataTypeVisitor)
         */
        @Override
        public void accept(IDataTypeVisitor visitor) {
            visitor.visitString(true);
        }
        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "case sensitive string";
        }
        /**
         * @see DataType#isCsString()
         */
        @Override
        public boolean isCsString() {
            return true;
        }
    };

    /**
     * This method is part of the visitor pattern implementation.
     * Data types accept visitors and call back with the method
     * corresponding to a particular data type being visited.
     *
     * @param visitor the visitor to call back with the information
     * on this data type.
     */
    void accept(IDataTypeVisitor visitor);

    /**
     * Determines if this is the NULL data type.
     * @return true if this is the NULL data type; false otherwise.
     */
    boolean isNull();

    /**
     * Determines if this is the BOOLEAN data type.
     * @return true if this is the BOOLEAN data type; false otherwise.
     */
    boolean isBoolean();

    /**
     * Determines if this is the INTEGER data type.
     * @return true if this is the INTEGER data type; false otherwise.
     */
    boolean isInteger();

    /**
     * Determines if this is the DOUBLE data type.
     * @return true if this is the DOUBLE data type; false otherwise.
     */
    boolean isDouble();

    /**
     * Determines if this is the STRING data type.
     * @return true if this is the STRING data type; false otherwise.
     */
    boolean isString();

    /**
     * Determines if this is the CS_STRING data type.
     * @return true if this is the CS_STRING data type; false otherwise.
     */
    boolean isCsString();

    /**
     * Determines if this is the DATE data type.
     * @return true if this is the DATE data type; false otherwise.
     */
    boolean isDate();

    /**
     * Determines if this is a code data type.
     * @return true if this is a code data type; false otherwise.
     */
    boolean isCode();

    /**
     * Determines if this is a reference data type.
     * @return true if this is a reference data type; false otherwise.
     */
    boolean isReference();

    /**
     * Determines if this is a multivalue data type.
     * @return true if this is a multivalue data type; false otherwise.
     */
    boolean isMultivalue();

    /**
     * Code data types return themselves as <code>IDodeDataType</code>;
     * other data types throw an exception. This method is implemented when
     * and only when isCode returns true.
     *
     * @return the data type cast as <code>IDodeDataType</code>.
     */
    ICodeDataType asCode();

    /**
     * Reference data types return themselves as
     * <code>IReferenceDataType</code>; other data types throw an exception.
     * This method is implemented when and only when isReference returns true.
     * @param refClass TODO
     *
     * @return the data type cast as <code>IReferenceDataType</code>.
     */
    IReferenceDataType asReference();

    /**
     * Multivalue data types return themselves as
     * <code>IMultivalueDataType</code>; other data types throw an exception.
     * This method is implemented when and only when isMultivalue returns true.
     *
     * @return the data type cast as <code>IMultivalueDataType</code>.
     */
    IMultivalueDataType asMultivalue();

}
