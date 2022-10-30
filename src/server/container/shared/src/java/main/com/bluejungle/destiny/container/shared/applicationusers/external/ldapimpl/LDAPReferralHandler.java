/*
 * Created on Jul 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import com.novell.ldap.LDAPBindHandler;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPReferralException;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPReferralHandler.java#1 $
 */

public class LDAPReferralHandler implements LDAPBindHandler {

    /**
     * @see com.novell.ldap.LDAPBindHandler#bind(java.lang.String[], com.novell.ldap.LDAPConnection)
     */
    public LDAPConnection bind(String[] arg0, LDAPConnection arg1) throws LDAPReferralException {
        return null;
    }

}
