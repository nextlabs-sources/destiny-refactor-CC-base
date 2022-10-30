/*
* Created on Aug 27, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/impl/DomainGroupEnrollmentInitializer.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentInitializer;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author dwashburn
 *
 */
public class DomainGroupEnrollmentInitializer extends	BaseLDAPEnrollmentInitializer implements
		DomainGroupEnrollmentProperties {
	   
	public DomainGroupEnrollmentInitializer(IEnrollment enrollment,
			Map<String, String[]> properties, IDictionary dictionary) {
        super(enrollment, properties, dictionary, LogFactory.getLog(DomainGroupEnrollmentInitializer.class));

	}
	
    /**
     * This method sets up the enrollment record with the provided enrollment
     * properties. It determines the appropriate types of the properties and
     * sets them on the enrollment record accordingly.
     * 
     * @param enrollmentToSetup
     * @param properties
     */
    public void setup() throws EnrollmentValidationException {
    	super.setupAutoEnrollment();
    	
    	// Setup global definition, and filter filename properties.
    	setStringProperty(DomainGroupEnrollmentProperties.DEFINITION_FILENAME_PROPERTY, null);
    	setStringProperty(DomainGroupEnrollmentProperties.FILTER_FILENAME_PROPERTY,     null);

		int subDomainCount = 0;
        while (true) {
        	String subdomainPropPrefix = DomainGroupEnrollmentProperties.SUBDOMAIN_PREFIX + subDomainCount;
        	String thisSubdomainTypeProp = subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_TYPE;
            String typeValue = getSingleValuedPropertyValue(thisSubdomainTypeProp, false, null, false);
        	if (typeValue == null) 
        		break;     	        	
        	
        	String thisSubdomainNameProp = subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_NAME;
            String nameValue = getSingleValuedPropertyValue(thisSubdomainNameProp, false, null, false);
        	if (nameValue == null) 
        		break;
        	
        	// Set subdomain.N.type and subdomain.N.name properties in enrollment record
            setStringProperty(thisSubdomainTypeProp);
         	setStringProperty(thisSubdomainNameProp);
        	       	
        	// Set subdomain.N.connectionfile, definitionfile, and filterfile properties in enrollment record
        	setStringProperty(subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_CONNECTION_FILENAME_PROPERTY, null);      	
        	setStringProperty(subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_DEFINITION_FILENAME_PROPERTY, null);      	
        	setStringProperty(subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_FILTER_FILENAME_PROPERTY, null);
       	
        	subDomainCount++;      	
        }

    	
    }
}
