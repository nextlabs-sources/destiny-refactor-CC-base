/*
 * Created on Feb 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;


/**
 * This interface represents a strongly-typed enrollment configuration for an Active Directory
 * enrollment.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/IADEnrollmentWrapper.java#1 $
 */

public interface IADEnrollmentWrapper extends ILDAPEnrollmentWrapper {

    /*
     * Connectivity properties:
     */
    String getServer();
    int getPort();
    String getLogin();
    String getPassword();
    String getDomainName();
    
    /*
     * Meta-configuration
     */
    String[] getSubtreesToEnroll();
    String[] getAllAttributesToRetrieve();
    String getFilter();

    String getParentGUIDAttributeName();
    String getIsDeletedAttributeName();
    String getLastKnownParentAttributeName();
    String getSecureTransportMode();
    /*
     * Dynamic data:
     */
    byte[] getCookie();
    void setCookie(final byte[] cookie);
    boolean isDirSyncEnabled();
    
}