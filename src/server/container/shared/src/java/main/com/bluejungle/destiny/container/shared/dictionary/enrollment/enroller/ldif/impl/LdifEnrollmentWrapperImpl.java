/*
 * Created on April 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BaseLDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.ILDIFEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.utils.StringUtils;

/**
 * @author atian 
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ldif/impl/LdifEnrollmentWrapperImpl.java#1 $
 */

/**
 * This is the implementation class of ILdifEnrollmentWrapper interface
 * We have 3 main functions implemented in this class
 * 1. Initialization
 *    get the LDIF File name from LDIF Enrollment configuration, open the file 
 *    Initialize LDIF File Reader
 * 2. resolve external names  
 *    from the given property mapping, we lookup the external names and map it
 *    a corresponding IElementField
 * 3. fetch elements from LDIF file and store the elements into dictionary 
 *    If there is an error occurred during the process, the whole transaction 
 *    will be rollback. If there is no error, the transaction will be commited.
 */

public class LdifEnrollmentWrapperImpl extends BaseLDAPEnrollmentWrapper implements
        LdifEnrollmentProperties, ILDIFEnrollmentWrapper {
    private final String ldifFileName;
    
    private final boolean groupMemberFromAllEnrollment;
    
    private final String memberAttributeKey;
    
    public LdifEnrollmentWrapperImpl(IEnrollment enrollment, IDictionary dictionary)
            throws EnrollmentValidationException, DictionaryException {
        super(enrollment, dictionary);

        ldifFileName = enrollment.getStrProperty(LDIF_NAME_PROPERTY);
        
        groupMemberFromAllEnrollment = StringUtils.stringToBoolean(enrollment.getStrProperty(GROUP_MEMBER_FROM_ALL_ENROLLMENT), false);
        memberAttributeKey = enrollment.getStrProperty(MEMBER_ATTRIBUTE_KEY);
    }
    
    public String getLdifFileName() {
        return ldifFileName;
    }

    @Override
    public boolean isGroupMemberFromAllEnrollment() {
        return groupMemberFromAllEnrollment;
    }

    @Override
    public String getMemberAttributeKey() {
        return memberAttributeKey;
    }
}
