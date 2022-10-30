/*
 * Created on Jan 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointEnrollmentProperties.java#1 $
 */

public interface SharePointEnrollmentProperties extends BasicLDAPEnrollmentProperties {
    /*
     * Property names:
     */
    String PASSWORD_PROPERTY = "password";
    String LOGIN_PROPERTY = "login";
    String DOMAIN_PROPERTY = "domain";
    String PORTALS_PROPERTY = "portals";
    
}
