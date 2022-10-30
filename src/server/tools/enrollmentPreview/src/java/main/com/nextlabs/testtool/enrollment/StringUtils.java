/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import com.nextlabs.shared.tools.StringFormatter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/StringUtils.java#1 $
 */

public class StringUtils {
    public static String maskString(String str){
        final int maskingPercent = 10;
        final int length = str.length();
        if(length < (100 / maskingPercent) ){
            return str.substring(0, length / 2)
                    + StringFormatter.repeat('*', length - length / 2);
        }

        int startIndex = length / maskingPercent;
        int endIndex = length - startIndex;
        return str.substring(0, startIndex)
                + StringFormatter.repeat('*', endIndex - startIndex) 
                + str.substring(endIndex);
    }
}
