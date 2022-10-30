/*
* Created on Aug 16, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/DomainGroupEnroller.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl.DomainGroupElementCreator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl.DomainGroupEnrollmentInitializer;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl.DomainGroupEnrollmentWrapperImpl;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;

/**
 * @author dwashburn
 *
 */
public class DomainGroupEnroller extends EnrollerBase {
	
    private static final Log LOG = LogFactory.getLog(DomainGroupEnroller.class.getName());
    
    private DomainGroupEnrollmentWrapperImpl wrapper;
    
	@Override
	public String getEnrollmentType() {
		return "Multi-Domain";
	}
	@Override
	protected Log getLog() {
		return LOG;
	}
	
	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase#process(com.bluejungle.dictionary.IEnrollment, java.util.Map, com.bluejungle.dictionary.IDictionary)
	 */
	@Override
	public void process(IEnrollment enrollment, 
						 Map<String, String[]> properties, 
						 IDictionary dictionary)
			throws EnrollmentValidationException, DictionaryException, NullPointerException {
		
		super.process(enrollment, properties, dictionary);
		new DomainGroupEnrollmentInitializer(enrollment, properties, dictionary).setup();

	}

	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase#preSync(com.bluejungle.dictionary.IEnrollment, com.bluejungle.dictionary.IDictionary, com.bluejungle.dictionary.IEnrollmentSession)
	 */
	@Override
	protected void preSync(IEnrollment enrollment, IDictionary dictionary,
			IEnrollmentSession session) throws EnrollmentValidationException,
			DictionaryException {
		wrapper = new DomainGroupEnrollmentWrapperImpl(enrollment, dictionary);
		
	}

	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase#internalSync(com.bluejungle.dictionary.IEnrollment, com.bluejungle.dictionary.IDictionary, com.bluejungle.dictionary.IEnrollmentSession, com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase.SyncResult)
	 */
	@Override
	protected void internalSync(IEnrollment enrollment, IDictionary dictionary,
			IEnrollmentSession session, SyncResult syncResult)
			throws EnrollmentSyncException {
		// DomainGroup doesn't track changes, as it doesn't have any users/groups, etc.
		syncResult.newCount = 0;
		syncResult.deleteCount = 0; 
		syncResult.failedCount = 0;
		syncResult.ignoreCount = 0;
		syncResult.changeCount = 0;
		syncResult.nochangeCount = 0;
		syncResult.totalCount = 0;
			
        syncResult.success = true;
		
	}
    // override this to provide different implementation
    protected DomainGroupElementCreator getMultiDomainElementCreator(
            IDomainGroupEnrollmentWrapper wrapper) throws DictionaryException {
        return new DomainGroupElementCreator(wrapper);
    }
    
}
