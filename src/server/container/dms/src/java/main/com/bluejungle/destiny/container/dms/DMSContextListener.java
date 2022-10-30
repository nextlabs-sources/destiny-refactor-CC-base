/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import java.io.File;

import javax.servlet.ServletContextEvent;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor;
import com.bluejungle.destiny.container.dms.components.licenseauditor.LicenseValidationException;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.LicenseAuditorImpl;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/DMSContextListener.java#1 $
 */

public class DMSContextListener extends DCCContextListener {

    private static final String LICENSE_JAR_FILE_NAME = "license.jar";
    private static final String LICENSE_DATA_FILE_NAME = "license.dat";

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent initEvent) {
        String installDir = initEvent.getServletContext().getInitParameter(IDCCContainer.INSTALL_HOME_PATH_CONFIG_PARAM);
        if (installDir == null) {
            StringBuffer errorMessage = new StringBuffer("The servlet context initialization parameter, ");
            errorMessage.append(IDCCContainer.INSTALL_HOME_PATH_CONFIG_PARAM);
            errorMessage.append(", could not be found.  Please check application configuration.");

            throw new IllegalStateException(errorMessage.toString());
        }

        String licenseJarLocation = buildLicenseJarLocation(installDir);
        String licenseDataFileLocation = buildLicenseDataFileLocation(installDir);

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(LicenseAuditorImpl.LICENSE_AUDIT_INTERVAL_CONFIG_PARAM_NAME, LicenseAuditorImpl.DEFAULT_LICENSE_AUDIT_INTERVAL);
        config.setProperty(LicenseAuditorImpl.LICENSE_JAR_LOCATION_CONFIG_PARAM_NAME, licenseJarLocation);
        config.setProperty(LicenseAuditorImpl.LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME, licenseDataFileLocation);
        ComponentInfo<ILicenseAuditor> licenseAuditorInfo = 
            new ComponentInfo<ILicenseAuditor>(
                ILicenseAuditor.COMP_NAME, 
                LicenseAuditorImpl.class, 
                ILicenseAuditor.class,
                LifestyleType.SINGLETON_TYPE, 
                config);

        ILicenseAuditor licenseAuditor = compMgr.getComponent(licenseAuditorInfo);
        try {
            licenseAuditor.validateLicenseInstallation();
        } catch (LicenseValidationException exception) {
            StringBuffer logMessage = new StringBuffer("\n\nINVALID LICENSE!");
            logMessage.append("\nLicense could not be found and/or read.");
            logMessage.append("\n\nPlease contact your account executive to obtain a valid license.\n\n");
            getLog().fatal(logMessage.toString());

            throw new IllegalStateException("License not installed.");
        }

        super.contextInitialized(initEvent);
    }

    /**
     * Build the license jar file location path based on the install directory
     * 
     * @param installDir
     *            the product install directory
     * @return the license jar file location path based on the specified install
     *         directory
     */
    private String buildLicenseJarLocation(String installDir) {
        StringBuffer licenseJarLocationBuffer = new StringBuffer(installDir);
        licenseJarLocationBuffer.append(File.separator);
        licenseJarLocationBuffer.append(ServerRelativeFolders.LICENSE_FOLDER.getPathOfContainedFile(LICENSE_JAR_FILE_NAME));

        return licenseJarLocationBuffer.toString();
    }

    /**
     * Build the license data file location path based on the install directory
     * 
     * @param installDir
     *            the product install directory
     * @return the license data file location path based on the specified
     *         install directory
     */
    private String buildLicenseDataFileLocation(String installDir) {
        StringBuffer licenseDataFileLocationBuffer = new StringBuffer(installDir);
        licenseDataFileLocationBuffer.append(File.separator);
        licenseDataFileLocationBuffer.append(ServerRelativeFolders.LICENSE_FOLDER.getPathOfContainedFile(LICENSE_DATA_FILE_NAME));

        return licenseDataFileLocationBuffer.toString();
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.DCCContextListener#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DMS;
    }

    /**
     * Returns the container class name
     * 
     * @return the container class name
     */
    protected Class<? extends IDCCContainer> getContainerClassName() {
        return DMSContainerImpl.class;
    }

    @Override
    public String getTypeDisplayName() {
        return "Management Server";
    }
}