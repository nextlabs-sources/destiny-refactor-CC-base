/*
 * Created on Sep 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;
/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/ILDAPStructuralGroupConfiguration.java#1 $
 */

/**
 * This interface represents configuration for a given structural group
 * search spec
 * 
 * @author safdar
 */
public interface ILDAPStructuralGroupConfiguration {

    /**
     * Search spec for the particular "type" of structual group represented
     * by this configuration object
     * 
     * @return search spec
     */
    public String getSearchSpec();

    /**
     * Title attribute for the particular "type" of structural group
     * represented by this configuration object.
     * 
     * @return title attribute
     */
    public String getTitleAttribute();
}