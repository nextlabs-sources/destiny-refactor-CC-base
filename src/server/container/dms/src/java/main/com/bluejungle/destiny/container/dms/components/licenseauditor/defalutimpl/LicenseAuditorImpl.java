/*
 * Created on May 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor;
import com.bluejungle.destiny.container.dms.components.licenseauditor.LicenseValidationException;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems.DesktopAgentCountLicenseAuditLineItem;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems.FileServerAgentCountLicenseAuditLineItem;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems.LicenseExpiredLicenseAuditLineItem;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems.UserCountLicenseAuditLineItem;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * Default implementation of the ILicenseAuditor interface. The implementation
 * periodically validates the license and reports all validity failures to the
 * product log file
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/LicenseAuditorImpl.java#1 $
 */

public class LicenseAuditorImpl implements ILicenseAuditor, IInitializable, IStartable, IConfigurable, ILogEnabled {

    private static final String LICENSE_JARCHECKER_GETPROPERTIES_METHOD_NAME = "getProperties";
    private static final String LICENSE_JARCHECKER_CHECK_METHOD_NAME = "check";
    private static final String LICENSE_JARCHECKER_SETCLASSLOADER_METHOD_NAME = "setClassLoader";
    private static final String LICENSE_JARCHECKER_SETJARFILENAME_METHOD_NAME = "setJarFileName";
    private static final String LICENSE_JARCHECKER_CLASS_NAME = "com.wald.license.checker.JarChecker";
    private static final String LICENSE_CLASSLOADER_CLASS_NAME = "com.wald.license.checker.LicenseClassLoader";
    private static final String FILE_PROTOCOL = "file:///";
    
    public static final PropertyKey<Long> LICENSE_AUDIT_INTERVAL_CONFIG_PARAM_NAME = new PropertyKey<Long>("LicenseAuditInterval");
    /**
     * Default License Audit Interval - 12 hours
     */
    public static final Long DEFAULT_LICENSE_AUDIT_INTERVAL = new Long(12 * 60 * 60 * 1000); 
    public static final PropertyKey<String> LICENSE_JAR_LOCATION_CONFIG_PARAM_NAME = new PropertyKey<String>("LicenseJarLocation");
    public static final PropertyKey<String> LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME = new PropertyKey<String>("LicenseDataFileLocation");        
    
    private Properties licenseProperties;
    private Throwable licenseReadThrowable;

    private IConfiguration config;
    private Log log;
    private Timer licenseAuditTimer;
    private Long licenseAuditTimeInterval;
    private String licenseJarFileLocation;
    private String licenseDataFileLocation;
    

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration myConfig = getConfiguration();
        if (myConfig == null) {
            throw new IllegalStateException("Required configuration not provided.");
        }
        
        this.licenseAuditTimeInterval = myConfig.get(LICENSE_AUDIT_INTERVAL_CONFIG_PARAM_NAME, DEFAULT_LICENSE_AUDIT_INTERVAL);
        this.licenseJarFileLocation = myConfig.get(LICENSE_JAR_LOCATION_CONFIG_PARAM_NAME);
        if (licenseJarFileLocation == null) {
            throw new IllegalStateException("License jar location config parameter not specified.");
        }
        this.licenseDataFileLocation = myConfig.get(LICENSE_DATA_FILE_LOCATION_CONFIG_PARAM_NAME);
        if (licenseDataFileLocation == null) {
            throw new IllegalStateException("License data file directory config parameter not specified.");
        }        
        

        readLicense();
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        if (this.licenseAuditTimer != null) {
            this.licenseAuditTimer.cancel();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor#validateLicenseInstallation()
     */
    public void validateLicenseInstallation() throws LicenseValidationException {
        if (this.licenseReadThrowable != null) {
            throw new LicenseValidationException(this.licenseReadThrowable);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.ILicenseAuditor#startPeriodicAudit()
     */
    public void startPeriodicAudit() {
        List<ILicenseAuditLineItem> licenseAuditLineItems = new LinkedList<ILicenseAuditLineItem>();
        licenseAuditLineItems.add(new LicenseExpiredLicenseAuditLineItem());
        licenseAuditLineItems.add(new DesktopAgentCountLicenseAuditLineItem());
        licenseAuditLineItems.add(new FileServerAgentCountLicenseAuditLineItem());
        licenseAuditLineItems.add(new UserCountLicenseAuditLineItem());

        LicenseAuditExecutor auditExecutor = new LicenseAuditExecutor(licenseAuditLineItems);
        
        this.licenseAuditTimer = new Timer("LicenseAuditor", true);
        this.licenseAuditTimer.schedule(auditExecutor, 0, this.licenseAuditTimeInterval.longValue());
    }

    private void readLicense() {
        try {
            URL jarLocation = new URL(FILE_PROTOCOL + this.licenseJarFileLocation);
            String licenseDataFileDirectory = new File(this.licenseDataFileLocation).getParent() + "/";             
            URL dataFileParentFolderLocation = new URL(FILE_PROTOCOL + licenseDataFileDirectory);
            URL[] classLoaderURLs = { jarLocation, dataFileParentFolderLocation };
            ClassLoader licenseLocationClassLoader = new URLClassLoader(classLoaderURLs, this.getClass().getClassLoader());

            Class<?> licenseClassLoaderClass = licenseLocationClassLoader.loadClass(LICENSE_CLASSLOADER_CLASS_NAME);
            Constructor<?> parentClassLoaderConstructor = licenseClassLoaderClass.getConstructor(ClassLoader.class);
            Object licenseClassLoader = parentClassLoaderConstructor.newInstance(licenseLocationClassLoader);

            Class<?> jarCheckerClass = licenseLocationClassLoader.loadClass(LICENSE_JARCHECKER_CLASS_NAME);
            Object jarCheckerInstance = jarCheckerClass.newInstance();
            Method setJarFileMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_SETJARFILENAME_METHOD_NAME, java.lang.String.class);
            setJarFileMethod.invoke(jarCheckerInstance, licenseJarFileLocation);

            Class<?> setClassLoaderMethodParams = licenseClassLoader.getClass();
            Method setClassLoaderMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_SETCLASSLOADER_METHOD_NAME, setClassLoaderMethodParams);
            setClassLoaderMethod.invoke(jarCheckerInstance, licenseClassLoader);

            Method checkMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_CHECK_METHOD_NAME);
            checkMethod.invoke(jarCheckerInstance);

            Method getPropertiesMethod = jarCheckerClass.getMethod(LICENSE_JARCHECKER_GETPROPERTIES_METHOD_NAME);
            this.licenseProperties = (Properties) getPropertiesMethod.invoke(jarCheckerInstance);
        } catch (MalformedURLException exception) {
            // This should never happen
            getLog().fatal("Failed to build license classloader.", exception);
            this.licenseReadThrowable = exception;
        } catch (ClassNotFoundException exception) {
            getLog().fatal("Failed to load class from license jar", exception);
            this.licenseReadThrowable = exception;
        } catch (NoSuchMethodException exception) {
            getLog().fatal("Invalid class found in license jar.", exception);
            this.licenseReadThrowable = exception;
        } catch (InstantiationException exception) {
            getLog().fatal("Failed to instantiate class from license jar", exception);
            this.licenseReadThrowable = exception;
        } catch (InvocationTargetException exception) {
            getLog().fatal("Failed to instantiate class from license jar", exception);
            this.licenseReadThrowable = exception;
        } catch (IllegalAccessException exception) {
            getLog().fatal("Failed to instantiate class from license jar", exception);
            this.licenseReadThrowable = exception;
        }
    }

    private boolean isLicenseInstalledAndReadable() {
        return (this.licenseReadThrowable != null);
    }

    private class LicenseAuditExecutor extends TimerTask {
        private List<ILicenseAuditLineItem> auditLineItems;
        
        /**
         * Create an instance of LicenseAuditExecutor
         * @param licenseAuditLineItems
         */
        private LicenseAuditExecutor(List<ILicenseAuditLineItem> licenseAuditLineItems) {
            if (licenseAuditLineItems == null) {
                throw new NullPointerException("licenseAuditLineItems cannot be null.");
            }
            
            this.auditLineItems = licenseAuditLineItems;
        }

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            if (LicenseAuditorImpl.this.isLicenseInstalledAndReadable()) {
                /**
                 * Theoretically, the system shouldn't have started, but just in
                 * case, print message to logs
                 */
                reportUnreadableLicense();
            } else {
                validateLineItems();
            }
        }

        /**
         * Invoke all of the installed license audit line items
         */
        private void validateLineItems() {
            List<InvalidLicenseLineItemException> failedLineItems = new LinkedList<InvalidLicenseLineItemException>();

            for (ILicenseAuditLineItem nextAuditLineItem : auditLineItems) {
                try {
                    nextAuditLineItem.verify(LicenseAuditorImpl.this.licenseProperties);
                } catch (InvalidLicenseLineItemException exception) {
                    failedLineItems.add(exception);
                }
            }

            reportFailedLineItems(failedLineItems);
        }

        /**
         * Report the list of failed line items
         * 
         * @param failedLineItems
         *            the list of failed line items
         */
        private void reportFailedLineItems(List<InvalidLicenseLineItemException> failedLineItems) {
            if (failedLineItems == null) {
                throw new NullPointerException("failedLineItems cannot be null.");
            }

            if (!failedLineItems.isEmpty()) {
                StringBuffer logMessage = new StringBuffer("\n\nINVALID LICENSE!");
                for (InvalidLicenseLineItemException nextFailure : failedLineItems) {
                    logMessage.append("\n");
                    logMessage.append(nextFailure.getFailureMessage());
                }

                logMessage.append("\n\nPlease contact your account executive to obtain a valid license.\n\n");

                getLog().fatal(logMessage.toString());
            }
        }

        /**
         * Report an unreadable license
         */
        private void reportUnreadableLicense() {
            StringBuffer logMessage = new StringBuffer("\n\nINVALID LICENSE!");
            logMessage.append("\nLicense could not be found and/or read.");
            logMessage.append("\n\nPlease contact your account executive to obtain a valid license.\n\n");
        }
    }
}