/*
 * Created on Oct 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/ExternalGroupLinkDataImpl.java#1 $
 */

public class ExternalGroupLinkDataImpl implements IExternalGroupLinkData {

    private String domainName;
    private String title;
    private byte[] externalId;

    /**
     * Constructor
     *  
     */
    public ExternalGroupLinkDataImpl(String domainName, String title, byte[] externalId) {
        super();
        this.domainName = domainName;
        this.title = title;
        this.externalId = externalId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getDomainName()
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getTitle()
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getExternalId()
     */
    public byte[] getExternalId() {
        return this.externalId;
    }

    /**
     * Sets the title
     * 
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}