package com.bluejungle.framework.plugins;

/*
 * Created on Dec 09, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/plugins/PluginLoaderException.java#1 $:
 */

public class PluginLoaderException extends Exception {
    PluginLoaderException(String message, Throwable cause) {
        super (message, cause);
    }

    PluginLoaderException(String message) {
        super (message);
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        return cause != null  ? super.getMessage() + " " + cause.getMessage() : super.getMessage();
    }
}
