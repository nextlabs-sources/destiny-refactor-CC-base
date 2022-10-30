package com.bluejungle.destiny.container.dcc.plugin;

/*
 * Created on Dec 09, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/plugin/DCCServerPluginManager.java#1 $:
 */

import java.io.File;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.heartbeat.IServerHeartbeatManager;
import com.bluejungle.framework.heartbeat.ServerHeartbeatManagerImpl;

import com.bluejungle.framework.plugins.FileClassLoader;
import com.bluejungle.framework.plugins.PluginLoaderException;
import com.bluejungle.framework.plugins.PluginLoaderUtils;

public class DCCServerPluginManager implements IDCCServerPluginManager, IConfigurable, ILogEnabled, IInitializable, IHasComponentInfo<IDCCServerPluginManager>, IManagerEnabled
{
    private static final String APPLICATIONS = "applications";
    private static final String PLUGIN_NAME = "name";
    private static final String JAR_PATH = "jar-path";
    private static final String NEXTLABS_SERVER_TOKEN = "[policy-server]";

    public static final ComponentInfo<IDCCServerPluginManager> COMP_INFO =
        new ComponentInfo<IDCCServerPluginManager>(DCCServerPluginManager.class,
                                                   LifestyleType.SINGLETON_TYPE);

    private IServerHeartbeatManager heartbeatMgr = null;
    private IComponentManager manager;
    private IConfiguration config;
    private Log log;
    private Map<String, List<Properties>> componentPropertiesMap = new HashMap<String, List<Properties>>();
    private INamedResourceLocator serverResourceLocator;

    public ComponentInfo<IDCCServerPluginManager> getComponentInfo() {
        return COMP_INFO;
    }

    public void init() {
        // Initialize heartbeat manager
        ComponentInfo<ServerHeartbeatManagerImpl> heartbeatMgrCompInfo = 
        	new ComponentInfo<ServerHeartbeatManagerImpl>(
        		IServerHeartbeatManager.COMP_NAME, 
        		ServerHeartbeatManagerImpl.class, 
        		IServerHeartbeatManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        heartbeatMgr = manager.getComponent(heartbeatMgrCompInfo);

        // Get the location of the plugin folder 
        List<String> folders = new ArrayList<String>();
        serverResourceLocator = (INamedResourceLocator) manager.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        folders.add(serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.PLUGINS_FOLDER.getPathOfContainedFile("config")));

        loadAllProperties(folders);
    }

    private void addPropertiesToComponent(String componentName, Properties properties) {
        List<Properties> plist = componentPropertiesMap.get(componentName);
        if (plist == null) {
            plist = new ArrayList<Properties>();
            componentPropertiesMap.put(componentName, plist);
        }

        plist.add(properties);
    }

    private void loadAllProperties(List<String> folders) {
        for (String folderName : folders) {
            log.debug("Looking for properties files in " + folderName);
            File folder = new File(folderName);

            if (folder != null) {
                if (!folder.exists()) {
                    log.warn("The directory " + folderName + " doesn't exist");
                    continue;
                } else if (!folder.canRead()) {
                    log.error("The directory " + folderName + " can not be read");
                    continue;
                } else if (!folder.isDirectory()) {
                    log.error("The path " + folder + " is not a directory");
                    continue;
                }

                File[] propertiesFiles = PluginLoaderUtils.getAllPropertiesFiles(folder);

                for (File propertiesFile : propertiesFiles) {
                    log.debug("Looking at properties file " + propertiesFile.getAbsolutePath());
                    try {
                        Properties properties = PluginLoaderUtils.getProperties(propertiesFile);
                    
                        // applications is a comma separate list of apps (e.g. "DABS, DMS, Reporter")
                        String appString = (String)properties.get(APPLICATIONS);
                        if (appString == null) {
                            appString = "";
                        }

                        String[] apps = appString.split(", *");
                        
                        for (String app : apps) {
                            addPropertiesToComponent(app, properties);
                        }

                    } catch (PluginLoaderException ple) {
                        log.error("Exception loading " + propertiesFile.getAbsolutePath() + ".  " + ple);
                    }
                }
            }
        }
    }
        
    

    public void initializePlugins(IRegisteredDCCComponent component) {
        initializePlugins(component, new FileClassLoader(Thread.currentThread().getContextClassLoader()));
    }

    public void initializePlugins(IRegisteredDCCComponent component, FileClassLoader fileClassLoader) {
        String appName = component.getComponentType().getName();

        List<Properties> plist = componentPropertiesMap.get(appName);

        if (plist != null) {
            for (Properties properties : plist) {
                String path = (String)properties.get(JAR_PATH);
                if (path == null) {
                    log.error("Unable to find " + JAR_PATH + " property in " + properties);
                    continue;
                }
                
                if (path.toLowerCase().startsWith(NEXTLABS_SERVER_TOKEN)) {
                    path = serverResourceLocator.getFullyQualifiedName(null) + path.substring(NEXTLABS_SERVER_TOKEN.length());
                }
                    
                log.info ("Plugin jar located at " + path);
                String pluginName = (String)properties.get(PLUGIN_NAME);
                if (pluginName == null) {
                    log.error("Unable to find plugin name in " + properties);
                    continue;
                }

                try {
                    initClass(component, pluginName, PluginLoaderUtils.loadPlugin(new File(path), fileClassLoader));
                } catch (PluginLoaderException ple) {
                    log.error("Unable to load plugin " + path + ".  " + ple);
                } catch (DCCServerPluginManagerException spme) {
                    log.error("Unable to load plugin " + path + ".  " + spme);
                }
            }
        } else {
            log.info("No plugins found for " + appName + " component.  Continuing...");
        }
    }

    private void initClass(IRegisteredDCCComponent component, String name, Class<?> clazz) throws DCCServerPluginManagerException {
        if (!IDCCServerPlugin.class.isAssignableFrom(clazz)) {
            throw new DCCServerPluginManagerException("Unable to assign " + name + " class to IDCCServerPluginManager");
        }

        Class<IDCCServerPlugin> spClass = (Class<IDCCServerPlugin>)clazz;
        IDCCServerPlugin serverPlugin;
        try {
            serverPlugin = spClass.newInstance();
        } catch (InstantiationException e) {
            throw new DCCServerPluginManagerException("Unable to create an instance of " + clazz + ".", e);
        } catch (IllegalAccessException e) {
            throw new DCCServerPluginManagerException("Unable to create an instance of " + clazz + ".", e);
        }

        try {
            serverPlugin.init(component);
        } catch (RuntimeException e) {
            throw new DCCServerPluginManagerException("Exception during server plugin init for " + clazz + ".", e);
        }

        register(name, serverPlugin);
    }


    private void register (String name, IDCCServerPlugin plugin) {
        if (plugin instanceof IDCCHeartbeatServerPlugin) {
            log.info("Registering " + name + " as heartbeat plugin");
            heartbeatMgr.register(name, (IDCCHeartbeatServerPlugin)plugin);
        }
    }

    private class DCCServerPluginManagerException extends Exception{
        public DCCServerPluginManagerException(String message, Throwable cause) {
            super(message, cause);
        }

        public DCCServerPluginManagerException(String message) {
            super(message);
        }

        @Override
        public String getMessage() {
            Throwable cause = getCause();
            return cause != null
                ? super.getMessage() + " " + cause.getMessage()
                : super.getMessage();
        }
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    public IComponentManager getManager() {
        return manager;

    }

}
