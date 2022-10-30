/*
 * Created on Oct 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Arrays;
import java.util.Collection;

import com.novell.ldap.LDAPBindHandler;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPReferralException;

/**
 * Authentication handler for referrals
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPAuthHandlerImpl.java#1 $
 */

public class LDAPBindHandlerImpl implements LDAPBindHandler {

    /**
     * Constructor
     *  
     */
    public LDAPBindHandlerImpl(String userDN, String password) {
        super();
    }

    /**
     * @see com.novell.ldap.LDAPBindHandler#bind(java.lang.String[],
     *      com.novell.ldap.LDAPConnection)
     */
    public LDAPConnection bind(String[] urlArray, LDAPConnection arg1) throws LDAPReferralException {
        Collection urlList = Arrays.asList(urlArray);
        throw new LDAPReferralException("Referrals not supported. Referral: '" + urlList + "' was ignored.");
    }
}