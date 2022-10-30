/*
 * Created on Sep 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/ILDAPEnumeratedGroup.java#1 $
 */

public interface ILDAPEnumeratedGroup extends IExternalGroup {

    /**
     * Returns an array of member DN values
     * 
     * @return array of member DNs
     */
    public String[] getMemberDNs();
}