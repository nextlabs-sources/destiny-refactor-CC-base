/*
 * Created on Mar 1, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import net.sf.hibernate.HibernateException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;
import com.nextlabs.destiny.container.shared.customapps.mapping.PolicyApplicationJO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportUIDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/ExternalApplicationLoader.java#1 $
 */

public class ExternalApplicationLoader implements IExternalApplicationLoader, ILogEnabled,
        IConfigurable, IManagerEnabled, ExternalApplicationFileStructure, IExternalApplication {
    private static final String CUSTOM_APP_SUFFIX = ".jar";
    
    private Log log;
    private IConfiguration configuration;
    private IComponentManager manager;
    
    /**
     * 
     * @throws Exception is from customAppDataManager.createCustomAppData();
     */
    public void load() throws IllegalArgumentException, HibernateException {
        INamedResourceLocator locator = (INamedResourceLocator) manager.getComponent(
                DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        
        final File[] appFolders;
        if (locator != null) {
            String folder = locator.getFullyQualifiedName(ServerRelativeFolders.CUSTOM_APPS_FOLDER.getPath());
            appFolders = new File[] { new File(folder) };
        } else {
            appFolders = configuration.get(APP_FOLDERS_KEY);
        }

        if (appFolders == null) {
            throw new IllegalArgumentException("You must set either 'INamedResourceLocator' or '" + APP_FOLDERS_KEY + "'.");
        }
        
        final CustomAppDataManager customAppDataManager = manager.getComponent(CustomAppDataManager.class);
        
        
        // Currently we don't have a update method, everything will be deleted and re-insert
        // Later, when we have the management part or lifecycle, probably we can't just delete everything.
        customAppDataManager.deleteAllCustomAppData();
        
        for (File appFolder : appFolders) {
            getLog().info("loading folder " + appFolder.getAbsolutePath());
            File[] customAppFiles = appFolder.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.isFile()
                            && pathname.getName().toLowerCase().endsWith(CUSTOM_APP_SUFFIX)) {
                        return true;
                    }
                    return false;
                }
            });
            
            if (customAppFiles != null) {
                for (File customAppFile : customAppFiles) {
                    getLog().debug("loading file " + customAppFile.getAbsolutePath());
                    try {
                        CustomAppDO customAppDO = read(customAppFile);
                        getLog().info("Loaded CustomApps " + customAppDO.getName() + " from file "
                                        + customAppFile.getAbsolutePath());
                        customAppDataManager.createCustomAppData(customAppDO);
                    } catch (Exception e) {
                        getLog().info("Fail to load customApp file, " + customAppFile.getAbsolutePath() + ".", e);
                        //continue
                    }
                }
            }
        }
    }
    
    
    CustomAppDO read(File customAppFile) throws InvalidCustomAppException {
        String customAppPath = customAppFile.getAbsolutePath();
        
        CustomAppDO customAppDO;
        
        //create reader that knows how to read the custom app
        CustomAppJarReader appReader = CustomAppJarReader.create(customAppFile);
        try {
            // get the java object
            PolicyApplicationJO dataConfig = appReader.read(
                    ExternalApplicationFileStructure.DATA_CONFIG_XML,
                    DS_CONFIG_XSD, 
                    new PolicyApplicationJO()
            );
            
            //convert from java object to database object
            CustomApplicationJtoDConverter converter = new CustomApplicationJtoDConverter(appReader);
            
            try {
                customAppDO = converter.convert(dataConfig);
            } catch (IOException e1) {
                throw new InvalidCustomAppException("Error during reading '" + DATA_CONFIG_XML
                        + " ' in custom app, " + customAppPath);
            }
            
            // read the ui config
            String uiConfigXmlContent;
            try {
                uiConfigXmlContent = appReader.getString(UI_CONFIG_XML);
            } catch (IOException e) {
                throw new InvalidCustomAppException("Can't read '" + UI_CONFIG_XML
                        + " ' in custom app, " + customAppPath);
            }
            
            CustomReportUIDO customReportUIDO = new CustomReportUIDO();
            customReportUIDO.setFileContent(uiConfigXmlContent);
            customReportUIDO.setCustomApp(customAppDO);
            customAppDO.setReportUI(customReportUIDO);
        } finally{
            try {
                appReader.close();
            } catch (IOException e) {
                log.error("Fail to close jarReader. " + appReader.customAppPath);
            }
        }
        return customAppDO;
    }
    
    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    public IComponentManager getManager() {
        return manager;
    }

    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }
}
