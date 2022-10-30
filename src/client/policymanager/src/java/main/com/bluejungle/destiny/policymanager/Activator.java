/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/Activator.java#2 $
 */

public class Activator extends AbstractUIPlugin {

    public static String ID = "com.bluejungle.destiny.policymanager";

    // The shared instance.
    private static Activator plugin;

    /**
     * The constructor.
     */
    public Activator() {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static Activator getDefault() {
        return plugin;
    }
}
