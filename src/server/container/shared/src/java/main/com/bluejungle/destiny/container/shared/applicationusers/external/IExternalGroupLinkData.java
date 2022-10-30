/*
 * Created on Sep 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/IExternalGroupLinkData.java#1 $
 */

public interface IExternalGroupLinkData {

    /**
     * Returns the domain to which this group belongs
     * 
     * @return domain name
     */
    public String getDomainName();

    /**
     * Returns the title of this external group
     * 
     * @return title
     */
    public String getTitle();

    /**
     * Returns the external id that serves as the physical link
     * 
     * @return external id
     */
    public byte[] getExternalId();
}