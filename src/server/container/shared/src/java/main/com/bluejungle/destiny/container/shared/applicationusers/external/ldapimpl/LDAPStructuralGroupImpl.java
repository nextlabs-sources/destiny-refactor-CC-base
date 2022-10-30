/*
 * Created on Sep 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import com.novell.ldap.LDAPEntry;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPStructuralGroupImpl.java#1 $
 */

public class LDAPStructuralGroupImpl extends LDAPAbstractExternalGroupImpl implements ILDAPStructuralGroup {

    protected ILDAPStructuralGroupConfiguration groupConfiguration;

    /**
     * Constructor
     *  
     */
    public LDAPStructuralGroupImpl(LDAPEntry backingEntry, String domainName, ILDAPAccessProviderConfiguration configuration, ILDAPStructuralGroupConfiguration groupConfig) throws InvalidEntryException {
        super(backingEntry, domainName, configuration);
        this.groupConfiguration = groupConfig;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup#getTitle()
     */
    public String getTitle() {
        return this.backingEntry.getAttribute(this.groupConfiguration.getTitleAttribute()).getStringValue();
    }
}