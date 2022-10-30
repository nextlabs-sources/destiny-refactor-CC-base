/*
 * Created on May 31, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl;

import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor;
import com.bluejungle.destiny.container.dms.components.licenseauditor.LicenseValidationException;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.LicenseAuditorImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

import junit.framework.TestCase;

/**
 * JUnit test fo LicenseAuditorImpl
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/LicenseAuditorImplTest.java#1 $
 */

public class LicenseAuditorImplTest extends TestCase {
    private static final String BUILD_ROOT_SYSTEM_PROPERTY = "build.root.dir";
    
    private String licenseJarLocation;
    private String licenseDataFileLocation;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LicenseAuditorImplTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        String buildRoot = System.getProperty(BUILD_ROOT_SYSTEM_PROPERTY);
        
        assertNotNull("Ensure build root was specified", buildRoot);
        
        this.licenseJarLocation = buildRoot + "/run/server/" + ServerRelativeFolders.LICENSE_FOLDER.getPathOfContainedFile("license.jar"); 
        this.licenseDataFileLocation = buildRoot + "/run/server/" + ServerRelativeFolders.LICENSE_FOLDER.getPathOfContainedFile("license.dat");          
     }

    public void testValidateLicenseInstallation() throws LicenseValidationException {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();        
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(LicenseAuditorImpl.LICENSE_AUDIT_INTERVAL_CONFIG_PARAM_NAME, LicenseAuditorImpl.DEFAULT_LICENSE_AUDIT_INTERVAL);
        config.setProperty(LicenseAuditorImpl.LICENSE_JAR_LOCATION_CONFIG_PARAM_NAME, "badJarLocation");
        config.setProperty(LicenseAuditorImpl.LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME, this.licenseDataFileLocation);
        ComponentInfo licenseAuditorInfo = new ComponentInfo(ILicenseAuditor.COMP_NAME, LicenseAuditorImpl.class.getName(), ILicenseAuditor.class.getName(), LifestyleType.SINGLETON_TYPE, config);

        ILicenseAuditor licenseAuditor = (ILicenseAuditor) compMgr.getComponent(licenseAuditorInfo);
        LicenseValidationException expectedException = null;
        try {            
            licenseAuditor.validateLicenseInstallation();
        } catch (LicenseValidationException exception) {
            expectedException = exception;
        }        
        assertNotNull("Ensure validation exception was throw for bad jar file location", expectedException);
        
        // Current, there is no way to shut down an individual component
        compMgr.shutdown(); 
        compMgr = ComponentManagerFactory.getComponentManager();        
        
        config.setProperty(LicenseAuditorImpl.LICENSE_JAR_LOCATION_CONFIG_PARAM_NAME, this.licenseJarLocation);
        config.setProperty(LicenseAuditorImpl.LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME, "badDataFileLocation");

        licenseAuditor = (ILicenseAuditor) compMgr.getComponent(licenseAuditorInfo);
        expectedException = null;
        try {            
            licenseAuditor.validateLicenseInstallation();
        } catch (LicenseValidationException exception) {
            expectedException = exception;
        }        
        assertNotNull("Ensure validation exception was throw for bad data file location", expectedException); 
        
        // Current, there is no way to shut down an individual component
        compMgr.shutdown(); 
        compMgr = ComponentManagerFactory.getComponentManager();                
        
        config.setProperty(LicenseAuditorImpl.LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME, this.licenseDataFileLocation);
        licenseAuditor = (ILicenseAuditor) compMgr.getComponent(licenseAuditorInfo);
        
        /**
         * Ensure validation passes
         */
        licenseAuditor.validateLicenseInstallation();
    }

    public void testStartPeriodicAudit() {
        // FIX ME - Requires a significant amount of set up (agent manager, ddif) for little reward.  Moving on for now
    }

}