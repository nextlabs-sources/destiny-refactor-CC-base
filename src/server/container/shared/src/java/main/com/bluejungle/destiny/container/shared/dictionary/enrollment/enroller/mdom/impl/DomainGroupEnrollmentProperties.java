/*
* Created on Aug 21, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/impl/DomainGroupEnrollmentProperties.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;

/**
 * @author dwashburn
 *
 */
public interface DomainGroupEnrollmentProperties extends BasicLDAPEnrollmentProperties {
	
    /*
     * global default files for sub-directories
     */
    String CONNECTION_FILENAME_PROPERTY = "connection.filename";
    String DEFINITION_FILENAME_PROPERTY = "definition.filename";
    String FILTER_FILENAME_PROPERTY = "filter.filename";
    
    // for definition of sub-domains to enroll.
    // Sample config entry:
    // subdomain.0.type=DIR
    // subdomain.0.name=mydomain.foobar.com
    // subdomain.0.connection.filename=c:\tmp\mydomain.conn.txt
    // subdomain.0.definition.filename=c:\tmp\mydomain.def.txt
    // subdomain.0.filter.filename=C:\tmp\myfilters.txt

    String SUBDOMAIN_PREFIX = "subdomain.";
    String SUBDOMAIN_TYPE = ".type";
    String SUBDOMAIN_NAME = ".name";
    // Optional file-Paths/args to use instead of global files.
    
    String SUBDOMAIN_CONNECTION_FILENAME_PROPERTY = ".connection.filename";
    String SUBDOMAIN_DEFINITION_FILENAME_PROPERTY = ".definition.filename";
    String SUBDOMAIN_FILTER_FILENAME_PROPERTY = ".filter.filename";
    
}
