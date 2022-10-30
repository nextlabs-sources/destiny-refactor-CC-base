/*
 * Created on Jul 1, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.enrollment;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/AbstractFieldEnumType.java#1 $
 */

public class AbstractFieldEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;
    
    private final String label;

    protected AbstractFieldEnumType(String name, String label) {
        super(name);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
