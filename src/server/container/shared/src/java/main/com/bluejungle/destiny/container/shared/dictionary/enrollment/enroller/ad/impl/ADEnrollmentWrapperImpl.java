/*
 * Created on Mar 29, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.RfcFilterEvaluator;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.utils.StringUtils;

/**
 * @author safdar
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/impl/ADEnrollmentImpl.java#1 $
 */
public class ADEnrollmentWrapperImpl extends BaseLDAPEnrollmentWrapper implements
        IADEnrollmentWrapper, ActiveDirectoryEnrollmentProperties {
    /*
     * Common configuration
     */
    private final String server;
    private final int port;
    private final String login;
    private final String password;
    private final String domainName;
    private final String[] subtreesToEnroll;
    private final String filter;
    private final String parentGUIDAttributeName;
    private final String isDeletedAttributeName;
    private final String lastKnownParentAttributeName;
    private final String[] allAttributesToRetrieve;
    private final boolean dirSyncEnabled;
    private final boolean isPagingEnabled;
    private final boolean alwaysTrustAD;
    private final String secureTransportMode;

    
    /*
     * Dynamic data:
     */
    private byte[] cookie;

    /**
     * Constructor
     * 
     * @param enrollment
     * @throws DictionaryException 
     * @throws EnrollmentFailedException 
     * @throws EnrollmentValidationException 
     */
    public ADEnrollmentWrapperImpl(IEnrollment enrollment, IDictionary dictionary)
            throws EnrollmentValidationException, DictionaryException {
        super(enrollment, dictionary);
        
        List<String> existingStrArrProperties = Arrays.asList(enrollment.getStrArrayPropertyNames());
        List<String> existingStrProperties = Arrays.asList(enrollment.getStrPropertyNames());
        server       = enrollment.getStrProperty(SERVER);
        port         = (int) enrollment.getNumProperty(PORT);
        secureTransportMode = enrollment.getStrProperty(SECURE_TRANSPORT_MODE);
        alwaysTrustAD = StringUtils.stringToBoolean(enrollment.getStrProperty(ALWAYS_TRUST_AD), false);
        login        = enrollment.getStrProperty(LOGIN);
        password     = enrollment.getStrProperty(PASSWORD);
        domainName   = enrollment.getDomainName();
    
        // make sure it is backward compatible
        dirSyncEnabled  = StringUtils.stringToBoolean(enrollment.getStrProperty(DIRSYNC_ENABLED), false);
        isPagingEnabled = StringUtils.stringToBoolean(enrollment.getStrProperty(PAGING_ENABLED), false);
        
        filter = existingStrProperties.contains(FILTER) 
                    ? enrollment.getStrProperty(FILTER) 
                    : null;
        
        subtreesToEnroll = existingStrArrProperties.contains(ROOTS)
                    ? enrollment.getStrArrayProperty(ROOTS)
                    : null;
        
        Set<String> attrsToRetrieveSet = new HashSet<String>();
                    
        // setup attributes related to DirSync Control
        if ( dirSyncEnabled ) {
            try {
                cookie = enrollment.getBinProperty(COOKIE);
            } catch (IllegalArgumentException e) {
                // the cookie is not set, set it now
                enrollment.setBinProperty(COOKIE, null);
            }
            parentGUIDAttributeName = enrollment.getStrProperty(PARENT_ID_ATTRIBUTE);
            isDeletedAttributeName = enrollment.getStrProperty(IS_DELETED_ATTRIBUTE);
            lastKnownParentAttributeName = enrollment.getStrProperty(LAST_PARENT_ATTRIBUTE);
            
            attrsToRetrieveSet.add(getParentGUIDAttributeName());
            attrsToRetrieveSet.add(getIsDeletedAttributeName());
        }else{
            parentGUIDAttributeName = null;
            isDeletedAttributeName = null;
            lastKnownParentAttributeName = null;
        }
        
        // Figure out the list of all attributes that we need from AD:
        if (enrollUsers()) {
            attrsToRetrieveSet.addAll(getSearchableAttributesForUsers().values());
            attrsToRetrieveSet.addAll(getAttributesFromFilter(getUserIdentification()));
        }
        if (enrollComputers()) {
            attrsToRetrieveSet.addAll(getSearchableAttributesForComputers().values());
            attrsToRetrieveSet.addAll(getAttributesFromFilter(getComputerIdentification()));
        }
        if (enrollApplications()) {
            attrsToRetrieveSet.addAll(getSearchableAttributesForApplications().values());
            attrsToRetrieveSet.addAll(getAttributesFromFilter(getApplicationIdentification()));
        }
        if (enrollContacts()) {
            attrsToRetrieveSet.addAll(getAttributesFromFilter(getContactIdentification()));
        }
        if (enrollGroups()) {
            attrsToRetrieveSet.addAll(getAttributesFromFilter(getGroupIdentification()));
            attrsToRetrieveSet.add(getMembershipAttributeName());
        }
        attrsToRetrieveSet.addAll(getAttributesFromFilter(getOtherIdentification()));
        attrsToRetrieveSet.addAll(getAttributesFromFilter(getStructureIdentification()));
        
        attrsToRetrieveSet.add(getObjectGUIDAttributeName());
//        attrsToRetrieveSet.add("uSNChanged");
        
        allAttributesToRetrieve = attrsToRetrieveSet.toArray(new String[attrsToRetrieveSet.size()]);

        // Test the connectivity:
        ADConnectionTester.testConnection(server, port, login, password, subtreesToEnroll, secureTransportMode, alwaysTrustAD);
    }
    
    private Set<String> getAttributesFromFilter(String filter) throws EnrollmentValidationException{
        return new RfcFilterEvaluator(filter).getAttributes();
    }
    
    
    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getServer()
     */
    public String getServer() {
        return server;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getPort()
     */
    public int getPort() {
        return port;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getLogin()
     */
    public String getLogin() {
        return login;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getDomainName()
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getParentGUIDAttributeName()
     */
    public String getParentGUIDAttributeName() {
        return parentGUIDAttributeName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getIsDeletedAttributeName()
     */
    public String getIsDeletedAttributeName() {
        return isDeletedAttributeName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getLastKnownParentAttributeName()
     */
    public String getLastKnownParentAttributeName() {
        return lastKnownParentAttributeName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getSubtreesToEnroll()
     */
    public String[] getSubtreesToEnroll() {
        return subtreesToEnroll;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getFilter()
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getAllAttributesToRetrieve()
     */
    public String[] getAllAttributesToRetrieve() {
        return allAttributesToRetrieve;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getCookie()
     */
    public byte[] getCookie() {
        return cookie;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#setCookie(byte[])
     */
    public void setCookie(byte[] cookie) {
        this.cookie = cookie;
        enrollment.setBinProperty(COOKIE, cookie);
    }
    
    /**
     * getter for dirSync Flag
     * @return true if dirSync is enabled
     */
    public boolean isDirSyncEnabled() {
        return dirSyncEnabled;
    }

    /**
     * getter for isPagingEnabled Flag
     * @return true if LDAP paging control is enabled
     */
    public boolean isPagingEnabled() {
        return isPagingEnabled;
    }

    /**
     * getter for alwaysTrustAD
     * @return true if we should always trust the AD server (i.e. we don't need
     * its cert in our truststore)
     */
    public boolean getAlwaysTrustAD() {
        return alwaysTrustAD;
    }
    
    /**
     * getter for what type of secure transport to use for the LDAP connection.
     */
    public String getSecureTransportMode() {
    	return secureTransportMode;
    }

}
