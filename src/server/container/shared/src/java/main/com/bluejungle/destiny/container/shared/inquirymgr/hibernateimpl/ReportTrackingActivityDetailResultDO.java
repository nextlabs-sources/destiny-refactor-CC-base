/*
 * Created on Nov 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportTrackingActivityDetailResult;
import com.bluejungle.domain.action.ActionEnumType;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportTrackingActivityDetailResultDO.java#1 $
 */

public class ReportTrackingActivityDetailResultDO extends BaseReportActivityResultDO implements IReportTrackingActivityDetailResult {

    /**
     * Constructor 
     */
    public ReportTrackingActivityDetailResultDO() {
        super();
    }
    
    /**
     * Constructor. This constructor is used to take results from a tracking
     * activity log entry.
     * 
     * @param action
     *            action type
     * @param applicationName
     *            application name
     * @param fromResName
     *            name of the "from resource" *
     * @param hostIPAddress
     *            host IP address
     * @param hostName
     *            name of the host
     * @param id
     *            record id
     * @param timestamp
     *            timestamp
     * @param toResName
     *            name of the "to resource"
     * @param userName
     *            name of the user
     */
    public ReportTrackingActivityDetailResultDO(ActionEnumType action, String applicationName, String fromResName, String hostIPAddress, String hostName, Long id, Calendar timestamp, String toResName, String userName, int loggingLevel) {
        super(action, applicationName, fromResName, hostIPAddress, hostName, id, timestamp, toResName, userName, loggingLevel);
    }

    /**
     * 
     * Similar constructor, except that it takes the stored result id value from
     * the stored result record in the result table.
     * 
     * @param action
     *            action type
     * @param applicationName
     *            name of the application
     * @param fromResName
     *            "from resource" name
     * @param hostIPAddress
     *            host IP Address
     * @param hostName
     *            hostname
     * @param id
     *            id of the matching data row
     * @param storedResultId
     *            id of the stored result in the result table
     * @param timestamp
     *            timestamp
     * @param toResName
     *            "to resource" name
     * @param userName
     *            name of the user
     */
    public ReportTrackingActivityDetailResultDO(ActionEnumType action, String applicationName, String fromResName, String hostIPAddress, String hostName, Long id, Long storedResultId, Calendar timestamp, String toResName, String userName, int loggingLevel) {
        super(action, applicationName, fromResName, hostIPAddress, hostName, id, timestamp, toResName, userName, loggingLevel);
        setStoredResultId(storedResultId);
    }

}
