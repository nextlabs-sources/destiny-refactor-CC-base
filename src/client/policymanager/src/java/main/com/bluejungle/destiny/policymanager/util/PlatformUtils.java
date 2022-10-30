/*
 * Created on Nov 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.util;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.KeyEvent;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.UserProfileEnum;

/**
 * Utilites for interacting with the Eclipse Platform
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/PlatformUtils.java#1 $
 */

public class PlatformUtils {

    /**
     * Find a resource relative to the plugin install directory
     * 
     * @param the
     *            path of the resource to fine
     * @throws IOException
     *             if an error occurs while finding the resource
     */
    public static URL getResource(String path) throws IOException {
        Bundle pluginBundle = Activator.getDefault().getBundle();
        URL foundURL = Platform.find(pluginBundle, new Path(path));
        if (foundURL == null) {
            throw new IllegalArgumentException("Resource with path, " + path + ", could not be found.");
        }

        return Platform.asLocalURL(foundURL);
    }

    public static UserProfileEnum getProfile() {
        IConfigurationElement[] decls = Platform.getExtensionRegistry().getConfigurationElementsFor("com.bluejungle.destiny.policymanager.profile");
        for (IConfigurationElement element : decls) {
            String name = element.getAttribute("name");
            if (name.equalsIgnoreCase(UserProfileEnum.CORPORATE.toString()))
                return UserProfileEnum.CORPORATE;
            else if (name.equalsIgnoreCase(UserProfileEnum.FILESYSTEM.toString()))
                return UserProfileEnum.FILESYSTEM;
            else if (name.equalsIgnoreCase(UserProfileEnum.PORTAL.toString()))
                return UserProfileEnum.PORTAL;
        }
        return null;
    }

    public static void validCharForName(KeyEvent e) {
        if (e.character == '&' || e.character == '$' || e.character == '*' || e.character == '?' || e.character == '/' || e.character == '\\')
            e.doit = false;
    }
}
