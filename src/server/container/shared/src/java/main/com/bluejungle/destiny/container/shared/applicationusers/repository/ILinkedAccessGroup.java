/*
 * Created on Sep 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import java.util.Collection;

import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/ILinkedAccessGroup.java#1 $
 */

public interface ILinkedAccessGroup extends IAccessGroup {

    /**
     * Returns whether the given linked group has become orphaned
     * 
     * @return
     */
    public boolean isOrphaned();

    /**
     * Returns the fully qualified name of this group in the external directory.
     * 
     * @return
     */
    public String getQualifiedExternalName();

    /**
     * Returns the external id for this group
     * 
     * @return
     */
    public byte[] getExternalId();

    /**
     * Returns a collection of <IExternalUser>entries
     * 
     * @return
     * @throws ExternalUserAccessException
     */
    public Collection<IExternalUser> getExternalMembers() throws ExternalUserAccessException;
}