/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPExternalUserImpl.java#3 $
 */

public class LDAPExternalUserImpl extends LDAPAbstractEntryImpl implements ILDAPExternalUser {
	//TODO load this from bundle
    private static final String DEFAULT_LAST_NAME = "";
	private static final String DEFAULT_FIRST_NAME = "";

	/*
     * Private variables:
     */
    private ILDAPAccessProviderConfiguration configuration;
    
    //requried fields
    private String domainName;
    private String login;
    private String uniqueName;
    private String displayName;
    
    //optional fields
    private String firstName;
    private String lastName;

    /**
     * Constructor
     * 
     * @param domainName
     * @param backingEntry
     */
    public LDAPExternalUserImpl(String domainName, LDAPEntry backingEntry,
			ILDAPAccessProviderConfiguration configuration) throws InvalidEntryException {
		super(backingEntry);

		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		if (domainName == null) {
			throw new NullPointerException("domain name is null");
		}
		if (backingEntry == null) {
			throw new NullPointerException("backing entry is null");
		}

		this.configuration = configuration;
		this.domainName = domainName;
		this.backingEntry = backingEntry;

		// Read the login name:
		try {
			String loginAttrName =  this.configuration.getUserLoginAttribute();
			login = getRequiredAttribute(loginAttrName).getStringValue();

			LDAPAttribute attributeToReturn;

			// Read the first name:
			String fnAttrName =  this.configuration.getUserFirstNameAttribute();
			attributeToReturn =  this.backingEntry.getAttribute(fnAttrName);
			firstName = attributeToReturn != null
					? attributeToReturn.getStringValue()
					: DEFAULT_FIRST_NAME;

			// Read the last name:
			String lnAttrName = this.configuration.getUserLastNameAttribute();
			attributeToReturn =  this.backingEntry.getAttribute(lnAttrName);
			lastName = attributeToReturn != null
					? attributeToReturn.getStringValue()
					: DEFAULT_LAST_NAME;

		} catch (MissingAttributeException e) {
			throw new InvalidEntryException(e);
		}

		uniqueName = login + "@" + domainName;
		displayName = lastName + ", " + firstName;
	}

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getDomainName()
     */
    public String getDomainName() {
        return this.domainName;
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
        return this.firstName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUser#getLastName()
     */
    public String getLastName() {
        return this.lastName;
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
        return this.displayName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPExternalUser#getDN()
     */
    public String getDN() {
        return this.backingEntry.getDN();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o instanceof IExternalUser) {
            IExternalUser rhs = (IExternalUser) o;
            if (getUniqueName().equals(rhs.getUniqueName())) {
                isEqual = true;
            }
        }
        return isEqual;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getUniqueName().hashCode();
    }
}