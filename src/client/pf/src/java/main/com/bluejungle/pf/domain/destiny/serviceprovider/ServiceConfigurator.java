/*
 * Created on Jan 14, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.serviceprovider;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Integer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProvider;
import com.nextlabs.pf.domain.destiny.serviceprovider.IConfigurableServiceProvider;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/ServiceConfigurator.java#1 $
 */

public class ServiceConfigurator {
    private static final Log LOG = LogFactory.getLog(ServiceConfigurator.class);

    /*
     * all the key is case sensitive.
     */
    private static final String NAME = "name";
    private static final String PRIORITY = "priority";
    private static final String JAR_PATH = "jar-path";
    private static final String PROVIDER_CLASS_MANIFEST = "Provider-Class";
    //the token is case insensitive
    private static final String NEXTLABS_FOLDER_TOKEN = "[nextlabs]";
    
    private FileClassLoader classLoader;
    private final ServiceProviderManager manager;
    
    ServiceConfigurator(ServiceProviderManager manager, FileClassLoader classLoader){
        this.classLoader = classLoader;
        this.manager = manager;
    }
    
    ServiceConfigurator(ServiceProviderManager manager){
        this(manager, new FileClassLoader(Thread.currentThread().getContextClassLoader()));
    }
    
    protected FileClassLoader getFileClassLoader(){
        return classLoader;
    }
    
    /**
     * read all configuration file under <code>folder</code>, initialize each service provider.
     * Any error will fail gracefully.
     * @param folder
     */
    public void init(File folder){
        assert folder != null;
        //check folder
        if (!folder.exists()) {
            LOG.error("The directory, " + folder.getAbsolutePath() + ", doesn't exist.");
            return;
        } else if (!folder.canRead()) {
            LOG.error("The directory, " + folder.getAbsolutePath() + ", can not be read.");
            return;
        } else if( !folder.isDirectory() ){
            LOG.error("The path, " + folder.getAbsolutePath() + ", is not a file.");
            return;
        }
        LOG.debug("Loading properties files in " + folder.getAbsolutePath());

        //get all configuration
        File[] propertiesFiles = getAllConfigurationFiles(folder);

        Map<Integer, ArrayList<ConfigurationSummary>> configMap = new TreeMap<Integer, ArrayList<ConfigurationSummary>>();

        //and load each configuration
        for(File propertiesFile : propertiesFiles){
            try {
                LOG.debug("Loading " + propertiesFile.getAbsolutePath());
                ConfigurationSummary config = loadConfiguration(propertiesFile);

                ArrayList<ConfigurationSummary> configList = configMap.get(config.getPriority());

                if (configList == null) {
                    configList = new ArrayList<ConfigurationSummary>();
                    configMap.put(config.getPriority(), configList);
                }

                configList.add(config);
            } catch (ServiceConfiguratorException e) {
                LOG.error(e.getMessage() + " Location: " + propertiesFile.getAbsolutePath()
                        + " Skip to next file.");
            }
        }

        
        for (ArrayList<ConfigurationSummary> configList : configMap.values()) {
            for (ConfigurationSummary config : configList) {
                try {
                    LOG.debug("Loading " + config.getPath());
                    Class<?> clazz = loadJar(new File(config.getPath()), getFileClassLoader());

                    initClass(config.getServiceName(), clazz, config.getProperties());
                } catch (ServiceConfiguratorException e) {
                    LOG.error(e.getMessage() + " when loading jar at " + config.getPath() + ". Skipping to next file");
                }
            }
        }
    }
    
    private class ServiceConfiguratorException extends Exception{
        public ServiceConfiguratorException(String message, Throwable cause) {
            super(message, cause);
        }

        public ServiceConfiguratorException(String message) {
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
    
    private class ConfigurationSummary {
        private final int loadPriority;
        private final String serviceName;
        private final String filePath;
        private final Properties properties;

        private ConfigurationSummary(String serviceName, String filePath, Properties properties) {
            this(-1, serviceName, filePath, properties);
        }

        private ConfigurationSummary(int loadPriority, String serviceName, String filePath, Properties properties) {
            this.loadPriority = loadPriority;
            this.serviceName = serviceName;
            this.filePath = filePath;
            this.properties = properties;
        }

        public int getPriority() {
            return loadPriority;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getPath() {
            return filePath;
        }

        public Properties getProperties() {
            return properties;
        }
    }

    /**
     * return all configuration file under <code>folder</code>.
     * Currently, they are files that ends with .properties. Case-insensitive. And doesn't lookup subdirectory.
     * @param folder to look up the configuration files.
     * @return
     */
    protected File[] getAllConfigurationFiles(File folder){
        File[] propertiesFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File f) {
                //only accept readable files.
                if (f.isFile() && f.canRead()) {
                    String name = f.getName();
                    //case insensitive
                    name = name.toLowerCase();
                    if (name.endsWith(".properties")) {
                        return true;
                    }
                }
                return false;
            }
        });
        
        return propertiesFiles;
    }
    
    /**
     * load the configuration file and initialize the service.
     * @param file is the configuration file.
     * @throws ServiceConfiguratorException
     */
    protected ConfigurationSummary loadConfiguration(File file) throws ServiceConfiguratorException{
        //load the configuration as java properties file.
        Properties properties = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ServiceConfiguratorException(file + " doesn't exist.", e);
        }
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new ServiceConfiguratorException("Unable to load configuration.", e);
        }finally{
            try {
                is.close();
            } catch (IOException e) {
                throw new ServiceConfiguratorException("Fail to close inputstream", e);
            }
        }

        //check the content, such as name, path, priority, etc
        int priority = 0;
        String priorityStr = properties.getProperty(PRIORITY);

        if (priorityStr != null) {
            try {
                priority = Integer.parseInt(priorityStr);
            } catch (NumberFormatException e) {
                priority = 0;
            }
        }

        String name = properties.getProperty(NAME);
        if(name == null){
            throw new ServiceConfiguratorException("The key, '" + NAME + "' is missing in the configuration.");
        }

        if(manager.isRegistered(name)){
            throw new ServiceConfiguratorException("'" + name + "' is already registered." );
        }
        
        String path = properties.getProperty(JAR_PATH);
        if(path == null){
            throw new ServiceConfiguratorException("The key, '" + JAR_PATH + "' is missing in the configuration.");
        }
        if (path.toLowerCase().startsWith(NEXTLABS_FOLDER_TOKEN)) {
            String currentWorkingDirStr = System.getProperty("user.dir");
            File currentWorkingDir = new File(currentWorkingDirStr);
            String parent = currentWorkingDir.getParent();
            //The current working dir is the root
            if (parent == null) {
                parent = currentWorkingDirStr;
            }
            path = parent + path.substring(NEXTLABS_FOLDER_TOKEN.length());
        }
    
        return new ConfigurationSummary(priority, name, path, properties);
    }
    /**
     * load the jar that specified in the configuration file.
     * @param file is the jar file
     * @param classLoader will be used to load the service class 
     * @throws ServiceConfiguratorException
     */
    protected Class<?> loadJar(File file, FileClassLoader classLoader)
            throws ServiceConfiguratorException {
        // check the jar file
        if (!file.exists()) {
            throw new ServiceConfiguratorException("The file, " + file.getAbsolutePath() + ", doesn't exist.");
        } else if (!file.canRead()) {
            throw new ServiceConfiguratorException("The file, " + file.getAbsolutePath() + ", can not be read.");
        } else if( !file.isFile() ){
            throw new ServiceConfiguratorException("The path, " + file.getAbsolutePath() + ", is not a file.");
        }
        
        //read jar manifest
        Attributes attrs;
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            attrs = manifest.getMainAttributes();
        } catch (IOException e) {
            throw new ServiceConfiguratorException("Unable to load manifest.", e);
        } catch (RuntimeException e){
            throw new ServiceConfiguratorException("Unexpected exception while reading manifest.", e);
        }

        //check manifest
        String providerClass = attrs.getValue(PROVIDER_CLASS_MANIFEST);
        if (providerClass == null || providerClass.trim().length() == 0) {
            throw new ServiceConfiguratorException("providerClass is not definied in the jar.");
        }
        
        //load the jar
        try {
            classLoader.addFile(file);
        } catch (MalformedURLException e) {
            throw new ServiceConfiguratorException("Fail to load the jar.", e);
        }

        //check the class exists in the jar
        Class<?> clazz;
        try {
            clazz = Class.forName(providerClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ServiceConfiguratorException(PROVIDER_CLASS_MANIFEST + " doesn't define correctly in manifest. " + providerClass + " is not the jar.", e);
        }
        
        return clazz;
    }
    
    @SuppressWarnings("unchecked")
    protected void initClass(String name, Class<?> clazz, Properties properties) throws ServiceConfiguratorException {
    	//check if the class has a correct interface
        if (!IServiceProvider.class.isAssignableFrom(clazz)) {
            throw new ServiceConfiguratorException("providerClass doesn't implement expected interface in the jar.");
        }
        
        //create instance
        Class<IServiceProvider> spClass = (Class<IServiceProvider>)clazz;
        IServiceProvider serviceProvider;
        try {
            serviceProvider = spClass.newInstance();
        } catch (InstantiationException e) {
            throw new ServiceConfiguratorException("Unable to create an instance of " + clazz + ".", e);
        } catch (IllegalAccessException e) {
            throw new ServiceConfiguratorException("Unable to create an instance of " + clazz + ".", e);
        }
        
        if (serviceProvider instanceof IConfigurableServiceProvider) {
            ((IConfigurableServiceProvider)serviceProvider).setProperties(properties);
        }

        // init the service
        try {
            serviceProvider.init();
        } catch (Exception e){
            throw new ServiceConfiguratorException("Exception during serviceProvider init.", e);
        }
        
        //everything is ok, register it.
        try {
            manager.register(name, serviceProvider);
        } catch (IllegalArgumentException e) {
            throw new ServiceConfiguratorException("'" + name + "' is already registered.", e);
        }
    }
}
