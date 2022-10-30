/*
 * Created on Sep 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPEnumeratedGroupImpl.java#1 $
 */

public class LDAPEnumeratedGroupImpl extends LDAPAbstractExternalGroupImpl implements ILDAPEnumeratedGroup {

    protected ILDAPEnumeratedGroupConfiguration groupConfiguration;

    /**
     * Constructor
     *  
     */
    public LDAPEnumeratedGroupImpl(LDAPEntry backingEntry, String domainName, ILDAPAccessProviderConfiguration configuration, ILDAPEnumeratedGroupConfiguration groupConfig) throws InvalidEntryException {
        super(backingEntry, domainName, configuration);
        this.groupConfiguration = groupConfig;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getTitle()
     */
    public String getTitle() {
        return this.backingEntry.getAttribute(this.groupConfiguration.getTitleAttribute()).getStringValue();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl.ILDAPEnumeratedGroup#getMemberDNs()
     */
    public String[] getMemberDNs() {
        LDAPAttribute membersAttr = this.backingEntry.getAttribute(this.groupConfiguration.getMembershipAttribute());
        String[] memberDNs;
        if (membersAttr != null) {
            memberDNs = membersAttr.getStringValueArray();
        } else {
            memberDNs = new String[0]; // An empty array
        }
        return memberDNs;
    }
}