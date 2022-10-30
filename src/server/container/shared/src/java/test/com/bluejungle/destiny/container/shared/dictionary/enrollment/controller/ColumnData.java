/*
 * Created on Aug 13, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IColumnData;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/ColumnData.java#1 $
 */

public class ColumnData implements IColumnData {
    private final String displayName;
    private final String elementType;
    private final String logicalName;
    private final String type;

    public ColumnData(String displayName, String elementType, String logicalName, String type) {
        this.displayName = displayName;
        this.elementType = elementType;
        this.logicalName = logicalName;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getElementType() {
        return elementType;
    }

    public String getLogicalName() {
        return logicalName;
    }

    public String getType() {
        return type;
    }
}
