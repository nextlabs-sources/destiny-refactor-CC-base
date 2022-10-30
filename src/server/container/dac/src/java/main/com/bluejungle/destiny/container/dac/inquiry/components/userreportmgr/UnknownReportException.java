/*
 * Created on Feb 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr;

import com.bluejungle.framework.exceptions.SingleErrorBlueJungleException;

/**
 * This exception is thrown whenever the caller tries to access a report that
 * does not exist in the database. This exception should not be fired when a
 * report exists but when the user does not have access to the report.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/inquiry/com/bluejungle/destiny/container/dac/inquiry/InquiryAccessException.java#1 $
 */

public class UnknownReportException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     * 
     * @param msg
     *            message
     * @param attrs
     *            attributes
     */
    public UnknownReportException(String msg, Object[] attrs) {
        super();
    }

    /**
     * Constructor
     * 
     * @param msg
     *            message
     * @param attrs
     *            attributes
     * @param cause
     *            nested exception
     */
    public UnknownReportException(Throwable cause) {
        super(cause);
    }
}