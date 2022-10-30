/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.subject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;
import com.bluejungle.pf.engine.destiny.IAgentPolicyAssembly;

/**
 * @author sasha
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/subject/AgentSubjectManager.java#1 $:
 */

public class AgentSubjectManager extends DefaultSubjectManager implements ILogEnabled {

    /**
     * Prevent NPEs
     */
    private IAgentPolicyAssembly assembly = new IAgentPolicyAssembly() {
        public Location getLocation(String name) {
            warn();
            return null;
        }
        public ISubjectInfo getSubjectInfo(String uid, ISubjectType type) {
            warn();
            return null;
        }
        public boolean existsSubject(String uid, ISubjectType type) {
            warn();
            return false;
        }
        public IEvalValue getGroupsForSubject(String uid, ISubjectType type) {
            warn();
            return null;
        }
        private void warn() {
            if (log != null && log.isWarnEnabled()) {
                log.warn("Policy bundle is not set");
            }
        }
    };

    private final Map<SubjectType,IEvalValue> unknownSubjectGroups =
        new HashMap<SubjectType,IEvalValue>();

    private final Map<SubjectType,Long> unknownSubjectIds =
        new HashMap<SubjectType,Long>();

    private Log log;

    private final Map<String,String> appFingerprintMap = new HashMap<String,String>();

    private IOSWrapper osWrapper = (IOSWrapper)ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    public static final ComponentInfo COMP_INFO = new ComponentInfo(IDSubjectManager.class.getName(), AgentSubjectManager.class.getName(), IDSubjectManager.class.getName(), LifestyleType.SINGLETON_TYPE);

    public void setAgentPolicyAssembly(IAgentPolicyAssembly assembly) {
        this.assembly = assembly;
        addUnknown(IDeploymentBundle.UID_OF_UNKNOWN_APPLICATION, SubjectType.APP);
        addUnknown(IDeploymentBundle.UID_OF_UNKNOWN_USER, SubjectType.USER);
        addUnknown(IDeploymentBundle.UID_OF_UNKNOWN_HOST, SubjectType.HOST);
    }

    public IDSubject getSubject(String uid, ISubjectType type, DynamicAttributes attributes) {
        // We handle some special cases in here and then call the real function to do the work

        // FIXME This code should be removed when PEP starts sending us the application fingerprint
        String name = (attributes!=null) ? attributes.getString("name") : null;

        if (type == SubjectType.APP && (uid == null || uid.length() == 0)) {
            if (attributes != null) {
                uid = getApplicationFingerprint(name);
            } else {
                uid = "";
            }
        }

        if (name == null) {
            name = uid;
        }
        
        if (type == SubjectType.USER || type == SubjectType.RECIPIENT) {
            // The name and id don't always match (the id will be that of the logged in user.  The name will
            // be that of the person performing the action).  Looking up by name doesn't always work, however.
            IDSubject subj = getSubject(name, name, type, attributes);
            
            if (subj.getId() != IHasId.UNKNOWN_ID &&
                subj.getId() != unknownSubjectIds.get(type)) {
                return subj;
            } 
        }
        
        return getSubject(uid, name, type, attributes);
    }

    private IDSubject getSubject(String uid, String name, ISubjectType type, DynamicAttributes attributes) {
        // Not sure about the best way to do this. If the subject type is RECIPIENT then
        // we want to look it up in the bundle as if it was USER. We can make the change here or in
        // assembly.
        ISubjectType underlyingType = (type == SubjectType.RECIPIENT) ? SubjectType.USER : type;
        
        IAgentPolicyAssembly.ISubjectInfo subjInfo = assembly.getSubjectInfo(uid, underlyingType);
        IEvalValue groups = null;
        Long subjId = null;
        // If subject info exists, get group/policy info from there
        if (subjInfo != null) {
            groups = subjInfo.getSubjectGroups();
            subjId = subjInfo.getSubjectID();
            subjInfo.addDynamicAttributes(attributes);
        }
        // If groups/policies are null, treat them as unknowmn
        if (groups == null) {
            groups = unknownSubjectGroups.get(underlyingType);
        }
        if (subjId == null) {
            subjId = unknownSubjectIds.get(underlyingType);
        }
        // If unknown groups/policies are not defined, use defaults
        if (groups == null) {
            groups = IEvalValue.EMPTY;
        }
        if (subjId == null) {
            subjId = IHasId.UNKNOWN_ID;
        }
        return new AgentSubject(
            uid
        ,   name
        ,   subjId
        ,   type
        ,   groups
        ,   attributes
        );
    }

    /**
     * @see IDSubjectManager#getLocation(String)
     */
    public Location getLocation(String name) {
        return assembly.getLocation(name);
    }

    private static final String EMPTY_FINGERPRINT = "";
    /**
     * Returns an application fingerprint. Uses cache if it is available,
     * otherwise computes the fingerprint using the IOSWrapper.
     *
     * @param fullName full name of the executable.
     * @return The fingerprint of the given application.
     */
    private String getApplicationFingerprint(String fullName) {
        if (fullName == null) {
            return EMPTY_FINGERPRINT;
        }
        String res;
        if (appFingerprintMap.containsKey(fullName)) {
            res = appFingerprintMap.get(fullName);
        } else {
            res = osWrapper.getAppInfo(fullName);

            if (res == null || res.length() == 0) {
                log.warn("Application '" + fullName+"' is not enrolled.");
                res = EMPTY_FINGERPRINT;
            }
            appFingerprintMap.put(fullName, res);
        }

        return res;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    private void addUnknown(String uid, SubjectType type) {
        IAgentPolicyAssembly.ISubjectInfo info = assembly.getSubjectInfo(uid, type);
        if (info != null) {
            unknownSubjectGroups.put(type, info.getSubjectGroups());
            unknownSubjectIds.put(type, info.getSubjectID());
        }
    }

}
