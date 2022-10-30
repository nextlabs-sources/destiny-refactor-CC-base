/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Types;

import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * This is the user hibernate class for the report summary type. It allows the
 * report summary type to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportSummaryUserType.java#2 $
 */

public class ReportSummaryUserType extends EnumUserType<ReportSummaryType> {

    /**
     * ReportSummaryUserType is saved as char(1)
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Constructor
     */
    public ReportSummaryUserType() {
        super(new ReportSummaryType[] { 
        			ReportSummaryType.NONE, 
        			ReportSummaryType.POLICY, 
        			ReportSummaryType.RESOURCE, 
        			ReportSummaryType.TIME_DAYS, 
        			ReportSummaryType.TIME_MONTHS, 
        			ReportSummaryType.USER }, 
        		new String[] { "N", "P", "R", "D", "M", "U" },
                ReportSummaryType.class);
    }
}