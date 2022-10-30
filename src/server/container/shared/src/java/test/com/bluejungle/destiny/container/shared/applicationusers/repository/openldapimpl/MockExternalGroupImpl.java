/*
 * Created on Oct 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.openldapimpl;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockExternalGroupImpl.java#1 $
 */

public class MockExternalGroupImpl implements IExternalGroup {

    private String title;
    private String domain;
    private byte[] id;
    private String uniqueName;

    /**
     * Constructor
     *  
     */
    public MockExternalGroupImpl(byte[] id, String title, String domain) {
        super();
        this.title = title;
        this.domain = domain;
        this.id = id;
        this.uniqueName = title + "@" + domain;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getDistinguishedName()
     */
    public String getDistinguishedName() {
        return this.uniqueName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getExternalId()
     */
    public byte[] getExternalId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getQualifiedExternalName()
     */
    public String getQualifiedExternalName() {
        return this.uniqueName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IGroup#getTitle()
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.domain;
    }
}