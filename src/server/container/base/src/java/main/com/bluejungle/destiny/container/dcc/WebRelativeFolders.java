/*
 * Created on Feb 7, 2005
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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/WebRelativeFolders.java#1 $
 */

public class WebRelativeFolders {

    /*
     * Private variables:
     */
    private String relativePathToFolder;


    /*
     * Paths relative to the web-app container:
     */
    public static final WebRelativeFolders CONFIGURATION_FOLDER;

    /*
     * Static initialization block:
     */
    static {
        CONFIGURATION_FOLDER = new WebRelativeFolders("WEB-INF/conf/");
    }
    
    /**
     * 
     * Constructor
     * 
     * @param relativePathToFolder
     */
    private WebRelativeFolders(String path) {
        this.relativePathToFolder = path;
    }

    /**
     * Appends a file or relative-file to the relativePathToFolder
     * 
     * @param relativeFilePath
     *            file path relative to the folder that this object represents
     * @return path to specified file relative to the web-app
     */
    public String getPathOfContainedFile(String relativeFilePath) {
        File relativeFile = new File(this.relativePathToFolder);
        relativeFile = new File(relativeFile, relativeFilePath);
        String path = relativeFile.toString();
        
        //see apache bug 43241, the path must start with '/' by spec
        if (File.separatorChar != '/')
            path = path.replace(File.separatorChar, '/');
        if (!path.startsWith("/"))
            path = "/" + path;
        return path;
    }
}