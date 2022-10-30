/*
 * Created on Sep 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Arrays;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.novell.ldap.LDAPEntry;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPAbstractExternalGroupImpl.java#1 $
 */

public abstract class LDAPAbstractExternalGroupImpl extends LDAPAbstractEntryImpl implements IExternalGroup {

    /*
     * Protected variables:
     */
    protected ILDAPAccessProviderConfiguration configuration;
    protected String domainName;
    protected Byte[] cachedExternalIdAsBytes;
    protected byte[] externalID;

    /**
     * Constructor
     *  
     */
    public LDAPAbstractExternalGroupImpl(LDAPEntry backingEntry, String domainName, ILDAPAccessProviderConfiguration accessConfig) throws InvalidEntryException {
        super(backingEntry);

        if (domainName == null) {
            throw new NullPointerException("domain name is null");
        }
        if (accessConfig == null) {
            throw new NullPointerException("access configuration is null");
        }

        this.configuration = accessConfig;
        this.domainName = domainName;

        // Retrieve the external id:
        try {
            String externalIDAttrName = this.configuration.getGloballyUniqueIdentifierAttribute();
            this.externalID = getRequiredAttribute(externalIDAttrName).getByteValue();
        } catch (MissingAttributeException e) {
            throw new InvalidEntryException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getDistinguishedName()
     */
    public String getDistinguishedName() {
        return this.backingEntry.getDN();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getExternalId()
     */
    public byte[] getExternalId() {
        return this.externalID;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getQualifiedExternalName()
     */
    public String getQualifiedExternalName() {
        return this.backingEntry.getDN();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        boolean isEqual = false;
        if (o instanceof IExternalGroup) {
            IExternalGroup rhs = (IExternalGroup) o;
            if (Arrays.equals(rhs.getExternalId(), getExternalId())) {
                isEqual = true;
            }
        }
        return isEqual;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (this.cachedExternalIdAsBytes == null) {
            // Store the external id:
            byte[] externalId = getExternalId();
            this.cachedExternalIdAsBytes = new Byte[externalId.length];
            for (int i = 0; i < externalId.length; i++) {
                this.cachedExternalIdAsBytes[i] = new Byte(externalId[i]);
            }
        }
        return Arrays.hashCode(this.cachedExternalIdAsBytes);
    }
}