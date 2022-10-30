/*
 * Created on Jul 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticationContext;
import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.common.AuthenticatedUserImpl;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.pf.domain.epicenter.common.ISpec;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

/**
 * Hibernate Authentication Domain Implementation
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/HibernateAuthenticationDomainImpl.java#2 $
 */

public class HibernateAuthenticationDomainImpl implements IAuthenticationDomain {

    private AccessDomainDO wrappedDomain;
    private IAuthenticator authenticator;

    /**
     * Create an instance of HibernateAuthenticationDomainImpl
     * 
     * @param applicationUserDomain
     */
    HibernateAuthenticationDomainImpl(AccessDomainDO applicationUserDomain) {
        if (applicationUserDomain == null) {
            throw new NullPointerException("applicationUserDomain cannot be null.");
        }

        this.wrappedDomain = applicationUserDomain;
    }

    /**
     * Create an instance of HibernateAuthenticationDomainImpl
     * 
     * @param domainDO
     * @param authenticatorOverride
     */
    HibernateAuthenticationDomainImpl(AccessDomainDO domain, IAuthenticator authenticatorOverride) {
        if (domain == null) {
            throw new NullPointerException("domainDO Override cannot be null.");
        }
        if (authenticatorOverride == null) {
            throw new NullPointerException("authenticatorOverride cannot be null.");
        }

        this.wrappedDomain = domain;
        this.authenticator = authenticatorOverride;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain#authenticateUser(java.lang.String,
     *      java.lang.String)
     */
    public IAuthenticatedUser authenticateUser(String login, String password) throws AuthenticationFailedException {
        IAuthenticatedUser authenticationUser = null;

        if (this.authenticator != null) {
            authenticationUser = authenticateUserWithAuthenticator(login, password);
        } else {
            authenticationUser = authenticateUserAgainstDatabase(login, password);
        }

        return authenticationUser;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomain#getName()
     */
    public String getName() {
        return this.wrappedDomain.getName();
    }

    /**
     * @param login
     * @param password
     * @param authenticatedUser
     * @return
     * @throws AuthenticationFailedException
     */
    private IAuthenticatedUser authenticateUserAgainstDatabase(String login, String password) throws AuthenticationFailedException {
        IAuthenticatedUser authenticatedUser = null;

        try {
            IApplicationUser applicationUser = this.wrappedDomain.getUser(login);
            if (applicationUser instanceof SuperUserDO) {
                if (!((SuperUserDO) applicationUser).isPasswordValid(password)) {
                    StringBuffer errorMessage = new StringBuffer("Provided incorrect password for Super User.");
                    throw new AuthenticationFailedException(errorMessage.toString());
                }
            } else if (applicationUser instanceof InternalApplicationUserDO) {
                if (!((InternalApplicationUserDO) applicationUser).isPasswordValid(password)) {
                    StringBuffer errorMessage = new StringBuffer("Provided incorrect password for user with login, ");
                    errorMessage.append(login);
                    errorMessage.append(".");
                    throw new AuthenticationFailedException(errorMessage.toString());
                }
            } else {
                StringBuffer errorMessage = new StringBuffer("Unexpected user class type found, ");
                errorMessage.append(applicationUser.getClass());
                errorMessage.append(".");
                throw new AuthenticationFailedException(errorMessage.toString());
            }

            authenticatedUser = new AuthenticatedUserImpl(applicationUser);
        } catch (ApplicationUserRepositoryAccessException exception) {
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve user with login, ");
            errorMessage.append(login);
            errorMessage.append(", from domain, ");
            errorMessage.append(this.getName());
            errorMessage.append(".");
            throw new AuthenticationFailedException(errorMessage.toString(), exception);
        } catch (UserNotFoundException exception) {
            StringBuffer errorMessage = new StringBuffer("User with login, ");
            errorMessage.append(login);
            errorMessage.append(", in domain, ");
            errorMessage.append(this.getName());
            errorMessage.append(", not found.");
            throw new AuthenticationFailedException(errorMessage.toString(), exception);
        }

        return authenticatedUser;
    }

    /**
     * Authenticate user with provided authenticator
     * 
     * @param login
     * @param password
     * @return
     * @throws AuthenticationFailedException
     */
    private IAuthenticatedUser authenticateUserWithAuthenticator(String login, String password) throws AuthenticationFailedException {
        IAuthenticatedUser authenticatedUser = null;

        IAuthenticationContext authenticationContext = this.authenticator.authenticate(login, password);

        try {
            IApplicationUser applicationUser = this.wrappedDomain.getUserIgnoreCase(login);
            authenticatedUser = new AuthenticatedUserImpl(applicationUser, authenticationContext);
        } catch (ApplicationUserRepositoryAccessException exception) {
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve user with login, ");
            errorMessage.append(login);
            errorMessage.append(", from domain, ");
            errorMessage.append(this.getName());
            errorMessage.append(".");
            throw new AuthenticationFailedException(errorMessage.toString(), exception);
        } catch (UserNotFoundException exception) {
            StringBuffer errorMessage = new StringBuffer("User with login, ");
            errorMessage.append(login);
            errorMessage.append(", in domain, ");
            errorMessage.append(this.getName());
            errorMessage.append(", not found.");
            throw new AuthenticationFailedException(errorMessage.toString(), exception);
        }

        return authenticatedUser;
    }
}
