/*
 * Created on Jan 26, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/importexport/src/java/main/com/nextlabs/logging/SimpleFormatter.java#1 $
 */

public class SimpleFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(record.getLevel().getLocalizedName())
          .append(": ")
          .append(formatMessage(record));
        Throwable throwable = record.getThrown();
        if (throwable != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                printWriter.close();
                sb.append(stringWriter.toString());
            } catch (Exception e) {
                //ignore
            }
        }
        sb.append(ConsoleDisplayHelper.NEWLINE);
        return sb.toString();
    }

}
