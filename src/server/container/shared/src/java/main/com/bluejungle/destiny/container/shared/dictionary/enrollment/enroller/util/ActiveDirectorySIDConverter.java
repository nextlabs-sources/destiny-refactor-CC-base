/*
 * Created on August 07, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

/**
 * @author sergey 
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/util/ActiveDirectorySIDConverter.java#1 $
 */


/**
 * ActiveDirectorySIDConverter class is designed for convertering SID String from binary 
 * 
 *  Pictorially the structure of a security descriptor is as follows:
 *
 *  3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1
 *  1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
 * +---------------------------------------------------------------+
 * |            Control            |Reserved1 (SBZ)|   Revision    |
 * +---------------------------------------------------------------+
 * |                            Owner                              |
 * +---------------------------------------------------------------+
 * |                            Group                              |
 * +---------------------------------------------------------------+
 * |                            Sacl                               |
 * +---------------------------------------------------------------+
 * |                            Dacl                               |
 * +---------------------------------------------------------------+
 *
 */
public class ActiveDirectorySIDConverter {

    /**
     * SID to String conversion
     * @param  byte[] sid : binary array
     * @return String the converted SID string
     */
    public static String sidToString(byte[] sid) {
        if (sid == null) {
            throw new NullPointerException("sid");
        }
        if (sid.length < 8) {
            //TODO_OJA
            throw new IllegalArgumentException("sid must be at least 8 charactor long. " + sid.length);
        }
        StringBuffer res = new StringBuffer(128);
        res.append("S-");
        res.append(sid[0]);
        res.append('-');
        res.append(getLong(sid, 1, 6));
        for (int i = 0 ; i != sid[7] ; i++) {
            res.append('-');
            int start = 8+i*4;
            if ( start < sid.length ) {
                res.append(getLong(sid, 8+i*4, 4));
            }
        }
        return res.toString();
    }


    /**
     * get Long from binary
     */
    private static long getLong(byte[] s, int n, int c) {
        assert s != null;
        if (s.length < n+c) {
            //TODO_OJA
            throw new IllegalArgumentException("sid is shorter than expected " + s.length + ", n="
                    + n + ", c=" + c);
        }
        long res = 0;
        while (c-- != 0) {
            res <<= 8;
            res += s[n+c]&0xFF;
        }
        return res;
    }

}
