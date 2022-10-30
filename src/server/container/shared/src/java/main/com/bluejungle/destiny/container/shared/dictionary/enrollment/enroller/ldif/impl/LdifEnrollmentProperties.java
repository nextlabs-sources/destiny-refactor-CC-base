/*
 * Created on Mar 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl;


import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties ;

/**
 * @author atian 
 * @version $Id:
 */

public interface LdifEnrollmentProperties extends BasicLDAPEnrollmentProperties {

    /*
     * Property names:
     */
    String UPLOAD_PATH_PROPERTY = "ldif.upload.path";
    String LDIF_NAME_PROPERTY = "ldif.filename";
    
    String GROUP_MEMBER_FROM_ALL_ENROLLMENT = "group.isMemberFromAllEnrollment".toLowerCase();
    
    String MEMBER_ATTRIBUTE_KEY = "group.memberAttributeKey".toLowerCase();

}
