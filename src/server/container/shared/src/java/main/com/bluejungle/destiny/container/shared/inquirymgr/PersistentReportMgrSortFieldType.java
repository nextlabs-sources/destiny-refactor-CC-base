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
 * This class represents the field that can be sorted on for a user search
 * specification.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/persistentreportmgr/ReportMgrSortFieldType.java#2 $
 */

public class PersistentReportMgrSortFieldType extends EnumBase {

    public static final PersistentReportMgrSortFieldType DESCRIPTION = new PersistentReportMgrSortFieldType("Description");
    public static final PersistentReportMgrSortFieldType SHARED = new PersistentReportMgrSortFieldType("Shared");
    public static final PersistentReportMgrSortFieldType TITLE = new PersistentReportMgrSortFieldType("Title");

    /**
     * Constructor
     * 
     * @param name
     *            name of the userQueryFieldType
     */
    private PersistentReportMgrSortFieldType(String name) {
        super(name);
    }
}