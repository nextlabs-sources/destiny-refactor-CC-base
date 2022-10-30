/*
 * Created on Mar 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;

/**
 * @author safdar
 * @version $Id:
 */

public interface ActiveDirectoryEnrollmentProperties extends BasicLDAPEnrollmentProperties {

    /*
     * Property names:
     */
    String SERVER                   = "server";
    String PORT                     = "port";
    String SECURE_TRANSPORT_MODE    = "secure.transport.mode"; // One of SSL or TLS
    String ALWAYS_TRUST_AD          = "always.trust.ad";       // Require AD cert in truststore?
    String PASSWORD                 = "password";
    String LOGIN                    = "login";
    String FILTER                   = "filter";
    String ROOTS                    = "roots";
    String PARENT_ID_ATTRIBUTE      = "entry.attributefor.parentid";
    String LAST_PARENT_ATTRIBUTE    = "entry.attributefor.lastknownparent";
    String IS_DELETED_ATTRIBUTE     = "entry.attributefor.isdeleted";
    String DIRSYNC_ENABLED          = "EnableADDirChgReplication".toLowerCase();
    String PAGING_ENABLED           = "IsPagingEnabled".toLowerCase();
    
    String COOKIE                   = "cookie";
}
