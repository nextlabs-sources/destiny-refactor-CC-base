/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Collection;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/ILDAPAccessProviderConfiguration.java#1 $
 */

public interface ILDAPAccessProviderConfiguration {

    /**
     * Returns the server name
     * 
     * @return server name
     */
    public String getServer();

    /**
     * Returns the server port
     * 
     * @return server port
     */
    public int getPort();

    /**
     * Returns whether we are using ssl
     * 
     * @return whether we are using ssl
     */
    public boolean isUsingSSL();

    /**
     * Returns the login dn
     * 
     * @return login dn
     */
    public String getLoginDN();

    /**
     * Returns the login password
     * 
     * @return login password
     */
    public String getLoginPwd();

    /**
     * Returns the root dn
     * 
     * @return root dn
     */
    public String getRootDN();

    /**
     * Returns the user search spec
     * 
     * @return user search spec
     */
    public String getUserSearchSpec();

    /**
     * Returns the user first name attribute
     * 
     * @return first name attribute
     */
    public String getUserFirstNameAttribute();

    /**
     * Returns the user last name attribute
     * 
     * @return last name attribute
     */
    public String getUserLastNameAttribute();

    /**
     * Returns the user login attribute
     * 
     * @return user login attribute
     */
    public String getUserLoginAttribute();

    /**
     * Returns the structural group configurations
     * 
     * @return collection of structural group configurations
     */
    public Collection<ILDAPStructuralGroupConfiguration> getStructuralGroupConfigurations();

    /**
     * Returns the enumerated group configurations
     * 
     * @return collection of enumerated group configurations
     */
    public Collection<ILDAPEnumeratedGroupConfiguration> getEnumeratedGroupConfigurations();

    /**
     * Returns the attribute for the globally unique identifier for entries
     * 
     * @return attribute for the globally unique identifier
     */
    public String getGloballyUniqueIdentifierAttribute();
}