/*
 * Created on May 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl;

/**
 * InvalidLicenseException is thrown when a license verification line item fails
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/InvalidLicenseLineItemException.java#1 $
 */

public class InvalidLicenseLineItemException extends Exception {
    private String failureMessage;
    private ILicenseAuditLineItem failedLineItem;
    
    /**
     * Create an instance of InvalidLicenseException
     * @param failureMessage the failure message to the customer
     */
    public InvalidLicenseLineItemException(String failureMessage, ILicenseAuditLineItem failedLineItem) {
        super(failureMessage);
        
        if (failureMessage == null) {
            throw new NullPointerException("failureMessage cannot be null.");
        }
        
        if (failedLineItem == null) {
            throw new NullPointerException("failedLineItem cannot be null.");
        }
        
        this.failureMessage = failureMessage;
        this.failedLineItem = failedLineItem;
    }
    
    public String getFailureMessage() {
        return this.failureMessage;
    }
    
    public ILicenseAuditLineItem getFailedLineItem() {
        return this.failedLineItem;
    }
}
