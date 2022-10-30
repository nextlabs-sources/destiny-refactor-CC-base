/*
 * Created on Oct 12, 2005
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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPAbstractEntryImpl.java#1 $
 */

public class LDAPAbstractEntryImpl {

    protected LDAPEntry backingEntry;

    /**
     * Constructor
     *  
     */
    public LDAPAbstractEntryImpl(LDAPEntry backingEntry) {
        super();
        this.backingEntry = backingEntry;
    }

    public LDAPAttribute getRequiredAttribute(String name) throws MissingAttributeException {
        LDAPAttribute attributeToReturn = this.backingEntry.getAttribute(name);
        if (attributeToReturn == null) {
            throw new MissingAttributeException("No value exists for attribute: '" + name + "' on entry: '" + this.backingEntry.getDN() + "'");
        }
        return attributeToReturn;
    }
}