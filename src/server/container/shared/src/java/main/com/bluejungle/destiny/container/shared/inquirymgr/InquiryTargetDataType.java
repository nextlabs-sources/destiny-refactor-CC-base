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
 * This is the enumeration class for the inquiry target data type. The target
 * data type is the type of data that the inquiry should be looking for.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/InquiryTargetDataType.java#1 $
 */

public abstract class InquiryTargetDataType extends EnumBase {

    public static final InquiryTargetDataType POLICY = new InquiryTargetDataType("POLICY") {
    };

    public static final InquiryTargetDataType ACTIVITY = new InquiryTargetDataType("ACTIVITY") {
    };

    /**
     * The constructor is private to prevent unwanted instanciations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private InquiryTargetDataType(String name) {
        super(name);
    }
}