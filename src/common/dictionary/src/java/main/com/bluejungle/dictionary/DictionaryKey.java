/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/dictionary/src/java/main/com/bluejungle/dictionary/DictionaryKey.java#1 $
 */

package com.bluejungle.dictionary;

/**
 * Instances of this class represent immutable keys for querying
 * the dictionary. 
 */
public class DictionaryKey {

    /** The data of this user-defined key. */
    private final byte[] key;

    /** Hash code cache. */
    private transient int hashCode = -1;

    /** Cached string representation of the key. */
    private transient String stringRep = null;

    /**
     * Builds a dictionary key from raw data.
     * @param key the raw data from which to build the key.
     */
    public DictionaryKey(byte[] key) {
        if ( key == null ) {
            throw new NullPointerException("key");
        }
        this.key = new byte[key.length];
        System.arraycopy(key, 0, this.key, 0, key.length);
    }

    /**
     * Obtains the raw data for the key.
     * @return the raw data for the key.
     */
    public byte[] getKey() {
        return copy(key); 
    }

    /**
     * Makes a copy of a byte array.
     * @param data the byte array to copy.
     * @return a copy of the byte array.
     */
    private static byte[] copy(byte[] data) {
        if ( data == null ) {
            throw new NullPointerException("data");
        }
        byte[] res = new byte[data.length];
        System.arraycopy(data, 0, res, 0, data.length);
        return res;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        if ( hashCode == -1 ) {
            hashCode = 0;
            for ( int i = 0 ; i != key.length ; i++ ) {
                hashCode += key[i];
            }
        }
        return hashCode;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object other) {
        if (other instanceof DictionaryKey) {
            DictionaryKey otherKey = (DictionaryKey)other;
            for ( int i = 0 ; i != key.length ; i++ ) {
                if ( key[i] != otherKey.key[i] ) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        if (stringRep == null) {
            boolean printable = true;
            for (int i = 0 ; printable && i < key.length ; i++) {
                printable &= !Character.isISOControl((char)key[i]);
            }
            if (printable) {
                char[] rep = new char[key.length];
                for (int i = 0 ; i < key.length ; i++) {
                    rep[i] = (char)key[i];
                }
                stringRep = new String(rep);
            } else {
                StringBuffer res = new StringBuffer();
                for (int i = 0 ; i < key.length ; i++) {
                    res.append(hexDigit(key[i]>>4));
                    res.append(hexDigit(key[i]));
                    res.append(' ');
                }
                stringRep = res.toString();
            }
        }
        return stringRep;
    }

    /**
     * Converts the last four bits of a number to a hex digit.
     * @param n the number to convert to a hex digit.
     * @return hex digit corresponding to the last four bits of n.
     */
    private static char hexDigit(int n) {
        return "0123456789abcdef".charAt(n&15);
    }

}
