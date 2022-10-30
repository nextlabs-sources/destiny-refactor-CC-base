/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This class represents the field that can be queried for a report search
 * specification. A report search specification can only be done on one of these
 * fields.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/persistentreportmgr/ReportMgrQueryFieldType.java#2 $
 */

public class PersistentReportMgrQueryFieldType extends EnumBase {

    public static final PersistentReportMgrQueryFieldType DESCRIPTION = new PersistentReportMgrQueryFieldType("Description");
    public static final PersistentReportMgrQueryFieldType SHARED = new PersistentReportMgrQueryFieldType("Shared");
    public static final PersistentReportMgrQueryFieldType TITLE = new PersistentReportMgrQueryFieldType("Title");

    /**
     * Constructor
     * 
     * @param name
     *            name of the report field to query on
     */
    private PersistentReportMgrQueryFieldType(String name) {
        super(name);
    }
}