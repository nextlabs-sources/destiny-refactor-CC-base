package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) copyright 2006
 * by Blue Jungle Inc., Redwood City CA, Ownership remains
 * with Blue Jungle Inc, All rights reserved worldwide.
 *
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/IAgentPolicyAssembly.java#1 $
 */

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * Agent policy assembly Interface.
 */
public interface IAgentPolicyAssembly extends IEngineSubjectResolver {

    interface ISubjectInfo {
        Long getSubjectID();
        IEvalValue getSubjectGroups();
        void addDynamicAttributes(DynamicAttributes attr);
    }

    /**
     * Returns a string representation of a location with a given name
     * @param name location name
     * @return string representation of a location
     */
    Location getLocation(String name);

    /**
     * Given a subject key and the type, returns the subject information
     * that comes from the server side.
     *
     * @param uid the UID of the subject.
     * @param type the subject type.
     * @return the <code>ISubjectInfo</code> for the specified subject.
     */
    ISubjectInfo getSubjectInfo(String uid, ISubjectType type);

}