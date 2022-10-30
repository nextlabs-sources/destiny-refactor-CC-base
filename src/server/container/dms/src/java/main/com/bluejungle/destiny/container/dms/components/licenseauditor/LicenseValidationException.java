/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor;


/**
 * LicenseValidationException is thrown when the system is installed with an
 * invalid license or the license could not be found
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/LicenseValidationException.java#1 $
 */

public class LicenseValidationException extends Exception {

    /**
     * Create an instance of LicenseValidationException
     *  
     */
    public LicenseValidationException() {
        super();
    }

    /**
     * Create an instance of LicenseValidationException
     * 
     * @param cause
     */
    public LicenseValidationException(Throwable cause) {
        super(cause);
    }
}