/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.InvalidLicenseLineItemException;

/**
 * Determines if the installed license has expired
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/auditlineitems/LicenseExpiredLicenseAuditLineItem.java#1 $
 */

public class LicenseExpiredLicenseAuditLineItem implements ILicenseAuditLineItem {

    private static final String DESCRIPTION = "Expiration License Property Validation";
    private static final String EXPIRATION_LICENSE_PROPERTY_NAME = "expiration";
    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem#verify(java.util.Properties)
     */
    public void verify(Properties licenseProperties) throws InvalidLicenseLineItemException {
        String expirationPropertyValue = licenseProperties.getProperty(EXPIRATION_LICENSE_PROPERTY_NAME);
        if (expirationPropertyValue == null) {
            throw new InvalidLicenseLineItemException("Required property, " + EXPIRATION_LICENSE_PROPERTY_NAME + ", not found.", this);
        }

        if (!"-1".equals(expirationPropertyValue)) {
            try {
                Date expirationDate = DATE_FORMAT.parse(expirationPropertyValue);
                if (System.currentTimeMillis() > expirationDate.getTime()) {
                    throw new InvalidLicenseLineItemException("License expired.", this);
                }
            } catch (ParseException exception) {
                throw new InvalidLicenseLineItemException("License expiration property has invalid format.", this);
            }
        }
    }
}