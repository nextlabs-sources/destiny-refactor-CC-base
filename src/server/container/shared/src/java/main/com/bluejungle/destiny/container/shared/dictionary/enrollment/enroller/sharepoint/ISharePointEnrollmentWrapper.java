/*
 * Created on Jan 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollmentWrapper;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/ISharePointEnrollmentWrapper.java#1 $
 */

public interface ISharePointEnrollmentWrapper extends IEnrollmentWrapper{
    
    String getDomainName();

    String getLogin();

    String getPassword();
    
    String getDomain();

    String[] getPortals();
}
