/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.clientinfo;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/dictionary/enrollment/enroller/clientinfo/ClientInfoEnroller.java#1 $
 */

public class ClientInfoEnroller extends EnrollerBase {
	private static final Log LOG = LogFactory.getLog(ClientInfoEnroller.class);
	
	private static final int DEFAULT_CLIENT_BATCH_SIZE = 50;

	// try to keep this number under 200, 
	// in my test env, performance are degraded if number is too big.
	private static final int DEFAULT_USER_BATCH_SIZE = 80;

	protected static final String CLIENT_INFO_FILE_KEY = "clientinfo.filepath";	

	private ClientInfoEnrollment clientInfoEnrollment = null;
	
	public void process(
			IEnrollment enrollment, 
			Map<String, String[]> properties,
			IDictionary dictionary) 
	throws EnrollmentValidationException, DictionaryException {
	    super.process(enrollment, properties, dictionary);
		// Properties related to Active Directory:
		String clientInfoFilePath = properties.get(CLIENT_INFO_FILE_KEY)[0];

		if ((clientInfoFilePath == null) || (clientInfoFilePath.length() == 0)) {
			throw new EnrollmentValidationException("clientInfo file upload path is not provided");
		}

		if (!(new File(clientInfoFilePath)).exists()) {
			throw new EnrollmentValidationException("clientInfo file " + clientInfoFilePath
					+ " does not exist");
		}
		enrollment.setStrProperty(CLIENT_INFO_FILE_KEY, clientInfoFilePath);
		
	}

    public String getEnrollmentType() {
        return "client-info";
    }

    @Override
    protected Log getLog() {
        return LOG;
    }

    @Override
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        clientInfoEnrollment = new ClientInfoEnrollment(
                session, 
                dictionary, 
                enrollment, 
                true, //preCacheAllUsers
                DEFAULT_CLIENT_BATCH_SIZE, //clientFetchSize
                DEFAULT_USER_BATCH_SIZE); //userFetchSize
    }

    public void internalSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession enrollmentSession, SyncResult syncResult) throws EnrollmentSyncException {	
        String message;
        try{
            clientInfoEnrollment.startEnrollment();
            message = clientInfoEnrollment.getWarningMessage();
        } finally {
            if(clientInfoEnrollment != null){
                clientInfoEnrollment.close();
            }
        }
        
        // client info enrollment is either all ok or all fail
        // so the total count must equals new count
        // and there is no fail neither ignored
        // and client-info doesn't allow delete elements
        syncResult.newCount = clientInfoEnrollment.getTotalCount();
        syncResult.totalCount = syncResult.newCount;
        syncResult.changeCount = 0;
        syncResult.deleteCount = 0;
        syncResult.ignoreCount = 0;
        syncResult.failedCount = 0;
        
        syncResult.success = true;
        syncResult.message = message;
	}
}
