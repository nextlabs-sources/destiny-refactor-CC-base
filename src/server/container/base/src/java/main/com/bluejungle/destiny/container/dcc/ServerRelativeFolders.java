/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.io.File;

/**
 * This is a class of constants that represent folders under the web-app root.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/ServerRelativeFolders.java#1 $
 */

public class ServerRelativeFolders {

    /*
     * Private variables:
     */
    private String relativePathToFolder;

    /*
     * Paths relative to the web-app container:
     */
    public static final ServerRelativeFolders ALIASED_SHARES_FOLDER = new ServerRelativeFolders("aliased_shares");
    public static final ServerRelativeFolders CERTIFICATES_FOLDER   = new ServerRelativeFolders("certificates");
    public static final ServerRelativeFolders CONFIGURATION_FOLDER  = new ServerRelativeFolders("configuration");
    public static final ServerRelativeFolders CUSTOM_APPS_FOLDER    = new ServerRelativeFolders("custom_apps");
    public static final ServerRelativeFolders LICENSE_FOLDER        = new ServerRelativeFolders("license");
    public static final ServerRelativeFolders LOG_QUEUE_FOLDER      = new ServerRelativeFolders("logqueue");
    public static final ServerRelativeFolders PLUGINS_FOLDER        = new ServerRelativeFolders("plugins");
    public static final ServerRelativeFolders REPORTS_FOLDER        = new ServerRelativeFolders("reports");
    public static final ServerRelativeFolders SCRIPTS_FOLDER        = new ServerRelativeFolders("scripts");

    /**
     * Constructor
     *  
     */
    private ServerRelativeFolders(String path) {
        relativePathToFolder = path;
    }

    /**
     * Appends a file or relative-file to the relativePathToFolder
     * 
     * @param relativeFilePath
     *            file path relative to the folder that this object represents
     * @return path to specified file relative to the web-app
     */
    public String getPathOfContainedFile(String relativeFilePath) {
        File relativeFile = new File(relativePathToFolder);
        relativeFile = new File(relativeFile, relativeFilePath);
        return relativeFile.toString();
    }

    /**
     * Returns the relative-path of this folder
     * 
     * @return relative path
     */
    public String getPath() {
        return relativePathToFolder;
    }
}
