/*
 * Created on Mar 19, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentInitializer;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointEnrollmentInitalizer.java#1 $
 */

public class SharePointEnrollmentInitalizer extends BaseLDAPEnrollmentInitializer implements
		SharePointEnrollmentProperties {
    
	public SharePointEnrollmentInitalizer(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) {
        super(enrollment, properties, dictionary, LogFactory.getLog(SharePointEnrollmentInitalizer.class));
    }

    /**
     * This method sets up the enrollment record with the provided enrollment
     * properties. It determines the appropriate types of the properties and
     * sets them on the enrollment record according.
     * 
     * @param enrollmentToSetup
     * @param properties
     */
    public void setup() throws EnrollmentValidationException {
    	super.setupAutoEnrollment();
    		
    	String login = setStringProperty(LOGIN_PROPERTY);
        String password = setStringProperty(PASSWORD_PROPERTY);
        String domain = setStringProperty(DOMAIN_PROPERTY, enrollment.getDomainName());

    	enrollment.setStrProperty(SharePointEnrollmentProperties.DOMAIN_PROPERTY, domain);

		String[] portalsToEnroll = properties.get(SharePointEnrollmentProperties.PORTALS_PROPERTY);
		if (portalsToEnroll != null) {

			for (int i = 0; i < portalsToEnroll.length; i++) {
				// Test the connectivity:
				portalsToEnroll[i] = portalsToEnroll[i].trim();
				if (!portalsToEnroll[i].endsWith("/")) {
					portalsToEnroll[i] += "/";
				}
				SharePointConnectionTester.testConnection(portalsToEnroll[i], login, password, domain);
			}

			enrollment.setStrArrayProperty(SharePointEnrollmentProperties.PORTALS_PROPERTY,
					portalsToEnroll);
		}
	}
}
