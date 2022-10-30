/*
 * Created on Jan 21, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import java.util.Date;

import com.bluejungle.dictionary.IEnrollment;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/IEnrollmentWrapper.java#1 $
 */

public interface IEnrollmentWrapper {
    IEnrollment getEnrollment();
    
    boolean isUpdate();
    
    Date getEnrollmentStartTime();
}
