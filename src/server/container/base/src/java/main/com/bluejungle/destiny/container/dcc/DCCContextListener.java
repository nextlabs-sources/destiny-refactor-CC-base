/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.environment.IResourceLocator;
import com.bluejungle.framework.environment.webapp.WebAppResourceLocatorImpl;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.axis.SSLSocketFactoryWrapper;

/**
 * This is the base class for the DCC context listener. This class is invoked
 * when the web application comes up, and it sets up all the required components
 * for the application to start. Upon shutdown of the web application, this
 * class performs the proper cleanup.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/DCCContextListener.java#2 $
 */

public abstract class DCCContextListener implements ServletContextListener {
    /**
     * Name of the container component name
     */
    private static final String CONTAINER_COMP_NAME = "DCCContainerComponent";

    private Log log = LogFactory.getLog(DCCContextListener.class);
    protected IDCCContainer container;
    
    /**
     * Initialization method. This method looks up servlet init parameters, and
     * calls the container factory to produce the right container for the type
     * of DCC component. The DCC component type is specified in init params.
     * 
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent initEvent) {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        
        try {
            //This method is critical for bootstrapping the server and MUST be
            // the first thing invoked by the init() method.
            initDefaultResourceLocators(initEvent.getServletContext());
            
            //Instantiate the container for the DCC component
            HashMapConfiguration containerConfig = new HashMapConfiguration();
            //Places the component configuration inside the container config.
            // This component configuration will be conveyed to the final
            // component itself
            IConfiguration componentConfig = prepareComponentConfiguration(initEvent.getServletContext());           
            containerConfig.setProperty(IDCCContainer.COMP_CONFIG_CONFIG_PARAM_NAME, componentConfig);
            containerConfig.setProperty(IDCCContainer.COMPONENT_TYPE_CONFIG_PARAM, getComponentType());
            containerConfig.setProperty(IDCCContainer.COMPONENT_TYPE_DISPLAY_NAME_CONFIG_PARAM,  getTypeDisplayName());
            containerConfig.setProperty(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM,  componentConfig.get(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM));
            String componentClassName = (String)componentConfig.get(IDCCContainer.COMPONENT_CLASS_CONFIG_PARAM.toString());
            Class clazz = Class.forName(componentClassName);
            Class<? extends IRegisteredDCCComponent> componentClass = clazz;
            containerConfig.setProperty(IDCCContainer.COMPONENT_CLASS_CONFIG_PARAM, componentClass);
            containerConfig.setProperty(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM, componentConfig.get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM));
            containerConfig.setProperty(IDCCContainer.COMPONENT_RESOUCES_CONFIG_PARAM, getApplicationResources());


            ComponentInfo<IDCCContainer> containerInfo = 
                new ComponentInfo<IDCCContainer>(
                    CONTAINER_COMP_NAME, 
                    getContainerClassName(), 
                    IDCCContainer.class, 
                    LifestyleType.SINGLETON_TYPE, 
                    containerConfig);
            this.container = manager.getComponent(containerInfo);
        } catch (Exception e) {
            getLog().fatal("Startup of DCC Container '" + getContainerClassName() + "' failed!!", e);
            manager.shutdown();
            throw new RuntimeException(e);
        }
    }

    /**
     * This function is called by the servlet container and destroys the servlet
     * instance.
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent destroyEvent) {
        try {
            SSLSocketFactoryWrapper.shutdown();
            final IComponentManager manager = ComponentManagerFactory.getComponentManager();
            manager.shutdown();
        } catch (RuntimeException e) {
            getLog().fatal("Shutdown of DCC Container '" + getContainerClassName() + "' failed!!", e);
        }
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return this.log;
    }

    /**
     * Returns the component type
     * 
     * @return the component type
     */
    public abstract ServerComponentType getComponentType();
    
    public abstract String getTypeDisplayName();
    
    /**
     * override me if you want to register any application resources. 
     * They will show in the ManagmentConsole role page. 
     * @return
     */
    protected Set<String> getApplicationResources(){
        return null;
    }

    /**
     * Returns the class name of the container. For special component, the class
     * name can be changed programmatically (or with external configuration if
     * necessary).
     * 
     * @return the class name of the container
     */
    protected Class<? extends IDCCContainer> getContainerClassName() {
        return DefaultContainerImpl.class;
    }

    /**
     * Sets up the resource locators required for the rest of the application -
     * this includes the Web-App resource locator and the Server-Root resource
     * locators. This method is critical for bootstrapping the server and MUST
     * be the first thing invoked by the init() method.
     */
    protected void initDefaultResourceLocators(ServletContext ctx) {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IDestinySharedContextLocator> locatorInfo = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
                DestinySharedContextLocatorImpl.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        IDestinySharedContextLocator locator = manager.getComponent(locatorInfo);

        // Initialize the server-root resource locator:
        String installRootDir = ctx.getInitParameter(IDCCContainer.INSTALL_HOME_PATH_CONFIG_PARAM);
        HashMapConfiguration serverLocatorConfig = new HashMapConfiguration();
        serverLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, installRootDir);
        ComponentInfo<INamedResourceLocator> serverLocatorInfo = 
            new ComponentInfo<INamedResourceLocator>(
                DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR, 
                FileSystemResourceLocatorImpl.class, 
                INamedResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                serverLocatorConfig);
        INamedResourceLocator serverResourceLocator = manager.getComponent(serverLocatorInfo);

        // Register a FileLocator instance for use by subcomponents to find
        // files in the web app - this should be done *BEFORE* the container is
        // created.
        HashMapConfiguration webAppFileLocatorConfig = new HashMapConfiguration();
        webAppFileLocatorConfig.setProperty(WebAppResourceLocatorImpl.CONTAINER_CTX_CONFIG_PARAM, ctx);
        ComponentInfo<IResourceLocator> webAppFileLocatorInfo = 
            new ComponentInfo<IResourceLocator>(
                DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR, 
                WebAppResourceLocatorImpl.class, 
                IResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                webAppFileLocatorConfig);
        manager.getComponent(webAppFileLocatorInfo);
        
        // Initialize the temporary directory locator for the current web-app:
        String contextName = ctx.getServletContextName();
        String tempDir = System.getProperty("java.io.tmpdir");
        String webAppTempDir = new File(tempDir, contextName).getAbsolutePath();
        HashMapConfiguration webAppTempDirResourceLocatorConfig = new HashMapConfiguration();
        webAppTempDirResourceLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, webAppTempDir);
        webAppTempDirResourceLocatorConfig.setProperty(FileSystemResourceLocatorImpl.CREATE_FOLDER_IF_NONEXISTENT, new Boolean(true));
        ComponentInfo<INamedResourceLocator> webAppTempDirResourceLocatorInfo = 
            new ComponentInfo<INamedResourceLocator>(
                DCCResourceLocators.WEB_APP_TEMP_DIR_RESOURCE_LOCATOR, 
                FileSystemResourceLocatorImpl.class, 
                INamedResourceLocator.class, 
                LifestyleType.SINGLETON_TYPE, 
                webAppTempDirResourceLocatorConfig);
        INamedResourceLocator webAppTempDirResourceLocator = manager.getComponent(webAppTempDirResourceLocatorInfo);
        
        // Initialize the application information component:
        HashMapConfiguration appInfoConfig = new HashMapConfiguration();
        appInfoConfig.setProperty(ApplicationInformationImpl.SERVLET_CONTEXT, ctx);
        ComponentInfo<IApplicationInformation> appInfoCompInfo = 
            new ComponentInfo<IApplicationInformation>(
                IApplicationInformation.COMP_NAME, 
                ApplicationInformationImpl.class, 
                IApplicationInformation.class, 
                LifestyleType.SINGLETON_TYPE, 
                appInfoConfig);
        IApplicationInformation appInfo = manager.getComponent(appInfoCompInfo);
    }

    /**
     * This method builds the component configuration. The basic component
     * configuration values are prepared here, but child classes are free to add
     * there own configuration based on the component class implementation. All
     * the servlet context init parameters (from web.xml and from server.xml)
     * are passed here.
     * 
     * @return a component configuration.
     */
    protected HashMapConfiguration prepareComponentConfiguration(ServletContext ctx) {
        if (ctx == null) {
            throw new NullPointerException("Servlet context cannot be null");
        }
        HashMapConfiguration result = new HashMapConfiguration();
        Enumeration nameEnum = ctx.getInitParameterNames();
        while (nameEnum.hasMoreElements()) {
            String initParamName = (String) nameEnum.nextElement();
            String initParamValue = ctx.getInitParameter(initParamName);
            result.setProperty(initParamName, initParamValue);
        }
        result.setProperty(IDCCContainer.SERVLET_CONTEXT_CONFIG_PARAM, ctx);

        // For JBoss we read the application properties via a resource defined in a module specified
        // by the jboss-deployment-structure.xml file
        readPropertiesFromResource("controlcenter.properties", result);
        readPropertiesFromResource("app.properties", result);

        readContextSpecificProperties(result);

        return result;
    }

    private static HashSet<String> passwordKeys = new HashSet<String>();

    static {
        // Passwords in the properties files are encrypted with our own reversible encryptor. Note
        // the keys here and they will be decrypted automatically.
        passwordKeys.add("nextlabs.javax.net.ssl.keyStorePassword");
        passwordKeys.add("nextlabs.javax.net.ssl.trustStorePassword");
    };

    private void readPropertiesFromResource(String resourceName, HashMapConfiguration result) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        readPropertiesFromInputStream(is, result);
        if (is != null) {
            try {
                is.close();
            } catch (IOException ioe) {
            }
        }
    }

    private void readContextSpecificProperties(HashMapConfiguration result) {
        String fileNameProperty = "nextlabs." + getComponentType().getName().toLowerCase() + ".config.file";

        String fileName = System.getProperty(fileNameProperty);

        if (fileName == null) {
            getLog().info("No per-application configuration found for " + getComponentType().getName().toLowerCase() + " (via " + fileNameProperty + " attribute)");
            return;
        }

        InputStream is = null;

        try {
            is = new FileInputStream(fileName);

            readPropertiesFromInputStream(is, result);
        } catch (FileNotFoundException fnfe) {
            getLog().info("No per-application configuration in file " + fileName);
            return;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ie) {
                }
            }
        }
    }

    private void readPropertiesFromInputStream(InputStream is, HashMapConfiguration result) {
        if (is != null) {
            try {
                Properties props = new Properties();
                props.load(is);
                
                ReversibleEncryptor decryptor = new ReversibleEncryptor();
                
                for (String name : props.stringPropertyNames()) {
                    String value = (String)props.getProperty(name);
                    
                    if (passwordKeys.contains(name)) {
                        result.setProperty(name, decryptor.decrypt(value));
                    } else {
                        result.setProperty(name, value);
                    }
                }
            } catch (IOException e) {
                getLog().info("Unable to load properties for " + getContainerClassName() + " from app.properties");
            }
        }
    }

}
