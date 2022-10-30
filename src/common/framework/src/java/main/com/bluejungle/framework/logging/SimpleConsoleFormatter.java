/*
 * Created on Sep 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/logging/SimpleConsoleFormatter.java#1 $
 */

public class SimpleConsoleFormatter extends Formatter {

    /**
     * Constructor
     *  
     */
    public SimpleConsoleFormatter() {
        super();
    }

    /**
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public String format(LogRecord log) {
        StringBuffer msg = new StringBuffer();
        msg.append(log.getMessage()).append("\n");
        return msg.toString();
    }
}