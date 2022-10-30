package com.bluejungle.pf.domain.destiny.subject;

/*
 * Created on Jul 1, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/subject/AgentSubject.java#1 $:
 */

public class AgentSubject extends Subject implements IMSubject {
    private static final long serialVersionUID = 1L;

    private final IEvalValue groups;
    private final DynamicAttributes attributes;

    /**
     * Constructor
     * @param uid
     * @param id
     * @param subjectType
     */
    public AgentSubject(String uid, String name, Long id, ISubjectType subjectType, IEvalValue groups, DynamicAttributes attributes) {
        super(uid, uid, name, id, subjectType);
        this.groups = groups;
        if (attributes != null) {
            this.attributes = attributes;
        } else {
            this.attributes = DynamicAttributes.EMPTY;
        }
    }

    /**
     * Returns an <code>EvalValue</code> representing the groups
     * in which the specified subject participates.
     */
    public IEvalValue getGroups() {
        return groups;
    }

    /**
     * @see Subject#getAttribute(String)
     */
    @Override
    public IEvalValue getAttribute(String name) {
        return attributes.get(name);
    }
    
    /**
     * @see Subject#getEntrySet()
     */
    @Override
    public Set<Map.Entry<String, IEvalValue>> getEntrySet() {
        return Collections.unmodifiableSet(attributes.entrySet());
    }

    /**
     * @see IMSubject#setAttribute(String, IEvalValue)
     */
    public void setAttribute(String name, IEvalValue value) {
        attributes.put(name, value);
    }

    /**
     * Subjects with non-empty dynamic attributes are considered non-cacheable.
     *
     * @see Subject#isCacheable()
     */
    @Override
    public boolean isCacheable() {
        return attributes.isEmpty();
    }

}
