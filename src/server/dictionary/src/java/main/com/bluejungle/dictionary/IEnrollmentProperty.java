/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/IEnrollmentProperty.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * This interface defines the behavior of an enrollment property.
 */
interface IEnrollmentProperty {

    /**
     * This is a dummy property. It is never added to enrollments.
     * It is returned instead of non-existent properties if required.
     * Its getters return <code>null</code>; its setters throw exceptions.
     */
    public static IEnrollmentProperty DUMMY = new EnrollmentProperty() {
        /**
         * @see IEnrollmentProperty#getBinary()
         */
        public byte[] getBinary() {
            return null;
        }
        /**
         * @see IEnrollmentProperty#getNumber()
         */
        public long getNumber() {
            return -1;
        }
        /**
         * @see IEnrollmentProperty#getStrArray()
         */
        public String[] getStrArray() {
            return null;
        }
        /**
         * @see IEnrollmentProperty#getString()
         */
        public String getString() {
            return null;
        }
        /**
         * @see IEnrollmentProperty#setBinary(byte[])
         */
        public void setBinary( byte[] binValue ) {
            throw new IllegalStateException("Unknown properties do not allow set operations.");
        }
        /**
         * @see IEnrollmentProperty#setNumber(long)
         */
        public void setNumber( long numValue ) {
            throw new IllegalStateException("Unknown properties do not allow set operations.");
        }
        /**
         * @see IEnrollmentProperty#setStrArray(String[])
         */
        public void setStrArray( String[] strValue ) {
            throw new IllegalStateException("Unknown properties do not allow set operations.");
        }
        /**
         * @see IEnrollmentProperty#setString()
         */
        public void setString( String strValue ) {
            throw new IllegalStateException("Unknown properties do not allow set operations.");
        }
    };

    /**
     * Gets the string value of the property
     * after checking the type compatibility.
     *
     * @return the string value of the property.
     */
    public String getString();

    /**
     * Sets the string value of the property
     * after checking the type compatibility.
     *
     * @param strValue the string value of the property.
     */
    public void setString( String strValue );

    /**
     * Gets the string array value of the property
     * after checking the type compatibility.
     *
     * @return the string array value of the property.
     */
    public String[] getStrArray();

    /**
     * Sets the string array value of the property
     * after checking the type compatibility.
     * Implementations of this method copy the array.
     *
     * @param strValue the new string array value of the property.
     */
    public void setStrArray( String[] strValue );

    /**
     * Gets the numeric value of the property
     * after checking the type compatibility.
     *
     * @return the numeric value of the property.
     */
    public long getNumber();

    /**
     * Sets the numeric value of the property
     * after checking the type compatibility.
     *
     * @param numValue the new numeric value of the property.
     */
    public void setNumber( long numValue );

    /**
     * Gets the binary value of the property
     * after checking the type compatibility.
     *
     * @return the binary value of the property.
     */
    public byte[] getBinary();

    /**
     * Sets the binary value of the property
     * after checking the type compatibility.
     *
     * @param binValue the new binary value of the property.
     */
    public void setBinary( byte[] binValue );

}
