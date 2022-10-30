/*
 * Created on Jan 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.ISharePointEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointEnrollmentWrapperImpl.java#1 $
 */

public class SharePointEnrollmentWrapperImpl extends BaseLDAPEnrollmentWrapper implements ISharePointEnrollmentWrapper {
    private final String login;
    private final String password;
    private final String domain;
    private final String[] portals;
    
    /**
     * Constructor
     * @throws DictionaryException 
     */
    public SharePointEnrollmentWrapperImpl(IEnrollment enrollment, IDictionary dictionary)
			throws EnrollmentValidationException, DictionaryException {
        super(enrollment, dictionary);
        
        login = enrollment.getStrProperty(SharePointEnrollmentProperties.LOGIN_PROPERTY);
        password = enrollment.getStrProperty(SharePointEnrollmentProperties.PASSWORD_PROPERTY);
        domain = enrollment.getStrProperty(SharePointEnrollmentProperties.DOMAIN_PROPERTY);
        portals = enrollment.getStrArrayProperty(SharePointEnrollmentProperties.PORTALS_PROPERTY);
    }


    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.ISharePointEnrollmentWrapper#getDomainName()
     */
    public String getDomainName() {
        return this.enrollment.getDomainName();
    }
    
    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public String[] getPortals() {
        return portals;
    }

    public IEnrollment getEnrollment() {
       return super.getEnrollment();
    }
}
