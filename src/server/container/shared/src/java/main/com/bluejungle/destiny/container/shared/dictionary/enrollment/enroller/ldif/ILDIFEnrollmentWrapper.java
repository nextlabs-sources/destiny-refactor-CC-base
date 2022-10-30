package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;

public interface ILDIFEnrollmentWrapper extends ILDAPEnrollmentWrapper {
    
    boolean isGroupMemberFromAllEnrollment();
    
    String getMemberAttributeKey();
    
}
