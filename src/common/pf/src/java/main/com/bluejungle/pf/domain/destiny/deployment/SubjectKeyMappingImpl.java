/*
 * Created on Dec 19, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.deployment;

import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle.ISubjectKeyMapping;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

import java.io.Serializable;

/**
 * Default implementation of the {@link ISubjectKeyMapping} insterface
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/SubjectKeyMappingImpl.java#1 $
 */

public class SubjectKeyMappingImpl implements ISubjectKeyMapping, Serializable {

    private static final long serialVersionUID = -4342060611974392909L;

    private Long id;
    private ISubjectType subjectType;
    private String uid;
    private String uidType;

    /**
     * Create an instance of SubjectKeyMappingImpl
     * 
     * @param id
     * @param subjectType
     * @param uid
     * @param uidType
     */
    public SubjectKeyMappingImpl(Long id, ISubjectType subjectType, String uid, String uidType) {
        super();
        this.id = id;
        this.subjectType = subjectType;
        this.uid = uid;
        this.uidType = uidType;
    }

    /**
     * @see IDeploymentBundle.ISubjectKeyMapping#getId()
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see IDeploymentBundle.ISubjectKeyMapping#getSubjectType()
     */
    public ISubjectType getSubjectType() {
        return this.subjectType;
    }

    /**
     * @see IDeploymentBundle.ISubjectKeyMapping#getUid()
     */
    public String getUid() {
        return this.uid;
    }

    /**
     * @see IDeploymentBundle.ISubjectKeyMapping#getUidType()
     */
    public String getUidType() {
        return this.uidType;
    }
}
