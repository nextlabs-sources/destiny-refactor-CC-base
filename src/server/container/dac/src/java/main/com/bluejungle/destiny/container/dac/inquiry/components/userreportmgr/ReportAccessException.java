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
 * This exception is thrown whenever the caller does not have the privilege to
 * view or modify the report data.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/inquiry/com/bluejungle/destiny/container/dac/inquiry/InquiryAccessException.java#1 $
 */

public class ReportAccessException extends SingleErrorBlueJungleException {

    /**
     * Constructor
     * 
     * @param msg
     *            message
     * @param attrs
     *            attributes
     */
    public ReportAccessException(String msg, Object[] attrs) {
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
    public ReportAccessException(Throwable cause) {
        super(cause);
    }
}