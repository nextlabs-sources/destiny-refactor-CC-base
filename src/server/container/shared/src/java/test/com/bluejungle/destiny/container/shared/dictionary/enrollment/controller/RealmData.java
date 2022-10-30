/*
 * Created on Aug 11, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller;

import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/RealmData.java#1 $
 */

public class RealmData implements IRealmData {
    private final String name;
    private final EnrollmentTypeEnumType type;
    private final Map<String, String[]> properties;

    public RealmData(String name, Map<String, String[]> properties, EnrollmentTypeEnumType type) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String[]> getProperties() {
        return this.properties;
    }

    public EnrollmentTypeEnumType getType() {
        return this.type;
    }
}
