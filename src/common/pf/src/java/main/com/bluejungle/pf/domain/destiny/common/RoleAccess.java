package com.bluejungle.pf.domain.destiny.common;

import java.util.Collection;

//All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc, Redwood City CA,
//Ownership remains with Blue Jungle Inc, All rights reserved worldwide.

/**
 * TODO Write file summary here.
 * 
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/common/RoleAccess.java#1 $
 */

/**
 * @author pkeni
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RoleAccess {

    /**
     * 
     */
    private IDSpec     role;
    private Collection actions;
    public RoleAccess(IDSpec role, Collection actions) {
        super();
        this.role = role;
        this.actions = actions;
    }

    public Collection getActions() {
        return actions;
    }
    public IDSpec getRole() {
        return role;
    }
}
