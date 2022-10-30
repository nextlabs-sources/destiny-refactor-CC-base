/*
 * Created on May 31, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;

/**
 * Application user implementation which has been created within Destiny
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/InternalApplicationUserDO.java#4 $
 */

public class InternalApplicationUserDO extends BaseApplicationUserDO {

    /** the password of user */
    private byte[] password;

    /**
     * Create an instance of InternalApplicationUserDO
     * 
     * @param login
     * @param firstName
     * @param lastName
     * @param accessDomain
     */
    InternalApplicationUserDO(String login, String firstName, String lastName, String password, AccessDomainDO accessDomain) {
        super(login, firstName, lastName, accessDomain);

        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }

        // Need to MD5 the password
        setPassword(password);
    }

    /**
     * Create an instance of InternalApplicationUserDO. For Hibernate User Only
     */
    InternalApplicationUserDO() {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.BaseApplicationUserDO#isManuallyCreated()
     */
    public boolean isManuallyCreated() {
        return true;
    }

    /**
     * access password attribute of user DO. For Hibernate use only
     * 
     * @return the password of user
     */
    byte[] getPassword() {
        return this.password;
    }

    /**
     * set password attribute of user DO. For Hibernate User Only
     * 
     * @param the
     *            password of user
     */
    void setPassword(byte[] password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }
        this.password = password;
    }

    /**
     * Set a new password
     * 
     * @param password
     *            the password to set
     */
    void setPassword(String password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }
        this.password = DigestUtils.md5(password);
    }

    /**
     * Determine if the provided password is valid
     * 
     * @param password
     *            the passowrd to check
     * @return true if the passoword is valid; false otherwise
     */
    boolean isPasswordValid(String password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }

        return Arrays.equals(this.password, DigestUtils.md5(password));
    }
}
