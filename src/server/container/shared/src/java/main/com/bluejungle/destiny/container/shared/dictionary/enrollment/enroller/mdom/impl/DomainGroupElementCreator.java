/*
* Created on Aug 27, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/impl/DomainGroupElementCreator.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.IDomainGroupEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author dwashburn
 *
 */
public class DomainGroupElementCreator {

    @SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(DomainGroupElementCreator.class);

    private final IDomainGroupEnrollmentWrapper enrollmentWrapper;
    @SuppressWarnings("unused")
	private final IEnrollment enrollment;
    
    public DomainGroupElementCreator(IDomainGroupEnrollmentWrapper enrollmentWrapper) throws DictionaryException {
        this.enrollmentWrapper = enrollmentWrapper;
        this.enrollment = this.enrollmentWrapper.getEnrollment(); 	
    }
   
    
}
