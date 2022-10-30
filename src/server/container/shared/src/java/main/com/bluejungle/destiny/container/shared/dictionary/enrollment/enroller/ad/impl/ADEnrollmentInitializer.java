/*
 * Created on Mar 19, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentInitializer;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.utils.ArrayUtils;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/impl/ADEnrollmentInitializer.java#1 $
 */

public class ADEnrollmentInitializer extends BaseLDAPEnrollmentInitializer implements ActiveDirectoryEnrollmentProperties {
    public ADEnrollmentInitializer(IEnrollment enrollment, Map<String, String[]> properties,
                                   IDictionary dictionary) {
        super(enrollment, properties, dictionary, LogFactory.getLog(ADEnrollmentInitializer.class));
    }

    /**
     * This method sets up the enrollment record with the provided enrollment
     * properties. It determines the appropriate types of the properties and
     * sets them on the enrollment record according.
     * 
     * @param enrollment
     * @param properties
     * @throws DictionaryException 
     */
    public void setup() throws EnrollmentValidationException, DictionaryException {
        // Setup the common properties shared by all LDAP:
        super.setup();

        // Properties related to Active Directory:
        String server = setStringProperty(SERVER);
        int port = (int) setLongProperty(PORT);
        String login = setStringProperty(LOGIN);
        String password = setStringProperty(PASSWORD);
        boolean isDirSyncEnable = setBooleanProperty(DIRSYNC_ENABLED);
        String secureTransactionMode = setStringProperty(SECURE_TRANSPORT_MODE, null);
        boolean alwaysTrustAD = setBooleanProperty(ALWAYS_TRUST_AD, false);
        setBooleanProperty(PAGING_ENABLED);

        String[] subtreesToEnroll = null;
        if (properties.containsKey(ROOTS)) {
            subtreesToEnroll = properties.get(ROOTS);
            if ((subtreesToEnroll == null) || (subtreesToEnroll.length == 0)) {
                throw new EnrollmentValidationException("enrollment subtree roots not defined");
            }
            for (int i = 0; i < subtreesToEnroll.length; i++) {
                subtreesToEnroll[i] = subtreesToEnroll[i].trim().toLowerCase();
            }
            enrollment.setStrArrayProperty(ROOTS, subtreesToEnroll);
        }

        String domainRootDN = subtreesToEnroll[0];

        setLDAPProperty(FILTER, MATCH_ALL_LDAP_FILTER);
		
        if (isDirSyncEnable) {
            setStringProperty(PARENT_ID_ATTRIBUTE);
            setStringProperty(IS_DELETED_ATTRIBUTE);
            setStringProperty(LAST_PARENT_ATTRIBUTE);
        }

        if(log.isTraceEnabled()){
            StringBuilder sb = new StringBuilder();
            sb.append("Testing ad connection:\n")
                .append("server: ").append(server)
                .append("port: ").append(port)
                .append("login: ").append(login)
                .append("password: ").append(password != null ? "<HIDDEN>" : "<NULL>")
                .append("domainRootDN: ").append(domainRootDN)
                .append("subtreesToEnroll: ").append(ArrayUtils.asString(subtreesToEnroll, "\n", "          "))
                .append("secure.transaction.mode: ").append(secureTransactionMode)
                .append("always.trust.ad: ").append(alwaysTrustAD ? "yes" : "no");
        }
		
        //Test the connectivity:
        // the rootDn is not using
        ADConnectionTester.testConnection(server, port, login, password, subtreesToEnroll, secureTransactionMode, alwaysTrustAD);
		
        log.info("The connection to active directory, " + server + ", is successful.");
    }	
}
