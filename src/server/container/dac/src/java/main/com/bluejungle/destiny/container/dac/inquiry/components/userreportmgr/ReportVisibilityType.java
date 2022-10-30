/*
 * Created on May 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/userreportmgr/ReportVisibilityType.java#1 $
 */

public class ReportVisibilityType extends EnumBase {

    public static final ReportVisibilityType ALL_REPORTS = new ReportVisibilityType("All");
    public static final ReportVisibilityType MY_REPORTS = new ReportVisibilityType("My");
    public static final ReportVisibilityType SHARED_REPORTS = new ReportVisibilityType("Shared");

    /**
     * The constructor is private to prevent unwanted instanciations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private ReportVisibilityType(String name) {
        super(name);
    }
}