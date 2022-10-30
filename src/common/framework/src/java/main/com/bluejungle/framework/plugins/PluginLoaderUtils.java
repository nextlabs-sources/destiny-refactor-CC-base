package com.bluejungle.framework.plugins;

/*
 * Created on Dec 09, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/plugins/PluginLoaderUtils.java#1 $:
 */
import java.io.File;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginLoaderUtils
{
    private static final String PROVIDER_CLASS_MANIFEST = "Provider-Class";

    /**
     * return all configuration file under <code>folder</code>.
     * Currently, they are files that ends with .properties. Case-insensitive. And doesn't lookup subdirectory.
     * @param folder to look up the configuration files.
     * @return
     */
    public static File[] getAllPropertiesFiles(File folder) {
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
     
        if (propertiesFiles == null) {
            propertiesFiles = new File[0];
        }

        return propertiesFiles;
    }

    public static Properties getProperties(File propertiesFile) throws PluginLoaderException {
        Properties properties = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            throw new PluginLoaderException(propertiesFile + " doesn't exist.", e);
        }

        try {
            properties.load(is);
        } catch (IOException e) {
            throw new PluginLoaderException("Unable to load properties from file " + propertiesFile, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new PluginLoaderException("Unable to close file " + propertiesFile, e);
            }
        }

        return properties;
    }
 
    public static Class<?> loadPlugin(File file)  throws PluginLoaderException {
        return loadPlugin(file, new FileClassLoader());
    }

    public static Class<?> loadPlugin(File file, FileClassLoader classLoader) throws PluginLoaderException {
        if (!file.exists()) {
            throw new PluginLoaderException ("File " + file.getAbsolutePath() + " doesn't exist");
        } else if (!file.canRead()) {
            throw new PluginLoaderException ("File " + file.getAbsolutePath() + " is not readable");
        } else if (!file.isFile()) {
            throw new PluginLoaderException (file.getAbsolutePath() + " is not a file");
        }

        Attributes attrs;
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            attrs = manifest.getMainAttributes();
        } catch (IOException e) {
            throw new PluginLoaderException("Unable to load manifest\n");
        } catch (RuntimeException e){
            throw new PluginLoaderException("Unexpected exception while reading manifest.", e);
        }

        String providerClass = attrs.getValue(PROVIDER_CLASS_MANIFEST);
        if (providerClass == null || providerClass.trim().length() == 0) {
            throw new PluginLoaderException("provider class is not defined in " + file.getAbsolutePath());
        }

        //load the jar
        try {
            classLoader.addFile(file);
        } catch (MalformedURLException e) {
            throw new PluginLoaderException("Fail to load the jar " + file.getAbsolutePath(), e);
        }

        //check the class exists in the jar
        Class<?> clazz;
        try {
            clazz = Class.forName(providerClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new PluginLoaderException(PROVIDER_CLASS_MANIFEST + " doesn't define correctly in manifest. " + providerClass + " is not the jar.", e);
        }

        return clazz;
    }
}
