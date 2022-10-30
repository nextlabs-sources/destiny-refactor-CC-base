/*
 * Created on May 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl;

import java.util.Properties;

/**
 * ILicenseAuditLineItem represents an individual line item verification which
 * must pass to have a valid license
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/ILicenseAuditLineItem.java#1 $
 */

public interface ILicenseAuditLineItem {
    public String getDescription();
    public void verify(Properties licenseProperties) throws InvalidLicenseLineItemException;
}