/*
 * Created on Sep 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import com.bluejungle.destiny.container.shared.applicationusers.core.IGroup;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/IExternalGroup.java#1 $
 */

public interface IExternalGroup extends IGroup {

    /**
     * Returns the distinguished name of this group - for help with
     * disambiguating groups with the same name.
     * 
     * @return
     */
    public String getDistinguishedName();

    /**
     * Returns the binary external id of this group as a byte array
     * 
     * @return external id as a byte array
     */
    public byte[] getExternalId();

    /**
     * Returns the fully qualified external name of this entry
     * 
     * @return qualified external names
     */
    public String getQualifiedExternalName();
}