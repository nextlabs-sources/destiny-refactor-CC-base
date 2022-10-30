/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This is the enumeration class for the report summary type. The report summary
 * type is the type of summary that should be performed by the report.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/ReportSummaryType.java#1 $
 */

public class ReportSummaryType extends EnumBase {

    public static final ReportSummaryType NONE = new ReportSummaryType("None");
    public static final ReportSummaryType POLICY = new ReportSummaryType("Policy");
    public static final ReportSummaryType RESOURCE = new ReportSummaryType("Resource");
    public static final ReportSummaryType TIME_DAYS = new ReportSummaryType("TimeDays");
    public static final ReportSummaryType TIME_MONTHS = new ReportSummaryType("TimeMonths");
    public static final ReportSummaryType USER = new ReportSummaryType("User");

    /**
     * The constructor is private to prevent unwanted instanciations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private ReportSummaryType(String name) {
        super(name);
    }

    /**
     * Retrieve a ReportSummaryType instance by name
     * 
     * @param name
     *            the name of the ReportSummaryType
     * @return the ReportSummaryType associated with the provided name
     * @throws IllegalArgumentException
     *             if no ReportSummaryType exists with the specified name
     */
    public static ReportSummaryType getReportSummaryType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        return getElement(name, ReportSummaryType.class);
    }
}