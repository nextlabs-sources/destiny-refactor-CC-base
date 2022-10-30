/*
 * Created on May 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2006 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author atian
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/ApplicationUserDO.java#1 $
 */

public abstract class BaseApplicationUserDO implements IApplicationUser {

    private static final String UNIQUE_NAME_SEPERATOR = "@";;

    /** the ID of user */
    private Long id;

    /** the first name of user */
    private String firstName;

    /** the last name of user */
    private String lastName;

    /** the login name of user */
    private String login;

    /** The domain in which this group is contained * */
    private AccessDomainDO accessDomain;

    /**
     * The primary access group for this user
     */
    private IAccessGroup primaryAccessGroup;
    
    /** status of the user - ACTIVE/ DELETED */
    private String status;
    
    /** record version */
    private Integer version;

    /**
     * Create an instance of BaseApplicationUserDO
     * 
     * @param login
     * @param firstName
     * @param lastName
     * @param password
     * @param accessDomain
     */
    BaseApplicationUserDO(String login, String firstName, String lastName, AccessDomainDO accessDomain) {
        if (login == null) {
            throw new NullPointerException("login cannot be null.");
        }

        if (firstName == null) {
            throw new NullPointerException("firstName cannot be null.");
        }

        if (lastName == null) {
            throw new NullPointerException("lastNamepassword cannot be null.");
        }

        if (accessDomain == null) {
            throw new NullPointerException("accessDomain cannot be null.");
        }

        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accessDomain = accessDomain;
    }

    /**
     * Create an instance of BaseApplicationUserDO. For Hibernate Use Only
     */
    BaseApplicationUserDO() {
        super();
    }

    /**
     * access id attribute of user DO
     * 
     * @return the id of user
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getDestinyId()
     */
    public Long getDestinyId() {
        return this.id;
    }

    /**
     * 
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getFirstName()
     */
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null) {
            throw new NullPointerException("firstName cannot be null.");
        }

        this.firstName = firstName;
    }

    /**
     * access lastName attribute of user DO
     * 
     * @return the lastName of user
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * set lastName attribute of user DO
     * 
     * @param the
     *            lastName of user
     */
    public void setLastName(String lastName) {
        if (lastName == null) {
            throw new NullPointerException("lastName cannot be null.");
        }
        this.lastName = lastName;
    }

    /**
     * access loginName attribute of user DO
     * 
     * @return the loginName of user
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.accessDomain.getName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDisplayName()
     */
    public String getDisplayName() {
        StringBuffer displayNameBuffer = new StringBuffer(this.getLastName());
        displayNameBuffer.append(", ");
        displayNameBuffer.append(this.getFirstName());

        return displayNameBuffer.toString();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getUniqueName()
     */
    public String getUniqueName() {
        StringBuffer uniqueNameBuffer = new StringBuffer(this.getLogin());
        uniqueNameBuffer.append(UNIQUE_NAME_SEPERATOR);
        uniqueNameBuffer.append(this.getDomainName());

        return uniqueNameBuffer.toString();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#isManuallyCreated()
     */
    public boolean isManuallyCreated() {
        return false;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object otherUser) {
        boolean valueToReturn = false;
        if ((otherUser != null) && (otherUser instanceof BaseApplicationUserDO)) {
            // Hibernate recommends not using ID, but I feel that it's the best
            // way to test equality. The reasons they provide are valid, but the
            // trade off of using the fields for equality is not worth it
            valueToReturn = this.getId().equals(((BaseApplicationUserDO) otherUser).getId());
        }
        return valueToReturn;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * set id attribute of user DO. Required by Hibernate
     * 
     * @param the
     *            id of user
     */
    void setId(Long id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }

        this.id = id;
    }

    /**
     * set loginName attribute of user DO. Required by Hibernate
     * 
     * @param the
     *            loginName of user
     */
    void setLogin(String loginName) {
        if (loginName == null) {
            throw new NullPointerException("loginName cannot be null.");
        }
        this.login = loginName;
    }

    /**
     * Retrieve the accessDomain. Required for Hibernate
     * 
     * @return the accessDomain.
     */
    AccessDomainDO getAccessDomain() {
        return this.accessDomain;
    }

    /**
     * Set the accessDomain. Required for Hibernate
     * 
     * @param accessDomain
     *            The accessDomain to set.
     */
    void setAccessDomain(AccessDomainDO accessDomain) {
        if (accessDomain == null) {
            throw new NullPointerException("accessDomain cannot be null.");
        }
        this.accessDomain = accessDomain;
    }

    /**
     * Set the primary access group. Required for Hibernate. May be null
     * 
     * @param primaryAccessGroup
     *            the access group to set
     */
    void setHibernatePrimaryAccessGroup(IAccessGroup primaryAccessGroup) {
        this.primaryAccessGroup = primaryAccessGroup;
    }

    /**
     * Set the primary access group. Required for Hibernate. Having a different
     * setter allows for the "hasPrimaryAccessGroup" behavior and avoids
     * returning null to clients, making the code more robust
     * 
     * @return The primary access group for this user; null if it doesn't have
     *         one
     * 
     */
    IAccessGroup getHibernatePrimaryAccessGroup() {
        return this.primaryAccessGroup;
    }

    /**
     * Clear the primary access group setting. Results in this user not having a
     * primary access group
     * 
     */
    void clearPrimaryAccessGroup() {
        this.primaryAccessGroup = null;
    }

    /**
     * Set the primaryAccessGroup
     * 
     * @param primaryAccessGroup
     *            The primaryAccessGroup to set.
     */
    void setPrimaryAccessGroup(IAccessGroup primaryAccessGroup) {
        if (primaryAccessGroup == null) {
            throw new NullPointerException("primaryAccessGroup cannot be null.");
        }
        this.primaryAccessGroup = primaryAccessGroup;
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getPrimaryAccessGroup()
     */
    IAccessGroup getPrimaryAccessGroup() {
        if (this.primaryAccessGroup == null) {
            throw new IllegalStateException("This user does not have a primary access group");
        }
        return this.primaryAccessGroup;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#hasPrimaryAccessGroup()
     */
    boolean hasPrimaryAccessGroup() {
        return (this.primaryAccessGroup != null);
    }

	/**
	 * get status attribute of user DO
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * set status attribute of user DO
	 */
	public void setStatus(String status) {
		this.status = (status == null) ? "ACTIVE" : status;

	}
	
	/**
	 * get version of the user record
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * set version of the user record
	 */
	public void setVersion(Integer version) {
		this.version = (version == null) ? 0 : version;

	}

}
