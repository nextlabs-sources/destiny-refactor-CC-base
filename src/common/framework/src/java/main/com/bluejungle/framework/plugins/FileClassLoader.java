/*
 * Created on Jan 15, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A classloader that allows to add a jar file.
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/plugins/FileClassLoader.java#1 $
 */

public class FileClassLoader extends URLClassLoader {
    public FileClassLoader() {
        super(new URL[] {});
    }

    public FileClassLoader(ClassLoader cl) {
        super(new URL[] {}, cl);
    }

    protected void addFile(File file) throws MalformedURLException {
        super.addURL(file.toURI().toURL());
    }
}
