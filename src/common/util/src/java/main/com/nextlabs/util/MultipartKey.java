package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/MultipartKey.java#1 $
 */

/**
 * Instances of this class represent multipart keys.
 *
 * @author Sergey Kalinichenko
 */
public class MultipartKey {
    private final Object[] parts;
    private int hashCode = -1;

    public <C> MultipartKey(C ... parts) {
        if ( parts == null ) {
            throw new NullPointerException( "parts" );
        }
        if ( parts.length == 0 ) {
            throw new IllegalArgumentException("parts must not be empty.");
        }
        this.parts = parts.clone();
    }

    public final int hashCode() {
        if (hashCode < 0) {
            int hc = 0;
            for ( int i = parts.length - 1; i >= 0 ; i-- ) {
                if (parts[i] != null) {
                    hc += parts[i].hashCode();
                }
            }
            hashCode = hc;
        }
        return hashCode;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof MultipartKey)) {
            return false;
        }
        MultipartKey mp = (MultipartKey)other;
        if (mp.parts.length != parts.length) {
            return false;
        }
        for (int i = 0 ; i != parts.length ; i++) {
            if (parts[i] != null) {
                if (!parts[i].equals(mp.parts[i])) {
                    return false;
                }
            } else {
                if (mp.parts[i] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("{");
        for (int i = 0 ; i != parts.length ; i++) {
            if ( i != 0 ) {
                res.append( ", " );
            }
            res.append(parts[i]);
        }
        res.append("}" );
        return res.toString();
    }

}
