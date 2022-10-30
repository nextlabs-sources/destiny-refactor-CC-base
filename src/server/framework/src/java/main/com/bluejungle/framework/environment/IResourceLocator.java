/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.environment;

import java.io.InputStream;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This interface is used to load resources contained in the application. The
 * physical location of the application is abstracted from the user.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IResourceLocator extends IInitializable, IDisposable, IConfigurable, ILogEnabled {

    /**
     * This method returns an input stream to the file described in the path
     * 
     * @param relativePath
     *            relative path to the file within the application
     * @return input stream to the file (if it exists), null otherwise
     */
    public InputStream getResourceAsStream(String relativePath);

    /**
     * This method returns a boolean whether the requested resource
     * "relativePath" exists
     * 
     * @param relativePath
     * @return boolean
     */
    public boolean exists(String relativePath);
}