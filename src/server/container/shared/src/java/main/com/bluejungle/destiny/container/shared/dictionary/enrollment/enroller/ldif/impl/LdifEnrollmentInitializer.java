/*
 * Created on Mar 19, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentInitializer;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ldif/impl/LdifEnrollmentInitializer.java#1 $
 */

public class LdifEnrollmentInitializer extends BaseLDAPEnrollmentInitializer implements
        LdifEnrollmentProperties {
    
    public LdifEnrollmentInitializer(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) {
        super(enrollment, properties, dictionary, LogFactory.getLog(LdifEnrollmentInitializer.class));
    }

    /**
     * This method sets up the enrollment record with the provided enrollment
     * properties. It determines the appropriate types of the properties and
     * sets them on the enrollment record accordingly.
     * 
     * @param enrollmentToSetup
     * @param properties
     * @throws DictionaryException 
     */
    public void setup() throws EnrollmentValidationException, DictionaryException {
        // Setup the common properties shared by all LDAP:
        super.setup();
        
        setStringProperty(GROUP_MEMBER_FROM_ALL_ENROLLMENT, Boolean.FALSE.toString());
        setStringProperty(MEMBER_ATTRIBUTE_KEY, null);
        
        setReadableFileStringProperty(LDIF_NAME_PROPERTY);
    }
}
