/*
 * Created on May 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor;

/**
 * ILicenseAuditor is reponsible for verifying the product license. It provides
 * a two step auditing process. The first step simply reads the license,
 * verifying its existence and validating the license format. This step is
 * executed by invoking {@see #validateLicenseInstallation()}. The second step
 * consists of a period check of the license properties and system state. It
 * determines if the system is running within the limits declared by the
 * license. This step is started by calling {@see #startPeriodicAudit()}. The
 * first step can be executed before starting any of the system components. The
 * second step, however, will require system infrastructure and, therefore,
 * should only be started once all system components have been instatiated
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/ILicenseAuditor.java#1 $
 */

public interface ILicenseAuditor {

    public static final String COMP_NAME = "LicenseAuditorComponent";

    /**
     * Validate the license existense and proper format
     * 
     * @throws LicenseValidationException
     *             if the license validation fails
     */
    public void validateLicenseInstallation() throws LicenseValidationException;

    /**
     * Start the period license audit, which will verify if the system is
     * running within the limits declared by the license
     */
    public void startPeriodicAudit();
}