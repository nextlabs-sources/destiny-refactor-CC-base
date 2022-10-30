/*
 * Created on Aug 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.versionfactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;
import com.bluejungle.versionexception.InvalidVersionException;

/**
 * The Default Version Factory
 * FIX ME - If needed can be changed to look for a subclass (or refactor into an interface) in the classpath similar to the way standard xml factories 
 * work in jdl
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/main/com/bluejungle/versionfactory/VersionFactory.java#1 $
 */

public class VersionFactory {

    private static final String VERSION_PROPERTIES = "META-INF/com/nextlabs/version/version.properties";
    
    /**
     * Returns the version of the component
     * @return IVersion representing the version of the component
     * @throws InvalidVersionException
     */
    public IVersion getVersion() throws InvalidVersionException, IOException {
        int major;
        int minor;
        int maintenance;
        int patch;
        int build;
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties versionProperties = new Properties();
        InputStream versionInput = loader.getResourceAsStream(VERSION_PROPERTIES);
        try {
            versionProperties.load(versionInput);
        } finally {
            try {
                versionInput.close();
            } catch (IOException e) {
                //TODO log
            }
        }
        
        major = Integer.parseInt(versionProperties.getProperty("major"));
        minor = Integer.parseInt(versionProperties.getProperty("minor"));
        maintenance = Integer.parseInt(versionProperties.getProperty("maintenance"));
        patch = Integer.parseInt(versionProperties.getProperty("patch"));
        build = Integer.parseInt(versionProperties.getProperty("build"));
        
        IVersion version = new VersionDefaultImpl(major, minor, maintenance, patch, build);        
        return version;
    }

    public static IVersion makeVersion(int major, int minor, int maintenance, int patch, int build) {
        return new VersionDefaultImpl(major, minor, maintenance, patch, build);
    }
}
