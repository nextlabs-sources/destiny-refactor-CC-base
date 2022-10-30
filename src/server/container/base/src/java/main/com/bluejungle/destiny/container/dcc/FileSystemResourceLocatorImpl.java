/*
 * Created on Feb 3, 2005
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

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IConfiguration;

/**
 * This class implements a resource-locator for files within a file system. It
 * is initialized with a base path and can be used to obtain file paths relative
 * to this path.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/FileSystemResourceLocatorImpl.java#2 $
 */

public class FileSystemResourceLocatorImpl implements INamedResourceLocator {

    /**
     * Configuration parameters:
     */
    /*
     *  
     */
    public static final String ROOT_PATH_PARAM = "RootPath";

    /*
     * Defaults to Boolean(FALSE). If Boolean(TRUE) the root path is ensured to
     * exist as a physical folder.
     */
    public static final String CREATE_FOLDER_IF_NONEXISTENT = "CreateFolderIfNonexistent";

    /*
     * Private variables:
     */
    private String rootPath;
    private IConfiguration configuration;
    private Log log;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.environment.IResourceLocator#exists(java.lang.String)
     */
    public boolean exists(String relativePath) {
        File file = new File(this.rootPath, relativePath);
        return file.exists();
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.INamedResourceLocator#getFullyQualifiedName(java.lang.String)
     */
    public String getFullyQualifiedName(String relativePath) {
        String fullyQualifiedName = null;
        if (relativePath == null) {
            fullyQualifiedName = this.rootPath;
        } else {
            File file = new File(this.rootPath, relativePath);
            fullyQualifiedName = file.getAbsolutePath();
        }
        return fullyQualifiedName;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.environment.IResourceLocator#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String relativePath) {
        File file = new File(this.rootPath, relativePath);
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
            if (getLog().isTraceEnabled()) {
                getLog().trace("Constructed file system resource stream for (" + this.rootPath + ", " + relativePath + ")");
            }
        } catch (FileNotFoundException e) {
            getLog().error("Requested file system resource - (" + this.rootPath + ", " + relativePath + ") was not found", e);
        }
        return fileStream;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.rootPath = (String) this.configuration.get(ROOT_PATH_PARAM);

        // Now we check if the folder should be created if it doesn't already
        // exist:
        if (this.configuration.get(CREATE_FOLDER_IF_NONEXISTENT) != null) {
            Boolean createIfNonexistent = (Boolean) this.configuration.get(CREATE_FOLDER_IF_NONEXISTENT);
            if (createIfNonexistent.booleanValue()) {
                File rootPathHandle = new File(this.rootPath);
                if (!rootPathHandle.exists()) {
                    boolean dirsCreated = rootPathHandle.mkdirs();
                    if (!dirsCreated) {
                        getLog().error("Unable to succesfully create root folder: '" + this.rootPath + "' for resource locator");
                    }
                }
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }
}