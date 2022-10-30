package com.bluejungle.pf.domain.destiny.subject;

import java.io.Serializable;

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.utils.ObjectHelper;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

// Copyright Blue Jungle, Inc.

/**
 * SubjectSpec is a base class for destiny's implmentation of ISubjectSpec
 * 
 * @author Sasha Vladimirov
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/subject/SubjectSpec.java#20 $
 */

public class SubjectSpec extends SpecBase implements IDSubjectSpec, Serializable {

    private final ISubjectType subjectType;

    /**
     * Constructor
     * 
     * @param id
     * @param name
     * @param description
     * @param type
     */
    public SubjectSpec(IDSpecManager manager, Long id, String name, String description, SubjectType type, DevelopmentStatus status, IPredicate pred, boolean hidden) {
        super(manager, type.getSpecType(), id, name, description, status, pred, hidden);
        this.subjectType = type;
    }

    /**
     * @see com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec#getSubjectType()
     */
    public ISubjectType getSubjectType() {
        return subjectType;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        boolean valueToReturn = false;
        if (obj == this) {
            valueToReturn = true;
        } else if ((obj != null) && (obj instanceof SubjectSpec)) {
            SubjectSpec objectToTest = (SubjectSpec) obj;
            if ( ObjectHelper.nullSafeEquals(getName(), objectToTest.getName())
              && ObjectHelper.nullSafeEquals(getSubjectType(), objectToTest.getSubjectType())) {
                valueToReturn = true;
            }
        }

        return valueToReturn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ObjectHelper.nullSafeHashCode(getName(), getSubjectType());
    }

}
