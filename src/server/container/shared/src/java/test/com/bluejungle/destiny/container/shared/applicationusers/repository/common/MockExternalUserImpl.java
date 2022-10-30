/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.common;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockExternalUserImpl.java#1 $
 */

public class MockExternalUserImpl implements IExternalUser {

    private String login;
    private String fn;
    private String ln;
    private String domain;
    private String uniqueName;

    /**
     * Constructor
     *  
     */
    public MockExternalUserImpl(String login, String fn, String ln, String domain) {
        super();
        this.login = login;
        this.fn = fn;
        this.ln = ln;
        this.domain = domain;
        this.uniqueName = login + "@" + domain;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLogin()
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getFirstName()
     */
    public String getFirstName() {
        return this.fn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLastName()
     */
    public String getLastName() {
        return this.ln;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getUniqueName()
     */
    public String getUniqueName() {
        return this.uniqueName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDisplayName()
     */
    public String getDisplayName() {
        return this.uniqueName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.domain;
    }
}