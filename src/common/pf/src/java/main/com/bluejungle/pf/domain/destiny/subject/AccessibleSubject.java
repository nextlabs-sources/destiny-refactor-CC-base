/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/subject/AccessibleSubject.java#1 $
 */

package com.bluejungle.pf.domain.destiny.subject;

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * @author sergey
 *
 */
public class AccessibleSubject extends Subject implements IAccessibleSubject {

    private static final long serialVersionUID = 1L;

    /** AccessGroups this subject belongs to */
    private IEvalValue accessGroups = IEvalValue.NULL;

    /**
     * Constructor
     * @param uid
     * @param name
     * @param id
     * @param type
     */
    public AccessibleSubject(String uid, String uniqueName, String name, Long id, IEvalValue accessGroups, ISubjectType subjectType) {
        super( uid, uniqueName, name, id, subjectType);
        this.accessGroups = accessGroups;
    }

    /**
     * @see IAccessibleSubject#getAccessGroups()
     */
    public IEvalValue getAccessGroups() {
        return accessGroups;
    }

}
