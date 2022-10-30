/*
* Created on Aug 27, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/impl/DomainGroupEnrollmentWrapperImpl.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl;

import java.util.Vector;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.IDomainGroupEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author dwashburn
 *
 */
public class DomainGroupEnrollmentWrapperImpl 
			extends BaseLDAPEnrollmentWrapper
			implements IDomainGroupEnrollmentWrapper {
	
	private final String connectionFilename;
	private final String definitionFilename;
	private final String filterFilename;
	private final Vector<String> subDomains = new Vector<String>();

	/**
	 * @param enrollment
	 * @param dictionary
	 * @param databaseBatchSize
	 * @throws DictionaryException
	 */
	public DomainGroupEnrollmentWrapperImpl(IEnrollment enrollment, IDictionary dictionary)
			throws EnrollmentValidationException, DictionaryException {
		super(enrollment, dictionary);
		
		connectionFilename = enrollment.getStrProperty(DomainGroupEnrollmentProperties.CONNECTION_FILENAME_PROPERTY);
		definitionFilename = enrollment.getStrProperty(DomainGroupEnrollmentProperties.DEFINITION_FILENAME_PROPERTY);
		filterFilename = enrollment.getStrProperty(DomainGroupEnrollmentProperties.FILTER_FILENAME_PROPERTY);
        int subDomainCount = 0;
        while (true) {
        	String subdomainPropPrefix = DomainGroupEnrollmentProperties.SUBDOMAIN_PREFIX + subDomainCount;
        	
        	String thisSubdomainNameProp = subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_NAME;
        	String thisSubdomainName = enrollment.getStrProperty(thisSubdomainNameProp);
        	if (thisSubdomainName == null) 
        		break;
        	subDomains.add(thisSubdomainName);
        	subDomainCount++;      	
        }

	}

	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.IMultiDomainEnrollmentWrapper#getDomainName()
	 */
	@Override
	public String getDomainName() {
        return this.enrollment.getDomainName();
	}
	
	@Override
	public String getConnectionFilename() {
		return connectionFilename;
	}

	@Override
	public String getDefinitionFilename() {
		return definitionFilename;
	}

	@Override
	public String getFilterFilename() {
		return filterFilename;
	}

	@Override
	public String[] getSubDomains() {
		return subDomains.toArray(new String[subDomains.size()]);
	}
	
}
