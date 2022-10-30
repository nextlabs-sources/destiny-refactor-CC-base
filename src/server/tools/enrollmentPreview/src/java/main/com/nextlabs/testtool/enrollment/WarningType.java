/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/WarningType.java#1 $
 */

public enum WarningType {
    MISSING_FIELD("The following fields are missing: %s"),
    VALUE_TOO_LONG("The length of '%s',%d , is longer than the limit, %d"),
    CHARSER_NOT_SUPPORTED("The value of '%s' contains character(s) that database doesn't support."),
    UNKNOWN("The entry is not recongized.");
    ;
    
    private final String template;

    private WarningType(String template) {
        this.template = template;
    }
    
    public String format(Object... params){
        return String.format(template, params);
    }
}
