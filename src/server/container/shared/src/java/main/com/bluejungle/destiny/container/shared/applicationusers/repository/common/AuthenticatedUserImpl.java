/*
 * Created on Jul 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.common;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;

import java.util.Date;


/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/common/AuthenticatedUserImpl.java#1 $
 */

public class AuthenticatedUserImpl implements IAuthenticatedUser {
    private static IAuthenticationContext DO_NOTHING_AUTHENTICATION_CONTEXT = new IAuthenticationContext() {

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext#logoff()
         */
        public void logoff() {
        }
    };
    
    private IApplicationUser wrappedUser;
    private IAuthenticationContext authContext;
    private Date loginDate;

    /**
     * Create an instance of AuthenticatedUserImpl without an authentication context
     * @param wrappedUser
     */
    public AuthenticatedUserImpl(IApplicationUser wrappedUser) {
        this(wrappedUser, DO_NOTHING_AUTHENTICATION_CONTEXT);
    }
    
    /**
     * Create an instance of AuthenticatedUserImpl
     * @param wrappedUser
     */
    public AuthenticatedUserImpl(IApplicationUser wrappedUser, IAuthenticationContext authContext) {
        if (wrappedUser == null) {
            throw new NullPointerException("wrappedUser cannot be null.");
        }
        if (authContext == null) {
            throw new NullPointerException("authContext cannot be null.");
        }
        
        this.wrappedUser = wrappedUser;
        this.authContext = authContext;
        this.loginDate = new Date();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser#getLoginDate()
     */
    public Date getLoginDate() {
        return this.loginDate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser#logoff()
     */
    public void logoff() {
        this.authContext.logoff();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getDestinyId()
     */
    public Long getDestinyId() {
        return this.wrappedUser.getDestinyId();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#isManuallyCreated()
     */
    public boolean isManuallyCreated() {
        return this.wrappedUser.isManuallyCreated();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDisplayName()
     */
    public String getDisplayName() {
        return this.wrappedUser.getDisplayName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getFirstName()
     */
    public String getFirstName() {
        return this.wrappedUser.getFirstName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLastName()
     */
    public String getLastName() {
        return this.wrappedUser.getLastName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLogin()
     */
    public String getLogin() {
        return this.wrappedUser.getLogin();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getUniqueName()
     */
    public String getUniqueName() {
        return this.wrappedUser.getUniqueName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.wrappedUser.getDomainName();
    }
}
