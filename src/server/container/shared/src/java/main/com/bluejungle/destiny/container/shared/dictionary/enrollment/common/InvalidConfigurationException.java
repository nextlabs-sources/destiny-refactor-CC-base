/*
 * Created on Feb 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * This exception is thrown if the configuration provided to the adapter is
 * considered incomplete. A value is missing or doesn't match.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/InvalidConfigurationException.java#1 $
 */

public class InvalidConfigurationException extends EnrollmentException {
    
    public InvalidConfigurationException(String message) {
        super(message);
    }
    
    public static InvalidConfigurationException missing(String fieldName){
        return missing(fieldName, false);
    }
    
    public static InvalidConfigurationException missing(String fieldName, boolean internal) {
        return new InvalidConfigurationException(String.format(internal
                ? "The value of '%s' is missing. Please supply a correct value."
                : "The value of '%s' is missing. Please contact administrators", fieldName));
    }
    
    public static InvalidConfigurationException unknown(String field, String value){
        return new InvalidConfigurationException(
                String.format("The value of '%s', %s, is unknown. Please supply a correct value.", 
                        field, value)
        );
    }
}