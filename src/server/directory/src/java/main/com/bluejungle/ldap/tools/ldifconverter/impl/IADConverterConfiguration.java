/*
 * Created on Apr 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.ldifconverter.impl;

import java.util.Map;

import com.bluejungle.ldap.tools.ldifconverter.IConverterConfiguration;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/directory/src/java/main/com/bluejungle/ldap/tools/ldifconverter/impl/IADConverterConfiguration.java#1 $
 */

public interface IADConverterConfiguration extends IConverterConfiguration {

    /**
     * @see com.bluejungle.ldap.tools.ldifconverter.IConverterConfiguration#getAttributesWithDNValues()
     */
    public abstract String[] getAttributesWithDNValues();

    /**
     * Extracts the AD->OpenLDAP mappings for user entries
     * 
     * @return optional attribute mappings
     */
    public abstract Map getOptionalUserAttributes();

    /**
     * Extracts the AD->OpenLDAP mappings for computer entries
     * 
     * @return optional attribute mappings
     */
    public abstract Map getOptionalComputerAttributeMappings();

    /**
     * Extracts the AD->OpenLDAP mappings for struct entries
     * 
     * @return optional attribute mappings
     */
    public abstract Map getOptionalStructuralGroupAttributeMappings();

    /**
     * Extracts the AD->OpenLDAP mappings for group entries
     * 
     * @return optional attribute mappings
     */
    public abstract Map getOptionalEnumeratedGroupAttributeMappings();

    /**
     * Extracts the AD->OpenLDAP mappings for default (all other) entries
     * 
     * @return optional attribute mappings
     */
    public abstract Map getOptionalDefaultAttributeMappings();
}